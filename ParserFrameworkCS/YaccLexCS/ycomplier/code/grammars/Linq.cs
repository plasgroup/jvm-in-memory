
using System;
using System.Collections.Generic;
using YaccLexCS.ycomplier;
using YaccLexCS.runtime;
using YaccLexCS.ycomplier.attribution;
namespace YaccLexCS.config
{
    [GrammarConfiguration]
    public static class GrammarLinq
    {

        [GrammarDefinition("query_expression",  "FROM IDENTIFIER IS typeT IN IDENTIFIER WHERE expression SELECT expression", "FROM IDENTIFIER IS typeT IN IDENTIFIER SELECT expression", "FROM IDENTIFIER IN IDENTIFIER WHERE expression SELECT expression", "FROM IDENTIFIER IN IDENTIFIER SELECT expression","cmp_expression")]
        public static void query_expression()
        {
        }
    }
}