package org.apache.xml.dtm;

/**
 * This is a default implementation of a table that manages mappings from 
 * expanded names to expandedNameIDs.
 */

public class ExpandedNameTable
{

  public ExpandedNameTable()
  {
  }
  
  /**
   * Given an expanded name, return an ID.  If the expanded-name does not
   * exist in the internal tables, the entry will be created, and the ID will
   * be returned.  Any additional nodes that are created that have this
   * expanded name will use this ID.
   *
   * @param nodeHandle The handle to the node in question.
   *
   * NEEDSDOC @param namespace
   * NEEDSDOC @param localName
   *
   * @return the expanded-name id of the node.
   */
  public int getExpandedNameID(String namespace, String localName)
  {

    // %TBD%
    return 0;
  }

  /**
   * Given an expanded-name ID, return the local name part.
   *
   * @param ExpandedNameID an ID that represents an expanded-name.
   * @return String Local name of this node.
   */
  public String getLocalNameFromExpandedNameID(int ExpandedNameID)
  {

    // %TBD%
    return null;
  }

  /**
   * Given an expanded-name ID, return the namespace URI part.
   *
   * @param ExpandedNameID an ID that represents an expanded-name.
   * @return String URI value of this node's namespace, or null if no
   * namespace was resolved.
   */
  public String getNamespaceFromExpandedNameID(int ExpandedNameID)
  {

    // %TBD%
    return null;
  }
}