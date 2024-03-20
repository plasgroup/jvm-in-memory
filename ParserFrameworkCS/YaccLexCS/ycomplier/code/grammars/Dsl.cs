
using System;
using System.Collections.Generic;
using YaccLexCS.ycomplier;
using YaccLexCS.runtime;
using YaccLexCS.ycomplier.attribution;
namespace YaccLexCS.config
{
    [GrammarConfiguration]
    public static class GrammarDsl
    {
        [GrammarDefinition("block",  "LB statements RB", "LB RB")]
        public static void block()
        {
        }

        [GrammarDefinition("statements", "statements statement", "statement")]
        public static void statements()
        {
        }

        [GrammarDefinition("statement",  "expression SEMICOLON")]
        public static void statement()
        {
        }

        [GrammarDefinition("expression",  "bracket_expression", "query_expression", "host_expression", "dsl_func_call", "primitive_expression", "var_definition")]
        public static void expression()
        {
        }
        
        [GrammarDefinition("var_definition", "typeT IDENTIFIER", "typeT IDENTIFIER var_initializer")]
        public static void var_definition()
        {
        }

        [GrammarDefinition("var_initializer", "ASSIGN expression")]
        public static void var_initializer()
        {
        }


        [GrammarDefinition("dsl_func_call", "DSL_ACCESSOR IDENTIFIER LP call_params RP", "DSL_ACCESSOR IDENTIFIER LP RP")]
        public static void dsl_func_call()
        {
        }
        [GrammarDefinition("call_params", "call_params COMMA expression", "call_param")]
        public static void call_params()
        {
        }
        [GrammarDefinition("call_param", "expression")]
        public static void call_param()
        {
        }

        [GrammarDefinition("primitive_expression", "IDENTIFIER", "member_access", "RETURN primitive_expression")]
        public static void primitive_expression()
        {
        }

        [GrammarDefinition("bracket_expression",  "LP expression RP")]
        public static void bracket_expression()
        {
        }

        [GrammarDefinition("host_expression",  "HOST_EXPRESSION")]
        public static void host_expression()
        {
        }

        [GrammarDefinition("member_access", "IDENTIFIER DOT IDENTIFIER", "IDENTIFIER DOT IDENTIFIER LP RP", "IDENTIFIER DOT IDENTIFIER LP call_params RP")]
        public static void member_access()
        {
        }

        [GrammarDefinition("cmp_expression", "cmp_expression EQ primitive_expression", "primitive_expression")]
        public static void query_expression()
        {
        }
    }
}