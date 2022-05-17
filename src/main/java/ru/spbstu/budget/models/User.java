package ru.spbstu.budget.models;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "usr")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    @Enumerated(value = EnumType.STRING)
    private Role role;

    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    private Status status;
}
