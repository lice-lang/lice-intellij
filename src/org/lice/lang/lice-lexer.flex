package org.lice.lang;

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

<YYINITIAL>
	{WHITE_SPACE}+
		{ return LiceTokenType.WHITE_SPACE; }

<YYINITIAL>
	{COMMENT}
		{ return LiceTokenType.COMMENT; }

<YYINITIAL>
	{LB}
		{ return LiceTokenType.LB; }

<YYINITIAL>
	{STR}
		{ return LiceTokenType.STR; }

<YYINITIAL>
	{SYM}
		{ return LiceTokenType.SYM; }

<YYINITIAL>
	{RB}
		{ return LiceTokenType.RB; }


