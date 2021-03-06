/* Generated By:JavaCC: Do not edit this line. SimplifierRules.java */
package net.sf.okapi.core.simplifierrules;

import java.io.StringReader;

import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.Util;

public class SimplifierRules implements SimplifierRulesConstants {
  private Code code;

  public static void main(String args [])
  {
    System.out.println("Reading from standard input...");
    System.out.print("Enter an expression like \u005c"if ADDABLE;\u005c" :");
    SimplifierRules parser = new SimplifierRules(System.in);
    try
    {
      boolean r = parser.rules();
    }
    catch (Exception e)
    {
      System.out.println("Oops.");
      System.out.println(e.getMessage());
    }
  }

  public final static void validate(String rules) throws ParseException {
    SimplifierRules r = new SimplifierRules(rules, new Code());
    r.parse();
  }

  public SimplifierRules()
  {
  }

  public SimplifierRules(String input, Code code)
  {
    this(new StringReader(input));
    this.code = code;
  }

  public boolean evaluate(String input, Code code) throws ParseException
  {
    if (Util.isEmpty(input) || code == null) {
        return false;
    }

    ReInit(new StringReader(input));
    this.code = code;
    return parse();
  }

  public boolean parse() throws ParseException
  {
    return rules();
  }

/**
    All rules are OR'ed with each other to get the final result.
    Short circuit and return early if any rule evaluates to true    
*/
  final public boolean rules() throws ParseException {
  boolean result=false; boolean r;
    label_1:
    while (true) {
      jj_consume_token(RULE_START);
      r = expression();
      jj_consume_token(RULE_END);
                                            if (r) {if (true) return true;} result |= r;
      if (jj_2_1(2)) {
        ;
      } else {
        break label_1;
      }
    }
    jj_consume_token(0);
    {if (true) return result;}
    throw new Error("Missing return statement in function");
  }

