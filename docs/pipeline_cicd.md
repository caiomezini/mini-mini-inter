# Implementação da pipeline CI/CD

Status em 08/11/2026: testes locais e CI aprovados. O CD concluiu a publicação após reexecução do job na `main`. Falta registrar um novo push com publicação na primeira tentativa e o teste funcional com o Aiven.
Repositório: https://github.com/caiomezini/mini-mini-inter
Pipeline: `.github/workflows/cicd.yml`
Branch de trabalho: `pipeline_cicd`.

## Responsabilidades e escopo

O usuário executa os testes, commits, pushes e a configuração do Render e do GitHub. O assistente preparou a implementação, corrigiu o Maven Wrapper para Windows após aprovação e consultou os registros de execuções já iniciadas pelo usuário. O PDF final será produzido após a conclusão da coleta de evidências.

A cópia local usada é `C:\Users\caiomachado-ieg\OneDrive - Instituto J&F\Área de Trabalho\Outras_Coisas\mini_mini_inter`.
A branch foi criada nessa cópia, sem clonagem. A alteração preexistente em `backend/.idea/misc.xml` pertence ao usuário e foi preservada.

## Arquivos e configurações

- `.github/workflows/cicd.yml`: eventos push/main e pull_request/main; dois jobs, CI e CD.
- `Dockerfile`: Maven com JDK 11 gera o WAR; Tomcat 10.1 com JDK 17 executa o bytecode Java 11. O WAR é copiado como ROOT.war para disponibilizar a aplicação em `/`. O Tomcat usa a variável PORT ao iniciar.
- `.dockerignore`: reduz o contexto, exclui metadados Git/IDE, arquivos .env, resultados de builds e pastas que não integram a aplicação JSP. O driver PostgreSQL é baixado pelo Maven, sem usar o JAR avulso do repositório.
- `backend/demo/src/main/java/com/example/demo/config/Conexao.java`: URL, usuário e senha saem do código e passam a ser lidos de DB_URL, DB_USER e DB_PASSWORD. Variáveis ausentes e falhas de conexão produzem erro explícito em vez de retornar uma conexão nula. A assinatura pública dos métodos foi mantida.
- `docs/pipeline_cicd.md`: este guia e registro do processo.

O pom.xml não foi alterado. Não foram criados testes unitários. `verify` compila e empacota o projeto e executa testes existentes; sem classes de teste, não significa que houve testes unitários. A verificação HTTP adicional exige resposta bem-sucedida e o texto `Login Page` em `/index.jsp`, verificando também a renderização de JSP pelo Tomcat. Ela não acessa o Aiven e não comprova cadastro e login.

### CI

1. Checkout obtém o código do evento. Em PR, o checkout padrão verifica o resultado de integração preparado pelo GitHub.
2. setup-java fornece Temurin 11 e cache de dependências do Maven.
3. `bash ./mvnw --batch-mode --no-transfer-progress verify`: usa o Maven Wrapper do projeto, resolve dependências, compila e verifica o WAR. Invocar por bash evita precisar mudar o bit executável de mvnw.
4. `docker build`: monta a imagem com o Dockerfile. O build Maven dentro da imagem também executa verify; essa repetição assegura que o Dockerfile possa funcionar sozinho no Render, com custo adicional de tempo.
5. Um contêiner temporário publica a porta 8080 apenas em localhost do runner; a CI tenta obter a página JSP até 60 vezes. Exibe logs em caso de falha e remove o contêiner ao terminar.

### CD

`needs: ci` impede iniciar CD quando CI falha. O `if` aceita exclusivamente push na main. PR executa CI, com CD ignorado. O token GitHub tem somente leitura do conteúdo.

O job CD usa Python disponível no runner, sem bibliotecas adicionais. A API do Render recebe o SHA exato de GITHUB_SHA em commitId. A pipeline acompanha o identificador do deploy por até 20 minutos, exige status live e o mesmo SHA, e verifica a página pública. Os resultados ficam no resumo do job. Ela falha se houver erro ou expirar o prazo; o Render pode continuar trabalhando após um timeout, e seu painel deve ser consultado antes de repetir.

