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
package org.apache.xalan.transformer;

import java.util.Vector;

import java.text.NumberFormat;
import java.text.CollationKey;

import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

import org.apache.xpath.axes.ContextNodeList;
import org.apache.xpath.XPathContext;
import org.apache.xpath.NodeSet;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xalan.utils.NodeVector;

import org.xml.sax.SAXException;

/**
 * <meta name="usage" content="internal"/>
 * This class can sort vectors of DOM nodes according to a select pattern.
 */
public class NodeSorter
{

  /** NEEDSDOC Field m_execContext          */
  XPathContext m_execContext;

  /** NEEDSDOC Field m_keys          */
  Vector m_keys;  // vector of NodeSortKeys

  /**
   * TODO: Adjust this for locale.
   */
  NumberFormat m_formatter = NumberFormat.getNumberInstance();

  /**
   * Construct a NodeSorter, passing in the XSL TransformerFactory
   * so it can know how to get the node data according to
   * the proper whitespace rules.
   *
   * NEEDSDOC @param p
   */
  public NodeSorter(XPathContext p)
  {
    m_execContext = p;
  }

  /**
   * Given a vector of nodes, sort each node according to
   * the criteria in the keys.
   * @param v an vector of Nodes.
   * @param keys a vector of NodeSortKeys.
   * NEEDSDOC @param support
   *
   * @throws org.xml.sax.SAXException
   */
  public void sort(NodeVector v, Vector keys, XPathContext support)
          throws org.xml.sax.SAXException
  {

    m_keys = keys;

    // QuickSort2(v, 0, v.size() - 1 );
    int n = v.size();

    // Create a vector of node compare elements
    // based on the input vector of nodes
    Vector nodes = new Vector();

    for (int i = 0; i < n; i++)
    {
      NodeCompareElem elem = new NodeCompareElem((Node) v.elementAt(i));

      nodes.addElement(elem);
    }

    Vector scratchVector = new Vector();

    mergesort(nodes, scratchVector, 0, n - 1, support);

    // return sorted vector of nodes
    for (int i = 0; i < n; i++)
    {
      v.setElementAt(((NodeCompareElem) nodes.elementAt(i)).m_node, i);
    }

    // old code...
    //NodeVector scratchVector = new NodeVector(n);
    //mergesort(v, scratchVector, 0, n - 1, support);
  }

  /**
   * Return the results of a compare of two nodes.
   * TODO: Optimize compare -- cache the getStringExpr results, key by m_selectPat + hash of node.
   *
   * NEEDSDOC @param n1
   * NEEDSDOC @param n2
   * NEEDSDOC @param kIndex
   * NEEDSDOC @param support
   *
   * NEEDSDOC ($objectName$) @return
   *
   * @throws SAXException
   */
  int compare(
          NodeCompareElem n1, NodeCompareElem n2, int kIndex, XPathContext support)
            throws SAXException
  {

    int result = 0;
    NodeSortKey k = (NodeSortKey) m_keys.elementAt(kIndex);

    if (k.m_treatAsNumbers)
    {
      double n1Num, n2Num;

      if (kIndex == 0)
      {
        n1Num = ((Double) n1.m_key1Value).doubleValue();
        n2Num = ((Double) n2.m_key1Value).doubleValue();
      }
      else if (kIndex == 1)
      {
        n1Num = ((Double) n1.m_key2Value).doubleValue();
        n2Num = ((Double) n2.m_key2Value).doubleValue();
      }

      /* Leave this in case we decide to use an array later
      if (kIndex < maxkey)
      {
      double n1Num = (double)n1.m_keyValue[kIndex];
      double n2Num = (double)n2.m_keyValue[kIndex];
      }*/
      else
      {

        // Get values dynamically
        XObject r1 = k.m_selectPat.execute(m_execContext, n1.m_node,
                                           k.m_namespaceContext);
        XObject r2 = k.m_selectPat.execute(m_execContext, n2.m_node,
                                           k.m_namespaceContext);
        double d = r1.num();

        // Can't use NaN for compare. They are never equal. Use zero instead.
        // That way we can keep elements in document order. 
        n1Num = Double.isNaN(d) ? 0.0 : d;
        d = r2.num();
        n2Num = Double.isNaN(d) ? 0.0 : d;
      }

      if ((n1Num == n2Num) && ((kIndex + 1) < m_keys.size()))
      {
        result = compare(n1, n2, kIndex + 1, support);
      }
      else
      {
        double diff = n1Num - n2Num;

        // process order parameter 
        result = (int) ((diff < 0.0)
                        ? (k.m_descending ? 1 : -1)
                        : (diff > 0.0) ? (k.m_descending ? -1 : 1) : 0);
      }
    }  // end treat as numbers 
    else
    {
      CollationKey n1String, n2String;

      if (kIndex == 0)
      {
        n1String = (CollationKey) n1.m_key1Value;
        n2String = (CollationKey) n2.m_key1Value;
      }
      else if (kIndex == 1)
      {
        n1String = (CollationKey) n1.m_key2Value;
        n2String = (CollationKey) n2.m_key2Value;
      }

      /* Leave this in case we decide to use an array later
      if (kIndex < maxkey)
      {
        String n1String = (String)n1.m_keyValue[kIndex];
        String n2String = (String)n2.m_keyValue[kIndex];
      }*/
      else
      {

        // Get values dynamically
        XObject r1 = k.m_selectPat.execute(m_execContext, n1.m_node,
                                           k.m_namespaceContext);
        XObject r2 = k.m_selectPat.execute(m_execContext, n2.m_node,
                                           k.m_namespaceContext);

        n1String = k.m_col.getCollationKey(r1.str());
        n2String = k.m_col.getCollationKey(r2.str());
      }

      // Use collation keys for faster compare, but note that whitespaces 
      // etc... are treated differently from if we were comparing Strings.
      result = n1String.compareTo(n2String);

      //Process caseOrder parameter
      if (k.m_caseOrderUpper)
      {
        String tempN1 = n1String.getSourceString().toLowerCase();
        String tempN2 = n2String.getSourceString().toLowerCase();

        if (tempN1.equals(tempN2))
        {

          //java defaults to upper case is greater.
          result = result == 0 ? 0 : -result;
        }
      }

      //Process order parameter
      if (k.m_descending)
      {
        result = -result;
      }
    }  //end else

    if (0 == result)
    {
      if ((kIndex + 1) < m_keys.size())
      {
        result = compare(n1, n2, kIndex + 1, support);
      }
    }

    if (0 == result)
    {

      // I shouldn't have to do this except that there seems to 
      // be a glitch in the mergesort
      // if(r1.getType() == r1.CLASS_NODESET)
      // {
      result = support.getDOMHelper().isNodeAfter(n1.m_node, n2.m_node)
               ? -1 : 1;

      // }
    }

    return result;
  }

