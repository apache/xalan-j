/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
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
package org.apache.xml.dtm.ref.sax2dtm;

import org.apache.xml.dtm.*;
import org.apache.xml.dtm.ref.*;
import org.apache.xml.utils.XMLString;
import org.apache.xml.utils.XMLStringDefault;
import org.apache.xml.utils.XMLStringFactory;
import org.apache.xml.res.XMLMessages;
import org.apache.xml.res.XMLErrorResources;

import javax.xml.transform.Source;
import java.util.Vector;
import org.xml.sax.*;

/**
 * SAX2DTM2 is an optimized version of SAX2DTM which is used in non-incremental case.
 * It is used as the super class of the XSLTC SAXImpl. Many of the interfaces in SAX2DTM
 * and DTMDefaultBase are overridden in SAX2DTM2 in order to allow fast, efficient 
 * access to the DTM model. Some nested iterators in DTMDefaultBaseIterators
 * are also overridden in SAX2DTM2 for performance reasons.
 *
 * %MK% The code in this class is critical to the XSLTC_DTM performance. Be very careful
 * when making changes here!
 */
public class SAX2DTM2 extends SAX2DTM
{

  /****************************************************************
   *       Optimized version of the nested iterators
   ****************************************************************/
   
  /**
   * Iterator that returns all immediate children of a given node
   */
  public final class ChildrenIterator extends InternalAxisIteratorBase
  {

    /**
     * Setting start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.
     *
     * If the iterator is not restartable, this has no effect.
     * %REVIEW% Should it return/throw something in that case,
     * or set current node to END, to indicate request-not-honored?
     *
     * @param node Sets the root of the iteration.
     *
     * @return A DTMAxisIterator set to the start of the iteration.
     */
    public DTMAxisIterator setStartNode(int node)
    {
//%HZ%: Added reference to DTMDefaultBase.ROOTNODE back in, temporarily
      if (node == DTMDefaultBase.ROOTNODE)
        node = getDocument();
      if (_isRestartable)
      {
        _startNode = node;
        _currentNode = (node == DTM.NULL) ? DTM.NULL
                                          : _firstch2(makeNodeIdentity(node));

        return resetPosition();
      }

      return this;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END if no more
     * are available.
     */
    public int next()
    {
      if (_currentNode != NULL) {
        int node = _currentNode;
        _currentNode = _nextsib2(node);
        return returnNode(makeNodeHandle(node));
      }

      return END;
    }
  }  // end of ChildrenIterator

  /**
   * Iterator that returns the parent of a given node. Note that
   * this delivers only a single node; if you want all the ancestors,
   * see AncestorIterator.
   */
  public final class ParentIterator extends InternalAxisIteratorBase
  {

    /** The extended type ID that was requested. */
    private int _nodeType = DTM.NULL;

    /**
     * Set start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.
     *
     * @param node Sets the root of the iteration.
     *
     * @return A DTMAxisIterator set to the start of the iteration.
     */
    public DTMAxisIterator setStartNode(int node)
    {
//%HZ%: Added reference to DTMDefaultBase.ROOTNODE back in, temporarily
      if (node == DTMDefaultBase.ROOTNODE)
        node = getDocument();
      if (_isRestartable)
      {
        _startNode = node;
        
        if (node != DTM.NULL)
          _currentNode = _parent2(makeNodeIdentity(node));
        else
          _currentNode = DTM.NULL;
        
        return resetPosition();
      }

      return this;
    }

    /**
     * Set the node type of the parent that we're looking for.
     * Note that this does _not_ mean "find the nearest ancestor of
     * this type", but "yield the parent if it is of this type".
     *
     *
     * @param type extended type ID.
     *
     * @return ParentIterator configured with the type filter set.
     */
    public DTMAxisIterator setNodeType(final int type)
    {

      _nodeType = type;

      return this;
    }

    /**
     * Get the next node in the iteration. In this case, we return
     * only the immediate parent, _if_ it matches the requested nodeType.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {
      int result = _currentNode;
      if (result == END)
        return DTM.NULL;

      // %OPT% The most common case is handled first.
      if (_nodeType == NULL) {
        _currentNode = END;
        return returnNode(makeNodeHandle(result));
      }
      else if (_nodeType >= DTM.NTYPES) {
        if (_nodeType == _exptype2(result)) {
          _currentNode = END;
	  return returnNode(makeNodeHandle(result));
        }
      } 
      else {
        if (_nodeType == _type2(result)) {
	  _currentNode = END;
	  return returnNode(makeNodeHandle(result));          
        }
      }
      
      return DTM.NULL;      
    }
  }  // end of ParentIterator

  /**
   * Iterator that returns children of a given type for a given node.
   * The functionality chould be achieved by putting a filter on top
   * of a basic child iterator, but a specialised iterator is used
   * for efficiency (both speed and size of translet).
   */
  public final class TypedChildrenIterator extends InternalAxisIteratorBase
  {

    /** The extended type ID that was requested. */
    private final int _nodeType;

    /**
     * Constructor TypedChildrenIterator
     *
     *
     * @param nodeType The extended type ID being requested.
     */
    public TypedChildrenIterator(int nodeType)
    {
      _nodeType = nodeType;
    }

    /**
     * Set start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.
     *
     * @param node Sets the root of the iteration.
     *
     * @return A DTMAxisIterator set to the start of the iteration.
     */
    public DTMAxisIterator setStartNode(int node)
    {
//%HZ%: Added reference to DTMDefaultBase.ROOTNODE back in, temporarily
      if (node == DTMDefaultBase.ROOTNODE)
        node = getDocument();
      if (_isRestartable)
      {
        _startNode = node;
        _currentNode = (node == DTM.NULL)
                                   ? DTM.NULL
                                   : _firstch2(makeNodeIdentity(_startNode));

        return resetPosition();
      }

      return this;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {
      int node = _currentNode;
      if (node == DTM.NULL)
        return DTM.NULL;

      final int nodeType = _nodeType;

      if (nodeType != DTM.ELEMENT_NODE) {
        /*
        while (node != DTM.NULL && _exptype2(node) != nodeType) {
          node = _nextsib2(node);
        }
        */
        node = getTypedSelfAndFollowingSibling(node, nodeType);
      }
      // %OPT% If the nodeType is element (matching child::*), we only
      // need to compare the expType with DTM.NTYPES. A child node of 
      // an element can be either an element, text, comment or
      // processing instruction node. Only element node has an extended
      // type greater than or equal to DTM.NTYPES.
      else {
      	int eType;
      	while (node != DTM.NULL) {
      	  eType = _exptype2(node);
      	  if (eType >= DTM.NTYPES)
      	    break;
      	  else
      	    node = _nextsib2(node);
      	}
      }

      if (node == DTM.NULL) {
        _currentNode = DTM.NULL;
        return DTM.NULL;
      } else {
        _currentNode = _nextsib2(node);
        return returnNode(makeNodeHandle(node));
      }

    }
  }  // end of TypedChildrenIterator

  /**
   * Iterator that returns the namespace nodes as defined by the XPath data model 
   * for a given node, filtered by extended type ID.
   */
  public class TypedRootIterator extends RootIterator
  {

    /** The extended type ID that was requested. */
    private final int _nodeType;

    /**
     * Constructor TypedRootIterator
     *
     * @param nodeType The extended type ID being requested.
     */
    public TypedRootIterator(int nodeType)
    {
      super();
      _nodeType = nodeType;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {
      if(_startNode == _currentNode)
        return NULL;

      final int node = _startNode;
      int expType = _exptype2(makeNodeIdentity(node));

      _currentNode = node;

      if (_nodeType >= DTM.NTYPES) {
        if (_nodeType == expType) {
          return returnNode(node);
        }
      } 
      else {
        if (expType < DTM.NTYPES) {
          if (expType == _nodeType) {
            return returnNode(node);
          }
        } 
        else {
          if (m_extendedTypes[expType].getNodeType() == _nodeType) {
            return returnNode(node);
          }
        }
      }

      return NULL;
    }
  }  // end of TypedRootIterator

  /**
   * Iterator that returns all siblings of a given node.
   */
  public class FollowingSiblingIterator extends InternalAxisIteratorBase
  {

    /**
     * Set start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.
     *
     * @param node Sets the root of the iteration.
     *
     * @return A DTMAxisIterator set to the start of the iteration.
     */
    public DTMAxisIterator setStartNode(int node)
    {
//%HZ%: Added reference to DTMDefaultBase.ROOTNODE back in, temporarily
      if (node == DTMDefaultBase.ROOTNODE)
        node = getDocument();
      if (_isRestartable)
      {
        _startNode = node;
        _currentNode = makeNodeIdentity(node);

        return resetPosition();
      }

      return this;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {
      _currentNode = (_currentNode == DTM.NULL) ? DTM.NULL
                                                : _nextsib2(_currentNode);
      return returnNode(makeNodeHandle(_currentNode));
    }
  }  // end of FollowingSiblingIterator

