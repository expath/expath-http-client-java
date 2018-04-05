/****************************************************************************/
/*  File:       BodyFactory.java                                            */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-02-03                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient.impl;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.message.BasicHeader;
import org.expath.httpclient.ContentType;
import org.expath.httpclient.HeaderSet;
import org.expath.httpclient.HttpClientException;
import org.expath.httpclient.HttpConnection;
import org.expath.httpclient.HttpRequestBody;
import org.expath.httpclient.HttpResponseBody;
import org.expath.httpclient.model.Result;
import org.expath.tools.model.Element;
import org.expath.tools.model.Sequence;


/**
 * Factory class for bodies, either in requests or in responses.
 *
 * @author Florent Georges
 */
public class BodyFactory {
    // TODO: Take new methods into account (XHTML, BASE64 and HEX).
    public static HttpRequestBody makeRequestBody(final Element elem, final Sequence bodies, final String ns)
            throws HttpClientException {
        // method is got from @method if any...
        Type method = parseMethod(elem);
        // ...or from @media-type if no @method
        if (method == null) {
            method = parseType(elem);
        }
        switch (method) {
            case MULTIPART:
                return new MultipartRequestBody(elem, bodies, ns);
            case SRC:
                return new HrefRequestBody(elem);
            case XML:
            case HTML:
            case XHTML:
            case TEXT:
            case BINARY:
            case HEX:
            case BASE64:
                return new SinglePartRequestBody(elem, bodies, method);
            default:
                throw new HttpClientException("could not happen");
        }
    }

    public static HttpResponseBody makeResponseBody(final Result result, final ContentType type, final HttpConnection conn)
            throws HttpClientException {
        if (type == null) {
            // it is legitimate to not have a body in a response; for instance
            // on a "304 Not Modified"
            return null;
        }
        String t = type.getType();
        if (t == null) {
            return null;
        }
        final InputStream in = conn.getResponseStream();
        if (in == null) {
            return null;
        }
        if (t.startsWith("multipart/")) {
            return new MultipartResponseBody(result, in, type);
        } else {
            return makeResponsePart(result, null, in, type);
        }
    }

    // package-level to be used within MultipartResponseBody ctor
    // TODO: Take new methods into account (XHTML, BASE64 and HEX).
    static HttpResponseBody makeResponsePart(final Result result, final HeaderSet headers, final InputStream in, final ContentType ctype)
            throws HttpClientException {
        switch (parseType(ctype)) {
            case XML:
                // TODO: 'content_type' is the header Content-Type without any param
                // (i.e. "text/xml".)  Should we keep this, or put the whole header
                // (i.e. "text/xml; charset=utf-8")? (and for other types as well...)
                return new XmlResponseBody(result, in, ctype, headers, false);
            case HTML:
                return new XmlResponseBody(result, in, ctype, headers, true);
            case TEXT:
                return new TextResponseBody(result, in, ctype, headers);
            case BINARY:
                return new BinaryResponseBody(result, in, ctype, headers);
            default:
                throw new HttpClientException("INTERNAL ERROR: cannot happen");
        }
    }

    public enum Type {
        XML,
        HTML,
        XHTML,
        TEXT,
        BINARY,
        BASE64,
        HEX,
        MULTIPART,
        SRC
    }

    /**
     * Media types that must be treated as text types (in addition to text/*).
     */
    private static final Set<String> TEXT_TYPES = new HashSet<>();
    static {
        TEXT_TYPES.add("application/x-www-form-urlencoded");
        TEXT_TYPES.add("application/xml-dtd");
    }

    /**
     * Media types that must be treated as XML types (in addition to *+xml).
     */
    private static final Set<String> XML_TYPES = new HashSet<>();
    static {
        // Doc: does not handle "application/xml-dtd" as XML
        // TODO: What about ".../xml-external-parsed-entity" ?
        XML_TYPES.add("text/xml");
        XML_TYPES.add("application/xml");
        XML_TYPES.add("text/xml-external-parsed-entity");
        XML_TYPES.add("application/xml-external-parsed-entity");
    }

