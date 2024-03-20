using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using QuickGraph;
using QuickGraph.Graphviz;
using QuickGraph.Graphviz.Dot;
using YaccLexCS.ycomplier.code;


namespace YaccLexCS.utils
{
    public sealed class FileDotEngine : IDotEngine
    {
        public string Run(GraphvizImageType imageType, string dot, string outputFileName)
        {
            string output = outputFileName;
            File.WriteAllText(output, dot);

            // assumes dot.exe is on the path:
            var args = string.Format(@"{0} -Tjpg -O", output);
            System.Diagnostics.Process.Start("dot.exe", args);
            return output;
        }
    }
    internal class GraphUtils
    {
        public class ASTNodeProxy
        {
            public ASTNode node;
            public ASTNodeProxy(ASTNode node)
            {
                this.node = node;
            }
            public override string ToString()
            {
                return node.ToString() + (node.IsLeaf ? "[" + ((ASTTerminalNode)node).Token + "]" : "");
            }

            
        }

        public class AstGraph: AdjacencyGraph<ASTNodeProxy, ASTEdge>
        {

        }
        public class ASTEdge : IEdge<ASTNodeProxy>
        {
            public ASTEdge(string n, ASTNodeProxy s, ASTNodeProxy t)
            {
                Name = n;
                Source = s;
                Target = t;
            }
            public string Name { get; set; }
            public ASTNodeProxy Source { get; set; }
            public ASTNodeProxy Target { get; set; }

        }
        public static void DrawAST(string filePath, ASTNode root)
        {
            int globalCounter = 0;
            AstGraph graph = new AstGraph();
            

            void DFSConvertGraph(ASTNodeProxy from, ASTNodeProxy to, int id_from)
            {
                if (to == null) return;
                // string toVertexString = GenerateGraphVertex(to);
                graph.AddVertex(to);
                int id_to = globalCounter++;
                graph.AddEdge(new ASTEdge($"{id_from}:{id_to}", from, to));
                foreach (ASTNode childen in to.node.Children())
                {
                    DFSConvertGraph(to, new ASTNodeProxy(childen), id_to);
                }
            }

            // string rootVertexString = GenerateGraphVertex(root);
            var rootProxy = new ASTNodeProxy(root);
            graph.AddVertex(rootProxy);
            globalCounter++;

            foreach(ASTNode node in root.Children()){
                DFSConvertGraph(rootProxy, new ASTNodeProxy(node), 0);
            }
            GraphvizAlgorithm<ASTNodeProxy, ASTEdge> graphvizAlgorithm = new GraphvizAlgorithm<ASTNodeProxy, ASTEdge>(graph);
            graphvizAlgorithm.ImageType = GraphvizImageType.Gif;
            graphvizAlgorithm.FormatVertex += (sender, args) => args.VertexFormatter.Comment = 
                args.Vertex.ToString();
            graphvizAlgorithm.FormatEdge += (sender, args) => { args.EdgeFormatter.Label.Value = args.Edge.Name; };

            var output = graphvizAlgorithm.Generate(new FileDotEngine(), filePath);
            
            return;
        }
    }
}
