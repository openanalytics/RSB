/*
 * R Service Bus
 * 
 * Copyright (c) Copyright of Open Analytics NV, 2010-2020
 * 
 * ===========================================================================
 * 
 * This file is part of R Service Bus.
 * 
 * R Service Bus is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 * 
 * You should have received a copy of the Apache License
 * along with R Service Bus.  If not, see <http://www.apache.org/licenses/>.
 */

package eu.openanalytics.rsb.component;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Response;

import org.eclipse.statet.rj.data.RObject;
import org.eclipse.statet.rj.data.RStore;
import org.eclipse.statet.rj.data.impl.RInteger32Store;
import org.eclipse.statet.rj.servi.RServi;
import org.eclipse.statet.rj.services.FunctionCall;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.rest.types.NodeInformation;
import eu.openanalytics.rsb.rservi.RServiInstanceProvider;
import eu.openanalytics.rsb.rservi.RServiInstanceProvider.PoolingStrategy;

/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@RunWith(MockitoJUnitRunner.class)
public class SystemHealthResourceTestCase
{

    private SystemHealthResource systemHealthResource;

    @Mock
    private Configuration configuration;
    @Mock
    private RServiInstanceProvider rServiInstanceProvider;

    @Before
    public void prepareTest() throws URISyntaxException
    {
        systemHealthResource = new SystemHealthResource();
        systemHealthResource.setConfiguration(configuration);
        systemHealthResource.setRServiInstanceProvider(rServiInstanceProvider);

        final URI defaultPoolUri = new URI("fake://default");
        when(configuration.getDefaultRserviPoolUri()).thenReturn(defaultPoolUri);
    }

    @Test
    public void getInfo() throws Exception
    {
        final NodeInformation info = systemHealthResource.getInfo(mock(ServletContext.class));

        assertThat(info, is(notNullValue()));
    }

    @Test
    public void defaultCheck() throws Exception
    {
        final RServi rServi = mock(RServi.class);
        when(rServiInstanceProvider.getRServiInstance(anyString(), anyString(), eq(PoolingStrategy.NEVER))).thenReturn(
            rServi);

        final Response checkResult = systemHealthResource.check();

        assertThat(checkResult.getStatus(), is(200));
        assertThat(checkResult.getEntity().toString(), is("OK"));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void happyCheck() throws Exception
    {
        final RServi rServi = mock(RServi.class);
        when(rServiInstanceProvider.getRServiInstance(anyString(), anyString(), eq(PoolingStrategy.NEVER))).thenReturn(
            rServi);

        final FunctionCall functionCall = mock(FunctionCall.class);
        when(rServi.createFunctionCall("sum")).thenReturn(functionCall);

        final RObject result = mock(RObject.class);
        when(functionCall.evalData(null)).thenReturn(result);
        when(result.getData()).thenReturn((RStore) new RInteger32Store(new int[]{3}));

        systemHealthResource.verifyNodeHealth();
        final Response checkResult = systemHealthResource.check();

        assertThat(checkResult.getStatus(), is(200));
        assertThat(checkResult.getEntity().toString(), is("OK"));
    }

    @Test
    public void unhappyCheck() throws Exception
    {
        when(rServiInstanceProvider.getRServiInstance(anyString(), anyString(), eq(PoolingStrategy.NEVER))).thenThrow(
            new RuntimeException("simulated RServi provider issue"));

        systemHealthResource.verifyNodeHealth();
        final Response checkResult = systemHealthResource.check();

        assertThat(checkResult.getStatus(), is(500));
        assertThat(checkResult.getEntity().toString(), is("ERROR"));
    }
}
