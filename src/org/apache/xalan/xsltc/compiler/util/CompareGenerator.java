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

package org.apache.xalan.xsltc.compiler.util;

import org.apache.bcel.generic.ACONST_NULL;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ILOAD;
import org.apache.bcel.generic.ISTORE;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.Type;
import org.apache.xalan.xsltc.compiler.Constants;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
public final class CompareGenerator extends MethodGenerator {

    private static int DOM_INDEX      = 1;
    private static int CURRENT_INDEX  = 2;
    private static int LEVEL_INDEX    = 3;
    private static int TRANSLET_INDEX = 4;
    private static int LAST_INDEX     = 5;
    private int ITERATOR_INDEX = 6;

    private final Instruction _iloadCurrent;
    private final Instruction _istoreCurrent;
    private final Instruction _aloadDom;
    private final Instruction _iloadLast;
    private final Instruction _aloadIterator;
    private final Instruction _astoreIterator;

    public CompareGenerator(int access_flags, Type return_type,
			    Type[] arg_types, String[] arg_names,
			    String method_name, String class_name,
			    InstructionList il, ConstantPoolGen cp) {
	super(access_flags, return_type, arg_types, arg_names, method_name, 
	      class_name, il, cp);
	
	_iloadCurrent = new ILOAD(CURRENT_INDEX);
	_istoreCurrent = new ISTORE(CURRENT_INDEX);
	_aloadDom = new ALOAD(DOM_INDEX);
	_iloadLast = new ILOAD(LAST_INDEX);

	LocalVariableGen iterator =
	    addLocalVariable("iterator",
			     Util.getJCRefType(Constants.NODE_ITERATOR_SIG),
			     null, null);
	ITERATOR_INDEX = iterator.getIndex();
	_aloadIterator = new ALOAD(ITERATOR_INDEX);
	_astoreIterator = new ASTORE(ITERATOR_INDEX);
	il.append(new ACONST_NULL());
	il.append(storeIterator());
    }

    public Instruction loadLastNode() {
	return _iloadLast;
    }

    public Instruction loadCurrentNode() {
	return _iloadCurrent;
    }

    public Instruction storeCurrentNode() {
	return _istoreCurrent;
    }

    public Instruction loadDOM() {
	return _aloadDom;
    }

    public int getHandlerIndex() {
	return INVALID_INDEX;		// not available
    }

    public int getIteratorIndex() {
	return INVALID_INDEX;
    }

    public Instruction storeIterator() {
	return _astoreIterator;
    }
    
    public Instruction loadIterator() {
	return _aloadIterator;
    }

    //??? may not be used anymore
    public int getLocalIndex(String name) {
	if (name.equals("current")) {
	    return CURRENT_INDEX;
	}
	return super.getLocalIndex(name);
    }
}
