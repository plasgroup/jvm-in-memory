using System.Text.RegularExpressions;

namespace YaccLexCS.ycomplier.util
{
    public class StringProcess
    {
        public static Regex StringToRegex(string str)
        {
            return
                new(
                    str.Replace("\\", "\\\\").Replace("$", "\\$")
                        .Replace("(", "\\(").Replace(")", "\\)")
                        .Replace("*", "\\*").Replace("+", "\\+")
                        .Replace(".", "\\.").Replace("[", "\\[")
                        .Replace("]", "\\]").Replace("?", "\\?")
                        .Replace("^", "\\^").Replace("{", "\\{")
                        .Replace("}", "\\}").Replace("|", "\\|")
                );
        }
    }
}