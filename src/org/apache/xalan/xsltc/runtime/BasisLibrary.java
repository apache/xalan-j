/*
 * @(#)$Id$
 *
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
 * originally based on software copyright (c) 2001, Sun
 * Microsystems., http://www.sun.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 * @author Erwin Bolwidt <ejb@klomp.org>
 * @author John Howard <johnh@schemasoft.com>
 *
 */

package org.apache.xalan.xsltc.runtime;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;

import org.apache.xalan.xsltc.DOM;
import org.apache.xalan.xsltc.Translet;
import org.apache.xalan.xsltc.dom.AbsoluteIterator;
import org.apache.xalan.xsltc.dom.Axis;
import org.apache.xalan.xsltc.dom.DOMAdapter;
import org.apache.xalan.xsltc.dom.SAXImpl;
import org.apache.xalan.xsltc.dom.MultiDOM;
import org.apache.xalan.xsltc.dom.SingletonIterator;
import org.apache.xalan.xsltc.dom.StepIterator;
import org.apache.xml.dtm.DTMAxisIterator;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.dtm.ref.DTMDefaultBase;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.apache.xml.serializer.SerializationHandler;

/**
 * Standard XSLT functions. All standard functions expect the current node 
 * and the DOM as their last two arguments.
 */
public final class BasisLibrary implements Operators {

    private final static String EMPTYSTRING = "";

    /**
     * Standard function count(node-set)
     */
    public static int countF(DTMAxisIterator iterator) {
	return(iterator.getLast());
    }

    /**
     * Standard function position()
     */
    public static int positionF(DTMAxisIterator iterator) {
	return iterator.isReverse()
                     ? iterator.getLast() - iterator.getPosition() + 1
                     : iterator.getPosition();
    }

    /**
     * XSLT Standard function sum(node-set). 
     * stringToDouble is inlined
     */
    public static double sumF(DTMAxisIterator iterator, DOM dom) {
	try {
	    double result = 0.0;
	    int node;
	    while ((node = iterator.next()) != DTMAxisIterator.END) {
		result += Double.parseDouble(dom.getStringValueX(node));
	    }
	    return result;
	}
	catch (NumberFormatException e) {
	    return Double.NaN;
	}
    }

    /**
     * XSLT Standard function string()
     */
    public static String stringF(int node, DOM dom) {
	return dom.getStringValueX(node);
    }

    /**
     * XSLT Standard function string(value)
     */
    public static String stringF(Object obj, DOM dom) {
	if (obj instanceof DTMAxisIterator) {
	    return dom.getStringValueX(((DTMAxisIterator)obj).reset().next());
	}
	else if (obj instanceof Node) {
	    return dom.getStringValueX(((Node)obj).node);
	}
	else if (obj instanceof DOM) {
	    return ((DOM)obj).getStringValue();
	}
	else {
	    return obj.toString();
	}
    }

    /**
     * XSLT Standard function string(value)
     */
    public static String stringF(Object obj, int node, DOM dom) {
	if (obj instanceof DTMAxisIterator) {
	    return dom.getStringValueX(((DTMAxisIterator)obj).reset().next());
	}
	else if (obj instanceof Node) {
	    return dom.getStringValueX(((Node)obj).node);
	}
	else if (obj instanceof DOM) {
	    // When the first argument is a DOM we want the whole fecking
	    // DOM and not just a single node - that would not make sense.
	    //return ((DOM)obj).getStringValueX(node);
	    return ((DOM)obj).getStringValue();
	}
	else if (obj instanceof Double) {
	    Double d = (Double)obj;
	    final String result = d.toString();
	    final int length = result.length();
	    if ((result.charAt(length-2)=='.') &&
		(result.charAt(length-1) == '0'))
		return result.substring(0, length-2);
	    else
		return result;
	}
	else {
	    if (obj != null)
		return obj.toString();
	    else
		return stringF(node, dom);
	}
    }

    /**
     * XSLT Standard function number()
     */
    public static double numberF(int node, DOM dom) {
	return stringToReal(dom.getStringValueX(node));
    }

    /**
     * XSLT Standard function number(value)
     */
    public static double numberF(Object obj, DOM dom) {
	if (obj instanceof Double) {
	    return ((Double) obj).doubleValue();
	}
	else if (obj instanceof Integer) {
	    return ((Integer) obj).doubleValue();
	}
	else if (obj instanceof Boolean) {
	    return  ((Boolean) obj).booleanValue() ? 1.0 : 0.0;
	}
	else if (obj instanceof String) {
	    return stringToReal((String) obj);
	}
	else if (obj instanceof DTMAxisIterator) {
	    DTMAxisIterator iter = (DTMAxisIterator) obj;
	    return stringToReal(dom.getStringValueX(iter.reset().next()));
	}
	else if (obj instanceof Node) {
	    return stringToReal(dom.getStringValueX(((Node) obj).node));
	}
	else if (obj instanceof DOM) {
	    return stringToReal(((DOM) obj).getStringValue());
	}
	else {
	    final String className = obj.getClass().getName();
	    runTimeError(INVALID_ARGUMENT_ERR, className, "number()");
	    return 0.0;
	}
    }

