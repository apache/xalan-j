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
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES{} LOSS OF
 * USE, DATA, OR PROFITS{} OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
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
 * @author Santiago Pericas-Geertsen
 *
 */

package org.apache.xalan.xsltc.runtime.output;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

class WriterOutputBuffer implements OutputBuffer {
    private static final int KB = 1024;
    private static int BUFFER_SIZE = 4 * KB;

    static {
	// Set a larger buffer size for Solaris
	final String osName = System.getProperty("os.name");
	if (osName.equalsIgnoreCase("solaris")) {
	    BUFFER_SIZE = 32 * KB;
	}
    }

    private Writer _writer;

    /**
     * Initializes a WriterOutputBuffer by creating an instance of a 
     * BufferedWriter. The size of the buffer in this writer may have 
     * a significant impact on throughput. Solaris prefers a larger
     * buffer, while Linux works better with a smaller one.
     */
    public WriterOutputBuffer(Writer writer) {
	_writer = new BufferedWriter(writer, BUFFER_SIZE);
    }

    public String close() {
	try {
	    _writer.flush();
	}
	catch (IOException e) {
	    throw new RuntimeException(e.toString());
	}
	return "";
    }

    public OutputBuffer append(String s) {
	try {
	    _writer.write(s);
	}
	catch (IOException e) {
	    throw new RuntimeException(e.toString());
	}
	return this;
    }

    public OutputBuffer append(char[] s, int from, int to) {
	try {
	    _writer.write(s, from, to);
	}
	catch (IOException e) {
	    throw new RuntimeException(e.toString());
	}
	return this;
    }

    public OutputBuffer append(char ch) {
	try {
	    _writer.write(ch);
	}
	catch (IOException e) {
	    throw new RuntimeException(e.toString());
	}
	return this;
    }
}


