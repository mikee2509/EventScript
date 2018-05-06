parser grammar EventScriptParser;

options { tokenVocab=EventScriptLexer; }

script
    : statement* function* EOF
    ;

statement
    : variableDeclaration eos                                                              #varDeclarationStmt
    | variableDefinition eos                                                               #varDefinitionStmt
    | IF '(' expression ')' blockOrStatement (ELSE blockOrStatement)?                      #ifStmt
    | FOR '(' forInit? ';' expression? ';' forUpdate=expressionList? ')' blockOrStatement  #forStmt
    | RETURN expressionList? eos                                                           #returnStmt
    | BREAK eos                                                                            #breakStmt
    | CONTINUE eos                                                                         #continueStmt
    | eos                                                                                  #noOpStmt
    | statementExpression=expression eos                                                   #expressionStmt
    ;

eos
    : NL
    | ';' NL?
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
    | IDENTIFIER                                                #identifierExp
    | expression bop='.' ( IDENTIFIER | functionCall)           #childExp
    | functionCall                                              #functionExp
    | builtInFunctionCall                                       #builtInFuncExp
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
    : DECIMAL_LITERAL                      #decimalLiteral
    | FLOAT_LITERAL                        #floatLiteral
    | DATETIME parExpressionList           #datetimeLiteral
    | DURATION parExpressionList           #durationLiteral
    | STRING_LITERAL                       #stringLiteral
    | BOOL_LITERAL                         #boolLiteral
    ;

functionCall
    : IDENTIFIER parExpressionList
    ;

builtInFunctionCall
    : builtInFunction parExpressionList
    ;

parExpressionList
    : '(' expressionList? ')'
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
    | VOID
    ;

builtInFunction
    : RING
    | SPEAK
    | VIBRATE
    | NOTIFY
    | CALL
    | LAUNCH
    | ADD_TO_CALENDAR
    | SET_RINGER_VOLUME
    | SET_MEDIA_VOLUME
    | SET_ALARM_CLOCK
    | SET_WIFI
    | SET_FLASHLIGHT
    | SET_BRIGHTNESS
    | ON_INTERVAL
    | ON_TIME
    | ON_LOCATION
    | ON_MESSAGE
    | ON_WIFI_ENABLED
    | ON_WIFI_DISABLED
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
    : '{' NL? statement* '}' NL?
    ;

blockOrStatement
    : block
    | statement
    ;