  /**
   * Iterator that returns all following siblings of a given node.
   */
  public final class TypedFollowingSiblingIterator
          extends FollowingSiblingIterator
  {

    /** The extended type ID that was requested. */
    private final int _nodeType;

    /**
     * Constructor TypedFollowingSiblingIterator
     *
     *
     * @param type The extended type ID being requested.
     */
    public TypedFollowingSiblingIterator(int type)
    {
      _nodeType = type;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {
      if (_currentNode == DTM.NULL) {
        return DTM.NULL;
      }

      int node = _currentNode;
      final int nodeType = _nodeType;

      if (nodeType != DTM.ELEMENT_NODE) {
        node = getTypedFollowingSibling(node, nodeType);
      }
      else {
        while ((node = _nextsib2(node)) != DTM.NULL && _exptype2(node) < DTM.NTYPES) {}
      }
      
      _currentNode = node;

      return (node == DTM.NULL)
                      ? DTM.NULL
                      : returnNode(makeNodeHandle(node));
    }
  }  // end of TypedFollowingSiblingIterator

  /**
   * Iterator that returns attribute nodes (of what nodes?)
   */
  public final class AttributeIterator extends InternalAxisIteratorBase
  {

    // assumes caller will pass element nodes

    /**
     * Set start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.
     *
     * @param node Sets the root of the iteration.
     *
     * @return A DTMAxisIterator set to the start of the iteration.
     */
    public DTMAxisIterator setStartNode(int node)
    {
//%HZ%: Added reference to DTMDefaultBase.ROOTNODE back in, temporarily
      if (node == DTMDefaultBase.ROOTNODE)
        node = getDocument();
      if (_isRestartable)
      {
        _startNode = node;
        _currentNode = getFirstAttributeIdentity(makeNodeIdentity(node));

        return resetPosition();
      }

      return this;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {

      final int node = _currentNode;

      if (node != NULL) {
        _currentNode = getNextAttributeIdentity(node);
        return returnNode(makeNodeHandle(node));
      }

      return NULL;
    }
  }  // end of AttributeIterator

  /**
   * Iterator that returns attribute nodes of a given type
   */
  public final class TypedAttributeIterator extends InternalAxisIteratorBase
  {

    /** The extended type ID that was requested. */
    private final int _nodeType;

    /**
     * Constructor TypedAttributeIterator
     *
     *
     * @param nodeType The extended type ID that is requested.
     */
    public TypedAttributeIterator(int nodeType)
    {
      _nodeType = nodeType;
    }

    // assumes caller will pass element nodes

    /**
     * Set start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.
     *
     * @param node Sets the root of the iteration.
     *
     * @return A DTMAxisIterator set to the start of the iteration.
     */
    public DTMAxisIterator setStartNode(int node)
    {
      if (_isRestartable)
      {
        _startNode = node;

        _currentNode = getTypedAttribute(node, _nodeType);

        return resetPosition();
      }

      return this;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {

      final int node = _currentNode;

      // singleton iterator, since there can only be one attribute of 
      // a given type.
      _currentNode = NULL;

      return returnNode(node);
    }
  }  // end of TypedAttributeIterator

  /**
   * Iterator that returns preceding siblings of a given node
   */
  public class PrecedingSiblingIterator extends InternalAxisIteratorBase
  {

    /**
     * The node identity of _startNode for this iterator
     */
    protected int _startNodeID;

    /**
     * True if this iterator has a reversed axis.
     *
     * @return true.
     */
    public boolean isReverse()
    {
      return true;
    }

    /**
     * Set start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.
     *
     * @param node Sets the root of the iteration.
     *
     * @return A DTMAxisIterator set to the start of the iteration.
     */
    public DTMAxisIterator setStartNode(int node)
    {
//%HZ%: Added reference to DTMDefaultBase.ROOTNODE back in, temporarily
      if (node == DTMDefaultBase.ROOTNODE)
        node = getDocument();
      if (_isRestartable)
      {
        _startNode = node;
        node = _startNodeID = makeNodeIdentity(node);

        if(node == NULL)
        {
          _currentNode = node;
          return resetPosition();
        }

        int type = _type2(node);
        if(ExpandedNameTable.ATTRIBUTE == type 
           || ExpandedNameTable.NAMESPACE == type )
        {
          _currentNode = node;
        }
        else
        {
          // Be careful to handle the Document node properly
          _currentNode = _parent2(node);
          if(NULL!=_currentNode)	
            _currentNode = _firstch2(_currentNode);
          else
            _currentNode = node;
        }

        return resetPosition();
      }

      return this;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {

      if (_currentNode == _startNodeID || _currentNode == DTM.NULL)
      {
        return NULL;
      }
      else
      {
        final int node = _currentNode;
        _currentNode = _nextsib2(node);

        return returnNode(makeNodeHandle(node));
      }
    }
  }  // end of PrecedingSiblingIterator

  /**
   * Iterator that returns preceding siblings of a given type for
   * a given node
   */
  public final class TypedPrecedingSiblingIterator
          extends PrecedingSiblingIterator
  {

    /** The extended type ID that was requested. */
    private final int _nodeType;

    /**
     * Constructor TypedPrecedingSiblingIterator
     *
     *
     * @param type The extended type ID being requested.
     */
    public TypedPrecedingSiblingIterator(int type)
    {
      _nodeType = type;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {
      int node = _currentNode;
      int expType;

      int nodeType = _nodeType;
      int startID = _startNodeID;

      if (nodeType >= DTM.NTYPES) {
        while (node != NULL && node != startID && _exptype2(node) != nodeType) {
          node = _nextsib2(node);
        }
      } else {
        while (node != NULL && node != startID) {
          expType = _exptype2(node);
          if (expType < DTM.NTYPES) {
            if (expType == nodeType) {
              break;
            }
          } else {
            if (m_extendedTypes[expType].getNodeType() == nodeType) {
              break;
            }
          }
          node = _nextsib2(node);
        }
      }

      if (node == DTM.NULL || node == _startNodeID) {
        _currentNode = NULL;
        return NULL;
      } else {
        _currentNode = _nextsib2(node);
        return returnNode(makeNodeHandle(node));
      }
    }
  }  // end of TypedPrecedingSiblingIterator

  /**
   * Iterator that returns preceding nodes of a given node.
   * This includes the node set {root+1, start-1}, but excludes
   * all ancestors, attributes, and namespace nodes.
   */
  public class PrecedingIterator extends InternalAxisIteratorBase
  {

    /** The max ancestors, but it can grow... */
    private final int _maxAncestors = 8;

    /**
     * The stack of start node + ancestors up to the root of the tree,
     *  which we must avoid.
     */
    protected int[] _stack = new int[_maxAncestors];

    /** (not sure yet... -sb) */
    protected int _sp, _oldsp;

    protected int _markedsp, _markedNode, _markedDescendant;

    /* _currentNode precedes candidates.  This is the identity, not the handle! */

    /**
     * True if this iterator has a reversed axis.
     *
     * @return true since this iterator is a reversed axis.
     */
    public boolean isReverse()
    {
      return true;
    }

    /**
     * Returns a deep copy of this iterator.   The cloned iterator is not reset.
     *
     * @return a deep copy of this iterator.
     */
    public DTMAxisIterator cloneIterator()
    {
      _isRestartable = false;

      try
      {
        final PrecedingIterator clone = (PrecedingIterator) super.clone();
        final int[] stackCopy = new int[_stack.length];
        System.arraycopy(_stack, 0, stackCopy, 0, _stack.length);

        clone._stack = stackCopy;

        // return clone.reset();
        return clone;
      }
      catch (CloneNotSupportedException e)
      {
        throw new DTMException(XMLMessages.createXMLMessage(XMLErrorResources.ER_ITERATOR_CLONE_NOT_SUPPORTED, null)); //"Iterator clone not supported.");
      }
    }

    /**
     * Set start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.
     *
     * @param node Sets the root of the iteration.
     *
     * @return A DTMAxisIterator set to the start of the iteration.
     */
    public DTMAxisIterator setStartNode(int node)
    {
//%HZ%: Added reference to DTMDefaultBase.ROOTNODE back in, temporarily
      if (node == DTMDefaultBase.ROOTNODE)
        node = getDocument();
      if (_isRestartable)
      {
        node = makeNodeIdentity(node);

        // iterator is not a clone
        int parent, index;

       if (_type2(node) == DTM.ATTRIBUTE_NODE)
         node = _parent2(node);

        _startNode = node;
        _stack[index = 0] = node;
        
       	parent=node;
	while ((parent = _parent2(parent)) != NULL)
	{
	  if (++index == _stack.length)
	  {
	    final int[] stack = new int[index*2];
	    System.arraycopy(_stack, 0, stack, 0, index);
	    _stack = stack;
	  }
	  _stack[index] = parent;
        }
        
        if(index>0)
	  --index; // Pop actual root node (if not start) back off the stack

        _currentNode=_stack[index]; // Last parent before root node

        _oldsp = _sp = index;

        return resetPosition();
      }

      return this;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {
    	// Bugzilla 8324: We were forgetting to skip Attrs and NS nodes.
    	// Also recoded the loop controls for clarity and to flatten out
    	// the tail-recursion.
   	for(++_currentNode; _sp>=0; ++_currentNode)
   	{
   	  if(_currentNode < _stack[_sp])
   	  {
   	    int type = _type2(_currentNode);
   	    if(type != ATTRIBUTE_NODE && type != NAMESPACE_NODE)
   	      return returnNode(makeNodeHandle(_currentNode));
   	  }
   	  else
   	    --_sp;
   	}
   	return NULL;
    }

    // redefine DTMAxisIteratorBase's reset

    /**
     * Resets the iterator to the last start node.
     *
     * @return A DTMAxisIterator, which may or may not be the same as this
     *         iterator.
     */
    public DTMAxisIterator reset()
    {

      _sp = _oldsp;

      return resetPosition();
    }

    public void setMark() {
        _markedsp = _sp;
        _markedNode = _currentNode;
        _markedDescendant = _stack[0];
    }

    public void gotoMark() {
        _sp = _markedsp;
        _currentNode = _markedNode;
    }
  }  // end of PrecedingIterator

  /**
   * Iterator that returns preceding nodes of agiven type for a
   * given node. This includes the node set {root+1, start-1}, but
   * excludes all ancestors.
   */
  public final class TypedPrecedingIterator extends PrecedingIterator
  {

    /** The extended type ID that was requested. */
    private final int _nodeType;

    /**
     * Constructor TypedPrecedingIterator
     *
     *
     * @param type The extended type ID being requested.
     */
    public TypedPrecedingIterator(int type)
    {
      _nodeType = type;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {
      int node = _currentNode;
      final int nodeType = _nodeType;

      if (nodeType >= DTM.NTYPES) {
        while (true) {
          node++;

          if (_sp < 0) {
            node = NULL;
            break;
          } 
          else if (node >= _stack[_sp]) {
            if (--_sp < 0) {
              node = NULL;
              break;
            }
          } 
          else if (_exptype2(node) == nodeType) {
            break;
          }
        }
      }
      else {
        int expType;

        while (true) {
          node++;

          if (_sp < 0) {
            node = NULL;
            break;
          }
          else if (node >= _stack[_sp]) {
            if (--_sp < 0) {
              node = NULL;
              break;
            }
          }
          else {
            expType = _exptype2(node);
            if (expType < DTM.NTYPES) {
              if (expType == nodeType) {
                break;
              }
            }
            else {
              if (m_extendedTypes[expType].getNodeType() == nodeType) {
                break;
              }
            }
          }
        }
      }

      _currentNode = node;
             
      return (node == NULL) ? NULL : returnNode(makeNodeHandle(node));
    }
  }  // end of TypedPrecedingIterator

  /**
   * Iterator that returns following nodes of for a given node.
   */
  public class FollowingIterator extends InternalAxisIteratorBase
  {
    //DTMAxisTraverser m_traverser; // easier for now
    
    public FollowingIterator()
    {
      //m_traverser = getAxisTraverser(Axis.FOLLOWING);
    }

    /**
     * Set start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.
     *
     * @param node Sets the root of the iteration.
     *
     * @return A DTMAxisIterator set to the start of the iteration.
     */
    public DTMAxisIterator setStartNode(int node)
    {
//%HZ%: Added reference to DTMDefaultBase.ROOTNODE back in, temporarily
      if (node == DTMDefaultBase.ROOTNODE)
        node = getDocument();
      if (_isRestartable)
      {
        _startNode = node;

        //_currentNode = m_traverser.first(node);
        
        node = makeNodeIdentity(node);

        int first;
        int type = _type2(node);

        if ((DTM.ATTRIBUTE_NODE == type) || (DTM.NAMESPACE_NODE == type))
        {
          node = _parent2(node);
          first = _firstch2(node);

          if (NULL != first) {
            _currentNode = makeNodeHandle(first);
            return resetPosition();
          }
        }

        do
        {
          first = _nextsib2(node);

          if (NULL == first)
            node = _parent2(node);
        }
        while (NULL == first && NULL != node);

        _currentNode = makeNodeHandle(first);

        // _currentNode precedes possible following(node) nodes
        return resetPosition();
      }

      return this;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {

      int node = _currentNode;

      //_currentNode = m_traverser.next(_startNode, _currentNode);
      int current = makeNodeIdentity(node);

      while (true)
      {
        current++;

        int type = _type2(current);
        if (NULL == type) {
          _currentNode = NULL;
          return returnNode(node);
        }

        if (ATTRIBUTE_NODE == type || NAMESPACE_NODE == type)
          continue;

        _currentNode = makeNodeHandle(current);
        return returnNode(node);
      }
    }
    
  }  // end of FollowingIterator

  /**
   * Iterator that returns following nodes of a given type for a given node.
   */
  public final class TypedFollowingIterator extends FollowingIterator
  {

    /** The extended type ID that was requested. */
    private final int _nodeType;

    /**
     * Constructor TypedFollowingIterator
     *
     *
     * @param type The extended type ID being requested.
     */
    public TypedFollowingIterator(int type)
    {
      _nodeType = type;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {
      int current;
      int node;
      int type;

      final int nodeType = _nodeType;
      int currentNodeID = makeNodeIdentity(_currentNode);
      
      if (nodeType >= DTM.NTYPES) {
        do {
          node = currentNodeID;
	  current = node;

          do {
            current++;
            type = _type2(current);
          } 
          while (type != NULL && (ATTRIBUTE_NODE == type || NAMESPACE_NODE == type));
        
          currentNodeID = (type != NULL) ? current : NULL;    
        } 
        while (node != DTM.NULL && _exptype2(node) != nodeType);
      }
      else {
        do {
          node = currentNodeID;
	  current = node;

          do {
            current++;
            type = _type2(current);
          } 
          while (type != NULL && (ATTRIBUTE_NODE == type || NAMESPACE_NODE == type));
        
          currentNodeID = (type != NULL) ? current : NULL;    
        } 
        while (node != DTM.NULL
               && (_exptype2(node) != nodeType && _type2(node) != nodeType));      
      }

      _currentNode = makeNodeHandle(currentNodeID);
      return (node == DTM.NULL ? DTM.NULL :returnNode(makeNodeHandle(node)));
    }
  }  // end of TypedFollowingIterator

  /**
   * Iterator that returns the ancestors of a given node in document
   * order.  (NOTE!  This was changed from the XSLTC code!)
   */
  public class AncestorIterator extends InternalAxisIteratorBase
  {
    // The initial size of the ancestor array
    private static final int m_blocksize = 32;
    
    // The array for ancestor nodes. This array will grow dynamically.
    int[] m_ancestors = new int[m_blocksize];
    
    // Number of ancestor nodes in the array    
    int m_size = 0;
    
    int m_ancestorsPos;

    int m_markedPos;
    
    /** The real start node for this axes, since _startNode will be adjusted. */
    int m_realStartNode;
    
    /**
     * Get start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.
     *
     * @return The root node of the iteration.
     */
    public int getStartNode()
    {
      return m_realStartNode;
    }

    /**
     * True if this iterator has a reversed axis.
     *
     * @return true since this iterator is a reversed axis.
     */
    public final boolean isReverse()
    {
      return true;
    }

    /**
     * Returns a deep copy of this iterator.  The cloned iterator is not reset.
     *
     * @return a deep copy of this iterator.
     */
    public DTMAxisIterator cloneIterator()
    {
      _isRestartable = false;  // must set to false for any clone

      try
      {
        final AncestorIterator clone = (AncestorIterator) super.clone();

        clone._startNode = _startNode;

        // return clone.reset();
        return clone;
      }
      catch (CloneNotSupportedException e)
      {
        throw new DTMException(XMLMessages.createXMLMessage(XMLErrorResources.ER_ITERATOR_CLONE_NOT_SUPPORTED, null)); //"Iterator clone not supported.");
      }
    }

    /**
     * Set start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.
     *
     * @param node Sets the root of the iteration.
     *
     * @return A DTMAxisIterator set to the start of the iteration.
     */
    public DTMAxisIterator setStartNode(int node)
    {
//%HZ%: Added reference to DTMDefaultBase.ROOTNODE back in, temporarily
      if (node == DTMDefaultBase.ROOTNODE)
        node = getDocument();
      m_realStartNode = node;

      if (_isRestartable)
      {
        int nodeID = makeNodeIdentity(node);
        
        if (nodeID == DTM.NULL) {
          _currentNode = DTM.NULL;
          m_ancestorsPos = 0;
          return this;
        }

        // Start from the current node's parent if 
        // _includeSelf is false.
        if (!_includeSelf) {
          nodeID = _parent2(nodeID);
          node = makeNodeHandle(nodeID);
        }

        _startNode = node;

        while (nodeID != END) {
          //m_ancestors.addElement(node);
          if (m_size >= m_ancestors.length)
          {
            int[] newAncestors = new int[m_size * 2];
            System.arraycopy(m_ancestors, 0, newAncestors, 0, m_ancestors.length);
            m_ancestors = newAncestors;
          }
          
          m_ancestors[m_size++] = node;
          nodeID = _parent2(nodeID);
          node = makeNodeHandle(nodeID);
        }
        
        m_ancestorsPos = m_size - 1;

        _currentNode = (m_ancestorsPos>=0)
                               ? m_ancestors[m_ancestorsPos]
                               : DTM.NULL;

        return resetPosition();
      }

      return this;
    }

    /**
     * Resets the iterator to the last start node.
     *
     * @return A DTMAxisIterator, which may or may not be the same as this
     *         iterator.
     */
    public DTMAxisIterator reset()
    {

      m_ancestorsPos = m_size - 1;

      _currentNode = (m_ancestorsPos >= 0) ? m_ancestors[m_ancestorsPos]
                                         : DTM.NULL;

      return resetPosition();
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {

      int next = _currentNode;
      
      int pos = --m_ancestorsPos;

      _currentNode = (pos >= 0) ? m_ancestors[m_ancestorsPos]
                                : DTM.NULL;
      
      return returnNode(next);
    }

    public void setMark() {
        m_markedPos = m_ancestorsPos;
    }

    public void gotoMark() {
        m_ancestorsPos = m_markedPos;
        _currentNode = m_ancestorsPos>=0 ? m_ancestors[m_ancestorsPos]
                                         : DTM.NULL;
    }
  }  // end of AncestorIterator

  /**
   * Typed iterator that returns the ancestors of a given node.
   */
  public final class TypedAncestorIterator extends AncestorIterator
  {

    /** The extended type ID that was requested. */
    private final int _nodeType;

    /**
     * Constructor TypedAncestorIterator
     *
     *
     * @param type The extended type ID being requested.
     */
    public TypedAncestorIterator(int type)
    {
      _nodeType = type;
    }

    /**
     * Set start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.
     *
     * @param node Sets the root of the iteration.
     *
     * @return A DTMAxisIterator set to the start of the iteration.
     */
    public DTMAxisIterator setStartNode(int node)
    {
//%HZ%: Added reference to DTMDefaultBase.ROOTNODE back in, temporarily
      if (node == DTMDefaultBase.ROOTNODE)
        node = getDocument();
      m_realStartNode = node;

      if (_isRestartable)
      {
        int nodeID = makeNodeIdentity(node);
        
        if (nodeID == DTM.NULL) {
          _currentNode = DTM.NULL;
          m_ancestorsPos = 0;
          return this;
        }
        
        final int nodeType = _nodeType;

        if (!_includeSelf) {
          nodeID = _parent2(nodeID);
          node = makeNodeHandle(nodeID);
        }

        _startNode = node;

        if (nodeType >= DTM.NTYPES) {
          while (nodeID != END) {
            int eType = _exptype2(nodeID);

            if (eType == nodeType) {
              if (m_size >= m_ancestors.length)
              {
              	int[] newAncestors = new int[m_size * 2];
              	System.arraycopy(m_ancestors, 0, newAncestors, 0, m_ancestors.length);
              	m_ancestors = newAncestors;
              }
              m_ancestors[m_size++] = makeNodeHandle(nodeID);
            }
            nodeID = _parent2(nodeID);
          }
        }
        else {
          while (nodeID != END) {
            int eType = _exptype2(nodeID);

            if ((eType < DTM.NTYPES && eType == nodeType)
                || (eType >= DTM.NTYPES
                    && m_extendedTypes[eType].getNodeType() == nodeType)) {
              if (m_size >= m_ancestors.length)
              {
              	int[] newAncestors = new int[m_size * 2];
              	System.arraycopy(m_ancestors, 0, newAncestors, 0, m_ancestors.length);
              	m_ancestors = newAncestors;
              }
              m_ancestors[m_size++] = makeNodeHandle(nodeID);
            }
            nodeID = _parent2(nodeID);
          }
        }
        m_ancestorsPos = m_size - 1;

        _currentNode = (m_ancestorsPos>=0)
                               ? m_ancestors[m_ancestorsPos]
                               : DTM.NULL;

        return resetPosition();
      }

      return this;
    }
  }  // end of TypedAncestorIterator

  /**
   * Iterator that returns the descendants of a given node.
   */
  public class DescendantIterator extends InternalAxisIteratorBase
  {

    /**
     * Set start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.
     *
     * @param node Sets the root of the iteration.
     *
     * @return A DTMAxisIterator set to the start of the iteration.
     */
    public DTMAxisIterator setStartNode(int node)
    {
//%HZ%: Added reference to DTMDefaultBase.ROOTNODE back in, temporarily
      if (node == DTMDefaultBase.ROOTNODE)
        node = getDocument();
      if (_isRestartable)
      {
        node = makeNodeIdentity(node);
        _startNode = node;

        if (_includeSelf)
          node--;

        _currentNode = node;

        return resetPosition();
      }

      return this;
    }

    /**
     * Tell if this node identity is a descendant.  Assumes that
     * the node info for the element has already been obtained.
     *
     * This one-sided test works only if the parent has been
     * previously tested and is known to be a descendent. It fails if
     * the parent is the _startNode's next sibling, or indeed any node
     * that follows _startNode in document order.  That may suffice
     * for this iterator, but it's not really an isDescendent() test.
     * %REVIEW% rename?
     *
     * @param identity The index number of the node in question.
     * @return true if the index is a descendant of _startNode.
     */
    protected final boolean isDescendant(int identity)
    {
      return (_parent2(identity) >= _startNode) || (_startNode == identity);
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {
      final int startNode = _startNode;
      if (startNode == NULL) {
        return NULL;
      }

      if (_includeSelf && (_currentNode + 1) == startNode)
          return returnNode(makeNodeHandle(++_currentNode)); // | m_dtmIdent);

      int node = _currentNode;
      int type;

      // %OPT% If the startNode is the root node, do not need
      // to do the isDescendant() check.
      if (startNode == ROOTNODE) {
        int eType;
        do {
          node++;
          eType = _exptype2(node);

          if (NULL == eType) {
            _currentNode = NULL;
            return END;
          }
        } while (eType == TEXT_NODE
                 || (type = m_extendedTypes[eType].getNodeType()) == ATTRIBUTE_NODE
                 || type == NAMESPACE_NODE);
      }
      else {
        do {
          node++;
          type = _type2(node);

          if (NULL == type ||!isDescendant(node)) {
            _currentNode = NULL;
            return END;
          }
        } while(ATTRIBUTE_NODE == type || TEXT_NODE == type
                 || NAMESPACE_NODE == type);      
      }

      _currentNode = node;
      return returnNode(makeNodeHandle(node));  // make handle.
    }
  
    /**
     * Reset.
     *
     */ 
  public DTMAxisIterator reset()
  {

    final boolean temp = _isRestartable;

    _isRestartable = true;

    setStartNode(makeNodeHandle(_startNode));

    _isRestartable = temp;

    return this;
  }
    
  }  // end of DescendantIterator

  /**
   * Typed iterator that returns the descendants of a given node.
   */
  public final class TypedDescendantIterator extends DescendantIterator
  {

    /** The extended type ID that was requested. */
    private final int _nodeType;

    /**
     * Constructor TypedDescendantIterator
     *
     *
     * @param nodeType Extended type ID being requested.
     */
    public TypedDescendantIterator(int nodeType)
    {
      _nodeType = nodeType;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {
      final int startNode = _startNode;
      if (_startNode == NULL) {
        return NULL;
      }

      int node = _currentNode;

      int expType;
      final int nodeType = _nodeType;
      
      if (nodeType != DTM.ELEMENT_NODE)
      {
        do
        {
          node++;
	  expType = _exptype2(node);

          if (NULL == expType || _parent2(node) < startNode && startNode != node) {
            _currentNode = NULL;
            return END;
          }
        }
        while (expType != nodeType);      
      }
      // %OPT% If the start node is root (e.g. in the case of //node),
      // we can save the isDescendant() check, because all nodes are
      // descendants of root.
      else if (startNode == DTMDefaultBase.ROOTNODE)
      {
	do
	{
	  node++;
	  expType = _exptype2(node);

	  if (NULL == expType) {
	    _currentNode = NULL;
	    return END;
	  }
	} while (expType < DTM.NTYPES 
	        || m_extendedTypes[expType].getNodeType() != DTM.ELEMENT_NODE);
      }
      else
      {
        do
        {
          node++;
	  expType = _exptype2(node);

          if (NULL == expType || _parent2(node) < startNode && startNode != node) {
            _currentNode = NULL;
            return END;
          }
        }
        while (expType < DTM.NTYPES 
	       || m_extendedTypes[expType].getNodeType() != DTM.ELEMENT_NODE);      
      }
      
      _currentNode = node;
      return returnNode(makeNodeHandle(node));
    }
  }  // end of TypedDescendantIterator

  /**
   * Iterator that returns a given node only if it is of a given type.
   */
  public final class TypedSingletonIterator extends SingletonIterator
  {

    /** The extended type ID that was requested. */
    private final int _nodeType;

    /**
     * Constructor TypedSingletonIterator
     *
     *
     * @param nodeType The extended type ID being requested.
     */
    public TypedSingletonIterator(int nodeType)
    {
      _nodeType = nodeType;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {

      final int result = _currentNode;
      if (result == END)
        return DTM.NULL;
      
      _currentNode = END;

      if (_nodeType >= DTM.NTYPES) {
        if (_exptype2(makeNodeIdentity(result)) == _nodeType) {
          return returnNode(result);
        }
      } 
      else {
        if (_type2(makeNodeIdentity(result)) == _nodeType) {
          return returnNode(result);
        }
      }

      return NULL;
    }
  }  // end of TypedSingletonIterator

  /*******************************************************************
   *                End of nested iterators          
   *******************************************************************/


  // %OPT% Array references which are used to cache the map0 arrays in 
  // SuballocatedIntVectors. Using the cached arrays reduces the level
  // of indirection and results in better performance than just calling
  // SuballocatedIntVector.elementAt().
  private int[] m_exptype_map0;
  private int[] m_nextsib_map0;
  private int[] m_firstch_map0;
  private int[] m_parent_map0;
  
  // Double array references to the map arrays in SuballocatedIntVectors.
  private int[][] m_exptype_map;
  private int[][] m_nextsib_map;
  private int[][] m_firstch_map;
  private int[][] m_parent_map;

  // %OPT% Cache the array of extended types in this class
  protected ExtendedType[] m_extendedTypes;
  
  // A Vector which is used to store the values of attribute, namespace, 
  // comment and PI nodes.
  //
  // %OPT% These values are unlikely to be equal. Storing
  // them in a plain Vector is more efficient than storing in the
  // DTMStringPool because we can save the cost for hash calculation.
  //
  // %REVISIT% Do we need a custom class (e.g. StringVector) here?
  private Vector m_values;
  
  // The current index into the m_values Vector.
  private int m_valueIndex = 0;
  
  // Cache the shift and mask values for the SuballocatedIntVectors.
  protected int m_SHIFT;
  protected int m_MASK;
  protected int m_blocksize;
  
  /** %OPT% If the offset and length of a Text node are within certain limits,
   * we store a bitwise encoded value into an int, using 10 bits (max. 1024)
   * for length and 21 bits for offset. We can save two SuballocatedIntVector
   * calls for each getStringValueX() and dispatchCharacterEvents() call by 
   * doing this.
   */
  // The number of bits for the length of a Text node.
  protected final static int TEXT_LENGTH_BITS = 10;
  
  // The number of bits for the offset of a Text node.
  protected final static int TEXT_OFFSET_BITS = 21;
  
  // The maximum length value
  protected final static int TEXT_LENGTH_MAX = (1<<TEXT_LENGTH_BITS) - 1;
  
  // The maximum offset value
  protected final static int TEXT_OFFSET_MAX = (1<<TEXT_OFFSET_BITS) - 1;
  
  // True if we want to build the ID index table.
  protected boolean m_buildIdIndex = true;
      
  // Constant for empty string
  private static final String EMPTY_STR = "";

  /**
   * Construct a SAX2DTM2 object using the default block size.
   */
  public SAX2DTM2(DTMManager mgr, Source source, int dtmIdentity,
                 DTMWSFilter whiteSpaceFilter,
                 XMLStringFactory xstringfactory,
                 boolean doIndexing)
  {

    this(mgr, source, dtmIdentity, whiteSpaceFilter,
          xstringfactory, doIndexing, DEFAULT_BLOCKSIZE, true, true);
  }
 
  /**
   * Construct a SAX2DTM2 object using the given block size.
   */
  public SAX2DTM2(DTMManager mgr, Source source, int dtmIdentity,
                 DTMWSFilter whiteSpaceFilter,
                 XMLStringFactory xstringfactory,
                 boolean doIndexing,
                 int blocksize,
                 boolean usePrevsib,
                 boolean buildIdIndex)
  {

    super(mgr, source, dtmIdentity, whiteSpaceFilter,
          xstringfactory, doIndexing, blocksize, usePrevsib);
        
    // Initialize the values of m_SHIFT and m_MASK.
    int shift;
    for(shift=0; (blocksize>>>=1) != 0; ++shift);
    
    m_blocksize = 1<<shift;
    m_SHIFT = shift;
    m_MASK = m_blocksize - 1;
    
    m_buildIdIndex = buildIdIndex;
    
    // Some documents do not have attribute nodes. That is why
    // we set the initial size of this Vector to be small and set
    // the increment to a bigger number.
    m_values = new Vector(32, 512);
    
    // Set the map0 values in the constructor.
    m_exptype_map0 = m_exptype.getMap0();
    m_nextsib_map0 = m_nextsib.getMap0();
    m_firstch_map0 = m_firstch.getMap0();
    m_parent_map0  = m_parent.getMap0();
  }
  
  /**
   * Override DTMDefaultBase._exptype().
   * This one is less efficient than _exptype2. It is only used during
   * DOM building. _exptype2 is used in the case when the document
   * is fully built.
   */
  public final int _exptype(int identity)
  {
    return m_exptype.elementAt(identity);    
  }

  /**
   * The optimized version of DTMDefaultBase._exptype().
   */
  public final int _exptype2(int identity)
  {
    //return m_exptype.elementAt(identity);
    
    if (identity < m_blocksize)
      return m_exptype_map0[identity];
    else
      return m_exptype_map[identity>>>m_SHIFT][identity&m_MASK];    
  }
  
  /**
   * The optimized version of DTMDefaultBase._nextsib().
   */
  public final int _nextsib2(int identity)
  {
    //return m_nextsib.elementAt(identity);
    
    if (identity < m_blocksize)
      return m_nextsib_map0[identity];
    else
      return m_nextsib_map[identity>>>m_SHIFT][identity&m_MASK];     
  }
  
  /**
   * The optimized version of DTMDefaultBase._firstch().
   */
  public final int _firstch2(int identity)
  {
    //return m_firstch.elementAt(identity);
    
    if (identity < m_blocksize)
      return m_firstch_map0[identity];
    else
      return m_firstch_map[identity>>>m_SHIFT][identity&m_MASK];    
  }
  
  /**
   * The optimized version of DTMDefaultBase._parent().
   */
  public final int _parent2(int identity)
  {
    //return m_parent.elementAt(identity);
    
    if (identity < m_blocksize)
      return m_parent_map0[identity];
    else
      return m_parent_map[identity>>>m_SHIFT][identity&m_MASK];    
  }
  
  /**
   * The optimized version of DTMDefaultBase._type().
   */
  public final int _type2(int identity)
  {
    //int eType = _exptype2(identity);
    int eType;
    if (identity < m_blocksize)
      eType = m_exptype_map0[identity];
    else
      eType = m_exptype_map[identity>>>m_SHIFT][identity&m_MASK];    
  	
    if (NULL != eType)
      return m_extendedTypes[eType].getNodeType();
    else
      return NULL;
  }
  
  /**
   * Return the node type from the expanded type
   */
  public final int _exptype2Type(int exptype)
  {
    if (NULL != exptype)
      return m_extendedTypes[exptype].getNodeType();
    else
      return NULL;    
  }
  
  /**
   * Return the expanded type id from the node handle
   */
  public final int getExpandedTypeID(int nodeHandle)
  {
    int nodeID = makeNodeIdentity(nodeHandle);
    
    //return (nodeID != NULL) ? _exptype2(nodeID) : NULL;
    
    if (nodeID != NULL) {
      if (nodeID < m_blocksize)
        return m_exptype_map0[nodeID];
      else
        return m_exptype_map[nodeID>>>m_SHIFT][nodeID&m_MASK]; 
    }
    else
      return NULL;
  }

  /**
   * Given a node identity and an expanded type, return a node in the
   * following-sibling axis of the given node whose expanded type is
   * the given value. This is one of the low-level accessor methods
   * which have access to the SuballocatedIntVector internal arrays to
   * achieve the best performance.
   */
  public final int getTypedFollowingSibling(int node, int exptype)
  {
    /*
    do {
      node = _nextsib2(node);
    } while (node != DTM.NULL && _exptype2(node) != nodeType);
    */
    do {
      node = (node < m_blocksize) ? m_nextsib_map0[node]
             : m_nextsib_map[node>>>m_SHIFT][node&m_MASK];
    } while (node != DTM.NULL && ((node < m_blocksize) ? m_exptype_map0[node] 
             : m_exptype_map[node>>>m_SHIFT][node&m_MASK]) != exptype);
    return node;
  }

  /**
   * Given a node identity and an expanded type, return a node which is
   * the following-sibling of the given node or the given node itself and
   * whose expanded type is the given exptype.
   */
  public final int getTypedSelfAndFollowingSibling(int node, int exptype)
  {
    /*
        while (node != DTM.NULL && _exptype2(node) != nodeType) {
          node = _nextsib2(node);
        }
    */
    while (node != DTM.NULL && ((node < m_blocksize) ? m_exptype_map0[node]
           : m_exptype_map[node>>>m_SHIFT][node&m_MASK]) != exptype)
    {
      node = (node < m_blocksize) ? m_nextsib_map0[node]
             : m_nextsib_map[node>>>m_SHIFT][node&m_MASK];
    }
    return node;
  }

  /**
   * Get a prefix either from the uri mapping, or just make
   * one up!
   *
   * @param uri The namespace URI, which may be null.
   *
   * @return The prefix if there is one, or null.
   */
  public int getIdForNamespace(String uri)
  {
     int index = m_values.indexOf(uri);
     if (index < 0)
     {
       m_values.addElement(uri);
       return m_valueIndex++;
     }
     else
       return index;
     
     //return m_valuesOrPrefixes.stringToIndex(uri);    
  }

  /**
   * Override SAX2DTM.startElement()
   *
   * Receive notification of the start of an element.
   *
   * <p>By default, do nothing.  Application writers may override this
   * method in a subclass to take specific actions at the start of
   * each element (such as allocating a new tree node or writing
   * output to a file).</p>
   *
   * @param name The element type name.
   *
   * @param uri The Namespace URI, or the empty string if the
   *        element has no Namespace URI or if Namespace
   *        processing is not being performed.
   * @param localName The local name (without prefix), or the
   *        empty string if Namespace processing is not being
   *        performed.
   * @param qName The qualified name (with prefix), or the
   *        empty string if qualified names are not available.
   * @param attributes The specified or defaulted attributes.
   * @throws SAXException Any SAX exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.ContentHandler#startElement
   */
  public void startElement(
          String uri, String localName, String qName, Attributes attributes)
            throws SAXException
  {
		
    charactersFlush();

    int exName = m_expandedNameTable.getExpandedTypeID(uri, localName, DTM.ELEMENT_NODE);
    
    int prefixIndex = (qName.length() != localName.length())
                      ? m_valuesOrPrefixes.stringToIndex(qName) : 0;

    int elemNode = addNode(DTM.ELEMENT_NODE, exName,
                           m_parents.peek(), m_previous, prefixIndex, true);

    if(m_indexing)
      indexNode(exName, elemNode);
    
    m_parents.push(elemNode);

    int startDecls = m_contextIndexes.peek();
    int nDecls = m_prefixMappings.size();
    //int prev = DTM.NULL;
    String prefix;

    if(!m_pastFirstElement)
    {
      // SPECIAL CASE: Implied declaration at root element
      prefix="xml";
      String declURL = "http://www.w3.org/XML/1998/namespace";
      exName = m_expandedNameTable.getExpandedTypeID(null, prefix, DTM.NAMESPACE_NODE);
      //int val = m_valuesOrPrefixes.stringToIndex(declURL);
      m_values.addElement(declURL);
      int val = m_valueIndex++;
      addNode(DTM.NAMESPACE_NODE, exName, elemNode,
                     DTM.NULL, val, false);
      m_pastFirstElement=true;
    }

    for (int i = startDecls; i < nDecls; i += 2)
    {
      prefix = (String) m_prefixMappings.elementAt(i);

      if (prefix == null)
        continue;

      String declURL = (String) m_prefixMappings.elementAt(i + 1);

      exName = m_expandedNameTable.getExpandedTypeID(null, prefix, DTM.NAMESPACE_NODE);

      //int val = m_valuesOrPrefixes.stringToIndex(declURL);
      m_values.addElement(declURL);
      int val = m_valueIndex++;

      addNode(DTM.NAMESPACE_NODE, exName, elemNode,
                     DTM.NULL, val, false);
    }

    int n = attributes.getLength();

    for (int i = 0; i < n; i++)
    {
      String attrUri = attributes.getURI(i);
      String attrQName = attributes.getQName(i);
      String valString = attributes.getValue(i);

      int nodeType;
      
      String attrLocalName = attributes.getLocalName(i);

      if ((null != attrQName)
              && (attrQName.equals("xmlns")
                  || attrQName.startsWith("xmlns:")))
      {
        prefix = getPrefix(attrQName, attrUri);
        if (declAlreadyDeclared(prefix))
          continue;  // go to the next attribute.

        nodeType = DTM.NAMESPACE_NODE;
      }
      else
      {
        nodeType = DTM.ATTRIBUTE_NODE;

        if (m_buildIdIndex && attributes.getType(i).equalsIgnoreCase("ID"))
          setIDAttribute(valString, elemNode);
      }

      // Bit of a hack... if somehow valString is null, stringToIndex will
      // return -1, which will make things very unhappy.
      if(null == valString)
        valString = "";

      //int val = m_valuesOrPrefixes.stringToIndex(valString);
      m_values.addElement(valString);
      int val = m_valueIndex++;

      if (attrLocalName.length() != attrQName.length())
      {

        prefixIndex = m_valuesOrPrefixes.stringToIndex(attrQName);

        int dataIndex = m_data.size();

        m_data.addElement(prefixIndex);
        m_data.addElement(val);

        val = -dataIndex;
      }

      exName = m_expandedNameTable.getExpandedTypeID(attrUri, attrLocalName, nodeType);
      addNode(nodeType, exName, elemNode, DTM.NULL, val,
                     false);
    }

    /*
    if (DTM.NULL != prev)
      m_nextsib.setElementAt(DTM.NULL,prev);
    */

    if (null != m_wsfilter)
    {
      short wsv = m_wsfilter.getShouldStripSpace(makeNodeHandle(elemNode), this);
      boolean shouldStrip = (DTMWSFilter.INHERIT == wsv)
                            ? getShouldStripWhitespace()
                            : (DTMWSFilter.STRIP == wsv);

      pushShouldStripWhitespace(shouldStrip);
    }

    m_previous = DTM.NULL;

    m_contextIndexes.push(m_prefixMappings.size());  // for the children.
  }

  /**
   * Receive notification of the end of an element.
   *
   * <p>By default, do nothing.  Application writers may override this
   * method in a subclass to take specific actions at the end of
   * each element (such as finalising a tree node or writing
   * output to a file).</p>
   *
   * @param name The element type name.
   * @param attributes The specified or defaulted attributes.
   *
   * @param uri The Namespace URI, or the empty string if the
   *        element has no Namespace URI or if Namespace
   *        processing is not being performed.
   * @param localName The local name (without prefix), or the
   *        empty string if Namespace processing is not being
   *        performed.
   * @param qName The qualified XML 1.0 name (with prefix), or the
   *        empty string if qualified names are not available.
   * @throws SAXException Any SAX exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.ContentHandler#endElement
   */
  public void endElement(String uri, String localName, String qName)
          throws SAXException
  {
    charactersFlush();

    // If no one noticed, startPrefixMapping is a drag.
    // Pop the context for the last child (the one pushed by startElement)
    m_contextIndexes.quickPop(1);

    // Do it again for this one (the one pushed by the last endElement).
    int topContextIndex = m_contextIndexes.peek();
    if (topContextIndex != m_prefixMappings.size()) {
      m_prefixMappings.setSize(topContextIndex);
    }

    int lastNode = m_previous;

    m_previous = m_parents.pop();

    // If lastNode is still DTM.NULL, this element had no children
    if (DTM.NULL == lastNode)
      m_firstch.setElementAt(DTM.NULL,m_previous);
    else
      m_nextsib.setElementAt(DTM.NULL,lastNode);

    popShouldStripWhitespace();
  }

  /**
   * Report an XML comment anywhere in the document.
   *
   * <p>This callback will be used for comments inside or outside the
   * document element, including comments in the external DTD
   * subset (if read).</p>
   *
   * @param ch An array holding the characters in the comment.
   * @param start The starting position in the array.
   * @param length The number of characters to use from the array.
   * @throws SAXException The application may raise an exception.
   */
  public void comment(char ch[], int start, int length) throws SAXException
  {

    if (m_insideDTD)      // ignore comments if we're inside the DTD
      return;

    charactersFlush();

    //int exName = m_expandedNameTable.getExpandedTypeID(DTM.COMMENT_NODE);

    // For now, treat comments as strings...  I guess we should do a
    // seperate FSB buffer instead.
    /*
    int dataIndex = m_valuesOrPrefixes.stringToIndex(new String(ch, start,
                      length));
    */
    // %OPT% Saving the comment string in a Vector has a lower cost than
    // saving it in DTMStringPool.
    m_values.addElement(new String(ch, start, length));
    int dataIndex = m_valueIndex++;

    m_previous = addNode(DTM.COMMENT_NODE, DTM.COMMENT_NODE,
                         m_parents.peek(), m_previous, dataIndex, false);
  }  

  /**
   * Receive notification of the beginning of the document.
   *
   * @throws SAXException Any SAX exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.ContentHandler#startDocument
   */
  public void startDocument() throws SAXException
  {
		
    int doc = addNode(DTM.DOCUMENT_NODE,
                      DTM.DOCUMENT_NODE,
                      DTM.NULL, DTM.NULL, 0, true);

    m_parents.push(doc);
    m_previous = DTM.NULL;

    m_contextIndexes.push(m_prefixMappings.size());  // for the next element.
  }
  
  /**
   * Receive notification of the end of the document.
   *
   * @throws SAXException Any SAX exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.ContentHandler#endDocument
   */
  public void endDocument() throws SAXException
  {
    super.endDocument();
        
    // Add a NULL entry to the end of the node arrays as
    // the end indication.
    m_exptype.addElement(NULL);
    m_parent.addElement(NULL);
    m_nextsib.addElement(NULL);
    m_firstch.addElement(NULL);
    
    // Set the cached references after the document is built.
    m_extendedTypes = m_expandedNameTable.getExtendedTypes();
    m_exptype_map = m_exptype.getMap();
    m_nextsib_map = m_nextsib.getMap();
    m_firstch_map = m_firstch.getMap();
    m_parent_map  = m_parent.getMap();
  }

  /**
   * Construct the node map from the node.
   *
   * @param type raw type ID, one of DTM.XXX_NODE.
   * @param expandedTypeID The expended type ID.
   * @param parentIndex The current parent index.
   * @param previousSibling The previous sibling index.
   * @param dataOrPrefix index into m_data table, or string handle.
   * @param canHaveFirstChild true if the node can have a first child, false
   *                          if it is atomic.
   *
   * @return The index identity of the node that was added.
   */
  protected final int addNode(int type, int expandedTypeID,
                        int parentIndex, int previousSibling,
                        int dataOrPrefix, boolean canHaveFirstChild)
  {
    // Common to all nodes:
    int nodeIndex = m_size++;

    // Have we overflowed a DTM Identity's addressing range?
    if(m_dtmIdent.size() == (nodeIndex>>>DTMManager.IDENT_DTM_NODE_BITS))
    {
      addNewDTMID(nodeIndex);
    }

    m_firstch.addElement(DTM.NULL);
    m_nextsib.addElement(DTM.NULL);
    m_parent.addElement(parentIndex);
    m_exptype.addElement(expandedTypeID);
    m_dataOrQName.addElement(dataOrPrefix);

    if (m_prevsib != null) {
      m_prevsib.addElement(previousSibling);
    }

    if (DTM.NULL != previousSibling) {
      m_nextsib.setElementAt(nodeIndex,previousSibling);
    }

    if (m_locator != null && m_useSourceLocationProperty) {
      setSourceLocation();
    }

    // Note that nextSibling is not processed until charactersFlush()
    // is called, to handle successive characters() events.

    // Special handling by type: Declare namespaces, attach first child
    switch(type)
    {
    case DTM.NAMESPACE_NODE:
      declareNamespaceInContext(parentIndex,nodeIndex);
      break;
    case DTM.ATTRIBUTE_NODE:
      break;
    default:
      if (DTM.NULL == previousSibling && DTM.NULL != parentIndex) {
        m_firstch.setElementAt(nodeIndex,parentIndex);
      }
      break;
    }

    return nodeIndex;
  }

  /**
   * Check whether accumulated text should be stripped; if not,
   * append the appropriate flavor of text/cdata node.
   */
  protected final void charactersFlush()
  {

    if (m_textPendingStart >= 0)  // -1 indicates no-text-in-progress
    {
      int length = m_chars.size() - m_textPendingStart;
      boolean doStrip = false;

      if (getShouldStripWhitespace())
      {
        doStrip = m_chars.isWhitespace(m_textPendingStart, length);
      }

      if (doStrip)
        m_chars.setLength(m_textPendingStart);  // Discard accumulated text
      else
      {
        // If the offset and length do not exceed the given limits
        // (offset < 2^21 and length < 2^10), then save both the offset
        // and length in a bitwise encoded value.
        if (length <= TEXT_LENGTH_MAX && m_textPendingStart <= TEXT_OFFSET_MAX)
        {
          m_previous = addNode(m_coalescedTextType, DTM.TEXT_NODE,
                             m_parents.peek(), m_previous,
                             length + (m_textPendingStart << TEXT_LENGTH_BITS),
                             false);
          
        }
        else
        {
          // Store the offset and length in the m_data array if one of them
          // exceeds the given limits. Use a negative dataIndex as an indication. 
          int dataIndex = m_data.size();
          m_previous = addNode(m_coalescedTextType, DTM.TEXT_NODE,
                             m_parents.peek(), m_previous, -dataIndex, false);

          m_data.addElement(m_textPendingStart);
          m_data.addElement(length);
        }
      }

      // Reset for next text block
      m_textPendingStart = -1;
      m_textType = m_coalescedTextType = DTM.TEXT_NODE;
    }
  }

  /**
   * Override the processingInstruction() interface in SAX2DTM2.
   * 
   * %OPT% This one is different from SAX2DTM.processingInstruction()
   * in that we do not use extended types for PI nodes. The name of
   * the PI is saved in the DTMStringPool.
   * 
   * Receive notification of a processing instruction.
   * 
   * @param target The processing instruction target.
   * @param data The processing instruction data, or null if
   *             none is supplied.
   * @throws SAXException Any SAX exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.ContentHandler#processingInstruction
   */
  public void processingInstruction(String target, String data)
	  throws SAXException
  {

    charactersFlush();

    int dataIndex = m_data.size();
    m_previous = addNode(DTM.PROCESSING_INSTRUCTION_NODE, 
			 DTM.PROCESSING_INSTRUCTION_NODE,
			 m_parents.peek(), m_previous,
			 -dataIndex, false);

    m_data.addElement(m_valuesOrPrefixes.stringToIndex(target));
    //m_data.addElement(m_valuesOrPrefixes.stringToIndex(data));
    m_values.addElement(data);
    m_data.addElement(m_valueIndex++);

  }

  /**
   * The optimized version of DTMDefaultBase.getFirstAttribute().
   * 
   * Given a node handle, get the index of the node's first attribute.
   *
   * @param nodeHandle int Handle of the node.
   * @return Handle of first attribute, or DTM.NULL to indicate none exists.
   */
  public final int getFirstAttribute(int nodeHandle)
  {
    int nodeID = makeNodeIdentity(nodeHandle);

    if (nodeID == DTM.NULL)
      return DTM.NULL;
    
    int type = _type2(nodeID);

    if (DTM.ELEMENT_NODE == type)
    {
      // Assume that attributes and namespaces immediately follow the element.
      while (true)
      {
        nodeID++;
	// Assume this can not be null.
	type = _type2(nodeID);

	if (type == DTM.ATTRIBUTE_NODE)
	{
	  return makeNodeHandle(nodeID);
	}
	else if (DTM.NAMESPACE_NODE != type)
	{
	  break;
	}
      }
    }

    return DTM.NULL;
  }

  /**
   * The optimized version of DTMDefaultBase.getFirstAttributeIdentity(int).
   *
   * Given a node identity, get the index of the node's first attribute.
   *
   * @param identity int identity of the node.
   * @return Identity of first attribute, or DTM.NULL to indicate none exists.
   */
  protected int getFirstAttributeIdentity(int identity) {
    int type = _type2(identity);

    if (DTM.ELEMENT_NODE == type)
    {
      // Assume that attributes and namespaces immediately follow the element.
      while (true)
      {
        identity++;

        // Assume this can not be null.
        type = _type2(identity);

        if (type == DTM.ATTRIBUTE_NODE)
        {
          return identity;
        }
        else if (DTM.NAMESPACE_NODE != type)
        {
          break;
        }
      }
    }

    return DTM.NULL;
  }

  /**
   * The optimized version of DTMDefaultBase.getNextAttributeIdentity(int).
   *
   * Given a node identity for an attribute, advance to the next attribute.
   *
   * @param identity int identity of the attribute node.  This
   * <strong>must</strong> be an attribute node.
   *
   * @return int DTM node-identity of the resolved attr,
   * or DTM.NULL to indicate none exists.
   *
   */
  protected int getNextAttributeIdentity(int identity) {
    // Assume that attributes and namespace nodes immediately follow the element
    while (true) {
      identity++;
      int type = _type2(identity);

      if (type == DTM.ATTRIBUTE_NODE) {
        return identity;
      } else if (type != DTM.NAMESPACE_NODE) {
        break;
      }
    }

    return DTM.NULL;
  }

  /**
   * The optimized version of DTMDefaultBase.getTypedAttribute(int, int).
   * 
   * Given a node handle and an expanded type ID, get the index of the node's
   * attribute of that type, if any.
   *
   * @param nodeHandle int Handle of the node.
   * @param attType int expanded type ID of the required attribute.
   * @return Handle of attribute of the required type, or DTM.NULL to indicate
   * none exists.
   */
  protected final int getTypedAttribute(int nodeHandle, int attType) 
  {
          
    int nodeID = makeNodeIdentity(nodeHandle);
    
    if (nodeID == DTM.NULL)
      return DTM.NULL;
    
    int type = _type2(nodeID);
    
    if (DTM.ELEMENT_NODE == type)
    {
      int expType;
      while (true)
      {
	nodeID++;
	expType = _exptype2(nodeID);
	
	if (expType != DTM.NULL)
	  type = m_extendedTypes[expType].getNodeType();
	else
	  return DTM.NULL;

	if (type == DTM.ATTRIBUTE_NODE)
	{
	  if (expType == attType) return makeNodeHandle(nodeID);
	}
	else if (DTM.NAMESPACE_NODE != type)
	{
	  break;
	}
      }
    }

    return DTM.NULL;
  }

  /**
   * Override SAX2DTM.getLocalName() in SAX2DTM2
   * Processing for PIs is different.
   * 
   * Given a node handle, return its XPath- style localname. (As defined in
   * Namespaces, this is the portion of the name after any colon character).
   *
   * @param nodeHandle the id of the node.
   * @return String Local name of this node.
   */
  public String getLocalName(int nodeHandle)
  {
    int expType = _exptype(makeNodeIdentity(nodeHandle));
    
    if (expType == DTM.PROCESSING_INSTRUCTION_NODE)
    {
      int dataIndex = _dataOrQName(makeNodeIdentity(nodeHandle));
      dataIndex = m_data.elementAt(-dataIndex);
      return m_valuesOrPrefixes.indexToString(dataIndex);     
    }
    else
      return m_expandedNameTable.getLocalName(expType);
  }

  /**
   * The optimized version of SAX2DTM.getNodeNameX().
   * 
   * Given  a node handle, return the XPath node name. This should be the name
   * as described by the XPath data model, NOT the DOM- style name.
   *
   * @param nodeHandle the id of the node.
   * @return String Name of this node, which may be an empty string.
   */
  public final String getNodeNameX(int nodeHandle)
  {

    int nodeID = makeNodeIdentity(nodeHandle);
    int eType = _exptype2(nodeID);
    
    if (eType == DTM.PROCESSING_INSTRUCTION_NODE)
    {
      int dataIndex = _dataOrQName(nodeID);
      dataIndex = m_data.elementAt(-dataIndex);
      return m_valuesOrPrefixes.indexToString(dataIndex);           
    }
    
    final ExtendedType extType = m_extendedTypes[eType];
                         
    if (extType.getNamespace().length() == 0)
    {
      return extType.getLocalName();
    }
    else
    {
      int qnameIndex = m_dataOrQName.elementAt(nodeID);

      if (qnameIndex == 0)
        return extType.getLocalName();
      
      if (qnameIndex < 0)
      {
	qnameIndex = -qnameIndex;
	qnameIndex = m_data.elementAt(qnameIndex);
      }

      return m_valuesOrPrefixes.indexToString(qnameIndex);
    }
  }

  /**
   * The optimized version of SAX2DTM.getNodeName().
   * 
   * Given a node handle, return its DOM-style node name. This will include
   * names such as #text or #document.
   *
   * @param nodeHandle the id of the node.
   * @return String Name of this node, which may be an empty string.
   * %REVIEW% Document when empty string is possible...
   * %REVIEW-COMMENT% It should never be empty, should it?
   */
  public String getNodeName(int nodeHandle)
  {

    int nodeID = makeNodeIdentity(nodeHandle);
    int eType = _exptype2(nodeID);

    final ExtendedType extType = m_extendedTypes[eType];
    if (extType.getNamespace().length() == 0)
    {
      int type = extType.getNodeType();
      
      String localName = extType.getLocalName(); 
      if (type == DTM.NAMESPACE_NODE)
      {
	if (localName.length() == 0)
	  return "xmlns";
	else
	  return "xmlns:" + localName;
      }
      else if (type == DTM.PROCESSING_INSTRUCTION_NODE)
      {
	int dataIndex = _dataOrQName(nodeID);
	dataIndex = m_data.elementAt(-dataIndex);
	return m_valuesOrPrefixes.indexToString(dataIndex);           	
      }
      else if (localName.length() == 0)
      {
	return m_fixednames[type];
      }
      else
	return localName;      
    }
    else
    {
      int qnameIndex = m_dataOrQName.elementAt(nodeID);

      if (qnameIndex == 0)
        return extType.getLocalName();
      
      if (qnameIndex < 0)
      {
	qnameIndex = -qnameIndex;
	qnameIndex = m_data.elementAt(qnameIndex);
      }

      return m_valuesOrPrefixes.indexToString(qnameIndex);
    }
  }

  /**
   * Override SAX2DTM.getStringValue(int)
   *
   *%REVISIT% There should be no need to override this interface.
   * It is only a temporary solution to keep the extensions working.
   * We can get rid of this when the code becomes more integrated.
   *
   * If the caller supplies an XMLStringFactory, the getStringValue() interface
   * in SAX2DTM will be called. Otherwise just calls getStringValueX() and
   * wraps the returned String in an XMLString.
   *
   * Get the string-value of a node as a String object
   * (see http://www.w3.org/TR/xpath#data-model
   * for the definition of a node's string-value).
   *
   * @param nodeHandle The node ID.
   *
   * @return A string object that represents the string-value of the given node.
   */
  public XMLString getStringValue(int nodeHandle)
  {
    if (m_xstrf != null)
      return super.getStringValue(nodeHandle);
    else
      return new XMLStringDefault(getStringValueX(nodeHandle));
  }
  
  /**
   * The optimized version of SAX2DTM.getStringValue(int).
   * 
   * %OPT% This is one of the most often used interfaces. Performance is
   * critical here. This one is different from SAX2DTM.getStringValue(int) in
   * that it returns a String instead of a XMLString.
   * 
   * Get the string- value of a node as a String object (see http: //www. w3.
   * org/TR/xpath#data- model for the definition of a node's string- value).
   *
   * @param nodeHandle The node ID.
   *
   * @return A string object that represents the string-value of the given node.
   */
  public final String getStringValueX(final int nodeHandle)
  {
    int identity = makeNodeIdentity(nodeHandle);
    if (identity == DTM.NULL)
      return EMPTY_STR;
    
    int type= _type2(identity);

    if (type == DTM.ELEMENT_NODE || type == DTM.DOCUMENT_NODE)
    {
      int startNode = identity;
      identity = _firstch2(identity);
      if (DTM.NULL != identity)
      {
	int offset = -1;
	int length = 0;

	do 
	{
	  type = _exptype2(identity);

	  if (type == DTM.TEXT_NODE || type == DTM.CDATA_SECTION_NODE)
	  {
	    int dataIndex = m_dataOrQName.elementAt(identity);
	    if (dataIndex > 0)
	    {
	      if (-1 == offset)
	      {
                offset = dataIndex >>> TEXT_LENGTH_BITS;
	      }

	      length += dataIndex & TEXT_LENGTH_MAX;	      
	    }
	    else
	    {
	      if (-1 == offset)
	      {
                offset = m_data.elementAt(-dataIndex);
	      }

	      length += m_data.elementAt(-dataIndex + 1);
	    }
	  }

	  identity++;
	} while (_parent2(identity) >= startNode);

	if (length > 0)
	{
	  return m_chars.getString(offset, length);
	}
	else
	  return EMPTY_STR;
      }
      else
        return EMPTY_STR;
    } 
    else if (DTM.TEXT_NODE == type || DTM.CDATA_SECTION_NODE == type)
    {
      int dataIndex = m_dataOrQName.elementAt(identity);
      if (dataIndex > 0)
      {
      	return m_chars.getString(dataIndex >>> TEXT_LENGTH_BITS, 
      	                          dataIndex & TEXT_LENGTH_MAX);
      }
      else
      {
        return m_chars.getString(m_data.elementAt(-dataIndex), 
                                  m_data.elementAt(-dataIndex+1));
      }
    }
    else
    {
      int dataIndex = m_dataOrQName.elementAt(identity);

      if (dataIndex < 0)
      {
        dataIndex = -dataIndex;
        dataIndex = m_data.elementAt(dataIndex + 1);
      }
      //return m_valuesOrPrefixes.indexToString(dataIndex);
      return (String)m_values.elementAt(dataIndex);
    }
  }

  /**
   * Returns the string value of the entire tree
   */
  public String getStringValue()
  {
    int child = _firstch2(ROOTNODE);
    if (child == DTM.NULL) return EMPTY_STR;
      
    // optimization: only create StringBuffer if > 1 child      
    if ((_exptype2(child) == DTM.TEXT_NODE) && (_nextsib2(child) == DTM.NULL))
    {
      int dataIndex = m_dataOrQName.elementAt(child);
      if (dataIndex > 0)
        return m_chars.getString(dataIndex >>> TEXT_LENGTH_BITS, dataIndex & TEXT_LENGTH_MAX);
      else
        return m_chars.getString(m_data.elementAt(-dataIndex), 
                                  m_data.elementAt(-dataIndex + 1));      
    }
    else
      return getStringValueX(getDocument());
    
  }

  /**
   * The optimized version of SAX2DTM.dispatchCharactersEvents(int, ContentHandler, boolean).
   * This one does not have the third boolean parameter.
   *
   * Directly call the
   * characters method on the passed ContentHandler for the
   * string-value of the given node (see http://www.w3.org/TR/xpath#data-model
   * for the definition of a node's string-value). Multiple calls to the
   * ContentHandler's characters methods may well occur for a single call to
   * this method.
   *
   * @param nodeHandle The node ID.
   * @param ch A non-null reference to a ContentHandler.
   * @param normalize true if the content should be normalized according to
   * the rules for the XPath
   * <a href="http://www.w3.org/TR/xpath#function-normalize-space">normalize-space</a>
   * function.
   *
   * @throws SAXException
   */
  public final void dispatchCharactersEvents(int nodeHandle, ContentHandler ch)
          throws SAXException
  {

    int identity = makeNodeIdentity(nodeHandle);
    
    if (identity == DTM.NULL)
      return;
    
    int type = _type2(identity);

    if (type == DTM.ELEMENT_NODE || type == DTM.DOCUMENT_NODE)
    {
      int startNode = identity;
      identity = _firstch2(identity);
      if (DTM.NULL != identity)
      {
	int offset = -1;
	int length = 0;

	do 
	{
	  type = _exptype2(identity);

	  if (type == DTM.TEXT_NODE || type == DTM.CDATA_SECTION_NODE)
	  {
	    int dataIndex = m_dataOrQName.elementAt(identity);

	    if (dataIndex > 0)
	    {
	      if (-1 == offset)
	      {
                offset = dataIndex >>> TEXT_LENGTH_BITS;
	      }

	      length += dataIndex & TEXT_LENGTH_MAX;	      
	    }
	    else
	    {
	      if (-1 == offset)
	      {
                offset = m_data.elementAt(-dataIndex);
	      }

	      length += m_data.elementAt(-dataIndex + 1);
	    }
	  }

	  identity++;
	} while (_parent2(identity) >= startNode);

	if (length > 0)
	{
	  m_chars.sendSAXcharacters(ch, offset, length);
	}
      }
    } 
    else if (DTM.TEXT_NODE == type || DTM.CDATA_SECTION_NODE == type)
    {
      int dataIndex = m_dataOrQName.elementAt(identity);

      if (dataIndex > 0)
      {
      	m_chars.sendSAXcharacters(ch, dataIndex >>> TEXT_LENGTH_BITS,
      	                          dataIndex & TEXT_LENGTH_MAX);
      }
      else
      {
        m_chars.sendSAXcharacters(ch, m_data.elementAt(-dataIndex), 
                                  m_data.elementAt(-dataIndex+1));
      }
    }
    else
    {
      int dataIndex = m_dataOrQName.elementAt(identity);

      if (dataIndex < 0)
      {
        dataIndex = -dataIndex;
        dataIndex = m_data.elementAt(dataIndex + 1);
      }
      
      //String str = m_valuesOrPrefixes.indexToString(dataIndex);
      String str = (String)m_values.elementAt(dataIndex);
      ch.characters(str.toCharArray(), 0, str.length());
    }
  }

  /**
   * Given a node handle, return its node value. This is mostly
   * as defined by the DOM, but may ignore some conveniences.
   * <p>
   *
   * @param nodeHandle The node id.
   * @return String Value of this node, or null if not
   * meaningful for this node type.
   */
  public String getNodeValue(int nodeHandle)
  {

    int identity = makeNodeIdentity(nodeHandle);
    int type = _type2(identity);

    if (type == DTM.TEXT_NODE || type == DTM.CDATA_SECTION_NODE)
    {
      int dataIndex = _dataOrQName(identity);
      if (dataIndex > 0)
      {
      	return m_chars.getString(dataIndex >>> TEXT_LENGTH_BITS, 
      	                          dataIndex & TEXT_LENGTH_MAX);
      }
      else
      {
        return m_chars.getString(m_data.elementAt(-dataIndex), 
                                  m_data.elementAt(-dataIndex+1));
      }
    }
    else if (DTM.ELEMENT_NODE == type || DTM.DOCUMENT_FRAGMENT_NODE == type
             || DTM.DOCUMENT_NODE == type)
    {
      return null;
    }
    else
    {
      int dataIndex = m_dataOrQName.elementAt(identity);

      if (dataIndex < 0)
      {
        dataIndex = -dataIndex;
        dataIndex = m_data.elementAt(dataIndex + 1);
      }

      //return m_valuesOrPrefixes.indexToString(dataIndex);
      return (String)m_values.elementAt(dataIndex);
    }
  }
  
  
}