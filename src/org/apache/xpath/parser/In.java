/* Generated By:JJTree: Do not edit this line. In.java */

package org.apache.xpath.parser;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xpath.Expression;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.XPathContext;
import org.apache.xpath.XPathVisitor;
import org.apache.xpath.objects.XObject;

// This node could probably be void. -sb
public class In extends NonExecutableExpression
{
	
  public In(XPath parser) {
    super(parser);
  }

  /** Accept the visitor. **/
  public Object jjtAccept(org.apache.xpath.parser.XPathVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
  
}
