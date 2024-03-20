using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using ConfigFileGenerator.configurator.CongFileGen;

namespace ConfigFileGenerator.configurator
{
    public class EvaluationListFileGen
    {
        public static string[] Header =
        {
            "using System;",
            "using System.Collections.Generic;",
            "using System.Linq;",
            "using System.Reflection;",
            "using YaccLexCS.code.structure;",
            "using YaccLexCS.runtime;"
        };

        public static string NameSpace = "YaccLexCS.ycomplier.code.structure";
        public static string ClassName = "EvaluationConfiguration";
        const string T6 = "            ";
        const string T4 = "        ";
        const string T2 = "    ";
        const string T8 = "                ";
        const string LE = "\r\n";
        private const string LB4 = $"{T4}" + "{";
        private const string RB4 = $"{T4}" + "}";

        const string LazyMappingCode = 
            $"{T4}public static readonly Dictionary<string, MethodInfo> ClassNameMapping\r\n"+
            $"{T6}= new Lazy<Dictionary<string, MethodInfo>>(() =>\r\n" +
            T6 + "{\r\n" +
            T8 + " var methods = typeof(EvaluationConfiguration).GetMethods();" + LE +
            T8 + " var result = methods" + LE +
            T8 + " .Where(m => m.IsStatic)" + LE +
            T8 +" .ToDictionary(kv => kv.Name, kv => kv);" + LE +
            T8 +"return result;" + LE +
            T6 +"}).Value;" + LE;
        
        private static string PatternMatchGen(int count, string cfgDesc, string[] typeNames)
        {
            
            var sb = new StringBuilder();
            var comment = $"/*{cfgDesc}*/\r\n";
            sb.Append(T6 + comment);

            string ToIf()
            {
               
                var sb2 = new StringBuilder();
                var typeMatchString = typeNames.Select((t, i) => $"node[{i}].GetType().IsAssignableFrom(typeof({t}"
                    + (t == "ASTTerminalNode" ?"" : "Node")+
                "))");
                sb2.Append($"{T6}if(node.Count() == {count} "
                           + typeMatchString.Aggregate("", (a, b) => a + "\r\n"+ T8 + " && " + b)
               );
                return sb2 + "";
            }
            sb.Append(ToIf());
            sb.Append(")");
            return sb + "";
        }
        private static string MethodGen(KeyValuePair<string, (string folder, List<string> cfgs)> method, HashSet<string> nonTerminals)
        {
            var sb = new StringBuilder();

            string FormalizeName(string s) => s.Split("_")
                .Select(w => w[0..1].ToUpper() + w[1..].ToLower())
                .Aggregate("", (a, b) => a + b);

            var nodeName = FormalizeName(method.Key) + "Node";
            
            void AddMethodLine(string funcName) => 
                sb.Append($"{T4}public static dynamic {funcName}({funcName} node, RuntimeContext context)\r\n");

            AddMethodLine(nodeName);
            sb.Append(LB4 + LE);
            sb.Append(T6 + "throw new NotImplementedException();" + LE);

            // var groups = method.Value.cfgs.Select(ws => ws.Split(" "))
            //     .Select(ws => ws.Select(w => nonTerminals.Contains(w) ? FormalizeName(w) : "ASTTerminalNode"));
            foreach (var cfgItem in method.Value.cfgs)
            {
                var typesMatch = cfgItem.Split(" ").Select(w => w.Trim()).
                    Select(w => nonTerminals.Contains(w) ? FormalizeName(w) : "ASTTerminalNode").
                    ToArray();
                sb.Append(PatternMatchGen(typesMatch.Length, cfgItem, typesMatch) + LE + T6 + "{\r\n"  +
                          T8 + "return null;" + "\r\n" + T6 + "}\r\n");
            }
            
            sb.Append(RB4 + LE);
            return sb + "";
        }
        public static string GenFileContentString(Dictionary<string, (string folder, List<string> cfgs)> memo)
        {
            var sb = new StringBuilder();
            var nonTerminals = memo.Keys.ToHashSet();
            sb.Append(Header.Aggregate("", (a, b) => a + "\r\n" + b));
            sb.Append("\r\n");
            sb.Append("namespace " + NameSpace + "\r\n{\r\n");
            //sb.Append("    [TokenConfiguration]\r\n");
            sb.Append("    public static class " + ClassName + "\r\n    {\r\n");
         
            sb.Append(LazyMappingCode);
            foreach (var m in memo) 
                sb.Append("\r\n" + MethodGen(m, nonTerminals) + "\r\n");
            sb.Append("    }\r\n");
            sb.Append('}');
            sb.PrintToConsole();
            return sb + "";
        }
        
    }
}