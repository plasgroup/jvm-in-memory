#define NOOUTPUTAUTOMATA
#define NOSHOWAUTOMATATRANS

using System;
using System.Collections.Generic;
using System.Linq;

namespace YaccLexCS.ycomplier.automata
{
    [Serializable]
    public class Automata
    {
        public IEnumerable<object> StartState = new HashSet<object>();
        public HashSet<object> AcceptState = new();
        public readonly HashSet<object> CurrentStateCollection = new();
        
        public HashSet<object> StartNodes => StartState.ToHashSet();

        public readonly List<AutomataNode> Nodes = new(); //节点集合
        public readonly List<AutomataEdge> Edges = new(); //边集合
        
        public readonly Dictionary<object, List<AutomataEdge>> NodeNext = new(); // map node id -> edges that from this node
        public readonly Dictionary<object, AutomataNode> NodeMap = new(); //map node id -> node


        public readonly AutomataContext Context = new();
        public Automata SetStartState(params object[] idSet)
        {
            StartState = new HashSet<object>(idSet);
            return this;
        }
        public object? RunWithInputQueue(Queue<object> input)
        {
            return RunWithInputQueue(input, _ => true);
        }
        public object? RunWithInputQueue(Queue<object> input, Func<Automata, object>? runningStrategy)
        {
            while (input.Any())
            {
                var e = input.Dequeue();
                ParseSingleInputFromCurrentStates(e);
            }

            return runningStrategy?.Invoke(this);
        }
        
        public object Run(Func<Automata, object> func)
        {
            return func.Invoke(this);
        }
        public bool IsCanTransWith(object input)
        {
            ApplyClosure();
            var transEdges = CurrentStateCollection.SelectMany(e => 
                NodeNext[e].Where(edge => edge.IsCanTrans.Judge(Context, input, null))).ToArray();
            return transEdges.Any();
        }
        public void ParseSingleInputFromCurrentStates(object input)
        {
#if  !NOSHOWAUTOMATATRANS
            $">> get input {input}".PrintToConsole();
#endif
            ApplyClosure();
            var transEdges = CurrentStateCollection.SelectMany(e => 
                NodeNext[e].Where(edge => edge.IsCanTrans.Judge(Context, input, null))).ToArray();
#if  !NOSHOWAUTOMATATRANS
            foreach (var node in transEdges)
            {
                $"can trans from {node.FromNode.NodeId} -> {node.ToNode.NodeId}".PrintToConsole();
            }
#endif    
            CurrentStateCollection.Clear();
            foreach (var e in transEdges)
            {
                e.EventTransInEdge?.Invoke(input, Context);
                CurrentStateCollection.Add(e.ToNode.NodeId);
            }
#if  !NOSHOWAUTOMATATRANS            
            $"get non-closure state = {CurrentStateCollection.GetMultiDimensionString()}".PrintToConsole();
#endif
            //closure
            ApplyClosure();
            
#if  !NOSHOWAUTOMATATRANS
            $"after closure state = {CurrentStateCollection.GetMultiDimensionString()}".PrintToConsole();
            $"<< process {input} finish.\n".PrintToConsole();
#endif
        }
        public bool ParseFromCurrentStates(IEnumerable<object> input)
        {
            $">> parse {input.GetMultiDimensionString()}".PrintToConsole();
            $"cur state = {CurrentStateCollection.GetMultiDimensionString()} and apply closure.".PrintToConsole();
            ApplyClosure();
            $"cur state = {CurrentStateCollection.GetMultiDimensionString()} after closure.\n".PrintToConsole();
            
            foreach (var e in input)
            {
                $"try input {e} from {CurrentStateCollection.GetMultiDimensionString()}".PrintToConsole();
                ParseSingleInputFromCurrentStates(e);
                $"after input state = {CurrentStateCollection.GetMultiDimensionString()}".PrintToConsole();
                
                if (!CurrentStateCollection.Any())
                {
                    $"Parse error after input {e}".PrintToConsole();
                    return false;
                }
                "".PrintToConsole();
            }

            return true;
        }
        public Automata SetAcceptState(params object[] idSet)
        {
            AcceptState = new HashSet<object>(idSet);
            return this;
        }

        public Automata InitState()
        {
            return ResetAutomata();
        }

        public Automata ResetAutomata()
        {
            Context.ResetContext();
            CurrentStateCollection.Clear();
            foreach (var s in StartState)
                CurrentStateCollection.Add(s);
            ApplyClosure();
            return this;
        }

        public Automata AddNode(AutomataNode node)
        {
            if (NodeMap.ContainsKey(node.NodeId))
                throw new Exception("Repeat same node id");
            NodeNext[node.NodeId] = new List<AutomataEdge>();
            NodeMap[node.NodeId] = node;
            Nodes.Add(node);
            return this;
        }

        public void AddNodes(IEnumerable<AutomataNode> node)
        {
            foreach (var n in node)
            {
                AddNode(n);
            }
        }

        
        public bool IsAccepted()
        {
            return CurrentStateCollection.Any(s => AcceptState.Contains(s));
        }
        public void ApplyClosure()
        {
            var set = CurrentStateCollection.ToList();
            
            foreach (var edge in set.Select(node => NodeNext[node])
                .SelectMany(e => e.Where(edge => edge.IsCanTrans.Judge(Context, null))))
            {
                edge.EventTransInEdge?.Invoke(null!, Context);
                CurrentStateCollection.Add(edge.ToNode.NodeId);
            }
            if(CurrentStateCollection.Count != set.Count)
                ApplyClosure();

        }

        public void AddEdges(IEnumerable<AutomataEdge> edge)
        {
            foreach (var e in edge)
            {
                AddEdge(e);
            }
        }
        public void AddEdge(AutomataEdge edge)
        {
            Edges.Add(edge);
            if(!NodeMap.ContainsKey(edge.FromNode.NodeId))
                NodeMap.Add(edge.FromNode.NodeId, edge.FromNode);
            if(!NodeMap.ContainsKey(edge.ToNode.NodeId))
                NodeMap.Add(edge.ToNode.NodeId, edge.ToNode);
            if (!NodeNext.ContainsKey(edge.FromNode.NodeId))
                NodeNext.Add(edge.FromNode.NodeId, new List<AutomataEdge>());
            if(!NodeNext[edge.FromNode.NodeId].Contains(edge))
                NodeNext[edge.FromNode.NodeId].Add(edge);
        }
        
        public Automata()
        {
            
        }
        
        

        public override string ToString()
        {
#if NOOUTPUTAUTOMATA
            return "";
#endif
            var str = "===============================================\n";
            str += $"[Cur state: {CurrentStateCollection.Aggregate("", (a, b) => a + ", " + b)}]\n";
            str += $"start from node_id {StartState.Aggregate("",(a, b) => a + ", " + b)}\n end state = {AcceptState.GetMultiDimensionString()}\n";
            var i = 0;
            foreach(var node in Nodes)
            {
                str += $"*** Node ID = {node.NodeId}:\n";
                str = NodeNext[node.NodeId].Aggregate(str, (current, e) => current + $"  * Edge_{i++}: {e}\n");
                
                str += "\n";
            }
            return str;
        }

        public AutomataNode GetNode(object id)
        {
            return NodeMap[id];
        }
    }
}