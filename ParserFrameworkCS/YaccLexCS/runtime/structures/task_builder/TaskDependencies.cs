namespace YaccLexCS.runtime.structures.task_builder
{
    internal class TaskDependencies : TaskComponent
    {
        public TaskDependencies() : base("dependencies")
        {
        }
        public override string ToGraphDSLString()
        {

            return
                ".dependencies{\r\n" +
                items.Select(item => item.ToGraphDSLString())
                    .Aggregate(string.Empty, (a, b) => a + "\r\n" + b)
               + "}\r\n";
        }

    }
}