O job utiliza RENDER_API_KEY como secret e RENDER_SERVICE_ID e RENDER_SERVICE_URL como variables do repositório. As credenciais Aiven são configuradas somente no serviço do Render e no ambiente local usado para testes funcionais. Não são necessárias na CI.

A API foi escolhida para acompanhar a conclusão e o commit do deploy. Um deploy hook simples também dispara a publicação, mas sua aceitação não comprova que o deploy terminou. A chave de API exige cuidado porque pode conceder acesso amplo à conta; use permissões restritas ao serviço quando a conta permitir e não compartilhe seu valor.

As tags das imagens-base podem receber atualizações. O Render reconstrói a imagem do commit aprovado, em vez de receber a mesma imagem construída no runner. Não há publicação no Docker Hub. Para exigir identidade binária entre CI e produção, seria necessário aprovar uma estratégia adicional de registro e digest.

## Passo 1 - testes locais manuais (usuário)

Abra o PowerShell na raiz do projeto. Confira a branch e os arquivos:

```powershell
git branch --show-current
git status --short
```

A branch esperada é pipeline_cicd. A alteração em backend/.idea/misc.xml já existia e não deve ser incluída por engano nos commits da pipeline.

Se tiver Java 11 instalado, confira e execute o Wrapper:

```powershell
java -version
Push-Location backend/demo
.\mvnw.cmd --batch-mode --no-transfer-progress verify
Pop-Location
```

Resultado esperado: BUILD SUCCESS e geração de backend/demo/target/demo-1.0-SNAPSHOT.war. Se java não estiver disponível ou JAVA_HOME apontar para outra instalação, ajuste o Java antes de repetir. Não interprete No tests to run como testes unitários aprovados.

Com Docker Desktop iniciado e usando contêineres Linux:

```powershell
docker version
docker build --tag mini-mini-inter:local .
docker run --detach --name mini-mini-inter-local --publish 127.0.0.1:8080:8080 mini-mini-inter:local
```

Abra http://localhost:8080/index.jsp. Deve aparecer a página de login. Este contêiner não tem credenciais Aiven: use-o apenas para verificar carregamento da página. Não tente cadastrar ou entrar nele.

Se não abrir, consulte:

```powershell
docker logs mini-mini-inter-local
```

Depois, remova o contêiner temporário:

```powershell
docker rm --force mini-mini-inter-local
```

Se a porta 8080 estiver ocupada, use `--publish 127.0.0.1:8081:8080` e abra localhost:8081. Se o nome já existir, consulte os contêineres no Docker Desktop e remova apenas o contêiner temporário deste teste antes de repetir.

Para executar a aplicação pelo IDE e verificar o banco localmente, configure DB_URL, DB_USER e DB_PASSWORD na configuração de execução do servidor Tomcat. Um processo que já está aberto precisa ser reiniciado para receber novas variáveis.

## Passo 2 - publicar a branch e verificar a CI de PR (usuário)

Depois de revisar e executar os testes locais, selecione somente os arquivos desta implementação:

```powershell
git add .github/workflows/cicd.yml Dockerfile .dockerignore backend/demo/src/main/java/com/example/demo/config/Conexao.java docs/pipeline_cicd.md
git diff --cached --stat
git commit -m "ci: configurar pipeline Maven Docker e deploy Render"
git push -u origin pipeline_cicd
```

Esses comandos são instruções; o assistente não os executou. Evite divulgar a saída completa do diff de Conexao.java: a versão anterior contém a senha exposta.

No GitHub, crie um Pull Request de pipeline_cicd para main. Em Actions, acompanhe o workflow CI/CD - Maven, Docker e Render. Resultado esperado: CI aprovada e CD ignorado. Um push somente na pipeline_cicd sem PR aberto não dispara o workflow. Configurar Render não é necessário para testar a CI do PR.

