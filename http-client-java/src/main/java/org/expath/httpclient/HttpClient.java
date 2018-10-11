/****************************************************************************/
/*  File:       HttpClient.java                                             */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-02-01                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient;

import java.net.URI;
import java.net.URISyntaxException;
import org.expath.httpclient.impl.ApacheHttpConnection;
import org.expath.httpclient.impl.RequestParser;
import org.expath.httpclient.model.Result;
import org.expath.tools.model.Element;
import org.expath.tools.model.Sequence;


/**
 * Facade for the EXPath HTTP Client, generic implementation for Java.
 *
 * @author Florent Georges
 */
public class HttpClient
{
    /**
     * Implement the EXPath function {@code http:send-request()}.
     *
     * <pre>
     * http:send-request($request as element(http:request)?) as item()+
     * </pre>
     * 
     * @param result The {@link Result} object to send the results to.
     * @param request The {@code http:request} element.
     * @return The result object.
     * @throws HttpClientException If any error occurs.
     */
    public static Result sendRequest(Result result, Element request)
            throws HttpClientException
    {
        return sendRequest(result, request, null, null);
    }

    /**
     * Implement the EXPath function {@code http:send-request()}.
     *
     * <pre>
     * http:send-request($request as element(http:request)?,
     *                   $href as xs:string?) as item()+
     * </pre>
     * 
     * @param result The {@link Result} object to send the results to.
     * @param request The {@code http:request} element.
     * @param href The URL to sent the HTTP request to.  Overrides the one in
     *      the request element.
     * @return The result object.
     * @throws HttpClientException If any error occurs.
     */
    public static Result sendRequest(Result result, Element request, String href)
            throws HttpClientException
    {
        return sendRequest(result, request, href, null);
    }

    /**
     * Implement the EXPath function {@code http:send-request()}.
     *
     * <pre>
     * http:send-request($request as element(http:request)?,
     *                   $href as xs:string?,
     *                   $bodies as item()*) as item()+
     * </pre>
     * 
     * @param result The {@link Result} object to send the results to.
     * @param request The {@code http:request} element.
     * @param href The URL to sent the HTTP request to.  Overrides the one in
     *      the request element.
     * @param bodies The content of the HTTP request (the entity body, or bodies
     *      in case of multi-part).
     * @return The result object.
     * @throws HttpClientException If any error occurs.
     */
    public static Result sendRequest(Result result, Element request, String href, Sequence bodies)
            throws HttpClientException
    {
        HttpClient client = new HttpClient();
        return client.doSendRequest(result, request, href, bodies);
    }

    private Result doSendRequest(Result result, Element request, String href, Sequence bodies)
            throws HttpClientException
    {
        RequestParser parser = new RequestParser(request);
        HttpRequest req = parser.parse(bodies, href);
        // override anyway it href exists
        if ( href != null && ! "".equals(href) ) {
            req.setHref(href);
        }
        try {
            URI uri = new URI(req.getHref());
            return sendOnce(result, uri, req, parser);
        }
        catch ( URISyntaxException ex ) {
            throw new HttpClientException("Href is not valid: " + req.getHref(), ex);
        }
    }

    /**
     * Send one request, not following redirect but handling authentication.
     * 
     * Authentication may require to reply to an authentication challenge,
     * by sending again the request, with credentials.
     */
    private Result sendOnce(Result result, URI uri, HttpRequest request, RequestParser parser)
            throws HttpClientException
    {
        HttpConnection conn = new ApacheHttpConnection(uri);
        try {
            HttpResponse response = request.send(result, conn, parser.getCredentials());
            if ( response.getStatus() == 401 ) {
                conn.disconnect();
                conn = new ApacheHttpConnection(uri);
                // create a new result, and throw the old one away
                result = result.makeNewResult();
                request.send(result, conn, parser.getCredentials());
            }
        }
        finally {
            conn.disconnect();
        }
        return result;
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
