package com.github.mikee2509.eventscript;

import com.github.mikee2509.eventscript.EventScriptLexer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

import java.util.Arrays;
import java.util.List;

/*
Wynik dzialania programu:

NOTIFY LPAREN STRING_LITERAL RPAREN
VAR IDENTIFIER ASSIGN FLOAT_LITERAL
ON_TIME LPAREN DATETIME LPAREN RPAREN DOT IDENTIFIER LPAREN DECIMAL_LITERAL RPAREN COMMA RING LPAREN RPAREN RPAREN
*/

public class App {
    public static void main(String[] args) {
        List<String> testStrings = Arrays.asList(
                "Notify(\"Take a break!\")",
                "var price = 12.99",
                "OnTime(datetime().plusHours(1), Ring())"
        );

        testStrings.stream()
            .map(CharStreams::fromString)
            .map(EventScriptLexer::new)
            .forEach(lexer -> {
                Token token = lexer.nextToken();
                while (token.getType() != Token.EOF) {
                    String tokenChannelName = EventScriptLexer.channelNames[token.getChannel()];
                    if (!tokenChannelName.equals("HIDDEN")) {
                        System.out.print(lexer.getVocabulary().getSymbolicName(token.getType()));
                        System.out.print(" ");
                    }
                    token = lexer.nextToken();
                }
                System.out.println();
            });
    }
}
