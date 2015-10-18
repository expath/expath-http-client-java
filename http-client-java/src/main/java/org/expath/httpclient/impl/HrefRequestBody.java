/****************************************************************************/
/*  File:       HrefRequestBody.java                                        */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-02-25                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import org.expath.httpclient.HeaderSet;
import org.expath.httpclient.HttpClientException;
import org.expath.httpclient.HttpRequestBody;
import org.expath.tools.model.Element;

/**
 * A body in the request, defined by reference.
 *
 * @author Florent Georges
 */
public class HrefRequestBody
        extends HttpRequestBody
{
    /**
     * TODO: Check there is no other attributes (only @src and @media-type)...
     */
    public HrefRequestBody(Element elem)
            throws HttpClientException
    {
        super(elem);
        myHref = elem.getAttribute("src");
    }

    @Override
    public boolean isMultipart()
    {
        return false;
    }

    @Override
    public void setHeaders(HeaderSet headers)
            throws HttpClientException
    {
        // set the Content-Type header (if not set by the user)
        if ( headers.getFirstHeader("Content-Type") == null ) {
            headers.add("Content-Type", getContentType());
        }
    }

    @Override
    public void serialize(OutputStream out)
            throws HttpClientException
    {
        try {
            String filename = new URI(myHref).getPath();
            try (final InputStream in = new FileInputStream(new File(filename))) {
                byte[] buf = new byte[4096];
                int l = -1;
                while ((l = in.read(buf)) != -1) {
                    out.write(buf, 0, l);
                }
            } catch (IOException ex) {
                throw new HttpClientException("Error sending the file content", ex);
            }
        } catch ( URISyntaxException ex ) {
            throw new HttpClientException("Bad URI: " + myHref, ex);
        }
    }

    private String myHref;
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
