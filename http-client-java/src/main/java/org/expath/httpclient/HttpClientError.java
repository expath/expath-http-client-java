/****************************************************************************/
/*  File:       org.expath.httpclient.HttpClientError.java                  */
/*  Author:     A. Retter - adamretter.org.uk                               */
/*  Date:       2024-06-18                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2024 Adam Retter (see end of file.)                   */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient;

import org.expath.ExpathConstants;
import org.expath.ExpathError;

import javax.xml.namespace.QName;

/**
 * An error as defined by the EXPath HTTP Client specification.
 */
public enum HttpClientError implements ExpathError {
  HC001("HC001", "An HTTP error occurred"),
  HC002("HC002", "Error parsing the entity content as XML or HTML."),
  HC003("HC003", "With a multipart response, the override-media-type must be either a multipart media type or application/octet-stream."),
  HC004("HC004", "The src attribute on the body element is mutually exclusive with all other attribute (except the media-type)."),
  HC005("HC005", "The request element is not valid."),
  HC006("HC006", "A timeout occurred waiting for the response.");

  private final QName name;
  private final String description;

  HttpClientError(final String code, final String description) {
    this.name = new QName(ExpathConstants.ERR_NS_URI, code, ExpathConstants.ERR_NS_PREFIX);
    this.description = description;
  }

  @Override
  public QName getName() {
    return name;
  }

  @Override
  public String getDescription() {
    return description;
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
/*  The Initial Developer of the Original Code is Adam Retter.              */
/*                                                                          */
/*  Contributor(s): none.                                                   */
/* ------------------------------------------------------------------------ */
