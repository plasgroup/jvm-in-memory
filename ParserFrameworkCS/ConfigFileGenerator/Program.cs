// See https://aka.ms/new-console-template for more information

using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using ConfigFileGenerator.configurator;
using ConfigFileGenerator.configurator.CongFileGen;

// var c = new ConfigurationFileConfigurator("d:\\d.txt");
// //c.GenTokenConfigFile();
// c.GenGrammarConfigFile();

class ASTNodeStrut
{
    
}

public class Program
{

    
    public static void Main(string[] args)
    {
        var configFilePath = "./meta_config.txt";
        var outPath = "./out/";
        var configurationClassesNamespace = "YaccLexCS.config";
        if (args.Length >= 2)
        {
            configFilePath = args[0];
            outPath = args[1];
        }
        if (args.Length >= 3)
        {
            configurationClassesNamespace = args[2];
        }
        var gfc = new ConfigurationFileConfigurator(configFilePath, configurationClassesNamespace);

        gfc.GenTokenConfigFile(outPath);
        gfc.GenGrammarConfigFiles(outPath + "/grammars\\");
        gfc.GenASTFiles(outPath);
        gfc.GenEvaluationHelperFile(outPath + "/structure\\");

        /*//var nodes = ConfigurationFileConfigurator.GenAstNodes(ts, gs);
        var eval = EvaluationListFileGen.GenFileContentString(ConfigurationFileConfigurator.GetCFGList(ts, gs));
        var res = ConfigurationFileConfigurator.GenAstNodes(ts, gs);*/

/*
        foreach (var r in res)
        {
            var p = r.Key.Split("::");
            var fold = p[0].ToLower();
            var fName = p[1].Split("_").Select(s => (s[0] + "").ToUpper() + s[1..])
                .Aggregate("", (a,b) => a + b);
            if (File.Exists(outPath + "structure\\" + fold + "\\" + fName))
            {
                File.Delete(outPath + "structure\\" + fold + "\\" + fName);
            }

            if (!Directory.Exists(outPath + "structure\\" + fold))
            {
                Directory.CreateDirectory(outPath + "structure\\" + fold);
            }
            File.WriteAllText(outPath + "structure\\" + fold + "\\" + fName + "Node.cs", r.Value);
            r.Key.PrintToConsole();
        }
        File.WriteAllText(outPath + "structure\\EvaluationConfiguration.cs",eval);
        return;
        
        TokensConfigFileGen.GenFileContentString(ts);
        foreach (var g in gs)
        {
            GrammarConfigFileGen.GenFileContentString(g);
            break;
        }
*/


    }
}
