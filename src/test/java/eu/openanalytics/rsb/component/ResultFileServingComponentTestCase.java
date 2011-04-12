package eu.openanalytics.rsb.component;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.StreamingOutput;

import org.apache.activemq.util.ByteArrayOutputStream;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;

import eu.openanalytics.rsb.config.Configuration;

public class ResultFileServingComponentTestCase {

    private static final String MISSING_RSB_RESULT = "_missing_rsb_result_";

    private HttpServletResponse httpServletResponse;
    private ResultFileServingComponent component;
    private File tempDir;
    private String testApplicationName;
    private String testResult;
    private String testResultPayload;

    @Before
    public void initialize() {
        tempDir = new File(System.getProperty("java.io.tmpdir"));
        testApplicationName = tempDir.getName();
        testResult = "rsb-" + UUID.randomUUID().toString() + ".tst";
        testResultPayload = RandomStringUtils.randomAlphanumeric(25 + RandomUtils.nextInt(25));

        final Configuration configuration = mock(Configuration.class);
        when(configuration.getRsbResultsDirectory()).thenReturn(tempDir.getParentFile());

        component = new ResultFileServingComponent();
        component.setConfiguration(configuration);

        httpServletResponse = mock(HttpServletResponse.class);
    }

    @Test(expected = WebApplicationException.class)
    public void getResultNotFound() throws IOException {
        component.getResult(testApplicationName, MISSING_RSB_RESULT, httpServletResponse);
    }

    @Test
    public void getResult() throws IOException {
        createTestResultFile();

        final StreamingOutput result = component.getResult(testApplicationName, testResult, httpServletResponse);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        result.write(baos);

        assertThat(baos.toByteArray(), is(testResultPayload.getBytes()));

        verify(httpServletResponse).addHeader(HttpHeaders.ETAG, ResultFileServingComponent.getEtag(testApplicationName, testResult));
    }

    @Test(expected = WebApplicationException.class)
    public void getResultMetaNotFound() {
        component.getResultMeta(testApplicationName, MISSING_RSB_RESULT, httpServletResponse);
    }

    @Test
    public void getResultMeta() throws IOException {
        final File testResultFile = createTestResultFile();

        component.getResultMeta(testApplicationName, testResult, httpServletResponse);

        verify(httpServletResponse).addHeader(HttpHeaders.CONTENT_LENGTH, Long.toString(testResultFile.length()));
        verify(httpServletResponse).addHeader(HttpHeaders.ETAG, ResultFileServingComponent.getEtag(testApplicationName, testResult));
    }

    private File createTestResultFile() throws IOException {
        final File testResultFile = new File(tempDir, testResult);
        testResultFile.deleteOnExit();
        FileCopyUtils.copy(testResultPayload, new FileWriter(testResultFile));
        return testResultFile;
    }
}
