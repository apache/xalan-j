package org.apache.xalan.stree;

import org.w3c.dom.Node;

/**
 * Right now this is the same as TextImpl.  Not sure what I 
 * want to do with this, but it certainly seems like there 
 * should be some way to optimize the storage of whitespace 
 * nodes.
 */
public class WhiteSpace extends Child
{
  String m_data;
  
  public WhiteSpace (DocumentImpl doc, char ch[], int start, int length)
  {
    super(doc);
    m_data = new String(ch, start, start+length);
  }
  
  /** 
   * A short integer indicating what type of node this is. The named
   * constants for this value are defined in the org.w3c.dom.Node interface.
   */
  public short getNodeType() 
  {
    return Node.TEXT_NODE;
  }

  /** Returns the node name. */
  public String getNodeName() 
  {
    return "#text";
  }
  
  /**
   * Returns the local part of the qualified name of this node.
   * <br>For nodes created with a DOM Level 1 method, such as 
   * <code>createElement</code> from the <code>Document</code> interface, 
   * it is <code>null</code>.
   * @since DOM Level 2
   */
  public String       getLocalName()
  {
    return "#text";
  }
  
  /**
   * Retrieve character data currently stored in this node.
   * 
   * @throws DOMExcpetion(DOMSTRING_SIZE_ERR) In some implementations,
   * the stored data may exceed the permitted length of strings. If so,
   * getData() will throw this DOMException advising the user to
   * instead retrieve the data in chunks via the substring() operation.  
   */
  public String getData() 
  {
    return m_data;
  }

  /** 
   * Report number of characters currently stored in this node's
   * data. It may be 0, meaning that the value is an empty string. 
   */
  public int getLength() 
  {   
    return m_data.length();
  }  
  
  public String getNodeValue() 
  {
    return m_data;
  } // getNodeValue():String
}
