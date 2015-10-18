/****************************************************************************/
/*  File:       SinglePartRequestBody.java                                  */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-02-06                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient.impl;

import java.io.OutputStream;
import java.util.Properties;
import javax.xml.namespace.QName;
import net.iharder.Base64;
import org.expath.httpclient.HeaderSet;
import org.expath.httpclient.HttpClientException;
import org.expath.httpclient.HttpConstants;
import org.expath.httpclient.HttpRequestBody;
import org.expath.httpclient.impl.BodyFactory.Type;
import org.expath.tools.ToolsException;
import org.expath.tools.model.Element;
import org.expath.tools.model.Sequence;
import org.expath.tools.serial.SerialParameters;

/**
 * A body in the request, defined by content.
 *
 * @author Florent Georges
 */
public class SinglePartRequestBody
        extends HttpRequestBody
{
    //
    // TODO: FIXME: Take serialization attributes into account!
    //
    public SinglePartRequestBody(Element elem, Sequence bodies, Type method)
            throws HttpClientException
    {
        super(elem);
        myMethod = method;
        switch ( myMethod ) {
            case XML:
                mySerial.setMethod(new QName("xml"));
                break;
            case TEXT:
                mySerial.setMethod(new QName("text"));
                break;
            case HTML:
                mySerial.setMethod(new QName("html"));
                break;
            case XHTML:
                mySerial.setMethod(new QName("xhtml"));
                break;
            case BINARY:
            case BASE64:
                // TODO: Set the proper namespace URI.
                mySerial.setMethod(new QName("expath:base64"));
                break;
            case HEX:
                // TODO: Set the proper namespace URI.
                mySerial.setMethod(new QName("expath:hex"));
                break;
            default:
                throw new HttpClientException("Unsupported method! (yet?): " + myMethod);
        }
        String[] attr_names = {
            "src",
            "media-type",
            "method",
            "byte-order-mark",
            "cdata-section-elements",
            "doctype-public",
            "doctype-system",
            "encoding",
            "escape-uri-attributes",
            "indent",
            "normalization-form",
            "omit-xml-declaration",
            "standalone",
            "suppress-indentation",
            "undeclare-prefixes",
            "version"
        };
        mySerial.setEncoding(elem.getAttribute(attr_names[7]));
        mySerial.setIndent(parseYesNo(elem, attr_names[9]));
        mySerial.setOmitXmlDeclaration(parseYesNo(elem, attr_names[11]));
        // TODO: Add other serial parameters...
        {
            // FIXME: For now, most of the attrs are not supported.  Throw an
            // error if any of them is there...
            String[] NOT_SUPPORTED_ATTRS = {
                "byte-order-mark",
                "cdata-section-elements",
                "doctype-public",
                "doctype-system",
                "escape-uri-attributes",
                "normalization-form",
                "standalone",
                "suppress-indentation",
                "undeclare-prefixes",
                "version"
            };
            for ( int i = 0; i < NOT_SUPPORTED_ATTRS.length; ++i ) {
                String name = NOT_SUPPORTED_ATTRS[i];
                String val = elem.getAttribute(name);
                if ( val != null ) {
                    throw new HttpClientException("Attribute not supported yet: http:body/@" + name);
                }
            }
        }
        // check for not allowed attributes
        try {
            elem.noOtherNCNameAttribute(attr_names, HttpConstants.BOTH_NS_URIS);
        }
        catch ( ToolsException ex ) {
            throw new HttpClientException("Invalid attributes", ex);
        }
        // handle childs
        myChilds = getBodyElement().getContent();
        // If there is no content in the http:body element, take the next item
        // in the $bodies parameter.
        try {
            if ( myChilds.isEmpty() ) {
                Sequence body = bodies.next();
                if ( body == null ) {
                    throw new HttpClientException("There is not enough items within $bodies");
                }
                myChilds = body;
            }
        }
        catch ( ToolsException ex ) {
            throw new HttpClientException("Technical error walking through the http:body content", ex);
        }
    }

    private Boolean parseYesNo(Element elem, String attr_name)
            throws HttpClientException
    {
        String val = elem.getAttribute(attr_name);
        if ( val == null ) {
            return null;
        }
        else if ( "yes".equals(val) ) {
            return Boolean.TRUE;
        }
        else if ( "no".equals(val) ) {
            return Boolean.FALSE;
        }
        else {
            String msg = "Incorrect value for " + attr_name + ": " + val;
            throw new HttpClientException(msg);
        }
    }

    @Override
    public void setHeaders(HeaderSet headers)
            throws HttpClientException
    {
        // set the Content-Type header (if not set by the user)
        // TODO: "if not set by the user" -> really? To clarify within the spec.
        if ( headers.getFirstHeader("Content-Type") == null ) {
            String type = getContentType();
            // TODO: This has to be re-written when the @encoding serialization
            // param will be supported.
            if ( myMethod == Type.XML
                    || myMethod == Type.HTML
                    || myMethod == Type.XHTML
                    || myMethod == Type.TEXT ) {
                if ( mySerial.getEncoding() == null ) {
                    mySerial.setEncoding("UTF-8");
                }
                type += "; charset=" + mySerial.getEncoding();
            }
            else if ( mySerial.getEncoding() != null ) {
                throw new HttpClientException("Encoding is not allowed with method '" + myMethod + "'");
            }
            headers.add("Content-Type", type);
        }
    }

    @Override
    public void serialize(OutputStream out)
            throws HttpClientException
    {
        if ( myMethod == Type.HEX ) {
            // TODO: Add support for HEX (== "base16")
            // out = new Base16.OutputStream(out, Base16.DECODE);
            throw new HttpClientException("Method 'hex' not supported yet");
        }
        else if ( myMethod == Type.BINARY || myMethod == Type.BASE64 ) {
            out = new Base64.OutputStream(out, Base64.DECODE);
        }
        try {
            myChilds.serialize(out, mySerial);
        }
        catch ( ToolsException ex ) {
            throw new HttpClientException("Error serializing the result", ex);
        }
    }

    @Override
    public boolean isMultipart()
    {
        return false;
    }

    private void setOutputProperty(Properties props, String value, String key)
    {
        if ( value != null ) {
            props.setProperty(key, value);
        }
    }

    private void setYesNoOutputProperty(Properties props, Boolean value, String key)
    {
        if ( value != null ) {
            props.setProperty(key, value ? "yes" : "no");
        }
    }

    private Type myMethod;
    private Sequence myChilds;
    private SerialParameters mySerial = new SerialParameters();
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
