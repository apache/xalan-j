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

package org.apache.xalan.xsltc.runtime;

import org.xml.sax.SAXException;

import org.apache.xml.serializer.EmptySerializer;

public final class StringValueHandler extends EmptySerializer {

    private StringBuffer _buffer = new StringBuffer();
    private String _str = null;
    private static final String EMPTY_STR = "";
    private boolean m_escaping = false;
    private int _nestedLevel = 0;
	
    public void characters(char[] ch, int off, int len) 
	throws SAXException 
    {
	if (_nestedLevel > 0)
	    return;
	
	if (_str != null) {
	    _buffer.append(_str);
	    _str = null;
	}
	_buffer.append(ch, off, len);
    }

    public String getValue() {
	if (_buffer.length() != 0) {
	    String result = _buffer.toString();
	    _buffer.setLength(0);
	    return result;
	}
	else {
	    String result = _str;
	    _str = null;
	    return (result != null) ? result : EMPTY_STR;
	}
    }

    public void characters(String characters) throws SAXException {
	if (_nestedLevel > 0)
	    return;

	if (_str == null && _buffer.length() == 0) {
	    _str = characters;
	}
	else {
	    if (_str != null) {
	        _buffer.append(_str);
	        _str = null;
	    }
	    
	    _buffer.append(characters);
	}
    }
    
    public void startElement(String qname) throws SAXException {
        _nestedLevel++;
    }

    public void endElement(String qname) throws SAXException {
        _nestedLevel--;
    }

    // Override the setEscaping method just to indicate that this class is
    // aware that that method might be called.
    public boolean setEscaping(boolean bool) {
        boolean oldEscaping = m_escaping;
        m_escaping = bool;

        return bool;
    }

    /**
     * The value of a PI must not contain the substring "?>". Should
     * that substring be present, replace it by "? >". 
     */
    public String getValueOfPI() {
	final String value = getValue();

	if (value.indexOf("?>") > 0) {
	    final int n = value.length(); 
	    final StringBuffer valueOfPI = new StringBuffer();

	    for (int i = 0; i < n;) {
		final char ch = value.charAt(i++);
		if (ch == '?' && value.charAt(i) == '>') {
		    valueOfPI.append("? >"); i++;
		}
		else {
		    valueOfPI.append(ch);
		}
	    } 
	    return valueOfPI.toString();
	}
	return value;
    }
}
