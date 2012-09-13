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

package eu.openanalytics.rsb.rservi;

import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool.Config;
import org.apache.commons.pool.impl.GenericKeyedObjectPoolFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.rj.data.RObject;
import de.walware.rj.data.RReference;
import de.walware.rj.servi.RServi;
import de.walware.rj.servi.RServiUtil;
import de.walware.rj.services.FunctionCall;
import de.walware.rj.services.RGraphicCreator;
import de.walware.rj.services.RPlatform;
import eu.openanalytics.rsb.config.Configuration;

/**
 * Provides RServi connection over RMI.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class RmiRServiInstanceProvider implements RServiInstanceProvider
{
    private static class RServiPoolKey
    {
        private final String address, clientId;

        RServiPoolKey(final String address, final String clientId)
        {
            this.address = address;
            this.clientId = clientId;
        }

        String getAddress()
        {
            return address;
        }

        String getClientId()
        {
            return clientId;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this);
        }

        @Override
        public int hashCode()
        {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public boolean equals(final Object obj)
        {
            return EqualsBuilder.reflectionEquals(this, obj);
        }
    }

    private static class PooledRServiWrapper implements RServi
    {
        private final KeyedObjectPool<RServiPoolKey, PooledRServiWrapper> rServiPool;
        private final RServiPoolKey key;
        private final RServi rServi;

        PooledRServiWrapper(final KeyedObjectPool<RServiPoolKey, PooledRServiWrapper> rServiPool,
                            final RServiPoolKey key,
                            final RServi rServi)
        {
            this.rServiPool = rServiPool;
            this.key = key;
            this.rServi = rServi;
        }

        public void close() throws CoreException
        {
            try
            {
                rServiPool.returnObject(key, this);
            }
            catch (final Exception e)
            {
                LOGGER.error("Failed to return object to pool", e);
            }
        }

        public void destroy() throws CoreException
        {
            rServi.close();
        }

        public RPlatform getPlatform()
        {
            return rServi.getPlatform();
        }

        public void evalVoid(final String expression, final IProgressMonitor monitor) throws CoreException
        {
            rServi.evalVoid(expression, monitor);
        }

        public RObject evalData(final String expression, final IProgressMonitor monitor) throws CoreException
        {
            return rServi.evalData(expression, monitor);
        }

        public RObject evalData(final String expression,
                                final String factoryId,
                                final int options,
                                final int depth,
                                final IProgressMonitor monitor) throws CoreException
        {
            return rServi.evalData(expression, factoryId, options, depth, monitor);
        }

        public RObject evalData(final RReference reference, final IProgressMonitor monitor)
            throws CoreException
        {
            return rServi.evalData(reference, monitor);
        }

        public RObject evalData(final RReference reference,
                                final String factoryId,
                                final int options,
                                final int depth,
                                final IProgressMonitor monitor) throws CoreException
        {
            return rServi.evalData(reference, factoryId, options, depth, monitor);
        }

        public void assignData(final String expression, final RObject data, final IProgressMonitor monitor)
            throws CoreException
        {
            rServi.assignData(expression, data, monitor);
        }

        public void uploadFile(final InputStream in,
                               final long length,
                               final String fileName,
                               final int options,
                               final IProgressMonitor monitor) throws CoreException
        {
            rServi.uploadFile(in, length, fileName, options, monitor);
        }

        public void downloadFile(final OutputStream out,
                                 final String fileName,
                                 final int options,
                                 final IProgressMonitor monitor) throws CoreException
        {
            rServi.downloadFile(out, fileName, options, monitor);
        }

        public byte[] downloadFile(final String fileName, final int options, final IProgressMonitor monitor)
            throws CoreException
        {
            return rServi.downloadFile(fileName, options, monitor);
        }

        public FunctionCall createFunctionCall(final String name) throws CoreException
        {
            return rServi.createFunctionCall(name);
        }

        public RGraphicCreator createRGraphicCreator(final int options) throws CoreException
        {
            return rServi.createRGraphicCreator(options);
        }
    }

    private final static Log LOGGER = LogFactory.getLog(RmiRServiInstanceProvider.class);

    @Resource
    private Configuration configuration;

    private KeyedObjectPool<RServiPoolKey, PooledRServiWrapper> rServiPool;

    @PostConstruct
    public void initialize()
    {
        final Config config = configuration.getRServiClientPoolConfig();
        if (config != null)
        {
            initializeRServiClientPool(config);
        }
    }

    public void initializeRServiClientPool(final Config config)
    {
        final KeyedPoolableObjectFactory<RServiPoolKey, PooledRServiWrapper> factory = new BaseKeyedPoolableObjectFactory<RServiPoolKey, PooledRServiWrapper>()
        {
            @Override
            public PooledRServiWrapper makeObject(final RServiPoolKey key) throws Exception
            {
                final RServi rServi = RServiUtil.getRServi(key.getAddress(), key.getClientId());
                return new PooledRServiWrapper(rServiPool, key, rServi);
            }

            @Override
            public void destroyObject(final RServiPoolKey key, final PooledRServiWrapper rservi)
                throws Exception
            {
                rservi.destroy();
            }
        };
        rServiPool = new GenericKeyedObjectPoolFactory<RServiPoolKey, PooledRServiWrapper>(factory, config).createPool();
        LOGGER.info("RServi pool instantiated and configured with: "
                    + ToStringBuilder.reflectionToString(config));
    }

    @PreDestroy
    public void terminate() throws Exception
    {
        if (rServiPool != null)
        {
            rServiPool.close();
            LOGGER.info("RServi pool destroyed");
        }
    }

    public RServi getRServiInstance(final String address,
                                    final String clientId,
                                    final PoolingStrategy poolingStrategy) throws Exception
    {
        if ((rServiPool == null) || (poolingStrategy == PoolingStrategy.NEVER))
        {
            return RServiUtil.getRServi(address, clientId);
        }
        else
        {
            return rServiPool.borrowObject(new RServiPoolKey(address, "pooled-" + clientId));
        }
    }
}
