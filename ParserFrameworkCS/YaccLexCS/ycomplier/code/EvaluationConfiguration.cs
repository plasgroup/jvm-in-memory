
using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Reflection;
using System.Security.Cryptography.X509Certificates;
using YaccLexCS.code.structure;
using YaccLexCS.runtime;
using YaccLexCS.runtime.structures.task_builder;
namespace YaccLexCS.ycomplier.code.structure
{
    public static class EvaluationConfiguration
    {
        public static readonly Dictionary<string, MethodInfo> ClassNameMapping
            = new Lazy<Dictionary<string, MethodInfo>>(() =>
            {
                 var methods = typeof(EvaluationConfiguration).GetMethods();
                 var result = methods
                 .Where(m => m.IsStatic)
                 .ToDictionary(kv => kv.Name, kv => kv);
                return result;
            }).Value;

        public static dynamic CompilationUnitNode(CompilationUnitNode node, RuntimeContext context)
        {
            /*compilation_unit definition_or_comment*/
            if(node.Count() == 2 
                 && node[0].GetType().IsAssignableFrom(typeof(CompilationUnitNode))
                 && node[1].GetType().IsAssignableFrom(typeof(DefinitionOrCommentNode)))
            {
                node[0].Eval(context);
                return node[1].Eval(context);
            }

            /* definition_or_comment */
            if(node.Count() == 1 
                 && node[0].GetType().IsAssignableFrom(typeof(DefinitionOrCommentNode)))
            {
                return node[0].Eval(context);
            }
            return null;
        }

        public static dynamic DefinitionOrCommentNode(DefinitionOrCommentNode node, RuntimeContext context)
        {
            /* task_definition_statement */
            if(node.Count() == 1 
                 && node[0].GetType().IsAssignableFrom(typeof(TaskDefinitionStatementNode)))
            {
                return node[0].Eval(context);
            }
            
            /* comment */
            if(node.Count() == 1 
                 && node[0].GetType().IsAssignableFrom(typeof(CommentNode)))
            {
                return null;
            }
            return null;
        }

        public static dynamic CommentNode(CommentNode node, RuntimeContext context)
        {
            /*SINGLE_LINE_COMMENT*/
            if(node.Count() == 1 
                 && node[0].GetType().IsAssignableFrom(typeof(ASTTerminalNode)))
            {
                return null;
            }
            return null;
        }

        public static dynamic TaskDefinitionStatementNode(TaskDefinitionStatementNode node, RuntimeContext context)
        {
            /* DEF_TASK IDENTIFIER LP task_param_list RP RIGHT_ARROW typeT block */
            if(node.Count() == 8 
                 && node[0].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[1].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[2].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[3].GetType().IsAssignableFrom(typeof(TaskParamListNode))
                 && node[4].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[5].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[6].GetType().IsAssignableFrom(typeof(TypetNode))
                 && node[7].GetType().IsAssignableFrom(typeof(BlockNode)))
            {
                List<BindVariablePrototype> variables = new List<BindVariablePrototype>();
                (node[3] as TaskParamListNode).EvalForGetBindingVariables(variables);
                HybridTask task = 
                    BuildNamingTask(context, node[1].GetSourceText(), node[7] as BlockNode, variables, node[6] as TypetNode);
                return task;
            }
            return null;
        }

        private static HybridTask BuildNamingTask(RuntimeContext rc, String taskName, BlockNode? blockNode, List<BindVariablePrototype> variables, TypetNode? returnType)
        {
            HybridTask task = new HybridTask() { taskName = taskName };
            TaskGraph tg = (TaskGraph) task["task_graph"];
            void buildBindArea()
            {
                for (int i = 0; i < variables.Count; i++)
                {
                    task.AppendBindVariable(TaskVariableBindType.IN,
                        variables[i].type.GetSourceText(),
                        variables[i].name.GetSourceText()
                        );
                }
                task.AppendBindVariable(TaskVariableBindType.OUT,
                    returnType.GetSourceText(), "");
            }

            void buildTaskGraph(ASTNode node)
            {
                rc["state_stack"] = new Stack<string>();
                rc["state"] = "building_task";
                (rc["state_stack"] as Stack<string>).Push("building_task");
                rc["eval_node"] = node;
                rc["task_stack"] = new Stack<HybridTask>();
                (rc["task_stack"] as Stack<HybridTask>).Push(task);
                tg.AddVertex(new TGInVertex((task["bind"] as TaskBindingVariables).Select(e => (e as TaskBindItem).register).ToArray()));

                node.Eval(rc);
                

                rc["state"] = "";
                rc["eval_node"] = null;
                (rc["state_stack"] as Stack<string>).Pop();

            }

            buildBindArea();
            buildTaskGraph(blockNode);
            task.ToGraphDSLString().PrintToConsole();
            return task;
        }

