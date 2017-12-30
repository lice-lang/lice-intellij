package org.lice.lang;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import org.lice.lang.psi.LiceTypes;

%%

%class LiceLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{ return;
%eof}

COMMENT=;[^\n]*
WHITE_SPACE=[, \n\r\t]
LBRACKET=\(
RBRACKET=\)

STRING_LITERAL=\"([^\"]*)\"

SYNBOL=[a-zA-Z!@#$%\^&*_=:<>.?/\\+\-*/%\[\]{}|]+

HEX_NUM=0[xX][0-9a-fA-F]+
OCT_NUM=0[oO][0-7]+
BIN_NUM=0[bB][01]+
DEC_NUM=[0-9]+[dDfFbBsSlLnNmM]

FLOAT=[0-9]+\.[0-9]+[dDfFmM]

%%

<YYINITIAL>
	{COMMENT}
		{ yybegin(YYINITIAL); return LiceTypes.COMMENT; }

<YYINITIAL>
	{LBRACKET}
		{ yybegin(YYINITIAL); return LiceTypes.LEFT_BRACKET; }

<YYINITIAL>
	{STRING_LITERAL}
		{ yybegin(YYINITIAL); return LiceTypes.STR; }

<YYINITIAL>
	{SYNBOL}
		{ yybegin(YYINITIAL); return LiceTypes.SYM; }

<YYINITIAL>
	{RBRACKET}
		{ yybegin(YYINITIAL); return LiceTypes.RIGHT_BRACKET; }

{WHITE_SPACE}+
	{ yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }
