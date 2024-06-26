/****************************************************************************/
/*  File:       HttpResponse.java                                           */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-02-02                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient;

import org.expath.httpclient.model.TreeBuilder;
import org.expath.tools.ToolsException;

/**
 * A HTTP response.
 *
 * TODO: Make an abstract class, and factorize code...
 *
 * @author Florent Georges
 */
public class HttpResponse
{
    public HttpResponse(int status, String msg, HeaderSet headers, HttpResponseBody body, long time)
    {
        myStatus = status;
        myMessage = msg;
        myHeaders = headers;
        myBody = body;
        myTime = time;
    }

    public int getStatus()
    {
        return myStatus;
    }

    public HeaderSet getHeaders()
    {
        return myHeaders;
    }

    public HttpResponseBody getBody()
    {
        return myBody;
    }

    public void outputResponseElement(TreeBuilder b)
            throws HttpClientException
    {
        try {
            b.startElem("response");
            b.attribute("status", Integer.toString(myStatus));
            b.attribute("message", myMessage);
            b.attribute("spent-millis", Long.toString(myTime));
            b.startContent();
            b.outputHeaders(myHeaders);
            if ( myBody != null ) {
                // "recurse" on bodies...
                myBody.outputBody(b);
            }
            // end the response element
            b.endElem();
        }
        catch ( ToolsException ex ) {
            throw new HttpClientException(HttpClientError.HC002, "Error building the response", ex);
        }
    }

    private final int myStatus;
    private final String myMessage;
    private final HeaderSet myHeaders;
    private final HttpResponseBody myBody;
    private final long myTime;
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
