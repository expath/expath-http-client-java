/****************************************************************************/
/*  File:       SaxonResult.java                                            */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-03-10                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient.saxon;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.Source;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ArrayIterator;
import net.sf.saxon.value.Base64BinaryValue;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.StringValue;
import org.expath.httpclient.HttpClientException;
import org.expath.httpclient.HttpConstants;
import org.expath.httpclient.HttpResponse;
import org.expath.httpclient.model.Result;
import org.expath.tools.ToolsException;

/**
 * Implementation of {@link Item} for Saxon.
 *
 * @author Florent Georges
 */
public class SaxonResult
        implements Result
{
    public SaxonResult(XPathContext ctxt, String ns)
    {
        myItems = new ArrayList<Item>();
        myCtxt = ctxt;
        myNs = ns;
    }

    @Override
    public Result makeNewResult()
            throws HttpClientException
    {
        return new SaxonResult(myCtxt, myNs);
    }

    @Override
    public void add(Reader reader)
            throws HttpClientException
    {
        try(final BufferedReader buf_in = new BufferedReader(reader)) {
            final StringBuilder builder = new StringBuilder();

            String buf = null;
            while ( (buf = buf_in.readLine()) != null ) {
                builder.append(buf);
                builder.append('\n');
            }
            final String value = builder.toString();

            Item item = new StringValue(value);
            myItems.add(item);
        }
        catch ( final IOException ex ) {
            throw new HttpClientException("error reading HTTP response", ex);
        }
    }

    @Override
    public void add(InputStream inputStream)
            throws HttpClientException
    {
        try(final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            final byte[] buf = new byte[4096];
            int read = -1;
            while ( (read = inputStream.read(buf)) > 0 ) {
                    out.write(buf, 0, read);
            }
            final byte[] bytes = out.toByteArray();

            Item item = new Base64BinaryValue(bytes);
            myItems.add(item);
        } catch(final IOException e) {
            throw new HttpClientException(e.getMessage(), e);
        }
    }

    @Override
    public void add(Source src)
            throws HttpClientException
    {
        try {
            Item doc = myCtxt.getConfiguration().buildDocument(src);
            myItems.add(doc);
        }
        catch ( XPathException ex ) {
            throw new HttpClientException("Error building the XML or HTML document", ex);
        }
    }

    @Override
    public void add(HttpResponse response)
            throws HttpClientException
    {
        try {
            SaxonTreeBuilder builder = new SaxonTreeBuilder(myCtxt, HttpConstants.HTTP_NS_PREFIX, myNs);
            response.outputResponseElement(builder);
            Item elem = builder.getCurrentRoot();
            myItems.add(0, elem);
        }
        catch ( ToolsException ex ) {
            throw new HttpClientException("Error building the response", ex);
        }
    }

    public SequenceIterator newIterator()
    {
        Item[] array = myItems.toArray(new Item[0]);
        return new ArrayIterator(array);
    }

    public Sequence newSequence()
    {
        return new SequenceExtent(myItems);
    }

    private final List<Item>   myItems;
    private final XPathContext myCtxt;
    /** The namespace used for the elements. */
    private final String       myNs;
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
