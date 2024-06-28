/****************************************************************************/
/*  File:       org.expath.httpclient.saxon.SendRequestFunctionIT.java      */
/*  Author:     A. Retter - adamretter.org.uk                               */
/*  Date:       2024-06-28                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2024 Adam Retter (see end of file.)                   */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient.saxon;

import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import net.sf.saxon.s9api.*;
import org.expath.httpclient.HttpConstants;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.*;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

public class SendRequestFunctionIT {

  @ClassRule public static WireMockRule wireMockRule = new WireMockRule(Options.DYNAMIC_PORT);

  private static final Processor PROCESSOR = new Processor(false);
  private static XsltExecutable SIMPLE_REQUEST_TRANSFORM;

  private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
  private static javax.xml.parsers.DocumentBuilder DOCUMENT_BUILDER;

  private static final String PARAM_REQUEST = "request";

  private static final String ARGUMENT_ENDPOINT_URL = "ENDPOINT_URL";

  private static final Predicate<String> IS_UUID = value -> {
    try {
      UUID.fromString(value);
      return true;
    } catch (final IllegalArgumentException e) {
      return false;
    }
  };

  @BeforeClass
  public static void setup() throws IOException, SaxonApiException, ParserConfigurationException {
    // Register the EXPath HTTP Client Function for with Saxon
    PROCESSOR.registerExtensionFunction(new SendRequestFunction());

    // Compile our transformations
    final XsltCompiler xsltCompiler = PROCESSOR.newXsltCompiler();
    try (final InputStream is = SendRequestFunctionIT.class.getResourceAsStream("simple-request.xslt")) {
      SIMPLE_REQUEST_TRANSFORM = xsltCompiler.compile(new StreamSource(is));
    }

    // Setup the org.w3c.dom Document Builder
    DOCUMENT_BUILDER_FACTORY.setNamespaceAware(true);
    DOCUMENT_BUILDER = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
  }

  @Test
  public void simpleRequestGet() throws IOException, SaxonApiException, ParserConfigurationException {
    final String endpoint = "/simpleRequestGet";

    // Stub the HTTP Server endpoint for this test
    wireMockRule.stubFor(
        get(endpoint)
            .willReturn(ok()
              .withHeader("Content-Type", "application/xml")
              .withBody("<response>SUCCESS</response>"))
    );

    // the EXPath HTTP Client request to test
    final String inputXmlFilename = "simple-request-get.input.xml";

    // the expected result
    final String expectedResultFilename = "simple-request-get.expected.xml";

    // perform the request and assert the result
    assertSimpleRequest(endpoint, inputXmlFilename, expectedResultFilename);
  }

  @Test
  public void simpleRequestPut() throws IOException, SaxonApiException, ParserConfigurationException {
    final String endpoint = "/simpleRequestPut";

    // Stub the HTTP Server endpoint for this test
    wireMockRule.stubFor(
        put(endpoint)
            .withHeader("Content-Type", equalTo("application/xml; charset=UTF-8"))
            .withRequestBody(equalToXml("<data>hello</data>"))
            .willReturn(created()
                .withHeader("Content-Type", "application/xml")
                .withBody("<response>SUCCESS</response>"))
    );

    // the EXPath HTTP Client request to test
    final String inputXmlFilename = "simple-request-put.input.xml";

    // the expected result
    final String expectedResultFilename = "simple-request-put.expected.xml";

    // perform the request and assert the result
    assertSimpleRequest(endpoint, inputXmlFilename, expectedResultFilename);
  }

  @Test
  public void simpleRequestPost() throws IOException, SaxonApiException, ParserConfigurationException {
    final String endpoint = "/simpleRequestPost";

    // Stub the HTTP Server endpoint for this test
    wireMockRule.stubFor(
        post(endpoint)
            .withHeader("Content-Type", equalTo("application/json"))
            .withRequestBody(equalToJson("{ \"key1\": \"value1\", \"key2\": \"value2\" }"))
            .willReturn(aResponse().withStatus(202)
                .withHeader("Content-Type", "application/xml")
                .withBody("<response>SUCCESS</response>"))
    );

    // the EXPath HTTP Client request to test
    final String inputXmlFilename = "simple-request-post.input.xml";

    // the expected result
    final String expectedResultFilename = "simple-request-post.expected.xml";

    // perform the request and assert the result
    assertSimpleRequest(endpoint, inputXmlFilename, expectedResultFilename);
  }

