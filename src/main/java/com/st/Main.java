package com.st;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class Main {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(ConfigurationApplicationContext.class);
        ctx.refresh();
        GeneratorService application = ctx.getBean(GeneratorService.class);
        application.run(args);
    }
}
