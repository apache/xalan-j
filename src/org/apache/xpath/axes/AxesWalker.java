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
package org.apache.xpath.axes;

import java.util.Stack;
import java.util.Vector;

// Xalan imports
import org.apache.xpath.axes.LocPathIterator;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.axes.SubContextList;
import org.apache.xpath.compiler.Compiler;
import org.apache.xpath.compiler.OpCodes;
import org.apache.xpath.Expression;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.patterns.NodeTestFilter;
import org.apache.xpath.patterns.NodeTest;
import org.apache.xpath.XPathContext;
import org.apache.xpath.XPath;

import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.dtm.DTMFilter;
import org.apache.xml.dtm.DTMAxisTraverser;
import org.apache.xml.dtm.Axis;

import org.apache.xml.utils.XMLString;

/**
 * Serves as common interface for axes Walkers, and stores common
 * state variables.
 */
public class AxesWalker extends PredicatedNodeTest
        implements Cloneable
{
  
  /**
   * Construct an AxesWalker using a LocPathIterator.
   *
   * @param locPathIterator non-null reference to the parent iterator.
   */
  public AxesWalker(LocPathIterator locPathIterator, int axis)
  {
    super( locPathIterator );
    m_axis = axis;
  }
  
  public final WalkingIterator wi()
  {
    return (WalkingIterator)m_lpi;
  }

  /**
   * Initialize an AxesWalker during the parse of the XPath expression.
   *
   * @param compiler The Compiler object that has information about this 
   *                 walker in the op map.
   * @param opPos The op code position of this location step.
   * @param stepType  The type of location step.
   *
   * @throws javax.xml.transform.TransformerException
   */
  public void init(Compiler compiler, int opPos, int stepType)
          throws javax.xml.transform.TransformerException
  {

    initPredicateInfo(compiler, opPos);

    // int testType = compiler.getOp(nodeTestOpPos);
  }

  /**
   * Get a cloned AxesWalker.
   *
   * @return A new AxesWalker that can be used without mutating this one.
   *
   * @throws CloneNotSupportedException
   */
  public Object clone() throws CloneNotSupportedException
  {
    // Do not access the location path itterator during this operation!
    
    AxesWalker clone = (AxesWalker) super.clone();

    //clone.setCurrentNode(clone.m_root);

    // clone.m_isFresh = true;

    return clone;
  }
  
  /**
   * Do a deep clone of this walker, including next and previous walkers.
   * If the this AxesWalker is on the clone list, don't clone but 
   * return the already cloned version.
   * 
   * @param cloneOwner non-null reference to the cloned location path 
   *                   iterator to which this clone will be added.
   * @param cloneList non-null vector of sources in odd elements, and the 
   *                  corresponding clones in even vectors.
   * 
   * @return non-null clone, which may be a new clone, or may be a clone 
   *         contained on the cloneList.
   */
  AxesWalker cloneDeep(WalkingIterator cloneOwner, Vector cloneList)
     throws CloneNotSupportedException
  {
    AxesWalker clone = findClone(this, cloneList);
    if(null != clone)
      return clone;
    clone = (AxesWalker)this.clone();
    clone.setLocPathIterator(cloneOwner);
    if(null != cloneList)
    {
      cloneList.addElement(this);
      cloneList.addElement(clone);
    }
    
    if(wi().m_lastUsedWalker == this)
      cloneOwner.m_lastUsedWalker = clone;
      
    if(null != m_nextWalker)
      clone.m_nextWalker = m_nextWalker.cloneDeep(cloneOwner, cloneList);
      
    // If you don't check for the cloneList here, you'll go into an 
    // recursive infinate loop.  
    if(null != cloneList)
    {
      if(null != m_prevWalker)
        clone.m_prevWalker = m_prevWalker.cloneDeep(cloneOwner, cloneList);
    }
    else
    {
      if(null != m_nextWalker)
        clone.m_nextWalker.m_prevWalker = clone;
    }
    return clone;
  }
  
  /**
   * Find a clone that corresponds to the key argument.
   * 
   * @param key The original AxesWalker for which there may be a clone.
   * @param cloneList vector of sources in odd elements, and the 
   *                  corresponding clones in even vectors, may be null.
   * 
   * @return A clone that corresponds to the key, or null if key not found.
   */
  static AxesWalker findClone(AxesWalker key, Vector cloneList)
  {
    if(null != cloneList)
    {
      // First, look for clone on list.
      int n = cloneList.size();
      for (int i = 0; i < n; i+=2) 
      {
        if(key == cloneList.elementAt(i))
          return (AxesWalker)cloneList.elementAt(i+1);
      }
    }
    return null;    
  }
  
  //=============== TreeWalker Implementation ===============

  /**
   * The root node of the TreeWalker, as specified in setRoot(int root).
   * Note that this may actually be below the current node.
   *
   * @return The context node of the step.
   */
  public int getRoot()
  {
    return m_root;
  }

  /**
   * Set the root node of the TreeWalker.
   * (Not part of the DOM2 TreeWalker interface).
   *
   * @param root The context node of this step.
   */
  public void setRoot(int root)
  {
    // %OPT% Get this directly from the lpi.
    m_dtm = wi().getXPathContext().getDTM(root);
    m_traverser = m_dtm.getAxisTraverser(m_axis);
    m_isFresh = true;
    m_foundLast = false;
    m_root = root;
    m_currentNode = root;

    if (DTM.NULL == root)
    {
      throw new RuntimeException(
        "\n !!!! Error! Setting the root of a walker to null!!!");
    }

    resetProximityPositions();
  }

  /**
   * The node at which the TreeWalker is currently positioned.
   * <br> The value must not be null. Alterations to the DOM tree may cause
   * the current node to no longer be accepted by the TreeWalker's
   * associated filter. currentNode may also be explicitly set to any node,
   * whether or not it is within the subtree specified by the root node or
   * would be accepted by the filter and whatToShow flags. Further
   * traversal occurs relative to currentNode even if it is not part of the
   * current view by applying the filters in the requested direction (not
   * changing currentNode where no traversal is possible).
   *
   * @return The node at which the TreeWalker is currently positioned, only null 
   * if setRoot has not yet been called.
   */
  public final int getCurrentNode()
  {
    return m_currentNode;
  }

  /**
   * Set the next walker in the location step chain.
   *
   *
   * @param walker Reference to AxesWalker derivative, or may be null.
   */
  public void setNextWalker(AxesWalker walker)
  {
    m_nextWalker = walker;
  }

  /**
   * Get the next walker in the location step chain.
   *
   *
   * @return Reference to AxesWalker derivative, or null.
   */
  public AxesWalker getNextWalker()
  {
    return m_nextWalker;
  }

  /**
   * Set or clear the previous walker reference in the location step chain.
   *
   *
   * @param walker Reference to previous walker reference in the location 
   *               step chain, or null.
   */
  public void setPrevWalker(AxesWalker walker)
  {
    m_prevWalker = walker;
  }

  /**
   * Get the previous walker reference in the location step chain.
   *
   *
   * @return Reference to previous walker reference in the location 
   *               step chain, or null.
   */
  public AxesWalker getPrevWalker()
  {
    return m_prevWalker;
  }

  /**
   * This is simply a way to bottle-neck the return of the next node, for 
   * diagnostic purposes.
   *
   * @param n Node to return, or null.
   *
   * @return The argument.
   */
  private int returnNextNode(int n)
  {

    return n;
  }

  /**
   * Get the next node in document order on the axes.
   *
   * @return the next node in document order on the axes, or null.
   */
  protected int getNextNode()
  {
    if (m_foundLast)
      return DTM.NULL;

    if (m_isFresh)
    {
      m_currentNode = m_traverser.first(m_root);
      m_isFresh = false;
    }
    // I shouldn't have to do this the check for current node, I think.
    // numbering\numbering24.xsl fails if I don't do this.  I think 
    // it occurs as the walkers are backing up. -sb
    else if(DTM.NULL != m_currentNode) 
    {
      m_currentNode = m_traverser.next(m_root, m_currentNode);
    }

    if (DTM.NULL == m_currentNode)
      this.m_foundLast = true;

    return m_currentNode;
  }

  /**
   *  Moves the <code>TreeWalker</code> to the next visible node in document
   * order relative to the current node, and returns the new node. If the
   * current node has no next node,  or if the search for nextNode attempts
   * to step upward from the TreeWalker's root node, returns
   * <code>null</code> , and retains the current node.
   * @return  The new node, or <code>null</code> if the current node has no
   *   next node  in the TreeWalker's logical view.
   */
  public int nextNode()
  {
    int nextNode = DTM.NULL;
    AxesWalker walker = wi().getLastUsedWalker();

    while (true)
    {
      if (null == walker)
        break;

      nextNode = walker.getNextNode();

      if (DTM.NULL == nextNode)
      {

        walker = walker.m_prevWalker;
      }
      else
      {
        if (walker.acceptNode(nextNode) != DTMIterator.FILTER_ACCEPT)
        {
          continue;
        }

        if (null == walker.m_nextWalker)
        {
          wi().setLastUsedWalker(walker);

          // return walker.returnNextNode(nextNode);
          break;
        }
        else
        {
          AxesWalker prev = walker;

          walker = walker.m_nextWalker;

          walker.setRoot(nextNode);

          walker.m_prevWalker = prev;

          continue;
        }
      }  // if(null != nextNode)
    }  // while(null != walker)

    return nextNode;
  }

  //============= End TreeWalker Implementation =============

  /**
   * Get the index of the last node that can be itterated to.
   *
   *
   * @param xctxt XPath runtime context.
   *
   * @return the index of the last node that can be itterated to.
   */
  public int getLastPos(XPathContext xctxt)
  {

    int pos = getProximityPosition();
    
    AxesWalker walker;

    try
    {
      walker = (AxesWalker) clone();
    }
    catch (CloneNotSupportedException cnse)
    {
      return -1;
    }

    walker.setPredicateCount(walker.getPredicateCount() - 1);
    walker.setNextWalker(null);
    walker.setPrevWalker(null);

    WalkingIterator lpi = wi();
    AxesWalker savedWalker = lpi.getLastUsedWalker();

    try
    {
      lpi.setLastUsedWalker(walker);

      int next;

      while (DTM.NULL != (next = walker.nextNode()))
      {
        pos++;
      }

      // TODO: Should probably save this in the iterator.
    }
    finally
    {
      lpi.setLastUsedWalker(savedWalker);
    }

    // System.out.println("pos: "+pos);
    return pos;
  }
  
  //============= State Data =============
  
  /**
   * The DTM for the root.  This can not be used, or must be changed, 
   * for the filter walker, or any walker that can have nodes 
   * from multiple documents.
   * Never, ever, access this value without going through getDTM(int node).
   */
  private DTM m_dtm;
  
  /**
   * Set the DTM for this walker.
   * 
   * @param dtm Non-null reference to a DTM.
   */
  public void setDefaultDTM(DTM dtm)
  {
    m_dtm = dtm;
  }
  
  /**
   * Get the DTM for this walker.
   * 
   * @return Non-null reference to a DTM.
   */
  public DTM getDTM(int node)
  {
    //
    return wi().getXPathContext().getDTM(node);
  }
  
  /**
   * Returns true if all the nodes in the iteration well be returned in document 
   * order.
   * Warning: This can only be called after setRoot has been called!
   * 
   * @return true as a default.
   */
  public boolean isDocOrdered()
  {
    return true;
  }
  
  /**
   * Returns the axis being iterated, if it is known.
   * 
   * @return Axis.CHILD, etc., or -1 if the axis is not known or is of multiple 
   * types.
   */
  public int getAxis()
  {
    return m_axis;
  }


  /**
   *  The root node of the TreeWalker, as specified when it was created.
   */
  transient int m_root = DTM.NULL;

  /**
   *  The node at which the TreeWalker is currently positioned.
   */
  private transient int m_currentNode = DTM.NULL;
  
  /** True if an itteration has not begun.  */
  transient boolean m_isFresh;

  /** The next walker in the location step chain.
   *  @serial  */
  protected AxesWalker m_nextWalker;
  
  /** The previous walker in the location step chain, or null.
   *  @serial   */
  AxesWalker m_prevWalker;
  
  /** The traversal axis from where the nodes will be filtered. */
  protected int m_axis = -1;

  /** The DTM inner traversal class, that corresponds to the super axis. */
  protected DTMAxisTraverser m_traverser;   
}
