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

package org.apache.xml.dtm;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;
import java.io.IOException;
import org.apache.xml.dtm.CoroutineManager;

/** <p>CoroutineSAXParser runs a SAX2 parser in a coroutine to achieve
 * incremental parsing. Output from the parser will still be issued
 * via callbacks, which will need to be recieved and acted upon by an
 * appopriate handler. But those callbacks will pass through a
 * counting stage which periodically yields control back to the other
 * coroutines in this set.</p>
 *
 * <p>For a brief usage example, see the unit-test main() method.</p>
 *
 * <p>Status: Passes simple main() unit-test</p>
 *
 * %TBD% Javadoc needs lots of work.
 * */
public class CoroutineSAXParser
implements CoroutineParser, Runnable, ContentHandler, LexicalHandler  {

  boolean DEBUG=false; //Internal status report

  //
  // Data
  //

  private CoroutineManager fCoroutineManager = null;
  private int fAppCoroutineID = -1;
  private int fParserCoroutineID = -1;
  private boolean fParseInProgress=false;
  private org.xml.sax.XMLReader xmlreader=null;
  private org.xml.sax.ContentHandler clientContentHandler=null;
  private org.xml.sax.ext.LexicalHandler clientLexicalHandler=null;
  private int eventcounter;
  private int frequency=10;

  //
  // Constructors
  //

  /** Wrap a SAX2 parser.
   * %TBD% whether we should consider supporting SAX1
   */
  public CoroutineSAXParser(CoroutineManager co, int appCoroutineID,
                            org.xml.sax.XMLReader parser) {
    xmlreader=parser;
    xmlreader.setContentHandler(this);

    // Not supported by all SAX2 parsers:
    try 
      {
        xmlreader.setProperty("http://xml.org/sax/properties/lexical-handler",
                              this);
      }
    catch(SAXNotRecognizedException e)
      {
        // Nothing we can do about it
      }
    catch(SAXNotSupportedException e)
      {
        // Nothing we can do about it
      }

    eventcounter=frequency;

    fCoroutineManager = co;
    fAppCoroutineID = appCoroutineID;
    fParserCoroutineID = co.co_joinCoroutineSet(-1);
    if (fParserCoroutineID == -1)
      throw new RuntimeException("co_joinCoroutineSet() failed");
    Thread t = new Thread(this);
    t.setDaemon(false);
    t.start();
  }

  //
  // Public methods
  //

  // Register a content handler for us to output to
  public void setContentHandler(ContentHandler handler)
  {
    clientContentHandler=handler;
  }
  // Register a lexical handler for us to output to
  // Not all parsers support this...
  // ??? Should we register directly on the parser?
  // NOTE NAME.
  public void setLexHandler(LexicalHandler handler)
  {
    clientLexicalHandler=handler;
  }

  // Set the number of events between resumes of our coroutine
  // Immediately resets number of events before _next_ resume as well.
  public void setReturnFrequency(int events)
  {
    if(events<1) events=1;
    frequency=eventcounter=events;
  }
  
  //
  // ContentHandler methods
  // These  pass the data to our client ContentHandler...
  // but they also count the number of events passing through,
  // and resume our coroutine each time that counter hits zero and
  // is reset.
  //
  // Note that for everything except endDocument, we do the count-and-yield
  // BEFORE passing the call along. I'm hoping that this will encourage JIT
  // compilers to realize that these are tail-calls, reducing the expense of
  // the additional layer of data flow.
  //
  // %REVIEW% Glenn suggests that pausing after endElement, endDocument,
  // and characters may be sufficient. I actually may not want to
  // stop after characters, since in our application these wind up being
  // concatenated before they're processed... but that risks huge blocks of
  // text causing greater than usual readahead. (Unlikely? Consider the
  // possibility of a large base-64 block in a SOAP stream.)
  //
  public void characters(char[] ch, int start, int length)
       throws org.xml.sax.SAXException
  {
    if(--eventcounter<=0)
      {
        co_yield(true);
        eventcounter=frequency;
      }
    if(clientContentHandler!=null)
      clientContentHandler.characters(ch,start,length);
  }
  public void endDocument() 
       throws org.xml.sax.SAXException
  {
    // EXCEPTION: In this case we need to run the event BEFORE we yield.
    if(clientContentHandler!=null)
      clientContentHandler.endDocument();

    eventcounter=0;	
    // We do not have to yield in this case. Just return.
    // When parser exits, the run() loop will yield false to indicate
    // parsing is done.
  }
  public void endElement(java.lang.String namespaceURI, java.lang.String localName,
      java.lang.String qName) 
       throws org.xml.sax.SAXException
  {
    if(--eventcounter<=0)
      {
        co_yield(true);
        eventcounter=frequency;
      }
    if(clientContentHandler!=null)
      clientContentHandler.endElement(namespaceURI,localName,qName);
  }
  public void endPrefixMapping(java.lang.String prefix) 
       throws org.xml.sax.SAXException
  {
    if(--eventcounter<=0)
      {
        co_yield(true);
        eventcounter=frequency;
      }
    if(clientContentHandler!=null)
      clientContentHandler.endPrefixMapping(prefix);
  }
  public void ignorableWhitespace(char[] ch, int start, int length) 
       throws org.xml.sax.SAXException
  {
    if(--eventcounter<=0)
      {
        co_yield(true);
        eventcounter=frequency;
      }
    if(clientContentHandler!=null)
      clientContentHandler.ignorableWhitespace(ch,start,length);
  }
  public void processingInstruction(java.lang.String target, java.lang.String data) 
       throws org.xml.sax.SAXException
  {
    if(--eventcounter<=0)
      {
        co_yield(true);
        eventcounter=frequency;
      }
    if(clientContentHandler!=null)
      clientContentHandler.processingInstruction(target,data);
  }
  public void setDocumentLocator(Locator locator) 
  {
    if(--eventcounter<=0)
      {
        co_yield(true);
        eventcounter=frequency;
      }
    if(clientContentHandler!=null)
      clientContentHandler.setDocumentLocator(locator);
  }
  public void skippedEntity(java.lang.String name) 
       throws org.xml.sax.SAXException
  {
    if(--eventcounter<=0)
      {
        co_yield(true);
        eventcounter=frequency;
      }
    if(clientContentHandler!=null)
      clientContentHandler.skippedEntity(name);
  }
  public void startDocument() 
       throws org.xml.sax.SAXException
  {
    if(--eventcounter<=0)
      {
        co_yield(true);
        eventcounter=frequency;
      }
    if(clientContentHandler!=null)
      clientContentHandler.startDocument();
  }
  public void startElement(java.lang.String namespaceURI, java.lang.String localName,
      java.lang.String qName, Attributes atts) 
       throws org.xml.sax.SAXException
  {
    if(--eventcounter<=0)
      {
        co_yield(true);
        eventcounter=frequency;
      }
    if(clientContentHandler!=null)
      clientContentHandler.startElement(namespaceURI, localName, qName, atts);
  }
  public void startPrefixMapping(java.lang.String prefix, java.lang.String uri) 
       throws org.xml.sax.SAXException
  {
    if(--eventcounter<=0)
      {
        co_yield(true);
        eventcounter=frequency;
      }
    if(clientContentHandler!=null)
      clientContentHandler.startPrefixMapping(prefix,uri);
  }

  //
  // LexicalHandler support. Not all SAX2 parsers support these events
  // but we may want to pass them through when they exist...
  //
  // %REVIEW% These do NOT currently affect the eventcounter; I'm asserting
  // that they're rare enough that it makes little or no sense to
  // pause after them. As such, it may make more sense for folks who
  // actually want to use them to register directly with the parser.
  // But I want 'em here for now, to remind us to recheck this assertion!
  //
  public void comment(char[] ch, int start, int length) 
       throws org.xml.sax.SAXException
  {
    if(null!=clientLexicalHandler)
      clientLexicalHandler.comment(ch,start,length);
  }
  public void endCDATA() 
       throws org.xml.sax.SAXException
  {
    if(null!=clientLexicalHandler)
      clientLexicalHandler.endCDATA();
  }
  public void endDTD() 
       throws org.xml.sax.SAXException
  {
    if(null!=clientLexicalHandler)
      clientLexicalHandler.endDTD();
  }
  public void endEntity(java.lang.String name) 
       throws org.xml.sax.SAXException
  {
    if(null!=clientLexicalHandler)
      clientLexicalHandler.endEntity(name);
  }
  public void startCDATA() 
       throws org.xml.sax.SAXException
  {
    if(null!=clientLexicalHandler)
      clientLexicalHandler.startCDATA();
  }
  public void startDTD(java.lang.String name, java.lang.String publicId,
      java.lang.String systemId) 
       throws org.xml.sax.SAXException
  {
    if(null!=clientLexicalHandler)
      clientLexicalHandler. startDTD(name, publicId, systemId);
  }
  public void startEntity(java.lang.String name) 
       throws org.xml.sax.SAXException
  {
    if(null!=clientLexicalHandler)
      clientLexicalHandler.startEntity(name);
  }

  //
  // coroutine support
  //

  public int getParserCoroutineID() {
    return fParserCoroutineID;
  }

  /** <p>In the SAX delegation code, I've inlined the count-down in
   * the hope of encouraging compilers to deliver better
   * performance. However, if we subclass (eg to directly connect the
   * output to a DTM builder), that would require calling super in
   * order to run that logic... which seems inelegant.  Hence this
   * routine for the convenience of subclasses: every [frequency]
   * invocations, issue a co_yield.</p>
   *
   * @param moreExepcted Should always be true unless this is being called
   * at the end of endDocument() handling.
   * */
  void count_and_yield(boolean moreExpected)
  {
    if(!moreExpected) eventcounter=0;
    
    if(--eventcounter<=0)
      {
        co_yield(true);
        eventcounter=frequency;
      }
  }
  

  /**
   * Co_Yield handles coroutine interactions while a parse is in progress.
   * It will resume with 
   *   co_resume(Boolean.TRUE, ...) on success with more to parse.
   *   co_resume(Boolean.FALSE, ...) on success after endDocument.
   *
   * When control is passed back it may indicate
   *
   *      null            terminate this coroutine.
   *                      Issues
   *                          co_exit_to(null, ...)
   *			  and throws UserRequestedShutdownException
   *
   *      Boolean.TRUE    indication to continue parsing the current document.
   *			  Resumes normal SAX parsing.
   *
   *      Boolean.FALSE   indication to discontinue parsing and reset.
   *			  Throws UserRequestedStopException
   *			  to return control to the run() loop.
   */
  private void co_yield(boolean notYetDone)
  {
    Object arg= notYetDone ? Boolean.TRUE : Boolean.FALSE;
    try
      {
        arg = fCoroutineManager.co_resume(arg, fParserCoroutineID, fAppCoroutineID);
        
        if (arg == null) {
          fCoroutineManager.co_exit_to(arg, fParserCoroutineID, fAppCoroutineID);
          throw shutdownException;
        }

        else if (arg instanceof Boolean) {
          boolean keepgoing = ((Boolean)arg).booleanValue();
          if (!keepgoing)
            throw stopException;
          }

        else // Unexpected!
          {
            System.err.println(
                  "Active CoroutineSAXParser: unexpected resume parameter, "
                  +arg.getClass
                  ()+" with value=\""+arg+'"');
            System.err.println("\tStopping parser rather than risk deadlock");
            throw new RuntimeException("Coroutine parameter error ("+arg+')');
          }

      }
    catch(java.lang.NoSuchMethodException e)
      {
        // Shouldn't happen unless we've miscoded our coroutine logic
        // "Shut down the garbage smashers on the detention level!"
        e.printStackTrace(System.err);
        fCoroutineManager.co_exit(fParserCoroutineID);
        throw shutdownException;
      }
  }


  /** Between parser executions, wait for our partner to resume us and
   * tell us what to do:
   *
   *      null            terminate this coroutine.
   *                      exits with:
   *                          co_exit_to(null, ...)
   *                      expects next:
   *                          nothing, we have terminated the thread.
   *
   *      InputSource     setup to read from this source.
   *                      resumes with:
   *                          co_resume(Boolean.TRUE, ...) on partial parse
   *                          co_resume(Boolean.FALSE, ...) on complete parse
   *                          co_resume(Exception, ...) on error.
   *
   * %REVEIW% Should this be able to set listeners? Partner coroutine ID?
   * */
  public void run() {
    try 
      {
        for(Object arg=fCoroutineManager.co_entry_pause(fParserCoroutineID);
            true;
            arg=fCoroutineManager.co_resume(arg, fParserCoroutineID, fAppCoroutineID))
          {
            
            // Shut down requested.
            if (arg == null) {
              if(DEBUG)System.out.println("CoroutineSAXParser at-rest shutdown requested");
              fCoroutineManager.co_exit_to(arg, fParserCoroutineID, fAppCoroutineID);
              break;
            }
            
            // Start-Parse requested
            // For the duration of this operation, all coroutine handshaking
            // will occur in the co_yield method. That's the nice thing about
            // coroutines; they give us a way to hand off control from the
            // middle of a synchronous method.
            if (arg instanceof InputSource) {
              try {
              if(DEBUG)System.out.println("Inactive CoroutineSAXParser new parse "+arg);
                xmlreader.parse((InputSource)arg);
		// Tell caller we returned from parsing
                arg=Boolean.FALSE;
              }

              catch (SAXException ex) {
                Exception inner=ex.getException();
                if(inner instanceof UserRequestedStopException){
                  if(DEBUG)System.out.println("Active CoroutineSAXParser user stop exception");
                  arg=Boolean.FALSE;
                }
                else if(inner instanceof UserRequestedShutdownException){
                  if(DEBUG)System.out.println("Active CoroutineSAXParser user shutdown exception");
                  break;
                }
                else {
                  if(DEBUG)System.out.println("Active CoroutineSAXParser UNEXPECTED SAX exception: "+ex);
                  arg=ex;		  
                }
                
              }
              catch(Exception ex)
                {
                  if(DEBUG)System.out.println("Active CoroutineSAXParser non-SAX exception: "+ex);
                  arg = ex;
                }
              
            }

            else // Unexpected!
              {
                System.err.println(
                  "Inactive CoroutineSAXParser: unexpected resume parameter, "
                  +arg.getClass()+" with value=\""+arg+'"');
              }

          } // end while
      } // end try
    catch(java.lang.NoSuchMethodException e)
      {
        // Shouldn't happen unless we've miscoded our coroutine logic
        // "CPO, shut down the garbage smashers on the detention level!"
        e.printStackTrace(System.err);
        fCoroutineManager.co_exit(fParserCoroutineID);
      }
  }

  /** Used so co_yield can return control to run for early parse
   * termination.  */
  class UserRequestedStopException extends RuntimeException
  {
  }
  
  /** %REVIEW% Should be static, but can't be because internal class */
  final UserRequestedStopException stopException=new UserRequestedStopException();

  /** Used so co_yield can return control to run for coroutine thread
   * termination.  */
  class UserRequestedShutdownException extends RuntimeException
  {
  }

  /** %REVIEW% Should be static, but can't be because internal class */
  final UserRequestedShutdownException shutdownException = new UserRequestedShutdownException();

  //================================================================
  /** doParse() is a simple API which tells the coroutine parser
   * to begin reading from a file.  This is intended to be called from one
   * of our partner coroutines, and serves both to encapsulate the
   * communication protocol and to avoid having to explicitly use the
   * CoroutineParser's coroutine ID number.
   *
   * %REVIEW% Can/should this unify with doMore? (if URI hasn't changed,
   * parse more from same file, else end and restart parsing...?
   *
   * @param source The InputSource to parse from.
   * @param appCoroutineID The coroutine ID number of the coroutine invoking
   * this method, so it can be resumed after the parser has responded to the
   * request.
   * @return Boolean.TRUE if the CoroutineParser believes more data may be available
   * for further parsing. Boolean.FALSE if parsing ran to completion.
   * Exception if the parser objected for some reason.
   * */
  public Object doParse(InputSource source, int appCoroutineID)
  {
    try 
      {
	Object result=    
	  fCoroutineManager.co_resume(source, appCoroutineID, fParserCoroutineID);
	
	// %REVIEW% Better error reporting needed... though most of these
	// should never arise during normal operation.
	// Should this rethrow the parse exception?
	if (result instanceof Exception) {
	  System.out.println("\nParser threw exception:");
	  ((Exception)result).printStackTrace();
	}

	return result;
      }

    // SHOULD NEVER OCCUR, since the coroutine number and coroutine manager
    // are those previously established for this CoroutineSAXParser...
    // So I'm just going to return it as a parsing exception, for now.
    catch(NoSuchMethodException e)
      {
	return e;
      }
  }
  
  
  /** doMore() is a simple API which tells the coroutine parser
   * that we need more nodes.  This is intended to be called from one
   * of our partner coroutines, and serves both to encapsulate the
   * communication protocol and to avoid having to explicitly use the
   * CoroutineParser's coroutine ID number.
   *
   * @param parsemore If true, tells the incremental parser to generate
   * another chunk of output. If false, tells the parser that we're
   * satisfied and it can terminate parsing of this document.
   * @param appCoroutineID The coroutine ID number of the coroutine invoking
   * this method, so it can be resumed after the parser has responded to the
   * request.
   * @return Boolean.TRUE if the CoroutineParser believes more data may be available
   * for further parsing. Boolean.FALSE if parsing ran to completion.
   * Exception if the parser objected for some reason.
   * */
  public Object doMore (boolean parsemore, int appCoroutineID)
  {
    try 
      {
	Object result =
	  fCoroutineManager.co_resume(parsemore?Boolean.TRUE:Boolean.FALSE,
				      appCoroutineID, fParserCoroutineID);
	
	// %REVIEW% Better error reporting needed
	if (result == null)
	  {
	    System.out.println("\nUNEXPECTED: Parser doMore says shut down prematurely.\n");
	  }
	else if (result instanceof Exception) {
	  System.out.println("\nParser threw exception:");
	  ((Exception)result).printStackTrace();
	}
	
	return result;
      }
  
    // SHOULD NEVER OCCUR, since the coroutine number and coroutine manager
    // are those previously established for this CoroutineSAXParser...
    // So I'm just going to return it as a parsing exception, for now.
    catch(NoSuchMethodException e)
      {
	return e;
      }
  }
  
  
  /** doTerminate() is a simple API which tells the coroutine
   * parser to terminate itself.  This is intended to be called from
   * one of our partner coroutines, and serves both to encapsulate the
   * communication protocol and to avoid having to explicitly use the
   * CoroutineParser's coroutine ID number.
   *
   * Returns only after the CoroutineParser has acknowledged the request.
   *
   * @param appCoroutineID The coroutine ID number of the coroutine invoking
   * this method, so it can be resumed after the parser has responded to the
   * request.
   * */
  public void doTerminate(int appCoroutineID)
  {
    try
      {
        Object result =
	  fCoroutineManager.co_resume(null, appCoroutineID, fParserCoroutineID);

	// Debugging; shouldn't arise in normal operation
        if(result!=null)
          System.out.println("\nUNEXPECTED: Parser doTerminate answers "+result);
      }
    catch(java.lang.NoSuchMethodException e)
      {
	// That's OK; if it doesn't exist, we don't need to terminate it
      }
  }

  //================================================================
  /** Simple unit test. Attempt coroutine parsing of document indicated
   * by first argument (as a URI), report progress.
   */
  public static void main(String args[])
  {
    System.out.println("Starting...");

    org.xml.sax.XMLReader theSAXParser=
      new org.apache.xerces.parsers.SAXParser();
    
    CoroutineManager co = new CoroutineManager();
    int appCoroutineID = co.co_joinCoroutineSet(-1);
    if (appCoroutineID == -1)
      {
        System.out.println("ERROR: Couldn't allocate coroutine number.\n");
        return;
      }
    CoroutineSAXParser parser=
      new CoroutineSAXParser(co, appCoroutineID, theSAXParser);
    int parserCoroutineID = parser.getParserCoroutineID();

    // Use a serializer as our sample output
    org.apache.xml.serialize.XMLSerializer trace;
    trace=new org.apache.xml.serialize.XMLSerializer(System.out,null);
    parser.setContentHandler(trace);
    parser.setLexHandler(trace);

    // Tell coroutine to begin parsing, run while parsing is in progress
    for(int arg=0;arg<args.length;++arg)
      {
	InputSource source = new InputSource(args[arg]);
	Object result=null;
	boolean more=true;
	/**    
	  for(result = co.co_resume(source, appCoroutineID, parserCoroutineID);
	  (result instanceof Boolean && ((Boolean)result)==Boolean.TRUE);
	  result = co.co_resume(more, appCoroutineID, parserCoroutineID))
	  **/
	for(result = parser.doParse(source, appCoroutineID);
	    (result instanceof Boolean && ((Boolean)result)==Boolean.TRUE);
	    result = parser.doMore(more, appCoroutineID))
	  {
	    System.out.println("\nSome parsing successful, trying more.\n");
            
	    // Special test: Terminate parsing early.
	    if(arg+1<args.length && "!".equals(args[arg+1]))
	      {
		++arg;
		more=false;
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

    parser.doTerminate(appCoroutineID);
  }
  
} // class CoroutineSAXParser
