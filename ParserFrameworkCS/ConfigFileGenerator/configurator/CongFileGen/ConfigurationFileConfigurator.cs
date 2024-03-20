using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.CompilerServices;

namespace ConfigFileGenerator.configurator.CongFileGen
{
    public class ConfigurationFileConfigurator
    {
        private readonly ConfigFileParser _configFileParser;
        private readonly List<TokenMethod> _ts;
        private readonly List<GrammarFileStrut> _gs;
        private readonly string _configurationNamespace;

        public string GenEvaluationHelperFile(string outPath = null)
        {

            var res = EvaluationListFileGen.GenFileContentString(ConfigurationFileConfigurator.GetCFGList(_ts, _gs));

            if (outPath != null)
            {
                if (!Directory.Exists(outPath)) Directory.CreateDirectory(outPath);
                File.WriteAllText(outPath + "/EvaluationConfiguration.cs", res);

            }

            return res;
        }
        public ConfigurationFileConfigurator(string filePath, string configurationNamespace)
        {
            _configFileParser = new ConfigFileParser(filePath);
            _configFileParser.Init();
            _ts = _configFileParser.ParseTokensConfig();
            _gs = _configFileParser.ParseTokensGrammar();
            _configurationNamespace = configurationNamespace;
        }

        public string GenTokenConfigFile(string outPath = null)
        {
            var tc = TokensConfigFileGen.GenFileContentString(_ts);
            if (outPath != null)
            {
                if(!Directory.Exists(outPath)) Directory.CreateDirectory(outPath);
                if(File.Exists(outPath + "TokenList.cs"))
                    File.Delete(outPath + "TokenList.cs");
                File.WriteAllText(outPath + "TokenList.cs", tc);
            }
            return tc;
        }
        
        public Dictionary<string, string> GenASTFiles(string outPath)
        {
            var res =  GenAstNodes(_ts, _gs);
            foreach (var r in res)
            {
                var p = r.Key.Split("::");
                var fold = p[0].ToLower();
                var fName = p[1].Split("_").Select(s => (s[0] + "").ToUpper() + s[1..])
                    .Aggregate("", (a, b) => a + b);
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
            return res;
        }
        public Dictionary<string, string> GenGrammarConfigFiles(string outPath = null)
        {
            var dict = _gs.ToDictionary(k => k.FileName, GrammarConfigFileGen.GenFileContentString);
            foreach(var f in dict)
            {
                if (outPath != null)
                {
                    if (!Directory.Exists(outPath)) Directory.CreateDirectory(outPath);
                    if (File.Exists(outPath))
                        File.Delete(outPath);
                    File.WriteAllText(outPath + "\\" + f.Key + ".cs" , f.Value);
                }
            }
            
            return dict;
        }
        public string GenGrammarConfigFile()
        {
            return TokensConfigFileGen.GenFileContentString(_configFileParser.ParseTokensConfig());
        }

        public static Dictionary<string, (string folder, List<string> cfgs)> GetCFGList(IEnumerable<TokenMethod> ts, List<GrammarFileStrut> gs){
            var terminalWords = ts.SelectMany(t => t.Descs.Select(d => d.Name)).ToHashSet();
            var memo = new Dictionary<string, (string folder, List<string> cfgs)>(); //nodename -> method list
            var res = gs
                .ToDictionary(f => f.FileName, f => new List<ASTNodeStrut>());
            
            foreach (var g in gs)
            {
                foreach(var cfgs in g.Descs)
                {
                    //这里是为了合并
                    /*
                     *A -> B | C
                     * A -> D
                     * 这样出现了相同名的话，就合并。 A -> [B, C , D]
                     * 
                     */
                    if (!memo.ContainsKey(cfgs.WordName))
                        memo[cfgs.WordName] = (g.FileName, new List<string>(cfgs.Descs));
                    else
                        memo[cfgs.WordName].cfgs.AddRange(cfgs.Descs);

                }
            }

            return memo;
            void CheckIntegrality()
            {
                var allWords = memo.Keys.Concat(memo.SelectMany(t => 
                        t.Value.cfgs.SelectMany(w => w.Split(" ").Select(sw => sw.Trim()).Where(sw => sw != "")))).ToHashSet()
                    .Except(terminalWords);
                if (allWords.Count() != memo.Count)
                    throw new Exception("not all node are appear: " + allWords.Except(memo.Keys).GetCollectionString());
            }
          

        }
        public static Dictionary<string, string> GenAstNodes(IEnumerable<TokenMethod> ts, List<GrammarFileStrut> gs){
            var terminalWords = ts.SelectMany(t => t.Descs.Select(d => d.Name)).ToHashSet();
            var memo = GetCFGList(ts, gs);
            var filesResult = new Dictionary<string, string>();
            void CheckIntegrality()
            {
                var allWords = memo.Keys.Concat(memo.SelectMany(t => 
                        t.Value.cfgs.SelectMany(w => w.Split(" ").Select(sw => sw.Trim()).Where(sw => sw != "")))).ToHashSet()
                    .Except(terminalWords);
                if (allWords.Count() != memo.Count)
                    throw new Exception("not all node are appear: " + allWords.Except(memo.Keys).GetCollectionString());
            }
            CheckIntegrality();
            foreach (var k in memo)
            {
                var (folder, cfgs) = k.Value;
                var nodeName = k.Key;
                //save to //folder//nodeName.cs
                cfgs.Count.PrintToConsole();
                var r= ASTNodeClassFileGen.GenASTNode(nodeName, cfgs);
                filesResult[folder + "::" + nodeName] = r;
                //这里应该根据不同进行进行传入参数的匹配
            }

            return filesResult;

        }

        public Dictionary<string, string> GenAstNodes()
        {
            return GenAstNodes(_configFileParser.ParseTokensConfig(), _configFileParser.ParseTokensGrammar());
        }
    }
}