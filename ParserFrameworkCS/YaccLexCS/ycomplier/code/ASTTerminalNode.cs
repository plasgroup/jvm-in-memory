using System;
using System.Collections.Generic;
using YaccLexCS.runtime;

namespace YaccLexCS.ycomplier.code
{
    public class ASTTerminalNode : ASTNode
    {
        public Token Token { get; }
        public dynamic Value { get; }

        public ASTTerminalNode(Token token) : base(token.Type)
        {
            Token = token;
        }

        public override ASTNode? Child(int i)
        {
            return null;
        }

        public override dynamic Eval(RuntimeContext context)
        {
            return Token;
        }

        public override IEnumerable<ASTNode> Children()
        {
            return Array.Empty<ASTNode>();
        }

        public override string Location()
        {
            return $"at line {Token.LineNum}";
        }

        public override string GetSourceText()
        {
            return Token.SourceText;
        }
    }
}