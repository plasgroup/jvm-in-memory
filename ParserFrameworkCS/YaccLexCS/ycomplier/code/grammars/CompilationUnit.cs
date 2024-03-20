
using System;
using System.Collections.Generic;
using YaccLexCS.ycomplier;
using YaccLexCS.runtime;
using YaccLexCS.ycomplier.attribution;
namespace YaccLexCS.config
{
    [GrammarConfiguration]
    public static class GrammarCompilationUnit
    {

        [BeginningGrammarDefinition("compilation_unit", "compilation_unit definition_or_comment", "definition_or_comment")]
        public static void compilation_unit()
        {
        }

        [GrammarDefinition("definition_or_comment",  "task_definition_statement", "comment")]
        public static void definition_or_comment()
        {
        }

        [GrammarDefinition("comment",  "SINGLE_LINE_COMMENT")]
        public static void comment()
        {
        }

        [GrammarDefinition("task_definition_statement",  "DEF_TASK IDENTIFIER LP task_param_list RP RIGHT_ARROW typeT block")]
        public static void task_definition_statement()
        {
        }
    }
}