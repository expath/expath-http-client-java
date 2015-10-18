/****************************************************************************/
/*  File:       Result.java                                                 */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-03-10                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient.model;

import java.io.Reader;
import java.io.InputStream;
import javax.xml.transform.Source;
import org.expath.httpclient.HttpClientException;
import org.expath.httpclient.HttpResponse;

/**
 * An abstract representation of the result sequence.
 *
 * Accumulate result items from strings, bytes, JAXP sources and HTTP response
 * objects.
 *
 * A specific implementation is obviously supposed to setup a way to provide
 * the caller with the final result sequence within the processor's own object
 * model.
 *
 * The items are added in order to the result sequence (in the same order than
 * the method calls).  Except for the HTTP response objects, which will be
 * called once per result sequence, and always must be added to the front of
 * the sequence.
 *
 * @author Florent Georges
 */
public interface Result
{
    /**
     * Construct a new {@link Result} object, from the same implementation.
     * 
     * TODO: This mechanism is not satisfactory.  It forces the user of the
     * class {@code HttpClient} to down cast the result of {@code sendRequest()}
     * to its own implementation of {@link Result}.  A better approach would be
     * to make this an abstract class, that forwards everything to some
     * {@code RequestResult} interface.  The abstract method would be a factory
     * method as well, but then the client could add its own way to retrieve
     * its own {@code RequestResult}, with the correct type.  A {@code RequestResult}
     * object would represent the result of one HTTP request on the wire, so there
     * could be a few in case of a authentication handshake, or a redirect, etc.
     * 
     * TODO: Actually, an even better approach would be to say that the function
     * {@code http:send-request} must send exactly only ONE request on the wire.
     * Authentication back-and-forth, redirects, and other alike would then be
     * implemented in XSLT and XQuery themselves, by providing a library on top
     * of the extension function (taking some configuration XML element, with
     * options, function items, etc., saying what to do in case of such events.
     * 
     * That is, keep the extension implementation as a minimum, and share all
     * the rest in XQuery and XSLT.  That would simplify the specification as
     * well, and increase compatibility.
     * 
     * @return The new result object.
     * @throws HttpClientException If any error occurs.
     */
    public Result makeNewResult()
            throws HttpClientException;

    /**
     * Add a string value to the result sequence.
     * 
     * @param string The string to add to the result sequence.
     * @throws HttpClientException If any error occurs.
     */
    public void add(Reader string)
            throws HttpClientException;

    /**
     * Add raw binary to the result sequence.
     * 
     * @param bytes The bytes representing the binary item to add to the
     *      result sequence.
     * @throws HttpClientException If any error occurs.
     */
    public void add(InputStream bytes)
            throws HttpClientException;

    /**
     * Add a document node to the result sequence.
     * 
     * @param src The {@link Source} representing the document to add to the
     *      result sequence.
     * @throws HttpClientException If any error occurs.
     */
    public void add(Source src)
            throws HttpClientException;

    /**
     * Add the http:response element to the result sequence.
     *
     * The implementation for a specific processor is supposed to call the
     * method {@code HttpResponse#makeResultElement(TreeBuilder)} with a tree
     * builder for the same processor.  This must be added at the front of the
     * sequence, always, even if it is called after other methods.
     * 
     * @param response The response element to add to the result sequence.
     * @throws HttpClientException If any error occurs.
     */
    public void add(HttpResponse response)
            throws HttpClientException;
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
/*  Contributor(s): Adam Retter                                             */
/* ------------------------------------------------------------------------ */
