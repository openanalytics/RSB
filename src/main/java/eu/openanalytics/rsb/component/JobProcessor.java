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

import javax.security.auth.login.LoginException;

import org.eclipse.core.runtime.CoreException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RObject;
import de.walware.rj.servi.RServi;
import de.walware.rj.servi.RServiUtil;
import de.walware.rj.services.FunctionCall;
import eu.openanalytics.rsb.message.FunctionCallJob;

/**
 * Processes job requests and builds job responses.
 * 
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
@Component("jobProcessor")
public class JobProcessor extends AbstractConfigurable {
    @Autowired
    private JmsTemplate jmsTemplate;

    private final String STRING;

    public JobProcessor() throws UnknownHostException {
        STRING = "rsb@" + InetAddress.getLocalHost().getHostName();
    }

    public void process(final FunctionCallJob functionCallJob) throws LoginException, CoreException {
        final long startTime = System.currentTimeMillis();

        final URI rserviPoolAddress = getRServiPoolUri(functionCallJob.getApplicationName());

        // don't catch RServi pool here so the error is propagated and the job can be retried
        final RServi rServi = RServiUtil.getRServi(rserviPoolAddress.toString(), STRING);
        try {
            // FIXME build result
            final String result = callFunctionOnR(rServi, functionCallJob.getFunctionName(), functionCallJob.getArgument());
            getLogger().info("========= " + result);

            final long processTime = System.currentTimeMillis() - startTime;

            // FIXME call statisticsHandler
            // statisticsHandler.storeJobStatistics(appName, jobId, new GregorianCalendar(),
            // processTime, rserviPoolAddress)
            getLogger().info(
                    String.format("Successfully processed job: %s on RServi: %s in: %dms", functionCallJob.getJobId(), rserviPoolAddress,
                            processTime));
        } catch (final Throwable t) {
            // catch wide to try to disrupt the main flow as less as possible
            final long processTime = System.currentTimeMillis() - startTime;
            getLogger().error(
                    String.format("Failed to process job: %s on RServi: %s in: %dms", functionCallJob.getJobId(), rserviPoolAddress,
                            processTime), t);
        } finally {
            rServi.close();
        }
    }

    private URI getRServiPoolUri(final String applicationName) {
        final URI applicationRserviPoolUri = getConfiguration().getApplicationSpecificRserviPoolUris().get(applicationName);
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
