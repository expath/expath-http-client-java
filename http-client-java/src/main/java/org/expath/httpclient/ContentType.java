/****************************************************************************/
/*  File:       ContentType.java                                            */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-02-22                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;

import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Represent a Content-Type header.
 * <p>
 * Provide the ability to get the boundary param in case of a multipart
 * content type on the one hand, and the ability to get only the MIME type
 * string without any param on the other hand.
 *
 * @author Florent Georges
 */
public class ContentType {

    public static final Charset DEFAULT_HTTP_CHARSET = StandardCharsets.ISO_8859_1;

    public ContentType(final String type, final String charset, final String boundary) {
        myHeader = null;
        myType = type;
        myCharset = charset;
        myBoundary = boundary;
    }

    public ContentType(final Header h) throws HttpClientException {
        if (h == null) {
            throw new HttpClientException("Header is null");
        }
        if (!"Content-Type".equalsIgnoreCase(h.getName())) {
            throw new HttpClientException("Header is not content type");
        }

        this.myHeader = h;

        final HeaderElement[] elems = myHeader.getElements();
        if (elems == null || elems.length == 0) {
            this.myType = null;
        } else if (elems.length > 1) {
            throw new HttpClientException("Multiple Content-Type headers");
        } else {
            this.myType = elems[0].getName();
        }

        String charset = null;
        String boundary = null;
        if (elems != null) {
            for (final HeaderElement e : elems) {
                final NameValuePair nvpCharset = e.getParameterByName("charset");
                if (nvpCharset != null) {
                    charset = nvpCharset.getValue();
                }
                final NameValuePair nvpBoundary = e.getParameterByName("boundary");
                if (nvpBoundary != null) {
                    boundary = nvpBoundary.getValue();
                }
            }
        }
        this.myCharset = charset;
        this.myBoundary = boundary;
    }

    @Override
    public String toString() {
        if (myHeader == null) {
            final StringBuilder builder = new StringBuilder("Content-Type: ").append(getValue());
            if (myCharset != null) {
                builder.append("; charset=").append(myCharset);
            }
            if (myBoundary != null) {
                builder.append("; boundary=").append(myBoundary);
            }

            return builder.toString();
        } else {
            return myHeader.toString();
        }
    }

    @Nullable
    public String getType() {
        return myType;
    }

    @Nullable
    public String getCharset() {
        return myCharset;
    }

    @Nullable
    public String getBoundary() {
        return myBoundary;
    }

    @Nullable
    public String getValue() {
        // TODO: Why did I add the boundary before...?
//        if ( myHeader == null ) {
//            StringBuilder b = new StringBuilder();
//            b.append(myType);
//            if ( myBoundary != null ) {
//                b.append("; boundary=\"");
//                // TODO: Is that correct escaping sequence?
//                b.append(myBoundary.replace("\"", "\\\""));
//                b.append("\"");
//            }
//            return b.toString();
//        }
        if (myType != null) {
            return myType;
        }
        if (myHeader != null) {
            return myHeader.getValue();
        } else {
            return null;
        }
    }

    private final Header myHeader;
    private final String myType;
    private final String myCharset;
    private final String myBoundary;
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
