using System;
using System.Collections.Generic;
using System.Linq;

namespace YaccLexCS.ycomplier.LrParser
{
    public static class CfgTools
    {
        public static HashSet<string> GetSequenceFirstSet(CfgProducerDefinition definition, List<string> input)
         {
             if (input.Count <= 0)
                 return null!;
             if (input[0] == "$")
                 return new HashSet<string> {"$"};
             var f = GetFirstSet(definition);
             var ans = new HashSet<string>();
             foreach (var t in input)
             {
                 if (!f[t].Contains("ε"))
                 {
                     ans.AddRange(f[t]);
                     return ans;
                 }
                 ans.AddRange(f[t].Except(new []{"ε"}));
             }

             return ans;
         }
        public static Dictionary<string, HashSet<string>> GetFirstSet(CfgProducerDefinition definition)
        {
            /*
             * （1）若X 是终结符或ε，则First (X) = {X}。
（2）若X 是非终结符，则对于每个产生式X→X1X2. . . Xn ，
First (X)都包含了First(X1) - {ε}。
若对于某个i < n ，所有的集合First (X1), . . . , First (Xi) 都包括了。则First (X) 也包括了First (X i + 1 ) -{ε}。若所有集合First (X1), . . . , First (Xn)包括了ε
，则First (X)也包括ε。
             */
            var path = new Stack<string>();
            var finished = new HashSet<string>();
            var resultDictionary = new Dictionary<string, HashSet<string>>();
            void GetFirstSetDfs(string symbol)
            {
                //var produceMapping = resultDictionary[symbol];
                path.Push(symbol);
                if (finished.Contains(symbol))
                {
                    resultDictionary[path.Peek()].AddRange(resultDictionary[symbol]);
                  
                    path.Pop();
                    return;
                }
            
            
                if (definition.Terminations.Contains(symbol) || symbol.Equals("ε"))
                {
                            
                    resultDictionary[symbol].Add(symbol);
                    resultDictionary[path.Peek()].AddRange(resultDictionary[symbol]);
                    path.Pop();
                    return;
                }
                if (definition.NonTerminations.Contains(symbol))
                {
                    var produceItems = definition.ProduceMappingList[symbol];
                    
                    foreach (var item in produceItems) //每个产生式
                    {
                        foreach (var t1 in item)
                        {
                            if(path.Peek() == t1)
                                break;
                            GetFirstSetDfs(t1);
                            resultDictionary[path.Peek()].AddRange(resultDictionary[t1]);
                            
                            if (definition.Terminations.Contains(t1) || !resultDictionary[t1].Contains("ε"))
                                break;
                        }
                    }
                            
                }else { 
                    throw new ArithmeticException();
                }
            
                finished.Add(path.Pop());
            }
            
            
            
           
            foreach (var s in definition.ProduceMappingList?.Keys!)
            {
                resultDictionary[s] = new HashSet<string>();
            }
            foreach (var t in definition.Terminations.Where(nonT => !resultDictionary.ContainsKey(nonT)))
            {
                resultDictionary[t] = new HashSet<string> {t};
            }
            
            foreach (var startWord in definition.ProduceMappingList.Keys)
            {
                GetFirstSetDfs(startWord);
            }
            
           
            
            return resultDictionary;
        }
    }
}