using Microsoft.Win32;
using System.ComponentModel.DataAnnotations;
using System.Text;
using YaccLexCS.ycomplier.code.structure;

namespace YaccLexCS.runtime.structures.task_builder
{
    internal class TaskGraph : TaskComponent
    {
        TaskGraphVertexes _vertexes = new TaskGraphVertexes();
        TaskGraphEdges _edges = new TaskGraphEdges();

        public TaskGraph() : base("task_graph")
        {
        
        }

        public int VertexCount => _vertexes.Count();
        public int EdgesCount => _edges.Count();

        public void AddVertex(TaskGraphVertex vertex)
        {
            _vertexes.AddVertex(vertex);
        }

        public void AddEdge(int fromNode, int toNode)
        {
            _edges.AddEdge(fromNode, toNode);
        }
        public void AddEdge(int fromNode, int[] toNode)
        {
            for(int i = 0; i < toNode.Length; i++)
            {
                _edges.AddEdge(fromNode, toNode[i]);
            }
        }

        public override string ToGraphDSLString()
        {
            var sb = new StringBuilder();
            sb.Append(".task_graph{\r\n");
            sb.Append(".vertex{\r\n");
            foreach(string str in _vertexes.Select((vertex, index) =>
                $"{index} {vertex.VertexName}"))
            {
                sb.Append(str + "\r\n");
            }
            sb.Append("}");
            sb.Append("}");

            sb.Append("\r\n");
            return sb.ToString();
        }
    }
}