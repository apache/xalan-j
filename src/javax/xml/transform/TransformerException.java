/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
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
 * originally based on software copyright (c) 1999, Lotus
 * Development Corporation., http://www.lotus.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package javax.xml.transform;

import java.lang.reflect.Method;
import java.lang.IllegalAccessException;
import java.lang.reflect.InvocationTargetException;

import javax.xml.transform.SourceLocator;

/**
 * This class specifies an exceptional condition that occured 
 * during the transformation process.
 */
public class TransformerException extends Exception
{

  /** Field locator specifies where the error occured */
  SourceLocator locator;

  /**
   * Method getLocator retrieves and instance of a SourceLocator
   * object that specifies where an error occured.
   *
   * @return A SourceLocator object, or null if none was specified.
   */
  public SourceLocator getLocator()
  {
    return locator;
  }

  /** Field containedException specifies a wrapped exception.  May be null. */
  Exception containedException;

  /**
   * This method retrieves an exception that this exception wraps.
   *
   * @return An Exception object, or null.
   * @see #getCause
   */
  public Exception getException()
  {
    return containedException;
  }
  
  /**
   * Returns the cause of this throwable or <code>null</code> if the
   * cause is nonexistent or unknown.  (The cause is the throwable that
   * caused this throwable to get thrown.)
   */
  public Throwable getCause() {
    return (containedException==this ? null : containedException);
  }

  /**
   * Initializes the <i>cause</i> of this throwable to the specified value.
   * (The cause is the throwable that caused this throwable to get thrown.) 
   *
   * <p>This method can be called at most once.  It is generally called from 
   * within the constructor, or immediately after creating the
   * throwable.  If this throwable was created
   * with {@link #TransformerException(Exception)} or
   * {@link #TransformerException(String,Exception)}, this method cannot be called
   * even once.
   *
   * @param  cause the cause (which is saved for later retrieval by the
   *         {@link #getCause()} method).  (A <tt>null</tt> value is
   *         permitted, and indicates that the cause is nonexistent or
   *         unknown.)
   * @return  a reference to this <code>Throwable</code> instance.
   * @throws IllegalArgumentException if <code>cause</code> is this
   *         throwable.  (A throwable cannot
   *         be its own cause.)
   * @throws IllegalStateException if this throwable was
   *         created with {@link #TransformerException(Exception)} or
   *         {@link #TransformerException(String,Exception)}, or this method has already
   *         been called on this throwable.
   */
  public synchronized Throwable initCause(Throwable cause) {
    if (this.containedException == null)
      throw new IllegalStateException("Can't overwrite cause");
    if (containedException == this)
      throw new IllegalArgumentException("Self-causation not permitted");
    this.containedException = containedException;
    return this;
  }

  /**
   * Create a new TransformerException.
   *
   * @param message The error or warning message.
   */
  public TransformerException(String message)
  {

    super(message);

    this.containedException = null;
    this.locator = null;
  }

  /**
   * Create a new TransformerException wrapping an existing exception.
   *
   * @param e The exception to be wrapped.
   */
  public TransformerException(Exception e)
  {

    super("TRaX Transform Exception");

    this.containedException = e;
    this.locator = null;
  }

  /**
   * Wrap an existing exception in a TransformerException.
   *
   * <p>This is used for throwing processor exceptions before
   * the processing has started.</p>
   *
   * @param message The error or warning message, or null to
   *                use the message from the embedded exception.
   * @param e Any exception
   */
  public TransformerException(String message, Exception e)
  {
    super ((message == null || message.length()== 0)? 
           "TRaX Transform Exception" : message);
      

    this.containedException = e;
    this.locator = null;
  }

  /**
   * Create a new TransformerException from a message and a Locator.
   *
   * <p>This constructor is especially useful when an application is
   * creating its own exception from within a DocumentHandler
   * callback.</p>
   *
   * @param message The error or warning message.
   * @param locator The locator object for the error or warning.
   */
  public TransformerException(String message, SourceLocator locator)
  {

    super(message);

    this.containedException = null;
    this.locator = locator;
  }

