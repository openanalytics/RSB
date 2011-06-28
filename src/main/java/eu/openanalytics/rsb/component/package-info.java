/**
 * <p>This is the R Service Bus (RSB) API.</p>
 *  
 * <h3>SOAP+MTOM API (Synchronous)</h3>
 * <p>The RSB SOAP+MTOM API is fully described by its <a href="/api-specs.html">WSDL and the related XSDs</a>.</p>
 * <h3>REST API (Asynchronous)</h3>
 * <p>The RSB REST API is documented below. Its micro-format is defined in the <a href="/api-specs.html">XSDs for XML message defintions</a>.
 *    The JSON equivalents of these XML messages is built following the Jettison conventions as highlighted in <a href="https://cwiki.apache.org/CXF20DOC/json-support.html">this CXF documentation</a>.</p>
 * <p>The REST API supports a set of HTTP headers:
 * <ul>
 * <li>Content-Type: required, defines the format of the data being sent by the client. Either application/json, application/xml or application/zip.</li>
 * <li>Accept: optional, defines the micro-format used by RSB when responding to a request. Either: application/vnd.rsb+xml or application/vnd.rsb+json (defaults to the former if absent).</li>
 * <li>X-RSB-Application-Name: required, a valid RSB application name (ie. must match this regular expression: /w+).</li>
 * <li>X-RSB-Meta-rScript and X-RSB-Meta-sweaveFile: standard optional job meta information parameters.</li>
 * <li>X-RSB-Meta-*: custom optional job-specific meta information parameters.</li>
 * </ul>
 * </p>
 * <p>Any HTTP client that supports the GET, POST and DELETE verbs and HTTP headers can be used to interact with the REST API.</p>
 * <h4>Example Interaction</h4>
 * <p><i>This interaction will be done using the XML RSB micro-format. Only the significant HTTP headers are shown. Posting XML and JSON jobs is done the same way, only with different content types.</i></p>
 * <h5>Posting a job</h5>
 * <p>Request:</p>
 * <pre>
 * POST /rsb/api/rest/jobs HTTP/1.1
 * Content-Type: application/zip
 * X-RSB-Application-Name: testApp
 * 
 * ...ZIP data...
 * </pre>
 * <p>Response:</p>
 * <pre>
 * HTTP/1.1 202 Accepted
 * Content-Type: application/vnd.rsb+xml
 * 
 * &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?>
 * &lt;jobToken xmlns="http://rest.rsb.openanalytics.eu/types"
 *           jobId="1f8c8aea-8d90-4bdf-92bb-b8da52f35e58"
 *           applicationName="testApp"
 *           submissionTime="2011-06-28T17:07:03.220Z"
 *           applicationResultsUri="http://localhost:8888/rsb/api/rest/results/testApp"
 *           resultUri="http://localhost:8888/rsb/api/rest/results/testApp/1f8c8aea-8d90-4bdf-92bb-b8da52f35e58"/>
 * </pre>
 * <p>The job token contains the necessary links to either browse all the jobs for the concerned application or to retrieve the result of the current job.</p>
 * <h5>Retrieving a result</h5>
 * <p>Request:</p>
 * <pre>
 * GET /rsb/api/rest/results/testApp/1f8c8aea-8d90-4bdf-92bb-b8da52f35e58 HTTP/1.1
 * </pre>
 * <p>Response when the result is not ready:<p>
 * <pre>
 * HTTP/1.1 404 Not Found
 * </pre>
 * <p>Response when the result is ready:<p>
 * <pre>
 * HTTP/1.1 200 OK
 * Content-Type: application/vnd.rsb+xml
 * &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?>
 * &lt;result xmlns="http://rest.rsb.openanalytics.eu/types"
 *            jobId="1f8c8aea-8d90-4bdf-92bb-b8da52f35e58"
 *            applicationName="testApp"
 *            resultTime="2011-06-28T17:07:03.000Z"
 *            success="true"
 *            type="zip"
 *            selfUri="http://localhost:8888/rsb/api/rest/results/testApp/1f8c8aea-8d90-4bdf-92bb-b8da52f35e58"
 *            dataUri="http://localhost:8888/rsb/api/rest/result/testApp/1f8c8aea-8d90-4bdf-92bb-b8da52f35e58.zip"/>
 * </pre>
 * <p>As you can see, this is only the metadata for a result. The actual result data is reachable, with an HTTP GET, using the the provided data URI.</p>
 * <h5>Deleting a result</h5>
 * <p>Request:</p>
 * <pre>
 * DELETE /rsb/api/rest/results/testApp/1f8c8aea-8d90-4bdf-92bb-b8da52f35e58 HTTP/1.1
 * </pre>
 * <p>Response:</p>
 * <pre>
 * HTTP/1.1 204 No Content
 * </pre>
 * <h4>Browser Client</h4>
 * <p>It is also possible to use the REST API from a browser, using a standard HTML form. In that case the following rules apply:
 * <ul>
 * <li>Content-Type must be multipart/form-data upload.</li>
 * <li>Accept: must be either application/xml or application/json (content sub-types are not well understood by browsers, hence the usage of the canonical XML and JSON types).</li>
 * <li>The X-RSB-* headers must be passed as form input field values named after the HTTP header they pass a value for.</li>
 * <li>File attachments must be passed as form input fields of type 'file' that must be named: X-RSB-JobFile[]</li>
 * </ul>
 * </p>
 */
@XmlSchema(namespace = "http://soap.rsb.openanalytics.eu/jobs")
package eu.openanalytics.rsb.component;

import javax.xml.bind.annotation.XmlSchema;

