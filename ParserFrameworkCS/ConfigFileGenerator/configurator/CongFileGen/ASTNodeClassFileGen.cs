using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ConfigFileGenerator.configurator.CongFileGen
{
    public static class ASTNodeClassFileGen
    {
        public static string[] Header =
        {
            "using System.Collections.Generic;",
            "using YaccLexCS.ycomplier;",
            "using YaccLexCS.ycomplier.attribution;",
            "using YaccLexCS.ycomplier.code;",
            "using YaccLexCS.ycomplier.code.structure;"
            ,"using YaccLexCS.runtime;"
        };

        public static string NameSpace = "YaccLexCS.code.structure";
        public static string EvaluationConfigurationHelperClassName = "EvaluationConfiguration";
        const string T6 = "            ";
        const string T4 = "        ";
        const string LE = "\r\n";
         public static string GenASTNode(string nodeName, List<string> generateList)
         {
             var className = $"{nodeName}".Split("_").Select(w => w[0..1].ToUpper() + w[1..].ToLower())
                 .Aggregate("", (a, b) => a + b) + "Node";
            
             var sb = new StringBuilder();
             sb.Append(Header.Aggregate("", (a, b) => a + "\r\n" + b));
             sb.Append("\r\n");
             sb.Append("namespace " + NameSpace + "\r\n{\r\n");
             sb.Append("    [GrammarConfiguration]\r\n");
             sb.Append("    public class " + className + " : ASTNonTerminalNode\r\n    {\r\n");
             
             sb.Append($"{T4}public override dynamic Eval(RuntimeContext context)\r\n{T4}" + "{\r\n" +
             $"{T6}return {EvaluationConfigurationHelperClassName}.ClassNameMapping[GetType().Name]" +
                 ".Invoke(null, new object[]{this, context});\r\n"  
                 + T4 + "}\r\n");
             sb.Append($"{T4}public {className}(IEnumerable<ASTNode> child) : base(child, \"{nodeName}\")\r\n{T4}" + "{\r\n" +
                       T4  + "}\r\n");
             
             // foreach (var m in fileStrut.Descs) 
             //     sb.Append("\r\n" + MethodGen(m) + "\r\n");
             sb.Append("    }\r\n");
             sb.Append('}');
             sb.PrintToConsole();
            
             return sb + "";
         }
    }
}