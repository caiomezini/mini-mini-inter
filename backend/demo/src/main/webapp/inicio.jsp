<%--
  Created by IntelliJ IDEA.
  User: caiomachado-ieg
  Date: 20/02/2026
  Time: 16:42
  To change this template use File | Settings | File Templates.
--%>

<%
  if (session.getAttribute("usuarioLogado") == null) {
    response.sendRedirect("index.jsp?erro=proibido");
    return;
  }
%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <link rel="stylesheet" href="inicio.css">
  <title>Home Page</title>
</head>
<body>
  <main>
    <%
      com.example.demo.model.Cliente c = (com.example.demo.model.Cliente) session.getAttribute("usuarioLogado");
    %>
    <h1>Seja Bem-vindo <%= c.getNome() %>!</h1>
    <a href="LogoutServlet" style="color: red;"><button>Sair do sistema</button></a>
  </main>
</body>
</html>
