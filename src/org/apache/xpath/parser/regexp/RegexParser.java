/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights 
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
 * 4. The names "Xerces" and "Apache Software Foundation" must
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
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xpath.parser.regexp;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * A Regular Expression Parser.
 */
class RegexParser {
    static final int T_CHAR = 0;
    static final int T_EOF = 1;
    static final int T_OR = 2;                  // '|'
    static final int T_STAR = 3;                // '*'
    static final int T_PLUS = 4;                // '+'
    static final int T_QUESTION = 5;            // '?'
    static final int T_LPAREN = 6;              // '('
    static final int T_RPAREN = 7;              // ')'
    static final int T_DOT = 8;                 // '.'
    static final int T_LBRACKET = 9;            // '['
    static final int T_BACKSOLIDUS = 10;        // '\'
    static final int T_CARET = 11;              // '^'
    static final int T_DOLLAR = 12;             // '$'
    static final int T_LPAREN2 = 13;            // '(?:'
    static final int T_LOOKAHEAD = 14;          // '(?='
    static final int T_NEGATIVELOOKAHEAD = 15;  // '(?!'
    static final int T_LOOKBEHIND = 16;         // '(?<='
    static final int T_NEGATIVELOOKBEHIND = 17; // '(?<!'
    static final int T_INDEPENDENT = 18;        // '(?>'
    static final int T_SET_OPERATIONS = 19;     // '(?['
    static final int T_POSIX_CHARCLASS_START = 20; // '[:' in a character class
    static final int T_COMMENT = 21;            // '(?#'
    static final int T_MODIFIERS = 22;          // '(?' [\-,a-z,A-Z]
    static final int T_CONDITION = 23;          // '(?('
    static final int T_XMLSCHEMA_CC_SUBTRACTION = 24; // '-[' in a character class

    static class ReferencePosition {
        int refNumber;
        int position;
        ReferencePosition(int n, int pos) {
            this.refNumber = n;
            this.position = pos;
        }
    }

    int offset;
    String regex;
    int regexlen;
    int options;
    ResourceBundle resources;
    int chardata;
    int nexttoken;
    static protected final int S_NORMAL = 0;
    static protected final int S_INBRACKETS = 1;
    static protected final int S_INXBRACKETS = 2;
    int context = S_NORMAL;
    int parennumber = 1;
    boolean hasBackReferences;
    Vector references = null;

    public RegexParser() {
        this.setLocale(Locale.getDefault());
    }
    public RegexParser(Locale locale) {
        this.setLocale(locale);
    }

    public void setLocale(Locale locale) {
        try {
            this.resources = ResourceBundle.getBundle("org.apache.xerces.impl.xpath.regex.message", locale);
        } catch (MissingResourceException mre) {
            throw new RuntimeException("Installation Problem???  Couldn't load messages: "
                                       +mre.getMessage());
        }
    }

    final ParseException ex(String key, int loc) {
        return new ParseException(this.resources.getString(key), loc);
    }

    private final boolean isSet(int flag) {
        return (this.options & flag) == flag;
    }

    synchronized Token parse(String regex, int options) throws ParseException {
        this.options = options;
        this.offset = 0;
        this.setContext(S_NORMAL);
        this.parennumber = 1;
        this.hasBackReferences = false;
        this.regex = regex;
        if (this.isSet(RegularExpression.EXTENDED_COMMENT))
            this.regex = REUtil.stripExtendedComment(this.regex);
        this.regexlen = this.regex.length();


        this.next();
        Token ret = this.parseRegex();
        if (this.offset != this.regexlen)
            throw ex("parser.parse.1", this.offset);
        if (this.references != null) {
            for (int i = 0;  i < this.references.size();  i ++) {
                ReferencePosition position = (ReferencePosition)this.references.elementAt(i);
                if (this.parennumber <= position.refNumber)
                    throw ex("parser.parse.2", position.position);
            }
            this.references.removeAllElements();
        }
        return ret;
    }

    /*
    public RegularExpression createRegex(String regex, int options) throws ParseException {
        Token tok = this.parse(regex, options);
        return new RegularExpression(regex, tok, this.parennumber, this.hasBackReferences, options);
    }
    */

    protected final void setContext(int con) {
        this.context = con;
    }

    final int read() {
        return this.nexttoken;
    }

