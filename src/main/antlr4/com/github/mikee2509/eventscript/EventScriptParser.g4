parser grammar EventScriptParser;

options { tokenVocab=EventScriptLexer; }

script
    : statement* function* EOF
    ;

statement
    : variableDeclaration NL
    | variableDefinition NL
    | IF '(' expression ')' blockOrStatement (ELSE blockOrStatement)?
    | FOR '(' forInit? ';' expression? ';' forUpdate=expressionList? ')' blockOrStatement
    | RETURN expression? NL
    | BREAK NL
    | CONTINUE NL
    | statementExpression=expression NL
    ;

variableDeclaration
    : VAR IDENTIFIER ':' type
    ;

variableDefinition
    : VAR IDENTIFIER '=' expression
    ;

forInit
    : variableDefinition
    | expressionList
    ;

expression
    : literal                                                   #literalExp
    | expression bop='.' ( IDENTIFIER | functionCall)           #childExp
    | functionCall                                              #functionExp
    | expression postfix=('++' | '--')                          #postfixExp
    | prefix=('+'|'-'|'++'|'--') expression                     #unaryExp
    | prefix='!' expression                                     #negationExp
    | expression bop=('*'|'/'|'%') expression                   #multiplicativeExp
    | expression bop=('+'|'-') expression                       #additiveExp
    | expression bop=('<=' | '>=' | '>' | '<') expression       #relationalExp
    | expression bop=('==' | '!=') expression                   #equalityExp
    | expression bop='&&' expression                            #logicalAndExp
    | expression bop='||' expression                            #logicalOrExp
    | <assoc=right> expression bop='=' expression               #assignmentExp
    ;

literal
    : numberLiteral
    | STRING_LITERAL
    | BOOL_LITERAL
    ;

numberLiteral
    : DECIMAL_LITERAL
    | FLOAT_LITERAL
    ;

functionCall
    : IDENTIFIER '(' expressionList? ')'
    ;

expressionList
    : expression (',' expression)*
    ;

type
    : BOOL
    | DATETIME
    | DURATION
    | FLOAT
    | FUNC
    | INT
    | STRING
    ;

function
    : FUNC IDENTIFIER '(' parameterList? ')' ('->' returnType)? block
    ;

parameterList
    : parameter (',' parameter)*
    ;

parameter
    : IDENTIFIER ':' type
    ;

returnType
    : type
    | '(' type (',' type)* ')'
    ;

block
    : '{' statement* '}' NL
    ;

blockOrStatement
    : block
    | statement
    ;