    /**
     * XSLT Standard function boolean()
     */
    public static boolean booleanF(Object obj) {
	if (obj instanceof Double) {
	    final double temp = ((Double) obj).doubleValue();
	    return temp != 0.0 && !Double.isNaN(temp);
	}
	else if (obj instanceof Integer) {
	    return ((Integer) obj).doubleValue() != 0;
	}
	else if (obj instanceof Boolean) {
	    return  ((Boolean) obj).booleanValue();
	}
	else if (obj instanceof String) {
	    return !((String) obj).equals(EMPTYSTRING);
	}
	else if (obj instanceof DTMAxisIterator) {
	    DTMAxisIterator iter = (DTMAxisIterator) obj;
	    return iter.reset().next() != DTMAxisIterator.END;
	}
	else if (obj instanceof Node) {
	    return true;
	}
	else if (obj instanceof DOM) {
	    String temp = ((DOM) obj).getStringValue();
	    return !temp.equals(EMPTYSTRING);
	}
	else {
	    final String className = obj.getClass().getName();
	    runTimeError(INVALID_ARGUMENT_ERR, className, "number()");
	}
	return false;
    }

    /**
     * XSLT Standard function substring(). Must take a double because of
     * conversions resulting into NaNs and rounding.
     */
    public static String substringF(String value, double start) {
	try {
	    final int strlen = value.length();
	    int istart = (int)Math.round(start) - 1;

	    if (Double.isNaN(start)) return(EMPTYSTRING);
	    if (istart > strlen) return(EMPTYSTRING);
 	    if (istart < 1) istart = 0;

	    return value.substring(istart);
	}
	catch (IndexOutOfBoundsException e) {
	    runTimeError(RUN_TIME_INTERNAL_ERR, "substring()");
	    return null;
	}
    }

    /**
     * XSLT Standard function substring(). Must take a double because of
     * conversions resulting into NaNs and rounding.
     */
    public static String substringF(String value, double start, double length) {
	try {
	    final int strlen  = value.length();
	    int istart = (int)Math.round(start) - 1;
	    int isum   = istart + (int)Math.round(length);

	    if (Double.isInfinite(length)) isum = Integer.MAX_VALUE;

	    if (Double.isNaN(start) || Double.isNaN(length))
		return(EMPTYSTRING);
	    if (Double.isInfinite(start)) return(EMPTYSTRING);
	    if (istart > strlen) return(EMPTYSTRING);
	    if (isum < 0) return(EMPTYSTRING);
 	    if (istart < 0) istart = 0;

	    if (isum > strlen)
		return value.substring(istart);
	    else
		return value.substring(istart, isum);
	}
	catch (IndexOutOfBoundsException e) {
	    runTimeError(RUN_TIME_INTERNAL_ERR, "substring()");
	    return null;
	}
    }

    /**
     * XSLT Standard function substring-after(). 
     */
    public static String substring_afterF(String value, String substring) {
	final int index = value.indexOf(substring);
	if (index >= 0)
	    return value.substring(index + substring.length());
	else
	    return EMPTYSTRING;
    }

    /**
     * XSLT Standard function substring-before(). 
     */
    public static String substring_beforeF(String value, String substring) {
	final int index = value.indexOf(substring);
	if (index >= 0)
	    return value.substring(0, index);
	else
	    return EMPTYSTRING;
    }

    /**
     * XSLT Standard function translate(). 
     */
    public static String translateF(String value, String from, String to) {
	final int tol = to.length();
	final int froml = from.length();
	final int valuel = value.length();

	final StringBuffer result = new StringBuffer();
	for (int j, i = 0; i < valuel; i++) {
	    final char ch = value.charAt(i);
	    for (j = 0; j < froml; j++) {
		if (ch == from.charAt(j)) {
		    if (j < tol)
			result.append(to.charAt(j));
		    break;
		}
	    }	
	    if (j == froml)
		result.append(ch);
	}
	return result.toString();
    }

    /**
     * XSLT Standard function normalize-space(). 
     */
    public static String normalize_spaceF(int node, DOM dom) {
	return normalize_spaceF(dom.getStringValueX(node));
    }

    /**
     * XSLT Standard function normalize-space(string). 
     */
    public static String normalize_spaceF(String value) {
	int i = 0, n = value.length();
	StringBuffer result = new StringBuffer();

	while (i < n && isWhiteSpace(value.charAt(i)))
	    i++;

	while (true) {
	    while (i < n && !isWhiteSpace(value.charAt(i))) {
		result.append(value.charAt(i++));
	    }
	    if (i == n)
		break;
	    while (i < n && isWhiteSpace(value.charAt(i))) {
		i++;
	    }
	    if (i < n)
		result.append(' ');
	}
	return result.toString();
    }

    /**
     * XSLT Standard function generate-id(). 
     */
    public static String generate_idF(int node) {
	if (node > 0)
	    // Only generate ID if node exists
	    return "N" + node;
	else
	    // Otherwise return an empty string
	    return EMPTYSTRING;
    }
    
    /**
     * utility function for calls to local-name(). 
     */
    public static String getLocalName(String value) {
	int idx = value.lastIndexOf(':');
	if (idx >= 0) value = value.substring(idx + 1);
	idx = value.lastIndexOf('@');
	if (idx >= 0) value = value.substring(idx + 1);
	return(value);
    }

