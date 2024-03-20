using System.Collections.Generic;
using System.Linq;

namespace ConfigFileGenerator.configurator.CongFileGen
{
    public class GrammarFileStrut
    {
        public class GrammarMethodDesc
        {
            public string Name;
            public string WordName;
            public List<string> Descs;
            public bool IsBeginningWord = false;

            public GrammarMethodDesc(string name, List<string> descs, bool isBeginningWord, string wordName)
            {
                this.Name = name;
                this.Descs = descs;
                this.IsBeginningWord = isBeginningWord;
                WordName = wordName;
            }

            public override string ToString()
            {
                return $"{Name}  ==> \r\n" +
                       Descs.Aggregate("", (a, b)=> a + "\t* " + b  + "\r\n");
            }
        }

        public readonly List<GrammarMethodDesc> Descs = new();
        public readonly string FileName;

        public GrammarFileStrut(string fileName)
        {
            FileName = fileName;
        }

        public void AddMethod(string name, IEnumerable<string> descs ,string wName, bool isBeginningWord = false)
        {
            Descs.Add(new GrammarMethodDesc(name, descs.ToList(), isBeginningWord, wName));
        }

        public override string ToString()
        {
            return $"in file = {FileName}\r\n" + Descs.Aggregate("", (a, b) => a + "\r\n" + b);
        }
    }
}