  /**
   * Wrap an existing exception in a TransformerException.
   *
   * @param message The error or warning message, or null to
   *                use the message from the embedded exception.
   * @param locator The locator object for the error or warning.
   * @param e Any exception
   */
  public TransformerException(String message, SourceLocator locator,
                              Exception e)
  {

    super(message);

    this.containedException = e;
    this.locator = locator;
  }
        
  /**
   * Get the error message with location information 
   * appended.
   */
  public String getMessageAndLocation()
  {
    StringBuffer sbuffer = new StringBuffer();
    String message = super.getMessage();    
    if(null != message)
    {
      sbuffer.append(message);
    }
    if(null != locator)
    {
      String systemID = locator.getSystemId();
      int line = locator.getLineNumber();
      int column = locator.getColumnNumber();

      if(null != systemID)
      {
        sbuffer.append("; SystemID: ");
        sbuffer.append(systemID);
      }
      if(0 != line)
      {
        sbuffer.append("; Line#: ");
        sbuffer.append(line);
      }
      if(0 != column)
      {
        sbuffer.append("; Column#: ");
        sbuffer.append(column);
      }
    }
    return sbuffer.toString();
  }
  
  /**
   * Get the location information as a string.
   * 
   * @return A string with location info, or null 
   * if there is no location information.
   */
  public String getLocationAsString()
  {
    if(null != locator)
    {
      StringBuffer sbuffer = new StringBuffer();
      String systemID = locator.getSystemId();
      int line = locator.getLineNumber();
      int column = locator.getColumnNumber();

      if(null != systemID)
      {
        sbuffer.append("; SystemID: ");
        sbuffer.append(systemID);
      }
      if(0 != line)
      {
        sbuffer.append("; Line#: ");
        sbuffer.append(line);
      }
      if(0 != column)
      {
        sbuffer.append("; Column#: ");
        sbuffer.append(column);
      }
      return sbuffer.toString();
    }
    else return null;
  }
  
  /**
   * Print the the trace of methods from where the error 
   * originated.  This will trace all nested exception 
   * objects, as well as this object.
   */
  public void printStackTrace() 
  {
    printStackTrace(new java.io.PrintWriter(System.err, true));
  }
  
  /**
   * Print the the trace of methods from where the error 
   * originated.  This will trace all nested exception 
   * objects, as well as this object.
   * @param s The stream where the dump will be sent to.
   */
  public void printStackTrace(java.io.PrintStream s) 
  {
    printStackTrace(new java.io.PrintWriter(s));
  }

  /**
   * Print the the trace of methods from where the error 
   * originated.  This will trace all nested exception 
   * objects, as well as this object.
   * @param s The writer where the dump will be sent to.
   */
  public void printStackTrace(java.io.PrintWriter s) 
  {
    if(s == null)
      s = new java.io.PrintWriter(System.err, true);
    try
    {
      String locInfo = getLocationAsString();
      if(null != locInfo)
        s.println(locInfo);
      super.printStackTrace(s);
    }
    catch(Exception e)
    {
    }
    Exception exception = getException();
    
    for(int i = 0; (i < 10) && (null != exception); i++)
    {
      s.println("---------");
      try
      {
        if(exception instanceof TransformerException)
        {
          String locInfo = ((TransformerException)exception).getLocationAsString();
          if(null != locInfo)
            s.println(locInfo);
        }
        exception.printStackTrace(s);
      }
      catch(Exception e)
      {
        s.println("Could not print stack trace...");
      }
      try
      {
        Method meth = ((Object)exception).getClass().getMethod("getException", null);
        if(null != meth)
        {
          Exception prev = exception;
          exception = (Exception)meth.invoke(exception, null);
          if(prev == exception)
            break;
        }
        else
        {
          exception = null;
        }
      }
      catch(InvocationTargetException ite)
      {
        exception = null;
      }
      catch(IllegalAccessException iae)
      {
        exception = null;
      }
      catch(NoSuchMethodException nsme)
      {
        exception = null;
      }
    }
  }
}
