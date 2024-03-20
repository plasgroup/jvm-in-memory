//#define DELTAILMODE
using System;
using System.Collections.Generic;
using System.Linq;


namespace YaccLexCS.ycomplier.automata.re
{
    public static class ReAutomataConstruction
    {


        public static object? EnterQuestionChar(object input, object[] objs)
        {
            var context = (AutomataContext)objs[0];

            var bStack = (Stack<char>)context["stack_Brace"];

            var strStack = (Stack<string>)context["tmp_strStack"];
            var andStack = (Stack<Automata>)context["stack_AndAutomata"];

            var cur = (string)context["tmp_cur"];
            var curAutomata = (Automata)context["automata"];

#if DELTAILMODE
            $"meet *, cur automata = \n {curAutomata}".PrintToConsole();
#endif

            var orExpAutomataStack = (Stack<List<Automata>>)context["stack_OrAutomata"];
            var orExpAutomata = (List<Automata>)context["orExpAutomata"];
            var orExpStack = (Stack<List<string>>)context["tmp_OrExpStack"];
            var orExp = (List<string>)context["tmp_OrExp"];
            var lastNodeStack = (Stack<AutomataNode>)context["stack_lastNode"];
            var lastNode = (AutomataNode)context["lastNode"];

            var lastResult = (AutomataNode)context["preResultNode"];
#if DELTAILMODE
            $"lastResult Node = {lastResult?.NodeId??""}".PrintToConsole();
            $"last Node = {lastNode.NodeId}".PrintToConsole();
#endif
            curAutomata.AddEdge(new ReEdge(lastResult, lastNode, CommonTransitionStrategy.EpsilonTrans.Instance));
#if DELTAILMODE
            curAutomata.PrintToConsole();
#endif
            context["preResultNode"] = lastNode;

            context["tmp_cur"] = (string)context["tmp_cur"] + (char)input;
            return null;
        }


        public static object? EnterStarChar(object input, object[] objs)
        {
            var context = (AutomataContext) objs[0];
         
            var bStack = (Stack<char>) context["stack_Brace"];
            
            var strStack = (Stack<string>) context["tmp_strStack"];
            var andStack = (Stack<Automata>) context["stack_AndAutomata"];
            
            var cur = (string) context["tmp_cur"];
            var curAutomata = (Automata) context["automata"];

#if DELTAILMODE
            $"meet *, cur automata = \n {curAutomata}".PrintToConsole();
#endif
            
            var orExpAutomataStack =  (Stack<List<Automata>>) context["stack_OrAutomata"];
            var orExpAutomata = (List<Automata>) context["orExpAutomata"];
            var orExpStack =  (Stack<List<string>>) context["tmp_OrExpStack"];
            var orExp = (List<string>) context["tmp_OrExp"];
            var lastNodeStack = (Stack<AutomataNode>) context["stack_lastNode"];
            var lastNode = (AutomataNode) context["lastNode"];
            
            var lastResult = (AutomataNode) context["preResultNode"];
#if DELTAILMODE
            $"lastResult Node = {lastResult?.NodeId??""}".PrintToConsole();
            $"last Node = {lastNode.NodeId}".PrintToConsole();
#endif
            curAutomata.AddEdge(new ReEdge(lastResult, lastNode, CommonTransitionStrategy.EpsilonTrans.Instance));
            curAutomata.AddEdge(new ReEdge(lastNode, lastResult, CommonTransitionStrategy.EpsilonTrans.Instance));
#if DELTAILMODE
            curAutomata.PrintToConsole();
#endif
            context["preResultNode"] = lastNode;
            
            context["tmp_cur"] = (string)context["tmp_cur"] + (char)input;
            return null;
        }
        public static object? EnterPlusChar(object input, object[] objs)
        {
            var context = (AutomataContext) objs[0];
         
            var bStack = (Stack<char>)context["stack_Brace"];
            
            
            var strStack = (Stack<string>) context["tmp_strStack"];
            var andStack = (Stack<Automata>) context["stack_AndAutomata"];
            
            var cur = (string) context["tmp_cur"];
            var curAutomata = (Automata) context["automata"];
#if DELTAILMODE
            $"meet +, cur automata = \n {curAutomata}".PrintToConsole();
#endif
            
