using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text.RegularExpressions;

namespace ConfigFileGenerator.configurator.CongFileGen
{
    public class ConfigFileParser
    {
        private readonly string _filePath;
        private string _startWord = "";
        private readonly List<string> _tokensDesc = new();
        private readonly List<string> _grammarsDesc = new();

        public ConfigFileParser(string path)
        {
            _filePath = path;
        }
        
        public List<TokenMethod> ParseTokensConfig()
        {
            var level = 0;
            var externalBlockName = "";
            TokenMethod currMethod = new TokenMethod("");
            var methodList = new List<TokenMethod>();

            // merge all token description lines
            var all = _tokensDesc.Aggregate("", (a, b) => a + "\r\n" + b);

            static string FormalizeName(string s) => 
                s.Split("_").Select(w => w.Substring(0, 1).ToUpper() + w[1..].ToLower())
                    .Aggregate("", (a, b) => a + b);

            // get all lines that length > 0
            foreach (var l in _tokensDesc.Where(l => l.Length > 0))
            {
                if (level == 0)
                {
                    switch (l[0])
                    {
                        case '.':
                            // get the 1~ part of this line
                            externalBlockName = l.Replace("{","")[1..];
                            $"to {externalBlockName}".PrintToConsole();
                            // temporary record the token
                            currMethod = new TokenMethod(FormalizeName(externalBlockName));
                            // add level
                            level = 1;
                            break;
                        case '$':
                            // Use regex. Use specific priority
                            if (l[1] == '!')
                            {
                                // $! leftSymbol rightExp p

                                var sp = l.Split(" ");
                                var leftSymbol = sp[1]; 
                                var rightExp = sp.Skip(2).SkipLast(1).Aggregate("", (a, b) => a + b).Trim();
                                var p = int.Parse(sp[^1]); // Priority
                                (leftSymbol, rightExp).PrintToConsole();
                                var m = new TokenMethod(FormalizeName(leftSymbol))
                                    .AddTokenAttr(leftSymbol, rightExp, true, p);
                                methodList.Add(m);
                            }
                            else
                            {
                                // Use regex. Use specific priority.
                                // $! leftSymbol rightExp
                                var leftSymbol = l.Split(" ")[1];
                                var rightExp = l.Split(" ").Skip(2).Aggregate("", (a, b) => a + b).Trim();
                                (leftSymbol, rightExp).PrintToConsole();
                                var m = new TokenMethod(FormalizeName(leftSymbol))
                                    .AddTokenAttr(leftSymbol, rightExp, true);
                                methodList.Add(m);
                            }
                            break;
                        case '!':
                            if (l[1] == '$')
                            {
                                var sp = l.Split(" ");
                                var leftSymbol = sp[1];
                                var rightExp = sp.Skip(2).SkipLast(1).Aggregate("", (a, b) => a + b).Trim();
                                var p = int.Parse(sp[^1]);
                                (leftSymbol, rightExp).PrintToConsole();
                                var m = new TokenMethod(FormalizeName(leftSymbol))
                                    .AddTokenAttr(leftSymbol, rightExp, true, p);
                                methodList.Add(m);
                            }
                            else
                            {
                                var leftSymbol = l.Split(" ")[1];
                                var rightExp = l.Split(" ").Skip(2).Aggregate("", (a, b) => a + b).Trim();
                                (leftSymbol, rightExp).PrintToConsole();
                                var m = new TokenMethod(FormalizeName(leftSymbol))
                                    .AddTokenAttr(leftSymbol, rightExp, true);
                                methodList.Add(m);
                            }
                            break;
                        default:
                        { 
                            if (l == "{") {
                                level = 1; 
                            }
                            else
                            {
                                var leftSymbol = l.Split(" ")[0];
                                var rightExp = l.Split(" ").Skip(1).Aggregate("", (a, b) => a + b);
                                (leftSymbol, rightExp).PrintToConsole();
                                var m = new TokenMethod(FormalizeName(leftSymbol));
                                methodList.Add(m.AddTokenAttr(leftSymbol, rightExp.Trim(), false , 0));
                            }
                            break;
                        }
                    }
                }
                else
                { 
                    switch (l[0])
                    {
                        case '$':
                            if (l[1] == '!')
                            {
                                var sp = l.Split(" ");
                                var leftSymbol = sp[1];
                                var rightExp = sp.Skip(2).SkipLast(1).Aggregate("", (a, b) => a + b).Trim();
                                var p = int.Parse(sp[^1]);
                                (leftSymbol, rightExp).PrintToConsole();
                                currMethod.AddTokenAttr(leftSymbol, rightExp, true, p);
                            }
                            else
                            {
                                var leftSymbol = l.Split(" ")[1];
                                var rightExp = l.Split(" ").Skip(2).Aggregate("", (a, b) => a + b).Trim();
                                (leftSymbol, rightExp).PrintToConsole();
                                currMethod.AddTokenAttr(leftSymbol, rightExp, true, 0);
                            }
                            break;
                        case '!':
                            if (l[1] == '$')
                            {
                                var sp = l.Split(" ");
                                var leftSymbol = sp[1];
                                var rightExp = sp.Skip(2).SkipLast(1).Aggregate("", (a, b) => a + b).Trim();
                                var p = int.Parse(sp[^1]);
                                (leftSymbol, rightExp).PrintToConsole();
                                currMethod.AddTokenAttr(leftSymbol, rightExp, true, p);
                            }
                            else
                            {
                                var leftSymbol = l.Split(" ")[1];
                                var rightExp = l.Split(" ").Skip(2).Aggregate("", (a, b) => a + b).Trim();
                                (leftSymbol, rightExp).PrintToConsole();
                                currMethod.AddTokenAttr(leftSymbol, rightExp, true, 0);
                            }
                            break;
                        case '}':
                            level = 0;
                            methodList.Add(currMethod!);
                            break;
                        default:
                        {
                            if (l == "{")
                                level = 1;
                            else
                            {
                                var leftSymbol = l.Split(" ")[0];
                                var rightExp = l.Split(" ").Skip(1).Aggregate("", (a, b) => a + b);
                                $"{(leftSymbol, rightExp)} in {externalBlockName}".PrintToConsole();
                                currMethod.AddTokenAttr(leftSymbol, rightExp.Trim(), false, 0);
                            }

                            break;
                        }
                    }
                }
            }

            return methodList;
        }
        