        public static dynamic TaskParamListNode(TaskParamListNode node, RuntimeContext context)
        {
            throw new NotImplementedException();
            /*task_param_list COMMA task_param*/
            if(node.Count() == 3 
                 && node[0].GetType().IsAssignableFrom(typeof(TaskParamListNode))
                 && node[1].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[2].GetType().IsAssignableFrom(typeof(TaskParamNode)))
            {
                return null;
            }
            /*task_param*/
            if(node.Count() == 1 
                 && node[0].GetType().IsAssignableFrom(typeof(TaskParamNode)))
            {
                return null;
            }
        }

        public static dynamic TaskParamNode(TaskParamNode node, RuntimeContext context)
        {
            throw new NotImplementedException();
            /*typeT IDENTIFIER*/
            if(node.Count() == 2 
                 && node[0].GetType().IsAssignableFrom(typeof(TypetNode))
                 && node[1].GetType().IsAssignableFrom(typeof(ASTTerminalNode)))
            {
                return null;
            }
        }
        
        public static dynamic TypetNode(TypetNode node, RuntimeContext context)
        {
            throw new NotImplementedException();
            /*host_type*/
            if(node.Count() == 1 
                 && node[0].GetType().IsAssignableFrom(typeof(HostTypeNode)))
            {
                return null;
            }
            /*dsl_type*/
            if(node.Count() == 1 
                 && node[0].GetType().IsAssignableFrom(typeof(DslTypeNode)))
            {
                return null;
            }
        }

        public static dynamic HostTypeNode(HostTypeNode node, RuntimeContext context)
        {
            throw new NotImplementedException();
            /*HOST_TYPE*/
            if(node.Count() == 1 
                 && node[0].GetType().IsAssignableFrom(typeof(ASTTerminalNode)))
            {
                return null;
            }
        }

        public static dynamic DslTypeNode(DslTypeNode node, RuntimeContext context)
        {
            throw new NotImplementedException();
            /*DSL_ACCESSOR IDENTIFIER*/
            if(node.Count() == 2 
                 && node[0].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[1].GetType().IsAssignableFrom(typeof(ASTTerminalNode)))
            {
                return null;
            }
        }

        public static dynamic BlockNode(BlockNode node, RuntimeContext context)
        {
            /*LB statements RB*/
            if(node.Count() == 3 
                 && node[0].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[1].GetType().IsAssignableFrom(typeof(StatementsNode))
                 && node[2].GetType().IsAssignableFrom(typeof(ASTTerminalNode)))
            {
                return node[1].Eval(context);
            }

            /*LB RB*/
            if(node.Count() == 2 
                 && node[0].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[1].GetType().IsAssignableFrom(typeof(ASTTerminalNode)))
            {
                return null;
            }
            return null;
        }

        public static dynamic StatementsNode(StatementsNode node, RuntimeContext context)
        {
            /*statements statement*/
            if(node.Count() == 2 
                 && node[0].GetType().IsAssignableFrom(typeof(StatementsNode))
                 && node[1].GetType().IsAssignableFrom(typeof(StatementNode)))
            {
                node[0].Eval(context);
                return node[1].Eval(context);
            }

            if(node.Count() == 1
                && node[0].GetType().IsAssignableFrom(typeof(StatementNode))
                )
            {
                return node[0].Eval(context);
            }
            return null;
        }

        public static dynamic StatementNode(StatementNode node, RuntimeContext context)
        {
            /*expression SEMICOLON*/
            if(node.Count() == 2 
                 && node[0].GetType().IsAssignableFrom(typeof(ExpressionNode))
                 && node[1].GetType().IsAssignableFrom(typeof(ASTTerminalNode)))
            {
                return node[0].Eval(context);
            }
            return null;
        }