            var orExpAutomataStack =  (Stack<List<Automata>>) context["stack_OrAutomata"];
            var orExpAutomata = (List<Automata>) context["orExpAutomata"];
            var orExpStack =  (Stack<List<string>>) context["tmp_OrExpStack"];
            var orExp = (List<string>) context["tmp_OrExp"];
            var lastNodeStack = (Stack<AutomataNode>) context["stack_lastNode"];
            var lastNode = (AutomataNode) context["lastNode"];
            
            var lastResult = (AutomataNode) context["preResultNode"];
            
#if DELTAILMODE
            $"lastResult Node = {lastResult.NodeId}".PrintToConsole();
            $"last Node = {lastNode.NodeId}".PrintToConsole();
#endif
            
            curAutomata.AddEdge(new ReEdge(lastNode, lastResult, CommonTransitionStrategy.EpsilonTrans.Instance));
            
#if DELTAILMODE
            curAutomata.PrintToConsole();
#endif
           
            context["preResultNode"] = lastNode;
            
            context["tmp_cur"] = (string)context["tmp_cur"] + (char)input;
            return null;
        }

        public static object? EnterLeftMBrace(object input, object[] objs)
        {
            var context = (AutomataContext) objs[0];
#if DELTAILMODE
            $"meet [, begin a new exp, to get char range state".PrintToConsole();
#endif
          
            context["v_charRange_desc"] = "";
            var curAutomata = (Automata) context["automata"];
            context["preResultNode"] = curAutomata.NodeMap[curAutomata.Nodes.Count - 1];
            context["tmp_cur"] = (string)context["tmp_cur"] + (char)input;
            
            //var bStack = (Stack<char>)context["stack_Brace"];
            //bStack.Push('(');
            
            return null;
        }
        public static object? EnterLeftLargeBrace(object input, object[] objs)
        {
            var context = (AutomataContext) objs[0];
            context["v_repeat_num"] = "";
            var curAutomata = (Automata) context["automata"];
            context["preResultNode"] = curAutomata.NodeMap[curAutomata.Nodes.Count - 1];
            context["tmp_cur"] = (string)context["tmp_cur"] + (char)input;

            return null;
        }
        
        public static object? LeaveLargeBrace(object input, object[] objs)
        {
            var context = (AutomataContext) objs[0];
            var num = Convert.ToInt32(context["v_repeat_num"] + "");
           
            var lastNode = (AutomataNode) context["lastNode"];
            var preResultNode = (AutomataNode) context["preResultNode"];
            var automata = (Automata) context["automata"];

            var node = new AutomataNode(automata.Nodes.Count);
            automata.AddNode(node);
            automata.AddEdge(new ReEdge(lastNode, preResultNode,
                delegate(object o, object[] objects)
                {
                    var context = (AutomataContext) o;
                    context["v_repeat"] = (int)context["v_repeat"] + 1;
                    return null;
                }
                ,new CommonTransitionStrategy
                .CustomTrans(delegate(AutomataContext? ctx, object? item, object[]? objects)
                {
                    if (!(ctx.ContainsKey("v_repeat")))
                    {
                        (ctx)["v_repeat"] = 0;
                    }

                    return (int)(ctx["v_repeat"]) < num;
                })));
            automata.AddEdge(new ReEdge(lastNode, node, delegate(object o, object[] objects)
            {
                if (((AutomataContext)o).ContainsKey("v_repeat"))
                {
                    ((AutomataContext) o)["v_repeat"] = 0;
                }

                return null;
            },new CommonTransitionStrategy
                .CustomTrans(delegate(AutomataContext? ctx, object? item, object[]? objects)
                {
                    if (ctx.ContainsKey("v_repeat"))
                    {
                        return (int)ctx["v_repeat"] == num;
                    }

                    return false;
                })));
            
            
            context["lastNode"] = node;
            return null;
        }

        public static object? ProcessSlashReturn(object input, object[] objs)
        {
            var context = (AutomataContext) objs[0];
            var stack = (Stack<object>) context["stateStack"];
            stack.Pop();
            return null;
        }
        public static object? StatePushOne(object input, object[] objs)
        {
            var context = (AutomataContext) objs[0];
            if (!context.ContainsKey("stateStack"))
                context["stateStack"] = new Stack<object>();
            ((Stack<object>)context["stateStack"]).Push(1);
            return null;
        }
        public static object? StatePushZero(object input, object[] objs)
        {
            var context = (AutomataContext) objs[0];
            if (!context.ContainsKey("stateStack"))
                context["stateStack"] = new Stack<object>();
            ((Stack<object>)context["stateStack"]).Push(0);
            return null;
        }

       
        public static object? ProcessOr(object input, object[] objs)
        {
            var context = (AutomataContext) objs[0];
#if DELTAILMODE
            $"meet |, begin a new exp, save cur = {context["tmp_cur"]} to stack".PrintToConsole();
#endif
            
