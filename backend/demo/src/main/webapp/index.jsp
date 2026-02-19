<%--
  Created by IntelliJ IDEA.
  User: caiomachado-ieg
  Date: 19/02/2026
  Time: 17:23
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="style.css">
    <title>Login Page</title>
</head>

<body>
<main>
    <h1>Login Page</h1>
    <form action="/login" method="post">
        <label for="email">Email:</label>
        <input type="email" id="email" name="email" required placeholder="example@example.com">
        <label for="password">Password:</label>
        <input type="password" id="password" name="password" required>
        <button type="submit">Login</button>
    </form>
    <p>Don't have an account? <a href="./cadastro.jsp">Register here</a></p>
</main>
</body>

</html>
