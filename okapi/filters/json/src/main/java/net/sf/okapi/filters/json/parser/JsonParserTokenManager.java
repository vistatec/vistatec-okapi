/* JsonParserTokenManager.java */
/* Generated By:JavaCC: Do not edit this line. JsonParserTokenManager.java */
package net.sf.okapi.filters.json.parser;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/** Token Manager. */
@SuppressWarnings("all")
public class JsonParserTokenManager implements JsonParserConstants {
    int commentNesting = 0;
    StringBuilder multiLineComment = new StringBuilder();

  /** Debug output. */
  public  java.io.PrintStream debugStream = System.out;
  /** Set debug output. */
  public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
private int jjMoveStringLiteralDfa0_2(){
   switch(curChar)
   {
      case 45:
         return jjMoveStringLiteralDfa1_2(0x80L);
      default :
         return 1;
   }
}
private int jjMoveStringLiteralDfa1_2(long active0){
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      return 1;
   }
   switch(curChar)
   {
      case 45:
         return jjMoveStringLiteralDfa2_2(active0, 0x80L);
      default :
         return 2;
   }
}
private int jjMoveStringLiteralDfa2_2(long old0, long active0){
   if (((active0 &= old0)) == 0L)
      return 2;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      return 2;
   }
   switch(curChar)
   {
      case 62:
         if ((active0 & 0x80L) != 0L)
            return jjStopAtPos(2, 7);
         break;
      default :
         return 3;
   }
   return 3;
}
private final int jjStopStringLiteralDfa_0(int pos, long active0){
   switch (pos)
   {
      case 0:
         if ((active0 & 0x8L) != 0L)
            return 0;
         if ((active0 & 0x1000000000L) != 0L)
            return 40;
         if ((active0 & 0x800000000L) != 0L)
            return 41;
         if ((active0 & 0x70000000L) != 0L)
         {
            jjmatchedKind = 31;
            return 13;
         }
         return -1;
      case 1:
         if ((active0 & 0x70000000L) != 0L)
         {
            jjmatchedKind = 31;
            jjmatchedPos = 1;
            return 13;
         }
         return -1;
      case 2:
         if ((active0 & 0x70000000L) != 0L)
         {
            jjmatchedKind = 31;
            jjmatchedPos = 2;
            return 13;
         }
         return -1;
      case 3:
         if ((active0 & 0x50000000L) != 0L)
            return 13;
         if ((active0 & 0x20000000L) != 0L)
         {
            jjmatchedKind = 31;
            jjmatchedPos = 3;
            return 13;
         }
         return -1;
      default :
         return -1;
   }
}
private final int jjStartNfa_0(int pos, long active0){
   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
}
private int jjMoveStringLiteralDfa0_0(){
   switch(curChar)
   {
      case 34:
         return jjMoveStringLiteralDfa1_0(0x1000000000L);
      case 39:
         return jjMoveStringLiteralDfa1_0(0x800000000L);
      case 44:
         return jjStopAtPos(0, 12);
      case 47:
         return jjMoveStringLiteralDfa1_0(0x8L);
      case 58:
         return jjStopAtPos(0, 15);
      case 60:
         return jjMoveStringLiteralDfa1_0(0x10L);
      case 91:
         return jjStopAtPos(0, 16);
      case 93:
         return jjStopAtPos(0, 17);
      case 102:
         return jjMoveStringLiteralDfa1_0(0x20000000L);
      case 110:
         return jjMoveStringLiteralDfa1_0(0x40000000L);
      case 116:
         return jjMoveStringLiteralDfa1_0(0x10000000L);
      case 123:
         return jjStopAtPos(0, 13);
      case 125:
         return jjStopAtPos(0, 14);
      default :
         return jjMoveNfa_0(5, 0);
   }
}
private int jjMoveStringLiteralDfa1_0(long active0){
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(0, active0);
      return 1;
   }
   switch(curChar)
   {
      case 33:
         return jjMoveStringLiteralDfa2_0(active0, 0x10L);
      case 34:
         if ((active0 & 0x1000000000L) != 0L)
            return jjStopAtPos(1, 36);
         break;
      case 39:
         if ((active0 & 0x800000000L) != 0L)
            return jjStopAtPos(1, 35);
         break;
      case 42:
         if ((active0 & 0x8L) != 0L)
            return jjStopAtPos(1, 3);
         break;
      case 97:
         return jjMoveStringLiteralDfa2_0(active0, 0x20000000L);
      case 114:
         return jjMoveStringLiteralDfa2_0(active0, 0x10000000L);
      case 117:
         return jjMoveStringLiteralDfa2_0(active0, 0x40000000L);
      default :
         break;
   }
   return jjStartNfa_0(0, active0);
}
private int jjMoveStringLiteralDfa2_0(long old0, long active0){
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(0, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(1, active0);
      return 2;
   }
   switch(curChar)
   {
      case 45:
         return jjMoveStringLiteralDfa3_0(active0, 0x10L);
      case 108:
         return jjMoveStringLiteralDfa3_0(active0, 0x60000000L);
      case 117:
         return jjMoveStringLiteralDfa3_0(active0, 0x10000000L);
      default :
         break;
   }
   return jjStartNfa_0(1, active0);
}
private int jjMoveStringLiteralDfa3_0(long old0, long active0){
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(1, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(2, active0);
      return 3;
   }
   switch(curChar)
   {
      case 45:
         if ((active0 & 0x10L) != 0L)
            return jjStopAtPos(3, 4);
         break;
      case 101:
         if ((active0 & 0x10000000L) != 0L)
            return jjStartNfaWithStates_0(3, 28, 13);
         break;
      case 108:
         if ((active0 & 0x40000000L) != 0L)
            return jjStartNfaWithStates_0(3, 30, 13);
         break;
      case 115:
         return jjMoveStringLiteralDfa4_0(active0, 0x20000000L);
      default :
         break;
   }
   return jjStartNfa_0(2, active0);
}
private int jjMoveStringLiteralDfa4_0(long old0, long active0){
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(2, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(3, active0);
      return 4;
   }
   switch(curChar)
   {
      case 101:
         if ((active0 & 0x20000000L) != 0L)
            return jjStartNfaWithStates_0(4, 29, 13);
         break;
      default :
         break;
   }
   return jjStartNfa_0(3, active0);
}
private int jjStartNfaWithStates_0(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_0(state, pos + 1);
}
static final long[] jjbitVec0 = {
   0xfffffffffffffffeL, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec2 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec3 = {
   0x0L, 0x0L, 0x100000000L, 0x0L
};
static final long[] jjbitVec4 = {
   0x0L, 0x0L, 0x1L, 0x0L
};
static final long[] jjbitVec5 = {
   0x4000L, 0x0L, 0x0L, 0x0L
};
static final long[] jjbitVec6 = {
   0x800000000fffL, 0x80000000L, 0x0L, 0x0L
};
static final long[] jjbitVec7 = {
   0x1L, 0x0L, 0x0L, 0x0L
};
static final long[] jjbitVec8 = {
   0x0L, 0x0L, 0x0L, 0x8000000000000000L
};
static final long[] jjbitVec9 = {
   0x1ff00000fffffffeL, 0xffffffffffffc000L, 0xffffffffL, 0x1600000000000000L
};
static final long[] jjbitVec10 = {
   0x0L, 0x0L, 0xc603c0000000000L, 0xff7fffffff7fffffL
};
static final long[] jjbitVec11 = {
   0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec12 = {
   0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffL, 0x0L
};
static final long[] jjbitVec13 = {
   0xffffffffffffffffL, 0xffffffffffffffffL, 0x0L, 0x0L
};
static final long[] jjbitVec14 = {
   0x3fffffffffffL, 0x0L, 0x0L, 0x0L
};
static final long[] jjbitVec15 = {
   0x7f7ffdff80f8007fL, 0xffffffffffffffdbL, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec16 = {
   0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL, 0x3fff000000ffffffL
};
static final long[] jjbitVec17 = {
   0x0L, 0xffff000000000000L, 0xffffffffffffffffL, 0x1fffffffffffffffL
};
static final long[] jjbitVec18 = {
   0x87ffffff80000000L, 0xfffffffe7fffffffL, 0x7fffffffffffffffL, 0x1cfcfcfcL
};
static final long[] jjbitVec19 = {
   0x3ff00000000L, 0x0L, 0x0L, 0x0L
};
static final long[] jjbitVec20 = {
   0x0L, 0x0L, 0x0L, 0x3ff03ff0000L
};
static final long[] jjbitVec21 = {
   0x3ffL, 0x0L, 0x0L, 0x3ff0000L
};
static final long[] jjbitVec22 = {
   0x0L, 0x3ff0000L, 0x0L, 0x0L
};
static final long[] jjbitVec23 = {
   0x0L, 0x0L, 0x0L, 0x3ff000000000000L
};
static final long[] jjbitVec24 = {
   0x87ffffff83ff0000L, 0xfffffffe7fffffffL, 0x7fffffffffffffffL, 0x1cfcfcfcL
};
static final long[] jjbitVec25 = {
   0x1e00L, 0x0L, 0x0L, 0x0L
};
static final long[] jjbitVec26 = {
   0x0L, 0xffc000000000L, 0x0L, 0xffc000000000L
};
static final long[] jjbitVec27 = {
   0x8000L, 0x0L, 0x4000000000L, 0x0L
};
static final long[] jjbitVec28 = {
   0x1000000L, 0x0L, 0x0L, 0x8000000000000000L
};
static final long[] jjbitVec29 = {
   0x3ff0000L, 0x0L, 0x0L, 0x0L
};
static final long[] jjbitVec30 = {
   0x0L, 0x3ff00000000L, 0x0L, 0x3ff000000000000L
};
static final long[] jjbitVec31 = {
   0x0L, 0x0L, 0x0L, 0x3ffL
};
static final long[] jjbitVec32 = {
   0x0L, 0xffc000000000L, 0x0L, 0x0L
};
static final long[] jjbitVec33 = {
   0x0L, 0x3ff0000L, 0x0L, 0x3ff0000L
};
static final long[] jjbitVec34 = {
   0x0L, 0x3ffL, 0x3ff0000L, 0x0L
};
static final long[] jjbitVec35 = {
   0x0L, 0x3fe0000000000L, 0x0L, 0x0L
};
static final long[] jjbitVec36 = {
   0x0L, 0x0L, 0x0L, 0x3ff00000000L
};
static final long[] jjbitVec37 = {
   0x0L, 0xffc0L, 0x0L, 0x3ff0000L
};
static final long[] jjbitVec38 = {
   0x0L, 0x0L, 0x3ff03ffL, 0x0L
};
static final long[] jjbitVec39 = {
   0x0L, 0x3ff0000L, 0x3ff000000000000L, 0x0L
};
static final long[] jjbitVec40 = {
   0x0L, 0x3ff03ffL, 0x0L, 0x0L
};
private int jjMoveNfa_0(int startState, int curPos)
{
   int startsAt = 0;
   jjnewStateCnt = 40;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         do
         {
            switch(jjstateSet[--i])
            {
               case 40:
               case 20:
                  if ((0xfffffffbffffc9ffL & l) != 0L)
                     { jjCheckNAddStates(0, 2); }
                  break;
               case 5:
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 23)
                        kind = 23;
                     { jjCheckNAddStates(3, 9); }
                  }
                  else if ((0x100003600L & l) != 0L)
                  {
                     if (kind > 11)
                        kind = 11;
                     { jjCheckNAdd(11); }
                  }
                  else if (curChar == 45)
                     { jjCheckNAddStates(10, 13); }
                  else if (curChar == 34)
                     { jjCheckNAddTwoStates(20, 21); }
                  else if (curChar == 39)
                     { jjCheckNAddTwoStates(15, 16); }
                  else if (curChar == 36)
                  {
                     if (kind > 31)
                        kind = 31;
                     { jjCheckNAdd(13); }
                  }
                  else if (curChar == 35)
                     { jjCheckNAddStates(14, 16); }
                  else if (curChar == 47)
                     jjstateSet[jjnewStateCnt++] = 0;
                  break;
               case 41:
               case 15:
                  if ((0xffffff7fffffc9ffL & l) != 0L)
                     { jjCheckNAddStates(17, 19); }
                  break;
               case 0:
                  if (curChar == 47)
                     { jjCheckNAddStates(20, 22); }
                  break;
               case 1:
                  if ((0xffffffffffffdbffL & l) != 0L)
                     { jjCheckNAddStates(20, 22); }
                  break;
               case 2:
                  if ((0x2400L & l) != 0L && kind > 1)
                     kind = 1;
                  break;
               case 3:
                  if (curChar == 10 && kind > 1)
                     kind = 1;
                  break;
               case 4:
                  if (curChar == 13)
                     jjstateSet[jjnewStateCnt++] = 3;
                  break;
               case 6:
                  if (curChar == 35)
                     { jjCheckNAddStates(14, 16); }
                  break;
               case 7:
                  if ((0xffffffffffffdbffL & l) != 0L)
                     { jjCheckNAddStates(14, 16); }
                  break;
               case 8:
                  if ((0x2400L & l) != 0L && kind > 2)
                     kind = 2;
                  break;
               case 9:
                  if (curChar == 10 && kind > 2)
                     kind = 2;
                  break;
               case 10:
                  if (curChar == 13)
                     jjstateSet[jjnewStateCnt++] = 9;
                  break;
               case 11:
                  if ((0x100003600L & l) == 0L)
                     break;
                  if (kind > 11)
                     kind = 11;
                  { jjCheckNAdd(11); }
                  break;
               case 12:
                  if (curChar != 36)
                     break;
                  if (kind > 31)
                     kind = 31;
                  { jjCheckNAdd(13); }
                  break;
               case 13:
                  if ((0x3ff001000000000L & l) == 0L)
                     break;
                  if (kind > 31)
                     kind = 31;
                  { jjCheckNAdd(13); }
                  break;
               case 14:
                  if (curChar == 39)
                     { jjCheckNAddTwoStates(15, 16); }
                  break;
               case 17:
                  if ((0x808000000000L & l) != 0L)
                     { jjCheckNAddStates(17, 19); }
                  break;
               case 18:
                  if (curChar == 39 && kind > 39)
                     kind = 39;
                  break;
               case 19:
                  if (curChar == 34)
                     { jjCheckNAddTwoStates(20, 21); }
                  break;
               case 22:
                  if ((0x800400000000L & l) != 0L)
                     { jjCheckNAddStates(0, 2); }
                  break;
               case 23:
                  if (curChar == 34 && kind > 40)
                     kind = 40;
                  break;
               case 24:
                  if (curChar == 45)
                     { jjCheckNAddStates(10, 13); }
                  break;
               case 25:
                  if ((0x3ff000000000000L & l) != 0L)
                     { jjCheckNAddTwoStates(25, 26); }
                  break;
               case 26:
                  if (curChar == 46)
                     { jjCheckNAdd(27); }
                  break;
               case 27:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 23)
                     kind = 23;
                  { jjCheckNAdd(27); }
                  break;
               case 28:
                  if ((0x3ff000000000000L & l) != 0L)
                     { jjCheckNAddTwoStates(28, 29); }
                  break;
               case 30:
                  if ((0x280000000000L & l) != 0L)
                     { jjCheckNAdd(31); }
                  break;
               case 31:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 23)
                     kind = 23;
                  { jjCheckNAdd(31); }
                  break;
               case 32:
                  if ((0x3ff000000000000L & l) != 0L)
                     { jjCheckNAddTwoStates(32, 33); }
                  break;
               case 33:
                  if (curChar == 46)
                     { jjCheckNAdd(34); }
                  break;
               case 34:
                  if ((0x3ff000000000000L & l) != 0L)
                     { jjCheckNAddTwoStates(34, 35); }
                  break;
               case 36:
                  if ((0x280000000000L & l) != 0L)
                     { jjCheckNAdd(37); }
                  break;
               case 37:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 23)
                     kind = 23;
                  { jjCheckNAdd(37); }
                  break;
               case 38:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 23)
                     kind = 23;
                  { jjCheckNAdd(38); }
                  break;
               case 39:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 23)
                     kind = 23;
                  { jjCheckNAddStates(3, 9); }
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 40:
                  if ((0xffffffffefffffffL & l) != 0L)
                     { jjCheckNAddStates(0, 2); }
                  else if (curChar == 92)
                     jjstateSet[jjnewStateCnt++] = 22;
                  break;
               case 5:
               case 13:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 31)
                     kind = 31;
                  { jjCheckNAdd(13); }
                  break;
               case 41:
                  if ((0xffffffffefffffffL & l) != 0L)
                     { jjCheckNAddStates(17, 19); }
                  else if (curChar == 92)
                     jjstateSet[jjnewStateCnt++] = 17;
                  break;
               case 1:
                  { jjAddStates(20, 22); }
                  break;
               case 7:
                  { jjAddStates(14, 16); }
                  break;
               case 15:
                  if ((0xffffffffefffffffL & l) != 0L)
                     { jjCheckNAddStates(17, 19); }
                  break;
               case 16:
                  if (curChar == 92)
                     jjstateSet[jjnewStateCnt++] = 17;
                  break;
               case 17:
                  if ((0x14404410000000L & l) != 0L)
                     { jjCheckNAddStates(17, 19); }
                  break;
               case 20:
                  if ((0xffffffffefffffffL & l) != 0L)
                     { jjCheckNAddStates(0, 2); }
                  break;
               case 21:
                  if (curChar == 92)
                     jjstateSet[jjnewStateCnt++] = 22;
                  break;
               case 22:
                  if ((0x14404410000000L & l) != 0L)
                     { jjCheckNAddStates(0, 2); }
                  break;
               case 29:
                  if ((0x2000000020L & l) != 0L)
                     { jjAddStates(23, 24); }
                  break;
               case 35:
                  if ((0x2000000020L & l) != 0L)
                     { jjAddStates(25, 26); }
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 40:
               case 20:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     { jjCheckNAddStates(0, 2); }
                  break;
               case 5:
                  if (jjCanMove_1(hiByte, i1, i2, l1, l2))
                  {
                     if (kind > 11)
                        kind = 11;
                     { jjCheckNAdd(11); }
                  }
                  if (jjCanMove_2(hiByte, i1, i2, l1, l2))
                  {
                     if (kind > 31)
                        kind = 31;
                     { jjCheckNAdd(13); }
                  }
                  if (jjCanMove_4(hiByte, i1, i2, l1, l2))
                  {
                     if (kind > 23)
                        kind = 23;
                     { jjCheckNAddStates(3, 9); }
                  }
                  break;
               case 41:
               case 15:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     { jjCheckNAddStates(17, 19); }
                  break;
               case 1:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     { jjAddStates(20, 22); }
                  break;
               case 7:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     { jjAddStates(14, 16); }
                  break;
               case 11:
                  if (!jjCanMove_1(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 11)
                     kind = 11;
                  { jjCheckNAdd(11); }
                  break;
               case 12:
                  if (!jjCanMove_2(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 31)
                     kind = 31;
                  { jjCheckNAdd(13); }
                  break;
               case 13:
                  if (!jjCanMove_3(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 31)
                     kind = 31;
                  { jjCheckNAdd(13); }
                  break;
               case 25:
                  if (jjCanMove_4(hiByte, i1, i2, l1, l2))
                     { jjCheckNAddTwoStates(25, 26); }
                  break;
               case 27:
                  if (!jjCanMove_4(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 23)
                     kind = 23;
                  jjstateSet[jjnewStateCnt++] = 27;
                  break;
               case 28:
                  if (jjCanMove_4(hiByte, i1, i2, l1, l2))
                     { jjCheckNAddTwoStates(28, 29); }
                  break;
               case 31:
                  if (!jjCanMove_4(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 23)
                     kind = 23;
                  jjstateSet[jjnewStateCnt++] = 31;
                  break;
               case 32:
                  if (jjCanMove_4(hiByte, i1, i2, l1, l2))
                     { jjCheckNAddTwoStates(32, 33); }
                  break;
               case 34:
                  if (jjCanMove_4(hiByte, i1, i2, l1, l2))
                     { jjAddStates(27, 28); }
                  break;
               case 37:
                  if (!jjCanMove_4(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 23)
                     kind = 23;
                  jjstateSet[jjnewStateCnt++] = 37;
                  break;
               case 38:
                  if (!jjCanMove_4(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 23)
                     kind = 23;
                  { jjCheckNAdd(38); }
                  break;
               case 39:
                  if (!jjCanMove_4(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 23)
                     kind = 23;
                  { jjCheckNAddStates(3, 9); }
                  break;
               default : if (i1 == 0 || l1 == 0 || i2 == 0 ||  l2 == 0) break; else break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 40 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private int jjMoveStringLiteralDfa0_1(){
   switch(curChar)
   {
      case 42:
         return jjMoveStringLiteralDfa1_1(0x40L);
      case 47:
         return jjMoveStringLiteralDfa1_1(0x20L);
      default :
         return 1;
   }
}
private int jjMoveStringLiteralDfa1_1(long active0){
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      return 1;
   }
   switch(curChar)
   {
      case 42:
         if ((active0 & 0x20L) != 0L)
            return jjStopAtPos(1, 5);
         break;
      case 47:
         if ((active0 & 0x40L) != 0L)
            return jjStopAtPos(1, 6);
         break;
      default :
         return 2;
   }
   return 2;
}
static final int[] jjnextStates = {
   20, 21, 23, 25, 26, 28, 29, 32, 33, 38, 25, 28, 32, 38, 7, 8, 
   10, 15, 16, 18, 1, 2, 4, 30, 31, 36, 37, 34, 35, 
};
private static final boolean jjCanMove_0(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec2[i2] & l2) != 0L);
      default :
         if ((jjbitVec0[i1] & l1) != 0L)
            return true;
         return false;
   }
}
private static final boolean jjCanMove_1(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec3[i2] & l2) != 0L);
      case 22:
         return ((jjbitVec4[i2] & l2) != 0L);
      case 24:
         return ((jjbitVec5[i2] & l2) != 0L);
      case 32:
         return ((jjbitVec6[i2] & l2) != 0L);
      case 48:
         return ((jjbitVec7[i2] & l2) != 0L);
      case 254:
         return ((jjbitVec8[i2] & l2) != 0L);
      default :
         return false;
   }
}
private static final boolean jjCanMove_2(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec10[i2] & l2) != 0L);
      case 48:
         return ((jjbitVec11[i2] & l2) != 0L);
      case 49:
         return ((jjbitVec12[i2] & l2) != 0L);
      case 51:
         return ((jjbitVec13[i2] & l2) != 0L);
      case 61:
         return ((jjbitVec14[i2] & l2) != 0L);
      case 251:
         return ((jjbitVec15[i2] & l2) != 0L);
      case 253:
         return ((jjbitVec16[i2] & l2) != 0L);
      case 254:
         return ((jjbitVec17[i2] & l2) != 0L);
      case 255:
         return ((jjbitVec18[i2] & l2) != 0L);
      default :
         if ((jjbitVec9[i1] & l1) != 0L)
            return true;
         return false;
   }
}
private static final boolean jjCanMove_3(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec10[i2] & l2) != 0L);
      case 48:
         return ((jjbitVec11[i2] & l2) != 0L);
      case 49:
         return ((jjbitVec12[i2] & l2) != 0L);
      case 51:
         return ((jjbitVec13[i2] & l2) != 0L);
      case 61:
         return ((jjbitVec14[i2] & l2) != 0L);
      case 166:
         return ((jjbitVec19[i2] & l2) != 0L);
      case 168:
         return ((jjbitVec20[i2] & l2) != 0L);
      case 169:
         return ((jjbitVec21[i2] & l2) != 0L);
      case 170:
         return ((jjbitVec22[i2] & l2) != 0L);
      case 171:
         return ((jjbitVec23[i2] & l2) != 0L);
      case 251:
         return ((jjbitVec15[i2] & l2) != 0L);
      case 253:
         return ((jjbitVec16[i2] & l2) != 0L);
      case 254:
         return ((jjbitVec17[i2] & l2) != 0L);
      case 255:
         return ((jjbitVec24[i2] & l2) != 0L);
      default :
         if ((jjbitVec9[i1] & l1) != 0L)
            return true;
         return false;
   }
}
private static final boolean jjCanMove_4(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 6:
         return ((jjbitVec30[i2] & l2) != 0L);
      case 7:
         return ((jjbitVec31[i2] & l2) != 0L);
      case 13:
         return ((jjbitVec32[i2] & l2) != 0L);
      case 14:
         return ((jjbitVec33[i2] & l2) != 0L);
      case 16:
         return ((jjbitVec34[i2] & l2) != 0L);
      case 19:
         return ((jjbitVec35[i2] & l2) != 0L);
      case 23:
         return ((jjbitVec36[i2] & l2) != 0L);
      case 25:
         return ((jjbitVec37[i2] & l2) != 0L);
      case 26:
         return ((jjbitVec38[i2] & l2) != 0L);
      case 27:
         return ((jjbitVec39[i2] & l2) != 0L);
      case 28:
         return ((jjbitVec40[i2] & l2) != 0L);
      case 168:
         return ((jjbitVec20[i2] & l2) != 0L);
      case 169:
         return ((jjbitVec21[i2] & l2) != 0L);
      case 170:
         return ((jjbitVec22[i2] & l2) != 0L);
      case 171:
         return ((jjbitVec23[i2] & l2) != 0L);
      default :
         if ((jjbitVec28[i1] & l1) != 0L)
            if ((jjbitVec29[i2] & l2) == 0L)
               return false;
            else
            return true;
         if ((jjbitVec27[i1] & l1) != 0L)
            if ((jjbitVec19[i2] & l2) == 0L)
               return false;
            else
            return true;
         if ((jjbitVec25[i1] & l1) != 0L)
            if ((jjbitVec26[i2] & l2) == 0L)
               return false;
            else
            return true;
         return false;
   }
}