Guarde o link da execução, o SHA e capturas do evento pull_request e dos jobs. Não faça merge antes de conferir essa CI.

## Passo 3 - preparar o Aiven (usuário)

1. Entre no console Aiven e abra o serviço PostgreSQL usado pelo projeto.
2. Troque a senha que estava versionada. As telas variam; procure o gerenciamento de usuários do PostgreSQL e a opção de redefinir senha do usuário utilizado. Se não localizar, envie o nome das opções que aparecem, sem credenciais.
3. Confira os dados de conexão: hostname, porta, nome do banco, usuário e nova senha. O nome do banco deve corresponder ao banco real, inclusive maiúsculas/minúsculas quando aplicável.
4. Para DB_URL, use o formato JDBC, não a URI postgresql:// de outros clientes:

```text
jdbc:postgresql://HOST:PORTA/NOME_DO_BANCO?sslmode=require
```

sslmode=require exige conexão criptografada, mas não valida a identidade do servidor como verify-full. Se sua configuração exigir validação do certificado CA do Aiven, a inclusão desse certificado e a configuração verify-full precisarão de uma nova proposta de alteração; não desative SSL para contornar erros.

5. Confira se a tabela cliente já existe no banco utilizado. Não execute os seeds ou o schema automaticamente em produção. O arquivo database/schema.sql atual está sem ponto e vírgula depois do CREATE TABLE; isso precisa ser corrigido, com aprovação, se formos usá-lo para inicialização. Não foi alterado nesta etapa.
6. Se o Aiven aplicar restrições de IP, consulte o intervalo de saída do serviço Render escolhido e permita apenas o acesso necessário. Não altere as regras indiscriminadamente.

A remoção da senha do arquivo não a remove do histórico Git. Trocar a senha invalida a credencial exposta sem reescrever o histórico do projeto.

## Passo 4 - criar e configurar o Web Service no Render (usuário)

Primeiro faça merge do PR com CI aprovada. O primeiro push na main poderá falhar no CD por ainda não existir configuração Render. Essa falha de preparação é esperada e deve ser registrada, sem classificar a pipeline como concluída. Após criar o serviço e as credenciais, execute novamente o job CD da execução desse mesmo commit para concluir a configuração inicial. As publicações seguintes serão automáticas por push na main.

1. Acesse https://dashboard.render.com e entre na sua conta.
2. Clique em New e depois Web Service.
3. Escolha Git Provider, conecte GitHub e autorize acesso ao repositório caiomezini/mini-mini-inter.
4. Selecione esse repositório. Configure:

| Campo | Valor |
| --- | --- |
| Name | Um nome disponível, por exemplo mini-mini-inter |
| Region | Escolha levando em conta a região do banco Aiven |
| Branch | main |
| Language/Runtime | Docker |
| Root Directory | Em branco (raiz do repositório) |
| Dockerfile Path | ./Dockerfile |
| Docker Build Context Directory, se aparecer | . |
| Docker Command | Em branco, usa CMD do Dockerfile |
| Health Check Path | /index.jsp |
| Auto-Deploy | Off |

Auto-Deploy deve ficar Off; o job CD chamará a API após a CI. Se a opção só estiver disponível depois da criação, altere-a imediatamente em Settings. Não selecione On Commit. Não selecione After CI Checks Pass, pois o CD deste workflow já controla a publicação.

5. Escolha o plano apropriado ao laboratório, conferindo preço e limitações na tela. Não é necessário criar um banco Render: será usado o Aiven existente.
6. Na área Environment/Advanced, adicione:

| Variável | Valor |
| --- | --- |
| DB_URL | URL JDBC do banco Aiven com SSL |
| DB_USER | Usuário do banco |
| DB_PASSWORD | Nova senha do banco |
| PORT | 10000 |

