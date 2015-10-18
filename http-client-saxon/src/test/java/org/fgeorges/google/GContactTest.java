/****************************************************************************/
/*  File:       GContactTest.java                                           */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-02-23                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.fgeorges.google;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import org.junit.Test;

/**
 * TODO<doc>: ...
 *
 * @author Florent Georges
 * @date   2009-02-23
 */
public class GContactTest
{
    @Test
    public void helloWorld()
    {
        System.out.println("Hello, world!");
    }

    //@Test
    public void launchTest()
            throws SaxonApiException
    {
        // the style source document
        Source style_src = new StreamSource(STYLE_FILE);
        // from the processor to the transformer...
        Processor proc = new Processor(false);
        XsltCompiler compiler = proc.newXsltCompiler();
        XsltExecutable style = compiler.compile(style_src);
        XsltTransformer trans = style.load();
        // the initial template
        trans.setInitialTemplate(new QName("main"));
        // the output (to stdout)
        Serializer out = new Serializer();
        out.setOutputStream(System.out);
        trans.setDestination(out);
        // the authentication params
        trans.setParameter(new QName("user"),    new XdmAtomicValue(USER));
        trans.setParameter(new QName("pwd"),     new XdmAtomicValue(PWD));
        trans.setParameter(new QName("map-key"), new XdmAtomicValue(MAP_KEY));
        // actually transform
        trans.transform();
    }

    private static final String STYLE_FILE =
            "../../samples/google/gcontacts-test.xsl";
    private static final String USER = "fgeorges.test";
    private static final String PWD  = "testtest";
    private static final String MAP_KEY
            = "ABQIAAAApauHa22s3jjpFII3xsFOmBQqOhBDBnQJ8Ao-CIne506W8LCJ2BRLlYzn22KelrVOyoJ7EA7OLwsGgw";

    // set the proxy info
    static {
        System.setProperty("http.proxyHost", "proxy");
        System.setProperty("http.proxyPort", "8080");
        System.setProperty("https.proxyHost", "proxy");
        System.setProperty("https.proxyPort", "8080");
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
