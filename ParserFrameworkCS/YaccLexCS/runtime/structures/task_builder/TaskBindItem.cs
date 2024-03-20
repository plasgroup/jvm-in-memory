using System.Text;
using YaccLexCS.ycomplier.code.structure;

namespace YaccLexCS.runtime.structures.task_builder
{
    internal class TaskBindItem : PrimitiveTaskComponent
    {
        public TaskVariableBindType bindType { get; private set; }
        public string data_type { get; private set; }
        public string name { get; private set; }
        public TaskRegister register { get; private set; }

        public TaskBindItem(TaskVariableBindType bindType, string data_type, string name, TaskRegister register):base("bind_variable_item")
        {
            this.bindType = bindType;
            this.data_type = data_type;
            this.name = name;
            this.register = register;
        }

        public override string ToGraphDSLString()
        {
            var sb = new StringBuilder();
            sb.Append($"[{bindType switch { TaskVariableBindType.IN => "IN", TaskVariableBindType.OUT => "OUT" } }] ");
            sb.Append(data_type + " ");
            sb.Append(!"".Equals(name) ? ("'" + name + "' ") : "");
            if (register.kind == TaskRegisterKind.V)
            {
                sb.Append(
                     $"{register.kind switch { TaskRegisterKind.V => 'v' }}"
                    + "/" + register.number
                    );
            }
        
            return sb.ToString();
        }
    }
}