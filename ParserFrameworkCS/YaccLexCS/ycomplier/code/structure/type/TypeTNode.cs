
using System.Collections.Generic;
using YaccLexCS.ycomplier;
using YaccLexCS.ycomplier.attribution;
using YaccLexCS.ycomplier.code;
using YaccLexCS.ycomplier.code.structure;
using YaccLexCS.runtime;
using System.Xml.Linq;

namespace YaccLexCS.code.structure
{
    [GrammarConfiguration]
    public class TypetNode : ASTNonTerminalNode
    {
        public override dynamic Eval(RuntimeContext context)
        {
            return EvaluationConfiguration.ClassNameMapping[GetType().Name].Invoke(null, new object[]{this, context});
        }

        internal string GetTypeString()
        {
            /*host_type*/
            if (this.Count() == 1
                 && this[0].GetType().IsAssignableFrom(typeof(HostTypeNode)))
            {
                return (this[0] as HostTypeNode).GetTypeString();
            }
            /*dsl_type*/
            if (this.Count() == 1
                 && this[0].GetType().IsAssignableFrom(typeof(DslTypeNode)))
            {
                return (this[0] as DslTypeNode).GetTypeString();
            }
            return "";
        }

        public TypetNode(IEnumerable<ASTNode> child) : base(child, "typeT")
        {
        }
    }
}