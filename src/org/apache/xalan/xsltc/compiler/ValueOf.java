/*
 * @(#)$Id$
 *
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xalan" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 2001, Sun
 * Microsystems., http://www.sun.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 *
 */

package org.apache.xalan.xsltc.compiler;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.PUSH;
import org.apache.xalan.xsltc.compiler.util.ClassGenerator;
import org.apache.xalan.xsltc.compiler.util.ErrorMsg;
import org.apache.xalan.xsltc.compiler.util.MethodGenerator;
import org.apache.xalan.xsltc.compiler.util.Type;
import org.apache.xalan.xsltc.compiler.util.TypeCheckError;
import org.apache.xalan.xsltc.compiler.util.Util;

final class ValueOf extends Instruction {
    private Expression _select;
    private boolean _escaping = true;
    private boolean _isString = false;

    public void display(int indent) {
        indent(indent);
        Util.println("ValueOf");
        indent(indent + IndentIncrement);
        Util.println("select " + _select.toString());
    }

    public void parseContents(Parser parser) {
        _select = parser.parseExpression(this, "select", null);

        // make sure required attribute(s) have been set
        if (_select.isDummy()) {
            reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, "select");
            return;
        }
        final String str = getAttribute("disable-output-escaping");
        if ((str != null) && (str.equals("yes"))) _escaping = false;
    }

    public Type typeCheck(SymbolTable stable) throws TypeCheckError {
        Type type = _select.typeCheck(stable);

        // Prefer to handle the value as a node; fall back to String, otherwise
        if (type != null && !type.identicalTo(Type.Node)) {
            /***
             *** %HZ% Would like to treat result-tree fragments in the same
             *** %HZ% way as node sets for value-of, but that's running into
             *** %HZ% some snags.  Instead, they'll be converted to String
            if (type.identicalTo(Type.ResultTree)) {
                _select = new CastExpr(new CastExpr(_select, Type.NodeSet),
                                       Type.Node);
            } else
            ***/
            if (type.identicalTo(Type.NodeSet)) {
                _select = new CastExpr(_select, Type.Node);
            } else {
                _isString = true;
                if (!type.identicalTo(Type.String)) {
                    _select = new CastExpr(_select, Type.String);
                }
                _isString = true;
            }
        }
        return Type.Void;
    }

    public void translate(ClassGenerator classGen, MethodGenerator methodGen) {
        final ConstantPoolGen cpg = classGen.getConstantPool();
        final InstructionList il = methodGen.getInstructionList();
        final int setEscaping = cpg.addInterfaceMethodref(OUTPUT_HANDLER,
                                                          "setEscaping","(Z)Z");

        // Turn off character escaping if so is wanted.
        if (!_escaping) {
            il.append(methodGen.loadHandler());
            il.append(new PUSH(cpg,false));
            il.append(new INVOKEINTERFACE(setEscaping,2));
        }

        // Translate the contents.  If the value is a string, use the
        // translet.characters(String, TranslatOutputHandler) method.
        // Otherwise, the value is a node, and the
        // dom.characters(int node, TransletOutputHandler) method can dispatch
        // the string value of the node to the output handler more efficiently.
        if (_isString) {
            final int characters = cpg.addMethodref(TRANSLET_CLASS,
                                                    CHARACTERSW,
                                                    CHARACTERSW_SIG);

            il.append(classGen.loadTranslet());
            _select.translate(classGen, methodGen);
            il.append(methodGen.loadHandler());
            il.append(new INVOKEVIRTUAL(characters));
        } else {
            final int characters = cpg.addInterfaceMethodref(DOM_INTF,
                                                             CHARACTERS,
                                                             CHARACTERS_SIG);

            il.append(methodGen.loadDOM());
            _select.translate(classGen, methodGen);
            il.append(methodGen.loadHandler());
            il.append(new INVOKEINTERFACE(characters, 3));
        }

        // Restore character escaping setting to whatever it was.
        if (!_escaping) {
            il.append(methodGen.loadHandler());
            il.append(SWAP);
            il.append(new INVOKEINTERFACE(setEscaping,2));
            il.append(POP);
        }
    }
}
