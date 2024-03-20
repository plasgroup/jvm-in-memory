using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;
using YaccLexCS.ycomplier.attribution;
using YaccLexCS.ycomplier.util;

namespace YaccLexCS.ycomplier
{
    public static class YCompilerConfigurator
    {
        public static void GenerateGrammarDefinitionFileFrom(string inputFilePath, string outputPath)
        {
            var streamReader = File.OpenText(inputFilePath);
            var line = "";
            var beginWord = "";
            var state = 0;
            Dictionary<string, List<string>> definitions = new();
            var curName = "";
            while ((line = streamReader.ReadLine()) != null)
            {
                line = line.Trim();
                if (line.Length >= 1)
                {
                    if (line[0] == '#')
                    {
                        beginWord = line[1..].Replace("\r", "").Replace("\n", "");
                        $"begin from {beginWord}".PrintToConsole();
                        continue;
                    }
                    if (state == 1)
                    {
                        if (line[0] == '}')
                        {
                            state = 0;
                            
                        }
                        if (line[^1] == '}')
                        {
                            state = 0;
                            // line.Replace("}", "").PrintToConsole();
                            continue;
                        }
                        //line.PrintToConsole();
                        definitions[curName].Add(line);
                    }
                    if (line[0] == '{')
                    {
                        state = 1;
                    }
                    if (state == 0 && line[^1] == '{')
                    {
                        state = 1;
                        curName = line.Replace("{", "").Trim();
                        var name = curName.Replace("\r", "").Replace("\n", "");
                        if (!definitions.ContainsKey(name)) 
                            definitions[name] = new List<string>();
                        $"entry {curName}".PrintToConsole();
                    }
                   
                  
                }

                
              

            
            }
            foreach (var kv in definitions)
            {
                    
                    //写入ast节点类路径
                    var astClassPath = outputPath + "\\" + kv.Key.ToLower();
                    var memo = new HashSet<string>();
                    if(!Directory.Exists(outputPath)) Directory.CreateDirectory(outputPath);    
                    if (!Directory.Exists(astClassPath)) Directory.CreateDirectory(astClassPath);
                    

                    //output
                    var sb = new StringBuilder();
                    sb.Append("using YaccLexCS.ycomplier;\r\n"+
                              "using YaccLexCS.ycomplier.attribution;\r\n\r\n"+
                              "namespace YaccLexCS.config{\r\n" +
                              "\t[GrammarConfiguration]\r\n" +
                              $"\tpublic static class Grammar{kv.Key}"+"{"+"\r\n");
                    void AddMethod(string desc)
                    {
                        var param = desc.Trim().Substring(desc.IndexOf(":", StringComparison.Ordinal) + 1).Trim().Split("|");
                        var right = "";
                        var left = "\"" + desc.Trim().Split(":")[0].Trim() + "\"";
                        foreach (var p in param)
                        {
                            right += "\"" + p.Trim() + "\"";
                            right += ",";
                        }
                        var className = left.Trim('"') == beginWord ? "BeginningGrammarDefinition" : "GrammarDefinition";
                        (left.Trim('"') == beginWord, left.Trim('"'),beginWord).PrintToConsole();
                        sb.Append("\r\n" + $"\t\t[{className}({left},{right.Trim(',')})]\r\n");
                        sb.Append($"\t\tpublic static void {left.Trim('"')}(RuntimeContext context)" + "{\r\n\r\n\t\t}\r\n\r\n");
                        
                        var cName = left.Trim('"').Split("_").Select(e => (e[0] + "").ToUpper() + e[1..])
                            .Aggregate("", (a, b) => a + b) + "Node";
                        //write ast node class
                        if (!memo.Contains(cName))
                        {
                            
                            var classContext = new StringBuilder();
                            classContext.Append("using System.Collections.Generic;\r\n");
                            classContext.Append("using YaccLexCS.ycomplier;\r\n");
                            classContext.Append("using YaccLexCS.ycomplier.code;\r\n" +
                                                "using YaccLexCS.ycomplier.code.structure;\r\n");
                            classContext.Append("\r\nnamespace YaccLexCS.code.structure\r\n{\r\n");
                            classContext.Append($"\tpublic class {cName} : ASTNonTerminalNode\r\n" + "\t{\r\n");
                            classContext.Append($"\t\tpublic {cName}(IEnumerable<ASTNode> child) : base(child," +left+ ")\r\n\t\t{");
                           

                            classContext.Append("\r\n\t\t}\r\n");
                            classContext.Append("\t\tpublic override dynamic Eval(RuntimeContext context)\r\n\t\t{\r\n" +
                                                "\t\t\t\treturn EvaluationConfiguration.ClassNameMapping[GetType().Name].Invoke(null, new object[]{this, context});\r\n");
                            classContext.Append("\t\t}\r\n\t}\r\n}");
                            File.WriteAllText(astClassPath + "\\\\" +cName + ".cs", classContext + "");
                            memo.Add(cName);
                        }
                    }
                    foreach (var v in kv.Value)
                    {
                        AddMethod(v);
                    }

                    sb.Append("\t}\r\n}");
                    
                  
                    
                    var fName = outputPath + $"\\Grammar{kv.Key}.cs";
                    if (File.Exists(fName)) File.Delete(fName);
                    File.WriteAllText(fName, sb + "");


            }
               
            var fileName = outputPath + $"\\EvaluationConfiguration.cs";
            var evalCsFileContext = "using System;" +
                                    "\r\nusing System.Collections.Generic;" +
                                    "\r\nusing System.Linq;" +
                                    "\r\nusing System.Reflection;" +
                                    "\r\nusing YaccLexCS.code.structure;" +
                                    "\r\nusing YaccLexCS.ycomplier.code.structure;" +
                                    "\r\n" +
                                    "\r\nnamespace YaccLexCS.ycomplier.code.structure\r\n{\r\n" +
                                    "\t\tpublic class EvaluationConfiguration\r\n\t\t{\r\n" +
                                    "\t\t\t\t" +
                                    "public static readonly Dictionary<string, MethodInfo> ClassNameMapping\r\n" +
                                    "\t\t\t\t\t\t= new Lazy<Dictionary<string, MethodInfo>>(() =>\r\n" +
                                    "\t\t\t\t\t\t{\r\n" +
                                    "\t\t\t\t\t\t\t\tvar methods = typeof(EvaluationConfiguration).GetMethods();\r\n" +
                                    "\t\t\t\t\t\t\t\tvar result = methods\r\n" +
                                    "\t\t\t\t\t\t\t\t.Where(m => m.IsStatic)\r\n" +
                                    "\t\t\t\t\t\t\t\t.ToDictionary(kv => kv.Name, kv => kv);\r\n" +
                                    "\t\t\t\t\t\t\t\treturn result;\r\n"+
                                    "\t\t\t\t\t\t}).Value;\r\n";
            
            foreach (var kv in definitions)
            {
                    
                
                    var memo = new HashSet<string>();
                    if(!Directory.Exists(outputPath)) Directory.CreateDirectory(outputPath);

                    //output
                   
                    void AddOne(string desc)
                    {
                        var param = desc.Trim().Substring(desc.IndexOf(":", StringComparison.Ordinal) + 1).Trim().Split("|");
                        var right = "";
                        var left = "\"" + desc.Trim().Split(":")[0].Trim() + "\"";
                        foreach (var p in param)
                        {
                            right += "\"" + p.Trim() + "\"";
                            right += ",";
                        }
                        
                        (left.Trim('"') == beginWord, left.Trim('"'),beginWord).PrintToConsole();
                        
                        var cName = left.Trim('"').Split("_").Select(e => (e[0] + "").ToUpper() + e[1..])
                            .Aggregate("", (a, b) => a + b) + "Node";
                        //write ast node class
                        if (!memo.Contains(cName))
                        {
                            
                            evalCsFileContext +=
                                $"\r\n\t\t\t\tpublic static dynamic {cName}({cName} node, RuntimeContext context)\r\n\t\t\t\t" + "{\r\n\t\t\t\t\r\n" +
                                "\t\t\t\t\t\tthrow new NotImplementedException();\r\n" +
                                "\t\t\t\t}\r\n";
                           
                            memo.Add(cName);
                        }
                    }
                    foreach (var v in kv.Value)
                    {
                        AddOne(v);
                    }

                    
                  
                    
                    


            }
            evalCsFileContext += ("\t}\r\n}");
                    
            if (File.Exists(fileName)) File.Delete(fileName);
            File.WriteAllText(fileName, evalCsFileContext + "");
            
        }
        public static IEnumerable<Type> ScanTokenConfiguration(IEnumerable<string> packetName)
        {
            return ReflectionTool.ScanConfigurationClass<TokenConfiguration>(packetName);
        }
        public static IEnumerable<Type> ScanGrammarConfiguration(IEnumerable<string> packetName)
        {
            return ReflectionTool.ScanConfigurationClass<GrammarConfiguration>(packetName);
        }
        public static IEnumerable<(GrammarDefinition tokenDef, MethodInfo methodInfo)> GetAllGrammarDefinitions(IEnumerable<Type> types)
        {
            return ReflectionTool.GetAllDefinitions<GrammarDefinition>(types);
        }
        public static IEnumerable<(TokenDefinition tokenDef, MethodInfo methodInfo)> GetAllTokenDefinitions(IEnumerable<Type> types)
        {
            return ReflectionTool.GetAllDefinitions<TokenDefinition>(types);
        }
    }
}