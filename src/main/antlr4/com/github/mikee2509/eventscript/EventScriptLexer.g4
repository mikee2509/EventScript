lexer grammar EventScriptLexer;

// Keywords
BOOL:               'bool';
BREAK:              'break';
CONTINUE:           'continue';
DATETIME:           'datetime';
DURATION:           'duration';
ELSE:               'else';
FLOAT:              'float';
FOR:                'for';
FUNC:               'func';
IF:                 'if';
INT:                'int';
RETURN:             'return';
STRING:             'string';
VAR:                'var';
WHILE:              'while';


// Built-in functions
RING:               'Ring';
SPEAK:              'Speak';
VIBRATE:            'Vibrate';
NOTIFY:             'Notify';
CALL:               'Call';
LAUNCH:             'Launch';
ADD_TO_CALENDAR:    'AddToCalendar';
SET_RINGER_VOLUME:  'SetRingerVolume';
SET_MEDIA_VOLUME:   'SetMediaVolume';
SET_ALARM_CLOCK:    'SetAlarmClock';
SET_WIFI:           'SetWifi';
SET_FLASHLIGHT:     'SetFlashlight';
SET_BRIGHTNESS:     'SetBrightness';


// Schedulers
ON_INTERVAL:        'OnInterval';
ON_TIME:            'OnTime';
ON_LOCATION:        'OnLocation';
ON_MESSAGE:         'OnMessage';
ON_WIFI_ENABLED:    'OnWifiEnabled';
ON_WIFI_DISABLED:   'OnWifiDisabled';


// Literals
DECIMAL_LITERAL:    '0'
               |    [1-9] Digit*
               ;

FLOAT_LITERAL:      Digit+ '.' Digit+;

BOOL_LITERAL:       'true'
            |       'false'
            ;

STRING_LITERAL:     '"' (~["\\\r\n] | EscapeSequence)* '"';


// Separators
LPAREN:             '(';
RPAREN:             ')';
LBRACE:             '{';
RBRACE:             '}';
SEMI:               ';';
COMMA:              ',';
DOT:                '.';


// Operators
ASSIGN:             '=';
GT:                 '>';
LT:                 '<';
BANG:               '!';
EQUAL:              '==';
LE:                 '<=';
GE:                 '>=';
NOTEQUAL:           '!=';
AND:                '&&';
OR:                 '||';
INC:                '++';
DEC:                '--';
ADD:                '+';
SUB:                '-';
MUL:                '*';
DIV:                '/';
MOD:                '%';
COLON:              ':';
ARROW:              '->';


// Whitespace and comments
NL:                 [\r\n]+;
WS:                 [ \t\u000C]+     -> channel(HIDDEN);
COMMENT:            '/*' .*? '*/'    -> channel(HIDDEN);
LINE_COMMENT:       '//' ~[\r\n]*    -> channel(HIDDEN);


// Identifiers
IDENTIFIER:         Letter LetterOrDigit*;


// Fragment rules
fragment EscapeSequence
    : '\\' [btnfr"'\\]
    ;
fragment LetterOrDigit
    : Letter
    | Digit
    ;
fragment Digit
    : [0-9]
    ;
fragment Letter
    : [a-zA-Z$_]
    | ~[\u0000-\u007F\uD800-\uDBFF]
    | [\uD800-\uDBFF] [\uDC00-\uDFFF]
    ;