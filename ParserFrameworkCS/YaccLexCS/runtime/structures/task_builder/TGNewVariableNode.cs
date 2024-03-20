namespace YaccLexCS.runtime.structures.task_builder
{
    internal class TGNewVariableNode : TaskGraphVertex
    {
        private string getSourceText;
        private TaskRegister taskRegister;

        public TGNewVariableNode(string getSourceText, TaskRegister taskRegister) : base("new_var")
        {
            this.getSourceText = getSourceText;
            this.taskRegister = taskRegister;
        }
    }
}