    /**
     * External functions that cannot be resolved are replaced with a call
     * to this method. This method will generate a runtime errors. A good
     * stylesheet checks whether the function exists using conditional
     * constructs, and never really tries to call it if it doesn't exist.
     * But simple stylesheets may result in a call to this method.
     * The compiler should generate a warning if it encounters a call to
     * an unresolved external function.
     */
    public static void unresolved_externalF(String name) {
	runTimeError(EXTERNAL_FUNC_ERR, name);
    }

    /**
     * XSLT Standard function namespace-uri(node-set).
     */
    public static String namespace_uriF(DTMAxisIterator iter, DOM dom) {
	return namespace_uriF(iter.next(), dom);
    }

    /**
     * XSLT Standard function system-property(name)
     */
    public static String system_propertyF(String name) {
	if (name.equals("xsl:version"))
	    return("1.0");
	if (name.equals("xsl:vendor"))
	    return("Apache Software Foundation (Xalan XSLTC)");
	if (name.equals("xsl:vendor-url"))
	    return("http://xml.apache.org/xalan-j");
	
	runTimeError(INVALID_ARGUMENT_ERR, name, "system-property()");
	return(EMPTYSTRING);
    }

    /**
     * XSLT Standard function namespace-uri(). 
     */
    public static String namespace_uriF(int node, DOM dom) {
	final String value = dom.getNodeName(node);
	final int colon = value.lastIndexOf(':');
	if (colon >= 0)
	    return value.substring(0, colon);
	else
	    return EMPTYSTRING;
    }

    /**
     * Implements the object-type() extension function.
     * 
     * @see <a href="http://www.exslt.org/">EXSLT</a>
     */
    public static String objectTypeF(Object obj)
    {
      if (obj instanceof String)
        return "string";
      else if (obj instanceof Boolean)
        return "boolean";
      else if (obj instanceof Number)
        return "number";
      else if (obj instanceof DOM)
        return "RTF";
      else if (obj instanceof DTMAxisIterator)
        return "node-set";
      else
        return "unknown";
    }  

    /**
     * Implements the nodeset() extension function. 
     */
    public static DTMAxisIterator nodesetF(Object obj) {
	if (obj instanceof DOM) {
	   //final DOMAdapter adapter = (DOMAdapter) obj;
	   final DOM dom = (DOM)obj;
	   return new SingletonIterator(dom.getDocument(), true);
	}
        else if (obj instanceof DTMAxisIterator) {
	   return (DTMAxisIterator) obj;
        }
        else {
	    final String className = obj.getClass().getName();
	    runTimeError(DATA_CONVERSION_ERR, "node-set", className);
	    return null;
        }
    }

    //-- Begin utility functions

    private static boolean isWhiteSpace(char ch) {
	return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r';
    }

    private static boolean compareStrings(String lstring, String rstring,
					  int op, DOM dom) {
	switch (op) {
	case EQ:
	    return lstring.equals(rstring);

	case NE:
	    return !lstring.equals(rstring);

	case GT:
	    return numberF(lstring, dom) > numberF(rstring, dom);

	case LT:
	    return numberF(lstring, dom) < numberF(rstring, dom);

	case GE:
	    return numberF(lstring, dom) >= numberF(rstring, dom);

	case LE:
	    return numberF(lstring, dom) <= numberF(rstring, dom);

	default:
	    runTimeError(RUN_TIME_INTERNAL_ERR, "compare()");
	    return false;
	}
    }

    /**
     * Utility function: node-set/node-set compare. 
     */
    public static boolean compare(DTMAxisIterator left, DTMAxisIterator right,
				  int op, DOM dom) {
	int lnode;
	left.reset();
	
	while ((lnode = left.next()) != DTMAxisIterator.END) {
	    final String lvalue = dom.getStringValueX(lnode);
	    
	    int rnode;
	    right.reset();
	    while ((rnode = right.next()) != DTMAxisIterator.END) {
                // String value must be the same if both nodes are the same
                if (lnode == rnode) {
                    if (op == EQ) {
                        return true;
                    } else if (op == NE) {
                        continue;
                    }
                }
		if (compareStrings(lvalue, dom.getStringValueX(rnode), op,
                                   dom)) {
		    return true;
		}
	    }
	} 
	return false;
    }

    public static boolean compare(int node, DTMAxisIterator iterator,
				  int op, DOM dom) {
	//iterator.reset();

	int rnode;
	String value;

	switch(op) {
	case EQ:
            rnode = iterator.next();
            if (rnode != DTMAxisIterator.END) {
	        value = dom.getStringValueX(node);
                do {
		    if (node == rnode
                          || value.equals(dom.getStringValueX(rnode))) {
                       return true;
                    }
	        } while ((rnode = iterator.next()) != DTMAxisIterator.END);
            }
	    break;
	case NE:
            rnode = iterator.next();
            if (rnode != DTMAxisIterator.END) {
	        value = dom.getStringValueX(node);
                do {
		    if (node != rnode
                          && !value.equals(dom.getStringValueX(rnode))) {
                        return true;
                    }
	        } while ((rnode = iterator.next()) != DTMAxisIterator.END);
            }
	    break;
	case LT:
	    // Assume we're comparing document order here
	    while ((rnode = iterator.next()) != DTMAxisIterator.END) {
		if (rnode > node) return true;
	    }
	    break;
	case GT:
	    // Assume we're comparing document order here
	    while ((rnode = iterator.next()) != DTMAxisIterator.END) {
		if (rnode < node) return true;
	    }
	    break;
	} 
	return(false);
    }

    /**
     * Utility function: node-set/number compare.
     */
    public static boolean compare(DTMAxisIterator left, final double rnumber,
				  final int op, DOM dom) {
	int node;
	//left.reset();

	switch (op) {
	case EQ:
	    while ((node = left.next()) != DTMAxisIterator.END) {
		if (numberF(dom.getStringValueX(node), dom) == rnumber)
		    return true;
	    }
	    break;

	case NE:
	    while ((node = left.next()) != DTMAxisIterator.END) {
		if (numberF(dom.getStringValueX(node), dom) != rnumber)
		    return true;
	    }
	    break;

	case GT:
	    while ((node = left.next()) != DTMAxisIterator.END) {
		if (numberF(dom.getStringValueX(node), dom) > rnumber)
		    return true;
	    }
	    break;

	case LT:
	    while ((node = left.next()) != DTMAxisIterator.END) {
		if (numberF(dom.getStringValueX(node), dom) < rnumber)
		    return true;
	    }
	    break;

	case GE:
	    while ((node = left.next()) != DTMAxisIterator.END) {
		if (numberF(dom.getStringValueX(node), dom) >= rnumber)
		    return true;
	    }
	    break;

	case LE:
	    while ((node = left.next()) != DTMAxisIterator.END) {
		if (numberF(dom.getStringValueX(node), dom) <= rnumber)
		    return true;
	    }
	    break;

	default:
	    runTimeError(RUN_TIME_INTERNAL_ERR, "compare()");
	}

	return false;
    }

    /**
     * Utility function: node-set/string comparison. 
     */
    public static boolean compare(DTMAxisIterator left, final String rstring,
				  int op, DOM dom) {
	int node;
	//left.reset();
	while ((node = left.next()) != DTMAxisIterator.END) {
	    if (compareStrings(dom.getStringValueX(node), rstring, op, dom)) {
		return true;
	    }
	}
	return false;
    }


    public static boolean compare(Object left, Object right,
				  int op, DOM dom) 
    { 
	boolean result = false;
	boolean hasSimpleArgs = hasSimpleType(left) && hasSimpleType(right);

	if (op != EQ && op != NE) {
	    // If node-boolean comparison -> convert node to boolean
	    if (left instanceof Node || right instanceof Node) {
		if (left instanceof Boolean) {
		    right = new Boolean(booleanF(right));
		    hasSimpleArgs = true;
		}
		if (right instanceof Boolean) {
		    left = new Boolean(booleanF(left));
		    hasSimpleArgs = true;
		}
	    }

	    if (hasSimpleArgs) {
		switch (op) {
		case GT:
		    return numberF(left, dom) > numberF(right, dom);
		    
		case LT:
		    return numberF(left, dom) < numberF(right, dom);
		    
		case GE:
		    return numberF(left, dom) >= numberF(right, dom);
		    
		case LE:
		    return numberF(left, dom) <= numberF(right, dom);
		    
		default:
		    runTimeError(RUN_TIME_INTERNAL_ERR, "compare()");
		}
	    }
	    // falls through
	}

	if (hasSimpleArgs) {
	    if (left instanceof Boolean || right instanceof Boolean) {
		result = booleanF(left) == booleanF(right);
	    }
	    else if (left instanceof Double || right instanceof Double ||
		     left instanceof Integer || right instanceof Integer) {
		result = numberF(left, dom) == numberF(right, dom);
	    }
	    else { // compare them as strings
		result = stringF(left, dom).equals(stringF(right, dom));
	    }

	    if (op == Operators.NE) {
		result = !result;
	    }
	}
	else {
	    if (left instanceof Node) {
		left = new SingletonIterator(((Node)left).node);
	    }
	    if (right instanceof Node) {
		right = new SingletonIterator(((Node)right).node);
	    }

	    if (hasSimpleType(left) ||
		left instanceof DOM && right instanceof DTMAxisIterator) {
		// swap operands
		final Object temp = right; right = left; left = temp;
	    }

	    if (left instanceof DOM) {
		if (right instanceof Boolean) {
		    result = ((Boolean)right).booleanValue();
		    return result == (op == Operators.EQ);
		}

		final String sleft = ((DOM)left).getStringValue();

		if (right instanceof Number) {
		    result = ((Number)right).doubleValue() ==
			stringToReal(sleft);
		}
		else if (right instanceof String) {
		    result = sleft.equals((String)right);
		}
		else if (right instanceof DOM) {
		    result = sleft.equals(((DOM)right).getStringValue());
		}

		if (op == Operators.NE) {
		    result = !result;
		}
		return result;
	    }

	    // Next, node-set/t for t in {real, string, node-set, result-tree}

	    DTMAxisIterator iter = ((DTMAxisIterator)left).reset();

	    if (right instanceof DTMAxisIterator) {
		result = compare(iter, (DTMAxisIterator)right, op, dom);
	    }
	    else if (right instanceof String) {
		result = compare(iter, (String)right, op, dom);
	    }	
	    else if (right instanceof Number) {
		final double temp = ((Number)right).doubleValue();
		result = compare(iter, temp, op, dom);
	    }
	    else if (right instanceof Boolean) {
		boolean temp = ((Boolean)right).booleanValue();
		result = (iter.reset().next() != DTMAxisIterator.END) == temp;
	    }
	    else if (right instanceof DOM) {
		result = compare(iter, ((DOM)right).getStringValue(),
				 op, dom);
	    }
	    else if (right == null) {
		return(false);
	    }
	    else {
		final String className = right.getClass().getName();
		runTimeError(INVALID_ARGUMENT_ERR, className, "compare()");
	    }
	}
	return result;
    }

    /**
     * Utility function: used to test context node's language
     */
    public static boolean testLanguage(String testLang, DOM dom, int node) {
	// language for context node (if any)
	String nodeLang = dom.getLanguage(node);
	if (nodeLang == null)
	    return(false);
	else
	    nodeLang = nodeLang.toLowerCase();

	// compare context node's language agains test language
	testLang = testLang.toLowerCase();
	if (testLang.length() == 2) {
	    return(nodeLang.startsWith(testLang));
	}
	else {
	    return(nodeLang.equals(testLang));
	}
    }

    private static boolean hasSimpleType(Object obj) {
	return obj instanceof Boolean || obj instanceof Double ||
	    obj instanceof Integer || obj instanceof String ||
	    obj instanceof Node || obj instanceof DOM; 
    }

    /**
     * Utility function: used in StringType to convert a string to a real.
     */
    public static double stringToReal(String s) {
	try {
	    return Double.valueOf(s).doubleValue();
	}
	catch (NumberFormatException e) {
	    return Double.NaN;
	}
    }

    /**
     * Utility function: used in StringType to convert a string to an int.
     */
    public static int stringToInt(String s) {
	try {
	    return Integer.parseInt(s);
	}
	catch (NumberFormatException e) {
	    return(-1); // ???
	}
    }

    private static double lowerBounds = 0.001;
    private static double upperBounds = 10000000;
    private static DecimalFormat defaultFormatter;
    private static String defaultPattern = "";

    static {
	NumberFormat f = NumberFormat.getInstance(Locale.getDefault());
	// set max fraction digits so that truncation does not occur,
	// see conf test string134
	f.setMaximumFractionDigits(Integer.MAX_VALUE);
	defaultFormatter = (f instanceof DecimalFormat) ?
	    (DecimalFormat) f : new DecimalFormat();
	defaultFormatter.setGroupingUsed(false);
    }

    /**
     * Utility function: used in RealType to convert a real to a string.
     * Removes the decimal if null.
     */
    public static String realToString(double d) {
	final double m = Math.abs(d);
	if ((m >= lowerBounds) && (m < upperBounds)) {
	    final String result = Double.toString(d);
	    final int length = result.length();
	    // Remove leading zeros.
	    if ((result.charAt(length-2) == '.') &&
		(result.charAt(length-1) == '0'))
		return result.substring(0, length-2);
	    else
		return result;
	}
	else {
	    if (Double.isNaN(d) || Double.isInfinite(d))
		return(Double.toString(d));
	    return formatNumber(d, defaultPattern, defaultFormatter);
	}
    }

    /**
     * Utility function: used in RealType to convert a real to an integer
     */
    public static int realToInt(double d) {
	return (int)d;
    }

    /**
     * Utility function: used to format/adjust  a double to a string. The 
     * DecimalFormat object comes from the 'format_symbols' hashtable in 
     * AbstractTranslet.
     */
    private static FieldPosition _fieldPosition = new FieldPosition(0);

    public static String formatNumber(double number, String pattern,
				      DecimalFormat formatter) {
        // bugzilla fix 12813 
	if (formatter == null) {
	    formatter = defaultFormatter;
	}
	try {
	    StringBuffer result = new StringBuffer();
	    if (pattern != defaultPattern) {
		formatter.applyLocalizedPattern(pattern);
	    }

	    //------------------------------------------------------
 	    // bug fix # 9179 - make sure localized pattern contains
	    //   a leading zero before decimal, handle cases where  
	    //   decimal is in position zero, and >= to 1. 
	    //   localized pattern is ###.### convert to ##0.###
	    //   localized pattern is .###    convert to 0.###
	    //------------------------------------------------------
	    String localizedPattern = formatter.toPattern();
	    int index = localizedPattern.indexOf('.');
	    if ( index >= 1  && localizedPattern.charAt(index-1) == '#' ) {
		//insert a zero before the decimal point in the pattern
		StringBuffer newpattern = new StringBuffer();
		newpattern.append(localizedPattern.substring(0, index-1));
                newpattern.append("0");
                newpattern.append(localizedPattern.substring(index));
		formatter.applyLocalizedPattern(newpattern.toString());
	    } else if (index == 0) {
                // insert a zero before decimal point in pattern
                StringBuffer newpattern = new StringBuffer();
                newpattern.append("0");
                newpattern.append(localizedPattern);
		formatter.applyLocalizedPattern(newpattern.toString());
            }

	    formatter.format(number, result, _fieldPosition);
	    return(result.toString());
	}
	catch (IllegalArgumentException e) {
	    runTimeError(FORMAT_NUMBER_ERR, Double.toString(number), pattern);
	    return(EMPTYSTRING);
	}
    }
    
