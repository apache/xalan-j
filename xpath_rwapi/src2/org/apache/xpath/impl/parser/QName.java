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

import org.apache.xpath.expression.NodeTest;

/**
 * QNameNode wrappers a 'real' QName object.
 * //@TODO should be renamed! 13-Mar-03 -sc
 */
public class QName extends SimpleNode {

    /**
     * The wrapped QName
     */
    org.apache.xml.QName m_qname;
    

	/**
	 * Constructor for QName.
	 * @param i
	 */
	public QName(int i) {
		super(i);
	}

	/**
	 * Constructor for QName.
	 * @param p
	 * @param i
	 */
	public QName(XPath p, int i) {
		super(p, i);
		//m_prefixResolver = p.m_prefixResolver;
	}

	/**
	 * @see org.apache.xpath.impl.parser.SimpleNode#processToken(Token)
	 */
	public void processToken(Token t) {
		super.processToken(t);
		String qname;
		switch (id) {
			case XPathTreeConstants.JJTSTAR :
				//m_prefix = null;
                //m_localPart = NodeTest.WILDCARD;
                             
                m_qname = new org.apache.xml.QName(NodeTest.WILDCARD);                
				break;
			case XPathTreeConstants.JJTSTARCOLONNCNAME :
                //m_prefix = NodeTest.WILDCARD;
                //m_localPart = t.image.trim(); 
                //m_localPart=m_localPart.substring(m_localPart.indexOf(":")+1);
                qname = t.image.trim();
                qname = qname.substring(qname.indexOf(":")+1);
                m_qname = new org.apache.xml.QName(NodeTest.WILDCARD, qname);
                
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
				//m_qname = new org.apache.xml.utils.QName(qname, m_prefixResolver);
                int colonIdx = qname.indexOf(":");
                //String m_prefix;
                //String m_localPart;
                if ( colonIdx == -1 ) {
                    //m_prefix = null;
                    //m_localPart = qname;
					m_qname = new org.apache.xml.QName(qname);
                } else {
                //m_prefix = qname.substring(0, colonIdx );
                //m_localPart = qname.substring(colonIdx + 1);
                m_qname = new org.apache.xml.QName(null, qname.substring(colonIdx + 1), qname.substring(0, colonIdx ) );                
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
