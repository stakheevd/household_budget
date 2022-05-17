package ru.spbstu.budget.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public enum Permission {
    USERS_READ("users:read"),
    USERS_WRITE("users:write");

    @Setter
    @Getter
    private final String permission;
}
