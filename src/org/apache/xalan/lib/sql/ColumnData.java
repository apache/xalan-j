package org.apache.xalan.lib.sql;

import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.Document;
import org.w3c.dom.DOMException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.xalan.res.XSLTErrorResources;

/**
 * This class represents a text node from a Column element.
 */
public class ColumnData extends StreamableNode implements Text
{
  Column m_parent;
  private static final boolean DEBUG = false;
  
  public ColumnData(XStatement statement, Column parent)
  {
    super(statement);
    m_parent = parent;
  }
  
  /**
   * Return Node.TEXT_NODE.
   */
  public short getNodeType()
  {
    return Node.TEXT_NODE;
  }
  
  public Text splitText(int offset)
    throws DOMException
  {
    error(XSLTErrorResources.ER_FUNCTION_NOT_SUPPORTED);
    return null;
  }

  public String getData()
    throws DOMException
  {
    try
    {
      ResultSet rs = this.getXStatement().getResultSet();
      int columnIndex = m_parent.m_columnIndex;
      
      if(DEBUG)
        System.out.println("In ColumnData.getData, columnIndex: "+columnIndex);
      if(columnIndex < m_parent.m_parent.m_childCount)
      {
        return rs.getString(columnIndex+1);
      }
      else
        return null;
    }
    catch(SQLException sqle)
    {
      return null;
    }
  }
  
  public String getNodeValue()
    throws DOMException
  {
    return getData();
  }
  
  /**
   *  The number of  16-bit units that are available through 
   * <code>data</code> and the <code>substringData</code> method below.  
   * This may have the value zero, i.e., <code>CharacterData</code> nodes 
   * may be empty.
   */
  public int getLength()
  {
    String s = getData();
    return (null != s) ? s.length() : 0;
  }
  
  public String substringData(int offset, 
                              int count)
    throws DOMException
  {
    error(XSLTErrorResources.ER_FUNCTION_NOT_SUPPORTED);
    return null;
  }
  
  public void appendData(String arg)
    throws DOMException
  {
    error(XSLTErrorResources.ER_FUNCTION_NOT_SUPPORTED);
  }
  
  public void insertData(int offset, 
                         String arg)
    throws DOMException
  {
    error(XSLTErrorResources.ER_FUNCTION_NOT_SUPPORTED);
  }

  public void deleteData(int offset, 
                         int count)
    throws DOMException
  {
    error(XSLTErrorResources.ER_FUNCTION_NOT_SUPPORTED);
  }

  public void replaceData(int offset, 
                          int count, 
                          String arg)
    throws DOMException
  {
    error(XSLTErrorResources.ER_FUNCTION_NOT_SUPPORTED);
  }
  
  public void setData(String data)
    throws DOMException
  {
    // TODO: It would be cool to make this callable, to set 
    // a value in the database.
    error(XSLTErrorResources.ER_FUNCTION_NOT_SUPPORTED);
  }

  
  /**
   * The parent of a document is null.
   */
  public Document getOwnerDocument()
  {
    return this.getXStatement();
  }
  
  /**
   * Return "#Text".
   */
  public String getNodeName()
  {
    return "#Text";
  }
      
  /**
   * Return null.
   */
  public Node               getFirstChild()
  {
    if(DEBUG)
      System.out.println("In ColumnData.getNextSibling");
    return null;
  }
  
  /**
   * This always returns null.
   */
  public Node               getNextSibling()
  {
    if(DEBUG)
      System.out.println("In ColumnData.getNextSibling");
    return null;
  }
  
  /**
   * The parent node of document is always null.
   */
  public Node               getParentNode()
  {
    if(DEBUG)
      System.out.println("In ColumnData.getParentNode");
    return m_parent;
  }
  
  /**
   * Tell if there are any children of the document, 
   * which is always true.
   */
  public boolean            hasChildNodes()
  {
    if(DEBUG)
      System.out.println("In ColumnData.hasChildNodes");
    return false;
  }


}
