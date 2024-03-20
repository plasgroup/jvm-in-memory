using System.IO;
using System.Linq;
using YaccLexCS.ycomplier.util;

namespace YaccLexCS.ycomplier.LrParser
{
    [Serializable]
    public class Lr1Table
    {
        private DataFrame _goto;
        private DataFrame _transition;

        public DataFrame Goto
        {
            get => _goto;
            set => _goto = value;
        }

        public DataFrame Transition
        {
            get => _transition;
            set => _transition = value;
        }

        public void AddRow()
        {
            _goto.AddRow(_goto.Serials.Count);
            _transition.AddRow(_transition.Serials.Count);
        }

        public override string ToString()
        {
            return _goto.ToStringTable() + "\r\n" + _transition.ToStringTable();
        }

        public int RowCount => _goto.Count();

        public void OutputToFilesAsCsv(string gotoTablePath, string transitionTablePath)
        {
            File.WriteAllText(gotoTablePath, this._goto.CsvText);
            File.WriteAllText(transitionTablePath, this._transition.CsvText);
        }
        public Lr1Table() { }
        public Lr1Table(CfgProducerDefinition definition)
        {
            var terminations = definition.Terminations;
            var nonTerminations = definition.NonTerminations;
            _goto = new DataFrame(nonTerminations.ToArray().Prepend("I(X)"));
            _transition = new DataFrame(terminations.ToArray().Prepend("I(X)").Append("$"));
            
        }
    }
}