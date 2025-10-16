package com.example.DI;

import org.springframework.stereotype.Component;

@Component
public class Laptop {

    public void compile() {
        System.out.println("Compiling...");
    }

    public void debug() {
        System.out.println("Debugging...");
    }
}
