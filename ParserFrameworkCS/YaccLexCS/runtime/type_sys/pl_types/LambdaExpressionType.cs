using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using YaccLexCS.code.structure;

namespace YaccLexCS.runtime.types
{
    public class LambdaExpressionType : IInvokable
    {
        private readonly List<string> _placeHold;
        private readonly StatementNode _body;

        public LambdaExpressionType(List<string> placeHold, StatementNode body)
        {
            this._placeHold = placeHold;
            this._body = body;
        }

        public dynamic Eval(List<object> inputs, RuntimeContext rc)
        {
            _placeHold.Select((x, i) => (x, i)).ToDictionary(k =>k.x, v => v.i);
            rc.GetCurrentCommonFrame().CreateNewStorageBlockForNewCodeBlock();
            for(var i = 0; i < _placeHold.Count && i < inputs.Count; i++)
            {
                rc.GetCurrentCommonFrame().SetLocalVarLexical(0, i, inputs[i]);
            }
            dynamic r = null;
            if(_body[0] is BlockNode)
            {
                r = _body[0].Eval(rc);
            }
            else
            {
                r = _body.Eval(rc);
            }
            
            rc.GetCurrentCommonFrame().RemoveNewestStorageBlock();
            return r;
        }
    }
}
