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

DIGIT=[0-9]

HEX_NUM=0[xX][0-9a-fA-F]+
OCT_NUM=0[oO][0-7]+
BIN_NUM=0[bB][01]+
DEC_NUM={DIGIT}+[dDfFbBsSlLnNmM]?
FLOAT={DIGIT}+\.{DIGIT}+[dDfFmM]?
NUMBER={BIN_NUM}|{OCT_NUM}|{DEC_NUM}|{HEX_NUM}|{FLOAT}

SYMBOL_CHAR=[a-zA-Z!@#$%\^&*_=:<>.?/\\+\-*/%\[\]{}|]
SYMBOL={SYMBOL_CHAR}({SYMBOL_CHAR}|{DIGIT})*

%state AFTER_NUM

%%

<YYINITIAL> {COMMENT}
		{ return LiceTypes.COMMENT; }

<YYINITIAL> {LBRACKET}
		{ return LiceTypes.LEFT_BRACKET; }

<YYINITIAL> {STRING_LITERAL}
		{ return LiceTypes.STR; }

<YYINITIAL> {SYMBOL}
		{ return LiceTypes.SYM; }

<YYINITIAL> {RBRACKET}
		{ return LiceTypes.RIGHT_BRACKET; }

<YYINITIAL> {NUMBER}
		{ yybegin(AFTER_NUM); return LiceTypes.NUMBER; }

<AFTER_NUM> {
	{SYMBOL}
		{ return TokenType.BAD_CHARACTER; }
}

{WHITE_SPACE}+
	{ yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }

