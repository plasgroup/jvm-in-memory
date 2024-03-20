using System.Collections;
using System.Text;

namespace YaccLexCS.runtime.structures.task_builder
{
    public abstract class TaskComponent : IEnumerable<TaskComponent>
    {
        protected string name;
        protected List<TaskComponent> items = new List<TaskComponent>();
        public TaskComponent(string name)
        {
            this.name = name;
        }

        public void Append(TaskComponent item) { 
            items.Add(item);
        }

        public IEnumerator<TaskComponent> GetEnumerator()
        {
            return items.GetEnumerator();
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            return items.GetEnumerator ();
        }


        public abstract string ToGraphDSLString();
    }
}