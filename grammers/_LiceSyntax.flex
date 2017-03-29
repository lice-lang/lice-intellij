package org.lice.lang;

import com.intellij.lang.Language;
import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import org.lice.lang.psi.LiceTypes;

import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.intellij.psi.TokenType.BAD_CHARACTER;

%%

%public
%class LiceLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{
    return;
%eof}


CRLF = \r
WHITE_SPACE = [\ \n\t\f]
END_OF_LINE_COMMENT = (";")[^\r\n]*
STRING = \"[^\"]*\"

%state WAITING_VALUE

%%
<YYINITIAL> {END_OF_LINE_COMMENT}                   { yybegin(YYINITIAL); return LiceTypes.COMMENT; }
<WAITING_VALUE> {CRLF}({CRLF}|{WHITE_SPACE})+       { yybegin(YYINITIAL); return WHITE_SPACE; }
<WAITING_VALUE> {WHITE_SPACE}+                      { yybegin(WAITING_VALUE); return WHITE_SPACE; }
"null"                                              { return C_NULL; }
true|false                                          { return C_BOOL; }
.                                                   { return BAD_CHARACTER; }
