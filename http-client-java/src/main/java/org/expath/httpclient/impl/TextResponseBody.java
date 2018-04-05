/****************************************************************************/
/*  File:       TextResponseBody.java                                       */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-02-02                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient.impl;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import org.expath.httpclient.ContentType;
import org.expath.httpclient.HeaderSet;
import org.expath.httpclient.HttpClientException;
import org.expath.httpclient.HttpResponseBody;
import org.expath.httpclient.model.Result;
import org.expath.httpclient.model.TreeBuilder;
import org.expath.tools.ToolsException;

import static org.expath.httpclient.ContentType.DEFAULT_HTTP_CHARSET;

/**
 * A text body in the response.
 *
 * @author Florent Georges
 */
public class TextResponseBody implements HttpResponseBody {

    public TextResponseBody(final Result result, final InputStream in, final ContentType type, final HeaderSet headers)
            throws HttpClientException {
        myContentType = type;
        myHeaders = headers;

        final Charset contentCharset;
        if (type.getCharset() != null) {
            contentCharset = Charset.forName(type.getCharset());
        } else {
            contentCharset = DEFAULT_HTTP_CHARSET;
        }

        final Reader reader = new InputStreamReader(in, contentCharset);
        result.add(reader, contentCharset);
    }

    public TextResponseBody(final Result result, final Reader in, final ContentType type, final HeaderSet headers)
            throws HttpClientException {
        myContentType = type;
        myHeaders = headers;

        final Charset contentCharset;
        if (type.getCharset() != null) {
            contentCharset = Charset.forName(type.getCharset());
        } else {
            contentCharset = DEFAULT_HTTP_CHARSET;
        }

        result.add(in, contentCharset);
    }

    @Override
    public void outputBody(final TreeBuilder b) throws HttpClientException {
        if (myHeaders != null) {
            b.outputHeaders(myHeaders);
        }
        try {
            b.startElem("body");
            b.attribute("media-type", myContentType.getValue());
            // TODO: Support other attributes as well?
            b.startContent();
            b.endElem();
        } catch (ToolsException ex) {
            throw new HttpClientException("Error building the body", ex);
        }
    }

    private final ContentType myContentType;
    private final HeaderSet myHeaders;
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
