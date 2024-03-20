namespace YaccLexCS.runtime.structures.task_builder
{
    internal class TGSetNode : TaskGraphVertex
    {
        private TaskRegister var_reg;
        private TaskRegister reg;

        public TGSetNode(TaskRegister var_reg, TaskRegister reg) : base("set")
        {
            this.var_reg = var_reg;
            this.reg = reg;
        }
    }
}