/****************************************************************************/
/*  File:       RequestParser.java                                          */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-03-10                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient.impl;

import org.expath.httpclient.HeaderSet;
import org.expath.httpclient.HttpClientException;
import org.expath.httpclient.HttpConstants;
import org.expath.httpclient.HttpCredentials;
import org.expath.httpclient.HttpRequest;
import org.expath.httpclient.HttpRequestBody;
import org.expath.tools.ToolsException;
import org.expath.tools.model.Attribute;
import org.expath.tools.model.Element;
import org.expath.tools.model.Sequence;

/**
 * Parse the http:request element into a {@link HttpRequest} object.
 *
 * @author Florent Georges
 */
public class RequestParser
{
    public RequestParser(Element request)
            throws HttpClientException
    {
        String ns   = request.getNamespaceUri();
        String name = request.getLocalName();
        boolean ns_ok = true;
        if ( HttpConstants.HTTP_NS_URI.equals(ns) ) {
            myNs = HttpConstants.HTTP_NS_URI;
            myOtherNs = HttpConstants.HTTP_CLIENT_NS_URI;
        }
        else if ( HttpConstants.HTTP_CLIENT_NS_URI.equals(ns) ) {
            myNs = HttpConstants.HTTP_CLIENT_NS_URI;
            myOtherNs = HttpConstants.HTTP_NS_URI;
        }
        else {
            ns_ok = false;
        }
        if ( ! "request".equals(name) || ! ns_ok ) {
            String clark = "{" + ns + "}" + name;
            throw new HttpClientException("$request is not an element(http:request), but is " + clark);
        }
        myRequest = request;
        myNs = ns;
    }

    public String getNamespaceURI()
    {
        return myNs;
    }

    public HttpCredentials getCredentials()
    {
        return myCredentials;
    }

    public HttpRequest parse(Sequence bodies, String href)
            throws HttpClientException
    {
        String username = null;
        String password = null;
        String auth_method = null;

        HttpRequest req = new HttpRequestImpl();
        req.setHref(href);

        // walk the attributes:
        //     method = NCName
        //     href? = anyURI
        //     http-version = string
        //     status-only? = boolean
        //     username? = string
        //     password? = string
        //     auth-method? = string
        //     send-authorization? = boolean
        //     override-media-type? = string
        //     follow-redirect? = boolean
        //     timeout? = integer
        //     gzip? = boolean
        //     chunked? = boolean
        for ( Attribute a : myRequest.attributes() ) {
            String local = a.getLocalName();
            if ( !(a.getNamespaceUri() == null || a.getNamespaceUri().isEmpty()) ) {
                // ignore namespace qualified attributes
            }
            else if ( "method".equals(local) ) {
                req.setMethod(a.getValue());
            }
            else if ( "href".equals(local) ) {
                req.setHref(a.getValue());
            }
            else if ( "http-version".equals(local) ) {
                req.setHttpVersion(a.getValue().trim());
            }
            else if ( "status-only".equals(local) ) {
                req.setStatusOnly(toBoolean(a));
            }
            else if ( "username".equals(local) ) {
                username = a.getValue();
            }
            else if ( "password".equals(local) ) {
                password = a.getValue();
            }
            else if ( "auth-method".equals(local) ) {
                auth_method = a.getValue();
            }
            else if ( "send-authorization".equals(local) ) {
                req.setPreemptiveAuthentication(toBoolean(a));
            }
            else if ( "override-media-type".equals(local) ) {
                req.setOverrideType(a.getValue());
            }
            else if ( "follow-redirect".equals(local) ) {
                req.setFollowRedirect(toBoolean(a));
            }
            else if ( "timeout".equals(local) ) {
                req.setTimeout(toInteger(a));
            }
            else if ( "gzip".equals(local) ) {
                req.setGzip(toBoolean(a));
            }
            else if ( "chunked".equals(local) ) {
                req.setChunked(toBoolean(a));
            }
            else {
                throw new HttpClientException("Unknown attribute http:request/@" + local);
            }
        }
        if ( req.getMethod() == null ) {
            throw new HttpClientException("required @method has not been set on http:request");
        }
        if ( req.getHref() == null ) {
            throw new HttpClientException("required @href has not been set on http:request");
        }
        if ( username != null || password != null || auth_method != null ) {
            setAuthentication(username, password, auth_method);
        }
        if(req.getHttpVersion() != null && req.getHttpVersion().equals(HttpConstants.HTTP_1_0) && req.isChunked()) {
            throw new HttpClientException("Chunked transfer encoding can only be used with HTTP 1.1");
        }

        // walk the elements
        // TODO: Check element structure validity (header*, (multipart|body)?)
        HeaderSet headers = new HeaderSet();
        req.setHeaders(headers);
        for ( Element child : myRequest.children() ) {
            String local = child.getLocalName();
            String ns = child.getNamespaceUri();
            if ( ns == null || ns.isEmpty() ) {
                // elements in no namespace are an error
                throw new HttpClientException("Element in no namespace: " + local);
            }
            else if ( myOtherNs.equals(ns) ) {
                String clark = "{" + ns + "}" + local;
                throw new HttpClientException("http:request mixes elements in the new and legacy HTTP namespace: " + clark);
            }
            else if ( ! myNs.equals(ns) ) {
                // ignore elements in other namespaces
            }
            else if ( "header".equals(local) ) {
                addHeader(headers, child);
            }
            else if ( "body".equals(local) || "multipart".equals(local) ) {
                HttpRequestBody b = BodyFactory.makeRequestBody(child, bodies, myNs);
                req.setBody(b);
            }
            else {
                throw new HttpClientException("Unknown element: " + local);
            }
        }

        return req;
    }

