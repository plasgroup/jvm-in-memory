using System.Collections.Generic;
using System.Linq;

namespace ConfigFileGenerator.configurator.CongFileGen
{
    public class TokenMethod
    {
        public class TokenAttributeDesc
        {
            public readonly string Name;
            public readonly string Desc;
            public readonly int Priority;
            public readonly bool IsRegex = false;
            public TokenAttributeDesc(string name, string desc, bool isRegex, int priority = 0)
            {
                Name = name;
                Desc = desc;
                IsRegex = isRegex;
                Priority = priority;
            }

            public override string ToString()
            {
                return (Name, Desc, IsRegex, Priority) + "";
            }
        }
        public readonly string MethodName;
        public readonly List<TokenAttributeDesc> Descs = new();
        public TokenMethod(string methodName)
        {
            MethodName = methodName;
        }

        public override string ToString()
        {
            return $".{MethodName} => {Descs.Aggregate("", (a, b) => a + "\r\n\t* " + b)}";
        }

        public TokenMethod AddTokenAttr(string tName, string desc, bool isRegex = false, int priority = 0)
        {
            Descs.Add(new TokenAttributeDesc(tName, desc, isRegex, priority));
            return this;
        }
    }
}