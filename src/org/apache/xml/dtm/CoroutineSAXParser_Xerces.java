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

/** <p>CoroutineSAXParser_Xerces illustrates how to run Xerces's incremental
 * parsing feature (parseSome()) in a coroutine. Output from Xerces
 * will still be issued via callbacks, which will need to be recieved
 * and acted upon by an appopriate handler.</p>
 *
 * Usage is something like this:
 * <code>
 *      CoroutineManager co = new CoroutineManager()
 *      int appCoroutine = co.co_joinCoroutineSet(-1);
 *      if (appCoroutine == -1) { [[error handling]] }
 *      CoroutineParser parser = CoroutineSAXParser_Xerces.createCoroutineparser(co, appCoroutine);
 *      int parserCoroutine = parser.getParserCoroutine();
 *      [[register typical SAX handlers with the parser]]
 *
 *      ...
 *
 *      InputSource source = [[document source]];
 *      Object result = co.co_resume(source, appCoroutine, parserCoroutine);
 *      if (result == null) {
 *          [[cannot happen here, only if we ask parser to terminate]]
 *      }
 *      else if (result instanceof Boolean) {
 *          if (((Boolean)result).booleanValue()) {
 *              [[document open, ready to proceed]]
 *          }
 *          else {
 *              [[document not open, but no exception?  I think that this might
 *               relate to the continue-on-fatal-error feature, but obviously
 *               we cannot continue in this case...]]
 *          }
 *      }
 *      else if (result instanceof Exception) {
 *          [[process error]]
 *      }
 *
 *      ...
 *
 *      [[nothing in queue to process, so run the parser coroutine]]
 *      Object result = co.co_resume(Boolean.TRUE, appCoroutine, parserCoroutine);
 *      if (result == null) {
 *          [[cannot happen here, only if we ask parser to terminate]]
 *      }
 *      else if (result instanceof Boolean) {
 *          if (((Boolean)result).booleanValue()) {
 *              [[some parsing has been performed and
 *                the end of the document has not been seen]]
 *          }
 *          else {
 *              [[some parsing might have been performed and
 *                the end of the document has been seen]]
 *          }
 *      }
 *      else if (result instanceof Exception) {
 *          [[process error during parsing]]
 *      }
 *
 *      ...
 *
 *      [[reset the parser coroutine]]
 *      Object result = co.co_resume(Boolean.FALSE, appCoroutine, parserCoroutine);
 *      [[returns Boolean.FALSE, expect next InputSource]]
 *
 *      ...
 *
 *      [[terminate the parser coroutine]]
 *      Object result = co.co_resume(null, appCoroutine, parserCoroutine);
 *      [[returns null]]
 * </code>
 *
 * <p>Status: In progress</p>
 *
 * */
public class CoroutineSAXParser_Xerces
extends org.apache.xerces.parsers.SAXParser
implements CoroutineParser, Runnable {

    //
    // Data
    //

    private CoroutineManager fCoroutineManager = null;
    private int fAppCoroutine = -1;
    private int fParserCoroutine = -1;
    private boolean fParseInProgress=false;

    //
    // Constructors
    //

    public CoroutineSAXParser_Xerces(CoroutineManager co, int appCoroutine) {

        initHandlers(true, this, this);

        fCoroutineManager = co;
        fAppCoroutine = appCoroutine;
        fParserCoroutine = co.co_joinCoroutineSet(-1);
        if (fParserCoroutine == -1)
            throw new RuntimeException("co_joinCoroutineSet() failed");
        Thread t = new Thread(this);
        t.setDaemon(false);
        t.start();
    }

    //
    // Factories
    //
    static public CoroutineParser createCoroutineParser(CoroutineManager co, int appCoroutine) {
      return new CoroutineSAXParser_Xerces(co, appCoroutine);
    }

    //
    // Public methods
    //

    // coroutine support

    public int getParserCoroutine() {
        return fParserCoroutine;
    }

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
    public void run() {
      try {
	  
        Object arg = null;
        while (true) {
            arg = fCoroutineManager.co_resume(arg, fParserCoroutine, fAppCoroutine);
            if (arg == null) {
                fCoroutineManager.co_exit_to(arg, fParserCoroutine, fAppCoroutine);
                break;
            }
            if (arg instanceof InputSource) {
                if (fParseInProgress) {
                    arg = new SAXException("parse may not be called while parsing.");
                }
                else {
                    try {
                        boolean ok = parseSomeSetup((InputSource)arg);
                        arg = ok ? Boolean.TRUE : Boolean.FALSE;
                    }
                    catch (Exception ex) {
                        arg = ex;
                    }
                }
            }
            else if (arg instanceof Boolean) {
                boolean keepgoing = ((Boolean)arg).booleanValue();
                if (!keepgoing) {
                    fParseInProgress = false;
                    arg = Boolean.FALSE;
                }
                else {
                    try {
                        keepgoing = parseSome();
                        arg = keepgoing ? Boolean.TRUE : Boolean.FALSE;
                    } catch (SAXException ex) {
                        arg = ex;
                    } catch (IOException ex) {
                        arg = ex;
                    } catch (Exception ex) {
                        arg = new SAXException(ex);
                    }
                }
            }
        }
	
      }
      catch(java.lang.NoSuchMethodException e)
	{
	  // Shouldn't happen unless we've miscoded our coroutine logic
	  // "Shut down the garbage smashers on the detention level!"
	  e.printStackTrace(System.err);
          fCoroutineManager.co_exit(fParserCoroutine);
	}
    }

} // class CoroutineSAXParser_Xerces
