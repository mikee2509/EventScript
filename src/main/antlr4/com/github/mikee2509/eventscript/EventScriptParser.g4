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
    : RING                     #ringFunc
    | SPEAK                    #speakFunc
    | VIBRATE                  #vibrateFunc
    | NOTIFY                   #notifyFunc
    | CALL                     #callFunc
    | LAUNCH                   #launchFunc
    | ADD_TO_CALENDAR          #addToCalendarFunc
    | SET_RINGER_VOLUME        #setRingerVolumeFunc
    | SET_MEDIA_VOLUME         #setMediaVolumeFunc
    | SET_ALARM_CLOCK          #setAlarmClockFunc
    | SET_WIFI                 #setWifiFunc
    | SET_FLASHLIGHT           #setFlashlightFunc
    | SET_BRIGHTNESS           #setBrightnessFunc
    | ON_INTERVAL              #onIntervalScheduleFunc
    | ON_TIME                  #onTimeScheduleFunc
    | ON_LOCATION              #onLocationScheduleFunc
    | ON_MESSAGE               #onMessageScheduleFunc
    | ON_WIFI_ENABLED          #onWifiEnabledScheduleFunc
    | ON_WIFI_DISABLED         #onWifiDisabledScheduleFunc
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
