namespace YaccLexCS.runtime.structures.task_builder
{
    public abstract class TaskGraphVertex
    {
        public string VertexName = "";
        public TaskGraphVertex(string vertexName)
        {
            this.VertexName = vertexName;
        }
    }
}