using YaccLexCS.runtime.structures.task_builder;

namespace YaccLexCS.ycomplier.code.structure
{
    public class EmbededFunction : TaskComponent
    {
        public EmbededFunction(string name) : base(name)
        {

        }

        public override string ToGraphDSLString()
        {
            return $".{name}" + "{\r\n"
                + "/* TODO: bytecode */"
                + "}\r\n";
        }
    }
}