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

    public int alterarNome(int id, String novoNome) {
        Connection conn = null;
        Conexao conexao = null;
        PreparedStatement pstmt = null;
        try {
            conexao = new Conexao();
            conn = conexao.conectar();
            pstmt = conn.prepareStatement("UPDATE CLIENTE SET NOME = ? WHERE ID = ?");
            pstmt.setDouble(1, novoNome);
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


}
