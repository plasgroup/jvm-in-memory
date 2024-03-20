
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Runtime.Serialization;
using System.Runtime.Serialization.Formatters.Binary;
using System.Text;
using System.Text.RegularExpressions;
using System.Timers;
using System.Xml.Serialization;
using YaccLexCS.code.structure;
using YaccLexCS.runtime;
using YaccLexCS.utils;
using YaccLexCS.ycomplier;
using YaccLexCS.ycomplier.automata;
using YaccLexCS.ycomplier.automata.re;
using YaccLexCS.ycomplier.code;
using YaccLexCS.ycomplier.code.structure;
using YaccLexCS.ycomplier.LrParser;
using YaccLexCS.ycomplier.util;

using Regex = System.Text.RegularExpressions.Regex;

namespace YaccLexCS
{
    
    

  

    public class Program
    {
        static class ProgramConfiguration
        {
            public static string serializeParserToFile = "parser.lr1";
            public static string deserializeParserFromFile = "parser.lr1";
            public static bool isSerializeParserToFile = true;
            public static bool isDdeserializeParserFromFile = true;
            public static bool enableOverwriteExistLr1File = true;
        }
        public static void Main(string[] args)
        {
            // parseParameters(args);
            var compilerContext = new CompilerContext();
            var runtimeContext = new RuntimeContext();

            //在指定命名空间扫描配置
            var lexer = Lexer.ConfigureFromPackages(new[] { "YaccLexCS.config" }, compilerContext);
            
           
            //create input stream
            var r = (TextReader)new StringReader("" +
                "let sum = 0;" + //(0,3)
                "let i = 0;" +  //(0,4)

                "while(i <= 10){" + //(0,4)
                "   i = i + 1;" + //(1,4) (1,4)
                "   if(i==7){" + //(1,4)
                "       for(var j = 0; i < 8; j = j + 1){" + //j:(0,0) i：(3,4) for需要创新帧再eval第一个块
                "           {" +
                "               i = i + 1;" + //i:(4,4)
                "           }" +
                "           if(j > 5){break;}" + //j:(0,0)
                "       }" +
                "       let l = lambda(x, y)=>{x + y + i;};" +//[x:(0,0), y(0,1)  i:(3,4) // l:(0,0)
                "       let a = l(sum, i);" + //l(0,0) sum:(2,3) i(2,4) a:(0,1)
                "       #Console.WriteLine(\"output by native function : \" + sum + i);" + //call c# native function
                "       a = fact(5);" + //a:(0,1) fact(2,2)
                "       new []{1, 2, 3, 4};" +
                "       break;" +
                "   }" +
                "   sum = sum + i;" +//sum(1,3), i(1,4)
                "   continue;" +
                "   break;" +
                "}" +
                "" +
                "dyfn fact(n){" + //hoisting  fact:(0,2)
                "   if(n==1) return 1;" + //n(0,0) 
                "   return n*fact(n-1);" + //fact:(1,2)
                "}" +
                "dyfn f1(){" + //f1=(0,1)
                "   return f2();" + //f2=(1,0)
                "}" +
                "dyfn f2(){" + //f2=(0,0)
                "   return f1();" + //f1=(1,1)
                "}" +
                "" +
                "class A{" +
                "   public a = 0;" +
                "   private b = 5;" + 
                "   private sA = 12;" + 
                "   A(){" +
                "       #Console.WriteLine(\"in A's Constructor\");" +
                "   }" +
                "   public static StaticFunc1(){" +
                "       return sA;" +
                "   }" +
                "   public GetB(){" +
                "       return b;" +
                "   }" +  
                "}" + 
                "");


            var tokenList = new List<Token>();

            //create parser
            Lr1Parser parser = null;

            if (ProgramConfiguration.isSerializeParserToFile)
            {
                "Build parser ...".PrintToConsole();
                parser = Lr1ParserBuilder.ConfigureFromPackages(lexer.TokenNames, new[] { "YaccLexCS.config" });
                parser.InitParser().SetContext(compilerContext);
                "Build parser end ...".PrintToConsole();
                if (File.Exists(ProgramConfiguration.serializeParserToFile))
                {
                    "[Warining] ${ProgramConfiguration.serializeParserToFile} is existed".PrintToConsole();
                    if (!ProgramConfiguration.enableOverwriteExistLr1File)
                    {
                        throw new IOException("${ProgramConfiguration.serializeParserToFile} is existed. It may attach 'ENABLE_OVERWRITE_LR1_FILE' option to program arguments to allow overwrite");
                    }

                    File.Delete(ProgramConfiguration.serializeParserToFile);
                }
                "Serialize parser to file ${ProgramConfiguration.serializeParserToFile}".PrintToConsole();
                parser.Serialize(ProgramConfiguration.serializeParserToFile);
            }

            if (ProgramConfiguration.isDdeserializeParserFromFile)
            {
                parser = Lr1ParserBuilder
                    .DeSerializeFromFile(ProgramConfiguration.deserializeParserFromFile, lexer.TokenNames, new[] { "YaccLexCS.config" });
                parser.SetContext(compilerContext);
            }

            if(parser == null)
            {
                throw new Exception("Build parser failed.");
            }

            var r2 = (TextReader)new StringReader("" +
                "// naming task\r\n" +
                "def_task batch_query(::int[200] K, ::G T)->::int[200]{\r\n" +
                    "\t::int[200] result =\r\n" +
                    "\t\t::eval(\r\n" +
                    "\t\t\tfrom k in K\r\n" +
                    "\t\t\tselect\r\n" +
                    "\t\t\t  from t is <@TreeNode> in T\r\n" +
                    "\t\t\t  where t.key == k\r\n" +
                    "\t\t          select t.value\r\n" +
                    "\t\t);\r\n" +
                    "\treturn result;" +
                "\r\n}" +
            "");

            lexer.ParseInStream(r2, token =>
            {
                if (token.Type.Equals("Skip"))
                    return;
                tokenList.Add(token);
                parser.ParseFromCurrentState(token);
            });
            parser.ParseFromCurrentState(new Token("$", "$"));

            tokenList.PrintEnumerationToConsole();
            var root = parser.GetCurrentStack().Peek();

            GraphUtils.DrawAST("./ast.png", root);


            var t = DateTime.Now;

            
            root.PrintCollectionToConsole();
            root.Eval(runtimeContext);
            $"{DateTime.Now - t}".PrintToConsole();
            t = DateTime.Now;
            //lexicalAST.Eval(runtimeContext); //998ms
            $"{DateTime.Now - t}".PrintToConsole();


        }

