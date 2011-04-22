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
package eu.openanalytics.rsb;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.activemq.util.ByteArrayInputStream;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.jitr.Jitr;
import org.jitr.annotation.BaseUri;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.StartsWith;
import org.springframework.util.FileCopyUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.WebClient;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpException;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

import eu.openanalytics.httpunit.DeleteMethodWebRequest;

/**
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
@RunWith(Jitr.class)
public class RestITCase extends XMLTestCase {
    @BaseUri
    private String baseUri;

    private String restJobsUri;
    private String restResultsUri;

    @Before
    public void before() {
        restJobsUri = baseUri + "api/rest/jobs";
        restResultsUri = baseUri + "api/rest/results";

        final Map<String, String> m = new HashMap<String, String>();
        m.put("rsb", "http://rest.rsb.openanalytics.eu/types");

        final NamespaceContext ctx = new SimpleNamespaceContext(m);
        XMLUnit.setXpathNamespaceContext(ctx);
    }

    @Test
    public void jobsBadMethod() throws Exception {
        doTestUnsupportedDeleteMethod(restJobsUri);
    }

    @Test
    public void jobsBadApplicationName() throws Exception {
        final WebConversation wc = createNewWebConversation();
        final WebRequest request = new PostMethodWebRequest(restJobsUri, new ByteArrayInputStream("ignored".getBytes()),
                "application/xml");
        request.setHeaderField("X-RSB-Application-Name", ":bad_app$name!");

        try {
            wc.sendRequest(request);
            fail("an exception should have been raised");
        } catch (final HttpException he) {
            assertEquals(400, he.getResponseCode());
        }
    }

    @Test
    public void jobsBadContentType() throws Exception {
        final WebConversation wc = createNewWebConversation();
        final WebRequest request = new PostMethodWebRequest(restJobsUri, new ByteArrayInputStream("ignored".getBytes()),
                "application/unsupported");
        request.setHeaderField("X-RSB-Application-Name", "myApp");

        try {
            wc.sendRequest(request);
            fail("an exception should have been raised");
        } catch (final HttpException he) {
            assertEquals(415, he.getResponseCode());
        }
    }

    @Test
    public void noResultForApplication() throws Exception {
        final WebConversation wc = createNewWebConversation();
        final WebResponse res = wc.sendRequest(new GetMethodWebRequest(restResultsUri + "/fooAppName"));
        assertEquals(200, res.getResponseCode());
        assertEquals("application/vnd.rsb+xml", res.getHeaderField("Content-Type"));

        final String xml = res.getText();
        assertXpathExists("/rsb:results", xml);
        assertXpathNotExists("/rsb:results/rsb:result", res.getText());
    }

    @Test
    public void noResultForApplicationAsJson() throws Exception {
        final WebConversation wc = createNewWebConversation();
        final GetMethodWebRequest request = new GetMethodWebRequest(restResultsUri + "/fooAppName");
        request.setHeaderField("Accept", "application/vnd.rsb+json");
        final WebResponse res = wc.sendRequest(request);
        assertEquals(200, res.getResponseCode());
        assertEquals("application/vnd.rsb+json", res.getHeaderField("Content-Type"));
        assertEquals("{\"results\":\"\"}", res.getText());
    }

    @Test
    public void resultNotFound() throws Exception {
        final WebConversation wc = createNewWebConversation();

        try {
            wc.sendRequest(new GetMethodWebRequest(restResultsUri + "/fooAppName/fooJobId"));
            fail("an exception should have been raised");
        } catch (final HttpException he) {
            assertEquals(404, he.getResponseCode());
        }
    }

    @Test
    public void submitValidXmlJobAndRetrieveByAppName() throws Exception {
        final String applicationName = newTestApplicationName();
        final Document resultDoc = doTestSubmitValidXmlJob(applicationName);
        final String applicationResultsUri = getApplicationResultsUri(resultDoc);
        final String resultUri = ponderUntilOneResultAvailable(applicationResultsUri);
        retrieveAndValidateXmlResult(resultUri);
    }

    @Test
    public void submitValidXmlJobAndRetrieveByResultUri() throws Exception {
        final String applicationName = newTestApplicationName();
        final Document resultDoc = doTestSubmitValidXmlJob(applicationName);
        final String resultUri = getResultUri(resultDoc);
        ponderUntilOneResultAvailable(resultUri);
        retrieveAndValidateXmlResult(resultUri);
    }

    @Test
    public void deleteResult() throws Exception {
        final String applicationName = newTestApplicationName();
        final Document resultDoc = doTestSubmitValidXmlJob(applicationName);
        final String resultUri = getResultUri(resultDoc);
        ponderUntilOneResultAvailable(resultUri);

        final WebConversation wc = createNewWebConversation();
        final WebResponse response = wc.sendRequest(new DeleteMethodWebRequest(resultUri));
        assertEquals(204, response.getResponseCode());

        try {
            wc.sendRequest(new GetMethodWebRequest(resultUri));
            fail("an exception should have been raised");
        } catch (final HttpException he) {
            assertEquals(404, he.getResponseCode());
        }
    }

    @Test
    public void submitInvalidXmlJobAndRetrieveErrorResult() throws Exception {
        final String applicationName = newTestApplicationName();
        final Document responseDocument = doTestSubmitInvalidXmlJob(applicationName);
        final String resultUri = getResultUri(responseDocument);
        final String jobId = XMLUnit.newXpathEngine().evaluate("/rsb:jobToken/@jobId", responseDocument);
        ponderUntilOneResultAvailable(resultUri);
        retrieveAndValidateXmlError(resultUri, jobId);
    }

    @Test
    public void submitValidJsonJobAndRetrieveByAppName() throws Exception {
        final String applicationName = newTestApplicationName();
        final Map<?, ?> responseObject = doTestSubmitValidJsonJob(applicationName);
        final String applicationResultsUri = getApplicationResultsUri(responseObject);
        ponderUntilOneResultAvailable(applicationResultsUri);
        final String resultUri = getResultUri(responseObject);
        retrieveAndValidateJsonResult(resultUri);
    }

    @Test
    public void submitInvalidJsonJobAndRetrieveByAppName() throws Exception {
        final String applicationName = newTestApplicationName();
        final Map<?, ?> responseObject = doTestSubmitInvalidJsonJob(applicationName);
        final String applicationResultsUri = getApplicationResultsUri(responseObject);
        ponderUntilOneResultAvailable(applicationResultsUri);
        final String resultUri = getResultUri(responseObject);
        retrieveAndValidateJsonError(resultUri);
    }

    @Test
    public void forwardedProtocol() throws Exception {
        final String applicationName = newTestApplicationName();
        final Document resultDoc = doTestSubmitJobWithXmlAck(applicationName, getTestStream("data/r-job-sample.xml"),
                Collections.singletonMap("X-Forwarded-Protocol", "foo"));

        assertThat(getApplicationResultsUri(resultDoc), is(new StartsWith("foo:/")));
        assertThat(getResultUri(resultDoc), is(new StartsWith("foo:/")));
    }

    private void retrieveAndValidateXmlResult(final String resultUri) throws Exception {
        final String dataUri = doTestResultAvailability(resultUri, "xml");
        final String result = getStringResponse(dataUri);
        final Document resultDocument = XMLUnit.buildTestDocument(result);
        assertXpathEvaluatesTo("provideMeansAndPlotPerDosageGroup", "/statDataPackage/statHeader/RFunction/text()", resultDocument);
    }

    private void retrieveAndValidateXmlError(final String resultUri, final String jobId) throws Exception {
        final String dataUri = doTestResultAvailability(resultUri, "xml");
        final String result = getStringResponse(dataUri);
        final Document resultDocument = XMLUnit.buildTestDocument(result);
        assertXpathEvaluatesTo(jobId, "/rsb:errorResult/@jobId", resultDocument);
    }

    private void retrieveAndValidateJsonResult(final String resultUri) throws Exception {
        final String dataUri = doTestResultAvailability(resultUri, "json");
        final String result = getStringResponse(dataUri);
        assertNotNull(Util.fromJson(result, List.class));
    }

    private void retrieveAndValidateJsonError(final String resultUri) throws Exception {
        final String dataUri = doTestResultAvailability(resultUri, "json");
        final String result = getStringResponse(dataUri);

        final Map<?, ?> jsonResult = Util.fromJson(result, Map.class);
        assertNotNull(jsonResult);
        assertNotNull(jsonResult.get("errorMessage"));
    }

    private Document doTestSubmitValidXmlJob(final String applicationName) throws IOException, SAXException, XpathException {
        return doTestSubmitXmlJob(applicationName, getTestStream("data/r-job-sample.xml"));
    }

    private Map<?, ?> doTestSubmitValidJsonJob(final String applicationName) throws IOException, SAXException, XpathException {
        return doTestSubmitJobWithJsonAck(applicationName, getTestStream("data/r-job-sample.json"));
    }

    private Map<?, ?> doTestSubmitInvalidJsonJob(final String applicationName) throws IOException, SAXException, XpathException {
        return doTestSubmitJobWithJsonAck(applicationName, new ByteArrayInputStream("not json".getBytes()));
    }

    private Document doTestSubmitInvalidXmlJob(final String applicationName) throws IOException, SAXException, XpathException {
        return doTestSubmitXmlJob(applicationName, new ByteArrayInputStream("<bad/>".getBytes()));
    }

    @SuppressWarnings("unchecked")
    private Document doTestSubmitXmlJob(final String applicationName, final InputStream xmlJob) throws IOException, SAXException,
            XpathException {
        return doTestSubmitJobWithXmlAck(applicationName, xmlJob, Collections.EMPTY_MAP);
    }

    // FIXME reactivate integration tests
    /*
     * @SuppressWarnings("unchecked") public void testSubmitValidZipJobAndRetrieveByAppName() throws
     * Exception { final String applicationName = newTestApplicationName();
     * doTestSubmitZipJob(applicationName, AbstractRsbFunctionalTestCase.ZIP_JOB_WITH_SCRIPT,
     * Collections.EMPTY_MAP);
     * 
     * ponderRetrieveAndValidateZipResult(applicationName); }
     * 
     * @SuppressWarnings("unchecked") public void testSubmitInvalidZipJobAndRetrieveByAppName()
     * throws Exception { final String applicationName = newTestApplicationName();
     * doTestSubmitZipJob(applicationName, AbstractRsbFunctionalTestCase.JOB_SCRIPT,
     * Collections.EMPTY_MAP);
     * 
     * final String resultUri = restResultsUri + "/" + applicationName;
     * ponderUntilResultAvailable(resultUri); retrieveAndValidateZipError(resultUri); }
     * 
     * @SuppressWarnings("unchecked") public void
     * testSubmitValidZipJobWithPropsAndRetrieveByAppName() throws Exception { final String
     * applicationName = newTestApplicationName(); doTestSubmitZipJob(applicationName,
     * AbstractRsbFunctionalTestCase.ZIP_JOB_WITH_PROPS, Collections.EMPTY_MAP);
     * ponderRetrieveAndValidateZipResult(applicationName); }
     * 
     * public void testSubmitValidDataOnlyZipJobAndRetrieveByAppName() throws Exception { final
     * String applicationName = newTestApplicationName(); doTestSubmitZipJob(applicationName,
     * AbstractRsbFunctionalTestCase.ZIP_JOB_DATA_ONLY,
     * Collections.singletonMap("X-RSB-Meta-rScript", "testscript.R"));
     * ponderRetrieveAndValidateZipResult(applicationName); }
     * 
     * public void testSubmitValidJobRequiringMetaAndRetrieveByAppName() throws Exception { final
     * String applicationName = newTestApplicationName(); doTestSubmitZipJob(applicationName,
     * "data/exp-meta-reportAuthor.zip", Collections.singletonMap("X-RSB-Meta-reportAuthor",
     * "Jules Verne")); ponderRetrieveAndValidateZipResult(applicationName); }
     * 
     * @SuppressWarnings("unchecked") public void
     * testSubmitMultipartValidZipJobAndRetrieveByAppName() throws Exception { final String
     * applicationName = newTestApplicationName(); doTestSubmitMultipartValidZipJob(applicationName,
     * Collections.singletonList(new
     * UploadedFile(AbstractRsbFunctionalTestCase.ZIP_JOB_WITH_SCRIPT)), Collections.EMPTY_MAP);
     * ponderRetrieveAndValidateZipResult(applicationName); }
     * 
     * public void testSubmitMultipartValidDataOnlyZipJobAndRetrieveByAppName() throws Exception {
     * final String applicationName = newTestApplicationName();
     * doTestSubmitMultipartValidZipJob(applicationName, Collections.singletonList(new
     * UploadedFile(AbstractRsbFunctionalTestCase.ZIP_JOB_DATA_ONLY)),
     * Collections.singletonMap("X-RSB-Meta-rScript", "testscript.R"));
     * ponderRetrieveAndValidateZipResult(applicationName); }
     * 
     * @SuppressWarnings("unchecked") public void
     * testSubmitMultipartValidNonZipJobAndRetrieveByAppName() throws Exception { final String
     * applicationName = newTestApplicationName(); doTestSubmitMultipartValidZipJob(applicationName,
     * Collections.singletonList(new UploadedFile(AbstractRsbFunctionalTestCase.JOB_SCRIPT)),
     * Collections.EMPTY_MAP); ponderRetrieveAndValidateZipResult(applicationName); }
     * 
     * @SuppressWarnings("unchecked") public void
     * testSubmitMultipartValidMultiFilesJobAndRetrieveByAppName() throws Exception { final String
     * applicationName = newTestApplicationName(); doTestSubmitMultipartValidZipJob(applicationName,
     * Arrays.asList(new UploadedFile("data/exampleSweave.Rnw"), new
     * UploadedFile("data/exampleSweaveRScript.R")), Collections.EMPTY_MAP);
     * ponderRetrieveAndValidateZipResult(applicationName); }
     * 
     * public void testSubmitMultipartValidJobRequiringMetaAndRetrieveByAppName() throws Exception {
     * final String applicationName = newTestApplicationName();
     * doTestSubmitMultipartValidZipJob(applicationName, Collections.singletonList(new
     * UploadedFile("data/exp-meta-reportAuthor.zip")),
     * Collections.singletonMap("X-RSB-Meta-reportAuthor", "Jules Verne"));
     * ponderRetrieveAndValidateZipResult(applicationName); }
     * 
     * @SuppressWarnings("unchecked") public void
     * testSubmitMultipartValidZipJobOctetAsStreamAndRetrieveByAppName() throws Exception { final
     * String applicationName = newTestApplicationName();
     * doTestSubmitMultipartValidZipJob(applicationName, Collections.singletonList(new
     * UploadedFile(AbstractRsbFunctionalTestCase.ZIP_JOB_WITH_SCRIPT, "application/octet-stream")),
     * Collections.EMPTY_MAP); ponderRetrieveAndValidateZipResult(applicationName); }
     * 
     * @SuppressWarnings("unchecked") public void testSubmitMultipartNoAttachedFile() throws
     * Exception { final String applicationName = newTestApplicationName();
     * doTestSubmitMultipartValidZipJob(applicationName, Collections.EMPTY_LIST,
     * Collections.singletonMap("X-RSB-Meta-rScript", "testscript.R"));
     * ponderRetrieveAndValidateZipResult(applicationName); }
     * 
     * private void ponderRetrieveAndValidateZipResult(final String applicationName) throws
     * Exception { final String resultUri = restResultsUri + "/" + applicationName;
     * ponderUntilResultAvailable(resultUri); retrieveAndValidateZipResult(resultUri); }
     * 
     * private void retrieveAndValidateZipResult(final String resultUri) throws Exception { final
     * String dataUri = doTestResultAvailability(resultUri, "zip");
     * TestSupport.validateZipResult(getStreamResponse(dataUri)); }
     * 
     * private void retrieveAndValidateZipError(final String resultUri) throws Exception { final
     * String dataUri = doTestResultAvailability(resultUri, "txt");
     * TestSupport.validateErrorResult(getStreamResponse(dataUri)); }
     * 
     * private String doTestSubmitZipJob(final String applicationName, final String zipJobFileName,
     * final Map<String, String> extraHeaders) throws IOException, SAXException, XpathException {
     * final Document result = doTestSubmitZipJob(applicationName, getTestStream(zipJobFileName),
     * extraHeaders);
     * 
     * return getResultUri(result); }
     * 
     * private Document doTestSubmitZipJob(final String applicationName, final InputStream job,
     * final Map<String, String> extraHeaders) throws IOException, SAXException, XpathException {
     * return doTestSubmitJobWithXmlAck(applicationName, job, ZIP_JOB_CONTENT_TYPE, extraHeaders); }
     * 
     * private Document doTestSubmitMultipartValidZipJob(final String applicationName, final
     * List<UploadedFile> jobFiles, final Map<String, String> extraFields) throws Exception { //
     * disable javascript because we want a normal form post to happen and not an AJAX one final
     * WebClient webClient = createNewWebClient();
     * 
     * // get the upload form from RSB web server final HtmlPage jobUploadPage =
     * webClient.getPage(TestSupport.RSB_JOB_UPLOAD_FORM_URI); final HtmlForm jobUploadForm =
     * jobUploadPage.getFormByName("jobUploadForm");
     * 
     * // fill the form and submit it if (!jobFiles.isEmpty()) { final HtmlFileInput fileInput =
     * jobUploadForm.getInputByName("X-RSB-JobFile[]"); final UploadedFile firstFile =
     * jobFiles.get(0); setUploadedFileOnInputControl(fileInput, firstFile);
     * 
     * for (int i = 1; i < jobFiles.size(); i++) { final HtmlFileInput extraFileInput =
     * (HtmlFileInput) fileInput.cloneNode(true); final UploadedFile uploadedFile = jobFiles.get(i);
     * setUploadedFileOnInputControl(extraFileInput, uploadedFile);
     * jobUploadForm.appendChild(extraFileInput); } }
     * 
     * final HtmlInput applicationNameInput =
     * jobUploadForm.getInputByName("X-RSB-Application-Name");
     * applicationNameInput.setValueAttribute(applicationName);
     * 
     * // deal with extra meta fields for (final Entry<String, String> extraField :
     * extraFields.entrySet()) { final String inputName = extraField.getKey(); HtmlInput input =
     * null;
     * 
     * try { input = jobUploadForm.getInputByName(inputName); } catch (final
     * ElementNotFoundException enfe) { // generate the missing input on the fly (like a dynamic web
     * form would do) // by cloning an existing one input = (HtmlInput)
     * applicationNameInput.cloneNode(true); input.setAttribute("name", inputName);
     * jobUploadForm.appendChild(input); }
     * 
     * input.setValueAttribute(extraField.getValue()); }
     * 
     * final Page jobUploadConfirmationPage =
     * jobUploadForm.getInputByName("jobSubmitButton").click(); final
     * com.gargoylesoftware.htmlunit.WebResponse response =
     * jobUploadConfirmationPage.getWebResponse();
     * 
     * // terminate web client and evaluate response validity webClient.closeAllWindows();
     * 
     * return parseJobSubmissionXmlAckResponse(applicationName, response.getStatusCode(),
     * response.getResponseHeaderValue("Content-Type"), response.getContentAsString()); }
     * 
     * private void setUploadedFileOnInputControl(final HtmlFileInput fileInput, final UploadedFile
     * uploadedFile) {
     * fileInput.setValueAttribute(AbstractRsbFunctionalTestCase.getTestFile(uploadedFile
     * .name).getPath());
     * 
     * if (uploadedFile.contentType != null) fileInput.setContentType(uploadedFile.contentType); }
     */

    private String ponderUntilOneResultAvailable(final String watchUri) throws Exception {
        for (int attemptCount = 0; attemptCount < 60; attemptCount++) {

            Thread.sleep(500);

            final WebConversation wc = createNewWebConversation();
            final WebRequest request = new GetMethodWebRequest(watchUri);
            try {
                final WebResponse response = wc.sendRequest(request);
                final String responseText = response.getText();
                final Document responseDocument = XMLUnit.buildTestDocument(responseText);
                final String selfUri = XMLUnit.newXpathEngine().evaluate("//rsb:result/@selfUri", responseDocument);

                if (StringUtils.isNotBlank(selfUri)) {
                    return selfUri;
                }
            } catch (final Exception e) {
                // ignore exceptions final due to 404
            }
        }
        fail("Result didn't come on time at URI: " + watchUri);
        return null;
    }

    private Document doTestSubmitJobWithXmlAck(final String applicationName, final InputStream job, final Map<String, String> extraHeaders)
            throws IOException, SAXException, XpathException {
        final WebConversation wc = createNewWebConversation();
        final PostMethodWebRequest request = new PostMethodWebRequest(restJobsUri, job, "application/xml");
        request.setHeaderField("X-RSB-Application-Name", applicationName);

        for (final Entry<String, String> extraHeader : extraHeaders.entrySet()) {
            request.setHeaderField(extraHeader.getKey(), extraHeader.getValue());
        }

        final WebResponse response = wc.sendRequest(request);

        return parseJobSubmissionXmlAckResponse(applicationName, response.getResponseCode(), response.getHeaderField("Content-Type"),
                response.getText());
    }

    private Document parseJobSubmissionXmlAckResponse(final String applicationName, final int statusCode, final String contentType,
            final String entity) throws SAXException, IOException, XpathException {
        assertEquals(202, statusCode);
        assertEquals("application/vnd.rsb+xml", contentType);

        final Document responseDocument = XMLUnit.buildTestDocument(entity);
        assertXpathEvaluatesTo(applicationName, "/rsb:jobToken/@applicationName", responseDocument);
        assertXpathExists("/rsb:jobToken/@jobId", responseDocument);
        assertXpathExists("/rsb:jobToken/@resultUri", responseDocument);
        return responseDocument;
    }

    private Map<?, ?> doTestSubmitJobWithJsonAck(final String applicationName, final InputStream job) throws IOException, SAXException,
            XpathException {
        final WebConversation wc = createNewWebConversation();
        final WebRequest request = new PostMethodWebRequest(restJobsUri, job, "application/json");
        request.setHeaderField("X-RSB-Application-Name", applicationName);
        request.setHeaderField("Accept", "application/vnd.rsb+json");

        final WebResponse response = wc.sendRequest(request);

        assertEquals(202, response.getResponseCode());
        assertEquals("application/vnd.rsb+json", response.getHeaderField("Content-Type"));

        final Map<?, ?> jsonResult = (Map<?, ?>) Util.fromJson(response.getText(), Map.class).get("jobToken");
        assertEquals(applicationName, jsonResult.get("applicationName"));
        assertNotNull(jsonResult.get("jobId"));
        assertNotNull(jsonResult.get("resultUri"));
        return jsonResult;
    }

    private String doTestResultAvailability(final String resultUri, final String expectedType) throws Exception {
        final WebConversation wc = createNewWebConversation();
        final WebResponse response = wc.sendRequest(new GetMethodWebRequest(resultUri));
        assertEquals(200, response.getResponseCode());
        assertEquals("application/vnd.rsb+xml", response.getHeaderField("Content-Type"));

        final String responseText = response.getText();
        final Document responseDocument = XMLUnit.buildTestDocument(responseText);
        assertXpathExists("//rsb:result/@jobId", responseDocument);
        assertXpathExists("//rsb:result/@applicationName", responseDocument);
        assertXpathExists("//rsb:result/@resultTime", responseDocument);
        assertXpathExists("//rsb:result/@selfUri", responseDocument);

        assertXpathExists("//rsb:result/@type", responseDocument);
        assertXpathEvaluatesTo(expectedType, "//rsb:result/@type", responseDocument);

        assertXpathExists("//rsb:result/@dataUri", responseDocument);
        return XMLUnit.newXpathEngine().evaluate("//rsb:result/@dataUri", responseDocument);
    }

    private String getStringResponse(final String resultUri) throws IOException, SAXException {
        return getResponse(resultUri).getText();
    }

    private InputStream getStreamResponse(final String resultUri) throws IOException, SAXException {
        return getResponse(resultUri).getInputStream();
    }

    private WebResponse getResponse(final String resultUri) throws IOException, SAXException {
        final WebConversation wc = createNewWebConversation();
        final WebRequest request = new GetMethodWebRequest(resultUri);
        final WebResponse response = wc.sendRequest(request);
        return response;
    }

    private String getResultUri(final Document responseDocument) throws XpathException {
        return XMLUnit.newXpathEngine().evaluate("/rsb:jobToken/@resultUri", responseDocument);
    }

    private String getApplicationResultsUri(final Document responseDocument) throws XpathException {
        return XMLUnit.newXpathEngine().evaluate("/rsb:jobToken/@applicationResultsUri", responseDocument);
    }

    private String getResultUri(final Map<?, ?> jsonResult) {
        return jsonResult.get("resultUri").toString();
    }

    private String getApplicationResultsUri(final Map<?, ?> jsonResult) {
        return jsonResult.get("applicationResultsUri").toString();
    }

    private String newTestApplicationName() {
        return "rsb_it_" + RandomStringUtils.randomAlphanumeric(20);
    }

    private static class UploadedFile {
        String name;
        String contentType;

        UploadedFile(final String name) {
            this(name, null);
        }

        UploadedFile(final String name, final String contentType) {
            this.name = name;
            this.contentType = contentType;
        }
    }

    private void doTestUnsupportedDeleteMethod(final String requestUri) throws IOException, SAXException {
        final WebConversation wc = createNewWebConversation();
        final WebRequest request = new DeleteMethodWebRequest(requestUri);
        try {
            wc.sendRequest(request);
            fail("an exception should have been raised");
        } catch (final HttpException he) {
            assertEquals(405, he.getResponseCode());
        }
    }

    private WebClient createNewWebClient() {
        final WebClient webClient = new WebClient();
        webClient.setJavaScriptEnabled(false);
        return webClient;
    }

    private WebConversation createNewWebConversation() {
        return new WebConversation();
    }

    private static InputStream getTestStream(final String payloadResourceFile) {
        return new ByteArrayInputStream(getTestBytes(payloadResourceFile));
    }

    private static byte[] getTestBytes(final String payloadResourceFile) {
        try {
            return FileCopyUtils.copyToByteArray(Thread.currentThread().getContextClassLoader().getResourceAsStream(payloadResourceFile));
        } catch (final IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
