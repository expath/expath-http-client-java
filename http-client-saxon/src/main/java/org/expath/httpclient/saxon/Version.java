/****************************************************************************/
/*  File:       Version.java                                                */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2016-05-17                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2016 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient.saxon;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

/**
 * Version of this project.
 *
 * @author Florent Georges
 */
public class Version
{
    protected Version()
    {
        // the version.properties file
        InputStream rsrc = null;

        try {
            rsrc = getClass().getResourceAsStream(VER_PROP);
            if (rsrc == null) {
                throw new IllegalStateException("Version properties file does not exist: " + VER_PROP);
            }

            // load into a Properties object
            final Properties props = new Properties();
            try {
                props.load(rsrc);
                rsrc.close();
            } catch (IOException ex) {
                throw new IllegalStateException("Error reading the version properties: " + VER_PROP, ex);
            }

            // get both properties
            myVersion = props.getProperty("org.expath.httpclient.saxon.version");
            myRevision = props.getProperty("org.expath.httpclient.saxon.revision");
        } finally {
            if (rsrc != null) {
                try {
                    rsrc.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args)
    {
        instance().display(System.out);
    }

    public void display(PrintStream out)
    {
        out.println("EXPath HTTP Client for Saxon.");
        out.println("Version: " + getVersion() + " (revision #" + getRevision() + ")");
    }

    public static synchronized Version instance()
    {
        if ( INSTANCE == null ) {
            INSTANCE = new Version();
        }
        return INSTANCE;
    }

    public String getVersion()
    {
        return myVersion;
    }

    public String getRevision()
    {
        return myRevision;
    }

    private static final String  VER_PROP = "version.properties";
    private static Version INSTANCE = null;
    private String myVersion;
    private String myRevision;
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
