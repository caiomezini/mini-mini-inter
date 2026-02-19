package com.example.demo.dao;

import com.example.demo.config.Conexao;
import com.example.demo.model.Cliente;

import java.sql.*;

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


}
