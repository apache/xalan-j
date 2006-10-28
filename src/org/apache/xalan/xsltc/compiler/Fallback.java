/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id$
 */

package org.apache.xalan.xsltc.compiler;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionList;
import org.apache.xalan.xsltc.compiler.util.ClassGenerator;
import org.apache.xalan.xsltc.compiler.util.MethodGenerator;
import org.apache.xalan.xsltc.compiler.util.Type;
import org.apache.xalan.xsltc.compiler.util.TypeCheckError;

/**
 * @author Morten Jorgensen
 */
final class Fallback extends Instruction {

    private boolean _active = false;

    /**
     * This element never produces any data on the stack
     */
    public Type typeCheck(SymbolTable stable) throws TypeCheckError {
	if (_active) {
	    return(typeCheckContents(stable));
	}
	else {
	    return Type.Void;
	}
    }

    /**
     * Activate this fallback element
     */
    public void activate() {
	_active = true;
    }

    public String toString() {
	return("fallback");
    }

    /**
     * Parse contents only if this fallback element is put in place of
     * some unsupported element or non-XSLTC extension element
     */
    public void parseContents(Parser parser) {
	if (_active) parseChildren(parser);
    }

    /**
     * Translate contents only if this fallback element is put in place of
     * some unsupported element or non-XSLTC extension element
     */
    public void translate(ClassGenerator classGen, MethodGenerator methodGen) {
	final ConstantPoolGen cpg = classGen.getConstantPool();
	final InstructionList il = methodGen.getInstructionList();

	if (_active) translateContents(classGen, methodGen);
    }
}
