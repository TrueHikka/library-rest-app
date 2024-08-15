package ru.library.models;

public enum Role {
    ROLE_ADMIN("ROLE_ADMIN"),
    ROLE_USER("ROLE_USER");

    private final String title;

    Role(String title) {
        this.title = title;
    }
    @Override
    public String toString() {
        return title;
    }
}
