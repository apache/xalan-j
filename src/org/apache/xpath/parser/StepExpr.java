package org.apache.xpath.parser;

import org.apache.xml.dtm.Axis;
import org.apache.xml.dtm.DTMFilter;
import org.apache.xpath.axes.LocPathIterator;

/**
 * The responsibility of StepExpr is to hold the axis, node test, and 
 * predicates while the expression is being constructed.  Objects of 
 * this class will be rewritten as DTMIterators or AxesWalkers.
 * 
 * Created Jul 12, 2002
 * 
 * @author sboag
 * @see org.apache.xpath.parser.NonExecutableExpression#NonExecutableExpression(XPath)
 * @see org.apache.xpath.parser.PathExpr#jjtAddChild(Node, int)
 * @see org.apache.xpath.parser.SimpleNode#jjtAddChild(Node, int)
 */
public class StepExpr extends NonExecutableExpression
{
  /**
   * Create a StepExpr object for construction purposes.
   */
  public StepExpr(XPath parser)
  {
    super(parser);
  }

  /**
   * Tell if this node is part of a PathExpr chain.  For instance:
   * <pre>
   * 	|UnaryExpr
   * 	|   PathExpr
   * 	|      StepExpr
   * 	|         AxisChild child::
   * 	|         NodeTest
   * 	|            NameTest
   * 	|               QName foo
   * 	|         Predicates   * 
   * </pre><br/>
   * In this example, UnaryExpr, PathExpr, and StepExpr should all return true.
   */
  public boolean isPathExpr()
  {
    // Don't reduce if there are predicates!
    return (getNumPredicates() > 0) ? false : true;
  }

  /**
   * @see org.apache.xpath.parser.Node#jjtClose()
   */
  public void jjtClose()
  {
    Node n = jjtGetChild(0);
    if (n instanceof PatternAxis && n.jjtGetNumChildren() == 2)
    {
      Node patternAxis = n.jjtGetChild(0);
      m_exprs.setElementAt(patternAxis, 0);
      patternAxis.jjtSetParent(this);

      NameTest nt = (NameTest) n.jjtGetChild(1);
      NodeTest nodeTest = new NodeTest(getParser());
      nodeTest.jjtAddChild(nt, 0);

      m_exprs.insertElementAt(nodeTest, 1);
      nodeTest.jjtSetParent(this);

      ((PatternAxis) n).m_exprs.removeAllElements();
    }
    else
      if (n instanceof PatternAxis && n.jjtGetNumChildren() == 1)
      {
        Node child = n.jjtGetChild(0);
        if (child instanceof NodeTest)
        {
          NodeTest nodeTest = (NodeTest) n.jjtGetChild(0);
          m_exprs.insertElementAt(nodeTest, 1);
          nodeTest.jjtSetParent(this);
        }
        else // instanceof pattern axis
          {
          // occurs for "." and "..".
          PatternAxis patAxes = (PatternAxis) n.jjtGetChild(0);
          m_exprs.setElementAt(patAxes, 0); // replace self
          patAxes.jjtSetParent(this);
          NodeTest nodeTest = (NodeTest) patAxes.jjtGetChild(0);
          m_exprs.insertElementAt(nodeTest, 1);
          nodeTest.jjtSetParent(this);
          patAxes.m_exprs.removeAllElements();
        }

        ((PatternAxis) n).m_exprs.removeAllElements();
      }

    // We can't tell if a "*" is an attribute until late, so we have to 
    // wait until now, and fix up the WhatToShow value.
    int axis = getAxis();

    if (Axis.ATTRIBUTE == axis || Axis.NAMESPACE == axis)
    {
      NodeTest ntest = getNodeTest();
      if (null != ntest)
      {
        if (org.apache.xml.dtm.Axis.ATTRIBUTE == axis)
          ntest.setWhatToShow(org.apache.xml.dtm.DTMFilter.SHOW_ATTRIBUTE);
        else
          if (org.apache.xml.dtm.Axis.NAMESPACE == axis)
          {
            ntest.setWhatToShow(org.apache.xml.dtm.DTMFilter.SHOW_NAMESPACE);
            org.apache.xml.utils.PrefixResolver resolver =
              getParser().getPrefixResolver();
            // NodeTest compares the resolved URI, so I guess this is done here.
            // (even though I can't find it in the old code... 
            // I guess it's in there somewhere? -sb)
            String prefix = ntest.getLocalName();
            if (!prefix.equals("*"))
            {
              String ns = resolver.getNamespaceForPrefix(prefix);
              ntest.setLocalName(ns);
            }
          }
      }
    }
  }

