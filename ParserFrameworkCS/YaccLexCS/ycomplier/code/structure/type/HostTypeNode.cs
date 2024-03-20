
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
    public class HostTypeNode : ASTNonTerminalNode
    {
        public override dynamic Eval(RuntimeContext context)
        {
            return EvaluationConfiguration.ClassNameMapping[GetType().Name].Invoke(null, new object[]{this, context});
        }

        internal string GetTypeString()
        {
            if (this.Count() == 1
                 && this[0].GetType().IsAssignableFrom(typeof(ASTTerminalNode)))
            {
                return (this[0] as ASTTerminalNode).Token.SourceText;
            }
            return "";
        }

        public HostTypeNode(IEnumerable<ASTNode> child) : base(child, "host_type")
        {
        }
    }
}