            var bStack = (Stack<char>)context["stack_Brace"];
            //bStack.Push('(');
            
            var strStack = (Stack<string>)context["tmp_strStack"];
            var andStack = (Stack<Automata>) context["stack_AndAutomata"];
            
            var cur = (string) context["tmp_cur"];
            var curAutomata = (Automata) context["automata"];
            var orExpAutomataStack =  (Stack<List<Automata>>) context["stack_OrAutomata"];
            var orExpAutomata = (List<Automata>) context["orExpAutomata"];
            var orExpStack =  (Stack<List<string>>) context["tmp_OrExpStack"];
            var orExp = (List<string>) context["tmp_OrExp"];
            
            orExp.Add(cur);
            context["tmp_cur"] = "";
            orExpAutomata.Add(curAutomata);
            var initNode = new AutomataNode(0);
#if DELTAILMODE
            $"save current automata = \n {curAutomata}".PrintToConsole();
            $"now or automata list len = {orExpAutomata.Count}".PrintToConsole();
#endif
          
            context["automata"] = new Automata().AddNode(initNode).SetStartState(0);
            context["lastNode"] = initNode;
            context["preResultNode"] = null;
          
            return null;
        }

        private static char[] _nonPrintChars = new[] {'t', 'n', 'w', 'f', 'r', 's', 'S', 't', 'v'};
        public static object? ProcessSlashChar(object input, object[] objs)
        {
            var context = (AutomataContext) objs[0];
            var stack = (Stack<object>) context["stateStack"];
            var real = input;
            if (_nonPrintChars.Contains((char)input))
            {
                var c = (char) input;

                real = c switch
                {
                    't' => '\t',
                    'f' => '\f',
                    'n' => '\n',
                    'r' => '\r',
                    'v' => '\v',
                    _ => real
                };
            }
            if ((int)stack.Peek() == 1)
            {
                
                context["v_charRange_desc"] = context["v_charRange_desc"] + "" + real;
#if DELTAILMODE
                $"now = {context["v_charRange_desc"]}".PrintToConsole();
#endif
               
            }else if ((int) stack.Peek() == 0)
            {
                
                AddSingleCharCompareNode(real, objs);
#if DELTAILMODE
                ((Automata) context["automata"]).PrintToConsole();
#endif
                
            }

            return null;
        }
        public static object? LeaveMBrace(object input, object[] objs)
        {
            var context = (AutomataContext) objs[0];
#if DELTAILMODE
            $"\n process {context["v_charRange_desc"]}".PrintToConsole();
#endif
            
            var desc = (string)context["v_charRange_desc"];
            List<(char f, char e)> ranges = new();
            HashSet<char> singleChar = new();
            var reverse = false;
            for (var i = 0; i < desc.Length;)
            {
                if (desc[i] == '^')
                {
                    if (reverse != true)
                    {
                        reverse = true;
                        i++;
                        continue;
                    }
                }

                if (i == desc.Length - 1)
                {
                    //singleChar.Add(desc[i++]);
                    if (!reverse)
                    {
                        singleChar.Add(desc[i++]);
                    }
                    else
                    {
                        var ascii = desc[i++] + 0;
                        ranges.Add(((char)0, (char)(ascii - 1)));
                        ranges.Add(((char)(ascii + 1), (char)255));
                        reverse = false;
                    }
                }
                else if (i < desc.Length - 1 && desc[i + 1] != '-')
                {
                    if (!reverse)
                    {
                        singleChar.Add(desc[i++]);
                    }
                    else
                    {
                        var ascii = desc[i++] + 0;
                        ranges.Add(((char)0, (char)(ascii - 1)));
                        ranges.Add(((char)(ascii + 1), (char)255));
                        reverse = false;
                    }
                    
                }
                else
                {
                    if (!reverse)
                    {
                        if (!(i < desc.Length - 2))
                            throw new Exception("error format in [...]");
                        ranges.Add((desc[i], desc[i + 2]));
                        i += 3;
                    }
                    else
                    {
                        if (!(i < desc.Length - 2))
                            throw new Exception("error format in [...]");
                        ranges.Add(((char)0, (char)(desc[i] - 1)));
                        ranges.Add(((char)(desc[i] + 1), (char)255));
                        i += 3;
                        reverse = false;
                    }
                   
                }
            }
            

