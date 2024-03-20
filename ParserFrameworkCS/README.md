# 1. Introduction

This is a compiler framework that can quickly design a language and generate an AST interpreter for it from a meta configuration of the language.



The `ConfigFileGenerator` project aims at generating `.cs` configuration files for `ycompiler` under `YaccLexCS` project by an input `meta configuration file`
This program will output follow `.cs` files.

+ (tokens configuration) `<output-path>/TokenList.cs`
+ (grammars configuration) `<output-path>/grammars/<NonterminationSymbolName>.cs` (multiple)
+ (data structures needed for generating AST) `<output-path>/structure/xx.cs` 



# 2. Meta Configuration File Example

``` plain
%Tokens%
.methodName{
  $! tokenName regexExp p
  $ tokenName regexExp
  $! tokenName regexExp p
  $ tokenName regexExp
tokenName regexExp

  ....
}

$! tokenName regexExp p
$ tokenName regexExp
$! tokenName regexExp p
$ tokenName regexExp
...
%%

%Grammars%
%%

```


[Example]
``` plain
%Tokens%
.Operator{
ADD +
SUB -
MUL *
MOD %
DIV /
}

.Relation{
$ LOGICAL_OR \|\|
LOGICAL_AND &&
NE !=
LT <
LE <=
GT >
GE >=
}

COMMA ,
EQ ==
INTRODUCE =>
ASSIGN =
$ STRING \"[^\"]*\"


.SpecialCharacter{
CR \n
LP (
RP )
LM [
RM ]
LC {
RC }
COLON :
SEMICOLON ;
SHARP #
POINT .
}

$ Skip [ \r\t]


.KeyWord{
WHILE while
IF if
ELSE else
ELSIF elsif
FOR for
FALSE_T false
TRUE_T true
NULL_T null
CONTINUE continue
RETURN return
BREAK break
LET let
VAR var     
LAMBDA lambda
DYFN dyfn
NEW new
CLASS class
PRIVATE private
PROTECTED protected
PUBLIC public
STATIC static
}

$! ID [A-Z_a-z]+|[A-Z_a-z]+[0-9_A-Za-z] 1
$ DOUBLE_LITERAL [0-9]+\.[0-9]+|[1-9][0-9]*|0

$! ERR . 255
%%

%Grammars%
.statement{
statement:if_statement|while_statement|expression SEMICOLON|for_statement
while_statement:WHILE LP expression RP statement
if_statement:IF LP expression RP statement|IF LP expression RP statement ELSE statement|IF LP expression RP statement elsif_list|IF LP expression RP statement elsif_list ELSE statement
elsif_list:elsif|elsif_list elsif
elsif:ELSIF LP expression RP statement
statement_list:statement|statement_list statement
for_statement:FOR LP expression SEMICOLON expression SEMICOLON expression RP statement
}

.expression{
expression:assign_expression|define_var_expression|lambda_expression|RETURN expression
assign_expression:ID ASSIGN expression|logical_or_expression
logical_or_expression:logical_and_expression|logical_or_expression LOGICAL_OR logical_and_expression
logical_and_expression:equality_expression|logical_and_expression LOGICAL_AND equality_expression
equality_expression:relational_expression|equality_expression EQ relational_expression|equality_expression NE relational_expression
relational_expression:additive_expression|relational_expression GT additive_expression|relational_expression GE additive_expression|relational_expression LT additive_expression|relational_expression LE additive_expression
additive_expression:additive_expression ADD multiplicative_expression|additive_expression SUB multiplicative_expression|multiplicative_expression
multiplicative_expression:unary_expression|multiplicative_expression MUL unary_expression|multiplicative_expression DIV unary_expression|multiplicative_expression MOD unary_expression
unary_expression:access_expression|SUB unary_expression|NEW object_new_expreesion|NEW array_new_expression
object_new_expreesion:ID LP augument_list RP|ID LP RP
array_new_expression:LM RM LC augument_list RC|LM DOUBLE_LITERAL RM LC augument_list RC
access_expression:primary_expression|ID LP RP|ID LP augument_list RP|LP expression RP|native_expression
primary_expression:DOUBLE_LITERAL|STRING|BREAK|CONTINUE|FALSE_T|TRUE_T|NULL_T|ID
define_var_expression:LET ID ASSIGN expression|VAR ID ASSIGN expression
native_expression:SHARP access_list LP augument_list RP|SHARP access_list LP RP
lambda_expression:LAMBDA LP params_list RP INTRODUCE statement
}

.compileUnit{
compile_unit:definition_or_statement|compile_unit definition_or_statement
definition_or_statement:statement|definition
}

.block{
block:LC statement_list RC|LC RC
}

.function{
access_list:access_list POINT access_name|access_name
access_name:ID
id_list:id_list COMMA ID|ID
augument_list:augument_list COMMA augument|augument
augument:expression
params_list:params_list COMMA param|param
param:typeless_param|typed_param
typeless_param:ID
typed_param:ID ID
}

.definition{
definition:function_definition|class_definition
function_definition:DYFN ID LP params_list RP block|DYFN ID LP RP block
class_definition:CLASS ID COLON id_list LC class_body RC|CLASS ID LC class_body RC
class_body:field_or_function_list
field_or_function_list:field_or_function_list field_or_function|field_or_function
field_or_function:class_field|class_function|access_control_list class_field|access_control_list class_function
class_field:ID ID SEMICOLON|ID SEMICOLON|ID ID ASSIGN expression SEMICOLON|ID ASSIGN expression SEMICOLON
class_function:ID LP params_list RP block|ID LP RP block
access_control_list:access_control_list access_control_word|access_control_word
access_control_word:PRIVATE|PUBLIC|PROTECTED|STATIC
}

%%
```



