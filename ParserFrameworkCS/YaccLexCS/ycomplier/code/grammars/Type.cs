
using System;
using System.Collections.Generic;
using YaccLexCS.ycomplier;
using YaccLexCS.runtime;
using YaccLexCS.ycomplier.attribution;
namespace YaccLexCS.config
{
    [GrammarConfiguration]
    public static class GrammarType
    {

        [GrammarDefinition("task_param_list",  "task_param", "task_param_list COMMA task_param")]
        public static void task_param_list()
        {
        }

        [GrammarDefinition("task_param",  "typeT IDENTIFIER")]
        public static void task_param()
        {
        }

        [GrammarDefinition("typeT",  "host_type", "dsl_type")]
        public static void typeT()
        {
        }

        [GrammarDefinition("host_type",  "HOST_TYPE")]
        public static void host_type()
        {
        }

        [GrammarDefinition("dsl_type",  "DSL_ACCESSOR IDENTIFIER", "DSL_ACCESSOR IDENTIFIER LSquareB RSquareB", "DSL_ACCESSOR IDENTIFIER LSquareB INTEGER_LITERAL RSquareB")]
        public static void dsl_type()
        {
        }
    }
}