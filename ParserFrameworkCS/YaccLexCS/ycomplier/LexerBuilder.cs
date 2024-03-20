using System.Collections.Generic;

namespace YaccLexCS.ycomplier
{
    public class LexerBuilder : Builder{
        public static Lexer ConfigureFromPackages(IEnumerable<string> scanPackage, CompilerContext context)
        {
            var lexer = new Lexer(context);
            YCompilerConfigurator.GetAllTokenDefinitions(
                YCompilerConfigurator.ScanTokenConfiguration(scanPackage)).ElementInvoke(e =>
            {
                if (!e.tokenDef.UseRegex)
                {
                    lexer.PatternMap[e.tokenDef] = e.methodInfo!;
                }
                else
                {
                    lexer.PatternMap[e.tokenDef] = e.methodInfo!;
                }
            });
            return lexer;
        }
    }
}