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
package org.apache.xml.dtm;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.IOException;
import org.apache.xml.dtm.CoroutineManager;

/** <p>CoroutineSAXParser_Xerces illustrates how to run Xerces's incremental
 * parsing feature (parseSome()) in a coroutine. Output from Xerces
 * will still be issued via callbacks, which will need to be recieved
 * and acted upon by an appopriate handler.</p>
 *
 * <p>Usage example: See main().</p>
 *
 * <p>Status: Passes simple main() unit-test</p>
 * */
public class CoroutineSAXParser_Xerces
extends org.apache.xerces.parsers.SAXParser
implements CoroutineParser, Runnable
{

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

  // Note name, needed to dodge the inherited Xerces setLexicalHandler
  // which isn't public.
  public void setLexHandler(org.xml.sax.ext.LexicalHandler handler)
  {
    // Not supported by all SAX2 parsers but should work in Xerces:
    try 
      {
	setProperty("http://xml.org/sax/properties/lexical-handler",
		    this);
      }
    catch(org.xml.sax.SAXNotRecognizedException e)
      {
	// Nothing we can do about it
      }
    catch(org.xml.sax.SAXNotSupportedException e)
      {
	// Nothing we can do about it
      }
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


  //================================================================
  /** doMore() is a simple API which tells the coroutine parser
   * that we need more nodes.  This is intended to be called from one
   * of our partner coroutines, and serves both to encapsulate the
   * communication protocol and to avoid having to explicitly use the
   * CoroutineParser's coroutine ID number.
   *
   * %TBD% doParse(uri)?
   *
   * @param parsemore If true, tells the incremental parser to generate
   * another chunk of output. If false, tells the parser that we're
   * satisfied and it can terminate parsing of this document.
   * @param appCoroutine The coroutine ID number of the coroutine invoking
   * this method, so it can be resumed after the parser has responded to the
   * request.
   * @return True if the CoroutineParser believes more data may be available
   * for further parsing. False means either parsemore=false or end of document
   * caused parsing to stop.
   * */
  public boolean doMore(boolean parsemore, int appCoroutine)
  {
    return false;    //%TBD%
  }
  
  
  /** doTerminate() is a simple API which tells the coroutine
   * parser to terminate itself.  This is intended to be called from
   * one of our partner coroutines, and serves both to encapsulate the
   * communication protocol and to avoid having to explicitly use the
   * CoroutineParser's coroutine ID number.
   *
   * Returns only after the CoroutineParser has acknowledged the request.
   *
   * @param appCoroutine The coroutine ID number of the coroutine invoking
   * this method, so it can be resumed after the parser has responded to the
   * request.
   * */
  public void doTerminate(int appCoroutine)
  {
    //%TBD%
  }

  //================================================================
  /** Simple unit test. Attempt coroutine parsing of document indicated
   * by first argument (as a URI), report progress.
   */
  public static void main(String args[])
  {
    System.out.println("Starting...");
    
    CoroutineManager co = new CoroutineManager();
    int appCoroutine = co.co_joinCoroutineSet(-1);
    if (appCoroutine == -1)
      {
	System.out.println("ERROR: Couldn't allocate coroutine number.\n");
	return;
      }
    CoroutineSAXParser_Xerces parser=
      new CoroutineSAXParser_Xerces(co, appCoroutine);
    int parserCoroutine = parser.getParserCoroutine();

    // Use a serializer as our sample output
    org.apache.xml.serialize.XMLSerializer trace;
    trace=new org.apache.xml.serialize.XMLSerializer(System.out,null);
    parser.setContentHandler(trace);
    parser.setLexHandler(trace);

    // Tell coroutine to begin parsing, run while parsing is in progress
    for(int arg=0;arg<args.length;++arg)
      {
	try
	  {
	    InputSource source = new InputSource(args[arg]);
	    Object result=null;
	    Boolean more=Boolean.TRUE;
	    for(result = co.co_resume(source, appCoroutine, parserCoroutine);
		(result instanceof Boolean && ((Boolean)result)==Boolean.TRUE);
		result = co.co_resume(more, appCoroutine, parserCoroutine))
	      {
		System.out.println("\nSome parsing successful, trying more.\n");
	    
		// Special test: Terminate parsing early.
		if(arg+1<args.length && "!".equals(args[arg+1]))
		  {
		    ++arg;
		    more=Boolean.FALSE;
		  }
	    
	      }
	
	    if (result instanceof Boolean && ((Boolean)result)==Boolean.FALSE)
	      {
		System.out.println("\nParser ended (EOF or on request).\n");
	      }
	    else if (result == null) {
	      System.out.println("\nUNEXPECTED: Parser says shut down prematurely.\n");
	    }
	    else if (result instanceof Exception) {
	      System.out.println("\nParser threw exception:");
	      ((Exception)result).printStackTrace();
	    }

	  }
	catch(java.lang.NoSuchMethodException e)
	  {
	    System.out.println("\nUNEXPECTED Coroutine not resolved:");
	    e.printStackTrace();
	  }
      }

    try
      {
	System.out.println("Requesting parser shutdown");
	Object result = co.co_resume(null, appCoroutine, parserCoroutine);
	if(result!=null)
	  System.out.println("\nUNEXPECTED: Parser co-shutdown answers "+result);
      }
    catch(java.lang.NoSuchMethodException e)
      {
	System.out.println("\nUNEXPECTED Coroutine not resolved:");
	e.printStackTrace();
      }
  }
} // class CoroutineSAXParser_Xerces
