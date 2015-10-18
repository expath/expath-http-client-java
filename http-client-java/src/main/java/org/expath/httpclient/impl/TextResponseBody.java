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
import java.nio.charset.StandardCharsets;

import org.expath.httpclient.ContentType;
import org.expath.httpclient.HeaderSet;
import org.expath.httpclient.HttpClientException;
import org.expath.httpclient.HttpResponseBody;
import org.expath.httpclient.model.Result;
import org.expath.httpclient.model.TreeBuilder;
import org.expath.tools.ToolsException;

/**
 * A text body in the response.
 *
 * @author Florent Georges
 */
public class TextResponseBody
        implements HttpResponseBody
{
    public TextResponseBody(Result result, InputStream in, ContentType type, HeaderSet headers)
            throws HttpClientException
    {
        myContentType = type;
        myHeaders = headers;
        // TODO: ...
        final Charset charset = StandardCharsets.UTF_8;
        final Reader reader = new InputStreamReader(in, charset);
        result.add(reader);
    }

    public TextResponseBody(Result result, Reader in, ContentType type, HeaderSet headers)
            throws HttpClientException
    {
        myContentType = type;
        myHeaders = headers;
        result.add(in);
    }

    @Override
    public void outputBody(TreeBuilder b)
            throws HttpClientException
    {
        if ( myHeaders != null ) {
            b.outputHeaders(myHeaders);
        }
        try {
            b.startElem("body");
            b.attribute("media-type", myContentType.getValue());
            // TODO: Support other attributes as well?
            b.startContent();
            b.endElem();
        }
        catch ( ToolsException ex ) {
            throw new HttpClientException("Error building the body", ex);
        }
    }

    private ContentType myContentType;
    private HeaderSet myHeaders;
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