/** Token literal values. */
public static final String[] jjstrLiteralImages = {
"", null, null, null, null, null, null, null, null, null, null, null, "\54", 
"\173", "\175", "\72", "\133", "\135", null, null, null, null, null, null, null, null, 
null, null, "\164\162\165\145", "\146\141\154\163\145", "\156\165\154\154", null, 
null, null, null, "\47\47", "\42\42", null, null, null, null, };
protected Token jjFillToken()
{
   final Token t;
   final String curTokenImage;
   final int beginLine;
   final int endLine;
   final int beginColumn;
   final int endColumn;
   String im = jjstrLiteralImages[jjmatchedKind];
   curTokenImage = (im == null) ? input_stream.GetImage() : im;
   beginLine = input_stream.getBeginLine();
   beginColumn = input_stream.getBeginColumn();
   endLine = input_stream.getEndLine();
   endColumn = input_stream.getEndColumn();
   t = Token.newToken(jjmatchedKind, curTokenImage);

   t.beginLine = beginLine;
   t.endLine = endLine;
   t.beginColumn = beginColumn;
   t.endColumn = endColumn;

   return t;
}

int curLexState = 0;
int defaultLexState = 0;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;

/** Get the next Token. */
public Token getNextToken() 
{
  Token specialToken = null;
  Token matchedToken;
  int curPos = 0;

  EOFLoop :
  for (;;)
  {
   try
   {
      curChar = input_stream.BeginToken();
   }
   catch(Exception e)
   {
      jjmatchedKind = 0;
      jjmatchedPos = -1;
      matchedToken = jjFillToken();
      matchedToken.specialToken = specialToken;
      return matchedToken;
   }
   image = jjimage;
   image.setLength(0);
   jjimageLen = 0;

   for (;;)
   {
     switch(curLexState)
     {
       case 0:
         jjmatchedKind = 0x7fffffff;
         jjmatchedPos = 0;
         curPos = jjMoveStringLiteralDfa0_0();
         break;
       case 1:
         jjmatchedKind = 0x7fffffff;
         jjmatchedPos = 0;
         curPos = jjMoveStringLiteralDfa0_1();
         if (jjmatchedPos == 0 && jjmatchedKind > 8)
         {
            jjmatchedKind = 8;
         }
         break;
       case 2:
         jjmatchedKind = 0x7fffffff;
         jjmatchedPos = 0;
         curPos = jjMoveStringLiteralDfa0_2();
         if (jjmatchedPos == 0 && jjmatchedKind > 9)
         {
            jjmatchedKind = 9;
         }
         break;
     }
     if (jjmatchedKind != 0x7fffffff)
     {
        if (jjmatchedPos + 1 < curPos)
           input_stream.backup(curPos - jjmatchedPos - 1);
        if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
        {
           matchedToken = jjFillToken();
           matchedToken.specialToken = specialToken;
       if (jjnewLexState[jjmatchedKind] != -1)
         curLexState = jjnewLexState[jjmatchedKind];
           return matchedToken;
        }
        else if ((jjtoSkip[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
        {
           if ((jjtoSpecial[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
           {
              matchedToken = jjFillToken();
              if (specialToken == null)
                 specialToken = matchedToken;
              else
              {
                 matchedToken.specialToken = specialToken;
                 specialToken = (specialToken.next = matchedToken);
              }
              SkipLexicalActions(matchedToken);
           }
           else
              SkipLexicalActions(null);
         if (jjnewLexState[jjmatchedKind] != -1)
           curLexState = jjnewLexState[jjmatchedKind];
           continue EOFLoop;
        }
        MoreLexicalActions();
      if (jjnewLexState[jjmatchedKind] != -1)
        curLexState = jjnewLexState[jjmatchedKind];
        curPos = 0;
        jjmatchedKind = 0x7fffffff;
        try {
           curChar = input_stream.readChar();
           continue;
        }
        catch (java.io.IOException e1) { }
     }
     int error_line = input_stream.getEndLine();
     int error_column = input_stream.getEndColumn();
     String error_after = null;
     boolean EOFSeen = false;
     try { input_stream.readChar(); input_stream.backup(1); }
     catch (java.io.IOException e1) {
        EOFSeen = true;
        error_after = curPos <= 1 ? "" : input_stream.GetImage();
        if (curChar == '\n' || curChar == '\r') {
           error_line++;
           error_column = 0;
        }
        else
           error_column++;
     }
     if (!EOFSeen) {
        input_stream.backup(1);
        error_after = curPos <= 1 ? "" : input_stream.GetImage();
     }
     throw new TokenMgrException(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrException.LEXICAL_ERROR);
   }
  }
}

void SkipLexicalActions(Token matchedToken)
{
   switch(jjmatchedKind)
   {
      case 3 :
         image.append(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1)));
                                       commentNesting++; multiLineComment.append("/*");
         break;
      case 5 :
         image.append(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1)));
    commentNesting++;
    multiLineComment.append("/*");
         break;
      case 6 :
         image.append(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1)));
    commentNesting--;
    multiLineComment.append("*/");
    if (commentNesting == 0) {
        SwitchTo(DEFAULT);
    }
         break;
      default :
         break;
   }
}
void MoreLexicalActions()
{
   jjimageLen += (lengthOfMatch = jjmatchedPos + 1);
   switch(jjmatchedKind)
   {
      case 8 :
         image.append(input_stream.GetSuffix(jjimageLen));
         jjimageLen = 0;
      // since image contains the chars collected so far we
         break;
      default :
         break;
   }
}
private void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}

