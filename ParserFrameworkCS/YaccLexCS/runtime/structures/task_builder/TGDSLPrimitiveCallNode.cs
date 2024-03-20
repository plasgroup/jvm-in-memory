namespace YaccLexCS.runtime.structures.task_builder
{
    internal class TGDSLPrimitiveCallNode : TaskGraphVertex
    {
        private string functionName;
        private TaskRegister[] registers;

        public TGDSLPrimitiveCallNode(string functionName, TaskRegister resultReg, params TaskRegister[] registers) : base("call_task")
        {
            this.functionName = functionName;
            this.registers = registers;
        }
    }
}