using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using YaccLexCS.ycomplier.code;

namespace YaccLexCS.ycomplier.attribution
{
    [AttributeUsage(AttributeTargets.Method, AllowMultiple = true)]
    [Serializable]
    public class GrammarDefinition : YDefinition
    {
        public readonly string Name;
        public List<string> CfgItem;
        public readonly Type Type;
        public GrammarDefinition(string name, Type nodeType, params string[] cfgItem)
        {
            Name = name;
            CfgItem = cfgItem.ToList();
            Type = nodeType;
            if (!Type.IsSubclassOf(typeof(ASTNode)))
                throw new Exception();
            Type = nodeType;
        }

        public GrammarDefinition(string name, params string[] cfgItem)
        {
            Name = name;
            CfgItem = cfgItem.ToList();
            var c = Assembly.GetExecutingAssembly()
                .GetTypes()
                .Where(t => t.IsClass &&
                            (t.Name.Replace("_","").ToLower() == Name.Replace("_","").ToLower() + "node" || 
                             t.Name.Replace("_","").ToLower() == Name.Replace("_","").ToLower())
                            && t.IsSubclassOf(typeof(ASTNode))).ToList();
            if (!c.Any())
            {
                $"can't found class {name}Node".PrintToConsole();
                throw new Exception();
            }
            Type = c.First();
        }

        public override string ToString()
        {
            return $"[Cfg: {Name} : {CfgItem.ToEnumerationString()}";
        }
    }
    
    
    [AttributeUsage(AttributeTargets.Method, AllowMultiple = true)]
    [Serializable]
    public class BeginningGrammarDefinition : GrammarDefinition
    {
        public BeginningGrammarDefinition(string name, params string[] cfgItem) : base(name, cfgItem)
        {
        }
        public BeginningGrammarDefinition(string name, Type t,params string[] cfgItem) : base(name, t, cfgItem)
        {
        }
    }
}