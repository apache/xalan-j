package org.apache.xalan.stree;

import org.apache.xpath.DOM2Helper;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

public class StreeDOMHelper extends DOM2Helper
{
  /**
   * Create an empty DOM Document.  Mainly used for RTFs.
   */
  public Document createDocument()
  {
    return new DocumentImpl();
  }

  /**
   * Figure out if node2 should be placed after node1 when 
   * placing nodes in a list that is to be sorted in 
   * document order.
   * NOTE: Make sure this does the right thing with attribute nodes!!!
   * @return true if node2 should be placed 
   * after node1, and false if node2 should be placed 
   * before node1.
   */
  public boolean isNodeAfter(Node node1, Node node2)
  {
    // Assume first that the nodes are DTM nodes, since discovering node 
    // order is massivly faster for the DTM.
    try
    {
      int index1 = ((Child)node1).getUid();
      int index2 = ((Child)node2).getUid();
      return index1 <= index2;
    }
    catch(ClassCastException cce)
    {
      // isNodeAfter will return true if node is after countedNode 
      // in document order. isDOMNodeAfter is sloooow (relativly).
      return super.isNodeAfter(node1, node2);
    }
  }   
  
  /**
   * <meta name="usage" content="internal"/>
   * Get the depth level of this node in the tree.
   */
  public short getLevel(Node node1)
  {
    return ((Child)node1).getLevel();
  }
  
  /**
   * Tell if the given node is a namespace decl node.
   */
  public boolean isNamespaceNode(Node n)
  {
    return ((Child)n).isNamespaceNode();
  }


}
