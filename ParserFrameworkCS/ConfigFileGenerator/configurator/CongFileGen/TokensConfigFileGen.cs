using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ConfigFileGenerator.configurator.CongFileGen
{
    public static class TokensConfigFileGen
    {
        public static string[] Header =
        {
            "using System;",
            "using YaccLexCS.ycomplier;",
            "using YaccLexCS.ycomplier.attribution;"
        };

        public static string NameSpace = "YaccLexCS.config";
        public static string ClassName = "TokenList";

       
        const string T6 = "            ";
        const string T4 = "        ";
        const string T2 = "    ";
        const string LE = "\r\n";

        private static string MethodGen(TokenMethod method)
        {
            var sb = new StringBuilder();
            string FormalDescription(string desc, bool isRegex = false)
            {
                return (isRegex ? "@" : "") + "\"" + desc.Replace("\"","\\\"") + "\"";
            }

            void AddAttrLine(TokenMethod.TokenAttributeDesc desc) => 
                sb.Append($"{T4}[TokenDefinition(\"{desc.Name}\", {FormalDescription(desc.Desc, desc.IsRegex)}, {(desc.IsRegex + "").ToLower()}, {desc.Priority})]{LE}");

            foreach (var d in method.Descs) AddAttrLine(d);

            sb.Append($"{T4}public static void {method.MethodName}(CompilerContext content){LE}" + T4 + "{" + $"{LE}{T4}" + "}");
            return sb + "";
        }
        
        public static string GenFileContentString(List<TokenMethod> methods)
        {
            var sb = new StringBuilder();
            sb.Append(Header.Aggregate("", (a, b) => a + "\r\n" + b));
            sb.Append("\r\n");
            sb.Append("namespace " + NameSpace + "\r\n{\r\n");
            sb.Append("    [TokenConfiguration]\r\n");
            sb.Append("    public static class " + ClassName + "\r\n    {\r\n");
            foreach (var m in methods) sb.Append("\r\n" + MethodGen(m) + "\r\n");
            sb.Append("    }\r\n");
            sb.Append('}');
            sb.PrintToConsole();
            return sb + "";
        }
    }
}