Digite valores reais apenas no painel. Nunca os coloque no repositório, relatório, logs compartilhados ou chat.

7. Clique em Create Web Service. A criação inicia um primeiro deploy de preparação, que deve ser registrado como operação manual de configuração, não como evidência do CD automático.
8. Acompanhe os logs. Após o serviço ficar disponível, abra a URL pública e /index.jsp.
9. Copie a URL HTTPS pública do serviço. Copie também o Service ID, que começa com srv-, exibido no painel/URL do serviço. Não confunda com identificadores de deploy, que começam com dep-.
10. Em Account Settings do Render, abra API Keys e crie uma chave para a pipeline. Quando houver configuração de escopo, limite ao serviço e às operações necessárias de criar/consultar deploy. Copie-a somente para o GitHub Secrets; ela pode ser mostrada integralmente apenas na criação.

## Passo 5 - configurar GitHub e concluir o CD (usuário)

No GitHub: repositório > Settings > Secrets and variables > Actions.

Na aba Secrets, clique New repository secret:

- Nome: RENDER_API_KEY
- Valor: a chave da API criada no Render.

Na aba Variables, clique New repository variable para cada item:

- RENDER_SERVICE_ID: o ID srv-... do Web Service.
- RENDER_SERVICE_URL: URL pública completa, por exemplo https://SEU-SERVICO.onrender.com, sem /index.jsp.

Não use chave da API como variable. Não adicione a senha Aiven no workflow.

Abra Actions e a execução gerada pelo push/merge na main. Se o CD falhou por configuração ausente, use Re-run failed jobs. Se o workflow ainda estiver em andamento, aguarde seu término antes de repetir. A execução precisa estar associada a push na main: repetir uma execução de PR continuará ignorando o CD.

Resultado esperado: CI aprovada; CD solicita o SHA dessa execução, acompanha até live, confirma o SHA e obtém HTTP 200 com a página esperada. Salve o link, o resumo, o SHA e o deploy ID. Compare esse SHA com o mostrado no Render.

A repetição após configurar as credenciais encerra a preparação inicial. Para demonstrar uma nova publicação automática, use um push/merge posterior real na main, com mudança previamente aprovada. Não use apenas o botão Manual Deploy como evidência do CD automático. Uma alteração adicional para essa demonstração deve ser proposta e aprovada antes de editar arquivos.

## Passo 6 - verificação funcional manual (usuário)

1. Abra a URL pública HTTPS, acesse cadastro.jsp e crie um usuário de teste sem dados pessoais reais.
2. Confirme o cadastro e faça login com esse usuário; confira a página inicio.jsp e depois o logout.
3. Se o cadastro ou login falhar, consulte os logs do Render e o estado do PostgreSQL no Aiven. Uma página inicial acessível não comprova conexão ao banco.
4. Guarde capturas sem senha, token ou dados pessoais. Não inclua o painel com DB_PASSWORD visível.
5. Informe os resultados e links nesta conversa. Correções de arquivos serão propostas para aprovação antes de implementadas.

## Diagnóstico inicial e limitações

| Sintoma | Verificação sugerida |
| --- | --- |
| Wrapper não encontra Java | Conferir Java 11 e JAVA_HOME |
| Docker daemon indisponível | Iniciar Docker Desktop com contêineres Linux |
| Erro ao baixar dependências/imagens | Conferir rede e mensagem específica do Maven/Docker |
| CI verde, CD ignorado | Esperado em PR; CD exige push/main |
| Secret/variable ausente | Conferir nomes e abas Secrets e Variables do GitHub |
| Render API HTTP 401/403 | Chave incorreta, expirada ou sem permissão |
| Render API HTTP 404 | Service ID incorreto ou serviço inacessível pela chave |
| Falha de build no Render | Conferir Root Directory, Dockerfile Path e logs |
| Timeout aguardando Render | Conferir status no painel antes de repetir; não é prova automática de falha do build |
| Página JSP retorna 404/500 | Conferir inicialização do Tomcat e implantação de ROOT.war |
| Página funciona, login/cadastro falha | Conferir DB_URL JDBC, usuário, nova senha, SSL, rede e tabela cliente |

