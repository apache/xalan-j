/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.BranchHandle;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.IFGT;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.PUSH;
import org.apache.xalan.xsltc.compiler.util.ClassGenerator;
import org.apache.xalan.xsltc.compiler.util.MethodGenerator;
import org.apache.xalan.xsltc.compiler.util.StringType;
import org.apache.xalan.xsltc.compiler.util.Type;
import org.apache.xalan.xsltc.compiler.util.TypeCheckError;
import org.apache.xalan.xsltc.compiler.util.Util;

/**
 * @author Morten Jorgensen
 * @author Santiago Pericas-Geertsen
 */
final class KeyCall extends FunctionCall {

    /**
     * The name of the key.
     */
    private Expression _name;

    /**
     * The value to look up in the key/index.
     */
    private Expression _value;

    /**
     * The value's data type.
     */
    private Type _valueType; // The value's data type

    /**
     * Expanded qname when name is literal.
     */
    private QName _resolvedQName = null;

    /**
     * Get the parameters passed to function:
     *   key(String name, String value)
     *   key(String name, NodeSet value)
     * The 'arguments' vector should contain two parameters for key() calls,
     * one holding the key name and one holding the value(s) to look up. The
     * vector has only one parameter for id() calls (the key name is always
     * "##id" for id() calls).
     *
     * @param fname The function name (should be 'key' or 'id')
     * @param arguments A vector containing the arguments the the function
     */
    public KeyCall(QName fname, Vector arguments) {
	super(fname, arguments);
	switch(argumentCount()) {
	case 1:
	    _name = null;
	    _value = argument(0);
	    break;
	case 2:
	    _name = argument(0);
	    _value = argument(1);
	    break;
	default:
	    _name = _value = null;
	    break;
	}
    }

    /**
     * Type check the parameters for the id() or key() function.
     * The index name (for key() call only) must be a string or convertable
     * to a string, and the lookup-value must be a string or a node-set.
     * @param stable The parser's symbol table
     * @throws TypeCheckError When the parameters have illegal type
     */
    public Type typeCheck(SymbolTable stable) throws TypeCheckError {
	final Type returnType = super.typeCheck(stable);

	// Run type check on the key name (first argument) - must be a string,
	// and if it is not it must be converted to one using string() rules.
	if (_name != null) {
	    final Type nameType = _name.typeCheck(stable); 

	    if (_name instanceof LiteralExpr) {
		final LiteralExpr literal = (LiteralExpr) _name;
		_resolvedQName = 
		    getParser().getQNameIgnoreDefaultNs(literal.getValue());
	    }
	    else if (nameType instanceof StringType == false) {
		_name = new CastExpr(_name, Type.String);
	    }
	}

	// Run type check on the value for this key. This value can be of
	// any data type, so this should never cause any type-check errors.
	// If the value is not a node-set then it should be converted to a
	// string before the lookup is done. If the value is a node-set then
	// this process (convert to string, then do lookup) should be applied
	// to every node in the set, and the result from all lookups should
	// be added to the resulting node-set.
	_valueType = _value.typeCheck(stable);

	if (_valueType != Type.NodeSet && _valueType != Type.String) 
	{
	    _value = new CastExpr(_value, Type.String);
	}

	return returnType;
    }

