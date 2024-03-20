using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using YaccLexCS.runtime;

namespace YaccLexCS.ycomplier.code
{
    public abstract class ASTNode : IEnumerable<ASTNode>
    {
        public readonly string NodeName;

        public dynamic Val { get; private set; }
        protected ASTNode(string nodeName)
        {
            NodeName = nodeName;
        }

        public abstract dynamic Eval(RuntimeContext context);
        
        public abstract ASTNode? Child(int i);
        
        public abstract IEnumerable<ASTNode> Children();
        public abstract string Location();
        
        
        public int ChildrenCount => Children().Count();
        

        public bool IsLeaf => ChildrenCount == 0;
       
        public IEnumerator<ASTNode> GetEnumerator()
        {
            return Children().GetEnumerator();
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            return GetEnumerator();
        }

        public ASTNode? this[int i] => Child(i);

        public override string ToString()
        {
            return $"({NodeName}:{ChildrenCount})";
        }

        public abstract string GetSourceText();
        public string GetTreeShapeDescribe()
        {
            StringBuilder r = new();
            Dfs(this, 0);
            void Dfs(ASTNode node, int dept)
            {
                r.Append("+ ".PadRight((dept) * 2, '-'));
                r.Append($"({dept})" + node.NodeName + "\r\n");
                if (node.IsLeaf) return;
                foreach (var c in node.Children())
                {
                    Dfs(c, dept + 1);
                }
            }

            return r.ToString();
        }
    }
}