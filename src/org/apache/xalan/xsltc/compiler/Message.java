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
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.PUSH;
import org.apache.xalan.xsltc.compiler.util.ClassGenerator;
import org.apache.xalan.xsltc.compiler.util.MethodGenerator;
import org.apache.xalan.xsltc.compiler.util.Type;
import org.apache.xalan.xsltc.compiler.util.TypeCheckError;

final class Message extends Instruction {
    private boolean _terminate = false;

    public void parseContents(Parser parser) {
	String termstr = getAttribute("terminate");
	if (termstr != null) {
            _terminate = termstr.equals("yes");
	}
	parseChildren(parser);
    }

    public Type typeCheck(SymbolTable stable) throws TypeCheckError {
	typeCheckContents(stable);
	return Type.Void;
    }

    public void translate(ClassGenerator classGen, MethodGenerator methodGen) {
	final ConstantPoolGen cpg = classGen.getConstantPool();
	final InstructionList il = methodGen.getInstructionList();

	// Load the translet (for call to displayMessage() function)
	il.append(classGen.loadTranslet());

        switch (elementCount()) {
            case 0:
                il.append(new PUSH(cpg, ""));
            break;
            case 1:
                SyntaxTreeNode child = (SyntaxTreeNode) elementAt(0);
                if (child instanceof Text) {
                    il.append(new PUSH(cpg, ((Text) child).getText()));
                    break;
                }
                // falls through
            default:
                // Push current output handler onto the stack
                il.append(methodGen.loadHandler());

                // Replace the current output handler by a ToXMLStream
                il.append(new NEW(cpg.addClass(STREAM_XML_OUTPUT)));
                il.append(methodGen.storeHandler());

                // Push a reference to a StringWriter
                il.append(new NEW(cpg.addClass(STRING_WRITER)));
                il.append(DUP);
                il.append(DUP);
                il.append(new INVOKESPECIAL(
                    cpg.addMethodref(STRING_WRITER, "<init>", "()V")));

                // Load ToXMLStream
                il.append(methodGen.loadHandler());
                il.append(new INVOKESPECIAL(
                    cpg.addMethodref(STREAM_XML_OUTPUT, "<init>",
                                     "()V")));

                // Invoke output.setWriter(STRING_WRITER)
                il.append(methodGen.loadHandler());
                il.append(SWAP);
                il.append(new INVOKEVIRTUAL(
                    cpg.addMethodref(OUTPUT_BASE, "setWriter",
                                     "("+WRITER_SIG+")V")));

                // Invoke output.setEncoding("UTF-8")
                il.append(methodGen.loadHandler());
                il.append(new PUSH(cpg, "UTF-8"));   // other encodings?
                il.append(new INVOKEVIRTUAL(
                    cpg.addMethodref(OUTPUT_BASE, "setEncoding",
                                     "("+STRING_SIG+")V")));

                // Invoke output.setOmitXMLDeclaration(true)
                il.append(methodGen.loadHandler());
                il.append(ICONST_1);
                il.append(new INVOKEVIRTUAL(
                    cpg.addMethodref(OUTPUT_BASE, "setOmitXMLDeclaration",
                                     "(Z)V")));

                il.append(methodGen.loadHandler());
                il.append(new INVOKEVIRTUAL(
                    cpg.addMethodref(OUTPUT_BASE, "startDocument",
                                     "()V")));

                // Inline translation of contents
                translateContents(classGen, methodGen);

                il.append(methodGen.loadHandler());
                il.append(new INVOKEVIRTUAL(
                    cpg.addMethodref(OUTPUT_BASE, "endDocument",
                                     "()V")));

                // Call toString() on StringWriter
                il.append(new INVOKEVIRTUAL(
                    cpg.addMethodref(STRING_WRITER, "toString",
                                     "()" + STRING_SIG)));

                // Restore old output handler
                il.append(SWAP);
                il.append(methodGen.storeHandler());
            break;
        }

	// Send the resulting string to the message handling method
	il.append(new INVOKEVIRTUAL(cpg.addMethodref(TRANSLET_CLASS,
						     "displayMessage",
						     "("+STRING_SIG+")V")));

	// If 'terminate' attribute is set to 'yes': Instanciate a
	// RunTimeException, but it on the stack and throw an exception
	if (_terminate == true) {
	    // Create a new instance of RunTimeException
	    final int einit = cpg.addMethodref("java.lang.RuntimeException",
					       "<init>",
					       "(Ljava/lang/String;)V");
	    il.append(new NEW(cpg.addClass("java.lang.RuntimeException")));
	    il.append(DUP);
	    il.append(new PUSH(cpg,"Termination forced by an " +
			           "xsl:message instruction"));
	    il.append(new INVOKESPECIAL(einit));
	    il.append(ATHROW);
	}
    }

}
