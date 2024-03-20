
using System.Collections.Generic;
using YaccLexCS.ycomplier;
using YaccLexCS.ycomplier.attribution;
using YaccLexCS.ycomplier.code;
using YaccLexCS.ycomplier.code.structure;
using YaccLexCS.runtime;
namespace YaccLexCS.code.structure
{
    [GrammarConfiguration]
    public class CallParamNode : ASTNonTerminalNode
    {
        public override dynamic Eval(RuntimeContext context)
        {
            return EvaluationConfiguration.ClassNameMapping[GetType().Name].Invoke(null, new object[]{this, context});
        }
        public CallParamNode(IEnumerable<ASTNode> child) : base(child, "call_param")
        {
        }
    }
}