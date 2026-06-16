/****************************************************************************/
/*  File:       org.expath.httpclient.BomAction.java                        */
/*  Author:     Adam Retter - Evolved Binary Ltd                            */
/*  Date:       2026-06-16                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2026 Evolved Binary Ltd (see end of file.)            */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient;

/**
 * Provides options for BOM (Byte Order Mark) handling.
 */
public enum BomAction {

    /**
     * Raise and error if a BOM is present.
     */
    ERROR,

    /**
     * Preserve the BOM in the output, and don't use it to inform the charset.
     */
    PRESERVE_IGNORE,

    /**
     * Preserve the BOM in the output, and use it to override the charset.
     */
    PRESERVE_OVERRIDE_CHARSET,

    /**
     * Drop the BOM in the output, and don't use it to inform the charset.
     */
    DROP_IGNORE,

    /**
     * Drop the BOM in the output, and use it to override the charset.
     */
    DROP_OVERRIDE_CHARSET;

    /**
     * Get the BOM Action from an Argument string.
     *
     * @param str the argument string.
     *
     * @return the BOM action.
     *
     * @throws IllegalArgumentException if there is no BOM action for the supplied argument string.
     */
    public static BomAction fromArgumentString(final String str) throws IllegalArgumentException {
        return valueOf(str.toUpperCase().replace("-", "_"));
    }

    /**
     * Get the argument version of the name.
     *
     * @return the argument name.
     */
    public String toArgumentString() {
        return name().toLowerCase().replace('_', '-');
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
/*  The Initial Developer of the Original Code is Evolved Binary Ltd.       */
/*                                                                          */
/*  Contributor(s): none.                                                   */
/* ------------------------------------------------------------------------ */
