/****************************************************************************/
/*  File:       SendRequestFunction.java                                    */
/*  Author:     F. Georges - fgeorges.org                                   */
/*  Date:       2009-08-08                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2009 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.httpclient.saxon;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceUri;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.SequenceType;
import org.expath.httpclient.HttpConstants;
import org.expath.pkg.saxon.EXPathFunctionDefinition;

/**
 * TODO: Doc...
 *
 * @author Florent Georges
 */
public class SendRequestFunction
        extends EXPathFunctionDefinition
{
// TODO: To adapt to the new EXPath Tools Saxon facilities...
//
//    public SendRequestFunction(Library lib)
//    {
//        super(lib);
//    }
//
//    @Override
//    protected Definition makeDefinition()
//            throws ToolsException
//    {
//        Types types = new Types(library());
//        return library()
//                .function(this, LOCAL_NAME)
//                .returns(Types.SEVERAL_ITEM)
//                .param(types.singleElement(PARAM_REQ, null), PARAM_REQ)
//                .optional()
//                .param(Types.OPTIONAL_STRING, PARAM_URL)
//                .param(Types.ANY_ITEM, PARAM_BODIES)
//                .make();
//    }

    @Override
    public void setConfiguration(Configuration config)
    {
        myConfig = config;
    }

    @Override
    public StructuredQName getFunctionQName()
    {
        final String uri    = HttpConstants.HTTP_CLIENT_NS_URI;
        final String prefix = HttpConstants.HTTP_CLIENT_NS_PREFIX;
        return new StructuredQName(prefix, uri, LOCAL_NAME);
    }

    @Override
    public int getMinimumNumberOfArguments()
    {
        return 1;
    }

    @Override
    public int getMaximumNumberOfArguments()
    {
        return 3;
    }

    @Override
    public SequenceType[] getArgumentTypes()
    {
        // 1/ element(http:request)
        final SequenceType stype1;
        if (myConfig != null) {
            final NamePool pool = myConfig.getNamePool();
            final ItemType itype = new NameTest(Type.ELEMENT, NamespaceUri.of(HttpConstants.HTTP_CLIENT_NS_URI), "request", pool);
            stype1 = SequenceType.makeSequenceType(itype, StaticProperty.EXACTLY_ONE);
        } else {
            stype1 = SequenceType.makeSequenceType(NodeKindTest.ELEMENT, StaticProperty.EXACTLY_ONE);
        }

        // 2/ xs:string?
        SequenceType   stype2 = SequenceType.OPTIONAL_STRING;
        // 3/ item()*
        SequenceType   stype3 = SequenceType.ANY_SEQUENCE;
        // 1/, 2/ and 3/
        return new SequenceType[]{ stype1, stype2, stype3 };
    }

    @Override
    public SequenceType getResultType(SequenceType[] params)
    {
        final int      more  = StaticProperty.ALLOWS_ONE_OR_MORE;
        final ItemType itype = AnyItemType.getInstance();
        return SequenceType.makeSequenceType(itype, more);
    }

    @Override
    public boolean hasSideEffects()
    {
        return true;
    }

    @Override
    public ExtensionFunctionCall makeCallExpression()
    {
        return new SendRequestCall();
    }

    private static final String LOCAL_NAME = "send-request";
    private Configuration myConfig;
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
