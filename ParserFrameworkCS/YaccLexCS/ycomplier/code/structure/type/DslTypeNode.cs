
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
    public class DslTypeNode : ASTNonTerminalNode
    {
        public override dynamic Eval(RuntimeContext context)
        {
            return EvaluationConfiguration.ClassNameMapping[GetType().Name].Invoke(null, new object[]{this, context});
        }

        internal string GetTypeString()
        {
            /*DSL_ACCESSOR IDENTIFIER*/
            if (this.Count() == 2
                 && this[0].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && this[1].GetType().IsAssignableFrom(typeof(ASTTerminalNode)))
            {
                return (this[1] as ASTTerminalNode).Token.SourceText;
            }
            return "";
        }

        public DslTypeNode(IEnumerable<ASTNode> child) : base(child, "dsl_type")
        {
        }
    }
}