            var targetRange = new List<(int start, int end)>();
            //区间合并
            foreach (var r in ranges.OrderBy(e => e.f))
            {
                if (targetRange.Count == 0)
                {
                    targetRange.Add(r);
                }
                else
                {
                    var last = targetRange[^1];
                    if (last.end < r.f)
                    {
                        targetRange.Add(r);
                    }
                    else
                    {
                        targetRange[^1] = (targetRange[^1].start, r.e); //merge
                    }
                }
            }
            //创建迁移判断策略
            var customTrans = new CommonTransitionStrategy
                .CustomTrans(delegate(AutomataContext? ctx, object? item, object[] objects)
                {
                    if (item == null) return false;
                    return singleChar.Contains((char) item) || (targetRange.Any(r => (char) item >= r.start && (char) item <= r.end));
                }, type: $"Range trans : [{targetRange.GetMultiDimensionString()}]");
            
#if DELTAILMODE
            targetRange.PrintEnumerationToConsole();
            singleChar.PrintEnumerationToConsole();
#endif
            
            
            //connect
            var lastNode = (AutomataNode) context["lastNode"];
            var automata = (Automata) context["automata"];

            var node = new AutomataNode(automata.Nodes.Count);
            automata.AddNode(node);
            automata.AddEdge(new ReEdge(lastNode, node, customTrans));
            context["lastNode"] = node;
            
#if DELTAILMODE
            $"add a char set {desc}".PrintToConsole();
#endif
            

            context["tmp_cur"] = (string)context["tmp_cur"] + (char)input;
            
#if DELTAILMODE
            automata.PrintToConsole();
#endif
            

            return null;

        }
        public static object? InReadingCharSet(object input, object[] objs)
        {
            var context = (AutomataContext) objs[0];
#if DELTAILMODE
            $"get char {input} (reading char set state)".PrintToConsole();
#endif
           
            context["v_charRange_desc"] = context["v_charRange_desc"] + "" + input;
            context["tmp_cur"] = (string)context["tmp_cur"] + (char)input;
#if DELTAILMODE
            $"now = {context["v_charRange_desc"]}".PrintToConsole();
#endif
           
           
            
            return null;
        }
        public static object? InReadingNum(object input, object[] objs)
        {
            var context = (AutomataContext) objs[0];
#if DELTAILMODE
            $"get char {input} (reading char set state)".PrintToConsole();
#endif
            
            context["v_repeat_num"] = context["v_charRange_desc"] + "" + input;
            context["tmp_cur"] = (string)context["tmp_cur"] + (char)input;
            
           
            
            return null;
        }
        public static object? EnterRightBrace(object input, object[] objs)
        {
            var context = (AutomataContext) objs[0];
         
            var bStack = (Stack<char>)context["stack_Brace"];
            
            
            var strStack = (Stack<string>)context["tmp_strStack"];
            var andStack = (Stack<Automata>) context["stack_AndAutomata"];
            
            var cur = (string) context["tmp_cur"];
            var curAutomata = (Automata) context["automata"];
            
#if DELTAILMODE
            $"meet ), return from subroutine, cur automata = \n {curAutomata}".PrintToConsole();
#endif
            
            
            
            var orExpAutomataStack =  (Stack<List<Automata>>) context["stack_OrAutomata"];
            var orExpAutomata = (List<Automata>) context["orExpAutomata"];
            var orExpStack =  (Stack<List<string>>) context["tmp_OrExpStack"];
            var orExp = (List<string>) context["tmp_OrExp"];
            var lastNodeStack = (Stack<AutomataNode>) context["stack_lastNode"];
            var lastNode = (AutomataNode) context["lastNode"];
            
            
            orExp.Add(cur);
            orExpAutomata = orExpAutomataStack.Pop();
            context["orExpAutomata"] = orExpAutomata;
            var result = OrMergeAutomata(orExp);
            var resultAutomata = OrMergeAutomata(orExpAutomata.Append(curAutomata));
#if DELTAILMODE
            $"build finish..begin merge result = {result}".PrintToConsole();
#endif
            
