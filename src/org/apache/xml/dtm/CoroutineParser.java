/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999,2000 The Apache Software Foundation.  All rights 
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
 * 4. The names "Xerces" and "Apache Software Foundation" must
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
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

//package org.apache.xerces.parsers;
package org.apache.xalan.xml.dtm;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.IOException;
import org.apache.xml.dtm.CoroutineManager;

/** <p>CoroutineParser is an API for parser threads that operate as
 * coroutines. See CoroutineSAXParser and CoroutineSAXParser_Xerces
 * for examples.</p>
 *
 * <p>&lt;grumble&gt; I'd like the interface to require a specific form
 * for either the base constructor or a static factory method. Java
 * doesn't allow us to specify either, so I'll just document them
 * here:
 *
 * <ul>
 * <li>public CoroutineParser(CoroutineManager co, int appCoroutine);</li>
 * <li>public CoroutineParser createCoroutineParser(CoroutineManager co, int appCoroutine);</li>
 * </ul>
 *
 * &lt;/grumble&gt;</p>
 *
 * <p>Status: In progress</p>
 * */
public interface CoroutineParser extends Runnable {

    /** @return the coroutine ID number for this CoroutineParser object.
     * Note that this isn't useful unless you know which CoroutineManager
     * you're talking to.
     * */
    public int getParserCoroutine();

    /**
     * This coroutine (thread) can be resumed with the following arguments:
     *
     *      null            terminate this coroutine.
     *                      exits with:
     *                          co_exit_to(null, ...)
     *                      expects next:
     *                          nothing, we have terminated the thread.
     *
     *      InputSource     setup to read from this source.
     *                      resumes with:
     *                          co_resume(Boolean.TRUE, ...) on success.
     *                          co_resume(Boolean.FALSE, ...) on failure. (not sure why?)
     *                          co_resume(Exception, ...) on error. (what I expect for failure)
     *
     *      Boolean.TRUE    indication to continue parsing the current document.
     *                      resumes with:
     *                          co_resume(Boolean.TRUE, ...) on success with more to parse.
     *                          co_resume(Boolean.FALSE, ...) on success when finished.
     *                          co_resume(Exception, ...) on error.
     *
     *      Boolean.FALSE   indication to discontinue parsing and reset.
     *                      resumes with:
     *                          co_resume(Boolean.FALSE, ...) always.
     */
    public void run();

} // class CoroutineSAXParser_Xerces
