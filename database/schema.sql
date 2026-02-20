create table cliente (
    id_cliente serial primary key,
    nome varchar(255) not null,
    sobrenome varchar(255) not null,
    email varchar(255) not null unique,
    senha varchar(255) not null,
    telefone varchar(20)
)

alter table cliente
alter column telefone set default 'Não registrado';