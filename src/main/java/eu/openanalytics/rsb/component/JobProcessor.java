/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of OpenAnalytics BVBA, 2010-2011
 *
 *   ===========================================================================
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.openanalytics.rsb.component;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.security.auth.login.LoginException;

import org.eclipse.core.runtime.CoreException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RObject;
import de.walware.rj.servi.RServi;
import de.walware.rj.services.FunctionCall;
import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.message.AbstractFunctionCallJob;
import eu.openanalytics.rsb.message.AbstractFunctionCallResult;
import eu.openanalytics.rsb.rservi.RServiInstanceProvider;
import eu.openanalytics.rsb.stats.JobStatisticsHandler;
import eu.openanalytics.rsb.stats.NoopJobStatisticsHandler;

/**
 * Processes job requests and builds job responses.
 * 
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
@Component("jobProcessor")
public class JobProcessor extends AbstractComponent {
    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private RServiInstanceProvider rServiInstanceProvider;

    private final String rServiClientId;

    public JobProcessor() throws UnknownHostException {
        rServiClientId = "rsb@" + InetAddress.getLocalHost().getHostName();
    }

    @PreDestroy
    public void destroyJobStatisticsHandler() {
        getJobStatisticsHandler().destroy();
    }

    private JobStatisticsHandler getJobStatisticsHandler() {
        return getConfiguration().getJobStatisticsHandler() == null ? NoopJobStatisticsHandler.INSTANCE : getConfiguration()
                .getJobStatisticsHandler();
    }

    public void process(final AbstractFunctionCallJob<AbstractFunctionCallResult> functionCallJob) throws LoginException, CoreException {
        AbstractFunctionCallResult result = null;
        final long startTime = System.currentTimeMillis();
        final URI rserviPoolAddress = getRServiPoolUri(functionCallJob.getApplicationName());

        // don't catch RServi pool here so the error is propagated and the job can be retried
        final RServi rServi = rServiInstanceProvider.getRServiInstance(rserviPoolAddress.toString(), rServiClientId);

        try {
            final String resultPayload = callFunctionOnR(rServi, functionCallJob.getFunctionName(), functionCallJob.getArgument());
            result = functionCallJob.buildResult(true, resultPayload);

            final long processTime = System.currentTimeMillis() - startTime;

            getJobStatisticsHandler().storeJobStatistics(functionCallJob.getApplicationName(), functionCallJob.getJobId(),
                    new GregorianCalendar(), processTime, rserviPoolAddress.toString());

            getLogger().info(
                    String.format("Successfully processed %s %s on %s in %dms", functionCallJob.getType(), functionCallJob.getJobId(),
                            rserviPoolAddress, processTime));
        } catch (final Throwable t) {
            // catch wide to prevent disrupting the main flow
            final long processTime = System.currentTimeMillis() - startTime;
            getLogger().error(
                    String.format("Failed to process %s %s on %s in %dms", functionCallJob.getType(), functionCallJob.getJobId(),
                            rserviPoolAddress, processTime), t);
            result = functionCallJob.buildResult(false, t.getMessage());
        } finally {
            rServi.close();

            if (result != null) {
                final AbstractFunctionCallResult finalResult = result;
                jmsTemplate.send(Util.getResultsQueueName(functionCallJob.getApplicationName()), new MessageCreator() {
                    public Message createMessage(final Session session) throws JMSException {
                        return session.createObjectMessage(finalResult);
                    }
                });
            }

            functionCallJob.destroy();
        }
    }

    // exposed for unit testing
    void setJmsTemplate(final JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    void setRServiInstanceProvider(final RServiInstanceProvider rServiInstanceProvider) {
        this.rServiInstanceProvider = rServiInstanceProvider;
    }

    URI getRServiPoolUri(final String applicationName) {
        final Map<String, URI> applicationSpecificRserviPoolUris = getConfiguration().getApplicationSpecificRserviPoolUris();

        if (applicationSpecificRserviPoolUris == null) {
            return getConfiguration().getDefaultRserviPoolUri();
        }

        final URI applicationRserviPoolUri = applicationSpecificRserviPoolUris.get(applicationName);

        return applicationRserviPoolUri == null ? getConfiguration().getDefaultRserviPoolUri() : applicationRserviPoolUri;
    }

    private static String callFunctionOnR(final RServi rServi, final String functionName, final String argument) throws CoreException {
        final FunctionCall functionCall = rServi.createFunctionCall(functionName);
        functionCall.addChar(argument);
        final RObject result = functionCall.evalData(null);
        if (!RDataUtil.isSingleString(result)) {
            throw new RuntimeException("Unexpected return value for function: " + functionName);
        }

        return result.getData().getChar(0);
    }
}
