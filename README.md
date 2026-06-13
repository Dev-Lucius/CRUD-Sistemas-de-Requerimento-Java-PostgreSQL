# 📜 Sistema de Gestão de Requerimentos Acadêmicos
> **Tecnologias:** Java 21 | PostgreSQL | JDBC (Camada DAO) | Javalin 7 (Moustache Templates)

---

## 📖 Visão Geral do Projeto
Este projeto consiste em uma aplicação web robusta voltada para o gerenciamento de requerimentos acadêmicos. O objetivo principal é demonstrar a integração de conceitos avançados de bancos de dados relacionais e orientados a objetos em uma aplicação real utilizando a arquitetura em camadas (Apresentação, Negócio e Persistência).

---

## 🚀 Recursos Avançados Implementados (Escopo do Trabalho)

O sistema foi blindado e construído seguindo rigorosamente os critérios técnicos exigidos:

### 1. 🧬 Herança no Banco de Dados (`INHERITS`)
* **Conceito:** Modelagem de herança física no PostgreSQL unida à herança de classes no Java.
* **Aplicação:** A classe `RequerimentoTrancamento` estende a classe pai `Requerimento`. No banco, a tabela `requerimento_trancamento` herda dinamicamente todos os atributos da tabela pai `requerimento`, adicionando isoladamente o campo específico `semestre_retorno`.

### 2. 🗂️ Manipulação de Arquivos e Binários (`BYTEA`)
* **Conceito:** Persistência e recuperação de arquivos diretamente no banco de dados.
* **Aplicação:** Upload, armazenamento, renderização em tela e exclusão de comprovantes/anexos utilizando o tipo de dado `BYTEA` via fluxos de bytes JDBC (`InputStream`).

### 3. 🔍 Consultas Avançadas em Dados Semiestruturados (`JSONB`)
* **Conceito:** Armazenamento e indexação de dados dinâmicos em formato JSON de alta performance.
* **Aplicação:** Filtros de buscas complexas e armazenamento de metadados dos históricos dos requerimentos utilizando operadores nativos do PostgreSQL para JSONB.

### 🔐 4. Controle de Acesso e Segurança Dinâmica (`DCL`)
O sistema integra as políticas de privilégios de usuários do banco de dados diretamente à sessão da aplicação Java:
* **`usuario_admin`**: Possui privilégios totais (`GRANT ALL PRIVILEGES`) sobre as tabelas do esquema. Consegue criar, alterar e excluir registros.
* **`usuario_leitura`**: Restrito estritamente a consultas (`GRANT SELECT`). Qualquer tentativa de escrita disparará uma quebra de restrição nativa tratada pela aplicação.

---

## 💾 Scripts de Banco de Dados (DCL)
Para testar o controle de acesso dinâmico, execute os seguintes comandos no terminal do PostgreSQL (`psql`):

```sql
-- Criando os usuários do sistema
CREATE USER usuario_admin WITH PASSWORD '123';
CREATE USER usuario_leitura WITH PASSWORD '123';

-- Concedendo os privilégios correspondentes
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO usuario_admin;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO usuario_leitura;
```

--- 

Para Validar os Acessos via Terminal do Sistema Operacional

```bash
psql -U [nome_do_usuario] -d sistema_requerimento_completo -h localhost
```

--- 

## 📂 Estrutura Arquitetural do Projeto

O projeto adota o padrão de projeto DAO (Data Access Object) estruturado nas seguintes camadas de software:

```bash
src/main/java/
├── apresentacao/
│   └── Main.java                 # Configuração do servidor Javalin, rotas e tratamento de exceções
├── negocio/
│   ├── Aluno.java                # Entidade Aluno
│   ├── Curso.java                # Entidade Curso
│   ├── Requerimento.java         # Entidade Pai (Requerimentos gerais)
│   ├── RequerimentoTrancamento.java # Entidade Filha (Específica para trancamentos)
│   ├── Status.java               # Enum contendo as regras de status amigáveis
│   └── TipoRequerimento.java     # Entidade de catálogo de tipos
└── persistencia/
    ├── ConexaoPostgreSQL.java    # Fábrica de conexões JDBC com chaveamento dinâmico de usuário
    ├── AlunoDAO.java             # Persistência de alunos
    ├── RequerimentoDAO.java      # CRUD com suporte a JSONB, BYTEA e Herança SQL
    └── TipoRequerimentoDAO.java  # Catálogo de tipos de requerimento
```

--- 

## 🛠️ Pré-requisitos e Instalação

### ⚙️ Requisitos de Ambiente

- Java JDK instalado (Mínimo v17, recomendado v21+)
- Apache Maven instalado e configurado nas variáveis de ambiente
- PostgreSQL Server rodando na porta 5432

### 📥 Executando a Aplicação localmente

### 📥 Executando a Aplicação Localmente

1. Clone o repositório ou extraia o código-fonte:
   ```bash
   git clone [https://github.com/Dev-Lucius/CRUD-Sistemas-de-Requerimento-Java-PostgreSQL](https://github.com/Dev-Lucius/CRUD-Sistemas-de-Requerimento-Java-PostgreSQL)
   ```

- 2. Certifique-se de que a estrutura do banco de dados sistema_requerimento_completo está criada e os usuários DCL foram gerados.

- 3. Abra o terminal na raiz do projeto (onde se encontra o arquivo pom.xml) e execute o comando para baixar as dependências e compilar:

```bash
cd demo

mvn clean install 

mvn exec:java
```
> A classe já está configurada no arquivo ```pom.xml``` via **exec-maven-plugin**, logo é possível usar o comando simplificado

- 4. Executando a classe Principal, o servidor web inciará automaticamente:
    * 🌐 Acesse em: ```http://localhost:7000/```

--- 

## 🎯 Instruções para a Banca de Avaliação (Apresentação do Projeto)

Durante a demonstração prática, execute os seguintes passos de validação:

- 1. Acesse a Listagem Principal: Veja os requerimentos polimórficos listados na mesma tabela (incluindo o Trancamento via Herança).

- 2. Teste o Botão de Alternância de Perfil (DCL):

    * Mude o perfil para Simular Leitura e tente excluir ou alterar um status. O sistema interceptará o erro nativo do Postgres (permissão negada) e exibirá um alerta em tela sem derrubar o servidor.

    * Mude para Simular Admin e repita o processo para provar o sucesso do commit.

- 3. Teste do BYTEA: Adicione um comprovante digitalizado, verifique sua renderização e delete para atestar o fluxo binário.