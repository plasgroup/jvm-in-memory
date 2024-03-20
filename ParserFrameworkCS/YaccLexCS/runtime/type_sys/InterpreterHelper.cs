using YaccLexCS.code.structure;
using YaccLexCS.runtime.types;
using YaccLexCS.ycomplier;
using YaccLexCS.ycomplier.code;

namespace YaccLexCS.runtime
{

    public static class InterpreterHelper
    {
		/*public static Dictionary<string, object> EntreNewBlock(RuntimeContext context)
        {
			return context.GetCurrentCommonFrame().CreateNewStorageBlockForNewCodeBlock();
        }
		public static void LeaveBlock(RuntimeContext context)
        {
			context.GetCurrentCommonFrame().RemoveNewestStorageBlock();
        }
		
		public static dynamic BasicTypesValueExtract(ASTTerminalNode terminalNode, RuntimeContext context)
        {
            switch (terminalNode.Token.Type)
            {
				case "ID":
				{
					var t = terminalNode.Token;
					var v = context.GetCurrentCommonFrame().GetLocalVarLexical(t.LexicalDistance.depth, t.LexicalDistance.order);
					
					return v;
				}
				case "STRING":
					return terminalNode.Token.SourceText;
				case "DOUBLE_LITERAL":
					return double.Parse(terminalNode.Token.SourceText);
				case "TRUE_T":
					return 1;
				case "FALSE_T":
					return 0;
				case "BREAK":
					return SpecialValue.BREAK;
				case "CONTINUE":
					return SpecialValue.CONTINUE;
			}
			return null;
        }

        public static ASTNode ToLexivalRepresentAst(ASTNode root)
        {
            var waitList = new Dictionary<string, List<(Token token, int waitDepth)>>(); //key: a global definition name be waittd. value: correspondance token

            void NotifyGlobalDefinitionForWaitList(string defineID, int order)
            {
                $"notify definition of {defineID}".PrintToConsole();
                if (waitList.ContainsKey(defineID))
                {
                    foreach (var t in waitList[defineID])
                    {
                        var dis = (t.waitDepth - 1, order);
                        $"****** change {t}'s lexical represent = {dis}".PrintToConsole();
                        t.token.LexicalDistance = dis;
                    }
                    waitList.Remove(defineID);
                }
                else
                {
                    $"****** No tokens are waiting for this id".PrintToConsole();
                }
            }

            void dfs(ASTNode node, Stack<Dictionary<string, (int depth, int order)>> s, int d)
            {
                (int depth, int order) TrackBack(Token t)
                {
                    var find = 0;
                    var id = t.SourceText;
                    foreach (var sFrame in s)
                    {
                        if (sFrame.ContainsKey(id))
                        {
                            var res = (find, sFrame[id].order);

                            //$">>>represent {id} with {res}\r\n".PrintToConsole();
                            t.LexicalDistance = res;
                            return (find, sFrame[id].order);
                        }
                        find++;
                    }
                    //$"not found {id}, it may in global area, put into wait list".PrintToConsole();
                    if (!waitList.ContainsKey(id))
                    {
                        waitList[id] = new List<(Token token, int waitDepth)>();
                    }
                    waitList[id].Add((t, s.Count));
                    t.LexicalDistance = (-1, -1);
                    return (-1, -1);
                }
                List<string> DfsGetParamsList(ASTNode pNode, List<string> ls = null)
                {
                    ls ??= new();
                    if (pNode is TypelessParamNode typelessParamNode)
                    {
                        ls.Add(((ASTTerminalNode)typelessParamNode[0]!).Token.SourceText);
                    }
                    else if (pNode is TypedParamNode typedParamNode)
                    {
                        ls.Add(((ASTTerminalNode)typedParamNode[1]!).Token.SourceText);
                    }
                    else
                    {
                        foreach (var c in pNode)
                            DfsGetParamsList(c, ls);
                    }
                    return ls;
                }
                if (node.NodeName == "compile_unit")
                {
                    var cNode = node as CompileUnitNode;
                    if (cNode.Count() == 1)
                        dfs(cNode[0]!, s, d);
                    else
                    {
                        var l = node[0];
                        var r = node[1]![0]; // hope exposure definition in advance.
                        if (r is DefinitionNode)
                        {
                            //global definition
                            dfs(r, s, d);

                            dfs(l, s, d);
                        }
                        else
                        {
                            dfs(l, s, d);
                            dfs(r, s, d);
                        }
                    }
                }
                else if (node.NodeName == "assign_expression")
                {
                    var tNode = node as AssignExpressionNode;
                    if (tNode.Count() == 3)
                    {
                        var token = (tNode[0] as ASTTerminalNode).Token;
                        //$"ID in left Assignment {token}".PrintToConsole();
                        TrackBack(token);
                        dfs(tNode[2], s, d);
                    }
                    else
                    {
                        dfs(tNode[0], s, d);
                    }

                }
                else if (node.NodeName == "define_var_expression")
                {
                    var tNode2 = node as DefineVarExpressionNode;
                    var token = (tNode2[1] as ASTTerminalNode).Token;
                    *//*  $"ID in Definition {token}".PrintToConsole();
                    $"define {token.SourceText} in depth {d}".PrintToConsole();*//*
                    var top = s.Peek();
                    var l = top.Count;
                    top[token.SourceText] = (0, l);
                    token.LexicalDistance = (0, l);
                    //$">>>represent {token} with {top[token.SourceText]}\r\n".PrintToConsole();
                    if (s.Count == 1)
                        NotifyGlobalDefinitionForWaitList(token.SourceText, l);
                    dfs(tNode2.Last(), s, d);
                }
                else if (node.NodeName == "primary_expression")
                {
                    var tNode3 = node as PrimaryExpressionNode;
                    if ((tNode3[0] as ASTTerminalNode).Token.Type == "ID")
                    {
                        var token = (tNode3[0] as ASTTerminalNode).Token;
                       // $"ID in read {token}".PrintToConsole();
                        TrackBack(token);
                    }

                }
                else if (node.NodeName == "block")
                {
                    var tNode4 = node as BlockNode;
                    // "!!depth++".PrintToConsole();
                    s.Push(new Dictionary<string, (int depth, int order)>());
                    foreach (var c in tNode4)
                    {
                        dfs(c, s, d + 1);
                    }
                    s.Pop();
                    //"depth--".PrintToConsole();
                }
                else if (node.NodeName == "for_statement")
                {
                    var tNode5 = node as ForStatementNode;
                    //"!!depth++ in for".PrintToConsole();
                    s.Push(new Dictionary<string, (int depth, int order)>());
                    foreach (var c in tNode5.SkipLast(1))
                    {
                        dfs(c, s, d + 1);
                    }
                    if (tNode5.Last()[0] is BlockNode bNode)
                    {
                        foreach (var c in bNode) dfs(c, s, d + 1);
                    }
                    s.Pop();
                    //"depth--".PrintToConsole();
                }
                else if (node.NodeName == "access_expression")
                {
                    var node6 = node as AccessExpressionNode;

                    switch (node6.Count())
                    {
                        case 4:
                            {
                                // $"call".PrintToConsole();
                                TrackBack((node6[0] as ASTTerminalNode).Token);
                                foreach (var c in node6.Skip(1))
                                {
                                    dfs(c, s, d);
                                }

                                break;
                            }

                        case 3:
                            if (node6[1] is ExpressionNode)
                                dfs(node6[1], s, d);
                            else if (node6[0] is ASTTerminalNode)
                            {
                                TrackBack((node6[0] as ASTTerminalNode).Token);
                            }
                            break;
                        case 1:
                            dfs(node6[0], s, d);
                            break;
                    }
                }
                else if (node.NodeName == "function_definition")
                {
                    var node7 = node as FunctionDefinitionNode;
                   // $"new function".PrintToConsole();
                    var t = (node7[1] as ASTTerminalNode).Token;
                    var id = t.SourceText;
                   // $"ID in Definition {id}".PrintToConsole();
                    //$"define {id} in depth {d}".PrintToConsole();
                    t.LexicalDistance = (0, s.Peek().Count());
                    if (s.Count == 1)
                        NotifyGlobalDefinitionForWaitList(id, t.LexicalDistance.order);
                    s.Peek()[id] = (0, s.Peek().Count());
                    var ls = DfsGetParamsList(node7[3]);
                    s.Push(new());
                    foreach (var p in ls)
                    {
                        var r = (0, s.Peek().Count());
                     //   $"define param {p} in {r}".PrintToConsole();
                        s.Peek()[p] = r;
                    }
                    foreach (var c in node7.Last())
                    {
                        dfs(c, s, d);
                    }
                    s.Pop();
                }
                else if (node.NodeName == "lambda_expression")
                {
                    var node8 = node as LambdaExpressionNode;
                    var ls = DfsGetParamsList(node8[2]);
                    s.Push(new());
                    foreach (var p in ls)
                    {
                        var r = (0, s.Peek().Count());
                     //   $"define param {p} in {r}".PrintToConsole();
                        s.Peek()[p] = r;
                    }
                    foreach (var c in node8.Last())
                    {
                        if (c is BlockNode block)
                        {
                            foreach (var c2 in c)
                                dfs(c2, s, d);
                        }
                        else
                        {
                            dfs(c, s, d);
                        }
                    }
                    s.Pop();
                }
                else
                {
                    foreach (var c in node)
                    {
                        dfs(c, s, d);
                    }
                }
            }
            var stack = new Stack<Dictionary<string, (int, int)>>();
            stack.Push(new());
            dfs(root, stack, 0);
            $"=================convert lexical representation finsh=====================".PrintToConsole();
            return root;
        }*/
    }
}