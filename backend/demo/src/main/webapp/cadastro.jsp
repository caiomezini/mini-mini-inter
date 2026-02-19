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
  <h1>Register Page</h1>
  <form action="/register" method="post">
    <label for="name">Name:</label>
    <input type="text" id="name" name="name" required>

    <label for="surname">Surname:</label>
    <input type="text" id="surname" name="surname" required>

    <label for="email">Email:</label>
    <input type="email" id="email" name="email" required placeholder="example@example.com">

    <label for="password">Password:</label>
    <input type="password" id="password" name="password" required>

    <label for="telephone">Telephone:</label>
    <input type="tel" id="telephone" name="telephone" placeholder="(xxx) xxxxx-xxxx">

    <button type="submit">Register</button>
  </form>
  <p>Already have an account? <a href="./index.jsp">Login here</a></p>
</main>
</body>

</html>