  private void assertSimpleRequest(final String endpoint, final String inputXmlFilename, final String expectedResultFilename) throws IOException, SaxonApiException, ParserConfigurationException {
    final String request;
    try (final InputStream isRequest = getClass().getResourceAsStream(inputXmlFilename)) {
      request = readString(isRequest, Argument(ARGUMENT_ENDPOINT_URL, endpointUrl(endpoint)));
    }

    // run the XSLT
    final Document result = transform(SIMPLE_REQUEST_TRANSFORM, Params(
        ElementParam(PARAM_REQUEST, request)
    ));

    assertNotNull(result);

    // NOTE(AR) - START DEBUG RESPONSE
//    final StringWriter stringWriter = new StringWriter();
//    Serializer serializer = PROCESSOR.newSerializer(stringWriter);
//    XdmNode source = PROCESSOR.newDocumentBuilder().wrap(result);
//    serializer.serializeNode(source);
//    System.out.println(stringWriter);
    // NOTE(AR) - END DEBUG RESPONSE

    try (final InputStream is = getClass().getResourceAsStream(expectedResultFilename)) {
      final Source expected = Input.fromStream(is).build();

      final Source actual = Input.fromDocument(result).build();
      final Diff diff = DiffBuilder
          .compare(expected)
          .withTest(actual)
          .ignoreWhitespace()
          .checkForSimilar()
          .withDifferenceEvaluator(DifferenceEvaluators.chain(
              DifferenceEvaluators.Default,
              new SpentMillisAttrDifferenceEvaluator(),
              new HttpHeaderElementDifferenceEvaluator("matched-stub-id", IS_UUID)))
          .build();

      assertFalse(diff.toString(), diff.hasDifferences());
    }
  }

  private static String endpointUrl(final String endpoint) {
      return "http://localhost:" + wireMockRule.port() + endpoint;
  }

  private static Document transform(final XsltExecutable xsltExecutable, final Params params) throws SaxonApiException, ParserConfigurationException {
    final XsltTransformer xsltTransformer = xsltExecutable.load();

    for (final Param param : params) {
      xsltTransformer.setParameter(param.name, param.value);
    }

    xsltTransformer.setInitialTemplate(new QName("make-request"));

    final Document document = DOCUMENT_BUILDER.newDocument();
    xsltTransformer.setDestination(new DOMDestination(document));

    xsltTransformer.transform();

    return document;
  }

  private static Param DocumentParam(final String name, final String xml) throws SaxonApiException, IOException {
    return Param(name, parse(xml));
  }

  private static Param ElementParam(final String name, final String xml) throws SaxonApiException, IOException {
    return Param(name, parse(xml).getOutermostElement());
  }

  private static Param Param(final String name, final XdmValue xdmValue) {
    return new Param(new QName(name), xdmValue);
  }

  private static class Param {
    final QName name;
    final XdmValue value;

    private Param(final QName name, final XdmValue value) {
      this.name = name;
      this.value = value;
    }
  }

  private static class Params implements Iterable<Param> {
    final Param[] params;

    private Params(final Param[] params) {
      this.params = params;
    }

    @Override
    public Iterator<Param> iterator() {
      return Arrays.asList(params).iterator();
    }
  }

  private static Params Params(final Param... params) {
    return new Params(params);
  }

  private static XdmNode parse(final String xml) throws IOException, SaxonApiException {
    final DocumentBuilder documentBuilder = PROCESSOR.newDocumentBuilder();
    try (final Reader reader = new StringReader(xml)) {
      return documentBuilder.build(new StreamSource(reader));
    }
  }

  private static Argument Argument(final String name, final String value) {
    return new Argument(name, value);
  }

  private static class Argument {
    final String name;
    final String value;

    private Argument(final String name, final String value) {
      this.name = name;
      this.value = value;
    }
  }

  private static String readString(final InputStream is, final Argument... arguments) throws IOException {
    final StringBuilder stringBuilder = readString(is);
    for (final Argument argument : arguments) {
      int i = -1;
      final String lookup = "${" + argument.name + "}";
      while ((i = stringBuilder.indexOf(lookup)) != -1) {
        stringBuilder.replace(i, i + lookup.length(), argument.value);
      }
    }
    return stringBuilder.toString();
  }

  private static StringBuilder readString(final InputStream is) throws IOException {
    final StringBuilder stringBuilder = new StringBuilder();
    final char[] buf = new char[1024];
    int n = -1;
    final Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
    while ((n = reader.read(buf)) != -1) {
      stringBuilder.append(buf, 0, n);
    }
    return stringBuilder;
  }

  private static class SpentMillisAttrDifferenceEvaluator implements DifferenceEvaluator {

