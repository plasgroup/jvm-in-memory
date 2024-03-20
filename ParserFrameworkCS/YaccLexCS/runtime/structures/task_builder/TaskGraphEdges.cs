

namespace YaccLexCS.runtime.structures.task_builder
{
    internal class TaskGraphEdges
    {
        Dictionary<int, List<int>> edges = new Dictionary<int, List<int>> ();
        internal void AddEdge(int fromNode, int toNode)
        {
            if(!edges.ContainsKey(fromNode))
                edges[fromNode] = new List<int> ();
            edges[fromNode].Add (toNode);
        }

        internal int Count()
        {
            throw new NotImplementedException();
        }
    }
}