        public List<GrammarFileStrut> ParseTokensGrammar()
        {
            var level = 0;
            var externalBlockName = "";
            GrammarFileStrut currStrut = new ("");
            var grammarFileList = new List<GrammarFileStrut>();
           
            static string FormalizeName(string s) => 
                s.Split("_").Select(w => w.Substring(0, 1).ToUpper() + w[1..].ToLower())
                    .Aggregate("", (a, b) => a + b);

            var timesMemo = new Dictionary<string, int>();
            var terminateWords = ParseTokensConfig()
                .SelectMany(t => t.Descs.Select(e => e.Name)).ToHashSet();
            foreach (var l in _grammarsDesc.Where(l => l.Length > 0))
            {
                if (level == 0)
                {
                    switch (l[0])
                    {
                        case '.':
                            externalBlockName = l.Replace("{","")[1..];
                            $"to {externalBlockName}".PrintToConsole();
                            currStrut = new GrammarFileStrut(externalBlockName);
                            level = 1;
                            break;
                        default:
                        {
                            throw new Exception("grammars should define in a block");
                        }
                    }
                }
                else
                {
                    if (l[0] == '}')
                    {
                        level = 0;
                        grammarFileList.Add(currStrut);
                        $"close {externalBlockName}".PrintToConsole();
                        continue;
                    }
                    // leftSymbol:a|b|c
                    var sp = l.Split(":").Select(e => e.Trim());
                    var leftSymbol = sp.First();
                    var rightExps = sp.Skip(1).Aggregate("", (a, b) => a + b).Trim().Split("|").Select(e => e.Trim());

                    ((leftSymbol, rightExps.Aggregate("" , (a, b) => a + " :: " + b)) + $" in {externalBlockName}")
                        .PrintToConsole();
                    if (timesMemo.ContainsKey(leftSymbol))
                        timesMemo[leftSymbol]++;
                    else
                        timesMemo[leftSymbol] = 0;
                    var methodName = timesMemo[leftSymbol] == 0 ? leftSymbol : leftSymbol + "_" + timesMemo[leftSymbol];
                    currStrut.AddMethod(methodName, rightExps , leftSymbol, leftSymbol == _startWord);
                }
            }

            return grammarFileList;
        }

        public ConfigFileParser Init()
        {
            var lines = File.ReadAllLines(path: _filePath);
            var s1 = 0;

            // analyze each line
            foreach (var l in lines)
            {
                // trim the line
                var tl = l.Trim();
                if (tl.Length <= 0) continue;
                switch (tl[0])
                {
                    case '#':
                        _startWord = tl[1..].Trim();
                        continue;
                    default:
                        switch (tl)
                        {
                            case "%Tokens%":
                                s1 = 1;
                                continue;
                            case "%Grammars%":
                                s1 = 2;
                                continue;
                            case "%%":
                                s1 = 0;
                                break;
                        }
                        switch (s1)
                        {
                            // tokens
                            case 1:
                                _tokensDesc.Add(tl);
                                break;
                            // grammar
                            case 2:
                                _grammarsDesc.Add(tl);
                                break;
                        }

                        break;
                }
            }
            return this;
        }
    }
}