        public static dynamic ExpressionNode(ExpressionNode node, RuntimeContext context)
        {
            var state = context["state"] + "";
            
            /*bracket_expression*/
            if(node.Count() == 1 
                 && node[0].GetType().IsAssignableFrom(typeof(BracketExpressionNode)))
            {
                return node[0].Eval(context);
            }

            /*query_expression*/
            if(node.Count() == 1 
                 && node[0].GetType().IsAssignableFrom(typeof(QueryExpressionNode)))
            {
                return node[0].Eval(context);
            }

            /*host_expression*/
            if(node.Count() == 1 
                 && node[0].GetType().IsAssignableFrom(typeof(HostExpressionNode)))
            {
                
                return node[0].Eval(context);
            }

            /*dsl_func_call*/
            if(node.Count() == 1 
                 && node[0].GetType().IsAssignableFrom(typeof(DslFuncCallNode)))
            {
                return node[0].Eval(context);
            }

            /*primitive_expression*/
            if(node.Count() == 1 
                 && node[0].GetType().IsAssignableFrom(typeof(PrimitiveExpressionNode)))
            {
                if ("building_query".Equals(context["state"]))
                {
                    HybridTask task = 
                        (context["task_stack"] as Stack<HybridTask>).Peek();
                    (task["dependencies"] as TaskDependencies)
                        .Append(new EmbededFunction("f%" 
                            + task.AllocateNewRegister(TaskRegisterKind.F)));
                    return task;
                }
                return node[0].Eval(context);
            }

            /*var_definition*/
            if(node.Count() == 1 
                 && node[0].GetType().IsAssignableFrom(typeof(VarDefinitionNode)))
            {
                return node[0].Eval(context);
            }
            return null;
        }


        public static dynamic VarDefinitionNode(VarDefinitionNode node, RuntimeContext context)
        {
            /*typeT IDENTIFIER*/
            if(node.Count() == 2 
                 && node[0].GetType().IsAssignableFrom(typeof(TypetNode))
                 && node[1].GetType().IsAssignableFrom(typeof(ASTTerminalNode)))
            {
                throw new NotImplementedException();
            }

            /*typeT IDENTIFIER var_initializer*/
            if(node.Count() == 3 
                 && node[0].GetType().IsAssignableFrom(typeof(TypetNode))
                 && node[1].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[2].GetType().IsAssignableFrom(typeof(VarInitializerNode)))
            {
                if (context.RuntimeMemoryContains("state"))
                {
                    var task = (context["task_stack"] as Stack<HybridTask>).Peek();
                    var task_graph = task["task_graph"] as TaskGraph;
                    var var_reg = task.AllocateNewRegister(TaskRegisterKind.V);
                    task.nameMap.Add(node[1].GetSourceText(), var_reg);
                    task_graph.AddVertex(new TGNewVariableNode(node[0].GetSourceText(), var_reg));
                    task_graph.AddEdge(task_graph.VertexCount - 1, task_graph.VertexCount);
                    TaskRegister reg = node[2].Eval(context);
                    task_graph.AddVertex(new TGSetNode(var_reg, reg));
                    task_graph.AddEdge(task_graph.VertexCount - 1, task_graph.VertexCount);
                }
                return null;
            }
            throw new NotImplementedException();
        }


        public static dynamic VarInitializerNode(VarInitializerNode node, RuntimeContext context)
        {
            /*ASSIGN expression*/
            if(node.Count() == 2 
                 && node[0].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[1].GetType().IsAssignableFrom(typeof(ExpressionNode)))
            {
                return node[1].Eval(context);
            }
            throw new NotImplementedException();
        }


        public static dynamic BracketExpressionNode(BracketExpressionNode node, RuntimeContext context)
        {
            throw new NotImplementedException();
            /*LP expression RP*/
            if(node.Count() == 3 
                 && node[0].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[1].GetType().IsAssignableFrom(typeof(ExpressionNode))
                 && node[2].GetType().IsAssignableFrom(typeof(ASTTerminalNode)))
            {
                return null;
            }
        }


