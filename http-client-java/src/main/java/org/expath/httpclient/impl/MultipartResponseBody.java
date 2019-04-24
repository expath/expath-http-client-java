/****************************************************************************/
/*  File:       MultipartResponseBody.java                                  */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-02-04                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.stream.EntityState;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.MimeTokenStream;
import org.expath.httpclient.ContentType;
import org.expath.httpclient.HeaderSet;
import org.expath.httpclient.HttpClientException;
import org.expath.httpclient.HttpResponseBody;
import org.expath.httpclient.model.Result;
import org.expath.httpclient.model.TreeBuilder;
import org.expath.tools.ToolsException;

/**
 * A multipart body in the response.
 *
 * @author Florent Georges
 */
public class MultipartResponseBody implements HttpResponseBody {

    public MultipartResponseBody(final Result result, final InputStream in, final ContentType type)
            throws HttpClientException {
        if (type == null || type.getType() == null) {
            throw new HttpClientException("No content type");
        }

        myContentType = type;
        myParts = new ArrayList<>();

        myBoundary = type.getBoundary();
        if (myBoundary == null) {
            throw new HttpClientException("No boundary");
        }
        try {
            analyzeParts(result, in);
        } catch (IOException ex) {
            throw new HttpClientException("error reading the response stream", ex);
        }
    }

    @Override
    public void outputBody(final TreeBuilder b) throws HttpClientException {
        try {
            b.startElem("multipart");
            b.attribute("media-type", myContentType.getValue());
            b.attribute("boundary", myBoundary);
            b.startContent();
            for (final HttpResponseBody part : myParts) {
                part.outputBody(b);
            }
            b.endElem();
        } catch (final ToolsException ex) {
            throw new HttpClientException("Error building the body", ex);
        }
    }

    private void analyzeParts(final Result result, final InputStream in) throws IOException, HttpClientException {
        final MimeTokenStream parser = new MimeTokenStream();

        final String contentType;
        if (myContentType.getCharset() != null) {
            contentType = myContentType.getType() + "; charset=" + myContentType.getCharset();
        } else {
            contentType = myContentType.getType();
        }
        parser.parseHeadless(in, contentType);
        try {
            HeaderSet headers = null;
            for (EntityState state = parser.getState();
                 state != EntityState.T_END_OF_STREAM;
                 state = parser.next()) {
                if (state == EntityState.T_START_HEADER) {
                    headers = new HeaderSet();
                }
                handleParserState(result, parser, headers);
            }
        } catch (final MimeException ex) {
            throw new HttpClientException("The response content is ill-formed.", ex);
        }
    }

    private void handleParserState(final Result result, final MimeTokenStream parser, final HeaderSet headers) throws HttpClientException {
        final EntityState state = parser.getState();
        if (LOG.isDebugEnabled()) {
            LOG.debug(MimeTokenStream.stateToString(state));
        }
        switch (state) {
            // It seems that in a headless parsing, END_HEADER appears
            // right after START_MESSAGE (without the corresponding
            // START_HEADER).  So if headers == null, we can just ignore
            // this state.
            case T_END_HEADER:
                // TODO: Just ignore anyway...?
                break;
            case T_FIELD:
                final Field f = parser.getField();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("  field: " + f);
                }
                headers.add(f.getName(), parseFieldBody(f));
                break;
            case T_BODY:
                if (LOG.isDebugEnabled()) {
                    LOG.debug("  body desc: " + parser.getBodyDescriptor());
                }
                final HttpResponseBody b = makeResponsePart(result, headers, parser);
                myParts.add(b);
                break;
            // START_HEADER is handled in the calling analyzeParts()
            case T_START_HEADER:
            case T_END_BODYPART:
            case T_END_MESSAGE:
            case T_END_MULTIPART:
            case T_EPILOGUE:
            case T_PREAMBLE:
            case T_START_BODYPART:
            case T_START_MESSAGE:
            case T_START_MULTIPART:
                // ignore
                break;
            // In a first time, take a very defensive approach, and
            // throw an error for all unexpected states, even if we
            // should discover slowly that we should probably just
            // ignore some of them.
            default:
                final String s = MimeTokenStream.stateToString(state);
                throw new HttpClientException("Unknown parsing state: " + s);
        }
    }

    private String parseFieldBody(final Field f) {
//        try {
        // WHy did I use AbstractField in the first place?
        final String b = f.getBody() /* AbstractField.parse(f.getRaw()).getBody() */;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Field: " + f.getName() + ": [" + b + "]");
        }
        return b;
//        }
//        catch ( MimeException ex ) {
//            LOG.error("Field value parsing error (" + f + ")", ex);
//            throw new HttpClientException("Field value parsing error (" + f + ")", ex);
//        }
    }

    private HttpResponseBody makeResponsePart(final Result result, final HeaderSet headers, final MimeTokenStream parser)
            throws HttpClientException {
        final Header h = headers.getFirstHeader("Content-Type");
        if (h == null) {
            throw new HttpClientException("impossible to find the content type");
        }
        final ContentType type = ContentType.parse(h, null, null);
        try {
            switch (BodyFactory.parseType(type)) {
                case XML: {
                    final Reader in = parser.getReader();
                    return new XmlResponseBody(result, in, type, headers, false);
                }
                case HTML: {
                    final Reader in = parser.getReader();
                    return new XmlResponseBody(result, in, type, headers, true);
                }
                case TEXT: {
                    final Reader in = parser.getReader();
                    return new TextResponseBody(result, in, type, headers);
                }
                case BINARY: {
                    final InputStream in = parser.getInputStream();
                    return new BinaryResponseBody(result, in, type, headers);
                }
                default:
                    throw new HttpClientException("INTERNAL ERROR: cannot happen");
            }
        } catch (final UnsupportedEncodingException ex) {
            throw new HttpClientException("Unable to parse response part", ex);
        }
    }

    private List<HttpResponseBody> myParts;
    private ContentType myContentType;
    private String myBoundary;
    private static final Log LOG = LogFactory.getLog(MultipartResponseBody.class);
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
/*  The Initial Developer of the Original Code is Florent Georges.          */
/*                                                                          */
/*  Contributor(s): none.                                                   */
/* ------------------------------------------------------------------------ */
