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
package org.apache.xalan.templates;

import org.w3c.dom.*;
import org.xml.sax.*;
import org.apache.xpath.*;
import org.apache.xalan.utils.QName;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;

/**
 * <meta name="usage" content="advanced"/>
 * Implement xsl:template.
 * <pre>
 * <!ELEMENT xsl:template
 *  (#PCDATA
 *   %instructions;
 *   %result-elements;
 *   | xsl:param)*
 * >
 * 
 * <!ATTLIST xsl:template
 *   match %pattern; #IMPLIED
 *   name %qname; #IMPLIED
 *   priority %priority; #IMPLIED
 *   mode %qname; #IMPLIED
 *   %space-att;
 * >
 * </pre>
 * @see <a href="http://www.w3.org/TR/xslt#section-Defining-Template-Rules">section-Defining-Template-Rules in XSLT Specification</a>
 */
public class ElemTemplate extends ElemTemplateElement
{
  private String m_publicId;
  private String m_systemId;
  
  /**
   * Return the public identifier for the current document event.
   * <p>This will be the public identifier
   * @return A string containing the public identifier, or
   *         null if none is available.
   * @see #getSystemId
   */
  public String getPublicId ()
  {
    return m_publicId;
  }
  
  /**
   * Return the system identifier for the current document event.
   *
   * <p>If the system identifier is a URL, the parser must resolve it
   * fully before passing it to the application.</p>
   *
   * @return A string containing the system identifier, or null
   *         if none is available.
   * @see #getPublicId
   */
  public String getSystemId ()
  {
    return m_systemId;
  }
  
  /**
   * Set the location information for this element.
   */
  public void setLocaterInfo(Locator locator)
  {
    m_publicId = locator.getPublicId();
    m_systemId = locator.getSystemId();
    super.setLocaterInfo(locator);
  }
  
  /** 
   * The owning stylesheet.
   * (Should this only be put on the template element, to
   * conserve space?)
   * @serial
   */
  private Stylesheet m_stylesheet;

  /**
   * Get the owning stylesheet.
   */
  public StylesheetComposed getStylesheetComposed()
  {
    return m_stylesheet.getStylesheetComposed();
  }

  /**
   * Get the owning stylesheet.
   */
  public Stylesheet getStylesheet()
  {
    return m_stylesheet;
  }
  
  /**
   * Set the owning stylesheet.
   */
  public void setStylesheet(Stylesheet sheet)
  {
    m_stylesheet = sheet;
  }

  /**
   * Get the owning stylesheet.
   */
  public StylesheetRoot getStylesheetRoot()
  {
    return m_stylesheet.getStylesheetRoot();
  }
  
  /**
   * The match attribute is a Pattern that identifies the source 
   * node or nodes to which the rule applies.
   */
  private XPath m_matchPattern = null;
  
  /**
   * Set the "match" attribute. 
   * The match attribute is a Pattern that identifies the source 
   * node or nodes to which the rule applies. The match attribute 
   * is required unless the xsl:template element has a name 
   * attribute (see [6 Named Templates]). It is an error for the 
   * value of the match attribute to contain a VariableReference. 
   * @see <a href="http://www.w3.org/TR/xslt#patterns">patterns in XSLT Specification</a>
   */
  public void setMatch(XPath v)
  {
    m_matchPattern = v;
  }

  /**
   * Get the "match" attribute. 
   * The match attribute is a Pattern that identifies the source 
   * node or nodes to which the rule applies. The match attribute 
   * is required unless the xsl:template element has a name 
   * attribute (see [6 Named Templates]). It is an error for the 
   * value of the match attribute to contain a VariableReference. 
   * @see <a href="http://www.w3.org/TR/xslt#patterns">patterns in XSLT Specification</a>
   */
  public XPath getMatch()
  {
    return m_matchPattern;
  }
  
  /**
   * An xsl:template element with a name attribute specifies a named template.
   */
  private QName m_name = null;
  
  /**
   * Set the "name" attribute. 
   * An xsl:template element with a name attribute specifies a named template.
   * If an xsl:template element has a name attribute, it may, but need not, 
   * also have a match attribute. 
   * @see <a href="http://www.w3.org/TR/xslt#named-templates">named-templates in XSLT Specification</a>
   */
  public void setName(QName v)
  {
    m_name = v;
  }

  /**
   * Get the "name" attribute. 
   * An xsl:template element with a name attribute specifies a named template.
   * If an xsl:template element has a name attribute, it may, but need not, 
   * also have a match attribute. 
   * @see <a href="http://www.w3.org/TR/xslt#named-templates">named-templates in XSLT Specification</a>
   */
  public QName getName()
  {
    return m_name;
  }
  
  /**
   * Modes allow an element to be processed multiple times, 
   * each time producing a different result.
   */
  private QName m_mode;
  
  /**
   * Set the "mode" attribute. 
   * Modes allow an element to be processed multiple times, 
   * each time producing a different result.  If xsl:template 
   * does not have a match attribute, it must not have a mode attribute. 
   * @see <a href="http://www.w3.org/TR/xslt#modes">modes in XSLT Specification</a>
   */
  public void setMode(QName v)
  {
    m_mode = v;
  }

  /**
   * Get the "mode" attribute. 
   * Modes allow an element to be processed multiple times, 
   * each time producing a different result.  If xsl:template 
   * does not have a match attribute, it must not have a mode attribute. 
   * @see <a href="http://www.w3.org/TR/xslt#modes">modes in XSLT Specification</a>
   */
  public QName getMode()
  {
    return m_mode;
  }
  
  /**
   * The priority of a template rule is specified by the priority 
   * attribute on the template rule.
   */
  private double m_priority = XPath.MATCH_SCORE_NONE;
  
  /**
   * Set the "priority" attribute. 
   * The priority of a template rule is specified by the priority 
   * attribute on the template rule. The value of this must be a 
   * real number (positive or negative), matching the production 
   * Number with an optional leading minus sign (-). 
   * @see <a href="http://www.w3.org/TR/xslt#conflict">conflict in XSLT Specification</a>
   */
  public void setPriority(double v)
  {
    m_priority = v;
  }

  /**
   * Get the "priority" attribute. 
   * The priority of a template rule is specified by the priority 
   * attribute on the template rule. The value of this must be a 
   * real number (positive or negative), matching the production 
   * Number with an optional leading minus sign (-). 
   * @see <a href="http://www.w3.org/TR/xslt#conflict">conflict in XSLT Specification</a>
   */
  public double getPriority()
  {
    return m_priority;
  }

  /**
   * Get an int constant identifying the type of element.
   * @see org.apache.xalan.templates.Constants
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_TEMPLATE;
  }
  
  /** 
   * Return the node name.
   */
  public String getNodeName()
  {
    return Constants.ELEMNAME_TEMPLATE_STRING;
  }

  /**
   * Copy the template contents into the result tree.
   * The content of the xsl:template element is the template 
   * that is instantiated when the template rule is applied.
   */
  public void execute(TransformerImpl transformer, 
                      Node sourceNode,
                      QName mode)
    throws SAXException
  {    
    if(TransformerImpl.S_DEBUG)
      transformer.getTraceManager().fireTraceEvent(sourceNode, mode, this);

    if(null != sourceNode)
    { 
      transformer.executeChildTemplates(this, sourceNode, mode);
    }
    else // if(null == sourceNode)
    {
      transformer.getMsgMgr().error(this, sourceNode, 
                                    XSLTErrorResources.ER_NULL_SOURCENODE_HANDLEAPPLYTEMPLATES); 
      //"sourceNode is null in handleApplyTemplatesInstruction!");
    }
  }
}