        public static dynamic HostExpressionNode(HostExpressionNode node, RuntimeContext context)
        {
            throw new NotImplementedException();
            /*HOST_EXPRESSION*/
            if(node.Count() == 1 
                 && node[0].GetType().IsAssignableFrom(typeof(ASTTerminalNode)))
            {
                return null;
            }
        }


        public static dynamic DslFuncCallNode(DslFuncCallNode node, RuntimeContext context)
        {
            /*DSL_ACCESSOR IDENTIFIER LP call_params RP*/
            if(node.Count() == 5 
                 && node[0].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[1].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[2].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[3].GetType().IsAssignableFrom(typeof(CallParamsNode))
                 && node[4].GetType().IsAssignableFrom(typeof(ASTTerminalNode)))
            {
                var task = (context["task_stack"] as Stack<HybridTask>).Peek();
                var task_graph = task["task_graph"] as TaskGraph;
                context["params_list"] = new List<TaskRegister>();
                List<TaskRegister> regs = node[3].Eval(context);
                var resultReg = task.AllocateNewRegister(TaskRegisterKind.V);
                task_graph.AddVertex(new TGDSLPrimitiveCallNode(node[1].GetSourceText(), resultReg, regs.ToArray()));
                task_graph.AddEdge(task_graph.VertexCount - 1, task_graph.VertexCount);

                context["params_list"] = null;
                return resultReg;
            }

            /*DSL_ACCESSOR IDENTIFIER LP RP*/
            if (node.Count() == 4 
                 && node[0].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[1].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[2].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[3].GetType().IsAssignableFrom(typeof(ASTTerminalNode)))
            {
                if (context.RuntimeMemoryContains("state"))
                {
                    var task = (context["task_stack"] as Stack<HybridTask>).Peek();
                    var task_graph = task["task_graph"] as TaskGraph;
                    var resultReg = task.AllocateNewRegister(TaskRegisterKind.V);
                    task_graph.AddVertex(new TGDSLPrimitiveCallNode(node[1].GetSourceText(), resultReg));
                    task_graph.AddEdge(task_graph.VertexCount - 1, task_graph.VertexCount);

                    return resultReg;
                }
            }
            throw new NotImplementedException();
        }


        public static dynamic CallParamsNode(CallParamsNode node, RuntimeContext context)
        {
            /* call_params COMMA call_param */
            if(node.Count() == 3 
                 && node[0].GetType().IsAssignableFrom(typeof(CallParamsNode))
                 && node[1].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[2].GetType().IsAssignableFrom(typeof(CallParamNode)))
            {
                node[0].Eval(context);
                List<TaskRegister> registers = (List<TaskRegister>)context["params_list"];
                TaskRegister reg = node[2].Eval(context);
                registers.Add(reg);
                return registers;
            }
            /* call_param */
            if(node.Count() == 1 
                 && node[0].GetType().IsAssignableFrom(typeof(CallParamNode)))
            {
                List<TaskRegister> registers = 
                    (List<TaskRegister>) context["params_list"];
                TaskRegister reg = node[0].Eval(context);
                registers.Add(reg);
                return registers;
            }
            throw new NotImplementedException();
        }


        public static dynamic CallParamNode(CallParamNode node, RuntimeContext context)
        {
            /*expression*/
            if(node.Count() == 1 
                 && node[0].GetType().IsAssignableFrom(typeof(ExpressionNode)))
            {

                return node[0].Eval(context);
            }
            throw new NotImplementedException();
        }


        public static dynamic PrimitiveExpressionNode(PrimitiveExpressionNode node, RuntimeContext context)
        {
            /*IDENTIFIER*/
            if(node.Count() == 1 
                 && node[0].GetType().IsAssignableFrom(typeof(ASTTerminalNode)))
            {

                if ("building_task".Equals(context["state"]) && context.RuntimeMemoryContains("task_stack"))
                {
                    var task = (context["task_stack"] as Stack<HybridTask>).Peek();
                    if (task.nameMap.ContainsKey(node[0].GetSourceText()))
                    {
                        return task.nameMap[node[0].GetSourceText()];
                    }
                }
                
                if ("building_query".Equals(context["state"]))
                {
                    throw new Exception();
                }
                return null;
            }
            /* member_access */
            if(node.Count() == 1 
                 && node[0].GetType().IsAssignableFrom(typeof(MemberAccessNode)))
            {
                return node[0].Eval(context);
            }
            /*return primitive_express*/
            if (node.Count() == 2
                 && node[0].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[1].GetType().IsAssignableFrom(typeof(PrimitiveExpressionNode))
                 )
            {
                TaskRegister reg = node[1].Eval(context);
                var task = (context["task_stack"] as Stack<HybridTask>).Peek();
                var task_graph = task["task_graph"] as TaskGraph;
                task_graph.AddVertex(new TGOutNode(reg));
                task_graph.AddEdge(task_graph.VertexCount - 1, task_graph.VertexCount);
                return reg;
            }
            throw new NotImplementedException();
        }


