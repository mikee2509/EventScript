package com.github.mikee2509.eventscript;

import com.github.mikee2509.eventscript.parser.ParserCreator;
import com.github.mikee2509.eventscript.parser.visitor.ScriptVisitor;
import lombok.extern.java.Log;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

@Log
@SpringBootApplication
public class EventScriptApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventScriptApplication.class, args);
    }

    @Bean
    CommandLineRunner declarationTest(ScriptVisitor visitor) {
        return args -> {
            Scanner sc = new Scanner(System.in);
            System.out.println("Printing the file passed in:");

            StringBuilder input = new StringBuilder();
            while(sc.hasNextLine()) input.append(sc.nextLine()).append("\n");

            log.info("\n\n\n");
            ParserCreator parserCreator = new ParserCreator();
            try {
                EventScriptParser parser = parserCreator.fromString(input.toString());
                visitor.visit(parser.script());
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info("\n\n\n");
        };
    }
}