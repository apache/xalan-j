/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import org.apache.xalan.xsltc.compiler.util.Type;
import org.apache.xalan.xsltc.compiler.util.TypeCheckError;

/**
 * @author Morten Jorgensen
 * @author Santiago Pericas-Geertsen
 */
class VariableRefBase extends Expression {

    /**
     * A reference to the associated variable.
     */
    protected final VariableBase _variable; 

    /**
     * A reference to the enclosing expression/instruction for which a
     * closure is needed (Predicate, Number or Sort).
     */
    protected Closure _closure = null;

    public VariableRefBase(VariableBase variable) {
	_variable = variable;
	variable.addReference(this);
    }

    public VariableRefBase() {
	_variable = null;
    }

    /**
     * Returns a reference to the associated variable
     */
    public VariableBase getVariable() {
	return _variable;
    }

    /**
     * Returns a reference to any parent variable
     */
    public VariableBase findParentVariable() {
	SyntaxTreeNode node = this;
	while (node != null && !(node instanceof VariableBase)) {
	    node = node.getParent();
	}
	return (VariableBase) node;
    }

    /**
     * Two variable references are deemed equal if they refer to the 
     * same variable.
     */
    public boolean equals(Object obj) {
	try {
	    return (_variable == ((VariableRefBase) obj)._variable);
	} 
	catch (ClassCastException e) {
	    return false;
	}
    }

    /**
     * Returns a string representation of this variable reference on the
     * format 'variable-ref(<var-name>)'.
     * @return Variable reference description
     */
    public String toString() {
	return "variable-ref("+_variable.getName()+'/'+_variable.getType()+')';
    }

    public Type typeCheck(SymbolTable stable) 
	throws TypeCheckError 
    {
	// Returned cached type if available
	if (_type != null) return _type;

	// Find nearest closure to add a variable reference
	if (_variable.isLocal()) {
	    SyntaxTreeNode node = getParent();
	    do {
		if (node instanceof Closure) {
		    _closure = (Closure) node;
		    break;
		}
		if (node instanceof TopLevelElement) {
		    break;	// way up in the tree
		}
		node = node.getParent();
	    } while (node != null);

	    if (_closure != null) {
		_closure.addVariable(this);
	    }
	}

	// Insert a dependency link from one variable to another
        VariableBase parent = findParentVariable();
        if (parent != null) {
            VariableBase var = _variable;
            if (_variable._ignore) {
                if (_variable instanceof Variable) {
                    var = parent.getSymbolTable()
                                .lookupVariable(_variable._name);
                } else if (_variable instanceof Param) {
                    var = parent.getSymbolTable().lookupParam(_variable._name);
                }
            }
            parent.addDependency(var);
        }

        // Attempt to get the cached variable type
        _type = _variable.getType();

        // If that does not work we must force a type-check (this is normally
        // only needed for globals in included/imported stylesheets
        if (_type == null) {
            _variable.typeCheck(stable);
            _type = _variable.getType();
        }

        // Return the type of the referenced variable
        return _type;
    }

}
