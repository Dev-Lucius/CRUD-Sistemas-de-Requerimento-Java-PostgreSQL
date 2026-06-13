package apresentacao;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinMustache;
import negocio.Aluno;
import negocio.Anexo;
import negocio.Curso;
import negocio.Requerimento;
import negocio.Usuario;
import persistencia.AlunoDAO;
import persistencia.AnexoDAO;
import persistencia.CursoDAO;
import persistencia.RequerimentoDAO;
import persistencia.UsuarioDAO;

public class Main {

    /**
     * Admin (Acesso à tudo) --> usuarioBancoAtual = "usuario_admin";
     * Leitor (Acesso à Leitura) --> usuarioBancoAtual = "usuario_leitura";
     * A senha para ambos é a mesma --> senhaBancoAtual = 123
     */
    // Exemplo de Usuário postgres 
    public static String usuarioBancoAtual = "postgres";
    public static String senhaBancoAtual = "12345";

    public static void main(String[] args) {

        Javalin.create(config -> {

            config.fileRenderer(new JavalinMustache());

            // ════════════════════════════════════════════════════════════════
            // INDEX
            // ════════════════════════════════════════════════════════════════

            config.routes.get("/", ctx -> {
                Map<String, Object> map = new HashMap<>();
                map.put("vetCurso", new CursoDAO().listarCursos());
                ctx.render("/templates/index.html", map);
            });

            // ════════════════════════════════════════════════════════════════
            // CURSO — /templates/curso/
            // ════════════════════════════════════════════════════════════════

            config.routes.get("/curso", ctx -> {
                Map<String, Object> map = new HashMap<>();
                map.put("vetCurso", new CursoDAO().listarCursos());
                ctx.render("/templates/curso/index.html", map);
            });

            config.routes.get("/curso/tela_adicionar", ctx -> {
                ctx.render("/templates/curso/tela_adicionar.html");
            });

            config.routes.post("/curso/adicionar", ctx -> {
                String nome = ctx.formParam("nome");
                String site = ctx.formParam("site");
                String turno = ctx.formParam("turno");
                int duracao = Integer.parseInt(ctx.formParam("duracao"));

                Curso curso = new Curso(nome, site, Curso.Turno.fromString(turno), duracao);

                if (new CursoDAO().inserirCurso(curso)) {
                    ctx.redirect("/curso");
                } else {
                    Map<String, Object> map = new HashMap<>();
                    map.put("curso", curso);
                    ctx.render("/templates/curso/tela_adicionar.html", map);
                }
            });

            config.routes.get("/curso/tela_alterar/{id}", ctx -> {
                Curso curso = new CursoDAO().buscarPorId(
                        Integer.parseInt(ctx.pathParam("id")));
                Map<String, Object> map = new HashMap<>();
                map.put("curso", curso);
                ctx.render("/templates/curso/tela_alterar.html", map);
            });

            config.routes.post("/curso/alterar", ctx -> {
                int id = Integer.parseInt(ctx.formParam("id"));
                String nome = ctx.formParam("nome");
                String site = ctx.formParam("site");
                String turno = ctx.formParam("turno");
                int duracao = Integer.parseInt(ctx.formParam("duracao"));

                Curso curso = new Curso(id, nome, site, Curso.Turno.fromString(turno), duracao);

                if (new CursoDAO().atualizarCurso(curso)) {
                    ctx.redirect("/curso");
                } else {
                    Map<String, Object> map = new HashMap<>();
                    map.put("curso", curso);
                    ctx.render("/templates/curso/tela_alterar.html", map);
                }
            });

            config.routes.post("/curso/excluir/{id}", ctx -> {
                new CursoDAO().deletarCurso(Integer.parseInt(ctx.pathParam("id")));
                ctx.redirect("/curso");
            });

            // ════════════════════════════════════════════════════════════════
            // USUARIO — /templates/usuario/
            // ════════════════════════════════════════════════════════════════

            config.routes.get("/usuario", ctx -> {
                Map<String, Object> map = new HashMap<>();
                map.put("vetUsuario", new UsuarioDAO().listarUsuarios());
                ctx.render("/templates/usuario/index.html", map);
            });

            config.routes.get("/usuario/tela_adicionar", ctx -> {
                ctx.render("/templates/usuario/tela_adicionar.html");
            });

            config.routes.post("/usuario/adicionar", ctx -> {
                String nome = ctx.formParam("nome");
                String email = ctx.formParam("email");
                String cpf = ctx.formParam("cpf");
                String cep = ctx.formParam("cep");
                String rua = ctx.formParam("rua");
                String complemento = ctx.formParam("complemento");
                LocalDate date = LocalDate.parse(ctx.formParam("data_nascimento"));

                Usuario usuario = new Usuario(0, nome, email, cpf, date, cep, complemento, rua);
                if (new UsuarioDAO().inserirUsuario(usuario)) {
                    ctx.redirect("/usuario");
                } else {
                    Map<String, Object> map = new HashMap<>();
                    map.put("usuario", usuario);
                    ctx.render("/templates/usuario/tela_adicionar.html", map);
                }
            });

            config.routes.get("/usuario/tela_alterar/{id}", ctx -> {
                Usuario usuario = new UsuarioDAO().buscarPorId(
                        Integer.parseInt(ctx.pathParam("id")));
                Map<String, Object> map = new HashMap<>();
                map.put("usuario", usuario);
                ctx.render("/templates/usuario/tela_alterar.html", map);
            });

            config.routes.post("/usuario/alterar", ctx -> {
                int id = Integer.parseInt(ctx.formParam("id"));
                String nome = ctx.formParam("nome");
                String email = ctx.formParam("email");
                String cpf = ctx.formParam("cpf");
                String cep = ctx.formParam("cep");
                String rua = ctx.formParam("rua");
                String complemento = ctx.formParam("complemento");
                LocalDate date = LocalDate.parse(ctx.formParam("data_nascimento"));

                Usuario usuario = new Usuario(id, nome, email, cpf, date, cep, complemento, rua);

                if (new UsuarioDAO().atualizarUsuario(usuario)) {
                    ctx.redirect("/usuario");
                } else {
                    Map<String, Object> map = new HashMap<>();
                    map.put("usuario", usuario);
                    ctx.render("/templates/usuario/tela_alterar.html", map);
                }
            });

            // 6. Excluir Usuário (Alterado para GET pois no HTML usamos uma tag <a>)
            config.routes.post("/usuario/excluir/{id}", ctx -> {
                new UsuarioDAO().deletarUsuario(Integer.parseInt(ctx.pathParam("id")));
                ctx.redirect("/usuario");
            });

            // ════════════════════════════════════════════════════════════════
            // ALUNO — /templates/aluno/
            // ════════════════════════════════════════════════════════════════
            // 1. Listar Todos os Alunos
            config.routes.get("/aluno", ctx -> {
                Map<String, Object> map = new HashMap<>();
                map.put("vetAluno", new AlunoDAO().listarTodos());
                ctx.render("/templates/aluno/index.html", map);
            });

            // 2. Listar Alunos por Curso (Filtragem exigida pelo trabalho)
            config.routes.get("/aluno/curso/{id}", ctx -> {
                int idDoCurso = Integer.parseInt(ctx.pathParam("id"));
                Map<String, Object> map = new HashMap<>();
                map.put("vetAluno", new AlunoDAO().listarPorCurso(idDoCurso));
                // Reaproveita a mesma tela de listagem!
                ctx.render("/templates/aluno/index.html", map);
            });

            // 3. Tela de Adicionar Aluno (Carrega Usuários e Cursos para os Selects)
            config.routes.get("/aluno/tela_adicionar", ctx -> {
                Map<String, Object> map = new HashMap<>();
                map.put("usuarios", new UsuarioDAO().listarUsuarios());
                map.put("cursos", new CursoDAO().listarCursos());
                ctx.render("/templates/aluno/tela_adicionar.html", map);
            });

            // 4. Ação de Adicionar Aluno no Banco (Recebe o POST do formulário)
            config.routes.post("/aluno/adicionar", ctx -> {
                String matricula = ctx.formParam("matricula");
                int usuarioId = Integer.parseInt(ctx.formParam("usuario_id"));
                int cursoId = Integer.parseInt(ctx.formParam("curso_id"));

                // Busca os objetos reais do banco para não disparar validações de "novo
                // usuario"
                Usuario u = new UsuarioDAO().buscarPorId(usuarioId); // Ou o nome do seu método de busca
                negocio.Curso c = new CursoDAO().buscarPorId(cursoId);

                if (u != null && c != null) {
                    new AlunoDAO().InserirAlunos(new Aluno(matricula, u, c));
                }

                ctx.redirect("/aluno");
            });

            // 5. Tela de Alterar Curso do Aluno
            config.routes.get("/aluno/tela_alterar/{matricula}", ctx -> {
                Aluno aluno = new AlunoDAO().BuscarPorMatricula(ctx.pathParam("matricula"));
                Map<String, Object> map = new HashMap<>();
                map.put("aluno", aluno);
                ctx.render("/templates/aluno/tela_alterar.html", map);
            });

            // 6. Ação de Alterar Curso no Banco (Recebe o POST do formulário)
            config.routes.post("/aluno/alterar", ctx -> {
                String matricula = ctx.formParam("matricula");
                int novoCursoId = Integer.parseInt(ctx.formParam("curso_id"));

                new AlunoDAO().atualizarCurso(matricula, novoCursoId);
                ctx.redirect("/aluno");
            });

            // 7. Ação de Excluir Aluno
            // No seu Main.java, dentro das configurações de rotas:
            config.routes.get("/aluno/excluir/{matricula}", ctx -> {
                // 1. Captura o valor que veio na URL (ex: 2024000002)
                String mat = ctx.pathParam("matricula");

                // 2. Instancia o DAO e chama o método de exclusão
                AlunoDAO dao = new AlunoDAO();
                dao.deletarAluno(mat);

                // 3. Manda o usuário de volta para a lista atualizada
                ctx.redirect("/aluno");
            });

            config.routes.get("/aluno/perfil/{matricula}", ctx -> {
                String matricula = ctx.pathParam("matricula");

                // 1. Busca o aluno no banco de dados para ter as informações dele
                Aluno aluno = new AlunoDAO().BuscarPorMatricula(matricula);

                if (aluno != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("aluno", aluno);

                    // CORREÇÃO: Informando o caminho real do diretório onde o seu HTML corrigido
                    // está!
                    ctx.render("/templates/aluno/tela_alterar.html", map);
                } else {
                    ctx.status(404).result("Aluno não encontrado.");
                }
            });

            // ROTA 1: POST para salvar ou realterar a foto
            config.routes.post("/aluno/foto/upload", ctx -> {
                String matricula = ctx.formParam("matricula");
                io.javalin.http.UploadedFile fotoEnv = ctx.uploadedFile("foto_perfil");

                if (fotoEnv != null && !fotoEnv.filename().isEmpty()) {
                    try (java.io.InputStream is = fotoEnv.content()) {
                        byte[] bytesFoto = is.readAllBytes();
                        String nomeArquivo = fotoEnv.filename();
                        String contentType = fotoEnv.contentType();

                        new persistencia.FotoAlunoDAO().salvarOuAtualizarFoto(matricula, nomeArquivo, contentType,
                                bytesFoto);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                ctx.redirect("/aluno/perfil/" + matricula);
            });

            // ROTA 2: GET que serve a imagem diretamente para o HTML
            config.routes.get("/aluno/foto/{matricula}", ctx -> {
                String matricula = ctx.pathParam("matricula");
                byte[] fotoBytes = new persistencia.FotoAlunoDAO().buscarFotoPorMatricula(matricula);

                if (fotoBytes != null) {
                    // Define o cabeçalho como imagem para o navegador renderizar em vez de baixar
                    ctx.contentType("image/jpeg");
                    ctx.result(fotoBytes);
                } else {
                    // Se o aluno não tiver foto, redireciona para uma imagem de avatar padrão do
                    // seu projeto
                    ctx.redirect("/img/avatar-padrao.png");
                }
            });

            // Rota GET para processar a exclusão de um anexo
            // e logo em seguida, redirecionar o usuário de volta para a tela de perfil
            config.routes.get("/aluno/foto/excluir/{matricula}", ctx -> {
                String matricula = ctx.pathParam("matricula");

                // Executa a exclusão no banco
                new persistencia.FotoAlunoDAO().deletarFoto(matricula);

                // Redireciona de volta para a tela de perfil do aluno
                ctx.redirect("/aluno/perfil/" + matricula);
            });

            // ════════════════════════════════════════════════════════════════
            // REQUERIMENTO — /templates/requerimento/
            // ════════════════════════════════════════════════════════════════

            config.routes.get("/requerimento", ctx -> {
                Map<String, Object> map = new HashMap<>();
                map.put("vetRequerimento", new RequerimentoDAO().listarTodosRequerimentos());
                ctx.render("/templates/requerimento/index.html", map);
            });

            config.routes.get("/requerimento/tela_alterar/{id}", ctx -> {
                int idReq = Integer.parseInt(ctx.pathParam("id"));

                // Busca o requerimento correspondente
                Requerimento req = new RequerimentoDAO().buscarPorId(idReq);

                // Busca os anexos que pertencem a este requerimento
                java.util.List<negocio.Anexo> anexos = new AnexoDAO().listarPorRequerimento(idReq);

                Map<String, Object> map = new HashMap<>();
                map.put("requerimento", req);
                map.put("listaAnexos", anexos); // Alimenta a tag {{#listaAnexos}} do seu HTML

                ctx.render("/templates/requerimento/tela_alterar.html", map);
            });

            config.routes.post("/requerimento/alterar", ctx -> {
                int id = Integer.parseInt(ctx.formParam("id"));
                String status = ctx.formParam("status");

                new RequerimentoDAO().atualizarStatus(
                        id,
                        Requerimento.Status.fromString(status));
                ctx.redirect("/requerimento");
            });

            config.routes.get("/requerimento/excluir/{id}", ctx -> {
                new RequerimentoDAO().deletarRequerimento(
                        Integer.parseInt(ctx.pathParam("id")));
                ctx.redirect("/requerimento");
            });

            // Rota para mostrar a tela de cadastro
            config.routes.get("/requerimento/tela_adicionar", ctx -> {
                ctx.render("/templates/requerimento/tela_adicionar.html");
            });

            // Rota para processar o formulário (usa o método que criamos no DAO)
            config.routes.post("/requerimento/abrir", ctx -> {
                String matricula = ctx.formParam("aluno_matricula");
                int tipoId = Integer.parseInt(ctx.formParam("tipo_id"));
                String obs = ctx.formParam("observacao");

                // 1. Salva o requerimento no banco e retorna o ID gerado pelo SERIAL
                int idRequerimentoGerado = new RequerimentoDAO().abrirRequerimento(matricula, tipoId, obs);

                // 2. Captura o arquivo enviado pelo formulário HTML
                io.javalin.http.UploadedFile arquivoEnv = ctx.uploadedFile("documento_anexo");

                // Verifica se o usuário de fato enviou um arquivo válido
                if (arquivoEnv != null && !arquivoEnv.filename().isEmpty()) {
                    try {
                        String nomeOriginal = arquivoEnv.filename();
                        String tipoMime = arquivoEnv.contentType();
                        long tamanhoBytes = arquivoEnv.size();
                        byte[] conteudoArquivo = arquivoEnv.content().readAllBytes();

                        // Monta a string JSON para guardar os metadados no PostgreSQL
                        String descricaoJson = String.format(
                                "{\"nome_arquivo\": \"%s\", \"conteudo_tipo\": \"%s\", \"tamanho\": %d}",
                                nomeOriginal, tipoMime, tamanhoBytes);

                        // 1. Instancia um Curso válido (Nome, Site, Turno, Duração > 0)
                        negocio.Curso cursoFake = new negocio.Curso(
                                1, "Curso Geral", "http://site.com", negocio.Curso.Turno.NOTURNO, 2000);

                        // 2. Instancia um Usuário válido (Campos obrigatórios preenchidos e válidos)
                        negocio.Usuario userFake = new negocio.Usuario(
                                0, "Aluno Temp", "valido@email.com", "00000000000",
                                java.time.LocalDate.of(2000, 1, 1), "00000000", "", "");

                        // 3. Instancia o Aluno passando a matrícula e as dependências criadas acima
                        negocio.Aluno alunoFake = new negocio.Aluno(matricula, userFake, cursoFake);

                        // 4. Instancia o TipoRequerimento
                        negocio.TipoRequerimento tipoFake = new negocio.TipoRequerimento(tipoId, "Tipo");

                        // 5. Agora sim! Montamos o Requerimento Raso sem estourar nenhuma validação do
                        // domínio
                        Requerimento reqRaso = new Requerimento(
                                idRequerimentoGerado, alunoFake, tipoFake, Requerimento.Status.PENDENTE,
                                java.time.LocalDateTime.now());

                        // 6. Envelopa tudo no Anexo de negócio
                        negocio.Anexo novoAnexo = new negocio.Anexo(descricaoJson, conteudoArquivo, reqRaso);

                        // 7. Salva o anexo de verdade no Banco de Dados
                        new AnexoDAO().inserirAnexo(novoAnexo);

                    } catch (Exception e) {
                        System.err.println("❌ Erro crítico ao processar e salvar o anexo no banco:");
                        e.printStackTrace(); // Se algo ainda falhar, agora veremos o motivo real no console do Java
                    }
                }

                // Redireciona de volta para a listagem principal
                ctx.redirect("/requerimento");
            });

            // Tela para abrir o trancamento
            config.routes.get("/requerimento/tela_trancamento", ctx -> {
                Map<String, Object> map = new HashMap<>();
                map.put("alunos", new AlunoDAO().listarTodos()); // Para listar os alunos no select
                ctx.render("/templates/requerimento/tela_trancamento.html", map);
            });

            // Ação de salvar o trancamento no banco
            config.routes.post("/requerimento/trancamento", ctx -> {
                String matricula = ctx.formParam("aluno_matricula");
                String semestre = ctx.formParam("semestre_retorno");

                Aluno aluno = new AlunoDAO().BuscarPorMatricula(matricula);

                if (aluno != null) {
                    // 1. Criamos ou buscamos o Tipo de Requerimento (ajuste o ID '3' conforme o seu
                    // banco de dados)
                    // Se você tiver um TipoRequerimentoDAO, use ele para buscar o tipo
                    // "Trancamento":
                    negocio.TipoRequerimento tipoRequerimento = new persistencia.TipoRequerimentoDAO().buscarPorId(3);

                    // NOTA: Se no seu projeto o TipoRequerimento não for uma classe com DAO, mas
                    // sim um ENUM, mude para:
                    // negocio.TipoRequerimento tipoRequerimento =
                    // negocio.TipoRequerimento.TRANCAMENTO;

                    // 2. Instanciamos o requerimento passando tudo de uma vez no construtor
                    negocio.RequerimentoTrancamento rt = new negocio.RequerimentoTrancamento(aluno, tipoRequerimento,
                            semestre);

                    // 3. O setSemestreRetorno também pode ser apagado, pois o construtor já guardou
                    // o 'semestre'
                    // rt.setSemestreRetorno(semestre); // Não é mais necessário!

                    // 4. Salva no banco de dados via Herança
                    new persistencia.RequerimentoDAO().inserirTrancamento(rt);
                }

                ctx.redirect("/requerimento"); // Redireciona para a lista geral
            });

            config.routes.get("/anexo/buscar", ctx -> {
                String mimeType = ctx.queryParam("mime_type");
                List<Anexo> anexosFiltrados = new java.util.ArrayList<>();

                // Se o usuário selecionou algum filtro, chama o método JSONB do DAO
                if (mimeType != null && !mimeType.isBlank()) {
                    anexosFiltrados = new persistencia.AnexoDAO().listarPorMimeType(mimeType);
                }

                Map<String, Object> map = new HashMap<>();
                map.put("anexos", anexosFiltrados);
                map.put("mimeTypeSelecionado", mimeType);

                // Renderiza a nova tela dentro da pasta de anexos
                ctx.render("/templates/anexo/buscar.html", map);
            });

            config.routes.get("/anexo/excluir/{id}", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));

                // Executa a exclusão
                new persistencia.AnexoDAO().deletarAnexo(id);

                // Captura o filtro atual para não perder a pesquisa do usuário ao recarregar
                String mimeType = ctx.queryParam("mime_type");
                if (mimeType != null) {
                    ctx.redirect("/anexo/buscar?mime_type=" + mimeType);
                } else {
                    ctx.redirect("/anexo/buscar");
                }
            });

            config.routes.get("/anexo/download/{id}", ctx -> {
                int idAnexo = Integer.parseInt(ctx.pathParam("id"));
                persistencia.AnexoDAO anexoDAO = new persistencia.AnexoDAO();
                negocio.Anexo anexo = anexoDAO.buscarPorId(idAnexo);

                if (anexo != null && anexo.temArquivo()) {
                    ctx.contentType("application/octet-stream");
                    ctx.header("Content-Disposition", "attachment; filename=\"anexo_" + idAnexo + ".pdf\"");
                    ctx.result(anexo.getArquivo());
                } else {
                    ctx.status(404).result("Arquivo nao encontrado.");
                }
            });

            // Rota para virar Admin
            config.routes.get("/login/admin", ctx -> {
                Main.usuarioBancoAtual = "usuario_admin";
                ctx.redirect("/requerimento"); // Recarrega a página
            });

            // Rota para virar apenas Leitura
            config.routes.get("/login/leitura", ctx -> {
                Main.usuarioBancoAtual = "usuario_leitura";
                ctx.redirect("/requerimento"); // Recarrega a página
            });

        }).start(7000);
    }
}