        public static dynamic MemberAccessNode(MemberAccessNode node, RuntimeContext context)
        {
            throw new NotImplementedException();
            /*IDENTIFIER DOT IDENTIFIER*/
            if(node.Count() == 3 
                 && node[0].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[1].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[2].GetType().IsAssignableFrom(typeof(ASTTerminalNode)))
            {
                return null;
            }
            /*IDENTIFIER DOT IDENTIFIER LP RP*/
            if(node.Count() == 5 
                 && node[0].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[1].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[2].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[3].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[4].GetType().IsAssignableFrom(typeof(ASTTerminalNode)))
            {
                return null;
            }
            /*IDENTIFIER DOT IDENTIFIER LP call_params RP*/
            if(node.Count() == 6 
                 && node[0].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[1].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[2].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[3].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[4].GetType().IsAssignableFrom(typeof(CallParamsNode))
                 && node[5].GetType().IsAssignableFrom(typeof(ASTTerminalNode)))
            {
                return null;
            }
        }

        public static (HybridTask task, TaskRegister register) BuildQueryTask(RuntimeContext context, ASTTerminalNode fromVariable, TypetNode fromVaribaleType, ASTTerminalNode inVariableType, ExpressionNode whereExpression, ExpressionNode selectExpression)
        {
            var state = context["state"] + "";

            if ("building_task".Equals(state) || "building_query".Equals(state))
            {
                (context["state_stack"] as Stack<string>).Push(state);
                context["state"] = "building_query";
                var task = (context["task_stack"] as Stack<HybridTask>).Peek();

                /* build anomyous task */
                TaskRegister reg = task.AllocateNewRegister(TaskRegisterKind.T);
                HybridTask newTask = new HybridTask() { taskName = "task%" + reg.number };
                (context["task_stack"] as Stack<HybridTask>).Push(newTask);

                /* TODO: type inference */
                newTask.AppendBindVariable(TaskVariableBindType.IN, "?", fromVariable.GetSourceText());
                newTask.AppendBindVariable(TaskVariableBindType.IN, "?", inVariableType.GetSourceText());
                newTask.AppendBindVariable(TaskVariableBindType.OUT, "?", "");

                var res1 = selectExpression.Eval(context);
                var res2 = whereExpression?.Eval(context);

                int num = 100;
                var task_graph = newTask["task_graph"] as TaskGraph;

                task_graph.AddVertex(new TGInVertex());

                int fromNodeIndex = task_graph.VertexCount + (num > 0 ? 1 : 0);

                // TODO Count Inference
                /* |DPU| if node[3] has a type of "::G" 
                 * else the size of a primitive array */
                for (int i = 0; i < num; i++)
                {
                    if(whereExpression != null)
                    {
                        task_graph.AddVertex(new TGWhereSelectNode());
                    }
                    else
                    {
                        task_graph.AddVertex(new TGSelectNode());
                    }
                }

                int toNodeIndex = task_graph.VertexCount;

                if (toNodeIndex - fromNodeIndex >= 1)
                {
                    task_graph.AddVertex(new TGMergeNode());
                }

                task_graph.AddVertex(new TGOutNode(newTask.AllocateNewRegister(TaskRegisterKind.T)));
                task_graph.AddEdge(fromNodeIndex, Enumerable.Range(fromNodeIndex - 1, toNodeIndex).ToArray());

                task["dependencies"].Append(newTask);
                (context["task_stack"] as Stack<HybridTask>).Pop();
                context["state"] = (context["state_stack"] as Stack<string>).Pop();

                return (newTask, reg);
            }
            return (null, null);
        }