  /**
   * This implements a standard Mergesort, as described in
   * Robert Sedgewick's Algorithms book.  This is a better
   * sort for our purpose than the Quicksort because it
   * maintains the original document order of the input if
   * the order isn't changed by the sort.
   *
   * NEEDSDOC @param a
   * NEEDSDOC @param b
   * NEEDSDOC @param l
   * NEEDSDOC @param r
   * NEEDSDOC @param support
   *
   * @throws SAXException
   */
  void mergesort(Vector a, Vector b, int l, int r, XPathContext support)
          throws SAXException
  {

    if ((r - l) > 0)
    {
      int m = (r + l) / 2;

      mergesort(a, b, l, m, support);
      mergesort(a, b, m + 1, r, support);

      int i, j, k;

      for (i = m; i >= l; i--)
      {

        // b[i] = a[i];
        // Use insert if we need to increment vector size.
        if (i >= b.size())
          b.insertElementAt(a.elementAt(i), i);
        else
          b.setElementAt(a.elementAt(i), i);
      }

      i = l;

      for (j = (m + 1); j <= r; j++)
      {

        // b[r+m+1-j] = a[j];
        if (r + m + 1 - j >= b.size())
          b.insertElementAt(a.elementAt(j), r + m + 1 - j);
        else
          b.setElementAt(a.elementAt(j), r + m + 1 - j);
      }

      j = r;

      int compVal;

      for (k = l; k <= r; k++)
      {

        // if(b[i] < b[j])
        if (i == j)
          compVal = -1;
        else
          compVal = compare((NodeCompareElem) b.elementAt(i),
                            (NodeCompareElem) b.elementAt(j), 0, support);

        if (compVal < 0)
        {

          // a[k]=b[i];
          a.setElementAt(b.elementAt(i), k);

          i++;
        }
        else if (compVal > 0)
        {

          // a[k]=b[j]; 
          a.setElementAt(b.elementAt(j), k);

          j--;
        }
      }
    }
  }

  /**
   * This is a generic version of C.A.R Hoare's Quick Sort
   * algorithm.  This will handle arrays that are already
   * sorted, and arrays with duplicate keys.<BR>
   *
   * If you think of a one dimensional array as going from
   * the lowest index on the left to the highest index on the right
   * then the parameters to this function are lowest index or
   * left and highest index or right.  The first time you call
   * this function it will be with the parameters 0, a.length - 1.
   *
   * @param a       an integer array
   * @param lo0     left boundary of array partition
   * @param hi0     right boundary of array partition
   *
   * NEEDSDOC @param v
   * NEEDSDOC @param i
   * NEEDSDOC @param j
   */

