/* Generated By:JJTree&JavaCC: Do not edit this line. UQLTreeParserTokenManager.java */
package com.dw.system.codedom.parser;
import java.io.StringReader;
import java.io.*;
import java.util.*;

public class UQLTreeParserTokenManager implements UQLTreeParserConstants
{
  public  java.io.PrintStream debugStream = System.out;
  public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private final int jjStopStringLiteralDfa_0(int pos, long active0, long active1)
{
   switch (pos)
   {
      case 0:
         if ((active0 & 0x4008000000000L) != 0L)
            return 35;
         if ((active0 & 0x80000L) != 0L)
            return 1;
         if ((active0 & 0x17cf80L) != 0L)
         {
            jjmatchedKind = 67;
            return 25;
         }
         return -1;
      case 1:
         if ((active0 & 0x15c680L) != 0L)
         {
            if (jjmatchedPos != 1)
            {
               jjmatchedKind = 67;
               jjmatchedPos = 1;
            }
            return 25;
         }
         if ((active0 & 0x20900L) != 0L)
            return 25;
         return -1;
      case 2:
         if ((active0 & 0x14ce00L) != 0L)
         {
            if (jjmatchedPos != 2)
            {
               jjmatchedKind = 67;
               jjmatchedPos = 2;
            }
            return 25;
         }
         if ((active0 & 0x10080L) != 0L)
            return 25;
         return -1;
      case 3:
         if ((active0 & 0x8800L) != 0L)
         {
            jjmatchedKind = 67;
            jjmatchedPos = 3;
            return 25;
         }
         if ((active0 & 0x144600L) != 0L)
            return 25;
         return -1;
      case 4:
         if ((active0 & 0x800L) != 0L)
         {
            jjmatchedKind = 67;
            jjmatchedPos = 4;
            return 25;
         }
         if ((active0 & 0x8000L) != 0L)
            return 25;
         return -1;
      case 5:
         if ((active0 & 0x800L) != 0L)
         {
            jjmatchedKind = 67;
            jjmatchedPos = 5;
            return 25;
         }
         return -1;
      default :
         return -1;
   }
}
private final int jjStartNfa_0(int pos, long active0, long active1)
{
   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0, active1), pos + 1);
}
private final int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
private final int jjStartNfaWithStates_0(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_0(state, pos + 1);
}
private final int jjMoveStringLiteralDfa0_0()
{
   switch(curChar)
   {
      case 33:
         jjmatchedKind = 24;
         return jjMoveStringLiteralDfa1_0(0x80000000L);
      case 37:
         jjmatchedKind = 43;
         return jjMoveStringLiteralDfa1_0(0x40000000000000L);
      case 38:
         jjmatchedKind = 40;
         return jjMoveStringLiteralDfa1_0(0x8000200000000L);
      case 40:
         return jjStopAtPos(0, 12);
      case 41:
         return jjStopAtPos(0, 13);
      case 42:
         jjmatchedKind = 38;
         return jjMoveStringLiteralDfa1_0(0x2000000000000L);
      case 43:
         jjmatchedKind = 36;
         return jjMoveStringLiteralDfa1_0(0x800400000000L);
      case 44:
         return jjStopAtPos(0, 70);
      case 45:
         jjmatchedKind = 37;
         return jjMoveStringLiteralDfa1_0(0x1000800000000L);
      case 46:
         return jjStartNfaWithStates_0(0, 19, 1);
      case 47:
         jjmatchedKind = 39;
         return jjMoveStringLiteralDfa1_0(0x4000000000000L);
      case 58:
         return jjStopAtPos(0, 27);
      case 59:
         return jjStopAtPos(0, 73);
      case 60:
         jjmatchedKind = 23;
         return jjMoveStringLiteralDfa1_0(0x80100020000000L);
      case 61:
         jjmatchedKind = 21;
         return jjMoveStringLiteralDfa1_0(0x10000000L);
      case 62:
         jjmatchedKind = 22;
         return jjMoveStringLiteralDfa1_0(0x300600040000000L);
      case 63:
         return jjStopAtPos(0, 26);
      case 64:
         return jjStopAtPos(0, 74);
      case 94:
         jjmatchedKind = 42;
         return jjMoveStringLiteralDfa1_0(0x20000000000000L);
      case 97:
         return jjMoveStringLiteralDfa1_0(0x80L);
      case 101:
         return jjMoveStringLiteralDfa1_0(0x40000L);
      case 102:
         return jjMoveStringLiteralDfa1_0(0x8000L);
      case 105:
         return jjMoveStringLiteralDfa1_0(0x20000L);
      case 110:
         return jjMoveStringLiteralDfa1_0(0x100000L);
      case 111:
         return jjMoveStringLiteralDfa1_0(0x900L);
      case 114:
         return jjMoveStringLiteralDfa1_0(0x400L);
      case 116:
         return jjMoveStringLiteralDfa1_0(0x4000L);
      case 117:
         return jjMoveStringLiteralDfa1_0(0x200L);
      case 118:
         return jjMoveStringLiteralDfa1_0(0x10000L);
      case 123:
         return jjStopAtPos(0, 71);
      case 124:
         jjmatchedKind = 41;
         return jjMoveStringLiteralDfa1_0(0x10000100000000L);
      case 125:
         return jjStopAtPos(0, 72);
      case 126:
         return jjStopAtPos(0, 25);
      default :
         return jjMoveNfa_0(0, 0);
   }
}
private final int jjMoveStringLiteralDfa1_0(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(0, active0, 0L);
      return 1;
   }
   switch(curChar)
   {
      case 38:
         if ((active0 & 0x200000000L) != 0L)
            return jjStopAtPos(1, 33);
         break;
      case 43:
         if ((active0 & 0x400000000L) != 0L)
            return jjStopAtPos(1, 34);
         break;
      case 45:
         if ((active0 & 0x800000000L) != 0L)
            return jjStopAtPos(1, 35);
         break;
      case 60:
         if ((active0 & 0x100000000000L) != 0L)
         {
            jjmatchedKind = 44;
            jjmatchedPos = 1;
         }
         return jjMoveStringLiteralDfa2_0(active0, 0x80000000000000L);
      case 61:
         if ((active0 & 0x10000000L) != 0L)
            return jjStopAtPos(1, 28);
         else if ((active0 & 0x20000000L) != 0L)
            return jjStopAtPos(1, 29);
         else if ((active0 & 0x40000000L) != 0L)
            return jjStopAtPos(1, 30);
         else if ((active0 & 0x80000000L) != 0L)
            return jjStopAtPos(1, 31);
         else if ((active0 & 0x800000000000L) != 0L)
            return jjStopAtPos(1, 47);
         else if ((active0 & 0x1000000000000L) != 0L)
            return jjStopAtPos(1, 48);
         else if ((active0 & 0x2000000000000L) != 0L)
            return jjStopAtPos(1, 49);
         else if ((active0 & 0x4000000000000L) != 0L)
            return jjStopAtPos(1, 50);
         else if ((active0 & 0x8000000000000L) != 0L)
            return jjStopAtPos(1, 51);
         else if ((active0 & 0x10000000000000L) != 0L)
            return jjStopAtPos(1, 52);
         else if ((active0 & 0x20000000000000L) != 0L)
            return jjStopAtPos(1, 53);
         else if ((active0 & 0x40000000000000L) != 0L)
            return jjStopAtPos(1, 54);
         break;
      case 62:
         if ((active0 & 0x200000000000L) != 0L)
         {
            jjmatchedKind = 45;
            jjmatchedPos = 1;
         }
         return jjMoveStringLiteralDfa2_0(active0, 0x300400000000000L);
      case 97:
         return jjMoveStringLiteralDfa2_0(active0, 0x18000L);
      case 102:
         if ((active0 & 0x20000L) != 0L)
            return jjStartNfaWithStates_0(1, 17, 25);
         break;
      case 108:
         return jjMoveStringLiteralDfa2_0(active0, 0x40000L);
      case 110:
         return jjMoveStringLiteralDfa2_0(active0, 0x80L);
      case 111:
         return jjMoveStringLiteralDfa2_0(active0, 0x400L);
      case 114:
         if ((active0 & 0x100L) != 0L)
         {
            jjmatchedKind = 8;
            jjmatchedPos = 1;
         }
         return jjMoveStringLiteralDfa2_0(active0, 0x4800L);
      case 115:
         return jjMoveStringLiteralDfa2_0(active0, 0x200L);
      case 117:
         return jjMoveStringLiteralDfa2_0(active0, 0x100000L);
      case 124:
         if ((active0 & 0x100000000L) != 0L)
            return jjStopAtPos(1, 32);
         break;
      default :
         break;
   }
   return jjStartNfa_0(0, active0, 0L);
}
private final int jjMoveStringLiteralDfa2_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(0, old0, 0L);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(1, active0, 0L);
      return 2;
   }
   switch(curChar)
   {
      case 61:
         if ((active0 & 0x80000000000000L) != 0L)
            return jjStopAtPos(2, 55);
         else if ((active0 & 0x100000000000000L) != 0L)
            return jjStopAtPos(2, 56);
         break;
      case 62:
         if ((active0 & 0x400000000000L) != 0L)
         {
            jjmatchedKind = 46;
            jjmatchedPos = 2;
         }
         return jjMoveStringLiteralDfa3_0(active0, 0x200000000000000L);
      case 100:
         if ((active0 & 0x80L) != 0L)
            return jjStartNfaWithStates_0(2, 7, 25);
         break;
      case 101:
         return jjMoveStringLiteralDfa3_0(active0, 0x200L);
      case 103:
         return jjMoveStringLiteralDfa3_0(active0, 0x800L);
      case 108:
         return jjMoveStringLiteralDfa3_0(active0, 0x108400L);
      case 114:
         if ((active0 & 0x10000L) != 0L)
            return jjStartNfaWithStates_0(2, 16, 25);
         break;
      case 115:
         return jjMoveStringLiteralDfa3_0(active0, 0x40000L);
      case 117:
         return jjMoveStringLiteralDfa3_0(active0, 0x4000L);
      default :
         break;
   }
   return jjStartNfa_0(1, active0, 0L);
}
private final int jjMoveStringLiteralDfa3_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(1, old0, 0L);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(2, active0, 0L);
      return 3;
   }
   switch(curChar)
   {
      case 61:
         if ((active0 & 0x200000000000000L) != 0L)
            return jjStopAtPos(3, 57);
         break;
      case 101:
         if ((active0 & 0x400L) != 0L)
            return jjStartNfaWithStates_0(3, 10, 25);
         else if ((active0 & 0x4000L) != 0L)
            return jjStartNfaWithStates_0(3, 14, 25);
         else if ((active0 & 0x40000L) != 0L)
            return jjStartNfaWithStates_0(3, 18, 25);
         break;
      case 108:
         if ((active0 & 0x100000L) != 0L)
            return jjStartNfaWithStates_0(3, 20, 25);
         break;
      case 114:
         if ((active0 & 0x200L) != 0L)
            return jjStartNfaWithStates_0(3, 9, 25);
         break;
      case 115:
         return jjMoveStringLiteralDfa4_0(active0, 0x8000L);
      case 117:
         return jjMoveStringLiteralDfa4_0(active0, 0x800L);
      default :
         break;
   }
   return jjStartNfa_0(2, active0, 0L);
}
private final int jjMoveStringLiteralDfa4_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(2, old0, 0L);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(3, active0, 0L);
      return 4;
   }
   switch(curChar)
   {
      case 101:
         if ((active0 & 0x8000L) != 0L)
            return jjStartNfaWithStates_0(4, 15, 25);
         break;
      case 110:
         return jjMoveStringLiteralDfa5_0(active0, 0x800L);
      default :
         break;
   }
   return jjStartNfa_0(3, active0, 0L);
}
private final int jjMoveStringLiteralDfa5_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(3, old0, 0L);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(4, active0, 0L);
      return 5;
   }
   switch(curChar)
   {
      case 105:
         return jjMoveStringLiteralDfa6_0(active0, 0x800L);
      default :
         break;
   }
   return jjStartNfa_0(4, active0, 0L);
}
private final int jjMoveStringLiteralDfa6_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(4, old0, 0L);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(5, active0, 0L);
      return 6;
   }
   switch(curChar)
   {
      case 116:
         if ((active0 & 0x800L) != 0L)
            return jjStartNfaWithStates_0(6, 11, 25);
         break;
      default :
         break;
   }
   return jjStartNfa_0(5, active0, 0L);
}
private final void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private final void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private final void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}
private final void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}
private final void jjCheckNAddStates(int start)
{
   jjCheckNAdd(jjnextStates[start]);
   jjCheckNAdd(jjnextStates[start + 1]);
}
static final long[] jjbitVec0 = {
   0xfffffffffffffffeL, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec2 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec3 = {
   0x1ff00000fffffffeL, 0xffffffffffffc000L, 0xffffffffL, 0x600000000000000L
};
static final long[] jjbitVec4 = {
   0x0L, 0x0L, 0x0L, 0xff7fffffff7fffffL
};
static final long[] jjbitVec5 = {
   0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec6 = {
   0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffL, 0x0L
};
static final long[] jjbitVec7 = {
   0xffffffffffffffffL, 0xffffffffffffffffL, 0x0L, 0x0L
};
static final long[] jjbitVec8 = {
   0x3fffffffffffL, 0x0L, 0x0L, 0x0L
};
private final int jjMoveNfa_0(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 62;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 35:
                  if (curChar == 42)
                     jjCheckNAddTwoStates(41, 42);
                  else if (curChar == 47)
                     jjCheckNAddStates(0, 2);
                  break;
               case 0:
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 58)
                        kind = 58;
                     jjCheckNAddStates(3, 11);
                  }
                  else if (curChar == 47)
                     jjAddStates(12, 13);
                  else if (curChar == 36)
                  {
                     if (kind > 67)
                        kind = 67;
                     jjCheckNAdd(25);
                  }
                  else if (curChar == 34)
                     jjCheckNAddStates(14, 16);
                  else if (curChar == 39)
                     jjAddStates(17, 18);
                  else if (curChar == 46)
                     jjCheckNAdd(1);
                  if (curChar == 48)
                     jjAddStates(19, 21);
                  break;
               case 1:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 63)
                     kind = 63;
                  jjCheckNAddStates(22, 24);
                  break;
               case 3:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(4);
                  break;
               case 4:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 63)
                     kind = 63;
                  jjCheckNAddTwoStates(4, 5);
                  break;
               case 6:
                  if (curChar == 39)
                     jjAddStates(17, 18);
                  break;
               case 7:
                  if ((0xffffff7fffffdbffL & l) != 0L)
                     jjCheckNAdd(8);
                  break;
               case 8:
                  if (curChar == 39 && kind > 65)
                     kind = 65;
                  break;
               case 10:
                  if ((0x8400000000L & l) != 0L)
                     jjCheckNAdd(8);
                  break;
               case 11:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(12, 8);
                  break;
               case 12:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAdd(8);
                  break;
               case 13:
                  if ((0xf000000000000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 14;
                  break;
               case 14:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAdd(12);
                  break;
               case 15:
                  if (curChar == 34)
                     jjCheckNAddStates(14, 16);
                  break;
               case 16:
                  if ((0xfffffffbffffdbffL & l) != 0L)
                     jjCheckNAddStates(14, 16);
                  break;
               case 18:
                  if ((0x8400000000L & l) != 0L)
                     jjCheckNAddStates(14, 16);
                  break;
               case 19:
                  if (curChar == 34 && kind > 66)
                     kind = 66;
                  break;
               case 20:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAddStates(25, 28);
                  break;
               case 21:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAddStates(14, 16);
                  break;
               case 22:
                  if ((0xf000000000000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 23;
                  break;
               case 23:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAdd(21);
                  break;
               case 24:
                  if (curChar != 36)
                     break;
                  if (kind > 67)
                     kind = 67;
                  jjCheckNAdd(25);
                  break;
               case 25:
                  if ((0x3ff001000000000L & l) == 0L)
                     break;
                  if (kind > 67)
                     kind = 67;
                  jjCheckNAdd(25);
                  break;
               case 26:
                  if (curChar == 48)
                     jjAddStates(19, 21);
                  break;
               case 28:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 58)
                     kind = 58;
                  jjCheckNAddTwoStates(28, 29);
                  break;
               case 31:
                  if ((0xff000000000000L & l) == 0L)
                     break;
                  if (kind > 58)
                     kind = 58;
                  jjCheckNAddTwoStates(31, 29);
                  break;
               case 33:
                  if ((0x3000000000000L & l) == 0L)
                     break;
                  if (kind > 58)
                     kind = 58;
                  jjCheckNAddTwoStates(33, 29);
                  break;
               case 34:
                  if (curChar == 47)
                     jjAddStates(12, 13);
                  break;
               case 36:
                  if ((0xffffffffffffdbffL & l) != 0L)
                     jjCheckNAddStates(0, 2);
                  break;
               case 37:
                  if ((0x2400L & l) != 0L && kind > 5)
                     kind = 5;
                  break;
               case 38:
                  if (curChar == 10 && kind > 5)
                     kind = 5;
                  break;
               case 39:
                  if (curChar == 13)
                     jjstateSet[jjnewStateCnt++] = 38;
                  break;
               case 40:
                  if (curChar == 42)
                     jjCheckNAddTwoStates(41, 42);
                  break;
               case 41:
                  if ((0xfffffbffffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(41, 42);
                  break;
               case 42:
                  if (curChar == 42)
                     jjAddStates(29, 30);
                  break;
               case 43:
                  if ((0xffff7fffffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(44, 42);
                  break;
               case 44:
                  if ((0xfffffbffffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(44, 42);
                  break;
               case 45:
                  if (curChar == 47 && kind > 6)
                     kind = 6;
                  break;
               case 46:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 58)
                     kind = 58;
                  jjCheckNAddStates(3, 11);
                  break;
               case 47:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 58)
                     kind = 58;
                  jjCheckNAddTwoStates(47, 29);
                  break;
               case 48:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(48, 49);
                  break;
               case 49:
                  if (curChar != 46)
                     break;
                  if (kind > 63)
                     kind = 63;
                  jjCheckNAddStates(31, 33);
                  break;
               case 50:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 63)
                     kind = 63;
                  jjCheckNAddStates(31, 33);
                  break;
               case 52:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(53);
                  break;
               case 53:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 63)
                     kind = 63;
                  jjCheckNAddTwoStates(53, 5);
                  break;
               case 54:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(54, 55);
                  break;
               case 56:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(57);
                  break;
               case 57:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 63)
                     kind = 63;
                  jjCheckNAddTwoStates(57, 5);
                  break;
               case 58:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddStates(34, 36);
                  break;
               case 60:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(61);
                  break;
               case 61:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(61, 5);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
               case 25:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 67)
                     kind = 67;
                  jjCheckNAdd(25);
                  break;
               case 2:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(37, 38);
                  break;
               case 5:
                  if ((0x5000000050L & l) != 0L && kind > 63)
                     kind = 63;
                  break;
               case 7:
                  if ((0xffffffffefffffffL & l) != 0L)
                     jjCheckNAdd(8);
                  break;
               case 9:
                  if (curChar == 92)
                     jjAddStates(39, 41);
                  break;
               case 10:
                  if ((0x14404410000000L & l) != 0L)
                     jjCheckNAdd(8);
                  break;
               case 16:
                  if ((0xffffffffefffffffL & l) != 0L)
                     jjCheckNAddStates(14, 16);
                  break;
               case 17:
                  if (curChar == 92)
                     jjAddStates(42, 44);
                  break;
               case 18:
                  if ((0x14404410000000L & l) != 0L)
                     jjCheckNAddStates(14, 16);
                  break;
               case 27:
                  if ((0x100000001000000L & l) != 0L)
                     jjCheckNAdd(28);
                  break;
               case 28:
                  if ((0x7e0000007eL & l) == 0L)
                     break;
                  if (kind > 58)
                     kind = 58;
                  jjCheckNAddTwoStates(28, 29);
                  break;
               case 29:
                  if ((0x8100000081000L & l) != 0L && kind > 58)
                     kind = 58;
                  break;
               case 30:
                  if ((0x800000008000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 31;
                  break;
               case 32:
                  if ((0x400000004L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 33;
                  break;
               case 36:
                  jjAddStates(0, 2);
                  break;
               case 41:
                  jjCheckNAddTwoStates(41, 42);
                  break;
               case 43:
               case 44:
                  jjCheckNAddTwoStates(44, 42);
                  break;
               case 51:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(45, 46);
                  break;
               case 55:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(47, 48);
                  break;
               case 59:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(49, 50);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
               case 25:
                  if (!jjCanMove_1(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 67)
                     kind = 67;
                  jjCheckNAdd(25);
                  break;
               case 7:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjstateSet[jjnewStateCnt++] = 8;
                  break;
               case 16:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjAddStates(14, 16);
                  break;
               case 36:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjAddStates(0, 2);
                  break;
               case 41:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(41, 42);
                  break;
               case 43:
               case 44:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(44, 42);
                  break;
               default : break;
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
      if ((i = jjnewStateCnt) == (startsAt = 62 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
static final int[] jjnextStates = {
   36, 37, 39, 47, 29, 48, 49, 54, 55, 58, 59, 5, 35, 40, 16, 17, 
   19, 7, 9, 27, 30, 32, 1, 2, 5, 16, 17, 21, 19, 43, 45, 50, 
   51, 5, 58, 59, 5, 3, 4, 10, 11, 13, 18, 20, 22, 52, 53, 56, 
   57, 60, 61, 
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
         return ((jjbitVec4[i2] & l2) != 0L);
      case 48:
         return ((jjbitVec5[i2] & l2) != 0L);
      case 49:
         return ((jjbitVec6[i2] & l2) != 0L);
      case 51:
         return ((jjbitVec7[i2] & l2) != 0L);
      case 61:
         return ((jjbitVec8[i2] & l2) != 0L);
      default : 
         if ((jjbitVec3[i1] & l1) != 0L)
            return true;
         return false;
   }
}
public static final String[] jjstrLiteralImages = {
"", null, null, null, null, null, null, "\141\156\144", "\157\162", 
"\165\163\145\162", "\162\157\154\145", "\157\162\147\165\156\151\164", "\50", "\51", 
"\164\162\165\145", "\146\141\154\163\145", "\166\141\162", "\151\146", "\145\154\163\145", "\56", 
"\156\165\154\154", "\75", "\76", "\74", "\41", "\176", "\77", "\72", "\75\75", "\74\75", 
"\76\75", "\41\75", "\174\174", "\46\46", "\53\53", "\55\55", "\53", "\55", "\52", 
"\57", "\46", "\174", "\136", "\45", "\74\74", "\76\76", "\76\76\76", "\53\75", 
"\55\75", "\52\75", "\57\75", "\46\75", "\174\75", "\136\75", "\45\75", "\74\74\75", 
"\76\76\75", "\76\76\76\75", null, null, null, null, null, null, null, null, null, null, 
null, null, "\54", "\173", "\175", "\73", "\100", };
public static final String[] lexStateNames = {
   "DEFAULT", 
};
static final long[] jjtoToken = {
   0x87ffffffffffff81L, 0x7ceL, 
};
static final long[] jjtoSkip = {
   0x7eL, 0x0L, 
};
protected JavaCharStream input_stream;
private final int[] jjrounds = new int[62];
private final int[] jjstateSet = new int[124];
protected char curChar;
public UQLTreeParserTokenManager(JavaCharStream stream)
{
   if (JavaCharStream.staticFlag)
      throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
   input_stream = stream;
}
public UQLTreeParserTokenManager(JavaCharStream stream, int lexState)
{
   this(stream);
   SwitchTo(lexState);
}
public void ReInit(JavaCharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private final void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 62; i-- > 0;)
      jjrounds[i] = 0x80000000;
}
public void ReInit(JavaCharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}
public void SwitchTo(int lexState)
{
   if (lexState >= 1 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

protected Token jjFillToken()
{
   Token t = Token.newToken(jjmatchedKind);
   t.kind = jjmatchedKind;
   String im = jjstrLiteralImages[jjmatchedKind];
   t.image = (im == null) ? input_stream.GetImage() : im;
   t.beginLine = input_stream.getBeginLine();
   t.beginColumn = input_stream.getBeginColumn();
   t.endLine = input_stream.getEndLine();
   t.endColumn = input_stream.getEndColumn();
   return t;
}

int curLexState = 0;
int defaultLexState = 0;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;

public Token getNextToken() 
{
  int kind;
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
   catch(java.io.IOException e)
   {        
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      return matchedToken;
   }

   try { input_stream.backup(0);
      while (curChar <= 32 && (0x100002600L & (1L << curChar)) != 0L)
         curChar = input_stream.BeginToken();
   }
   catch (java.io.IOException e1) { continue EOFLoop; }
   jjmatchedKind = 0x7fffffff;
   jjmatchedPos = 0;
   curPos = jjMoveStringLiteralDfa0_0();
   if (jjmatchedKind != 0x7fffffff)
   {
      if (jjmatchedPos + 1 < curPos)
         input_stream.backup(curPos - jjmatchedPos - 1);
      if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
      {
         matchedToken = jjFillToken();
         return matchedToken;
      }
      else
      {
         continue EOFLoop;
      }
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
   throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
  }
}

}