        public static dynamic QueryExpressionNode(QueryExpressionNode node, RuntimeContext context)
        {
            var state = context["state"] + "";

            /*FROM IDENTIFIER IS typeT IN IDENTIFIER WHERE expression SELECT expression*/
            if(node.Count() == 10 
                 && node[0].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[1].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[2].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[3].GetType().IsAssignableFrom(typeof(TypetNode))
                 && node[4].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[5].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[6].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[7].GetType().IsAssignableFrom(typeof(ExpressionNode))
                 && node[8].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[9].GetType().IsAssignableFrom(typeof(ExpressionNode)))
            {
                return BuildQueryTask(context, node[1] as ASTTerminalNode, node[3] as TypetNode, node[5] as ASTTerminalNode, node[7] as ExpressionNode, node[9] as ExpressionNode).register;

                if ("building_task".Equals(state) || "building_query".Equals(state))
                {
                    (context["state_stack"] as Stack<string>).Push(state);
                    context["state"] = "building_query";
                    var task = (context["task_stack"] as Stack<HybridTask>).Peek();
                    
                    /* build anomyous task */
                    TaskRegister reg = task.AllocateNewRegister(TaskRegisterKind.T);
                    HybridTask newTask = new HybridTask() { taskName = "task%" + reg.number };
                    (context["task_stack"] as Stack<HybridTask>).Push(newTask);

                    /* TODO: type inference */
                    newTask.AppendBindVariable(TaskVariableBindType.IN, "?", node[1].GetSourceText());
                    newTask.AppendBindVariable(TaskVariableBindType.IN, "?", node[3].GetSourceText());
                    newTask.AppendBindVariable(TaskVariableBindType.OUT, "?", "");

                    var res = node[9].Eval(context);
                    int num = 100;
                    var task_graph = newTask["task_graph"] as TaskGraph;

                    task_graph.AddVertex(new TGInVertex());

                    int fromNodeIndex = task_graph.VertexCount + (num > 0 ? 1 : 0);

                    // TODO Count Inference
                    /* |DPU| if node[3] has a type of "::G" 
                     * else the size of a primitive array */
                    for (int i = 0; i < num; i++)
                    {
                        task_graph.AddVertex(new TGWhereSelectNode());
                    }
                    
                    int toNodeIndex = task_graph.VertexCount;

                    if (toNodeIndex - fromNodeIndex >= 1)
                    {
                        task_graph.AddVertex(new TGMergeNode());
                    }

                    task_graph.AddVertex(new TGOutNode(newTask.AllocateNewRegister(TaskRegisterKind.T)));
                    task_graph.AddEdge(fromNodeIndex, Enumerable.Range(fromNodeIndex - 1, toNodeIndex).ToArray());

                    task["dependencies"].Append(newTask);
                    (context["task_stack"] as Stack<HybridTask>).Pop();
                    context["state"] = (context["state"] as Stack<string>).Pop();

                    return reg;
                }

            }

            /*FROM IDENTIFIER IS typeT IN IDENTIFIER SELECT expression*/
            if (node.Count() == 8 
                 && node[0].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[1].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[2].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[3].GetType().IsAssignableFrom(typeof(TypetNode))
                 && node[4].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[5].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[6].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[7].GetType().IsAssignableFrom(typeof(ExpressionNode)))
            {
                return BuildQueryTask(
                    context,
                    node[1] as ASTTerminalNode, 
                    node[3] as TypetNode, 
                    node[5] as ASTTerminalNode, null, 
                    node[7] as ExpressionNode).register;

            }

            /*FROM IDENTIFIER IN IDENTIFIER WHERE expression SELECT expression*/
            if (node.Count() == 8 
                 && node[0].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[1].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[2].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[3].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[4].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[5].GetType().IsAssignableFrom(typeof(ExpressionNode))
                 && node[6].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[7].GetType().IsAssignableFrom(typeof(ExpressionNode)))
            {
                return BuildQueryTask(
                        context, node[1] as ASTTerminalNode,
                        null, node[3] as ASTTerminalNode, 
                        node[5] as ExpressionNode, node[7] as ExpressionNode).register;
            }

            /*FROM IDENTIFIER IN IDENTIFIER SELECT expression*/
            if(node.Count() == 6 
                 && node[0].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[1].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[2].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[3].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[4].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[5].GetType().IsAssignableFrom(typeof(ExpressionNode)))
            {
                return BuildQueryTask(
                            context,
                            node[1] as ASTTerminalNode, null,
                            node[3] as ASTTerminalNode, null, node[5] as ExpressionNode).register;

                if ("building_task".Equals(state))
                {
                    var task = (context["task_stack"] as Stack<HybridTask>).Peek();
                    
                    /* build anomyous task */
                    TaskRegister reg = task.AllocateNewRegister(TaskRegisterKind.T);
                    HybridTask newTask = new HybridTask() { taskName = "task%" + reg.number};
                    (context["task_stack"] as Stack<HybridTask>).Push(newTask);

                    /** TODO: type inference **/
                    newTask.AppendBindVariable(TaskVariableBindType.IN, "?" , node[1].GetSourceText());
                    newTask.AppendBindVariable(TaskVariableBindType.IN, "?", node[3].GetSourceText());
                    newTask.AppendBindVariable(TaskVariableBindType.OUT, "?", "");

                    var res = node[5].Eval(context);
                    int num = 100;
                    var task_graph = newTask["task_graph"] as TaskGraph;

                    task_graph.AddVertex(new TGInVertex());

                    int fromNodeIndex = task_graph.VertexCount + (num > 0 ? 1 : 0);

                    // TODO Count Inference
                    /* |DPU| if node[3] has a type of "::G" 
                     * else the size of a primitive array 
                     */
                    for (int i = 0; i < 100; i++)
                    {
                        task_graph.AddVertex(new TGSelectNode());
                    }
                    int toNodeIndex = task_graph.VertexCount;

                    if(toNodeIndex - fromNodeIndex >= 1)
                    {
                        task_graph.AddVertex(new TGMergeNode());
                    }
                    task_graph.AddVertex(new TGOutNode(newTask.AllocateNewRegister(TaskRegisterKind.T)));
                    task_graph.AddEdge(fromNodeIndex, Enumerable.Range(fromNodeIndex - 1, toNodeIndex).ToArray());

                    task["dependencies"].Append(newTask);
                    (context["task_stack"] as Stack<HybridTask>).Pop();

                    return reg;
                }
                
                return null;
            }



            /*cmp_expression*/
            if (node.Count() == 1 
                 && node[0].GetType().IsAssignableFrom(typeof(CmpExpressionNode)))
            {
                return null;
            }
            throw new NotImplementedException();
        }


