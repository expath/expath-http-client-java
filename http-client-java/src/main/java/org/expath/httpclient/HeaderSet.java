/****************************************************************************/
/*  File:       HeaderHelper.java                                           */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-02-21                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.message.BasicHeader;

/**
 * TODO: Doc...
 *
 * TODO: Change this class to a real wrapper around a {@link Header[]} or a
 * {@link Collection}&lt;Header&gt;.
 *
 * @author Florent Georges
 */
public class HeaderSet
        implements Iterable<Header>
{
    /**
     * Build a new object with no header.
     */
    public HeaderSet()
    {
        myHeaders = new ArrayList<Header>();
    }

    /**
     * Build a new object by *copying* its parameter.
     *
     * @param headers the headers to add to the set
     * @throws HttpClientException if the headers are null
     */
    public HeaderSet(Header[] headers)
            throws HttpClientException
    {
        if ( headers == null ) {
            throw new HttpClientException(HttpClientError.HC005, "Headers array is null");
        }
        myHeaders = new ArrayList<Header>(headers.length);
        myHeaders.addAll(Arrays.asList(headers));
    }

    /**
     * Build a new object by *copying* its parameter.
     *
     * @param headers the headers to add to the set
     * @throws HttpClientException if the headers are null
     */
    public HeaderSet(Collection<Header> headers)
            throws HttpClientException
    {
        if ( headers == null ) {
            throw new HttpClientException(HttpClientError.HC005, "Headers list is null");
        }
        myHeaders = new ArrayList<Header>(headers);
    }

    public Iterator<Header> iterator()
    {
        return myHeaders.iterator();
    }

    public Header[] toArray()
    {
        return myHeaders.toArray(new Header[0]);
    }

    public boolean isEmpty()
    {
        return myHeaders.isEmpty();
    }

    public Header add(Header h)
    {
        myHeaders.add(h);
        return h;
    }

    public Header add(String name, String value)
    {
        Header h = new BasicHeader(name, value);
        myHeaders.add(h);
        return h;
    }

    public Header getFirstHeader(String name)
            throws HttpClientException
    {
        for ( Header h : myHeaders ) {
            if ( name.equalsIgnoreCase(h.getName()) ) {
                return h;
            }
        }
        return null;
    }

    private List<Header> myHeaders;
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
