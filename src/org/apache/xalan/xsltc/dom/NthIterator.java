/*
 * @(#)$Id$
 *
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 * @author Morten Jorgensen
 *
 */

package org.apache.xalan.xsltc.dom;

import org.apache.xalan.xsltc.DOM;
import org.apache.xalan.xsltc.NodeIterator;

public final class NthIterator extends NodeIteratorBase {
    // ...[N]
    private final NodeIterator _source;
    private int _position = 1;
    private int _n = 0;
    private boolean _ready;

    public NthIterator(NodeIterator source, int n) {
	_source = source;
	_n = n;
    }
    
    public int next() {
	if (_ready && _position > 0) {
	    _ready = false;
	    // skip N-1 nodes
	    for (int n = _position - 1; n-- > 0;) {
		if (_source.next() == NodeIterator.END) {
		    return NodeIterator.END;
		}
	    }
	    return _source.next();
	}
	return NodeIterator.END;
    }
	
    public NodeIterator setStartNode(final int node) {
	_source.setStartNode(node);
	/*
	// Make sure we count backwards if the iterator is reverse
	if (_source.isReverse()) {
	    int last = _source.getLast();
	    _position = (last - _n) + 1;
	    if (_position < 1) _position = 1;
	}
	else {
	    _position = _n;
	}
	*/
	_position = _n;
	_ready = true;
	return this;
    }
	
    public NodeIterator reset() {
	_source.reset();
	return this;
    }
    
    public int getLast() {
	return 1;
    }
    
    public int getPosition() {
	return 1;
    }
    
    public boolean isReverse() {
	return _source.isReverse();
    }
    
    public void setMark() {
	_source.setMark();
    }
    
    public void gotoMark() {
	_source.gotoMark();
    }
    
    public NodeIterator cloneIterator() {
	//!! not clear when cloning is performed
	// and what's the desired state of the new clone
	return new NthIterator(_source.cloneIterator(), _position);
    }

}
