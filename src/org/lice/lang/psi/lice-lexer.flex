package org.lice.lang.psi;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

%%

%class LiceLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType

COMMENT=;[^\n]*
WHITE_SPACE=[, \n\r\t]
SYM=([^;, \n\r\t])+
STR=\"[^\"]*\"
LB=\(
RB=\)

%%

<YYINITIAL> {
  {WHITE_SPACE}+
    { return LiceTokenType.WHITE_SPACE; }
  {COMMENT}
    { return LiceTokenType.COMMENT; }
  {LB}
    { return LiceTokenType.LB; }
  {STR}
    { return LiceTokenType.STR; }
  {SYM}
    { return LiceTokenType.SYM; }
  {RB}
    { return LiceTokenType.RB; }
}

