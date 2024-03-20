using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace YaccLexCS.ycomplier.automata.re
{
    public class ReEdge : AutomataEdge
    {
      

        public override string ToString()
        {
            return $"[RE Edge ({FromNode.NodeId} -> {ToNode.NodeId})] use strategy = {IsCanTrans}";
        }

        public ReEdge(AutomataNode fromNode, AutomataNode toNode, InTransEvent eventTransInEdge, ITransCondition transCondition) : base(fromNode, toNode, eventTransInEdge, transCondition)
        {
        }

        public ReEdge(AutomataNode fromNode, AutomataNode toNode, ITransCondition transCondition) : base(fromNode, toNode, transCondition)
        {
        }
    }


    public class ReAutomata : Automata
    {
        private static readonly Automata RegexAutomata = new Lazy<Automata>(ReParserBuilder.BuildReParserAutomata).Value;
        
        //ensure the expression provided is a top level expression

        private static Automata EndParse(Automata regexBuilderAutomata)
        {
            var context = regexBuilderAutomata.Context;
            var strStack = (Stack<string>)context["tmp_strStack"];
            var andStack = (Stack<Automata>) context["stack_AndAutomata"];
            
            var cur = (string) context["tmp_cur"];
            var curAutomata = (Automata) context["automata"];
            var orExpAutomataStack =  (Stack<List<Automata>>) context["stack_OrAutomata"];
            var orExpAutomata = (List<Automata>) context["orExpAutomata"];
            var orExpStack =  (Stack<List<string>>) context["tmp_OrExpStack"];
            var orExp = (List<string>) context["tmp_OrExp"];
            $"======= Try End Parse ====".DebugOutPut();
            var a = orExpAutomata.Append(curAutomata);
            var finalAutomata = ReAutomataConstruction.OrMergeAutomata(a);
            return finalAutomata;
        }

         public static Automata BuildAutomataFromExp(string exp)
         {
            var sb = new StringBuilder(exp);

            var regexBuilderAutomata = RegexAutomata;
            regexBuilderAutomata.ResetAutomata();
             
            var initNode = new AutomataNode(0);
            var targetAutomata = new Automata();
            targetAutomata.AddNode(initNode);
            targetAutomata.SetStartState(0);
             
            regexBuilderAutomata.Context["initNode"] = initNode;
            regexBuilderAutomata.Context["lastNode"] = initNode;
            regexBuilderAutomata.Context["automata"] = targetAutomata; // The automata current in building process.
          
            regexBuilderAutomata.Context["preResultNode"] = null!;
            regexBuilderAutomata.Context["orExpAutomata"] = new List<Automata>();

            regexBuilderAutomata.Context["stack_lastNode"] = new Stack<AutomataNode>();
            regexBuilderAutomata.Context["stack_OrAutomata"] = new Stack<List<Automata>>();
            regexBuilderAutomata.Context["stack_AndAutomata"] = new Stack<Automata>();
            regexBuilderAutomata.Context["stack_Brace"] = new Stack<char>();
            regexBuilderAutomata.Context["stack_preResultNode"] = new Stack<AutomataNode>();
            
            regexBuilderAutomata.Context["tmp_cur"] = "";
            regexBuilderAutomata.Context["tmp_strStack"] = new Stack<string>();
            regexBuilderAutomata.Context["tmp_OrExp"] = new List<string>();
            regexBuilderAutomata.Context["tmp_OrExpStack"] = new Stack<List<string>>();
            
            while (sb.Length > 0)
            {
                var c = sb[0];
                sb.Remove(0, 1);
                regexBuilderAutomata.ParseSingleInputFromCurrentStates(c);
            }
            
            regexBuilderAutomata.Context["automata"].PrintToConsole();
            var result = EndParse(regexBuilderAutomata);
            result.SetStartState(0).SetAcceptState(result.Nodes.Count - 1);
            return result;
         }
        public ReAutomata()
        {  
            // AddNode(new AutomataNode(0));
            // AddNode(new AutomataNode(1));
            // SetStartState(0).InitState();
            //
            // AddEdge(new ReEdge( GetNode(0), GetNode(1), CommonTransitionStrategy.EpsilonTrans.Instance));
            // this.PrintToConsole();
            // ApplyClosure();
            // this.PrintToConsole();
        }
    }
}