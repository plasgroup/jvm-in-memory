namespace YaccLexCS
{
    public class Token
    {
        public string SourceText;
        public (int depth, int order) LexicalDistance = (-1, -1);
        public string Type { get; }
        public int LineNum;
        public Token(string sourceText, string type)
        {
            SourceText = sourceText;
            Type = type;
        }

        public override string ToString()
        {
            return $"<{Type}, {SourceText}>";
        }
    }
}