    final void next() {
        if (this.offset >= this.regexlen) {
            this.chardata = -1;
            this.nexttoken = T_EOF;
            return;
        }

        int ret;
        int ch = this.regex.charAt(this.offset++);
        this.chardata = ch;

        if (this.context == S_INBRACKETS) {
            // In a character class, this.chardata has one character, that is to say,
            // a pair of surrogates is composed and stored to this.chardata.
            switch (ch) {
              case '\\':
                ret = T_BACKSOLIDUS;
                if (this.offset >= this.regexlen)
                    throw ex("parser.next.1", this.offset-1);
                this.chardata = this.regex.charAt(this.offset++);
                break;

              case '-':
                if (this.isSet(RegularExpression.XMLSCHEMA_MODE)
                    && this.offset < this.regexlen && this.regex.charAt(this.offset) == '[') {
                    this.offset++;
                    ret = T_XMLSCHEMA_CC_SUBTRACTION;
                } else
                    ret = T_CHAR;
                break;

              case '[':
                if (!this.isSet(RegularExpression.XMLSCHEMA_MODE)
                    && this.offset < this.regexlen && this.regex.charAt(this.offset) == ':') {
                    this.offset++;
                    ret = T_POSIX_CHARCLASS_START;
                    break;
                } // Through down
              default:
                if (REUtil.isHighSurrogate(ch) && this.offset < this.regexlen) {
                    int low = this.regex.charAt(this.offset);
                    if (REUtil.isLowSurrogate(low)) {
                        this.chardata = REUtil.composeFromSurrogates(ch, low);
                        this.offset ++;
                    }
                }
                ret = T_CHAR;
            }
            this.nexttoken = ret;
            return;
        }

        switch (ch) {
          case '|': ret = T_OR;             break;
          case '*': ret = T_STAR;           break;
          case '+': ret = T_PLUS;           break;
          case '?': ret = T_QUESTION;       break;
          case ')': ret = T_RPAREN;         break;
          case '.': ret = T_DOT;            break;
          case '[': ret = T_LBRACKET;       break;
          case '^': ret = T_CARET;          break;
          case '$': ret = T_DOLLAR;         break;
          case '(':
            ret = T_LPAREN;
            if (this.offset >= this.regexlen)
                break;
            if (this.regex.charAt(this.offset) != '?')
                break;
            if (++this.offset >= this.regexlen)
                throw ex("parser.next.2", this.offset-1);
            ch = this.regex.charAt(this.offset++);
            switch (ch) {
              case ':':  ret = T_LPAREN2;            break;
              case '=':  ret = T_LOOKAHEAD;          break;
              case '!':  ret = T_NEGATIVELOOKAHEAD;  break;
              case '[':  ret = T_SET_OPERATIONS;     break;
              case '>':  ret = T_INDEPENDENT;        break;
              case '<':
                if (this.offset >= this.regexlen)
                    throw ex("parser.next.2", this.offset-3);
                ch = this.regex.charAt(this.offset++);
                if (ch == '=') {
                    ret = T_LOOKBEHIND;
                } else if (ch == '!') {
                    ret = T_NEGATIVELOOKBEHIND;
                } else
                    throw ex("parser.next.3", this.offset-3);
                break;
              case '#':
                while (this.offset < this.regexlen) {
                    ch = this.regex.charAt(this.offset++);
                    if (ch == ')')  break;
                }
                if (ch != ')')
                    throw ex("parser.next.4", this.offset-1);
                ret = T_COMMENT;
                break;
              default:
                if (ch == '-' || 'a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z') {// Options
                    this.offset --;
                    ret = T_MODIFIERS;
                    break;
                } else if (ch == '(') {         // conditional
                    ret = T_CONDITION;          // this.offsets points the next of '('.
                    break;
                }
                throw ex("parser.next.2", this.offset-2);
            }
            break;
            
          case '\\':
            ret = T_BACKSOLIDUS;
            if (this.offset >= this.regexlen)
                throw ex("parser.next.1", this.offset-1);
            this.chardata = this.regex.charAt(this.offset++);
            break;

          default:
            ret = T_CHAR;
            if (REUtil.isHighSurrogate(this.chardata) && this.offset < this.regexlen)
                this.chardata = REUtil.composeFromSurrogates(this.chardata,
                                                             this.regex.charAt(this.offset++));
        }
        this.nexttoken = ret;
    }

    /**
     * regex ::= term (`|` term)*
     * term ::= factor+
     * factor ::= ('^' | '$' | '\A' | '\Z' | '\z' | '\b' | '\B' | '\<' | '\>'
     *            | atom (('*' | '+' | '?' | minmax ) '?'? )?)
     *            | '(?=' regex ')'  | '(?!' regex ')'  | '(?&lt;=' regex ')'  | '(?&lt;!' regex ')'
     * atom ::= char | '.' | range | '(' regex ')' | '(?:' regex ')' | '\' [0-9]
     *          | '\w' | '\W' | '\d' | '\D' | '\s' | '\S' | category-block 
     */
    Token parseRegex() throws ParseException {
        Token tok = this.parseTerm();
        Token parent = null;
        while (this.read() == T_OR) {
            this.next();                    // '|'
            if (parent == null) {
                parent = Token.createUnion();
                parent.addChild(tok);
                tok = parent;
            }
            tok.addChild(this.parseTerm());
        }
        return tok;
    }

    /**
     * term ::= factor+
     */
    Token parseTerm() throws ParseException {
        int ch = this.read();
        if (ch == T_OR || ch == T_RPAREN || ch == T_EOF) {
            return Token.createEmpty();
        } else {
            Token tok = this.parseFactor();
            Token concat = null;
            while ((ch = this.read()) != T_OR && ch != T_RPAREN && ch != T_EOF) {
                if (concat == null) {
                    concat = Token.createConcat();
                    concat.addChild(tok);
                    tok = concat;
                }
                concat.addChild(this.parseFactor());
                //tok = Token.createConcat(tok, this.parseFactor());
            }
            return tok;
        }
    }

    // ----------------------------------------------------------------

