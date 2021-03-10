package com.fr;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        var parser = new JSONParser("test.json");
        var map = parser.parse();
        System.out.println(map);
    }
}