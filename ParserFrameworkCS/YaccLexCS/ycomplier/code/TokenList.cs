
using System;
using YaccLexCS.ycomplier;
using YaccLexCS.ycomplier.attribution;
namespace YaccLexCS.config
{
    [TokenConfiguration]
    public static class TokenList
    { 
        [TokenDefinition("FROM", "from", false, 0)]
        public static void From(CompilerContext content)
        {
        }

        [TokenDefinition("SELECT", "select", false, 0)]
        public static void Select(CompilerContext content)
        {
        }

        [TokenDefinition("IN", "in", false, 0)]
        public static void In(CompilerContext content)
        {
        }

        [TokenDefinition("RETURN", "return", false, 0)]
        public static void Return(CompilerContext content)
        {
        }


        [TokenDefinition("DOT", ".", false, 0)]
        public static void Dot(CompilerContext content)
        {
        }

        [TokenDefinition("JOIN", "join", false, 0)]
        public static void Join(CompilerContext content)
        {
        }

        [TokenDefinition("WHERE", "where", false, 0)]
        public static void Where(CompilerContext content)
        {
        }

        [TokenDefinition("IS", "is", false, 0)]
        public static void Is(CompilerContext content)
        {
        }

        [TokenDefinition("EXCLAIMATION_MARK", "!", false, 0)]
        public static void ExclaimationMark(CompilerContext content)
        {
        }

        [TokenDefinition("LESS", "<", false, 0)]
        public static void Less(CompilerContext content)
        {
        }

        [TokenDefinition("GREATER", ">", false, 0)]
        public static void Greater(CompilerContext content)
        {
        }

        [TokenDefinition("MOD", "%", false, 0)]
        public static void Mod(CompilerContext content)
        {
        }

        [TokenDefinition("DEF_TASK", "def_task", false, 0)]
        public static void DefTask(CompilerContext content)
        {
        }

        [TokenDefinition("LP", "(", false, 0)]
        public static void Lp(CompilerContext content)
        {
        }

        [TokenDefinition("RP", ")", false, 0)]
        public static void Rp(CompilerContext content)
        {
        }

        [TokenDefinition("LSquareB", "[", false, 0)]
        public static void LSquareB(CompilerContext content)
        {
        }

        [TokenDefinition("RSquareB", "]", false, 0)]
        public static void RSquareB(CompilerContext content)
        {
        }
        [TokenDefinition("LB", "{", false, 0)]
        public static void Lb(CompilerContext content)
        {
        }


        [TokenDefinition("RB", "}", false, 0)]
        public static void Rb(CompilerContext content)
        {
        }

        [TokenDefinition("SEMICOLON", ";", false, 0)]
        public static void Semicolon(CompilerContext content)
        {
        }


        [TokenDefinition("ASSIGN", "=", false, 0)]
        public static void Assign(CompilerContext content)
        {
        }


        [TokenDefinition("EQ", "==", false, 0)]
        public static void Eq(CompilerContext content)
        {
        }

        [TokenDefinition("HOST_BLOCK", @"<\%\%(.)*\%\%>", true, 0)]
        public static void HostBlock(CompilerContext content)
        {
        }

        [TokenDefinition("HOST_EXPRESSION", @"<\%(.)*\%>", true, 0)]
        public static void HostExpression(CompilerContext content)
        {
        }

        [TokenDefinition("HOST_TYPE", @"<@_*[a-zA-Z][_a-zA-Z0-9]*(\[\])?>", true, 0)]
        public static void HostType(CompilerContext content)
        {
        }

        [TokenDefinition("CAPTURE_VARIABLE", @"<\!_*[a-zA-Z][_a-zA-Z0-9]*(\[\])?>", true, 0)]
        public static void CaptureVariable(CompilerContext content)
        {
        }

        [TokenDefinition("IDENTIFIER", @"[A-Z_a-z]+|[A-Z_a-z]+[0-9_A-Za-z]", true, 1)]
        public static void Identifier(CompilerContext content)
        {
            $"[lexer] Encounter identifier {content.CurrentRecognizedTokenName}".PrintToConsole();
        }

        [TokenDefinition("HEX_INTEGER_LITERAL", @"0[xX](_*[0-9a-fA-F])+([lL]?[uU]|[uU]?[lL])?", true, 0)]
        public static void HexIntegerLiteral(CompilerContext content)
        {
        }

        [TokenDefinition("BIN_INTEGER_LITERAL", @"0[bB](_*[01])+([lL]?[uU]|[uU]?[lL])?", true, 0)]
        public static void BinIntegerLiteral(CompilerContext content)
        {
        }

        [TokenDefinition("INTEGER_LITERAL", @"[0-9](_*[0-9])*([lL]?[uU]|[uU]?[lL])?", true, 0)]
        public static void IntegerLiteral(CompilerContext content)
        {
        }

        [TokenDefinition("SINGLE_LINE_COMMENT", @"\/\/[^\r\n]+", true, 0)]
        public static void SingleLineComment(CompilerContext content)
        {
        }

        [TokenDefinition("DSL_ACCESSOR", "::", false, 0)]
        public static void DslAccessor(CompilerContext content)
        {
        }

        [TokenDefinition("RIGHT_ARROW", "->", false, 0)]
        public static void RightArrow(CompilerContext content)
        {
        }


        [TokenDefinition("COMMA", ",", false, 0)]
        public static void Comma(CompilerContext content)
        {
        }


        [TokenDefinition("ERR", ".", true, 255)]
        public static void Error(CompilerContext content)
        {
            $"error token!".PrintToConsole();
            
            throw new Exception();
        }

        [TokenDefinition("Skip", @"[ \r\n\t]", true)]
        public static void Skip(CompilerContext context) { }
    }
}