<%--
  Created by IntelliJ IDEA.
  User: caiomachado-ieg
  Date: 19/02/2026
  Time: 17:26
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <link rel="stylesheet" href="cadastro.css">
  <title>Register Page</title>
</head>

<body>
<main>

  <%
    String status = request.getParameter("status");
    if ("sucesso".equals(status)) {
  %>
  <div style="background-color: #d4edda; color: #155724; padding: 15px; border: 1px solid #c3e6cb; border-radius: 5px; margin-bottom: 20px;">
    <strong>Sucesso!</strong> O cadastro foi realizado com sucesso.
  </div>
  <% } %>

  <h1>Register Page</h1>
  <form action="register" method="post">
    <label for="name">Name:</label>
    <input type="text" id="name" name="name" required>

    <label for="surname">Surname:</label>
    <input type="text" id="surname" name="surname" required>

    <label for="email">Email:</label>
    <input type="email" id="email" name="email" value="${param.email}" required placeholder="example@example.com">
    <% if (request.getAttribute("erroEmail") != null) { %>
    <span style="color: red;"><%= request.getAttribute("erroEmail") %></span>
    <% } %>

    <label for="password">Password:</label>
    <input type="password" id="password" name="password" required>
    <% if (request.getAttribute("erroSenha") != null) { %>
    <span style="color: red;"><%= request.getAttribute("erroSenha") %></span>
    <% } %>

    <label for="telephone">Telephone:</label>
    <input type="tel" id="telephone" name="telephone" placeholder="(XX) XXXXX-XXXX" maxlength="15"
      onkeyup="mascaraTelefone(this)" pattern="\(\d{2}\)\s\d{5}-\d{4}">

    <script>
      function mascaraTelefone(i) {
        let v = i.value;
        v = v.replace(/\D/g, "");
        v = v.replace(/^(\d{2})(\d)/g, "($1) $2");
        v = v.replace(/(\d{5})(\d)/, "$1-$2");
        i.value = v;
      }
    </script>

    <button type="submit">Register</button>

    <% if (request.getAttribute("erroGeral") != null) { %>
    <div style="color: darkred; background: #ffcccc; padding: 10px; border: 1px solid #ebccd1; border-radius: 4px; margin-top: 15px;">
      <strong>Erro:</strong> <%= request.getAttribute("erroGeral") %>
    </div>
    <% } %>
  </form>
  <p>Already have an account? <a href="./index.jsp">Login here</a></p>
</main>
</body>

</html>