Também foi observado SELECT * FROM PRODUTO em ClienteDAO.listarTodos(), embora o modelo seja Cliente; não foi alterado porque não integra as mudanças aprovadas. O schema.sql está sem delimitador entre CREATE TABLE e ALTER TABLE. Esses pontos serão tratados separadamente se afetarem os testes manuais. O projeto armazena/compara senhas de usuários diretamente no SQL; adequação de autenticação para produção exigiria outro escopo. O laboratório não deve usar dados reais.

Para demonstrar que uma CI com falha bloqueia CD, falta planejar e aprovar um teste controlado, sem deixar uma versão inválida publicada. Não houve indução de falha nesta etapa.

## Registro dos prompts

Contagem atual de **9 prompts relevantes**. A contagem inclui pedidos de implementação, informações novas para executar o trabalho, erros diagnosticados e mensagens com resultados ou evidências novos. A pergunta de esclarecimento sobre clonagem e as aprovações que apenas confirmam uma proposta ficam fora da contagem. A autorização que forneceu o caminho do repositório entra como prompt 2.

### Prompt relevante 1 - solicitação inicial

Transcrição do pedido, preservando o conteúdo e normalizando apenas a formatação Markdown:

> Link do repositório GitHub: https://github.com/caiomezini/mini-mini-inter
>
> Persona
>
> Você é um profissional Sênior que é especializado na parte de infraestrutura de software. Acostumado a utilizar ferramentas como Docker (Docker Compose para orquestração de containers, Dockerfile para imagens do DockerHub), K8S (Kubernetes), arquivos de extensão YML e YAML, Git, Github e GitHub Actions para criação de CI/CD Pipelines e Render para aplicações que precisem de um sistema de Back-End.
>
> Contexto
>
> Baseado no arquivo `Lab CICD-Pipeline.pdf`, você deverá implementar, criar uma pipeline de CI/CD utilizando Github Actions em um projeto desenvolvido e armazenado em um repositório no Github. Além disso, haverá fases de relatório do progresso da implementação da Pipeline.
>
> Regras
>
> Durante a implementação, você deverá seguir as seguintes regras:
>
> A pipeline deverá:
> - Executar a CI a cada `push` na branch `main`;
> - Executar a CI também em `pull_request` para a branch `main`;
> - Realizar o checkout do projeto;
> - Configurar o ambiente necessário para a aplicação;
> - Instalar as dependências, quando necessário;
> - Executar uma etapa de teste, validação ou verificação do projeto;
> - Realizar o CD após um `push` na `main`, somente se a CI for concluída com sucesso;
> - Realizar o Deployment da aplicação utilizando uma estratégia adequada ao projeto;
>
> Você deverá:
> - Entrar em uma branch separada da `main` (Ex.: `pipeline_cicd`) para realizar as modificações necessárias;
> - `commits` e `push` serão realizados manualmente por mim;
> - Criar a pipeline;
> - Explicar comandos e configurações (forma simples, porém, técnica);
> - Identificar erros;
> - Sugerir correções;
> - Modificar a pipeline;
> - Solucionar problemas encontrados durante a execução;
> - Qualquer modificação (seja inserção, alteração ou remoção) de algum arquivo, diretório. Solicitar aprovação antes de progredir, explicar o que vai fazer, o por que, as vantagens e desvantagens dessa modificação;
> - Os testes e validações devem ser realizados manualmente por mim (mas precisarei saber o que você fez para conseguir realizar as coisas da melhor forma possível);
> - Tão importante quanto o restante das regras, utilize todos os anexos para usar como referência e estudo (o Lab possui mais detalhes do que precisa ser feito, os pdfs 07 e 08 vão ajudar a entender melhor o CI, CD, Pipeline e Github Actions);
> - Para as partes que estão fora de seu alcance de edição, como o subir a aplicação no `Render`, me auxiliar me mostrando os passos a passos detalhadamente para subir a aplicação da maneira correta, pois nunca usei o `Render` para subir qualquer aplicação;
>
> O Deployment deverá:
> - Ser executado automaticamente após um `push` na branch `main`;
> - Ocorrer somente se as etapas de CI forem concluídas com sucesso;
> - Disponibilizar uma nova versão da aplicação em um ambiente acessível;
> - Ser realizado utilizando uma ferramenta ou serviço adequado ao projeto;
>
> Para subir nossa aplicação, iremos utilizar o `Render`, pois é uma ferramenta que eu já conheço, apesar de nunca ter usado antes, posso subir minha aplicação web que precisa de um sistema de back-end para funcionar corretamente, além disso, o banco de dados utilizado no projeto do github utiliza banco PostgreSQL em nuvem hospedado no Aiven. (salve exatamente essa justificativa para a `Saída`).
>
> Saída
>
> Quando tudo estiver funcionando na Pipeline CI/CD, você deverá criar um entregável em um arquivo PDF (Ex.: `relatorio_pipeline_cicd.pdf`) contendo os seguinte itens:
> - Link do repositório do projeto (Link no início do prompt);
> - Arquivo da pipeline (Path dentro do projeto);
> - Os prompts utilizados (Esse inclui na contagem de prompts utilizados. Também inclua apenas prompts que contribuam para o desenvolvimento da pipeline, prompts de pergunta não incluem nessa contagem);
> - Evidências da execução correta da pipeline CI/CD (alguns dos testes realizados manualmente);
>
> Na parte de prompts utilizados:
>
> A cada turno (uma pergunta minha e resposta sua. Novamente falando, apenas que contribuam para o desenvolvimento da pipeline), registrar o prompt utilizado, o resultado obtido, o problema encontrado, caso houver algum e a alteração realizada.
>
> E ao final de tudo:
> - Especificar quantos prompts (relevantes) foram necessários para chegar à versão final;
> - Quais foram os principais problemas encontrados;
> - Quais alterações foram necessárias;
> - Quais alterações foram realizadas manualmente;
> - Como você verificou que a pipeline estava funcionando corretamente;
> - Adicionar a `Justificativa` do uso do `Render`, exatamente como me posicionei nesse prompt.