## 3. Source Code Level Configuration

### 3.1 Lexer Configuration: `TokenList.cs`
The `TokenList.cs` generated by **[Example]**.
One or more token definitions are associated with a callback function. The callback function
will automatically invoked by the lexer everytime one of the token associated with the function
is parsed.

``` csharp
using System;
using YaccLexCS.ycomplier;
using YaccLexCS.ycomplier.attribution;

namespace YaccLexCS.config
{
    
    [TokenConfiguration]
    public static class TokenList
    {
        
        [TokenDefinition("ADD", "+")]
        [TokenDefinition("SUB", "-")]
        [TokenDefinition("MUL", "*")]
        [TokenDefinition("MOD", "%")]
        [TokenDefinition("DIV", "/")]
       
        public static void Operator(CompilerContext content)
        {
        }

        [TokenDefinition("LOGICAL_OR", @"\|\|", true)]
        [TokenDefinition("LOGICAL_AND", "&&")]
        [TokenDefinition("NE", "!=")]
        [TokenDefinition("LT", "<")]
        [TokenDefinition("LE", "<=")]
        [TokenDefinition("GT", ">")]
        [TokenDefinition("GE", ">=")]

        public static void Relation(CompilerContext content)
        {

        }
        
        [TokenDefinition("COMMA", ",")]
        public static void Romma(CompilerContext content)
        {

        }

        [TokenDefinition("EQ", "==")]
        public static void Eq(CompilerContext content)
        {
           // $"meet operator {content.TokenText}".PrintToConsole();
        }
        [TokenDefinition("INTRODUCE", "=>")]
        public static void Introduce(CompilerContext context)
        {

        }
        [TokenDefinition("ASSIGN", "=")]
        public static void Assign(CompilerContext content)
        {
            //$"meet operator {content.TokenText}".PrintToConsole();
        }
        [TokenDefinition("STRING", "\"[^\"]*\"", true)]
        public static void StringType(CompilerContext context)
        {
            var v = context.TokenText;
            $"get string = {v}".PrintToConsole();
            context.TokenVal = v;
        }
        
        
        [TokenDefinition("CR", "\n")]
        [TokenDefinition("LP", "(")]
        [TokenDefinition("RP", ")")]
        [TokenDefinition("LM", "[")]
        [TokenDefinition("RM", "]")]
        [TokenDefinition("LC", "{")]
        [TokenDefinition("RC", "}")]
        [TokenDefinition("COLON", ":")]
        [TokenDefinition("SEMICOLON", ";")]
        [TokenDefinition("SHARP", "#")]
        [TokenDefinition("POINT", ".")]
        
        public static void SpecialCharacter()
        {
            //$"meet special character".PrintToConsole();
        }
        
        [TokenDefinition("Skip", @"[ \r\t]", true)]
        public static void Skip(){}
        
        
        [TokenDefinition("WHILE", "while")]
        [TokenDefinition("IF", "if")]
        [TokenDefinition("ELSE", "else")]
        [TokenDefinition("ELSIF", "elsif")]
        [TokenDefinition("FOR", "for")]
        [TokenDefinition("FALSE_T","false")]
        [TokenDefinition("TRUE_T", "true")]
        [TokenDefinition("NULL_T", "null")]
        [TokenDefinition("CONTINUE", "continue")]
        [TokenDefinition("RETURN", "return")]
        [TokenDefinition("BREAK", "break")]
        [TokenDefinition("LET", "let")]
        [TokenDefinition("VAR", "var")]
        [TokenDefinition("LAMBDA", "lambda")]
        [TokenDefinition("DYFN", "dyfn")]
        [TokenDefinition("NEW", "new")]
        [TokenDefinition("CLASS", "class")]
        [TokenDefinition("PRIVATE", "private")]
        [TokenDefinition("PROTECTED", "protected")]
        [TokenDefinition("PUBLIC", "public")]
        [TokenDefinition("STATIC", "static")]
        public static void KeyWord()
        {
            
        }

        [TokenDefinition("ID", @"[A-Z_a-z]+|[A-Z_a-z]+[0-9_A-Za-z]", true, 1)]
        public static void Id(CompilerContext context)
        {
            
        }
        [TokenDefinition("DOUBLE_LITERAL", @"[0-9]+\.[0-9]+|[1-9][0-9]*|0", true)]
        public static void DoubleLiteral(CompilerContext context)
        {
            //$"DOUBLE_LITERAL with val = {context.TokenText}".PrintToConsole();
            var val = double.Parse(context.TokenText);
            context.TokenVal = val;
        }

        [TokenDefinition("ERR", ".", true, 255)] // the least priority
        public static void Error()
        {
            $"error token!".PrintToConsole();
            throw new Exception();
        }
        
    }
    
}
```