    /**
     * This method is called when the constructor is compiled in
     * Stylesheet.compileConstructor() and not as the syntax tree is traversed.
     * This method is a wrapper for the real translation method, which is
     * the private method translateCall() below. All this method does is to
     * wrap the KeyIndex that this function returns inside a duplicate filter.
     * The duplicate filter is used both to eliminate duplicates and to
     * cache the nodes in the index.
     * @param classGen The Java class generator
     * @param methodGen The method generator
     */
    public void translate(ClassGenerator classGen,
			  MethodGenerator methodGen) {
	final ConstantPoolGen cpg = classGen.getConstantPool();
	final InstructionList il = methodGen.getInstructionList();
						 
	final int getNodeHandle = cpg.addInterfaceMethodref(DOM_INTF,
							   "getNodeHandle",
							   "(I)"+NODE_SIG);	

	// Wrap the KeyIndex (iterator) inside a duplicate filter iterator
	// to pre-read the indexed nodes and cache them.
	final int dupInit = cpg.addMethodref(DUP_FILTERED_ITERATOR,
					     "<init>",
					     "("+NODE_ITERATOR_SIG+")V");


        // Backwards branches are prohibited if an uninitialized object is
        // on the stack by section 4.9.4 of the JVM Specification, 2nd Ed.
        // We don't know whether this code might contain backwards branches
        // so we mustn't create the new object until after we've created
        // the suspect arguments to its constructor.  Instead we calculate
        // the values of the arguments to the constructor first, store them
        // in temporary variables, create the object and reload the
        // arguments from the temporaries to avoid the problem.

	LocalVariableGen keyIterator = translateCall(classGen, methodGen);

	il.append(new NEW(cpg.addClass(DUP_FILTERED_ITERATOR)));	
	il.append(DUP);
        il.append(new ALOAD(keyIterator.getIndex()));
	il.append(new INVOKESPECIAL(dupInit));
    }

