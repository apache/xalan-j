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
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;
import org.xml.sax.ext.LexicalHandler;
import java.io.IOException;
import org.apache.xml.dtm.CoroutineManager;

/** <p>CoroutineSAXParser illustrates how to run a SAX2 parser in a
 * coroutine to achieve incremental parsing. Output from the parser
 * will still be issued via callbacks, which will need to be recieved
 * and acted upon by an appopriate handler. But those callbacks will
 * pass through a counting stage which periodically yields control
 * back to the other coroutines in this set.</p>
 *
 * Usage is something like this:
 * <code>
 *      CoroutineManager co = new CoroutineManager()
 *      int appCoroutine = co.co_joinCoroutineSet(-1);
 *      if (appCoroutine == -1) { [[error handling]] }
 *      CoroutineParser parser = CoroutineSAXParser.createCoroutineparser(co, appCoroutine,theSAXParserBeingWrapped);
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
public class CoroutineSAXParser
implements CoroutineParser, Runnable, ContentHandler, LexicalHandler  {

  //
  // Data
  //

  private CoroutineManager fCoroutineManager = null;
  private int fAppCoroutine = -1;
  private int fParserCoroutine = -1;
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
  public CoroutineSAXParser(CoroutineManager co, int appCoroutine,
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
    fAppCoroutine = appCoroutine;
    fParserCoroutine = co.co_joinCoroutineSet(-1);
    if (fParserCoroutine == -1)
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
  public void setLexicalHandler(LexicalHandler handler)
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
    clientContentHandler.characters(ch,start,length);
    if(--eventcounter<=0)
      {
	co_yield(true);
	eventcounter=frequency;
      }
  }
  public void endDocument() 
       throws org.xml.sax.SAXException
  {
    clientContentHandler.endDocument();
    eventcounter=0;	
    co_yield(false); // Nothing more expected; co_yield up the ghost!
  }
  public void endElement(java.lang.String namespaceURI, java.lang.String localName,
      java.lang.String qName) 
       throws org.xml.sax.SAXException
  {
    clientContentHandler.endElement(namespaceURI,localName,qName);
    if(--eventcounter<=0)
      {
	co_yield(true);
	eventcounter=frequency;
      }
  }
  public void endPrefixMapping(java.lang.String prefix) 
       throws org.xml.sax.SAXException
  {
    clientContentHandler.endPrefixMapping(prefix);
    if(--eventcounter<=0)
      {
	co_yield(true);
	eventcounter=frequency;
      }
  }
  public void ignorableWhitespace(char[] ch, int start, int length) 
       throws org.xml.sax.SAXException
  {
    clientContentHandler.ignorableWhitespace(ch,start,length);
    if(--eventcounter<=0)
      {
	co_yield(true);
	eventcounter=frequency;
      }
  }
  public void processingInstruction(java.lang.String target, java.lang.String data) 
       throws org.xml.sax.SAXException
  {
    clientContentHandler.processingInstruction(target,data);
    if(--eventcounter<=0)
      {
	co_yield(true);
	eventcounter=frequency;
      }
  }
  public void setDocumentLocator(Locator locator) 
  {
    clientContentHandler.setDocumentLocator(locator);
    if(--eventcounter<=0)
      {
	co_yield(true);
	eventcounter=frequency;
      }
  }
  public void skippedEntity(java.lang.String name) 
       throws org.xml.sax.SAXException
  {
    clientContentHandler.skippedEntity(name);
    if(--eventcounter<=0)
      {
	co_yield(true);
	eventcounter=frequency;
      }
  }
  public void startDocument() 
       throws org.xml.sax.SAXException
  {
    clientContentHandler.startDocument();
    if(--eventcounter<=0)
      {
	co_yield(true);
	eventcounter=frequency;
      }
  }
  public void startElement(java.lang.String namespaceURI, java.lang.String localName,
      java.lang.String qName, Attributes atts) 
       throws org.xml.sax.SAXException
  {
    clientContentHandler.startElement(namespaceURI, localName, qName, atts);
    if(--eventcounter<=0)
      {
	co_yield(true);
	eventcounter=frequency;
      }
  }
  public void startPrefixMapping(java.lang.String prefix, java.lang.String uri) 
       throws org.xml.sax.SAXException
  {
    clientContentHandler.startPrefixMapping(prefix,uri);
    if(--eventcounter<=0)
      {
	co_yield(true);
	eventcounter=frequency;
      }
  }

  //
  // LexicalHandler support. Not all SAX2 parsers support these events
  // but we may want to pass them through when they exist...
  //
  // %REVIEW% These do NOT currently affect the eventcounter; I'm asserting
  // that they're rare enough that it makes little or no sense to
  // pause after them. As such, it may make more sense for folks who
  // actually want to use them to register directly with the parser.
  // But I want 'em here to remind us to recheck this assertion!
  //
  public void comment(char[] ch, int start, int length) 
       throws org.xml.sax.SAXException
  {
    clientLexicalHandler.comment(ch,start,length);
  }
  public void endCDATA() 
       throws org.xml.sax.SAXException
  {
    clientLexicalHandler.endCDATA();
  }
  public void endDTD() 
       throws org.xml.sax.SAXException
  {
    clientLexicalHandler.endDTD();
  }
  public void endEntity(java.lang.String name) 
       throws org.xml.sax.SAXException
  {
    clientLexicalHandler.endEntity(name);
  }
  public void startCDATA() 
       throws org.xml.sax.SAXException
  {
    clientLexicalHandler.startCDATA();
  }
  public void startDTD(java.lang.String name, java.lang.String publicId,
      java.lang.String systemId) 
       throws org.xml.sax.SAXException
  {
    clientLexicalHandler. startDTD(name, publicId, systemId);
  }
  public void startEntity(java.lang.String name) 
       throws org.xml.sax.SAXException
  {
    clientLexicalHandler.startEntity(name);
  }

  //
  // coroutine support
  //

  public int getParserCoroutine() {
    return fParserCoroutine;
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
	arg = fCoroutineManager.co_resume(arg, fParserCoroutine, fAppCoroutine);
	
	if (arg == null) {
	  fCoroutineManager.co_exit_to(arg, fParserCoroutine, fAppCoroutine);
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
	fCoroutineManager.co_exit(fParserCoroutine);
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
   *                          co_resume(Boolean.TRUE, ...) on success.
   *                          co_resume(Exception, ...) on error.
   */
  public void run() {
    try 
      {
	for(Object arg=fCoroutineManager.co_entry_pause(fParserCoroutine);
	    true;
	    arg=fCoroutineManager.co_resume(arg, fParserCoroutine, fAppCoroutine))
	  {
	    
	    // Shut down requested.
	    if (arg == null) {
	      fCoroutineManager.co_exit_to(arg, fParserCoroutine, fAppCoroutine);
	      break;
	    }
	    
	    // Start-Parse requested
	    // For the duration of this operation, all coroutine handshaking
	    // will occur in the co_yield method. That's the nice thing about
	    // coroutines; they give us a way to hand off control from the
	    // middle of a synchronous method.
	    if (arg instanceof InputSource) {
	      try {
		xmlreader.parse((InputSource)arg);
		arg=Boolean.TRUE;
	      }
	      catch (UserRequestedStopException e)
		{
		  arg=Boolean.FALSE;
		}
	      catch (UserRequestedShutdownException e)
		{
		  break; 
		}
	      catch (Exception ex) {
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
	fCoroutineManager.co_exit(fParserCoroutine);
      }
  }

  /** Used so co_yield can return control to run for early parse
   * termination.  */
  class UserRequestedStopException extends RuntimeException
  {
  }
  // Instance so we don't have to create a new one each time
  UserRequestedStopException stopException=new UserRequestedStopException();

  /** Used so co_yield can return control to run for coroutine thread
   * termination.  */
  class UserRequestedShutdownException extends RuntimeException
  {
  }
  // Instance so we don't have to create a new one each time
  UserRequestedShutdownException shutdownException=new UserRequestedShutdownException();

} // class CoroutineSAXParser
