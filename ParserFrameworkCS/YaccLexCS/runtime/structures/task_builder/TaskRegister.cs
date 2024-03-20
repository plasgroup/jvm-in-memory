namespace YaccLexCS.runtime.structures.task_builder
{
    public class TaskRegister
    {
        public TaskRegisterKind kind { get; private set; }
        public int number { get; private set; }

        public TaskRegister(TaskRegisterKind kind, int number)
        {
            this.kind = kind;
            this.number = number;
        }
    }
}