Referências anexadas nesse pedido: 07 - CI - IntegracaoContinua.pdf; 08 - CI - GitAction_Docker.pdf; Lab CICD-Pipeline.pdf. Foram usados como material de referência, não como autorização para enviar o trabalho ao formulário mencionado no laboratório.

Resultado: análise dos três PDFs e do repositório remoto; identificação de Java 11, Maven, WAR, Servlets/JSP; proposta de Docker/Tomcat/Render e solicitação de aprovação.
Problema: credencial Aiven versionada, ausência de pipeline e ausência de testes na árvore inspecionada.
Alteração realizada: nenhuma; apenas análise e proposta.

### Esclarecimento excluído da contagem

> pq vai clonar o projeto em outro diretório se eu já possuo o projeto na minha máquina e fazer apenas um `git checkout -b`?

Resultado: clonagem dispensada; solicitado o caminho da cópia existente. Nenhuma alteração de arquivo.

### Prompt relevante 2 - autorização e caminho local

> Eu aprovo as suas propostas, apenas da clonagem que será substituída apenas para criar uma branch no meu repositório local que está localizado aqui:
>
> `C:\Users\caiomachado-ieg\OneDrive - Instituto J&F\Área de Trabalho\Outras_Coisas\mini_mini_inter`

Resultado: branch pipeline_cicd criada; arquivos aprovados implementados; guia manual de configuração e testes preparado.
Problemas: acesso de escrita inicialmente restrito e criação da branch inicialmente bloqueada nos metadados .git. Resolvidos mediante concessão de acesso à pasta e aos metadados do Git. Alteração preexistente de IDE preservada.
Alterações realizadas: criação de .github/workflows/cicd.yml, Dockerfile, .dockerignore e docs/pipeline_cicd.md; atualização de Conexao.java para variáveis de ambiente e erros explícitos. Nenhum commit, push ou teste foi executado pelo assistente.