    private void setAuthentication(String user, String pwd, String method)
            throws HttpClientException
    {
        if ( user == null || pwd == null || method == null ) {
            throw new HttpClientException("@username, @password and @auth-method must be all set");
        }
        if ( "basic".equals(method) ) {
            myCredentials = new HttpCredentials(user, pwd, method);
        }
        else if ( "digest".equals(method) ) {
            // FIXME: Wrong if HREF is not on http:request, but as a param, because
            // it will be set on myRequest after this method has been called.
            myCredentials = new HttpCredentials(user, pwd, method);
        }
        else {
            throw new HttpClientException("Unknown authentication method: " + method);
        }
    }

    private void addHeader(HeaderSet headers, Element e)
            throws HttpClientException
    {
        String name = null;
        String value = null;
        for ( Attribute a : e.attributes() ) {
            String local = a.getLocalName();
            if ( !(a.getNamespaceUri() == null || a.getNamespaceUri().isEmpty()) ) {
                // ignore namespace qualified attributes
            }
            else if ( "name".equals(local) ) {
                name = a.getValue();
            }
            else if ( "value".equals(local) ) {
                value = a.getValue();
            }
            else {
                throw new HttpClientException("Unknown attribute http:header/@" + local);
            }
        }
        // both are required
        if ( name == null || value == null ) {
            throw new HttpClientException("@name and @value are required on http:header");
        }

        if(name.equalsIgnoreCase("Content-Length")) {
            throw new HttpClientException("Content-Length should not be explicitly provided, either it will automatically be added or Transfer-Encoding will be used.");
        }

        if(name.equalsIgnoreCase("Transfer-Encoding")) {
            throw new HttpClientException("Transfer-Encoding should not be explicitly provided, it will automatically be added if required.");
        }

        // actually add the header
        headers.add(name, value);
    }

    /**
     * Helper function to handle the exception.
     * 
     * @return The attribute value as a boolean.
     * 
     * @throws HttpClientException If the attribute value is not a proper boolean.
     */
    private boolean toBoolean(Attribute a)
            throws HttpClientException
    {
        try {
            return a.getBoolean();
        }
        catch ( ToolsException ex ) {
            throw new HttpClientException("Error parsing the attribute as a boolean", ex);
        }
    }

    /**
     * Helper function to handle the exception.
     * 
     * @return The attribute value as a integer.
     * 
     * @throws HttpClientException If the attribute value is not a proper integer.
     */
    private int toInteger(Attribute a)
            throws HttpClientException
    {
        try {
            return a.getInteger();
        }
        catch ( ToolsException ex ) {
            throw new HttpClientException("Error parsing the attribute as an integer", ex);
        }
    }

    /** The http:request element. */
    private Element myRequest;
    /** The namespace URI of the http:request element (either the new or the legacy URI). */
    private String myNs;
    /** The legacy namespace URI if myNs is the new one, or the other way around. */
    private String myOtherNs;
    /** User credentials in case of authentication (from @username, @password and @auth-method). */
    private HttpCredentials myCredentials = null;
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
