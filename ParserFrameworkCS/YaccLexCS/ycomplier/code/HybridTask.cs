using System.Text;
using YaccLexCS.runtime.structures.task_builder;
namespace YaccLexCS.ycomplier.code.structure
{
    public class HybridTask : TaskComponent
    {
        TaskBindingVariables bindingVariables = new TaskBindingVariables();
        TaskDependencies dependencies = new TaskDependencies();
        Dictionary<TaskRegisterKind, int> allocatedRegisterCount = 
            new Dictionary<TaskRegisterKind, int>();

        TaskGraph taskGraph = new TaskGraph();
        public string taskName = "";

        public Dictionary<string, TaskRegister> nameMap = new Dictionary<string, TaskRegister>();

        void ResetAllocatedRegisters()
        {
            foreach(var x in Enum.GetValues(typeof(TaskRegisterKind)).Cast<TaskRegisterKind>())
            {
                allocatedRegisterCount[x] = 0;
            }
        }
        public int GetAllocatedRegisterCount(TaskRegisterKind registerKind)
            => allocatedRegisterCount[registerKind];
        
        public HybridTask() : base("task")
        {
            ResetAllocatedRegisters();
            components.Add("bind", bindingVariables);
            components.Add("task_graph", taskGraph);
            components.Add("dependencies", dependencies);
        }

        public void AppendBindVariable(TaskVariableBindType bindType, string data_type, string name)
        {
            TaskRegister register = 
                this.AllocateNewRegister(TaskRegisterKind.V);
            this.nameMap.Add(name, register);
            this["bind"].
                Append(new TaskBindItem(bindType, data_type, name, register));
        }

        Dictionary<string, TaskComponent> components = new ();
        public TaskComponent this[string name]
        {
            get => components[name];
            set => components[name] = value;
        }

        public TaskRegister AllocateNewRegister(TaskRegisterKind kind)
        {
            return new (kind, allocatedRegisterCount[kind]++);
        }
        public override string ToGraphDSLString()
        {
            var sb = new StringBuilder();
            sb.Append(".task::" + taskName + "{\r\n");
            foreach ((string name, TaskComponent taskComponent) in components)
            {
                sb.Append("\r\n");
                sb.Append(taskComponent.ToGraphDSLString());
                sb.Append("\r\n");
            }
            sb.Append("}");
            return sb.ToString();
        }
    }
}