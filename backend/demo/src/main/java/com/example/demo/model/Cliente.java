package com.example.demo.model;

public class Cliente {

//    ATRIBUTOS
    private int idCliente;
    private String nome;
    private String sobrenome;
    private String email;
    private String senha;
    private String telefone;

//    CONSTRUTORES
    public Cliente() {

    }

    public Cliente(int idCliente, String nome, String sobrenome, String email, String senha, String telefone) {
        this.idCliente = idCliente;
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
    }

    public Cliente(int idCliente, String nome, String sobrenome, String email, String senha) {
        this.idCliente = idCliente;
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.email = email;
        this.senha = senha;
    }

//    GETTERS
    public int getIdCliente() {
        return idCliente;
    }

    public String getNome() {
        return nome;
    }

    public String getSobrenome() {
        return sobrenome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public String getTelefone() {
        return telefone;
    }

//    SETTERS
    public void setSenha(String senha) {
        this.senha = senha;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

//    TOSTRING
    public String toString() {
        return "Id: " + this.idCliente + "\tNome: " + this.nome + "\tSobrenome: " + this.sobrenome + "Email: " + this.email +
                "Senha: " + this.senha + "Telefone: " + this.telefone;
    }
}
