package com.example.demo.dao;

import com.example.demo.config.Conexao;
import com.example.demo.model.Cliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {
    public Cliente inserir(Cliente cliente) {
        Connection conn = null;
        Conexao conexao = null;
        PreparedStatement pstmt = null;
        ResultSet rset = null;
        try {
            conexao = new Conexao();
            conn = conexao.conectar();
            pstmt = conn.prepareStatement("INSERT INTO CLIENTE (NOME, SOBRENOME, EMAIL, SENHA, TELEFONE) VALUES(?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, cliente.getNome());
            pstmt.setString(2, cliente.getSobrenome());
            pstmt.setString(3, cliente.getEmail());
            pstmt.setString(4, cliente.getSenha());
            pstmt.setString(5, cliente.getTelefone());
            pstmt.executeUpdate();

            rset = pstmt.getGeneratedKeys();
            int id = 0;

            if (rset.next()) {
                id = rset.getInt(1);
            }

            cliente.setIdCliente(id);

            return cliente;

        } catch (SQLException sqle) {
            sqle.printStackTrace();
            return null;
        } finally {
            try {
                if (rset != null) rset.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
            conexao.desconectar(conn);
        }
    }

    public List<Cliente> listarTodos() {
        List<Cliente> lista = new ArrayList<>();
        Connection conn = null;
        Conexao conexao = null;
        Statement stmt = null;
        ResultSet rset = null;
        try {
            conexao = new Conexao();
            conn = conexao.conectar();

            stmt = conn.createStatement();
            rset = stmt.executeQuery("SELECT * FROM PRODUTO");

            while (rset.next()) {
                Cliente cliente = new Cliente();
                cliente.setIdCliente(rset.getInt("ID_CLIENTE"));
                cliente.setNome(rset.getString("NOME"));
                cliente.setSobrenome(rset.getString("SOBRENOME"));
                cliente.setEmail(rset.getString("EMAIL"));
                cliente.setSenha(rset.getString("SENHA"));
                cliente.setTelefone(rset.getString("TELEFONE"));

                lista.add(cliente);
            }

        } catch (SQLException sqle) {
            sqle.printStackTrace();
            rset = null;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (rset != null) rset.close();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
            conexao.desconectar(conn);
        }
        return lista;
    }

    public Cliente validarLogin(String email, String senha) {
        Connection conn = null;
        Conexao conexao = null;
        ResultSet rset = null;
        PreparedStatement pstmt = null;
        Cliente clienteLogado = null;
        try {
            conexao = new Conexao();
            conn = conexao.conectar();

            pstmt = conn.prepareStatement("SELECT * FROM CLIENTE WHERE EMAIL = ? AND SENHA = ?");
            pstmt.setString(1, email);
            pstmt.setString(2, senha);
            rset = pstmt.executeQuery();

            if (rset.next()) {
                clienteLogado = new Cliente();
                clienteLogado.setIdCliente(rset.getInt("ID_CLIENTE"));
                clienteLogado.setNome(rset.getString("NOME"));
                clienteLogado.setSobrenome(rset.getString("SOBRENOME"));
                clienteLogado.setEmail(rset.getString("EMAIL"));
                clienteLogado.setSenha(rset.getString("SENHA"));
                clienteLogado.setTelefone(rset.getString("TELEFONE"));
                return clienteLogado;
            }


        } catch (SQLException sqle) {
            sqle.printStackTrace();
            rset = null;
            return null;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (rset != null) rset.close();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
            conexao.desconectar(conn);
        }
    }

    public int alterarNome(int id, String novoNome) {
        Connection conn = null;
        Conexao conexao = null;
        PreparedStatement pstmt = null;
        try {
            conexao = new Conexao();
            conn = conexao.conectar();
            pstmt = conn.prepareStatement("UPDATE CLIENTE SET NOME = ? WHERE ID_CLIENTE = ?");
            pstmt.setString(1, novoNome);
            pstmt.setInt(2, id);
            int linhas = pstmt.executeUpdate();

            if (linhas > 0) {
                return 1; //sucesso
            } else {
                return 0; //não funcionou
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            return -1; //erro
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
            conexao.desconectar(conn);
        }
    }

    public int remover(int id) {
        Connection conn = null;
        Conexao conexao = null;
        PreparedStatement pstmt = null;
        try {
            conexao = new Conexao();
            conn = conexao.conectar();
            pstmt = conn.prepareStatement("DELETE FROM CLIENTE WHERE ID_CLIENTE = ?");
            pstmt.setInt(1, id);
            int linhas = pstmt.executeUpdate();

            if (linhas > 0) {
                return 1; // sucesso
            } else {
                return 0; // não funcionou
            }

        } catch (SQLException sqle) {
            sqle.printStackTrace();
            return -1; // erro
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
            conexao.desconectar(conn);
        }
    }
}