  /*  private void QuickSort2(Vector v, int lo0, int hi0, XPathContext support)
      throws org.xml.sax.SAXException,
             java.net.MalformedURLException,
             java.io.FileNotFoundException,
             java.io.IOException
    {
      int lo = lo0;
      int hi = hi0;

      if ( hi0 > lo0)
      {
        // Arbitrarily establishing partition element as the midpoint of
        // the array.
        Node midNode = (Node)v.elementAt( ( lo0 + hi0 ) / 2 );

        // loop through the array until indices cross
        while( lo <= hi )
        {
          // find the first element that is greater than or equal to
          // the partition element starting from the left Index.
          while( (lo < hi0) && (compare((Node)v.elementAt(lo), midNode, 0, support) < 0) )
          {
            ++lo;
          } // end while

          // find an element that is smaller than or equal to
          // the partition element starting from the right Index.
          while( (hi > lo0) && (compare((Node)v.elementAt(hi), midNode, 0, support) > 0) )
          {
            --hi;
          }

          // if the indexes have not crossed, swap
          if( lo <= hi )
          {
            swap(v, lo, hi);
            ++lo;
            --hi;
          }
        }

        // If the right index has not reached the left side of array
        // must now sort the left partition.
        if( lo0 < hi )
        {
          QuickSort2( v, lo0, hi, support );
        }

        // If the left index has not reached the right side of array
        // must now sort the right partition.
        if( lo < hi0 )
        {
          QuickSort2( v, lo, hi0, support );
        }
      }
    } // end QuickSort2  */

  /**
   * Simple function to swap two elements in
   * a vector.
   */
  private void swap(Vector v, int i, int j)
  {

    Node node = (Node) v.elementAt(i);

    v.setElementAt(v.elementAt(j), i);
    v.setElementAt(node, j);
  }

  /**
   * <meta name="usage" content="internal"/>
   * NEEDSDOC Class NodeCompareElem <needs-comment/>
   */
  class NodeCompareElem
  {

    /** NEEDSDOC Field m_node          */
    Node m_node;

    // This maxkey value was chosen arbitrarily. We are assuming that the    
    // maxkey + 1 keys will only hit fairly rarely and therefore, we
    // will get the node values for those keys dynamically.

    /** NEEDSDOC Field maxkey          */
    int maxkey = 2;

    // Keep this in case we decide to use an array. Right now
    // using two variables is cheaper.
    //Object[] m_KeyValue = new Object[2];

    /** NEEDSDOC Field m_key1Value          */
    Object m_key1Value;

    /** NEEDSDOC Field m_key2Value          */
    Object m_key2Value;

    /**
     * Constructor NodeCompareElem
     *
     *
     * NEEDSDOC @param node
     *
     * @throws org.xml.sax.SAXException
     */
    NodeCompareElem(Node node) throws org.xml.sax.SAXException
    {

      boolean tryNextKey = true;

      m_node = node;

      if (!m_keys.isEmpty())
      {
        NodeSortKey k1 = (NodeSortKey) m_keys.elementAt(0);
        XObject r = k1.m_selectPat.execute(m_execContext, node,
                                           k1.m_namespaceContext);

        if (r == null)
          tryNextKey = false;

        double d;

        if (k1.m_treatAsNumbers)
        {
          d = r.num();

          // Can't use NaN for compare. They are never equal. Use zero instead.  
          m_key1Value = new Double(Double.isNaN(d) ? 0.0 : d);
        }
        else
        {
          m_key1Value = k1.m_col.getCollationKey(r.str());
        }

        if (r.getType() == XObject.CLASS_NODESET)
        {
          NodeIterator ni = (NodeIterator) r.object();

          if (ni instanceof ContextNodeList)
            tryNextKey = (((ContextNodeList) ni).getCurrentNode() != null);

          // else abdicate... should never happen, but... -sb
        }

        if (m_keys.size() > 1)
        {
          NodeSortKey k2 = (NodeSortKey) m_keys.elementAt(1);

          if (!tryNextKey)
          {
            if (k2.m_treatAsNumbers)
              m_key2Value = new Double(0.0);
            else
              m_key2Value = k2.m_col.getCollationKey("");
          }
          else
          {
            XObject r2 = k2.m_selectPat.execute(m_execContext, node,
                                                k2.m_namespaceContext);

            if (k2.m_treatAsNumbers)
            {
              d = r2.num();
              m_key2Value = new Double(Double.isNaN(d) ? 0.0 : d);
            }
            else
              m_key2Value = k2.m_col.getCollationKey(r2.str());
          }
        }

        /* Leave this in case we decide to use an array later
        while (kIndex <= m_keys.size() && kIndex < maxkey)
        {
          NodeSortKey k = (NodeSortKey)m_keys.elementAt(kIndex);
          XObject r = k.m_selectPat.execute(m_execContext, node, k.m_namespaceContext);
          if(k.m_treatAsNumbers)
            m_KeyValue[kIndex] = r.num();
          else
            m_KeyValue[kIndex] = r.str();
        } */
      }  // end if not empty    
    }
  }  // end NodeCompareElem class
}