  /**
   * Get the axis ID for this step expression.
   * @return int One of org.apache.xml.dtm.Axis.XXXX.
   * @see org.apache.xml.dtm.Axis
   */
  public int getAxis()
  {
    Node firstChild = jjtGetChild(0);
    if (firstChild instanceof PatternAxis)
      return ((PatternAxis) firstChild).getAxis();
    else
      return org.apache.xml.dtm.Axis.ALLFROMNODE;
  }

  /**
   * Get the axis to be traversed for this step expression.
   * @return PatternAxis The axis expression object, or null if there isn't one.
   */
  public PatternAxis getAxisExpr()
  {
    Node firstChild = jjtGetChild(0);
    if (firstChild instanceof PatternAxis)
      return ((PatternAxis) firstChild);
    else
      return null;
  }

  /**
   * Get the node test object owned by this step expression.
   * @return NodeTest The node test, or null if there isn't one.
   */
  public NodeTest getNodeTest()
  {
    if (jjtGetNumChildren() > 2)
    {
      Node secondChild = jjtGetChild(1);
      if (secondChild instanceof NodeTest)
        return (NodeTest) secondChild;
      else
        return null;
    }
    else
      return null;
  }

  /**
   * Tell what this node should show on the given axis.
   * @return int One of org.apache.xml.dtm.DTMFilter.SHOW_XXX.
   * @see org.apache.xml.dtm.DTMFilter
   */
  public int getWhatToShow()
  {
    NodeTest nt = getNodeTest();
    if (null != nt)
      return nt.getWhatToShow();
    else
      return DTMFilter.SHOW_ALL;
  }

  /**
   * Tell if this node test is a wildcard.
   * @return boolean true if the node test should show all the nodes on the axis.
   */
  public boolean isTotallyWild()
  {
    NodeTest nt = getNodeTest();
    if (null != nt)
      return nt.isTotallyWild();
    else
      return true;
  }

  /**
   * Get the namespace URI for the node test owned by this expression.
   * @return String URI string, or null if none.
   */
  public String getNamespaceURI()
  {
    NodeTest nt = getNodeTest();
    if (null != nt)
      return nt.getNamespaceURI();
    else
      return null;
  }

  /**
   * Get the local name for the node test owned by this expression.
   * @return String local name, or null if none.
   */
  public String getLocalName()
  {
    NodeTest nt = getNodeTest();
    if (null != nt)
      return nt.getLocalName();
    else
      return null;
  }

  /**
   * Get the number of predicates for this expression.
   * @return int The number of predicates to apply to this expression.
   */
  public int getNumPredicates()
  {
    int lastChildIndex = jjtGetNumChildren() - 1;
    if (lastChildIndex > 0)
    {
      Node lastChild = jjtGetChild(lastChildIndex);
      if (lastChild instanceof Predicates)
        return ((Predicates) lastChild).jjtGetNumChildren();
      else
        return 0;
    }
    else
      return 0;
  }

  /**
   * Get the predicates for this step expression.
   * @return Predicates predicates if they have been added, otherwise null.
   */
  public Predicates getPredicates()
  {
    int lastChildIndex = jjtGetNumChildren() - 1;
    if (lastChildIndex > 0)
    {
      Node lastChild = jjtGetChild(lastChildIndex);
      if (lastChild instanceof Predicates)
        return (Predicates) lastChild;
      else
        return null;
    }
    else
      return null;
  }

}
