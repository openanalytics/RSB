/**
 * <p>This is the R Service Bus (RSB) API.</p>
 *  
 * <h3>SOAP+MTOM API (Synchronous)</h3>
 * <p>The RSB SOAP+MTOM API is fully described by its <a href="/api-specs.html">WSDL and the related XSDs</a>.</p>
 * <h3>REST API (Asynchronous)</h3>
 * <p>The RSB REST API is documented below. It also relies on <a href="/api-specs.html">XSDs for XML message defintions</a>.
 *    The JSON equivalents of these XML messages is built following the Jettison conventions as highlighted in <a href="https://cwiki.apache.org/CXF20DOC/json-support.html">this CXF documentation</a>.</p>
 * <p>The REST API supports a set of HTTP headers:
 * <ul>
 * <li>X-RSB-Application-Name: required, a valid RSB application name (ie. must match this regular expression: /w+).</li>
 * <li>X-RSB-Meta-rScript and X-RSB-Meta-sweaveFile: standard optional job meta information parameters.</li>
 * <li>X-RSB-Meta-*: custom optional job-specific meta information parameters.</li>
 * </ul>
 * <p>If multiple files are passed to the REST API as a multipart/form-data upload, the input name must be: X-RSB-JobFile[]</p>
 * </p>   
 */
@XmlSchema(namespace = "http://soap.rsb.openanalytics.eu/jobs")
package eu.openanalytics.rsb.component;

import javax.xml.bind.annotation.XmlSchema;

