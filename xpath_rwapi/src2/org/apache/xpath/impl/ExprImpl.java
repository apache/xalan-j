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
package org.apache.xpath.impl;

import org.apache.xpath.expression.Expr;
import org.apache.xpath.expression.Visitor;
import org.apache.xpath.impl.parser.Node;
import org.apache.xpath.impl.parser.SimpleNode;
import org.apache.xpath.impl.parser.XPath;


/**
 *
 */
public abstract class ExprImpl extends SimpleNode implements Expr
{
    /**
    	 *
    	 */
    protected ExprImpl()
    {
        super();
    }

    /**
     * Constructor for ExprImpl.
     *
     * @param i
     */
    public ExprImpl(int i)
    {
        super(i);
    }

    /**
     * Constructor for ExprImpl.
     *
     * @param p
     * @param i
     */
    public ExprImpl(XPath p, int i)
    {
        super(p, i);
    }

    /**
     * Gets expression as external string representation
     *
     * @param expr DOCUMENT ME!
     * @param abbreviate DOCUMENT ME!
     */
    public void getString(StringBuffer expr, boolean abbreviate)
    {
    }

    /**
     * @see org.apache.xpath.expression.Expr#getString(boolean)
     */
    public String getString(boolean abbreviate)
    {
        StringBuffer buf = new StringBuffer();
        getString(buf, abbreviate);

        return buf.toString();
    }

    /**
     * @see org.apache.xpath.expression.Visitable#visit(Visitor)
     */
    public boolean visit(Visitor visitor)
    {
        return true;
    }

    /**
     * Clone children
     *
     * @return DOCUMENT ME!
     */
    protected Node[] cloneChildren()
    {
		Node[] clone;
    	if (m_children != null)
    	{
        	 clone = new Node[m_children.length];

        	for (int i = 0; i < m_children.length; i++)
        	{
        		Node child = m_children[i];
        		if (child instanceof Expr) {
            		clone[i] = (Node) ((Expr) child).cloneExpression();
        		} else {
        			// immutable object, just copy reference
        			clone[i] = child;
        		}
        	}
    	} else 
    	{
    		clone = null;
    	}

        return clone;
    }
}
