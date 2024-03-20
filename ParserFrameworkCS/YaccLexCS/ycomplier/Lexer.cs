using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Runtime.Serialization;
using System.Runtime.Serialization.Formatters.Binary;
using System.Text;
using System.Xml.Serialization;
using YaccLexCS.ycomplier.attribution;
using YaccLexCS.ycomplier.automata.re;

namespace YaccLexCS.ycomplier
{
    public abstract class Builder{
    
    }

    public class Lexer
    {
        public Lexer(CompilerContext compilerContext)
        {
            CompilerContext = compilerContext;
        }
        public readonly CompilerContext CompilerContext;
        private readonly Dictionary<TokenDefinition, MethodInfo> _patternMap = new();

        public Dictionary<TokenDefinition, MethodInfo> PatternMap => _patternMap;
        public HashSet<string> TokenNames => _patternMap.Select(e => e.Key.TokenName).ToHashSet();
        public static Lexer ConfigureFromPackages(IEnumerable<string> scanPackage, CompilerContext context)
        {
            var lexer = new Lexer(context);
            YCompilerConfigurator.GetAllTokenDefinitions(
                YCompilerConfigurator.ScanTokenConfiguration(scanPackage)).ElementInvoke(e =>
            {
                if (!e.tokenDef.UseRegex)
                {
                    lexer._patternMap[e.tokenDef] = e.methodInfo!;
                }
                else
                {
                    lexer._patternMap[e.tokenDef] = e.methodInfo!;
                }
            });
            return lexer;
        }


        public void SetPattenMapping(TokenDefinition definition, MethodInfo value) => _patternMap[definition] = value;
        
        private void InitAutomata()
        {
            foreach (var a in _patternMap)
            {
                a.Key.Automata?.ResetAutomata();
            }
        }

        private void InvokeTokenCallBackMethod(MethodBase methodInfo) {
            var p = methodInfo.GetParameters();
            if (!p.Any())
                methodInfo.Invoke(null, Array.Empty<object>());
            else if(p.Length == 1)
                methodInfo.Invoke(null, new object?[]{CompilerContext});
        }
        public void ParseInStream(TextReader stream, Action<Token> callBack)
        {
            InitAutomata();
        
            var order = _patternMap.GroupBy(e => e.Key.Priority)
                .OrderBy(g => g.Key).SelectMany(g => g.ToList()).ToList();
            var available = order.ToArray();

            

            var text = "";
            while (stream.Peek() > 0)
            {
                
                var cur = text;
                var peek = (char) stream.Peek();
                var str = order.Where(e => 
                    !e.Key.UseRegex && cur + peek == e.Key.SourcePattern);


                // filtering token recognizaztion rule candidates
                var t = available.Where(e =>
                        e.Key.UseRegex && e.Key.Automata!.IsCanTransWith(peek)).Concat(str)
                    .OrderBy(e => e.Key.Priority).ToArray();


                if (!t.Any()) {
                    CompilerContext.CurrentRecognizedTokenName = text;

                    text = "";
     
                    InvokeTokenCallBackMethod(available.First().Value);
                    callBack?.Invoke(new Token(CompilerContext.CurrentRecognizedTokenName,available.First().Key.TokenName));
                    available = order.ToArray();
                    InitAutomata();
                }else {
                    text += peek;
                    stream.Read();
                    
                    t.ElementInvoke(e => {
                        if (e.Key.UseRegex)
                            e.Key.Automata?.ParseSingleInputFromCurrentStates(peek);
                    });
                    available = t.ToArray();
                }
            }

            if (!available.Any())
                return;
            
            CompilerContext.CurrentRecognizedTokenName = text;
            InvokeTokenCallBackMethod(available.First().Value);
            
            callBack?.Invoke(new Token(CompilerContext.CurrentRecognizedTokenName,available.First().Key.TokenName));
        }
        public IEnumerable<Token> ParseWholeText(string s) {
            var sb = new StringBuilder(s);
            var cur = "";
        
           
            
            InitAutomata();
        
            var order = _patternMap.GroupBy(e => e.Key.Priority)
                        .OrderBy(g => g.Key).SelectMany(g => g.ToList()).ToList();
            var available = order.ToArray();
        
            
            while (sb.Length > 0) {
                var c = sb[0];

                var cur1 = cur;
                var str = order.Where(e => 
                    !e.Key.UseRegex && (cur1 + c) == e.Key.SourcePattern);
                var t = available.Where(e =>
                            e.Key.UseRegex && 
                            e.Key.Automata!.IsCanTransWith(c)).Concat(str)
                    .OrderBy(e => e.Key.Priority).ToArray();
                       
               
                if (!t.Any()) {
                            
                    //$"get token {cur}".PrintToConsole();
                    CompilerContext.CurrentRecognizedTokenName = cur;
                            
                    cur = "";
                          
                            
                    InvokeTokenCallBackMethod(available.First().Value);
                    yield return new Token(CompilerContext.CurrentRecognizedTokenName,available.First().Key.TokenName);
                    available = order.ToArray();
                    InitAutomata();
                }else {
                    cur += c;
                    sb.Remove(0, 1);
                    t.ElementInvoke(e => {
                        if (e.Key.UseRegex)
                                    e.Key.Automata?.ParseSingleInputFromCurrentStates(c);
                    });
                    available = t.ToArray();
                }
                        
            }
        
            if (!available.Any()) 
                yield break;
            $"get token {cur}".PrintToConsole();
            CompilerContext.CurrentRecognizedTokenName = cur;
            InvokeTokenCallBackMethod(available.First().Value);
            yield return new Token(CompilerContext.CurrentRecognizedTokenName,available.First().Key.TokenName);
        }
    }
}