using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using YaccLexCS.code.structure;
using YaccLexCS.ycomplier.code;

namespace YaccLexCS.runtime.types
{
    //a structure lazy function call
    public class FunctionThunk : IInvokable, IEquatable<FunctionThunk>
    {
        public readonly string FunctionName;
        public readonly List<string> ParamsPlaceHold;
        public readonly ASTNode Ast;
        public int ParamsCount => ParamsPlaceHold.Count;

        public FunctionThunk(string functionName, List<string> paramsPlaceHold, ASTNode ast)
        {
            FunctionName = functionName;
            ParamsPlaceHold = paramsPlaceHold;
            Ast = ast;
        }

        public bool Equals(FunctionThunk? other)
        {
            return other != null && ParamsCount == other.ParamsCount && FunctionName == other.FunctionName;
        }
        public override int GetHashCode()
        {
            return FunctionName.GetHashCode() + ParamsCount;
        }

        public dynamic Eval(RuntimeContext context, List<dynamic> paramsVal)
        {
            var frame = context.CreateNewStackFrame();
            frame.SetLocalVar(FunctionName, this);
            for(var i = 0; i < ParamsCount && i < paramsVal.Count; i++)
            {
                frame.SetLocalVarLexical(0, i, paramsVal[i]);
            }
            dynamic v = null;
            if(Ast[0] is BlockNode)
            {
                v = Ast[0].Eval(context);
            }
            else
            {
                v = Ast.Eval(context);
            }
            context.PopStackFrame();
            return v;

        }
    }
}
