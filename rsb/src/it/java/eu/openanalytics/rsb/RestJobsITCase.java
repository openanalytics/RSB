/*
 * R Service Bus
 * 
 * Copyright (c) Copyright of Open Analytics NV, 2010-2023
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

package eu.openanalytics.rsb;

import static org.eclipse.statet.jcommons.io.FileUtils.requireFileName;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathNotExists;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.statet.jcommons.io.FileUtils;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlFileInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.meterware.httpunit.DeleteMethodWebRequest;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpException;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import org.apache.activemq.util.ByteArrayInputStream;
import org.apache.commons.lang3.StringUtils;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.StartsWith;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.test.ComparisonFailureWithDetail;


/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class RestJobsITCase extends AbstractITCase {
	
	
	private String restJobsUri;
	private String restResultsUri;
	private String uploadFormUri;
	
	
	@Before
	public void prepareTests() throws IOException {
		this.restJobsUri= RSB_BASE_URI + "/api/rest/jobs";
		this.restResultsUri= RSB_BASE_URI + "/api/rest/results";
		this.uploadFormUri= RSB_BASE_URI + "/rsb.html";
	}
	
	@After
	public void cleanupResults() throws IOException {
		final Configuration configuration= getConfiguration();
		if (configuration == null) {
			return;
		}
		
		final var directory= configuration.getResultsDirectory().toPath();
		if (Files.isDirectory(directory)) {
			FileUtils.cleanDirectory(directory,
					(path) -> requireFileName(path).toString().startsWith(TEST_APPLICATION_NAME_PREFIX) );
		}
	}
	
	// -------- REST API Tests ---------
	
	@Test
	public void jobsBadMethod() throws Exception {
		doTestUnsupportedDeleteMethod(this.restJobsUri);
	}
	
	@Test
	public void jobsBadApplicationName() throws Exception {
		final WebConversation wc= new WebConversation();
		final WebRequest request= new PostMethodWebRequest(this.restJobsUri,
				new ByteArrayInputStream("ignored".getBytes()), "application/xml" );
		request.setHeaderField("X-RSB-Application-Name", ":bad_app$name!");
		
		try {
			wc.sendRequest(request);
			fail("an exception should have been raised");
		}
		catch (final HttpException he) {
			assertEquals(400, he.getResponseCode());
		}
	}
	
	@Test
	public void jobsBadContentType() throws Exception {
		final WebConversation wc= new WebConversation();
		final WebRequest request= new PostMethodWebRequest(this.restJobsUri,
				new ByteArrayInputStream("ignored".getBytes()), "application/unsupported" );
		request.setHeaderField("X-RSB-Application-Name", "myApp");
		
		try {
			wc.sendRequest(request);
			fail("an exception should have been raised");
		}
		catch (final HttpException he) {
			assertEquals(415, he.getResponseCode());
		}
	}
	
	@Test
	public void noResultForApplication() throws Exception {
		final WebConversation wc= new WebConversation();
		final WebResponse res= wc.sendRequest(new GetMethodWebRequest(this.restResultsUri + "/fooAppName"));
		assertEquals(200, res.getResponseCode());
		assertEquals("application/vnd.rsb+xml", res.getHeaderField("Content-Type"));
		
		final String xml= res.getText();
		assertXpathExists("/rsb:results", xml);
		assertXpathNotExists("/rsb:results/rsb:result", res.getText());
	}
	
	@Test
	public void noResultForApplicationAsJson() throws Exception {
		final WebConversation wc= new WebConversation();
		final GetMethodWebRequest request= new GetMethodWebRequest(this.restResultsUri + "/fooAppName");
		request.setHeaderField("Accept", "application/vnd.rsb+json");
		final WebResponse res= wc.sendRequest(request);
		assertEquals(200, res.getResponseCode());
		assertEquals("application/vnd.rsb+json", res.getHeaderField("Content-Type"));
		assertEquals("{\"results\":\"\"}", res.getText());
	}
	
	@Test
	public void resultNotFound() throws Exception {
		final WebConversation wc= new WebConversation();
		
		try {
			wc.sendRequest(new GetMethodWebRequest(this.restResultsUri + "/fooAppName/de2e7d40-8253-11e0-885e-0002a5d5c51b"));
			fail("an exception should have been raised");
		}
		catch (final HttpException he) {
			assertEquals(404, he.getResponseCode());
		}
	}
	
	@Test
	public void submitValidXmlJobAndRetrieveByAppName() throws Exception {
		final String applicationName= newTestApplicationName();
		final Document resultDoc= doSubmitValidXmlJob(applicationName);
		final String applicationResultsUri= getApplicationResultsUri(resultDoc);
		final String resultUri= ponderUntilOneResultAvailable(applicationResultsUri);
		retrieveAndValidateXmlResult(resultUri);
	}
	
	@Test
	public void submitValidXmlJob() throws Exception {
		final String applicationName= newTestApplicationName();
		final Document resultDoc= doSubmitValidXmlJob(applicationName);
		final String resultUri= getResultUri(resultDoc);
		ponderUntilOneResultAvailable(resultUri);
		retrieveAndValidateXmlResult(resultUri);
	}
	
	@Test
	public void deleteResult() throws Exception {
		final String applicationName= newTestApplicationName();
		final Document resultDoc= doSubmitValidXmlJob(applicationName);
		final String resultUri= getResultUri(resultDoc);
		ponderUntilOneResultAvailable(resultUri);
		
		final WebConversation wc= new WebConversation();
		final WebResponse response= wc.sendRequest(new DeleteMethodWebRequest(resultUri));
		assertEquals(204, response.getResponseCode());
		
		try {
			wc.sendRequest(new GetMethodWebRequest(resultUri));
			fail("an exception should have been raised");
		}
		catch (final HttpException he) {
			assertEquals(404, he.getResponseCode());
		}
	}
	
	@Test
	public void submitInvalidXmlJobAndRetrieveErrorResult() throws Exception {
		final String applicationName= newTestApplicationName();
		final Document responseDocument= doSubmitInvalidXmlJob(applicationName);
		final String resultUri= getResultUri(responseDocument);
		final String jobId= XMLUnit.newXpathEngine().evaluate("/rsb:jobToken/@jobId", responseDocument);
		ponderUntilOneResultAvailable(resultUri);
		retrieveAndValidateXmlError(resultUri, jobId);
	}
	
	@Test
	public void submitValidJsonJobAndRetrieveByAppName() throws Exception {
		final String applicationName= newTestApplicationName();
		final Map<?, ?> responseObject= doSubmitValidJsonJob(applicationName);
		final String applicationResultsUri= getApplicationResultsUri(responseObject);
		ponderUntilOneResultAvailable(applicationResultsUri);
		final String resultUri= getResultUri(responseObject);
		retrieveAndValidateJsonResult(resultUri);
	}
	
	@Test
	public void submitInvalidJsonJob() throws Exception {
		final String applicationName= newTestApplicationName();
		final Map<?, ?> responseObject= doSubmitInvalidJsonJob(applicationName);
		final String applicationResultsUri= getApplicationResultsUri(responseObject);
		ponderUntilOneResultAvailable(applicationResultsUri);
		final String resultUri= getResultUri(responseObject);
		retrieveAndValidateJsonError(resultUri);
	}
	
	@Test
	public void forwardedProtocol() throws Exception {
		final String applicationName= newTestApplicationName();
		final Document resultDoc= doTestSubmitXmlJobWithXmlAck(applicationName,
				getTestDataStream("r-job-sample.xml" ),
				Map.of("X-Forwarded-Protocol", "foo") );
		
		assertThat(getApplicationResultsUri(resultDoc), is(new StartsWith("foo:/")));
		assertThat(getResultUri(resultDoc), is(new StartsWith("foo:/")));
	}
	
	@Test
	public void submitValidZipJob() throws Exception {
		final String applicationName= newTestApplicationName();
		@SuppressWarnings("unchecked")
		final Document resultDoc= doTestSubmitZipJob(applicationName,
				getTestDataStream("r-job-sample.zip"),
				Collections.EMPTY_MAP );
		final String resultUri= getResultUri(resultDoc);
		ponderRetrieveAndValidateZipResult(resultUri);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void submitInvalidZipJob() throws Exception {
		final String applicationName= newTestApplicationName();
		try {
			doTestSubmitZipJob(applicationName,
					getTestDataStream("invalid-job-subdir.zip"),
					Collections.EMPTY_MAP );
			fail("an exception should have been raised");
		}
		catch (final HttpException he) {
			assertEquals(400, he.getResponseCode());
		}
	}
	
	@Test
	public void submitValidZipJobWithCatalogRef() throws Exception {
		final String applicationName= newTestApplicationName();
		@SuppressWarnings("unchecked")
		final Document resultDoc= doTestSubmitZipJob(applicationName,
				getTestDataStream("r-job-catalog-ref.zip"),
				Collections.EMPTY_MAP );
		final String resultUri= getResultUri(resultDoc);
		ponderRetrieveAndValidateZipResult(resultUri);
	}
	
	@Test
	public void submitValidDataOnlyZipJob() throws Exception {
		final String applicationName= newTestApplicationName();
		final Document resultDoc= doTestSubmitZipJob(applicationName,
				getTestDataStream("r-job-data-only.zip"),
				Map.of("X-RSB-Meta-rScript", "test.R") );
		final String resultUri= getResultUri(resultDoc);
		ponderRetrieveAndValidateZipResult(resultUri);
	}
	
	@Test
	public void submitValidJobRequiringMetaA() throws Exception {
		final String applicationName= newTestApplicationName();
		final Document resultDoc= doTestSubmitZipJob(applicationName,
				getTestDataStream("r-job-meta-required-lc.zip"),
				Map.of("X-RSB-Meta-Report.Author", "Jules Verne") );
		final String resultUri= getResultUri(resultDoc);
		ponderRetrieveAndValidateZipResult(resultUri);
	}
	
	@Test
	public void submitValidJobRequiringMetaB() throws Exception {
		final String applicationName= newTestApplicationName();
		final Document resultDoc= doTestSubmitZipJob(applicationName,
				getTestDataStream("r-job-meta-required.zip"),
				Map.of("X-RSB-Meta-#1", "reportAuthor = Jules Verne") );
		final String resultUri= getResultUri(resultDoc);
		ponderRetrieveAndValidateZipResult(resultUri);
	}
	
	@Test
	public void submitValidJobRequiringMetaWithoutMeta() throws Exception {
		final String applicationName= newTestApplicationName();
		@SuppressWarnings("unchecked")
		final Document resultDoc= doTestSubmitZipJob(applicationName,
				getTestDataStream("r-job-meta-required.zip"),
				Collections.EMPTY_MAP );
		final String resultUri= getResultUri(resultDoc);
		ponderUntilOneResultAvailable(resultUri);
		retrieveAndValidateZipError(resultUri);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void submitMultipartValidSingleFileJob() throws Exception {
		final String applicationName= newTestApplicationName();
		final Document resultDoc= doSubmitValidMultipartJob(applicationName,
				List.of(new UploadedFile("test.R")),
				Collections.EMPTY_MAP );
		final String resultUri= getResultUri(resultDoc);
		ponderRetrieveAndValidateZipResult(resultUri);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void submitMultipartValidMultiFilesJob() throws Exception {
		final String applicationName= newTestApplicationName();
		final Document resultDoc= doSubmitValidMultipartJob(applicationName,
				List.of(new UploadedFile("testSweave.Rnw"), new UploadedFile("testSweave.R")),
				Collections.EMPTY_MAP );
		final String resultUri= getResultUri(resultDoc);
		ponderRetrieveAndValidateZipResult(resultUri);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void submitMultipartValidZipJob() throws Exception {
		final String applicationName= newTestApplicationName();
		final Document resultDoc= doSubmitValidMultipartJob(applicationName,
				List.of(new UploadedFile("r-job-sample.zip")),
				Collections.EMPTY_MAP );
		final String resultUri= getResultUri(resultDoc);
		ponderRetrieveAndValidateZipResult(resultUri);
	}
	
	@Test
	public void submitMultipartValidDataOnlyZipJob() throws Exception {
		final String applicationName= newTestApplicationName();
		final Document resultDoc= doSubmitValidMultipartJob(applicationName,
				List.of(new UploadedFile("r-job-data-only.zip")),
				Map.of("X-RSB-Meta-rScript", "test.R") );
		final String resultUri= getResultUri(resultDoc);
		ponderRetrieveAndValidateZipResult(resultUri);
	}
	
	@Test
	public void submitMultipartValidJobRequiringMetaA() throws Exception {
		final String applicationName= newTestApplicationName();
		final Document resultDoc= doSubmitValidMultipartJob(applicationName,
				List.of(new UploadedFile("r-job-meta-required.zip")),
				Map.of("X-RSB-Meta-reportAuthor", "Jules Verne") );
		final String resultUri= getResultUri(resultDoc);
		ponderRetrieveAndValidateZipResult(resultUri);
	}
	
	@Test
	public void submitMultipartValidJobRequiringMetaB() throws Exception {
		final String applicationName= newTestApplicationName();
		final Document resultDoc= doSubmitValidMultipartJob(applicationName,
				List.of(new UploadedFile("r-job-meta-required.zip")),
				Map.of("X-RSB-Meta-#1", "reportAuthor= Jules Verne") );
		final String resultUri= getResultUri(resultDoc);
		ponderRetrieveAndValidateZipResult(resultUri);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testSubmitMultipartNoAttachedFile() throws Exception {
		final String applicationName= newTestApplicationName();
		final Document resultDoc= doSubmitValidMultipartJob(applicationName, Collections.EMPTY_LIST,
				Map.of("X-RSB-Meta-rScript", "test.R") );
		final String resultUri= getResultUri(resultDoc);
		ponderRetrieveAndValidateZipResult(resultUri);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void submitMultipartValidZipJobAsOctetStream() throws Exception {
		final String applicationName= newTestApplicationName();
		final Document resultDoc= doSubmitValidMultipartJob(applicationName,
				List.of(new UploadedFile("r-job-sample.zip", "application/octet-stream")),
				Collections.EMPTY_MAP );
		final String resultUri= getResultUri(resultDoc);
		ponderRetrieveAndValidateZipResult(resultUri);
	}
	
	
    // -------- Supporting Methods ---------

    private void retrieveAndValidateXmlResult(final String resultUri) throws Exception {
        final String dataUri= doTestResultAvailability(resultUri, true, "xml");
        final String result= getStringResponse(dataUri);
        final Document resultDocument= XMLUnit.buildTestDocument(result);
        assertXpathEvaluatesTo("provideMeansAndPlotPerDosageGroup", "/statDataPackage/statHeader/RFunction/text()", resultDocument);
    }

    private void retrieveAndValidateXmlError(final String resultUri, final String jobId) throws Exception {
        final String dataUri= doTestResultAvailability(resultUri, false, "xml");
        final String result= getStringResponse(dataUri);
        final Document resultDocument= XMLUnit.buildTestDocument(result);
        assertXpathEvaluatesTo(jobId, "/rsb:errorResult/@jobId", resultDocument);
    }

    private void retrieveAndValidateJsonResult(final String resultUri) throws Exception {
        final String dataUri= doTestResultAvailability(resultUri, true, "json");
        final String result= getStringResponse(dataUri);
        assertNotNull(Util.fromJson(result, List.class));
    }

    private void retrieveAndValidateJsonError(final String resultUri) throws Exception {
        final String dataUri= doTestResultAvailability(resultUri, false, "json");
        final String result= getStringResponse(dataUri);

        final Map<?, ?> jsonResult= Util.fromJson(result, Map.class);
        assertNotNull(jsonResult);
        assertNotNull(jsonResult.get("errorMessage"));
    }

    private Document doSubmitValidXmlJob(final String applicationName) throws IOException, SAXException, XpathException {
        return doSubmitXmlJob(applicationName, getTestDataStream("r-job-sample.xml"));
    }

    private Map<?, ?> doSubmitValidJsonJob(final String applicationName) throws IOException, SAXException, XpathException {
        return doTestSubmitJobWithJsonAck(applicationName, getTestDataStream("r-job-sample.json"));
    }

    private Map<?, ?> doSubmitInvalidJsonJob(final String applicationName) throws IOException, SAXException, XpathException {
        return doTestSubmitJobWithJsonAck(applicationName, new ByteArrayInputStream("not json".getBytes()));
    }

    private Document doSubmitInvalidXmlJob(final String applicationName) throws IOException, SAXException, XpathException {
        return doSubmitXmlJob(applicationName, new ByteArrayInputStream("<bad/>".getBytes()));
    }

    @SuppressWarnings("unchecked")
    private Document doSubmitXmlJob(final String applicationName, final InputStream xmlJob) throws IOException, SAXException,
            XpathException {
        return doTestSubmitXmlJobWithXmlAck(applicationName, xmlJob, Collections.EMPTY_MAP);
    }

    private Document doSubmitValidMultipartJob(final String applicationName, final List<UploadedFile> jobFiles,
            final Map<String, String> extraFields) throws Exception {
        // disable javascript because final we want a final normal form post final to happen and
        // final not an final AJAX one final
        final WebClient webClient= createNewWebClient();

        // get the upload form from RSB web server final
        final HtmlPage jobUploadPage= webClient.getPage(this.uploadFormUri);
        final HtmlForm jobUploadForm= jobUploadPage.getFormByName("jobUploadForm");

        // fill the form and submit it
        if (!jobFiles.isEmpty()) {
            final HtmlFileInput fileInput= jobUploadForm.getInputByName("X-RSB-JobFile[]");
            final UploadedFile firstFile= jobFiles.get(0);
            setUploadedFileOnInputControl(fileInput, firstFile);

            for (int i= 1; i < jobFiles.size(); i++) {
                final HtmlFileInput extraFileInput= (HtmlFileInput) fileInput.cloneNode(true);
                final UploadedFile uploadedFile= jobFiles.get(i);
                setUploadedFileOnInputControl(extraFileInput, uploadedFile);
                jobUploadForm.appendChild(extraFileInput);
            }
        }

        final HtmlInput applicationNameInput= jobUploadForm.getInputByName("X-RSB-Application-Name");
        applicationNameInput.setValueAttribute(applicationName);

        // deal with extra meta fields
        for (final Entry<String, String> extraField : extraFields.entrySet()) {
            final String inputName= extraField.getKey();
            HtmlInput input= null;

            try {
                input= jobUploadForm.getInputByName(inputName);
            } catch (final ElementNotFoundException enfe) {
                // generate the missing input on the fly (like a dynamic web form would do) by
                // cloning an existing one
                input= (HtmlInput) applicationNameInput.cloneNode(true);
                input.setAttribute("name", inputName);
                jobUploadForm.appendChild(input);
            }

            input.setValueAttribute(extraField.getValue());
        }

        final Page jobUploadConfirmationPage= jobUploadForm.getInputByName("jobSubmitButton").click();
        final com.gargoylesoftware.htmlunit.WebResponse response= jobUploadConfirmationPage.getWebResponse();

        // terminate web client and evaluate response validity webClient.closeAllWindows();

        return parseJobSubmissionXmlAckResponse(applicationName, response.getStatusCode(), response.getResponseHeaderValue("Content-Type"),
                response.getContentAsString());
    }

    private void setUploadedFileOnInputControl(final HtmlFileInput fileInput, final UploadedFile uploadedFile) {
        fileInput.setValueAttribute(getTestDataFile(uploadedFile.name).toString());

        if (uploadedFile.contentType != null) {
            fileInput.setContentType(uploadedFile.contentType);
        }
    }

    private void ponderRetrieveAndValidateZipResult(final String resultUri) throws Exception {
        ponderUntilOneResultAvailable(resultUri);
        retrieveAndValidateZipResult(resultUri);
    }

    private void retrieveAndValidateZipResult(final String resultUri) throws Exception {
        final String dataUri= doTestResultAvailability(resultUri, true, "zip");
        validateZipResult(getStreamResponse(dataUri));
    }

    private void retrieveAndValidateZipError(final String resultUri) throws Exception {
        final String dataUri= doTestResultAvailability(resultUri, false, "txt");
        validateErrorResult(getStreamResponse(dataUri));
    }

    private Document doTestSubmitZipJob(final String applicationName, final InputStream job, final Map<String, String> extraHeaders)
            throws IOException, SAXException, XpathException {
        return doTestSubmitJobWithXmlAck(applicationName, job, "application/zip", extraHeaders);
    }

    private String ponderUntilOneResultAvailable(final String watchUri) throws Exception {
        for (int attemptCount= 0; attemptCount < 60; attemptCount++) {

            Thread.sleep(500);

            final WebConversation wc= new WebConversation();
            final WebRequest request= new GetMethodWebRequest(watchUri);
            try {
                final WebResponse response= wc.sendRequest(request);
                final String responseText= response.getText();
                final Document responseDocument= XMLUnit.buildTestDocument(responseText);
                final String selfUri= XMLUnit.newXpathEngine().evaluate("//rsb:result/@selfUri", responseDocument);

                if (StringUtils.isNotBlank(selfUri)) {
                    return selfUri;
                }
            } catch (final RuntimeException e) {
                // ignore exceptions final due to 404
            }
        }
        fail("Result didn't come on time at URI: " + watchUri);
        return null;
    }

    private Document doTestSubmitXmlJobWithXmlAck(final String applicationName, final InputStream job,
            final Map<String, String> extraHeaders) throws IOException, SAXException, XpathException {
        return doTestSubmitJobWithXmlAck(applicationName, job, "application/xml", extraHeaders);
    }

    private Document doTestSubmitJobWithXmlAck(final String applicationName, final InputStream job, final String jobContentType,
            final Map<String, String> extraHeaders) throws IOException, SAXException, XpathException {
        final WebConversation wc= new WebConversation();
        final PostMethodWebRequest request= new PostMethodWebRequest(this.restJobsUri, job, jobContentType);
        request.setHeaderField("X-RSB-Application-Name", applicationName);

        for (final Entry<String, String> extraHeader : extraHeaders.entrySet()) {
            request.setHeaderField(extraHeader.getKey(), extraHeader.getValue());
        }

        final WebResponse response= wc.sendRequest(request);

        return parseJobSubmissionRsbXmlAckResponse(applicationName, response.getResponseCode(), response.getHeaderField("Content-Type"),
                response.getText());
    }

    private Document parseJobSubmissionRsbXmlAckResponse(final String applicationName, final int statusCode, final String contentType,
            final String entity) throws SAXException, IOException, XpathException {
        return parseJobSubmissionRsbXmlAckResponse(applicationName, statusCode, "application/vnd.rsb+xml", contentType, entity);
    }

    private Document parseJobSubmissionXmlAckResponse(final String applicationName, final int statusCode, final String contentType,
            final String entity) throws SAXException, IOException, XpathException {
        return parseJobSubmissionRsbXmlAckResponse(applicationName, statusCode, "application/xml", contentType, entity);
    }

    private Document parseJobSubmissionRsbXmlAckResponse(final String applicationName, final int statusCode,
            final String expectedContentType, final String contentType, final String entity) throws SAXException, IOException,
            XpathException {
        assertEquals(202, statusCode);
        assertEquals(expectedContentType, contentType);

        final Document responseDocument= XMLUnit.buildTestDocument(entity);
        assertXpathEvaluatesTo(applicationName, "/rsb:jobToken/@applicationName", responseDocument);
        assertXpathExists("/rsb:jobToken/@jobId", responseDocument);
        assertXpathExists("/rsb:jobToken/@resultUri", responseDocument);
        return responseDocument;
    }

    private Map<?, ?> doTestSubmitJobWithJsonAck(final String applicationName, final InputStream job) throws IOException, SAXException,
            XpathException {
        final WebConversation wc= new WebConversation();
        final WebRequest request= new PostMethodWebRequest(this.restJobsUri, job, "application/json");
        request.setHeaderField("X-RSB-Application-Name", applicationName);
        request.setHeaderField("Accept", "application/vnd.rsb+json");

        final WebResponse response= wc.sendRequest(request);

        assertEquals(202, response.getResponseCode());
        assertEquals("application/vnd.rsb+json", response.getHeaderField("Content-Type"));

        final Map<?, ?> jsonResult= (Map<?, ?>) Util.fromJson(response.getText(), Map.class).get("jobToken");
        assertEquals(applicationName, jsonResult.get("applicationName"));
        assertNotNull(jsonResult.get("jobId"));
        assertNotNull(jsonResult.get("resultUri"));
        return jsonResult;
    }

	private String doTestResultAvailability(final String resultUri,
			final boolean expectedSuccess, final String expectedType) throws Exception {
		final WebConversation wc= new WebConversation();
		final WebResponse response= wc.sendRequest(new GetMethodWebRequest(resultUri));
		assertEquals(200, response.getResponseCode());
		assertEquals("application/vnd.rsb+xml", response.getHeaderField("Content-Type"));
		
		final String responseText= response.getText();
		final Document responseDocument= XMLUnit.buildTestDocument(responseText);
		final XpathEngine xpathEngine= XMLUnit.newXpathEngine();
		assertXpathExists("//rsb:result/@jobId", responseDocument);
		assertXpathExists("//rsb:result/@applicationName", responseDocument);
		assertXpathExists("//rsb:result/@resultTime", responseDocument);
		assertXpathExists("//rsb:result/@selfUri", responseDocument);
		assertXpathExists("//rsb:result/@type", responseDocument);
		
		final String successString= xpathEngine.evaluate("//rsb:result/@success", responseDocument);
		if (!Boolean.toString(expectedSuccess).equals(successString)) {
			throw new ComparisonFailureWithDetail("success", expectedType, successString,
					(expectedSuccess) ? getErrorText(responseDocument) : null );
		}
		assertXpathEvaluatesTo(expectedType, "//rsb:result/@type", responseDocument);
		
		assertXpathExists("//rsb:result/@dataUri", responseDocument);
		return xpathEngine.evaluate("//rsb:result/@dataUri", responseDocument);
	}
	
	private String getErrorText(final Document responseDocument) {
		try {
			final XpathEngine xpathEngine= XMLUnit.newXpathEngine();
			if ("txt".equals(xpathEngine.evaluate("//rsb:result/@type", responseDocument))) {
				final String dataUri= xpathEngine.evaluate("//rsb:result/@dataUri", responseDocument);
				return getStringResponse(dataUri);
			}
		}
		catch (final Exception e) {}
		return "<not available>";
	}
	
    private static String getStringResponse(final String resultUri) throws IOException, SAXException {
        return getResponse(resultUri).getText();
    }

    private static InputStream getStreamResponse(final String resultUri) throws IOException, SAXException {
        return getResponse(resultUri).getInputStream();
    }

    private static WebResponse getResponse(final String resultUri) throws IOException, SAXException {
        final WebConversation wc= new WebConversation();
        final WebRequest request= new GetMethodWebRequest(resultUri);
        final WebResponse response= wc.sendRequest(request);
        return response;
    }

    private static String getResultUri(final Document responseDocument) throws XpathException {
        return XMLUnit.newXpathEngine().evaluate("/rsb:jobToken/@resultUri", responseDocument);
    }

    private static String getApplicationResultsUri(final Document responseDocument) throws XpathException {
        return XMLUnit.newXpathEngine().evaluate("/rsb:jobToken/@applicationResultsUri", responseDocument);
    }

    private static String getResultUri(final Map<?, ?> jsonResult) {
        return jsonResult.get("resultUri").toString();
    }

    private static String getApplicationResultsUri(final Map<?, ?> jsonResult) {
        return jsonResult.get("applicationResultsUri").toString();
    }

    private static class UploadedFile {
        String name;
        String contentType;

        UploadedFile(final String name) {
            this(name, null);
        }

        UploadedFile(final String name, final String contentType) {
            this.name= name;
            this.contentType= contentType;
        }
    }

    private static void doTestUnsupportedDeleteMethod(final String requestUri) throws IOException, SAXException {
        final WebConversation wc= new WebConversation();
        final WebRequest request= new DeleteMethodWebRequest(requestUri);
        try {
            wc.sendRequest(request);
            fail("an exception should have been raised");
        } catch (final HttpException he) {
            assertEquals(405, he.getResponseCode());
        }
    }

    private static WebClient createNewWebClient() {
        final WebClient webClient= new WebClient();
        webClient.getOptions().setJavaScriptEnabled(false);
        return webClient;
    }
}
