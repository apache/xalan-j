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
package org.apache.xpath.impl.parser;

import org.apache.xml.QName;
import org.apache.xpath.expression.NodeTest;

/**
 * QNameNode wrappers a 'real' QName object.
 */
public class QNameWrapper extends SimpleNode {

    /**
     * The wrapped QName
     */
    QName m_qname;
    

	/**
	 * Constructor for QName.
	 * @param i
	 */
	public QNameWrapper(int i) {
		super(i);
	}

	/**
	 * Constructor for QName.
	 * @param p
	 * @param i
	 */
	public QNameWrapper(XPath p, int i) {
		super(p, i);
	}

	/**
	 * @see org.apache.xpath.impl.parser.SimpleNode#processToken(Token)
	 */
	public void processToken(Token t) {
		super.processToken(t);
		String qname;
		switch (id) {
			case XPathTreeConstants.JJTSTAR :
                m_qname = NodeTest.WILDCARD;                
				break;
			case XPathTreeConstants.JJTSTARCOLONNCNAME :               
                qname = t.image.trim();
                qname = qname.substring(qname.indexOf(":")+1);
                m_qname = new QName("*", qname, "*");
                
				break;
			case XPathTreeConstants.JJTNCNAMECOLONSTAR :
            case XPathTreeConstants.JJTQNAME :
            case XPathTreeConstants.JJTQNAMELPAR :
				qname = t.image;
				int parenIndex = qname.lastIndexOf("("); 
				if (parenIndex > 0) {
					qname = qname.substring(0, parenIndex);
				}
				qname = qname.trim();
				int colonIdx = qname.indexOf(":");
                if ( colonIdx == -1 ) {
					m_qname = new QName(qname);
                } else {
                	// TODO: Need to use qname factory
                m_qname = new QName("defaultns", qname.substring(colonIdx + 1), qname.substring(0, colonIdx ) );                
                }
				break;
                   
           default:
           throw new RuntimeException( "Invalid jjtree id: doesn't match a QName id=" + id);
		}
	}


	/**
	 * @return org.apache.xml.QName
	 */
	public org.apache.xml.QName getQName() {
		return m_qname;
	}

}