private void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}

    /** Constructor. */
    public JsonParserTokenManager(JavaCharStream stream){

      if (JavaCharStream.staticFlag)
            throw new RuntimeException("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");

    input_stream = stream;
  }

  /** Constructor. */
  public JsonParserTokenManager (JavaCharStream stream, int lexState){
    ReInit(stream);
    SwitchTo(lexState);
  }

  /** Reinitialise parser. */
  public void ReInit(JavaCharStream stream)
  {
	
    jjmatchedPos = jjnewStateCnt = 0;
    curLexState = defaultLexState;
    input_stream = stream;
    ReInitRounds();
  }

  private void ReInitRounds()
  {
    int i;
    jjround = 0x80000001;
    for (i = 40; i-- > 0;)
      jjrounds[i] = 0x80000000;
  }

  /** Reinitialise parser. */
  public void ReInit( JavaCharStream stream, int lexState)
  {
  
    ReInit( stream);
    SwitchTo(lexState);
  }

  /** Switch to specified lex state. */
  public void SwitchTo(int lexState)
  {
    if (lexState >= 3 || lexState < 0)
      throw new TokenMgrException("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrException.INVALID_LEXICAL_STATE);
    else
      curLexState = lexState;
  }

/** Lexer state names. */
public static final String[] lexStateNames = {
   "DEFAULT",
   "WithinMLC",
   "WithinMLH",
};

/** Lex State array. */
public static final int[] jjnewLexState = {
   -1, -1, -1, 1, 2, -1, -1, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
   -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
};
static final long[] jjtoToken = {
   0x198f083f001L, 
};
static final long[] jjtoSkip = {
   0x8feL, 
};
static final long[] jjtoSpecial = {
   0x8feL, 
};
static final long[] jjtoMore = {
   0x300L, 
};
    protected JavaCharStream  input_stream;

    private final int[] jjrounds = new int[40];
    private final int[] jjstateSet = new int[2 * 40];

    private final StringBuilder jjimage = new StringBuilder();
    private StringBuilder image = jjimage;
    private int jjimageLen;
    private int lengthOfMatch;
    
    protected int curChar;
}