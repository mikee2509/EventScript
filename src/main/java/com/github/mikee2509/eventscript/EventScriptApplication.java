package com.github.mikee2509.eventscript;

import com.github.mikee2509.eventscript.domain.Script;
import com.github.mikee2509.eventscript.parser.ParserCreator;
import com.github.mikee2509.eventscript.parser.visitor.ScriptVisitor;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class EventScriptApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventScriptApplication.class, args);
    }

    /*
    Wynik dzialania programu:

    NOTIFY LPAREN STRING_LITERAL RPAREN
    VAR IDENTIFIER ASSIGN FLOAT_LITERAL
    ON_TIME LPAREN DATETIME LPAREN RPAREN DOT IDENTIFIER LPAREN DECIMAL_LITERAL RPAREN COMMA RING LPAREN RPAREN RPAREN
    */

    @Bean
    public CommandLineRunner lexerTest() {
        return args -> {
            List<String> testStrings = Arrays.asList(
                "Notify(\"Take a break!\")",
                "var price = 12.99",
                "OnTime(datetime().plusHours(1), Ring())"
            );

            System.out.println();
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
            System.out.println();
        };
    }

    @Bean
    CommandLineRunner declarationTest(ScriptVisitor visitor) {
        return args -> {
            ParserCreator parserCreator = new ParserCreator();
            EventScriptParser parser = parserCreator.fromString("var price : float\n");
            EventScriptParser.ScriptContext scriptContext = parser.script();
            Script script = visitor.visit(scriptContext);
            System.out.println(script);
        };
    }
}