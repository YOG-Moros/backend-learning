package com.example.quickstart.entity;


public record message(String message, String module) {
    @Override
    public String message() {
        return message;
    }

    @Override
    public String module() {
        return module;
    }
}
