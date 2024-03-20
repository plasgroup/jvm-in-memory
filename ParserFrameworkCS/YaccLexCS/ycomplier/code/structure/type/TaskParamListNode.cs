
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
    public class TaskParamListNode : ASTNonTerminalNode
    {
        public override dynamic Eval(RuntimeContext context)
        {
            return EvaluationConfiguration.ClassNameMapping[GetType().Name].Invoke(null, new object[]{this, context});
        }

        internal void EvalForGetBindingVariables(List<BindVariablePrototype> variables)
        {
            /*task_param_list COMMA task_param*/
            if (this.Count() == 3
                 && this[0].GetType().IsAssignableFrom(typeof(TaskParamListNode))
                 && this[1].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && this[2].GetType().IsAssignableFrom(typeof(TaskParamNode)))
            {
                (this[0] as TaskParamListNode).EvalForGetBindingVariables(variables);
                variables.Add(new BindVariablePrototype()
                {
                    type = ((this[2] as TaskParamNode)[0] as TypetNode),
                    name = ((this[2] as TaskParamNode)[1] as ASTTerminalNode)
                });
            }
            /*task_param*/
            if (this.Count() == 1
                 && this[0].GetType().IsAssignableFrom(typeof(TaskParamNode)))
            {
                variables.Add(new BindVariablePrototype()
                {
                    type = ((this[0] as TaskParamNode)[0] as TypetNode),
                    name = ((this[0] as TaskParamNode)[1] as ASTTerminalNode)
                });;
            }
        }

        public TaskParamListNode(IEnumerable<ASTNode> child) : base(child, "task_param_list")
        {
        }
    }
}