namespace YaccLexCS.runtime.structures.task_builder
{
    internal class TGOutNode : TaskGraphVertex
    {
        private TaskRegister reg;

        public TGOutNode(TaskRegister reg) : base("out")
        {
            this.reg = reg;
        }
    }
}