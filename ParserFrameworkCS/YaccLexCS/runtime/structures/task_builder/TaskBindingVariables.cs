using System.ComponentModel.DataAnnotations;
using System.Text;

namespace YaccLexCS.runtime.structures.task_builder
{
    internal class TaskBindingVariables : TaskComponent

    {
        public TaskBindingVariables() : base("bind")
        {

        }

        public override string ToGraphDSLString()
        {
            var sb = new StringBuilder();
            sb.Append(".bind{\r\n");
            foreach(TaskComponent component in items)
            {
                sb.Append(component.ToGraphDSLString() + "\r\n");
            }
            sb.Append("}\r\n");
            return sb.ToString();
        }
    }
}