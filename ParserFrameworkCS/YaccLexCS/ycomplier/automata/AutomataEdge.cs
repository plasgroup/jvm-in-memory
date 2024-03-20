using System;

namespace YaccLexCS.ycomplier.automata
{
    
    [Serializable]
    public abstract class AutomataEdge
    {

        public AutomataNode FromNode;
        public AutomataNode ToNode;
        
        public delegate object? InTransEvent(object input, params object[] objs);
       // public delegate bool TransStrategy(AutomataContext ctx, AutomataEdge edge, params object[] objs);

        public readonly InTransEvent EventTransInEdge;
        public readonly ITransCondition IsCanTrans;
        public AutomataEdge( AutomataNode fromNode, AutomataNode toNode,InTransEvent eventTransInEdge, ITransCondition transCondition)
        {
            EventTransInEdge = eventTransInEdge;
            IsCanTrans = transCondition;
            FromNode = fromNode;
            ToNode = toNode;
        }

        public AutomataEdge( AutomataNode fromNode, AutomataNode toNode, ITransCondition transCondition)
        {
            IsCanTrans = transCondition;
            FromNode = fromNode;
            ToNode = toNode;
        }
    }
}