package com.example.demo.controller;

import com.example.demo.dao.ClienteDAO;
import com.example.demo.model.Cliente;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String senha = request.getParameter("password");

        ClienteDAO dao = new ClienteDAO();
        Cliente clienteLogado = dao.validarLogin(email, senha);

        if (clienteLogado != null) {
            HttpSession session = request.getSession();
            session.setAttribute("usuarioLogado", clienteLogado);
            response.sendRedirect("inicio.jsp");
        } else {
            request.setAttribute("erroLogin", "E-mail ou senha incorretos.");
            request.getRequestDispatcher("index.jsp").forward(request, response);
        }
    }
}