    @Override
    public ComparisonResult evaluate(final Comparison comparison, final ComparisonResult outcome) {
      final Comparison.Detail expected = comparison.getControlDetails();
      final Comparison.Detail actual = comparison.getTestDetails();

      final Node expectedNode = expected.getTarget();
      final Node actualNode = actual.getTarget();

      if (expectedNode instanceof Attr && expectedNode.getLocalName().equals("spent-millis")
          && actualNode instanceof Attr && actualNode.getLocalName().equals("spent-millis")) {
        if (expectedNode.getTextContent().equals(actualNode.getTextContent())) {
          return ComparisonResult.EQUAL;
        }

        try {
          Integer.valueOf(expectedNode.getTextContent());
          Integer.valueOf(actualNode.getTextContent());
        } catch (final NumberFormatException e) {
          return ComparisonResult.DIFFERENT;
        }

        // as they both parse as integers, they are similar enough
        return ComparisonResult.SIMILAR;
      }

      return outcome;
    }
  }

  private static class HttpHeaderElementDifferenceEvaluator implements DifferenceEvaluator {

    private final String headerName;
    private final Predicate<String> headerValuePredicate;

    private final Set<Attr> approvedValueAttrs = new HashSet<>();

    public HttpHeaderElementDifferenceEvaluator(final String headerName, final Predicate<String> headerValuePredicate) {
      this.headerName = headerName;
      this.headerValuePredicate = headerValuePredicate;
    }

    @Override
    public ComparisonResult evaluate(final Comparison comparison, final ComparisonResult outcome) {
      final Comparison.Detail expected = comparison.getControlDetails();
      final Comparison.Detail actual = comparison.getTestDetails();

      final Node expectedNode = expected.getTarget();
      final Node actualNode = actual.getTarget();

      // 1) check for http:header element with similar value attributes
      if (expectedNode instanceof Element && expectedNode.getLocalName().equals("header") && expectedNode.getNamespaceURI().equals(HttpConstants.HTTP_CLIENT_NS_URI)
          && actualNode instanceof Element && actualNode.getLocalName().equals("header") && actualNode.getNamespaceURI().equals(HttpConstants.HTTP_CLIENT_NS_URI)) {

        @Nullable final String expectedHeaderName = ((Element) expectedNode).getAttribute("name");
        @Nullable final String actualHeaderName = ((Element) actualNode).getAttribute("name");

        if (Objects.equals(headerName, expectedHeaderName) && Objects.equals(expectedHeaderName, actualHeaderName)) {

          @Nullable final Attr expectedHeaderValueNode = ((Element) expectedNode).getAttributeNode("value");
          @Nullable final Attr actualHeaderValueNode = ((Element) actualNode).getAttributeNode("value");

          if (expectedHeaderValueNode == null ^ actualHeaderValueNode == null) {
            return ComparisonResult.DIFFERENT;
          }

          if (Objects.equals(expectedHeaderValueNode, actualHeaderValueNode)) {
            approvedValueAttrs.add(expectedHeaderValueNode);
            approvedValueAttrs.add(actualHeaderValueNode);
            return ComparisonResult.EQUAL;
          }

          @Nullable final String expectedHeaderValue = expectedHeaderValueNode.getValue();
          @Nullable final String actualHeaderValue = actualHeaderValueNode.getValue();

          // if they both pass the predicate then they are similar enough
          if (headerValuePredicate.test(expectedHeaderValue) && headerValuePredicate.test(actualHeaderValue)) {
            approvedValueAttrs.add(expectedHeaderValueNode);
            approvedValueAttrs.add(actualHeaderValueNode);
            return ComparisonResult.SIMILAR;
          }

          return ComparisonResult.DIFFERENT;
        }
      }

      // 2) check for previously approved value attributes (from above)
      if (expectedNode instanceof Attr && expectedNode.getLocalName().equals("value") && approvedValueAttrs.contains(expectedNode)
          && actualNode instanceof Attr && actualNode.getLocalName().equals("value") && approvedValueAttrs.contains(actualNode)) {
        return ComparisonResult.SIMILAR;
      }

      return outcome;
    }
  }
}


/* ------------------------------------------------------------------------ */
/*  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS COMMENT.               */
/*                                                                          */
/*  The contents of this file are subject to the Mozilla Public License     */
/*  Version 1.0 (the "License"); you may not use this file except in        */
/*  compliance with the License. You may obtain a copy of the License at    */
/*  http://www.mozilla.org/MPL/.                                            */
/*                                                                          */
/*  Software distributed under the License is distributed on an "AS IS"     */
/*  basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.  See    */
/*  the License for the specific language governing rights and limitations  */
/*  under the License.                                                      */
/*                                                                          */
/*  The Original Code is: all this file.                                    */
/*                                                                          */
/*  The Initial Developer of the Original Code is Adam Retter.              */
/*                                                                          */
/*  Contributor(s): none.                                                   */
/* ------------------------------------------------------------------------ */