            orExp.Clear();
            orExp = null;
            context["tmp_OrExp"] = orExpStack.Pop();
            orExp = (List<string>) context["tmp_OrExp"];
            
            if (!bStack.Any())
                throw new Exception("brace exception");
            bStack.Pop();
            cur = strStack.Pop();
            
#if DELTAILMODE
            $"restore str = {cur}\n".PrintToConsole();
#endif
            
            curAutomata = andStack.Pop();
            context["automata"] = curAutomata;
            var r = ConcatAutomata(cur, result);
            
            resultAutomata = ConcatAutomata(curAutomata, resultAutomata);
            context["lastNode"] = resultAutomata.NodeMap[resultAutomata.Nodes.Count - 1];
            var preNode = ((Stack<AutomataNode>) context["stack_preResultNode"]).Pop();
            context["preResultNode"] = preNode;
#if DELTAILMODE
            $"preResultNode = {((AutomataNode)context["lastResult"]).NodeId}".PrintToConsole();
#endif
           
            
          
            
            return null;
        }

        
        public static Automata? OrMergeAutomata(IEnumerable<Automata> orExpAutomata)
        {
            orExpAutomata = orExpAutomata.Where(a => a.Nodes.Count > 1);
#if DELTAILMODE
            $"Try merge {orExpAutomata.Count()} automata".PrintToConsole();
#endif
            
            
            if (!orExpAutomata.Any())
            {
                return null!;
            }

            if (orExpAutomata.Count() == 1)
                return orExpAutomata.First();

            var automata = new Automata();
            var iNode = new AutomataNode(0);
            automata.AddNode(iNode).SetStartState(0);
            var endSet = new List<int>();
            var startSet = new List<int>();
            foreach (var a in orExpAutomata)
            {
#if DELTAILMODE
                $"try add \n {a}".PrintToConsole();
#endif
                
                var c = automata.Nodes.Count;
                startSet.Add(c);
                foreach (var node in a.Nodes)
                {
                    var ess = a.NodeNext[node.NodeId];
                    automata.AddNode(new AutomataNode((int) node.NodeId + c));
                    
                }
                endSet.Add(c + a.Nodes.Count - 1);
                foreach (var e in a.Edges)
                {
                    automata.AddEdge(new ReEdge(automata.NodeMap[(int)e.FromNode.NodeId + c]
                        , automata.NodeMap[(int)e.ToNode.NodeId + c], e.EventTransInEdge, e.IsCanTrans));
                }
                automata.PrintToConsole();
                
                
            }
            
            var end = automata.Nodes.Count;
            automata.AddNode(new AutomataNode(end));
            foreach (var s in startSet)
            {
                automata.AddEdge(new ReEdge(automata.NodeMap[0], automata.NodeMap[s], 
                    CommonTransitionStrategy.EpsilonTrans.Instance));
            }
            foreach (var e in endSet)
            {
                automata.AddEdge(new ReEdge(automata.NodeMap[e], automata.NodeMap[end], 
                    CommonTransitionStrategy.EpsilonTrans.Instance));
            }
            automata.PrintToConsole();
           
            return automata;

        }

        public static string ConcatAutomata(string a1, string a2)
        {
#if DELTAILMODE
            $"Concat {a1} with {a2}".PrintToConsole();
#endif
            
            return a1 + a2;
        }
        public static Automata? ConcatAutomata(Automata? a1, Automata? a2)
        {
            if (a1 == null)
                return a2;
            if (a2 == null)
                return a1;
#if DELTAILMODE
            $"Concat {a1} \n with {a2}\n".PrintToConsole();
#endif
            
            var c = a1.Nodes.Count;
            var newNodes = a2.Nodes.Select(e => new AutomataNode(c + (int) e.NodeId)).ToArray();
            
            a1.AddNodes(newNodes);
            foreach(var node in newNodes)
            {
                var es = a2.NodeNext[(int) node.NodeId - c];
                foreach (var e in es)
                {
                    a1.AddEdge(new ReEdge(a1.NodeMap[(int)e.FromNode.NodeId + c], a1.NodeMap[(int)e.ToNode.NodeId + c]
                        , e.EventTransInEdge, e.IsCanTrans));
                }
                
            }

            foreach (var acc in a1.AcceptState)
            {
                foreach (var s in a2.StartNodes)
                {
                    a1.AddEdge(new ReEdge(a1.NodeMap[acc], a1.NodeMap[c + (int)s], CommonTransitionStrategy.EpsilonTrans.Instance));
                }
                
            }
            a1.AcceptState.Clear();
            a1.AcceptState.Add(a1.Nodes.Count - 1);
#if DELTAILMODE
            a1.PrintToConsole();
#endif
            return a1;
        }
        public static string OrMergeAutomata(IEnumerable<string> automatas)
        {
            var str = $"[automata: {automatas.Aggregate("",(a,b) => a + " | " + b)}]";
            return str;
        }
        public static object? EnterLeftBrace(object input, object[] objs)
        {
            var context = (AutomataContext) objs[0];
            
#if DELTAILMODE
            $"meet (, begin a new exp, save cur = {context["tmp_cur"]} to stack".PrintToConsole();
#endif
            