        private static void parseParameters(string[] args)
        {
            foreach(string str in args)
            {
                if (str.Length == 0) continue;
                string[] strings = str.Trim().Split("=");
                if(strings.Length == 0) continue;
                if (strings.Length == 1)
                {
                    processParameter(strings[0], null);
                    continue;
                }
                var (key, value) = (strings[0], strings[1..].Aggregate((a,b)=>a + b));
                processParameter(key, value);
            }
        }

        private static void processParameter(string key, string? value)
        {
            switch(key)
            {
                case "SERIALIZE_PARSER":
                    if(value == null)
                    {
                        "[Warning] Use default output lr1 file's serialzation path './parser.lr1'".PrintToConsole();
                        "--> [Usage] SERIALIZE_PARSER=output_lr1_file_path".PrintToConsole();
                    }
                    ProgramConfiguration.serializeParserToFile = value ?? "./parser.lr1";
                    ProgramConfiguration.isSerializeParserToFile = true;
                    break;
                case "DESERIALIZE_PARSER":
                    if (value == null)
                    {
                        "[Warning] Use default output lr1 file './parser.lr1'".PrintToConsole();
                        "--> [Usage] DESERIALIZE_PARSER=input_lr1_file_path".PrintToConsole();
                    }
                    ProgramConfiguration.deserializeParserFromFile = value ?? "./parser.lr1";
                    ProgramConfiguration.isDdeserializeParserFromFile = true;
                    break;
                case "ENABLE_OVERWRITE_LR1_FILE":
                    ProgramConfiguration.enableOverwriteExistLr1File = true;
                    break;

            }
        }
    }
}
