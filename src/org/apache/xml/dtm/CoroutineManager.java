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
package org.apache.xml.dtm;

import java.util.*;

/**
 * <meta name="usage" content="internal"/>
 * Support the coroutine design pattern.
 * <p>
 * A coroutine set is a very simple cooperative non-preemptive
 * multitasking model, where the switch from one task to another is
 * performed via an explicit request. Coroutines interact according to
 * the following rules:
 * <ul>
 * <li>One coroutine in the set has control, which it retains until it
 * either exits or resumes another coroutine.</li>
 * <li>A coroutine is activated when it is resumed by some other coroutine
 * for the first time.</li>
 * <li>An active coroutine that gives up control by resuming another in
 * the set retains its context -- including call stack and local variables
 * -- so that if/when it is resumed, it will proceed from the point at which
 * it last gave up control.</li>
 * </ul>
 * Coroutines can be thought of as falling somewhere between pipes and
 * subroutines. Like call/return, there is an explicit flow of control
 * from one coroutine to another. Like pipes, neither coroutine is
 * actually "in charge", and neither must exit in order to transfer
 * control to the other. 
 * <p>
 * One classic application of coroutines is in compilers, where both
 * the parser and the lexer are maintaining complex state
 * information. The parser resumes the lexer to process incoming
 * characters into lexical tokens, and the lexer resumes the parser
 * when it has reached a point at which it has a reliably interpreted
 * set of tokens available for semantic processing. Structuring this
 * as call-and-return would require saving and restoring a
 * considerable amount of state each time. Structuring it as two tasks
 * connected by a queue may involve higher overhead (in systems which
 * can optimize the coroutine metaphor), isn't necessarily as clear in
 * intent, may have trouble handling cases where data flows in both
 * directions, and may not handle some of the more complex cases where
 * more than two coroutines are involved.
 * <p>
 * Most coroutine systems also provide a way to pass data between the
 * source and target of a resume operation; this is sometimes referred
 * to as "yielding" a value.  Others rely on the fact that, since only
 * one member of a coroutine set is running at a time and does not
 * lose control until it chooses to do so, data structures may be
 * directly shared between them with only minimal precautions.
 * <p>
 * "Note: This should not be taken to mean that producer/consumer
 * problems should be always be done with coroutines." Queueing is
 * often a better solution when only two threads of execution are
 * involved and full two-way handshaking is not required. It's a bit
 * difficult to find short pedagogical examples that require
 * coroutines for a clear solution.
 * <p>
 * The fact that only one of a group of coroutines is running at a
 * time, and the control transfer between them is explicit, simplifies
 * their possible interactions, and in some implementations permits
 * them to be implemented more efficiently than general multitasking.
 * In some situations, coroutines can be compiled out entirely;
 * in others, they may only require a few instructions more than a
 * simple function call.
 * <p>
 * This version is built on top of standard Java threading, since
 * that's all we have available right now. It's been encapsulated for
 * code clarity and possible future optimization.
 * <p>
 * (Two possible approaches: wait-notify based and queue-based. Some
 * folks think that a one-item queue is a cleaner solution because it's
 * more abstract -- but since coroutine _is_ an abstraction I'm not really
 * worried about that; folks should be able to switch this code without
 * concern.)
 * <p>
 * %TBD% THIS SHOULD BE AN INTERFACE. Arguably Coroutine itself should be an
 * interface much like Runnable, but I think that can be built on top of this.
 */
public class CoroutineManager
{
  /** %TBD% */
  BitSet activeIDs=new BitSet();
  /** %TBD% */
  static final int m_unreasonableId=1024;

