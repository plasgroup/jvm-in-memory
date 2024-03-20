using System;

namespace YaccLexCS.ycomplier.automata
{
    [Serializable]
    public class AutomataNode
    {
       
        public delegate object? TransToNodeEvent(object? input, params object[] objects);

        public readonly object NodeId;
        public TransToNodeEvent EventTransToNode;

        public AutomataNode(TransToNodeEvent eventTransToNode, object nodeId)
        {
            EventTransToNode = eventTransToNode;
            NodeId = nodeId;
        }

        public AutomataNode(object nodeId)
        {
            NodeId = nodeId;
        }
    }
}