    Token processCaret() throws ParseException {
        this.next();
        return Token.token_linebeginning;
    }
    Token processDollar() throws ParseException {
        this.next();
        return Token.token_lineend;
    }
    Token processLookahead() throws ParseException {
        this.next();
        Token tok = Token.createLook(Token.LOOKAHEAD, this.parseRegex());
        if (this.read() != T_RPAREN)  throw ex("parser.factor.1", this.offset-1);
        this.next();                            // ')'
        return tok;
    }
    Token processNegativelookahead() throws ParseException {
        this.next();
        Token tok = Token.createLook(Token.NEGATIVELOOKAHEAD, this.parseRegex());
        if (this.read() != T_RPAREN)  throw ex("parser.factor.1", this.offset-1);
        this.next();                            // ')'
        return tok;
    }
    Token processLookbehind() throws ParseException {
        this.next();
        Token tok = Token.createLook(Token.LOOKBEHIND, this.parseRegex());
        if (this.read() != T_RPAREN)  throw ex("parser.factor.1", this.offset-1);
        this.next();                            // ')'
        return tok;
    }
    Token processNegativelookbehind() throws ParseException {
        this.next();
        Token tok = Token.createLook(Token.NEGATIVELOOKBEHIND, this.parseRegex());
        if (this.read() != T_RPAREN)  throw ex("parser.factor.1", this.offset-1);
        this.next();                    // ')'
        return tok;
    }
    Token processBacksolidus_A() throws ParseException {
        this.next();
        return Token.token_stringbeginning;
    }
    Token processBacksolidus_Z() throws ParseException {
        this.next();
        return Token.token_stringend2;
    }
    Token processBacksolidus_z() throws ParseException {
        this.next();
        return Token.token_stringend;
    }
    Token processBacksolidus_b() throws ParseException {
        this.next();
        return Token.token_wordedge;
    }
    Token processBacksolidus_B() throws ParseException {
        this.next();
        return Token.token_not_wordedge;
    }
    Token processBacksolidus_lt() throws ParseException {
        this.next();
        return Token.token_wordbeginning;
    }
    Token processBacksolidus_gt() throws ParseException {
        this.next();
        return Token.token_wordend;
    }
    Token processStar(Token tok) throws ParseException {
        this.next();
        if (this.read() == T_QUESTION) {
            this.next();
            return Token.createNGClosure(tok);
        } else
            return Token.createClosure(tok);
    }
    Token processPlus(Token tok) throws ParseException {
        // X+ -> XX*
        this.next();
        if (this.read() == T_QUESTION) {
            this.next();
            return Token.createConcat(tok, Token.createNGClosure(tok));
        } else
            return Token.createConcat(tok, Token.createClosure(tok));
    }
    Token processQuestion(Token tok) throws ParseException {
        // X? -> X|
        this.next();
        Token par = Token.createUnion();
        if (this.read() == T_QUESTION) {
            this.next();
            par.addChild(Token.createEmpty());
            par.addChild(tok);
        } else {
            par.addChild(tok);
            par.addChild(Token.createEmpty());
        }
        return par;
    }
    boolean checkQuestion(int off) {
        return off < this.regexlen && this.regex.charAt(off) == '?';
    }
    Token processParen() throws ParseException {
        this.next();
        int p = this.parennumber++;
        Token tok = Token.createParen(this.parseRegex(), p);
        if (this.read() != T_RPAREN)  throw ex("parser.factor.1", this.offset-1);
        this.next();                            // Skips ')'
        return tok;
    }
    Token processParen2() throws ParseException {
        this.next();
        Token tok = Token.createParen(this.parseRegex(), 0);
        if (this.read() != T_RPAREN)  throw ex("parser.factor.1", this.offset-1);
        this.next();                            // Skips ')'
        return tok;
    }
    Token processCondition() throws ParseException {
                                                // this.offset points the next of '('
        if (this.offset+1 >= this.regexlen)  throw ex("parser.factor.4", this.offset);
                                                // Parses a condition.
        int refno = -1;
        Token condition = null;
        int ch = this.regex.charAt(this.offset);
        if ('1' <= ch && ch <= '9') {
            refno = ch-'0';
            this.hasBackReferences = true;
            if (this.references == null)  this.references = new Vector();
            this.references.addElement(new ReferencePosition(refno, this.offset));
            this.offset ++;
            if (this.regex.charAt(this.offset) != ')')  throw ex("parser.factor.1", this.offset);
            this.offset ++;
        } else {
            if (ch == '?')  this.offset --; // Points '('.
            this.next();
            condition = this.parseFactor();
            switch (condition.type) {
              case Token.LOOKAHEAD:
              case Token.NEGATIVELOOKAHEAD:
              case Token.LOOKBEHIND:
              case Token.NEGATIVELOOKBEHIND:
                break;
              case Token.ANCHOR:
                if (this.read() != T_RPAREN)  throw ex("parser.factor.1", this.offset-1);
                break;
              default:
                throw ex("parser.factor.5", this.offset);
            }
        }
                                                // Parses yes/no-patterns.
        this.next();
        Token yesPattern = this.parseRegex();
        Token noPattern = null;
        if (yesPattern.type == Token.UNION) {
            if (yesPattern.size() != 2)  throw ex("parser.factor.6", this.offset);
            noPattern = yesPattern.getChild(1);
            yesPattern = yesPattern.getChild(0);
        }
        if (this.read() != T_RPAREN)  throw ex("parser.factor.1", this.offset-1);
        this.next();
        return Token.createCondition(refno, condition, yesPattern, noPattern);
    }
    Token processModifiers() throws ParseException {
                                                // this.offset points the next of '?'.
                                                // modifiers ::= [imsw]* ('-' [imsw]*)? ':'
        int add = 0, mask = 0, ch = -1;
        while (this.offset < this.regexlen) {
            ch = this.regex.charAt(this.offset);
            int v = REUtil.getOptionValue(ch);
            if (v == 0)  break;                 // '-' or ':'?
            add |= v;
            this.offset ++;
        }
        if (this.offset >= this.regexlen)  throw ex("parser.factor.2", this.offset-1);
        if (ch == '-') {
            this.offset ++;
            while (this.offset < this.regexlen) {
                ch = this.regex.charAt(this.offset);
                int v = REUtil.getOptionValue(ch);
                if (v == 0)  break;             // ':'?
                mask |= v;
                this.offset ++;
            }
            if (this.offset >= this.regexlen)  throw ex("parser.factor.2", this.offset-1);
        }
        Token tok;
        if (ch == ':') {
            this.offset ++;
            this.next();
            tok = Token.createModifierGroup(this.parseRegex(), add, mask);
            if (this.read() != T_RPAREN)  throw ex("parser.factor.1", this.offset-1);
            this.next();
        } else if (ch == ')') {                 // such as (?-i)
            this.offset ++;
            this.next();
            tok = Token.createModifierGroup(this.parseRegex(), add, mask);
        } else
            throw ex("parser.factor.3", this.offset);

        return tok;
    }
    Token processIndependent() throws ParseException {
        this.next();
        Token tok = Token.createLook(Token.INDEPENDENT, this.parseRegex());
        if (this.read() != T_RPAREN)  throw ex("parser.factor.1", this.offset-1);
        this.next();                            // Skips ')'
        return tok;
    }
    Token processBacksolidus_c() throws ParseException {
        int ch2;                                // Must be in 0x0040-0x005f
        if (this.offset >= this.regexlen
            || ((ch2 = this.regex.charAt(this.offset++)) & 0xffe0) != 0x0040)
            throw ex("parser.atom.1", this.offset-1);
        this.next();
        return Token.createChar(ch2-0x40);
    }
    Token processBacksolidus_C() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }
    Token processBacksolidus_i() throws ParseException {
        Token tok = Token.createChar('i');
        this.next();
        return tok;
    }
    Token processBacksolidus_I() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }
    Token processBacksolidus_g() throws ParseException {
        this.next();
        return Token.getGraphemePattern();
    }
    Token processBacksolidus_X() throws ParseException {
        this.next();
        return Token.getCombiningCharacterSequence();
    }
    Token processBackreference() throws ParseException {
        int refnum = this.chardata-'0';
        Token tok = Token.createBackReference(refnum);
        this.hasBackReferences = true;
        if (this.references == null)  this.references = new Vector();
        this.references.addElement(new ReferencePosition(refnum, this.offset-2));
        this.next();
        return tok;
    }

    // ----------------------------------------------------------------

    /**
     * factor ::= ('^' | '$' | '\A' | '\Z' | '\z' | '\b' | '\B' | '\<' | '\>'
     *            | atom (('*' | '+' | '?' | minmax ) '?'? )?)
     *            | '(?=' regex ')'  | '(?!' regex ')'  | '(?&lt;=' regex ')'  | '(?&lt;!' regex ')'
     *            | '(?#' [^)]* ')'
     * minmax ::= '{' min (',' max?)? '}'
     * min ::= [0-9]+
     * max ::= [0-9]+
     */
    Token parseFactor() throws ParseException {        
        int ch = this.read();
        Token tok;
        switch (ch) {
          case T_CARET:         return this.processCaret();
          case T_DOLLAR:        return this.processDollar();
          case T_LOOKAHEAD:     return this.processLookahead();
          case T_NEGATIVELOOKAHEAD: return this.processNegativelookahead();
          case T_LOOKBEHIND:    return this.processLookbehind();
          case T_NEGATIVELOOKBEHIND: return this.processNegativelookbehind();

          case T_COMMENT:
            this.next();
            return Token.createEmpty();

          case T_BACKSOLIDUS:
            switch (this.chardata) {
              case 'A': return this.processBacksolidus_A();
              case 'Z': return this.processBacksolidus_Z();
              case 'z': return this.processBacksolidus_z();
              case 'b': return this.processBacksolidus_b();
              case 'B': return this.processBacksolidus_B();
              case '<': return this.processBacksolidus_lt();
              case '>': return this.processBacksolidus_gt();
            }
                                                // through down
        }
        tok = this.parseAtom();
        ch = this.read();
        switch (ch) {
          case T_STAR:  return this.processStar(tok);
          case T_PLUS:  return this.processPlus(tok);
          case T_QUESTION: return this.processQuestion(tok);
          case T_CHAR:
            if (this.chardata == '{') {
                                                // this.offset -> next of '{'
                int off = this.offset;
                int min = 0, max = -1;
                if (off >= this.regexlen)  break;
                ch = this.regex.charAt(off++);
                if (ch != ',' && (ch < '0' || ch > '9'))  break;
                if (ch != ',') {                // 0-9
                    min = ch-'0';
                    while (off < this.regexlen
                           && (ch = this.regex.charAt(off++)) >= '0' && ch <= '9') {
                        min = min*10 +ch-'0';
                        ch = -1;
                    }
                    if (ch < 0)  break;
                }
                //if (off >= this.regexlen)  break;
                max = min;
                if (ch == ',') {
                    if (off >= this.regexlen
                        || ((ch = this.regex.charAt(off++)) < '0' || ch > '9')
                        && ch != '}')
                        break;
                    if (ch == '}') {
                        max = -1;           // {min,}
                    } else {
                        max = ch-'0';       // {min,max}
                        while (off < this.regexlen
                               && (ch = this.regex.charAt(off++)) >= '0'
                               && ch <= '9') {
                            max = max*10 +ch-'0';
                            ch = -1;
                        }
                        if (ch < 0)  break;
                        //if (min > max)
                        //    throw new ParseException("parseFactor(): min > max: "+min+", "+max);
                    }
                }
                if (ch != '}')  break;
                                                // off -> next of '}'
                if (this.checkQuestion(off)) {
                    tok = Token.createNGClosure(tok);
                    this.offset = off+1;
                } else {
                    tok = Token.createClosure(tok);
                    this.offset = off;
                }
                tok.setMin(min);
                tok.setMax(max);
                //System.err.println("CLOSURE: "+min+", "+max);
                this.next();
            }
        }
        return tok;
    }

    /**
     * atom ::= char | '.' | char-class | '(' regex ')' | '(?:' regex ')' | '\' [0-9]
     *          | '\w' | '\W' | '\d' | '\D' | '\s' | '\S' | category-block
     *          | '(?>' regex ')'
     * char ::= '\\' | '\' [efnrt] | bmp-code | character-1
     */
    Token parseAtom() throws ParseException {
        int ch = this.read();
        Token tok = null;
        switch (ch) {
          case T_LPAREN:        return this.processParen();
          case T_LPAREN2:       return this.processParen2(); // '(?:'
          case T_CONDITION:     return this.processCondition(); // '(?('
          case T_MODIFIERS:     return this.processModifiers(); // (?modifiers ... )
          case T_INDEPENDENT:   return this.processIndependent();
          case T_DOT:
            this.next();                    // Skips '.'
            tok = Token.token_dot;
            break;

            /**
             * char-class ::= '[' ( '^'? range ','?)+ ']'
             * range ::= '\d' | '\w' | '\s' | category-block | range-char
             *           | range-char '-' range-char
             * range-char ::= '\[' | '\]' | '\\' | '\' [,-efnrtv] | bmp-code | character-2
             * bmp-char ::= '\' 'u' [0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F]
             */
          case T_LBRACKET:      return this.parseCharacterClass(true);
          case T_SET_OPERATIONS: return this.parseSetOperations();

          case T_BACKSOLIDUS:
            switch (this.chardata) {
              case 'd':  case 'D':
              case 'w':  case 'W':
              case 's':  case 'S':
                tok = this.getTokenForShorthand(this.chardata);
                this.next();
                return tok;

              case 'e':  case 'f':  case 'n':  case 'r':
              case 't':  case 'u':  case 'v':  case 'x':
                {
                    int ch2 = this.decodeEscaped();
                    if (ch2 < 0x10000) {
                        tok = Token.createChar(ch2);
                    } else {
                        tok = Token.createString(REUtil.decomposeToSurrogates(ch2));
                    }
                }
                break;

              case 'c': return this.processBacksolidus_c();
              case 'C': return this.processBacksolidus_C();
              case 'i': return this.processBacksolidus_i();
              case 'I': return this.processBacksolidus_I();
              case 'g': return this.processBacksolidus_g();
              case 'X': return this.processBacksolidus_X();
              case '1':  case '2':  case '3':  case '4':
              case '5':  case '6':  case '7':  case '8':  case '9':
                return this.processBackreference();

              case 'P':
              case 'p':
                int pstart = this.offset;
                tok = processBacksolidus_pP(this.chardata);
                if (tok == null)  throw this.ex("parser.atom.5", pstart);
                break;

              default:
                tok = Token.createChar(this.chardata);
            }
            this.next();
            break;

          case T_CHAR:
            tok = Token.createChar(this.chardata);
            this.next();
            break;

          default:
            throw this.ex("parser.atom.4", this.offset-1);
        }
        return tok;
    }

    protected RangeToken processBacksolidus_pP(int c) throws ParseException {
        boolean positive = c == 'p';
        this.next();
        if (this.read() != T_CHAR)  throw this.ex("parser.atom.2", this.offset-1);
        RangeToken tok;
        switch (this.chardata) {
          case 'L':                             // Letter
            tok = Token.getRange("L", positive);  break;
          case 'M':                             // Mark
            tok = Token.getRange("M", positive);  break;
          case 'N':                             // Number
            tok = Token.getRange("N", positive);  break;
          case 'Z':                             // Separator
            tok = Token.getRange("Z", positive);  break;
          case 'C':                             // Other
            tok = Token.getRange("C", positive);  break;
          case 'P':                             // Punctuation
            tok = Token.getRange("P", positive);  break;
          case 'S':                             // Symbol
            tok = Token.getRange("S", positive);  break;
          case '{':
            // this.offset points the next of '{'.
            //pstart = this.offset;
            int namestart = this.offset;
            int nameend = this.regex.indexOf('}', namestart);
            if (nameend < 0)  throw this.ex("parser.atom.3", this.offset);
            this.offset = nameend+1;
            tok = Token.getRange(this.regex.substring(namestart, nameend), positive);
            /*
              if (this.isSet(RegularExpression.IGNORE_CASE))
              tok = RangeToken.createCaseInsensitiveToken(tok);
            */
            break;

          default:
            throw this.ex("parser.atom.2", this.offset-1);
        }
        return tok;
    }

    int processCIinCharacterClass(RangeToken tok, int c) {
        return this.decodeEscaped();
    }

    /**
     * char-class ::= '[' ( '^'? range ','?)+ ']'
     * range ::= '\d' | '\w' | '\s' | category-block | range-char
     *           | range-char '-' range-char
     * range-char ::= '\[' | '\]' | '\\' | '\' [,-efnrtv] | bmp-code | character-2
     * bmp-code ::= '\' 'u' [0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F]
     */
    protected RangeToken parseCharacterClass(boolean useNrange) throws ParseException {
        this.setContext(S_INBRACKETS);
        this.next();                            // '['
        boolean nrange = false;
        RangeToken base = null;
        RangeToken tok;
        if (this.read() == T_CHAR && this.chardata == '^') {
            nrange = true;
            this.next();                        // '^'
            if (useNrange) {
                tok = Token.createNRange();
            } else {
                base = Token.createRange();
                base.addRange(0, Token.UTF16_MAX);
                tok = Token.createRange();
            }
        } else {
            tok = Token.createRange();
        }
        int type;
        boolean firstloop = true;
        while ((type = this.read()) != T_EOF) {
            if (type == T_CHAR && this.chardata == ']' && !firstloop)
                break;
            firstloop = false;
            int c = this.chardata;
            boolean end = false;
            if (type == T_BACKSOLIDUS) {
                switch (c) {
                  case 'd':  case 'D':
                  case 'w':  case 'W':
                  case 's':  case 'S':
                    tok.mergeRanges(this.getTokenForShorthand(c));
                    end = true;
                    break;

                  case 'i':  case 'I':
                  case 'c':  case 'C':
                    c = this.processCIinCharacterClass(tok, c);
                    if (c < 0)  end = true;
                    break;
                    
                  case 'p':
                  case 'P':
                    int pstart = this.offset;
                    RangeToken tok2 = this.processBacksolidus_pP(c);
                    if (tok2 == null)  throw this.ex("parser.atom.5", pstart);
                    tok.mergeRanges(tok2);
                    end = true;
                    break;

                  default:
                    c = this.decodeEscaped();
                } // \ + c
            } // backsolidus
                                                // POSIX Character class such as [:alnum:]
            else if (type == T_POSIX_CHARCLASS_START) {
                int nameend = this.regex.indexOf(':', this.offset);
                if (nameend < 0) throw this.ex("parser.cc.1", this.offset);
                boolean positive = true;
                if (this.regex.charAt(this.offset) == '^') {
                    this.offset ++;
                    positive = false;
                }
                String name = this.regex.substring(this.offset, nameend);
                RangeToken range = Token.getRange(name, positive);
                if (range == null)  throw this.ex("parser.cc.3", this.offset);
                tok.mergeRanges(range);
                end = true;
                if (nameend+1 >= this.regexlen || this.regex.charAt(nameend+1) != ']')
                    throw this.ex("parser.cc.1", nameend);
                this.offset = nameend+2;
            }
            this.next();
            if (!end) {                         // if not shorthands...
                if (this.read() != T_CHAR || this.chardata != '-') { // Here is no '-'.
                    tok.addRange(c, c);
                } else {
                    this.next(); // Skips '-'
                    if ((type = this.read()) == T_EOF)  throw this.ex("parser.cc.2", this.offset);
                    if (type == T_CHAR && this.chardata == ']') {
                        tok.addRange(c, c);
                        tok.addRange('-', '-');
                    } else {
                        int rangeend = this.chardata;
                        if (type == T_BACKSOLIDUS)
                            rangeend = this.decodeEscaped();
                        this.next();
                        tok.addRange(c, rangeend);
                    }
                }
            }
            if (this.isSet(RegularExpression.SPECIAL_COMMA)
                && this.read() == T_CHAR && this.chardata == ',')
                this.next();
        }
        if (this.read() == T_EOF)
            throw this.ex("parser.cc.2", this.offset);
        if (!useNrange && nrange) {
            base.subtractRanges(tok);
            tok = base;
        }
        tok.sortRanges();
        tok.compactRanges();
        //tok.dumpRanges();
        /*
        if (this.isSet(RegularExpression.IGNORE_CASE))
            tok = RangeToken.createCaseInsensitiveToken(tok);
        */
        this.setContext(S_NORMAL);
        this.next();                    // Skips ']'

        return tok;
    }
    private RangeToken parseCharacterClass_old(boolean useNrange) throws ParseException {
        this.setContext(S_INBRACKETS);
        this.next();                            // '['
        boolean nrange = false;
        RangeToken base = null;
        RangeToken tok;
        if (this.read() == T_CHAR && this.chardata == '^') {
            nrange = true;
            this.next();                        // '^'
            if (useNrange) {
                tok = Token.createNRange();
            } else {
                base = Token.createRange();
                base.addRange(0, Token.UTF16_MAX);
                tok = Token.createRange();
            }
        } else {
            tok = Token.createRange();
        }
        int type;
        while ((type = this.read()) != T_EOF
               && !(type == T_CHAR && this.chardata == ']')) {
            int c = this.chardata;
            /*
            if (type == T_CHAR && c == '^') {
                this.next();
                type = this.read();
                c = this.chardata;
                if (type == T_EOF)  break;

                nrange = !nrange;
                if (nrange)
                    tok = Token.createRange();
                else {
                    base.subtractRanges(tok);
                    tok = base;
                }
            }
            */
            boolean end = false;
            if (type == T_BACKSOLIDUS) {
                switch (c) {
                  case 'd':  case 'D':
                  case 'w':  case 'W':
                  case 's':  case 'S':
                    tok.mergeRanges(this.getTokenForShorthand(c));
                    end = true;
                    break;

                  case 'i':  case 'I':
                  case 'c':  case 'C':
                    c = this.processCIinCharacterClass(tok, c);
                    if (c < 0)  end = true;
                    break;
                    
                  case 'p':
                  case 'P':
                    boolean positive = c ==  'p';
                    int pstart = this.offset;
                    this.next();
                    if (this.read() != T_CHAR)  throw ex("parser.atom.2", this.offset-1);
                    RangeToken tok2 = null;
                    switch (this.chardata) {
                      case 'L':                 // Letter
                        tok2 = Token.getRange("L", positive);  break;
                      case 'M':                 // Mark
                        tok2 = Token.getRange("M", positive);  break;
                      case 'N':                 // Number
                        tok2 = Token.getRange("N", positive);  break;
                      case 'Z':                 // Separator
                        tok2 = Token.getRange("Z", positive);  break;
                      case 'C':                 // Other
                        tok2 = Token.getRange("C", positive);  break;
                      case 'P':                 // Punctuation
                        tok2 = Token.getRange("P", positive);  break;
                      case 'S':                 // Symbol
                        tok2 = Token.getRange("S", positive);  break;
                      case '{':
                        // this.offset points the next of '{'.
                        pstart = this.offset;
                        int namestart = this.offset;
                        int nameend = this.regex.indexOf('}', namestart);
                        if (nameend < 0)  throw ex("parser.atom.3", this.offset);
                        this.offset = nameend+1;
                        tok2 = Token.getRange(this.regex.substring(namestart, nameend), positive);
                        break;

                      default:
                        throw ex("parser.atom.2", this.offset-1);
                    }
                    if (tok2 == null)  throw ex("parser.atom.5", pstart);
                    tok.mergeRanges(tok2);
                    end = true;
                    break;

                  default:
                    c = this.decodeEscaped();
                } // \ + c
            } // backsolidus
                                                // POSIX Character class such as [:alnum:]
            else if (type == T_POSIX_CHARCLASS_START) {
                int nameend = this.regex.indexOf(':', this.offset);
                if (nameend < 0) throw ex("parser.cc.1", this.offset);
                String name = this.regex.substring(this.offset, nameend);
                RangeToken range = Token.getRange(name, true);
                if (range == null)  throw ex("parser.cc.3", this.offset);
                tok.mergeRanges(range);
                end = true;
                if (nameend+1 >= this.regexlen || this.regex.charAt(nameend+1) != ']')
                    throw ex("parser.cc.1", nameend);
                this.offset = nameend+2;
            }
            this.next();
            if (!end) {
                if (this.read() != T_CHAR || this.chardata != '-') { // Here is no '-'.
                    tok.addRange(c, c);
                } else {
                    this.next(); // Skips '-'
                    if ((type = this.read()) == T_EOF) throw ex("parser.cc.2", this.offset);
                    int rangeend = this.chardata;
                    if (type == T_BACKSOLIDUS)
                        rangeend = this.decodeEscaped();
                    this.next();
                    tok.addRange(c, rangeend);
                }
            }
            if (this.read() == T_CHAR && this.chardata == ',')
                this.next();
        }
        if (this.read() == T_EOF)
            throw ex("parser.cc.2", this.offset);
        if (!useNrange && nrange) {
            base.subtractRanges(tok);
            tok = base;
        }
        tok.sortRanges();
        tok.compactRanges();
        //tok.dumpRanges();
        /*
        if (this.isSet(RegularExpression.IGNORE_CASE))
            tok = RangeToken.createCaseInsensitiveToken(tok);
        */
        this.setContext(S_NORMAL);
        this.next();                    // Skips ']'

        return tok;
    }

    /**
     * '(?[' ... ']' (('-' | '+' | '&') '[' ... ']')? ')'
     */
    protected RangeToken parseSetOperations() throws ParseException {
        RangeToken tok = this.parseCharacterClass(false);
        int type;
        while ((type = this.read()) != T_RPAREN) {
            int ch = this.chardata;
            if (type == T_CHAR && (ch == '-' || ch == '&')
                || type == T_PLUS) {
                this.next();
                if (this.read() != T_LBRACKET) throw ex("parser.ope.1", this.offset-1);
                RangeToken t2 = this.parseCharacterClass(false);
                if (type == T_PLUS)
                    tok.mergeRanges(t2);
                else if (ch == '-')
                    tok.subtractRanges(t2);
                else if (ch == '&')
                    tok.intersectRanges(t2);
                else
                    throw new RuntimeException("ASSERT");
            } else {
                throw ex("parser.ope.2", this.offset-1);
            }
        }
        this.next();
        return tok;
    }

    Token getTokenForShorthand(int ch) {
        Token tok;
        switch (ch) {
          case 'd':
            tok = this.isSet(RegularExpression.USE_UNICODE_CATEGORY)
                ? Token.getRange("Nd", true) : Token.token_0to9;
            break;
          case 'D':
            tok = this.isSet(RegularExpression.USE_UNICODE_CATEGORY)
                ? Token.getRange("Nd", false) : Token.token_not_0to9;
            break;
          case 'w':
            tok = this.isSet(RegularExpression.USE_UNICODE_CATEGORY)
                ? Token.getRange("IsWord", true) : Token.token_wordchars;
            break;
          case 'W':
            tok = this.isSet(RegularExpression.USE_UNICODE_CATEGORY)
                ? Token.getRange("IsWord", false) : Token.token_not_wordchars;
            break;
          case 's':
            tok = this.isSet(RegularExpression.USE_UNICODE_CATEGORY)
                ? Token.getRange("IsSpace", true) : Token.token_spaces;
            break;
          case 'S':
            tok = this.isSet(RegularExpression.USE_UNICODE_CATEGORY)
                ? Token.getRange("IsSpace", false) : Token.token_not_spaces;
            break;

          default:
            throw new RuntimeException("Internal Error: shorthands: \\u"+Integer.toString(ch, 16));
        }
        return tok;
    }

    /**
     */
    int decodeEscaped() throws ParseException {
        if (this.read() != T_BACKSOLIDUS)  throw ex("parser.next.1", this.offset-1);
        int c = this.chardata;
        switch (c) {
          case 'e':  c = 0x1b;  break; // ESCAPE U+001B
          case 'f':  c = '\f';  break; // FORM FEED U+000C
          case 'n':  c = '\n';  break; // LINE FEED U+000A
          case 'r':  c = '\r';  break; // CRRIAGE RETURN U+000D
          case 't':  c = '\t';  break; // HORIZONTAL TABULATION U+0009
          //case 'v':  c = 0x0b;  break; // VERTICAL TABULATION U+000B
          case 'x':
            this.next();
            if (this.read() != T_CHAR)  throw ex("parser.descape.1", this.offset-1);
            if (this.chardata == '{') {
                int v1 = 0;
                int uv = 0;
                do {
                    this.next();
                    if (this.read() != T_CHAR)  throw ex("parser.descape.1", this.offset-1);
                    if ((v1 = hexChar(this.chardata)) < 0)
                        break;
                    if (uv > uv*16) throw ex("parser.descape.2", this.offset-1);
                    uv = uv*16+v1;
                } while (true);
                if (this.chardata != '}')  throw ex("parser.descape.3", this.offset-1);
                if (uv > Token.UTF16_MAX)  throw ex("parser.descape.4", this.offset-1);
                c = uv;
            } else {
                int v1 = 0;
                if (this.read() != T_CHAR || (v1 = hexChar(this.chardata)) < 0)
                    throw ex("parser.descape.1", this.offset-1);
                int uv = v1;
                this.next();
                if (this.read() != T_CHAR || (v1 = hexChar(this.chardata)) < 0)
                    throw ex("parser.descape.1", this.offset-1);
                uv = uv*16+v1;
                c = uv;
            }
            break;

          case 'u':
            int v1 = 0;
            this.next();
            if (this.read() != T_CHAR || (v1 = hexChar(this.chardata)) < 0)
                throw ex("parser.descape.1", this.offset-1);
            int uv = v1;
            this.next();
            if (this.read() != T_CHAR || (v1 = hexChar(this.chardata)) < 0)
                throw ex("parser.descape.1", this.offset-1);
            uv = uv*16+v1;
            this.next();
            if (this.read() != T_CHAR || (v1 = hexChar(this.chardata)) < 0)
                throw ex("parser.descape.1", this.offset-1);
            uv = uv*16+v1;
            this.next();
            if (this.read() != T_CHAR || (v1 = hexChar(this.chardata)) < 0)
                throw ex("parser.descape.1", this.offset-1);
            uv = uv*16+v1;
            c = uv;
            break;

          case 'v':
            this.next();
            if (this.read() != T_CHAR || (v1 = hexChar(this.chardata)) < 0)
                throw ex("parser.descape.1", this.offset-1);
            uv = v1;
            this.next();
            if (this.read() != T_CHAR || (v1 = hexChar(this.chardata)) < 0)
                throw ex("parser.descape.1", this.offset-1);
            uv = uv*16+v1;
            this.next();
            if (this.read() != T_CHAR || (v1 = hexChar(this.chardata)) < 0)
                throw ex("parser.descape.1", this.offset-1);
            uv = uv*16+v1;
            this.next();
            if (this.read() != T_CHAR || (v1 = hexChar(this.chardata)) < 0)
                throw ex("parser.descape.1", this.offset-1);
            uv = uv*16+v1;
            this.next();
            if (this.read() != T_CHAR || (v1 = hexChar(this.chardata)) < 0)
                throw ex("parser.descape.1", this.offset-1);
            uv = uv*16+v1;
            this.next();
            if (this.read() != T_CHAR || (v1 = hexChar(this.chardata)) < 0)
                throw ex("parser.descape.1", this.offset-1);
            uv = uv*16+v1;
            if (uv > Token.UTF16_MAX)  throw ex("parser.descappe.4", this.offset-1);
            c = uv;
            break;
          case 'A':
          case 'Z':
          case 'z':
            throw ex("parser.descape.5", this.offset-2);
          default:
        }
        return c;
    }

    static private final int hexChar(int ch) {
        if (ch < '0')  return -1;
        if (ch > 'f')  return -1;
        if (ch <= '9')  return ch-'0';
        if (ch < 'A')  return -1;
        if (ch <= 'F')  return ch-'A'+10;
        if (ch < 'a')  return -1;
        return ch-'a'+10;
    }
}