        public static dynamic CmpExpressionNode(CmpExpressionNode node, RuntimeContext context)
        {
            throw new NotImplementedException();
            /*cmp_expression EQ expression*/
            if(node.Count() == 3 
                 && node[0].GetType().IsAssignableFrom(typeof(CmpExpressionNode))
                 && node[1].GetType().IsAssignableFrom(typeof(ASTTerminalNode))
                 && node[2].GetType().IsAssignableFrom(typeof(ExpressionNode)))
            {
                return null;
            }
            /*expression*/
            if(node.Count() == 1 
                 && node[0].GetType().IsAssignableFrom(typeof(ExpressionNode)))
            {
                return null;
            }
        }


        public static dynamic HostBlockNode(HostBlockNode node, RuntimeContext context)
        {
            throw new NotImplementedException();
            /*HOST_BLOCK*/
            if(node.Count() == 1 
                 && node[0].GetType().IsAssignableFrom(typeof(ASTTerminalNode)))
            {
                return null;
            }
        }

    }

    public enum TaskVariableBindType
    {
        IN,
        OUT
    }

    internal class BindVariablePrototype
    {
        public ASTNode type;
        public ASTNode name;
        public ASTNode this[string arg]
        {
            get
            {
                switch (arg)
                {
                    case "type":
                        return this.type;
                        break;
                    case "name":
                        return this.name;
                        break;
                }
                return null;
            }
            set
            {
                switch (arg) {
                    case "type":
                        this.type = value;
                        break;
                    case "name":
                        this.name = value;
                        break;
                }
            }
        }
    }
}