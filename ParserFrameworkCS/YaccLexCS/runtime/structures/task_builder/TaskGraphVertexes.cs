
using System.Collections;

namespace YaccLexCS.runtime.structures.task_builder
{
    internal class TaskGraphVertexes : IEnumerable<TaskGraphVertex>
    {
        List<TaskGraphVertex> vertexes = new List<TaskGraphVertex>();
        public TaskGraphVertexes() { 
        
        }


        public void AddVertex(TaskGraphVertex node)
        {
            vertexes.Add(node);
        }

        public IEnumerator<TaskGraphVertex> GetEnumerator()
        {
            return vertexes.GetEnumerator();
        }

        internal int Count()
        {
            return vertexes.Count;
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            return GetEnumerator();
        }
    }
}