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

import java.util.Vector;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.InstructionList;
import org.apache.xalan.xsltc.compiler.util.ClassGenerator;
import org.apache.xalan.xsltc.compiler.util.MethodGenerator;
import org.apache.xalan.xsltc.compiler.util.StringType;
import org.apache.xalan.xsltc.compiler.util.Type;
import org.apache.xalan.xsltc.compiler.util.TypeCheckError;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
final class UnparsedEntityUriCall extends FunctionCall {
    private Expression _entity;

    public UnparsedEntityUriCall(QName fname, Vector arguments) {
        super(fname, arguments);
        _entity = argument();
    }

    public Type typeCheck(SymbolTable stable) throws TypeCheckError {
        final Type entity = _entity.typeCheck(stable);
        if (entity instanceof StringType == false) {
            _entity = new CastExpr(_entity, Type.String);
        }
        return _type = Type.String;
    }
    
    public void translate(ClassGenerator classGen, MethodGenerator methodGen) {
        final ConstantPoolGen cpg = classGen.getConstantPool();
        final InstructionList il = methodGen.getInstructionList();
        // Push the this pointer on the stack...
        il.append(methodGen.loadDOM());
        // ...then the entity name...
        _entity.translate(classGen, methodGen);
        // ...to get the URI from the DOM object.
        il.append(new INVOKEINTERFACE(
                         cpg.addInterfaceMethodref(DOM_INTF,
                                                   GET_UNPARSED_ENTITY_URI,
                                                   GET_UNPARSED_ENTITY_URI_SIG),
                         2));
    }
}
