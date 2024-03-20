namespace YaccLexCS.runtime.structures.task_builder
{
    internal abstract class PrimitiveTaskComponent : TaskComponent
    {
        public PrimitiveTaskComponent(string name) : base(name)
        {
        }

        public abstract override string ToGraphDSLString();
    }
}