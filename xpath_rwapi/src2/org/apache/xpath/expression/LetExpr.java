/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002-2003 The Apache Software Foundation.  All rights 
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
 * originally based on software copyright (c) 2002, International
 * Business Machines Corporation., http://www.ibm.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xpath.expression;

import org.apache.xml.QName;
import org.apache.xpath.XPathException;
import org.apache.xpath.datamodel.SequenceType;

/**
 * Represents <code>let</code> expressions.
 * A <code>let</code> expression consists of a list of let clause with binds a variable,
 * eventually typed, to an expression. For example, the following expression:
 * <pre><![CDATA[
 * let $s := (<one/>, <two/>, <three/>)
 * ]>  
 * </pre>
 * contains one let clause which bind the variable <code>s</code> with a sequence
 * of three elements.
 * @author <a href="mailto:villard@us.ibm.com">Lionel Villard</a>
 * @version $Id$
 */
public interface LetExpr extends Expr
{

	/**
	 * Gets the number of let clauses 
	 * @return int
	 */
	int getClauseCount();
	
	/**
	 * Gets the variable name of the ith let clause
	 * @param i
	 * @return QName
	 * @throws XPathException when the specified index is out of bounds
	 */
	QName getVariableName(int i) throws XPathException;
	
	/**
	 * Gets the type of the ith let clause
	 * @param i
	 * @return A {@link SequenceType} or null when no type is specified.
	 * @throws XPathException when the specified index is out of bounds
	 */
	SequenceType getType(int i) throws XPathException;
	
	/**
	 * Gets the expression of the ith let clause
	 * @param i
	 * @return An expression
	 * @throws XPathException when the specified index is out of bounds
	 */
	Expr getExpr(int i) throws XPathException;
	
	/**
	 * Append a clause at the end of the clause list
	 * @param varName
	 * @param type
	 * @param expr
	 * @throws XPathException when the specified expression is not a single expression
	 */
	void appendClause(QName varName, SequenceType type, Expr expr)  throws XPathException;
	
	/**
	 * Insert a clause before the ith position.
	 * @param i
	 * @param varName
	 * @param type
	 * @param expr
	 * @throws XPathException when the specified expression is not a single expression
	 * or when the specified index is out of bounds
	 */
	void insertClause(int i, QName varName, SequenceType type, Expr expr) throws XPathException;
	
	/**
	 * Remove the clause at the ith position
	 * @param i
	 * @throws XPathException when the specified index is out of bounds
	 */
	void removeClause(int i) throws XPathException;
}
