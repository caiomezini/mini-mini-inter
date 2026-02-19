package com.example.demo.controller;

import com.example.demo.dao.ClienteDAO;
import com.example.demo.model.Cliente;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/register")
public class CadastroServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String nome = request.getParameter("name");
        String sobrenome = request.getParameter("surname");
        String email = request.getParameter("email");
        String senha = request.getParameter("password");
        String telefone = request.getParameter("telephone");

        Cliente novoCliente = new Cliente();
        novoCliente.setNome(nome);
        novoCliente.setSobrenome(sobrenome);
        String regexDominios = "^[^@]+@(gmail\\.com|yahoo\\.com|hotmail\\.com|outlook\\.com)$";

        if (email == null || email.trim().isEmpty() || !email.matches(regexDominios)) {
            request.setAttribute("erroEmail", "E-mail inválido ou domínio não suportado!");
            request.getRequestDispatcher("cadastro.jsp").forward(request, response);
            return;
        } else {
            novoCliente.setEmail(email.trim());
        }
        String regexSenha = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z0-9!@#$%^&*(),.?\":{}|<>]{8,}$";

        if (senha == null || !senha.matches(regexSenha)) {
            request.setAttribute("erroSenha", "A senha deve ter 8+ caracteres, 1 maiúscula, 1 número e 1 caractere especial.");
            response.sendRedirect("cadastro.html?error=senha_invalida");
            return;
        } else {
            novoCliente.setSenha(senha);
        }
        if (telefone != null && !telefone.trim().isEmpty()) {
            novoCliente.setTelefone(telefone);
        }

        ClienteDAO dao = new ClienteDAO();
        try {
            dao.inserir(novoCliente);
            response.sendRedirect("lista-clientes.jsp?msg=sucesso");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("cadastro.html?error=true");
        }

    }
}
