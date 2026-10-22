package top.cch.quickstart.entity;


public record message(String message, String module) {

    public String messageword() {
        return message;
    }

    @Override
    public String module() {
        return module;
    }
}