    /**
     * Utility function: used to convert references to node-sets. If the
     * obj is an instanceof Node then create a singleton iterator.
     */
    public static DTMAxisIterator referenceToNodeSet(Object obj) {
	// Convert var/param -> node
	if (obj instanceof Node) {
	    return(new SingletonIterator(((Node)obj).node));
	}
	// Convert var/param -> node-set
	else if (obj instanceof DTMAxisIterator) {
	    return(((DTMAxisIterator)obj).cloneIterator());
	}
	else {
	    final String className = obj.getClass().getName();
	    runTimeError(DATA_CONVERSION_ERR, "reference", className);
	    return null;
	}
    }
    
    /**
     * Utility function: used to convert reference to org.w3c.dom.NodeList.
     */
    public static NodeList referenceToNodeList(Object obj, DOM dom) {
        if (obj instanceof Node || obj instanceof DTMAxisIterator) {
            DTMAxisIterator iter = referenceToNodeSet(obj);
            return dom.makeNodeList(iter);
        }
        else if (obj instanceof DOM) {
          dom = (DOM)obj;
          return dom.makeNodeList(DTMDefaultBase.ROOTNODE);
        }
	else {
	    final String className = obj.getClass().getName();
	    runTimeError(DATA_CONVERSION_ERR, "reference", className);
	    return null;
	}
    }

    /**
     * Utility function: used to convert reference to org.w3c.dom.Node.
     */
    public static org.w3c.dom.Node referenceToNode(Object obj, DOM dom) {
        if (obj instanceof Node || obj instanceof DTMAxisIterator) {
            DTMAxisIterator iter = referenceToNodeSet(obj);
            return dom.makeNode(iter);
        }
        else if (obj instanceof DOM) {
          dom = (DOM)obj;
          DTMAxisIterator iter = dom.getChildren(DTMDefaultBase.ROOTNODE);
          return dom.makeNode(iter);
        }
	else {
	    final String className = obj.getClass().getName();
	    runTimeError(DATA_CONVERSION_ERR, "reference", className);
	    return null;
	}
    }
    
    /**
     * Utility function used to convert a w3c Node into an internal DOM iterator. 
     */
    public static DTMAxisIterator node2Iterator(org.w3c.dom.Node node,
	Translet translet, DOM dom) 
    {
        final org.w3c.dom.Node inNode = node;
        // Create a dummy NodeList which only contains the given node to make 
        // use of the nodeList2Iterator() interface.
        org.w3c.dom.NodeList nodelist = new org.w3c.dom.NodeList() {            
            public int getLength() {
                return 1;
            }
            
            public org.w3c.dom.Node item(int index) {
                if (index == 0)
                    return inNode;
                else
                    return null;
            }
        };
        
        return nodeList2Iterator(nodelist, translet, dom);
    }
    
    /**
     * Utility function used to copy a node list to be under a parent node.
     */
    private static void copyNodes(org.w3c.dom.NodeList nodeList, 
	org.w3c.dom.Document doc, org.w3c.dom.Node parent)
    {
        final int size = nodeList.getLength();

          // copy Nodes from NodeList into new w3c DOM
        for (int i = 0; i < size; i++) 
        {
            org.w3c.dom.Node curr = nodeList.item(i);
            int nodeType = curr.getNodeType();
            String value = null;
            try {
                value = curr.getNodeValue();
            } catch (DOMException ex) {
                runTimeError(RUN_TIME_INTERNAL_ERR, ex.getMessage());
                return;
            }
            
            String nodeName = curr.getNodeName();
            org.w3c.dom.Node newNode = null; 
            switch (nodeType){
                case org.w3c.dom.Node.ATTRIBUTE_NODE:
                     newNode = doc.createAttributeNS(curr.getNamespaceURI(), 
			nodeName);
                     break;
                case org.w3c.dom.Node.CDATA_SECTION_NODE: 
                     newNode = doc.createCDATASection(value);
                     break;
                case org.w3c.dom.Node.COMMENT_NODE: 
                     newNode = doc.createComment(value);
                     break;
                case org.w3c.dom.Node.DOCUMENT_FRAGMENT_NODE: 
                     newNode = doc.createDocumentFragment();
                     break;
                case org.w3c.dom.Node.DOCUMENT_NODE:
                     newNode = doc.createElementNS(null, "__document__");
                     copyNodes(curr.getChildNodes(), doc, newNode);
                     break;
                case org.w3c.dom.Node.DOCUMENT_TYPE_NODE:
                     // nothing?
                     break;
                case org.w3c.dom.Node.ELEMENT_NODE: 
                     // For Element node, also copy the children and the 
		     // attributes.
                     org.w3c.dom.Element element = doc.createElementNS(
			curr.getNamespaceURI(), nodeName);
                     if (curr.hasAttributes())
                     {
                       org.w3c.dom.NamedNodeMap attributes = curr.getAttributes();
                       for (int k = 0; k < attributes.getLength(); k++) {
                         org.w3c.dom.Node attr = attributes.item(k);
                         element.setAttribute(attr.getNodeName(), 
			    attr.getNodeValue());
                       }
                     }
                     copyNodes(curr.getChildNodes(), doc, element);
                     newNode = element;
                     break;
                case org.w3c.dom.Node.ENTITY_NODE: 
                     // nothing ? 
                     break;
                case org.w3c.dom.Node.ENTITY_REFERENCE_NODE: 
                     newNode = doc.createEntityReference(nodeName);
                     break;
                case org.w3c.dom.Node.NOTATION_NODE: 
                     // nothing ? 
                     break;
                case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE: 
                     newNode = doc.createProcessingInstruction(nodeName,
                        value);
                     break;
                case org.w3c.dom.Node.TEXT_NODE: 
                     newNode = doc.createTextNode(value);
                     break;
            }
            try {
                parent.appendChild(newNode);
            } catch (DOMException e) {
                runTimeError(RUN_TIME_INTERNAL_ERR, e.getMessage());
                return;
            }           
        }
    }