### Prompt relevante 3 - falha do Maven Wrapper no Windows

> Quando rodei esse código, retornou isso:

O comando selecionado era `.\mvnw.cmd --batch-mode --no-transfer-progress verify`. A saída mostrava `'F' não é reconhecido`, o caminho truncado em `OneDrive - Instituto J` e `ClassNotFoundException: org.apache.maven.wrapper.MavenWrapperMain`.

- **Resultado e problema**. O Wrapper parava antes da compilação. O `cmd.exe` interpretava o `&` de `Instituto J&F` como separador em atribuições sem aspas.
- **Alteração**. Após a autorização do usuário, foram colocadas aspas em seis atribuições de caminho no `backend/demo/mvnw.cmd`. O usuário repetiu o comando e enviou o `BUILD SUCCESS` no prompt 6.

### Prompt relevante 4 - Dockerfile não encontrado

> Agora deu problema quando rodei esse comando e devolveu isso:

O usuário executou `docker build --tag mini-mini-inter:local .` em `backend/demo` e recebeu `failed to read dockerfile`.

- **Resultado e problema**. O ponto indicava a pasta atual como contexto. O Dockerfile fica na raiz do repositório.
- **Alteração**. A orientação de teste passou a explicitar a volta à raiz com `cd ..\..` antes de repetir o build. A correção foi no comando executado pelo usuário.

### Prompt relevante 5 - resultado do teste em vídeo

> Gravei tanto o vídeo, quanto tirei alguns prints desse teste, pois é melhor ter duas formas na hora de colocar no pdf final. Esse vídeo mostra o teste funcionando corretamente

- **Resultado**. O usuário informou que o teste local com Docker funcionou e anexou `teste_docker_1.mp4`.
- **Problema e alteração**. Nenhum erro novo foi relatado. A gravação foi registrada como evidência complementar informada pelo usuário.

### Prompt relevante 6 - capturas dos testes locais

> Aqui estão os prints do teste

- **Resultado**. As capturas mostram Java 19.0.2 no terminal, `BUILD SUCCESS` no Maven, 17 de 17 etapas no build Docker, criação do contêiner e a página de login.
- **Problema e alteração**. A captura da página não mostrava o endereço. O usuário a complementou no turno seguinte. O Java 19 observado no terminal foi registrado separadamente do Java 11 configurado na CI e na compilação Docker.

### Prompt relevante 7 - página local com URL e CI do pull request

> Aqui está a nova imagem da aplicação rodando com a URL aparecendo, ao lado estão os prints do CI/CD no github

