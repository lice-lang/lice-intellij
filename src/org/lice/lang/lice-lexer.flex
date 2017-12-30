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
SYNBOL_OR_NUMBER=([^;, \n\r\t\(\)])+
STRING_LITERAL=\"([^\"]*)\"
LBRACKET=\(
RBRACKET=\)

%%

<YYINITIAL>
	{COMMENT}
		{ yybegin(YYINITIAL); return LiceTokenType.COMMENT; }

<YYINITIAL>
	{LBRACKET}
		{ yybegin(YYINITIAL); return LiceTokenType.LB; }

<YYINITIAL>
	{STRING_LITERAL}
		{ yybegin(YYINITIAL); return LiceTypes.STR; }

<YYINITIAL>
	{SYNBOL_OR_NUMBER}
		{ yybegin(YYINITIAL); return LiceTypes.SYM; }

<YYINITIAL>
	{RBRACKET}
		{ yybegin(YYINITIAL); return LiceTokenType.RB; }

{WHITE_SPACE}+
	{ yybegin(YYINITIAL); return LiceTokenType.WHITE_SPACE; }