    /**
     * Utility function used to convert a w3c NodeList into a internal
     * DOM iterator. 
     */
    public static DTMAxisIterator nodeList2Iterator(
                                        org.w3c.dom.NodeList nodeList,
                                    	Translet translet, DOM dom) 
    {
	// w3c NodeList -> w3c DOM
	DocumentBuilderFactory dfac = DocumentBuilderFactory.newInstance();
	DocumentBuilder docbldr = null;
	try {
	    docbldr = dfac.newDocumentBuilder();
	} catch (javax.xml.parsers.ParserConfigurationException e) {
	    runTimeError(RUN_TIME_INTERNAL_ERR, e.getMessage());
            return null;

	}
	// create new w3c DOM
	Document doc = docbldr.newDocument();	
        org.w3c.dom.Node topElementNode = 
            doc.appendChild(doc.createElementNS("", "__top__"));

        // Copy all the nodes in the nodelist to be under the top element
        copyNodes(nodeList, doc, topElementNode);

        // w3cDOM -> DTM -> DOMImpl
	if (dom instanceof MultiDOM) {
            final MultiDOM multiDOM = (MultiDOM) dom;

	    DTMDefaultBase dtm = (DTMDefaultBase)((DOMAdapter)multiDOM.getMain()).getDOMImpl();
	    DTMManager dtmManager = dtm.getManager();
	    
	    SAXImpl idom = (SAXImpl)dtmManager.getDTM(new DOMSource(doc), false,
						      null, true, false);
	    // Create DOMAdapter and register with MultiDOM
	    DOMAdapter domAdapter = new DOMAdapter(idom, 
                translet.getNamesArray(),
		translet.getNamespaceArray());
            multiDOM.addDOMAdapter(domAdapter);

	    DTMAxisIterator iter1 = idom.getAxisIterator(Axis.CHILD);
	    DTMAxisIterator iter2 = idom.getAxisIterator(Axis.CHILD);
            DTMAxisIterator iter = new AbsoluteIterator(
                new StepIterator(iter1, iter2));

 	    iter.setStartNode(DTMDefaultBase.ROOTNODE);
	    return iter;
	}
        else {
	    runTimeError(RUN_TIME_INTERNAL_ERR, "nodeList2Iterator()");
	    return null;
        }
    }

    /**
     * Utility function used to convert references to DOMs. 
     */
    public static DOM referenceToResultTree(Object obj) {
	try {
	    return ((DOM) obj);
	}
	catch (IllegalArgumentException e) {
	    final String className = obj.getClass().getName();
	    runTimeError(DATA_CONVERSION_ERR, "reference", className);
	    return null;
	}
    }

    /**
     * Utility function: used with nth position filters to convert a sequence
     * of nodes to just one single node (the one at position n).
     */
    public static DTMAxisIterator getSingleNode(DTMAxisIterator iterator) {
	int node = iterator.next();
	return(new SingletonIterator(node));
    }

    /**
     * Utility function: used in xsl:copy.
     */
    private static char[] _characterArray = new char[32];

    public static void copy(Object obj,
 			    SerializationHandler handler,
			    int node,
			    DOM dom) {
	try {
	    if (obj instanceof DTMAxisIterator) 
      {
		DTMAxisIterator iter = (DTMAxisIterator) obj;
		dom.copy(iter.reset(), handler);
	    }
	    else if (obj instanceof Node) {
		dom.copy(((Node) obj).node, handler);
	    }
	    else if (obj instanceof DOM) {
		//((DOM)obj).copy(((org.apache.xml.dtm.ref.DTMDefaultBase)((DOMAdapter)obj).getDOMImpl()).getDocument(), handler);
		DOM newDom = (DOM)obj;
		newDom.copy(newDom.getDocument(), handler);
	    }
	    else {
		String string = obj.toString();		// or call stringF()
		final int length = string.length();
		if (length > _characterArray.length)
		    _characterArray = new char[length];
		string.getChars(0, length, _characterArray, 0);
		handler.characters(_characterArray, 0, length);
	    }
	}
	catch (SAXException e) {
	    runTimeError(RUN_TIME_COPY_ERR);
	}
    }

    /**
     * Utility function for the implementation of xsl:element.
     */
    public static String startXslElement(String qname, String namespace,
	SerializationHandler handler, DOM dom, int node)
    {
	try {
	    // Get prefix from qname
	    String prefix;
	    final int index = qname.indexOf(':');

	    if (index > 0) {
		prefix = qname.substring(0, index);

		// Handle case when prefix is not known at compile time
		if (namespace == null || namespace.length() == 0) {
		    namespace = dom.lookupNamespace(node, prefix);
		}

		handler.startElement(namespace, qname.substring(index+1),
                                     qname);
		handler.namespaceAfterStartElement(prefix, namespace); 
	    }
	    else {
		// Need to generate a prefix?
		if (namespace != null && namespace.length() > 0) {
		    prefix = generatePrefix();
		    qname = prefix + ':' + qname;   
		    handler.startElement(namespace, qname, qname);   
		    handler.namespaceAfterStartElement(prefix, namespace);
		}
		else {
		    handler.startElement(null, null, qname);   
		}
	    }
	}
	catch (SAXException e) {
	    throw new RuntimeException(e.getMessage());
	}

	return qname;
    }

    /**
     * This function is used in the execution of xsl:element
     */
    public static String getPrefix(String qname) {
	final int index = qname.indexOf(':');
	return (index > 0) ? qname.substring(0, index) : null;
    }

    /**
     * This function is used in the execution of xsl:element
     */
    private static int prefixIndex = 0;		// not thread safe!!
    public static String generatePrefix() {
	return ("ns" + prefixIndex++);
    }

    public static final String RUN_TIME_INTERNAL_ERR =
                                           "RUN_TIME_INTERNAL_ERR";
    public static final String RUN_TIME_COPY_ERR =
                                           "RUN_TIME_COPY_ERR";
    public static final String DATA_CONVERSION_ERR =
                                           "DATA_CONVERSION_ERR";
    public static final String EXTERNAL_FUNC_ERR =
                                           "EXTERNAL_FUNC_ERR";
    public static final String EQUALITY_EXPR_ERR =
                                           "EQUALITY_EXPR_ERR";
    public static final String INVALID_ARGUMENT_ERR =
                                           "INVALID_ARGUMENT_ERR";
    public static final String FORMAT_NUMBER_ERR =
                                           "FORMAT_NUMBER_ERR";
    public static final String ITERATOR_CLONE_ERR =
                                           "ITERATOR_CLONE_ERR";
    public static final String AXIS_SUPPORT_ERR =
                                           "AXIS_SUPPORT_ERR";
    public static final String TYPED_AXIS_SUPPORT_ERR =
                                           "TYPED_AXIS_SUPPORT_ERR";
    public static final String STRAY_ATTRIBUTE_ERR =
                                           "STRAY_ATTRIBUTE_ERR"; 
    public static final String STRAY_NAMESPACE_ERR =
                                           "STRAY_NAMESPACE_ERR";
    public static final String NAMESPACE_PREFIX_ERR =
                                           "NAMESPACE_PREFIX_ERR";
    public static final String DOM_ADAPTER_INIT_ERR =
                                           "DOM_ADAPTER_INIT_ERR";
    public static final String PARSER_DTD_SUPPORT_ERR =
                                           "PARSER_DTD_SUPPORT_ERR";
    public static final String NAMESPACES_SUPPORT_ERR =
                                           "NAMESPACES_SUPPORT_ERR";
    public static final String CANT_RESOLVE_RELATIVE_URI_ERR =
                                           "CANT_RESOLVE_RELATIVE_URI_ERR";

    // All error messages are localized and are stored in resource bundles.
    protected static ResourceBundle m_bundle;
    
    public final static String ERROR_MESSAGES_KEY = "error-messages";

    static {
	String resource = "org.apache.xalan.xsltc.runtime.ErrorMessages";
	m_bundle = ResourceBundle.getBundle(resource);
    }

    /**
     * Print a run-time error message.
     */
    public static void runTimeError(String code) {
	throw new RuntimeException(m_bundle.getString(code));
    }

    public static void runTimeError(String code, Object[] args) {
	final String message = MessageFormat.format(m_bundle.getString(code),
                                                    args);
	throw new RuntimeException(message);
    }

    public static void runTimeError(String code, Object arg0) {
	runTimeError(code, new Object[]{ arg0 } );
    }

    public static void runTimeError(String code, Object arg0, Object arg1) {
	runTimeError(code, new Object[]{ arg0, arg1 } );
    }

    public static void consoleOutput(String msg) {
	System.out.println(msg);
    }

    /**
     * Replace a certain character in a string with a new substring.
     */
    public static String replace(String base, char ch, String str) {
	return (base.indexOf(ch) < 0) ? base : 
	    replace(base, String.valueOf(ch), new String[] { str });
    }

    public static String replace(String base, String delim, String[] str) {
	final int len = base.length();
	final StringBuffer result = new StringBuffer();

	for (int i = 0; i < len; i++) {
	    final char ch = base.charAt(i);
	    final int k = delim.indexOf(ch);

	    if (k >= 0) {
		result.append(str[k]);
	    }
	    else {
		result.append(ch);
	    }
	}
	return result.toString();
    }

    //-- End utility functions
}