    /**
     * Translate the actual index lookup - leaves KeyIndex (iterator) stored
     * in a temporary that the caller will need to load.
     * @param classGen The Java class generator
     * @param methodGen The method generator
     * @return A <code>org.apache.bcel.generic.LocalVariableGen</code>
     *         object that will contain the iterator for the key call.
     */
    private LocalVariableGen translateCall(ClassGenerator classGen,
			                   MethodGenerator methodGen) {

	final ConstantPoolGen cpg = classGen.getConstantPool();
	final InstructionList il = methodGen.getInstructionList();

	// Returns the string value for a node in the DOM
	final int getNodeValue = cpg.addInterfaceMethodref(DOM_INTF,
							   GET_NODE_VALUE,
							   "(I)"+STRING_SIG);

	// Returns the KeyIndex object of a given name
	final int getKeyIndex = cpg.addMethodref(TRANSLET_CLASS,
						 "getKeyIndex",
						 "(Ljava/lang/String;)"+
						 KEY_INDEX_SIG);

	// Initialises a KeyIndex to return nodes with specific values
	final int lookupId = cpg.addMethodref(KEY_INDEX_CLASS,
					      "lookupId",
					      "(Ljava/lang/Object;)V");
	final int lookupKey = cpg.addMethodref(KEY_INDEX_CLASS,
					       "lookupKey",
					       "(Ljava/lang/Object;)V");

	// Merges the nodes in two KeyIndex objects
	final int merge = cpg.addMethodref(KEY_INDEX_CLASS,
					   "merge",
					   "("+KEY_INDEX_SIG+")V");

	// Constructor for KeyIndex class
	final int indexConstructor = cpg.addMethodref(TRANSLET_CLASS,
						      "createKeyIndex",
						      "()"+KEY_INDEX_SIG);
						      
	// KeyIndex.setDom(Dom) => void
	final int keyDom = cpg.addMethodref(XSLT_PACKAGE + ".dom.KeyIndex",
					 "setDom",
					 "("+DOM_INTF_SIG+")V");				 
						      
	
	// This local variable holds the index/iterator we will return
	final LocalVariableGen returnIndex =
	    methodGen.addLocalVariable("returnIndex",
				       Util.getJCRefType(KEY_INDEX_SIG),
				       il.getEnd(), null);

	// This local variable holds the index we're using for search
	final LocalVariableGen searchIndex =
	    methodGen.addLocalVariable("searchIndex",
				       Util.getJCRefType(KEY_INDEX_SIG),
				       il.getEnd(), null);

	// If the second paramter is a node-set we need to go through each
	// node in the set, convert each one to a string and do a look up in
	// the named index, and then merge all the resulting node sets.
	if (_valueType == Type.NodeSet) {
	    // Save current node and current iterator on the stack
	    il.append(methodGen.loadCurrentNode());
	    il.append(methodGen.loadIterator());

	    // Get new iterator from 2nd parameter node-set & store in variable
	    _value.translate(classGen, methodGen);
	    _value.startIterator(classGen, methodGen);
	    il.append(methodGen.storeIterator());

	    // Create the KeyIndex object (the iterator) we'll return
	    il.append(classGen.loadTranslet());
	    il.append(new INVOKEVIRTUAL(indexConstructor));
	    il.append(DUP);
	    il.append(methodGen.loadDOM());
	    il.append(new INVOKEVIRTUAL(keyDom));
	    il.append(new ASTORE(returnIndex.getIndex()));

	    // Initialise the index specified in the first parameter of key()
	    il.append(classGen.loadTranslet());
	    if (_name == null) {
		il.append(new PUSH(cpg,"##id"));
	    }
	    else if (_resolvedQName != null) {
		il.append(new PUSH(cpg, _resolvedQName.toString()));
	    }
	    else {
		_name.translate(classGen, methodGen);
	    }

	    il.append(new INVOKEVIRTUAL(getKeyIndex));
	    il.append(new ASTORE(searchIndex.getIndex()));

	    // LOOP STARTS HERE

	    // Now we're ready to start traversing the node-set given in
	    // the key() function's second argument....
	    final BranchHandle nextNode = il.append(new GOTO(null));
	    final InstructionHandle loop = il.append(NOP);

	    // Push returnIndex on stack to prepare for call to merge()
	    il.append(new ALOAD(returnIndex.getIndex()));
	    
	    // Lookup index using the string value from the current node
	    il.append(new ALOAD(searchIndex.getIndex()));
	    il.append(DUP);
	    il.append(methodGen.loadDOM());
	    il.append(methodGen.loadCurrentNode());
	    il.append(new INVOKEINTERFACE(getNodeValue, 2));
	    if (_name == null) {
		il.append(new INVOKEVIRTUAL(lookupId));
	    }
	    else {
		il.append(new INVOKEVIRTUAL(lookupKey));
	    }

	    // Call to returnIndex.merge(searchIndex);
	    il.append(new INVOKEVIRTUAL(merge));
		
	    // Go on with next node in the 2nd parameter node-set
	    nextNode.setTarget(il.append(methodGen.loadIterator()));
	    il.append(methodGen.nextNode());
	    il.append(DUP);
	    il.append(methodGen.storeCurrentNode());
	    il.append(new IFGT(loop));

	    // LOOP ENDS HERE

	    // Restore current node and current iterator from the stack
	    il.append(methodGen.storeIterator());
	    il.append(methodGen.storeCurrentNode());
	}
	// If the second parameter is a single value we just lookup the named
	// index and initialise the iterator to return nodes with this value.
	else {
	    // Call getKeyIndex in AbstractTranslet with the name of the key
	    // to get the index for this key (which is also a node iterator).
	    il.append(classGen.loadTranslet());
	    if (_name == null) {
		il.append(new PUSH(cpg,"##id"));
	    }
	    else if (_resolvedQName != null) {
		il.append(new PUSH(cpg, _resolvedQName.toString()));
	    }
	    else {
		_name.translate(classGen, methodGen);
	    }
	    il.append(new INVOKEVIRTUAL(getKeyIndex));

	    // Now use the value in the second argument to determine what nodes
	    // the iterator should return.
	    il.append(DUP);

	    _value.translate(classGen, methodGen);

	    if (_name == null) {
		il.append(new INVOKEVIRTUAL(lookupId));
	    }
	    else {
		il.append(new INVOKEVIRTUAL(lookupKey));
	    }

	    il.append(new ASTORE(returnIndex.getIndex()));
	}

        return returnIndex;
    }
}