  final public boolean expression() throws ParseException {
  boolean result; boolean tail;
    result = term();
    label_2:
    while (true) {
      if (jj_2_2(2)) {
        ;
      } else {
        break label_2;
      }
      if (jj_2_3(2)) {
        jj_consume_token(AND);
        tail = term();
                             result &= tail;
      } else if (jj_2_4(2)) {
        jj_consume_token(OR);
        tail = term();
                            result |= tail;
      } else {
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
      {if (true) return result;}
    throw new Error("Missing return statement in function");
  }

  final public boolean term() throws ParseException {
  boolean result; TagType ctt; TagType ltt; String cs; String qs;
    if (jj_2_5(2)) {
      result = flagLiteral();
                         {if (true) return result;}
    } else if (jj_2_6(2)) {
      ctt = codeTagTypeField();
      jj_consume_token(EQUAL);
      ltt = tagTypeLiteral();
                                                         {if (true) return ctt == ltt;}
    } else if (jj_2_7(2)) {
      ctt = codeTagTypeField();
      jj_consume_token(NOT_EQUAL);
      ltt = tagTypeLiteral();
                                                             {if (true) return ctt != ltt;}
    } else if (jj_2_8(2)) {
      cs = codeString();
      jj_consume_token(EQUAL);
      qs = queryString();
                                              {if (true) return cs.equals(qs);}
    } else if (jj_2_9(2)) {
      cs = codeString();
      jj_consume_token(NOT_EQUAL);
      qs = queryString();
                                                  {if (true) return !cs.equals(qs);}
    } else if (jj_2_10(2)) {
      cs = codeString();
      jj_consume_token(MATCH);
      qs = queryString();
                                              {if (true) return cs.matches(qs);}
    } else if (jj_2_11(2)) {
      cs = codeString();
      jj_consume_token(NOT_MATCH);
      qs = queryString();
                                                  {if (true) return !cs.matches(qs);}
    } else if (jj_2_12(2)) {
      jj_consume_token(LPAREN);
      result = expression();
      jj_consume_token(RPAREN);
                                            {if (true) return result;}
    } else {
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public String codeString() throws ParseException {
    if (jj_2_13(2)) {
      jj_consume_token(DATA);
           {if (true) return code.getData() == null ? "" : code.getData();}
    } else if (jj_2_14(2)) {
      jj_consume_token(OUTER_DATA);
                 {if (true) return code.getOuterData() == null ? "" : code.getOuterData();}
    } else if (jj_2_15(2)) {
      jj_consume_token(ORIGINAL_ID);
                  {if (true) return code.getOriginalId() == null ? "" : code.getOriginalId();}
    } else if (jj_2_16(2)) {
      jj_consume_token(TYPE);
           {if (true) return code.getType() == null ? "" : code.getType();}
    } else {
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public TagType codeTagTypeField() throws ParseException {
    jj_consume_token(TAG_TYPE);
               {if (true) return code.getTagType();}
    throw new Error("Missing return statement in function");
  }

  final public TagType tagTypeLiteral() throws ParseException {
    if (jj_2_17(2)) {
      jj_consume_token(CLOSING);
              {if (true) return TagType.CLOSING;}
    } else if (jj_2_18(2)) {
      jj_consume_token(OPENING);
              {if (true) return TagType.OPENING;}
    } else if (jj_2_19(2)) {
      jj_consume_token(STANDALONE);
                 {if (true) return TagType.PLACEHOLDER;}
    } else {
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public boolean flagLiteral() throws ParseException {
    if (jj_2_20(2)) {
      jj_consume_token(ADDABLE);
              {if (true) return code.isAdded();}
    } else if (jj_2_21(2)) {
      jj_consume_token(DELETABLE);
                {if (true) return code.isDeleteable();}
    } else if (jj_2_22(2)) {
      jj_consume_token(CLONEABLE);
                {if (true) return code.isCloneable();}
    } else {
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public String queryString() throws ParseException {
 Token t;
    if (jj_2_23(2)) {
      t = jj_consume_token(STRING_DOUBLE_EMPTY);
  {if (true) return "";}
    } else if (jj_2_24(2)) {
      t = jj_consume_token(STRING_DOUBLE_NONEMPTY);
    } else {
      jj_consume_token(-1);
      throw new ParseException();
    }
  {if (true) return SimplifierRulesUtil.unescape(t.image.substring(1, t.image.length()-1));}
    throw new Error("Missing return statement in function");
  }

  private boolean jj_2_1(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_1(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(0, xla); }
  }

  private boolean jj_2_2(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_2(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(1, xla); }
  }

  private boolean jj_2_3(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_3(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(2, xla); }
  }

  private boolean jj_2_4(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_4(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(3, xla); }
  }

  private boolean jj_2_5(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_5(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(4, xla); }
  }

  private boolean jj_2_6(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_6(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(5, xla); }
  }

  private boolean jj_2_7(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_7(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(6, xla); }
  }

  private boolean jj_2_8(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_8(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(7, xla); }
  }

  private boolean jj_2_9(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_9(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(8, xla); }
  }

  private boolean jj_2_10(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_10(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(9, xla); }
  }

  private boolean jj_2_11(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_11(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(10, xla); }
  }

  private boolean jj_2_12(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_12(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(11, xla); }
  }

  private boolean jj_2_13(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_13(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(12, xla); }
  }

  private boolean jj_2_14(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_14(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(13, xla); }
  }

  private boolean jj_2_15(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_15(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(14, xla); }
  }

  private boolean jj_2_16(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_16(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(15, xla); }
  }

  private boolean jj_2_17(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_17(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(16, xla); }
  }

  private boolean jj_2_18(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_18(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(17, xla); }
  }

  private boolean jj_2_19(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_19(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(18, xla); }
  }

  private boolean jj_2_20(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_20(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(19, xla); }
  }

  private boolean jj_2_21(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_21(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(20, xla); }
  }

  private boolean jj_2_22(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_22(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(21, xla); }
  }

  private boolean jj_2_23(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_23(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(22, xla); }
  }

  private boolean jj_2_24(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_24(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(23, xla); }
  }

  private boolean jj_3_22() {
    if (jj_scan_token(CLONEABLE)) return true;
    return false;
  }

  private boolean jj_3R_4() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3_5()) {
    jj_scanpos = xsp;
    if (jj_3_6()) {
    jj_scanpos = xsp;
    if (jj_3_7()) {
    jj_scanpos = xsp;
    if (jj_3_8()) {
    jj_scanpos = xsp;
    if (jj_3_9()) {
    jj_scanpos = xsp;
    if (jj_3_10()) {
    jj_scanpos = xsp;
    if (jj_3_11()) {
    jj_scanpos = xsp;
    if (jj_3_12()) return true;
    }
    }
    }
    }
    }
    }
    }
    return false;
  }

  private boolean jj_3_21() {
    if (jj_scan_token(DELETABLE)) return true;
    return false;
  }

  private boolean jj_3_5() {
    if (jj_3R_5()) return true;
    return false;
  }

  private boolean jj_3_20() {
    if (jj_scan_token(ADDABLE)) return true;
    return false;
  }

  private boolean jj_3R_5() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3_20()) {
    jj_scanpos = xsp;
    if (jj_3_21()) {
    jj_scanpos = xsp;
    if (jj_3_22()) return true;
    }
    }
    return false;
  }

  private boolean jj_3_4() {
    if (jj_scan_token(OR)) return true;
    if (jj_3R_4()) return true;
    return false;
  }

  private boolean jj_3_3() {
    if (jj_scan_token(AND)) return true;
    if (jj_3R_4()) return true;
    return false;
  }

  private boolean jj_3_2() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3_3()) {
    jj_scanpos = xsp;
    if (jj_3_4()) return true;
    }
    return false;
  }

  private boolean jj_3_19() {
    if (jj_scan_token(STANDALONE)) return true;
    return false;
  }

  private boolean jj_3_18() {
    if (jj_scan_token(OPENING)) return true;
    return false;
  }

  private boolean jj_3_17() {
    if (jj_scan_token(CLOSING)) return true;
    return false;
  }

  private boolean jj_3R_3() {
    if (jj_3R_4()) return true;
    return false;
  }

  private boolean jj_3R_6() {
    if (jj_scan_token(TAG_TYPE)) return true;
    return false;
  }

  private boolean jj_3_1() {
    if (jj_scan_token(RULE_START)) return true;
    if (jj_3R_3()) return true;
    return false;
  }

  private boolean jj_3_16() {
    if (jj_scan_token(TYPE)) return true;
    return false;
  }

  private boolean jj_3_15() {
    if (jj_scan_token(ORIGINAL_ID)) return true;
    return false;
  }

  private boolean jj_3_14() {
    if (jj_scan_token(OUTER_DATA)) return true;
    return false;
  }

  private boolean jj_3R_7() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3_13()) {
    jj_scanpos = xsp;
    if (jj_3_14()) {
    jj_scanpos = xsp;
    if (jj_3_15()) {
    jj_scanpos = xsp;
    if (jj_3_16()) return true;
    }
    }
    }
    return false;
  }

  private boolean jj_3_13() {
    if (jj_scan_token(DATA)) return true;
    return false;
  }

  private boolean jj_3_12() {
    if (jj_scan_token(LPAREN)) return true;
    if (jj_3R_3()) return true;
    return false;
  }

  private boolean jj_3_11() {
    if (jj_3R_7()) return true;
    if (jj_scan_token(NOT_MATCH)) return true;
    return false;
  }

  private boolean jj_3_10() {
    if (jj_3R_7()) return true;
    if (jj_scan_token(MATCH)) return true;
    return false;
  }

  private boolean jj_3_24() {
    if (jj_scan_token(STRING_DOUBLE_NONEMPTY)) return true;
    return false;
  }

  private boolean jj_3_9() {
    if (jj_3R_7()) return true;
    if (jj_scan_token(NOT_EQUAL)) return true;
    return false;
  }

  private boolean jj_3_23() {
    if (jj_scan_token(STRING_DOUBLE_EMPTY)) return true;
    return false;
  }

  private boolean jj_3_8() {
    if (jj_3R_7()) return true;
    if (jj_scan_token(EQUAL)) return true;
    return false;
  }

  private boolean jj_3_7() {
    if (jj_3R_6()) return true;
    if (jj_scan_token(NOT_EQUAL)) return true;
    return false;
  }

  private boolean jj_3_6() {
    if (jj_3R_6()) return true;
    if (jj_scan_token(EQUAL)) return true;
    return false;
  }

  /** Generated Token Manager. */
  public SimplifierRulesTokenManager token_source;
  JavaCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private Token jj_scanpos, jj_lastpos;
  private int jj_la;
  private int jj_gen;
  final private int[] jj_la1 = new int[0];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {};
   }
  final private JJCalls[] jj_2_rtns = new JJCalls[24];
  private boolean jj_rescan = false;
  private int jj_gc = 0;

  /** Constructor with InputStream. */
  public SimplifierRules(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public SimplifierRules(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new JavaCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new SimplifierRulesTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor. */
  public SimplifierRules(java.io.Reader stream) {
    jj_input_stream = new JavaCharStream(stream, 1, 1);
    token_source = new SimplifierRulesTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor with generated Token Manager. */
  public SimplifierRules(SimplifierRulesTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(SimplifierRulesTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static private final class LookaheadSuccess extends java.lang.Error { }
  final private LookaheadSuccess jj_ls = new LookaheadSuccess();
  private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0; Token tok = token;
      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
      if (tok != null) jj_add_error_token(kind, i);
    }
    if (jj_scanpos.kind != kind) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
    return false;
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;
  private int[] jj_lasttokens = new int[100];
  private int jj_endpos;

  private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) return;
    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];
      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }
      jj_entries_loop: for (java.util.Iterator<?> it = jj_expentries.iterator(); it.hasNext();) {
        int[] oldentry = (int[])(it.next());
        if (oldentry.length == jj_expentry.length) {
          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              continue jj_entries_loop;
            }
          }
          jj_expentries.add(jj_expentry);
          break jj_entries_loop;
        }
      }
      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
    }
  }

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[32];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 0; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 32; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

  private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 24; i++) {
    try {
      JJCalls p = jj_2_rtns[i];
      do {
        if (p.gen > jj_gen) {
          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
          switch (i) {
            case 0: jj_3_1(); break;
            case 1: jj_3_2(); break;
            case 2: jj_3_3(); break;
            case 3: jj_3_4(); break;
            case 4: jj_3_5(); break;
            case 5: jj_3_6(); break;
            case 6: jj_3_7(); break;
            case 7: jj_3_8(); break;
            case 8: jj_3_9(); break;
            case 9: jj_3_10(); break;
            case 10: jj_3_11(); break;
            case 11: jj_3_12(); break;
            case 12: jj_3_13(); break;
            case 13: jj_3_14(); break;
            case 14: jj_3_15(); break;
            case 15: jj_3_16(); break;
            case 16: jj_3_17(); break;
            case 17: jj_3_18(); break;
            case 18: jj_3_19(); break;
            case 19: jj_3_20(); break;
            case 20: jj_3_21(); break;
            case 21: jj_3_22(); break;
            case 22: jj_3_23(); break;
            case 23: jj_3_24(); break;
          }
        }
        p = p.next;
      } while (p != null);
      } catch(LookaheadSuccess ls) { }
    }
    jj_rescan = false;
  }

  private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) { p = p.next = new JJCalls(); break; }
      p = p.next;
    }
    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
  }

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }

}
