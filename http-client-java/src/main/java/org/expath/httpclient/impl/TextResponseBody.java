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

import com.evolvedbinary.j8xu.BOM;
import com.evolvedbinary.j8xu.io.BomFilterInputStream;
import org.expath.httpclient.*;
import org.expath.httpclient.model.Result;
import org.expath.httpclient.model.TreeBuilder;
import org.expath.tools.ToolsException;

import javax.annotation.Nullable;

/**
 * A text body in the response.
 *
 * @author Florent Georges
 */
public class TextResponseBody implements HttpResponseBody {

    public static final Charset DEFAULT_HTTP_TEXT_CHARSET = StandardCharsets.ISO_8859_1;

    public TextResponseBody(final Result result, InputStream in, final ContentType type, final BomAction bomAction, final HeaderSet headers)
            throws HttpClientException {
        myContentType = type;
        myHeaders = headers;

        Charset contentCharset;
        if (type.getCharset() != null) {
            contentCharset = Charset.forName(type.getCharset());
        } else {
            contentCharset = DEFAULT_HTTP_TEXT_CHARSET;
        }

        if (bomAction != BomAction.PRESERVE_IGNORE) {
            final BomFilterInputStream bomIn = new BomFilterInputStream(in);
            try {
                @Nullable final BOM bom = bomIn.parseBom();

                if (bom != null) {
                    if (bomAction == BomAction.ERROR) {
                        throw new HttpClientException(HttpClientError.HC002, "bom-action='error' but found BOM: " + bom.name());
                    }

                    if (bomAction == BomAction.DROP_IGNORE) {
                        bomIn.skip(bom.getBomBytes().length);
                    } else if (bomAction == BomAction.DROP_OVERRIDE_CHARSET) {
                        contentCharset = bom.getCharset();
                        bomIn.skip(bom.getBomBytes().length);
                    } else if (bomAction == BomAction.PRESERVE_OVERRIDE_CHARSET) {
                        contentCharset = bom.getCharset();
                    }
                }
            } catch (final IOException e) {
                throw new HttpClientException(HttpClientError.HC002, "Unable to parser BOM: " + e.getMessage(), e);
            }

            in = bomIn;
        }

        final Reader reader = new InputStreamReader(in, contentCharset);
        result.add(reader, contentCharset);
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
            throw new HttpClientException(HttpClientError.HC002, "Error building the body", ex);
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
/*  Contributor(s): Evolved Binary Ltd.                                     */
/* ------------------------------------------------------------------------ */
