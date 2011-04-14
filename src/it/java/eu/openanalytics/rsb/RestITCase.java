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

import java.io.IOException;

import org.apache.activemq.util.ByteArrayInputStream;
import org.apache.commons.lang.StringUtils;
import org.custommonkey.xmlunit.XMLTestCase;
import org.jitr.Jitr;
import org.jitr.annotation.BaseUri;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.WebClient;
import com.meterware.httpunit.HeadMethodWebRequest;
import com.meterware.httpunit.HttpException;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;

/*
 import org.apache.commons.lang.RandomStringUtils;
 import org.custommonkey.xmlunit.XMLUnit;
 import org.custommonkey.xmlunit.exceptions.XpathException;
 import org.w3c.dom.Document;
 import com.gargoylesoftware.htmlunit.ElementNotFoundException;
 import com.gargoylesoftware.htmlunit.Page;
 import com.gargoylesoftware.htmlunit.html.HtmlFileInput;
 import com.gargoylesoftware.htmlunit.html.HtmlForm;
 import com.gargoylesoftware.htmlunit.html.HtmlInput;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.meterware.httpunit.GetMethodWebRequest;
 import com.meterware.httpunit.WebResponse;
 import eu.openanalytics.httpunit.DeleteMethodWebRequest;
 */
/**
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
@RunWith(Jitr.class)
public class RestITCase extends XMLTestCase {
    @BaseUri
    private String baseUri;

    private String restJobsUri;
    private String rsbUserName;
    private String rsbPassword;

    @Before
    public void before() {
        restJobsUri = baseUri + "api/rest/jobs";
        rsbUserName = null;
        rsbPassword = null;
    }

    @Test
    public void testJobsBadMethod() throws Exception {
        doTestUnsupportedHeadMethod(restJobsUri);
    }

    @Test
    public void testJobsBadApplicationName() throws Exception {
        final WebConversation wc = createNewWebConversation();
        final WebRequest request = new PostMethodWebRequest(restJobsUri, new ByteArrayInputStream("ignored".getBytes()),
                Constants.XML_JOB_CONTENT_TYPE);
        request.setHeaderField("X-RSB-Application-Name", ":bad_app$name!");

        try {
            wc.sendRequest(request);
            fail("an exception should have been raised");
        } catch (final HttpException he) {
            assertEquals(400, he.getResponseCode());
        }
    }

    @Test
    public void testJobsBadContentType() throws Exception {
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

    // FIXME test X-Base-Uri support

    /*
     * public void testResultsBadMethod() throws Exception {
     * doTestBadMethod(TestSupport.RSB_REST_RESULTS_URI); }
     * 
     * public void testNoResultForApplication() throws Exception { final WebConversation wc =
     * createNewWebConversation(); final WebResponse res = wc.sendRequest(new
     * GetMethodWebRequest(TestSupport.RSB_REST_RESULTS_URI + "/fooAppName")); assertEquals(200,
     * res.getResponseCode()); assertEquals("<r-results />", res.getText()); }
     * 
     * public void testResultNotFound() throws Exception { final WebConversation wc =
     * createNewWebConversation();
     * 
     * try { wc.sendRequest(new GetMethodWebRequest(TestSupport.RSB_REST_RESULTS_URI +
     * "/fooAppName/fooJobId")); fail("an exception should have been raised"); } catch (final
     * HttpException he) { assertEquals(404, he.getResponseCode()); } }
     * 
     * public void testSubmitValidXmlJobAndRetrieveByAppName() throws Exception { final String
     * applicationName = newTestApplicationName(); doTestSubmitValidXmlJob(applicationName); final
     * String resultUri = TestSupport.RSB_REST_RESULTS_URI + "/" + applicationName;
     * ponderUntilResultAvailable(resultUri); retrieveAndValidateXmlResult(resultUri); }
     * 
     * public void testSubmitValidXmlJobAndRetrieveByResultUri() throws Exception { final String
     * applicationName = newTestApplicationName(); final String resultUri =
     * doTestSubmitValidXmlJob(applicationName); ponderUntilResultAvailable(resultUri);
     * retrieveAndValidateXmlResult(resultUri); }
     * 
     * public void testDeleteResult() throws Exception { final String applicationName =
     * newTestApplicationName(); final String resultUri = doTestSubmitValidXmlJob(applicationName);
     * ponderUntilResultAvailable(resultUri);
     * 
     * final WebConversation wc = createNewWebConversation(); final WebResponse response =
     * wc.sendRequest(new DeleteMethodWebRequest(resultUri)); assertEquals(200,
     * response.getResponseCode());
     * 
     * try { wc.sendRequest(new GetMethodWebRequest(resultUri));
     * fail("an exception should have been raised"); } catch (final HttpException he) {
     * assertEquals(404, he.getResponseCode()); } }
     * 
     * public void testSubmitInvalidXmlJobAndRetrieveErrorResult() throws Exception { final String
     * applicationName = newTestApplicationName(); final Document responseDocument =
     * doTestSubmitInvalidXmlJob(applicationName); final String resultUri =
     * getResultUri(responseDocument); final String jobId =
     * XMLUnit.newXpathEngine().evaluate("/r-job-accepted/@jobId", responseDocument);
     * ponderUntilResultAvailable(resultUri); retrieveAndValidateXmlError(resultUri, jobId); }
     * 
     * public void testSubmitValidJsonJobAndRetrieveByAppName() throws Exception { final String
     * applicationName = newTestApplicationName(); doTestSubmitValidJsonJob(applicationName); final
     * String resultUri = TestSupport.RSB_REST_RESULTS_URI + "/" + applicationName;
     * ponderUntilResultAvailable(resultUri); retrieveAndValidateJsonResult(resultUri); }
     * 
     * public void testSubmitInvalidJsonJobAndRetrieveByAppName() throws Exception { final String
     * applicationName = newTestApplicationName(); doTestSubmitInvalidJsonJob(applicationName);
     * final String resultUri = TestSupport.RSB_REST_RESULTS_URI + "/" + applicationName;
     * ponderUntilResultAvailable(resultUri); retrieveAndValidateJsonError(resultUri); }
     * 
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
     * final String resultUri = TestSupport.RSB_REST_RESULTS_URI + "/" + applicationName;
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
     * Exception { final String resultUri = TestSupport.RSB_REST_RESULTS_URI + "/" +
     * applicationName; ponderUntilResultAvailable(resultUri);
     * retrieveAndValidateZipResult(resultUri); }
     * 
     * private String getResultUri(final Document responseDocument) throws XpathException { return
     * XMLUnit.newXpathEngine().evaluate("/r-job-accepted/@resultUri", responseDocument); }
     * 
     * private void retrieveAndValidateXmlResult(final String resultUri) throws Exception { final
     * String dataUri = doTestResultAvailability(resultUri, "xml"); final String result =
     * getStringResponse(dataUri); final Document resultDocument =
     * XMLUnit.buildTestDocument(result);
     * assertXpathEvaluatesTo("provideMeansAndPlotPerDosageGroup",
     * "/statDataPackage/statHeader/RFunction/text()", resultDocument); }
     * 
     * private void retrieveAndValidateXmlError(final String resultUri, final String jobId) throws
     * Exception { final String dataUri = doTestResultAvailability(resultUri, "xml"); final String
     * result = getStringResponse(dataUri); final Document resultDocument =
     * XMLUnit.buildTestDocument(result); assertXpathEvaluatesTo(jobId, "/error/@jobId",
     * resultDocument); }
     * 
     * private void retrieveAndValidateJsonResult(final String resultUri) throws Exception { final
     * String dataUri = doTestResultAvailability(resultUri, "json"); final String result =
     * getStringResponse(dataUri); assertNotNull(JSON.parse(result)); }
     * 
     * private void retrieveAndValidateJsonError(final String resultUri) throws Exception { final
     * String dataUri = doTestResultAvailability(resultUri, "json"); final String result =
     * getStringResponse(dataUri);
     * 
     * final Map<?, ?> jsonResult = (Map<?, ?>) JSON.parse(result); assertNotNull(jsonResult);
     * assertNotNull(jsonResult.get("errorMessage")); }
     * 
     * private void retrieveAndValidateZipResult(final String resultUri) throws Exception { final
     * String dataUri = doTestResultAvailability(resultUri, "zip");
     * TestSupport.validateZipResult(getStreamResponse(dataUri)); }
     * 
     * private void retrieveAndValidateZipError(final String resultUri) throws Exception { final
     * String dataUri = doTestResultAvailability(resultUri, "txt");
     * TestSupport.validateErrorResult(getStreamResponse(dataUri)); }
     * 
     * private String doTestSubmitValidXmlJob(final String applicationName) throws IOException,
     * SAXException, XpathException { final Document result = doTestSubmitXmlJob(applicationName,
     * AbstractRsbFunctionalTestCase.getTestStream("data/r-job-sample.xml"));
     * 
     * return getResultUri(result); }
     * 
     * private String doTestSubmitValidJsonJob(final String applicationName) throws IOException,
     * SAXException, XpathException { final Map<?, ?> jsonResult =
     * doTestSubmitJobWithJsonAck(applicationName,
     * AbstractRsbFunctionalTestCase.getTestStream("data/r-job-sample.json"),
     * JSON_JOB_CONTENT_TYPE);
     * 
     * return jsonResult.get("resultUri").toString(); }
     * 
     * private String doTestSubmitInvalidJsonJob(final String applicationName) throws IOException,
     * SAXException, XpathException { final Map<?, ?> jsonResult =
     * doTestSubmitJobWithJsonAck(applicationName, new ByteArrayInputStream("not json".getBytes()),
     * JSON_JOB_CONTENT_TYPE);
     * 
     * return jsonResult.get("resultUri").toString(); }
     * 
     * private Document doTestSubmitInvalidXmlJob(final String applicationName) throws IOException,
     * SAXException, XpathException { return doTestSubmitXmlJob(applicationName, new
     * ByteArrayInputStream("<bad/>".getBytes())); }
     * 
     * @SuppressWarnings("unchecked") private Document doTestSubmitXmlJob(final String
     * applicationName, final InputStream xmlJob) throws IOException, SAXException, XpathException {
     * return doTestSubmitJobWithXmlAck(applicationName, xmlJob, XML_RSB_CONTENT_TYPE,
     * Collections.EMPTY_MAP); }
     * 
     * private String doTestSubmitZipJob(final String applicationName, final String zipJobFileName,
     * final Map<String, String> extraHeaders) throws IOException, SAXException, XpathException {
     * final Document result = doTestSubmitZipJob(applicationName,
     * AbstractRsbFunctionalTestCase.getTestStream(zipJobFileName), extraHeaders);
     * 
     * return getResultUri(result); }
     * 
     * private Document doTestSubmitZipJob(final String applicationName, final InputStream job,
     * final Map<String, String> extraHeaders) throws IOException, SAXException, XpathException {
     * return doTestSubmitJobWithXmlAck(applicationName, job, ZIP_JOB_CONTENT_TYPE, extraHeaders); }
     * 
     * private Document doTestSubmitJobWithXmlAck(final String applicationName, final InputStream
     * job, final String contentType, final Map<String, String> extraHeaders) throws IOException,
     * SAXException, XpathException { final WebConversation wc = createNewWebConversation(); final
     * PostMethodWebRequest request = new PostMethodWebRequest(TestSupport.RSB_REST_JOBS_URI, job,
     * contentType); request.setHeaderField("X-RSB-Application-Name", applicationName);
     * 
     * for (final Entry<String, String> extraHeader : extraHeaders.entrySet()) {
     * request.setHeaderField(extraHeader.getKey(), extraHeader.getValue()); }
     * 
     * final WebResponse response = wc.sendRequest(request);
     * 
     * return parseJobSubmissionXmlAckResponse(applicationName, response.getResponseCode(),
     * response.getHeaderField("Content-Type"), response.getText()); }
     * 
     * private Document parseJobSubmissionXmlAckResponse(final String applicationName, final int
     * statusCode, final String contentType, final String entity) throws SAXException, IOException,
     * XpathException { assertEquals(202, statusCode); assertEquals(XML_CONTENT_TYPE, contentType);
     * 
     * final Document responseDocument = XMLUnit.buildTestDocument(entity);
     * assertXpathEvaluatesTo(applicationName, "/r-job-accepted/@appName", responseDocument);
     * assertXpathExists("/r-job-accepted/@jobId", responseDocument);
     * assertXpathExists("/r-job-accepted/@resultUri", responseDocument); return responseDocument; }
     * 
     * private Map<?, ?> doTestSubmitJobWithJsonAck(final String applicationName, final InputStream
     * job, final String contentType) throws IOException, SAXException, XpathException { final
     * WebConversation wc = createNewWebConversation(); final WebRequest request = new
     * PostMethodWebRequest(TestSupport.RSB_REST_JOBS_URI, job, contentType);
     * request.setHeaderField("X-RSB-Application-Name", applicationName);
     * 
     * final WebResponse response = wc.sendRequest(request);
     * 
     * assertEquals(202, response.getResponseCode()); assertEquals(JSON_ACK_CONTENT_TYPE,
     * response.getHeaderField("Content-Type"));
     * 
     * final Map<?, ?> jsonResult = (Map<?, ?>) JSON.parse(response.getText());
     * assertEquals(applicationName, jsonResult.get("appName"));
     * assertNotNull(jsonResult.get("jobId")); assertNotNull(jsonResult.get("resultUri")); return
     * jsonResult; }
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
     * 
     * private void ponderUntilResultAvailable(final String resultUri) throws Exception { for (int
     * attemptCount = 0; attemptCount < 60; attemptCount++) {
     * 
     * Thread.sleep(500);
     * 
     * final WebConversation wc = createNewWebConversation(); final WebRequest request = new
     * GetMethodWebRequest(resultUri); try { final WebResponse response = wc.sendRequest(request);
     * if (!("<r-results />".equals(response.getText()))) return; } catch (final Exception e) { //
     * ignore exceptions due to 404 } }
     * 
     * fail("Result didn't come on time at URI: " + resultUri); }
     * 
     * private String doTestResultAvailability(final String resultUri, final String expectedType)
     * throws Exception { final WebConversation wc = createNewWebConversation(); final WebResponse
     * response = wc.sendRequest(new GetMethodWebRequest(resultUri)); assertEquals(200,
     * response.getResponseCode()); assertEquals(XML_CONTENT_TYPE,
     * response.getHeaderField("Content-Type"));
     * 
     * final String responseText = response.getText(); final Document responseDocument =
     * XMLUnit.buildTestDocument(responseText); assertXpathExists("//r-result/@selfUri",
     * responseDocument); assertXpathExists("//r-result/@jobId", responseDocument);
     * assertXpathExists("//r-result/@appName", responseDocument);
     * assertXpathExists("//r-result/@dataUri", responseDocument);
     * assertXpathExists("//r-result/@timestamp", responseDocument);
     * assertXpathExists("//r-result/@type", responseDocument); assertXpathEvaluatesTo(expectedType,
     * "//r-result/@type", responseDocument);
     * 
     * return XMLUnit.newXpathEngine().evaluate("//r-result/@dataUri", responseDocument); }
     * 
     * private String getStringResponse(final String resultUri) throws IOException, SAXException {
     * return getResponse(resultUri).getText(); }
     * 
     * private InputStream getStreamResponse(final String resultUri) throws IOException,
     * SAXException { return getResponse(resultUri).getInputStream(); }
     * 
     * private WebResponse getResponse(final String resultUri) throws IOException, SAXException {
     * final WebConversation wc = createNewWebConversation(); final WebRequest request = new
     * GetMethodWebRequest(resultUri); final WebResponse response = wc.sendRequest(request); return
     * response; }
     * 
     * private String newTestApplicationName() { return "rsb_it_" +
     * RandomStringUtils.randomAlphanumeric(20); }
     * 
     * private static class UploadedFile { String name; String contentType;
     * 
     * UploadedFile(final String name) { this(name, null); }
     * 
     * UploadedFile(final String name, final String contentType) { this.name = name;
     * this.contentType = contentType; } }
     */

    private void doTestUnsupportedHeadMethod(final String requestUri) throws IOException, SAXException {
        final WebConversation wc = createNewWebConversation();
        final WebRequest request = new HeadMethodWebRequest(requestUri);
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

        if (StringUtils.isNotBlank(rsbUserName)) {
            final DefaultCredentialsProvider dcp = new DefaultCredentialsProvider();
            dcp.addCredentials(rsbUserName, rsbPassword);
            webClient.setCredentialsProvider(dcp);
        }

        return webClient;
    }

    @SuppressWarnings("deprecation")
    private WebConversation createNewWebConversation() {
        final WebConversation wc = new WebConversation();

        if (StringUtils.isNotBlank(rsbUserName)) {
            // the non-deprecated methods fails miserably when authenticating POST methods
            // wc.setAuthentication(TestSupport.RSB_REALM, TestSupport.RSB_USERNAME,
            // TestSupport.RSB_PASSWORD);
            wc.setAuthorization(rsbUserName, rsbPassword);
        }

        return wc;
    }

}