Auto-generated `TokenList.cs` should be put into `./code` inside the `ycompiler` folder.



### 3.2 Grammar File

The ConfigurationGenerator will generate a single `.cs` grammar configure 
file for each grammar block in the meta configuration file.

As an example, 
for grammar block

``` plain
.function{
access_list:access_list POINT access_name|access_name
access_name:ID
id_list:id_list COMMA ID|ID
augument_list:augument_list COMMA augument|augument
augument:expression
params_list:params_list COMMA param|param
param:typeless_param|typed_param
typeless_param:ID
typed_param:ID ID
}
```

in the meta configuration file,
the configurator generates a `GrammarFunction.cs` in `<output-path>/grammar/`.
With content as follow

``` csharp

using System;
using System.Collections.Generic;
using YaccLexCS.ycomplier;
using YaccLexCS.runtime;
using YaccLexCS.ycomplier.attribution;
namespace YaccLexCS.config
{
    [GrammarConfiguration]
    public static class GrammarFunction
    {
        [GrammarDefinition("access_list", "access_list POINT access_name", "access_name")]
        public static void access_list()
        {
        }

        [GrammarDefinition("access_name", "ID")]
        public static void access_name()
        {
        }
       
        [GrammarDefinition("id_list", "id_list COMMA ID", "ID")]
        public static void id_list()
        {
        }

        [GrammarDefinition("augument_list", "augument_list COMMA augument", "augument")]
        public static void augument_list()
        {
        }

        [GrammarDefinition("augument", "expression")]
        public static void augument()
        {
        }

        [GrammarDefinition("params_list", "params_list COMMA param",  "param")]
        public static void params_list()
        {
        }

        [GrammarDefinition("param",  "typeless_param", "typed_param")]
        public static void param()
        {
        }

        [GrammarDefinition("typeless_param",  "ID")]
        public static void typeless_param()
        {
        }

        [GrammarDefinition("typed_param",  "ID ID")]
        public static void typed_param()
        {
        }
    }
}
```



**Both Lexer and parser utilize an event-driven design. With the callback function grammar/token annotation is attach  is parsed/meet.**



Auto-generated source-code level grammar configurations

  should be put into `./grammars` folder  inside the `ycompiler` folder.



### 4. AST Node Data Structure

AST Node Data Structure needed for the designed language is auto-generated.

All `.cs` file should be put into the `code/structure` under `ycompiler`.

Each kind of AST Node has an `Eval(RuntimeContext context)`, in which `RuntimeContext` is the runtime context that can be utilized to implement a simple languague virtual machine.
