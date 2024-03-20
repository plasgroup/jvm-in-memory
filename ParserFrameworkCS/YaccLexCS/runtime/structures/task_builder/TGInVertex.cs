namespace YaccLexCS.runtime.structures.task_builder
{
    internal class TGInVertex : TaskGraphVertex
    {
        private TaskRegister[] registers;

        public TGInVertex(params TaskRegister[] registers) : base("in")
        {
            this.registers = registers;
        }
    }
}