            var bStack = (Stack<char>)context["stack_Brace"];
            
            
            var strStack = (Stack<string>)context["tmp_strStack"];
            var andStack = (Stack<Automata>) context["stack_AndAutomata"];
            var preResultNodeStack = (Stack<AutomataNode>) context["stack_preResultNode"];

            var lastNodeStack = (Stack<AutomataNode>) context["stack_lastNode"];
            var lastNode = (AutomataNode) context["lastNode"];
            
            var cur = (string) context["tmp_cur"];
            var curAutomata = (Automata) context["automata"];
            
            bStack.Push('(');
            curAutomata.SetAcceptState(curAutomata.NodeMap[curAutomata.Nodes.Count - 1].NodeId);
            strStack.Push(cur);
            andStack.Push(curAutomata);
            preResultNodeStack.Push(lastNode);
            
            var orExpAutomataStack =  (Stack<List<Automata>>) context["stack_OrAutomata"];
            var orExpAutomata = (List<Automata>) context["orExpAutomata"];
            var orExpStack =  (Stack<List<string>>) context["tmp_OrExpStack"];
            var orExp = (List<string>) context["tmp_OrExp"];
           
            
            orExpAutomataStack.Push(orExpAutomata);
            orExpStack.Push(orExp);

            
            context["orExpAutomata"] = new List<Automata>();
            context["tmp_OrExp"] = new List<string>();
            
            context["tmp_cur"] = "";
            var initNode = new AutomataNode(0);
            context["automata"] = new Automata().AddNode(initNode).SetStartState(0);
            
            lastNodeStack.Push(lastNode);
            context["lastNode"] = initNode;
            context["preResultNode"] = null;
#if DELTAILMODE
             strStack.GetMultiDimensionString().PrintToConsole();
            andStack.Count.PrintToConsole();
#endif
           

            return null;
        }
        
        public static object? AddSingleCharCompareNode(object input, object[] objs)
        {
            var context = (AutomataContext) objs[0];
            var lastNode = (AutomataNode) context["lastNode"];
            var automata = (Automata) context["automata"];

            var node = new AutomataNode(automata.Nodes.Count);
            automata.AddNode(node);
            automata.AddEdge(new ReEdge(lastNode, node, new CommonTransitionStrategy.EqualJudgeTrans<char>((char) input)));
            context["preResultNode"] = lastNode;
            context["lastNode"] = node;
#if DELTAILMODE
            $"add a normal char {input}".PrintToConsole();
#endif
           

            context["tmp_cur"] = (string)context["tmp_cur"] + (char)input;
            
            return null;
        }
        
        public static object? AddDotCompareNode(object input, object[] objs)
        {
            var context = (AutomataContext) objs[0];
            var lastNode = (AutomataNode) context["lastNode"];
            var automata = (Automata) context["automata"];

            var node = new AutomataNode(automata.Nodes.Count);
            automata.AddNode(node);
            automata.AddEdge(new ReEdge(lastNode, node, new CommonTransitionStrategy.CharacterRangeTrans((char)0, (char)255)));
            context["preResultNode"] = lastNode;
            context["lastNode"] = node;
#if DELTAILMODE
            $"add a .".PrintToConsole();
#endif
            

            context["tmp_cur"] = (string)context["tmp_cur"] + (char)input;
            
            //automata.PrintToConsole();
            return null;
        }
    }
}