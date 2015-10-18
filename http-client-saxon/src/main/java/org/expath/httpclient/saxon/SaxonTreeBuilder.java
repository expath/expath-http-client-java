/****************************************************************************/
/*  File:       TreeBuilderHelper.java                                      */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-02-02                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient.saxon;

import net.sf.saxon.expr.XPathContext;
import org.apache.http.Header;
import org.expath.httpclient.HeaderSet;
import org.expath.httpclient.HttpClientException;
import org.expath.httpclient.model.TreeBuilder;
import org.expath.tools.ToolsException;


/**
 * Implementation of {@link TreeBuilder} for Saxon.
 *
 * @author Florent Georges
 */
public class SaxonTreeBuilder
        extends org.expath.tools.saxon.model.SaxonTreeBuilder
        implements TreeBuilder
{
    public SaxonTreeBuilder(XPathContext ctxt, String prefix, String ns)
            throws ToolsException
    {
        super(ctxt, prefix, ns);
    }

    @Override
    public void outputHeaders(HeaderSet headers)
            throws HttpClientException
    {
        for ( Header h : headers ) {
            assert h.getName() != null : "Header name cannot be null";
            String name = h.getName().toLowerCase();
            try {
                startElem("header");
                attribute("name", name);
                attribute("value", h.getValue());
                startContent();
                endElem();
            }
            catch ( ToolsException ex ) {
                throw new HttpClientException("Error building the header " + name, ex);
            }
        }
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