  /** %TBD% */
  Object m_yield=null;
  /** %TBD% */
  int m_nextCoroutine=-1;

  
  /** <p>Each coroutine in the set managed by a single
   * CoroutineManager is identified by a small positive integer. This
   * brings up the question of how to manage those integers to avoid
   * reuse... since if two coroutines use the same ID number, resuming
   * that ID could resume either. I can see arguments for either
   * allowing applications to select their own numbers (they may want
   * to declare mnemonics via manefest constants) or generating
   * numbers on demand.  This routine's intended to support both
   * approaches.</p>
   *
   * <p>%REVIEW% We could use an object as the identifier. Not sure
   * it's a net gain, though it would allow the thread to be its own
   * ID. Ponder.</p>
   *
   * @param coroutineID: If >=0, requests that we reserve this number.
   * If <0, requests that we find, reserve, and return an available ID
   * number.
   *
   * @return If >=0, the ID number to be used by this coroutine. If <0,
   * an error occurred -- the ID requested was already in use, or we
   * couldn't assign one without going over the "unreasonable value" mark
   * */
  public synchronized int co_joinCoroutineSet(int coroutineID)
  {
    if(coroutineID>=0)
      {
	if(coroutineID>=m_unreasonableId || activeIDs.get(coroutineID))
	  return -1;
      }
    else
      {
	// What I want is "Find first clear bit". That doesn't exist.
	// JDK1.2 added "find last set bit", but that doesn't help now.
	coroutineID=0;
	while(coroutineID<m_unreasonableId)
	  {
	    if(activeIDs.get(coroutineID))
	      ++coroutineID;
	    else
	      break;
	  }
	if(coroutineID>=m_unreasonableId)
	  return -1;
      }

    activeIDs.set(coroutineID);
    return coroutineID;
  }

  /** Internal subroutine: Check whether an ID is active. In fact I'll
   * probably inline this test, but...
   */
  private boolean idExists(int coroutineID)
  {
    return (coroutineID>=m_unreasonableId || activeIDs.get(coroutineID));
  }

  /** In the standard coroutine architecture, coroutines are
   * identified by their method names and are launched and run up to
   * their first yield by simply resuming them; its's presumed that
   * this recognizes the not-already-running case and does the right
   * thing. We need a way to achieve that same threadsafe run-up...
   * start the coroutine with a wait or some such.
   *
   * %TBD% whether this makes any sense...
   * */
  Object co_entry_pause(int thisCoroutine)
  {
    while(m_nextCoroutine != thisCoroutine)
      {
	try 
	  {
	    wait();
	  }
	catch(java.lang.InterruptedException e)
	  {
	    // %TBD% -- Declare? Encapsulate? Ignore? Or
	    // dance deasil and widdershins about the instruction cache?
	  }
      }
    
    return m_yield;
  }

  /** Transfer control to another coroutine which has already been started and
   * is waiting on this CoroutineManager. We won't return from this call
   * until that routine has relinquished control.
   *
   * %TBD% What should we do if toCoroutine isn't registered? Exception?
   *
   * @param arg_object A value to be passed to the other coroutine.
   * @param thisCoroutine Integer identifier for this coroutine. This is the
   * ID we watch for to see if we're the ones being resumed.
   * @param toCoroutine. Integer identifier for the coroutine we wish to
   * invoke. 
   * */
  public synchronized Object co_resume(Object arg_object,int thisCoroutine,int toCoroutine)
  {
    // We expect these values to be overwritten during the notify()/wait()
    // periods, as other coroutines in this set get their opportunity to run.
    m_yield=arg_object;
    m_nextCoroutine=toCoroutine;

    notify();
    while(m_nextCoroutine != thisCoroutine)
      {
	try 
	  {
	    wait();
	  }
	catch(java.lang.InterruptedException e)
	  {
	    // %TBD% -- Declare? Encapsulate? Ignore? Or
	    // dance deasil and widdershins about the instruction cache?
	  }
      }
    
    return m_yield;
  }
  
  /** Make the ID available for reuse and terminate this coroutine,
   * transferring control to... %TBD%
   *
   * %TBD% What should we do if toCoroutine isn't registered? Exception?
   *
   * %TBD% who gets control, or do we just release one at random?
   * What does yield get set to? Or does this cause _all_ coroutines
   * in this set to exit -- that actually sounds more useful.
   */
  public synchronized void co_exit(int thisCoroutine)
  {
    activeIDs.clear(thisCoroutine);
    m_nextCoroutine=-1; // %REVIEW% Someone said Exit!    
    notify();
  }

  /** Make the ID available for reuse and terminate this coroutine,
   * transferring control to the specified coroutine.
   *
   * %TBD% What should we do if toCoroutine isn't registered? Exception?
   */
  public synchronized void co_exit_to(Object arg_object,int thisCoroutine,int toCoroutine)
  {
    // We expect these values to be overwritten during the notify()/wait()
    // periods, as other coroutines in this set get their opportunity to run.
    m_yield=arg_object;
    m_nextCoroutine=toCoroutine;

    activeIDs.clear(thisCoroutine);

    notify();
  }
}