    /**
     * Decode the content type from a MIME type string.
     * <p>
     * TODO: Take new methods into account (XHTML, BASE64 and HEX).
     */
    private static Type parseType(final String type) {
        if (type.startsWith("multipart/")) {
            return Type.MULTIPART;
        } else if ("text/html".equals(type)) {
            return Type.HTML;
        } else if (type.endsWith("+xml") || XML_TYPES.contains(type)) {
            return Type.XML;
        } else if (type.startsWith("text/") || TEXT_TYPES.contains(type)) {
            return Type.TEXT;
        } else {
            return Type.BINARY;
        }
    }

    /**
     * Look for the header COntent-Type in a header set and decode it.
     */
    public static Type parseType(final HeaderSet headers) throws HttpClientException {
        final Header h = headers.getFirstHeader("Content-Type");
        if (h == null) {
            throw new HttpClientException("impossible to find the content type");
        }
        final ContentType ct = new ContentType(h);
        return parseType(ct);
    }

    /**
     * Decode the content type from a ContentType object.
     */
    public static Type parseType(final ContentType type) throws HttpClientException {
        if (type == null) {
            throw new HttpClientException("impossible to find the content type");
        }
        final String t = type.getType();
        if (t == null) {
            throw new HttpClientException("impossible to find the content type");
        }
        return parseType(t);
    }

    /**
     * Parse the @media-type from a http:body or http:multipart element.
     */
    private static Type parseType(final Element elem) throws HttpClientException {
        final String local = elem.getLocalName();
        if ("multipart".equals(local)) {
            return Type.MULTIPART;
        } else if (!"body".equals(local)) {
            throw new HttpClientException("INTERNAL ERROR: cannot happen, checked before");
        } else {
            if (elem.getAttribute("src") != null) {
                return Type.SRC;
            }
            final String mediaType = elem.getAttribute("media-type");
            if (mediaType == null) {
                throw new HttpClientException("@media-type is not set on http:body");
            }
            final Header mediaTypeHeader = new BasicHeader("Media-Type", mediaType);
            final HeaderElement[] mediaTypeHeaderElems = mediaTypeHeader.getElements();
            if (mediaTypeHeaderElems == null || mediaTypeHeaderElems.length == 0) {
                throw new HttpClientException("@media-type is not set on http:body");
            } else if (mediaTypeHeaderElems.length > 1) {
                throw new HttpClientException("Multiple @media-type internet media types present");
            } else {
                final Type type = parseType(mediaTypeHeaderElems[0].getName());
                if (type == Type.MULTIPART) {
                    final String msg = "multipart type not allowed for http:body: " + mediaType;
                    throw new HttpClientException(msg);
                }
                return type;
            }
        }
    }

    /**
     * Parse the @method from a http:body or http:multipart element.
     * <p>
     * Return null if there is no @method.
     */
    private static Type parseMethod(final Element elem) throws HttpClientException {
        final String m = elem.getAttribute("method");
        if (m == null) {
            return null;
        } else if ("xml".equals(m)) {
            return Type.XML;
        } else if ("html".equals(m)) {
            return Type.HTML;
        } else if ("xhtml".equals(m)) {
            return Type.XHTML;
        } else if ("text".equals(m)) {
            return Type.TEXT;
        } else if ("binary".equals(m)) {
            return Type.BINARY;
        }
        // FIXME: The spec says "binary", but I think we need "base64" and "hex"
        // instead (or in addition, if "binary" is left implementation-defined).
        else if ("base64".equals(m)) {
            return Type.BASE64;
        } else if ("hex".equals(m)) {
            return Type.HEX;
        } else {
            throw new HttpClientException("Incorrect value for @method: " + m);
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
/*  The Initial Developer of the Original Code is Florent Georges.          */
/*                                                                          */
/*  Contributor(s): none.                                                   */
/* ------------------------------------------------------------------------ */