- **Resultado**. A página abriu em `127.0.0.1:8080`. No [PR #1](https://github.com/caiomezini/mini-mini-inter/pull/1) de `pipeline_cicd` para `main`, a CI passou e o CD ficou `Skipped`, como previsto.
- **Problema e alteração**. Nenhuma falha nova. O usuário foi orientado a fazer o merge para verificar o gatilho `push` na `main`.

### Prompt relevante 8 - primeira execução na main

> Novas prints

- **Resultado**. O merge disparou a [execução #2](https://github.com/caiomezini/mini-mini-inter/actions/runs/42535739726) para `8f67e3a`. A CI passou e o CD iniciou em seguida.
- **Problema**. A primeira tentativa parou antes de chamar o Render. O log mostrava `RENDER_API_KEY`, `RENDER_SERVICE_ID` e `RENDER_SERVICE_URL` vazias.
- **Alteração**. O usuário recebeu o passo a passo para criar o Web Service, configurar o segredo e as variáveis no GitHub e reexecutar o CD. A solução foi de configuração.

### Prompt relevante 9 - evidências da publicação no Render e do CD

> Prints e vídeos das etapas recentes

- **Resultado**. As capturas mostram a criação do serviço, um deploy pelo painel e a segunda tentativa da execução #2. O GitHub Actions informou deploy `live` para o commit `8f67e3ae50e4d422089667eee8376b0a0d378ffc` e resposta HTTP 200 com a página esperada. O usuário também anexou `teste_render_2.mp4` e `teste_cd_3.mp4` como material complementar.
- **Problema**. Foi preciso reexecutar o CD após cadastrar as configurações que faltavam na tentativa inicial.
- **Alteração**. O usuário configurou o serviço e os valores necessários e reexecutou o job. Após aprovação, o guia passou a registrar esses resultados e a preparação do próximo PR de documentação.

## Justificativa do Render - texto original para o PDF

Para subir nossa aplicação, iremos utilizar o `Render`, pois é uma ferramenta que eu já conheço, apesar de nunca ter usado antes, posso subir minha aplicação web que precisa de um sistema de back-end para funcionar corretamente, além disso, o banco de dados utilizado no projeto do github utiliza banco PostgreSQL em nuvem hospedado no Aiven.

## Evidências verificadas

| Etapa | Resultado | Capturas de 08/11/2026 |
| --- | --- | --- |
| Maven local | `BUILD SUCCESS` após a correção do Wrapper | 200139 |
| Docker local | Build 17/17, contêiner criado e login em `127.0.0.1:8080` | 200940, 201106 e 202119 |
| PR #1 | CI aprovada e CD ignorado | 202717 e 203045 |
| Push na main, tentativa 1 | CI aprovada e CD iniciado. Faltavam os valores de configuração do Render | 203349, 203450 e 203506 |
| Preparação no Render | Web Service criado e deploy pelo painel ficou `Live` para `8f67e3a` | 204503, 204753 e 204843 |
| Push na main, tentativa 2 | Reexecução manual do CD. A pipeline publicou o commit validado e verificou a página com HTTP 200 | 205814 e 205951 |

- Pull request: https://github.com/caiomezini/mini-mini-inter/pull/1
- Execução do GitHub Actions: https://github.com/caiomezini/mini-mini-inter/actions/runs/42535739726
- Aplicação publicada: https://mini-mini-inter.onrender.com

## Fechamento pendente

- Publicar a atualização deste guia por PR e fazer o merge na `main`. Guardar o link da nova execução com CI e CD aprovados na primeira tentativa, sem acionar `Re-run jobs` ou `Manual Deploy`.
- Verificar cadastro, login e logout com dados fictícios no serviço publicado. Registrar o resultado e preservar capturas sem credenciais.
- Confirmar quais configurações manuais do Aiven foram aplicadas, inclusive a troca da senha anteriormente exposta, sem registrar valores.
- Finalizar o PDF com os resultados observados, os prompts relevantes, o caminho da pipeline e a justificativa original. Usar as capturas para documentar a sequência. Registrar os vídeos como material complementar.

## Documentação técnica consultada

- GitHub Actions, eventos: https://docs.github.com/en/actions/reference/workflows-and-actions/events-that-trigger-workflows
- Actions checkout e setup-java: https://github.com/actions/checkout e https://github.com/actions/setup-java
- Render Docker: https://render.com/docs/docker
- Render Web Services e porta: https://render.com/docs/web-services
- Render Auto-Deploy: https://render.com/docs/deploys
- Render API e API Keys: https://render.com/docs/api
- Render API criar/consultar deploy: https://api-docs.render.com/reference/create-deploy e https://api-docs.render.com/reference/retrieve-deploy
- Render deploy hooks: https://render.com/docs/deploy-hooks
