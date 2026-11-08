package com.example.demo.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {
    public Connection conectar() {
        String url = variavelObrigatoria("DB_URL");
        String usuario = variavelObrigatoria("DB_USER");
        String senha = variavelObrigatoria("DB_PASSWORD");

        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(url, usuario, senha);
        } catch (SQLException | ClassNotFoundException e) {
            // Nao inclui URL, usuario ou senha na mensagem.
            throw new IllegalStateException("Nao foi possivel conectar ao PostgreSQL.", e);
        }
    }

    private String variavelObrigatoria(String nome) {
        String valor = System.getenv(nome);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Variavel de ambiente obrigatoria ausente: " + nome);
        }
        return valor;
    }

    public void desconectar(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }
}
