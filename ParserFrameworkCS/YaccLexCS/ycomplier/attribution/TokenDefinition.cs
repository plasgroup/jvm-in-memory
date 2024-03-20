using System;
using System.Text.RegularExpressions;
using YaccLexCS.ycomplier.automata;
using YaccLexCS.ycomplier.automata.re;
using YaccLexCS.ycomplier.util;

namespace YaccLexCS.ycomplier.attribution
{
    [AttributeUsage(AttributeTargets.Method, AllowMultiple = true)]
    [Serializable]
    public class TokenDefinition : YDefinition
    {
        public readonly Regex Pattern;
        public readonly string SourcePattern;
        public readonly bool UseRegex;
        public readonly Automata? Automata;
        public string TokenName { get; }

        public readonly int Priority;

        public TokenDefinition(string tokenName, string patternDesc, bool useRegex = false, int priority = 0)
        {

            
            TokenName = tokenName;
            Pattern = useRegex ? new Regex(patternDesc) : StringProcess.StringToRegex(patternDesc);
            Automata = useRegex ? ReAutomata.BuildAutomataFromExp(patternDesc) : null; 
            SourcePattern = patternDesc;
            UseRegex = useRegex;
            Priority = priority;
        }
    }

    
    
    
}