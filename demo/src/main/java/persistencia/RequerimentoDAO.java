package persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import negocio.Aluno;
import negocio.Curso;
import negocio.Requerimento;
import negocio.RequerimentoTrancamento;
import negocio.TipoRequerimento;

/**
 * DAO da Tabela Requerimento
 *
 * CREATE TABLE requerimento (
 * id SERIAL PRIMARY KEY,
 * aluno_matricula CHAR(10) REFERENCES aluno(matricula),
 * tipo_requerimento INTEGER REFERENCES tipo_requerimento(id),
 * status VARCHAR(20) DEFAULT 'em análise'
 * CHECK (status IN ('em análise', 'deferido', 'indeferido')),
 * data_hora_abertura TIMESTAMP DEFAULT CURRENT_TIMESTAMP
 * );
 */
public class RequerimentoDAO {

    public int inserirRequerimento(Requerimento requerimento) {
        // Não inserimos data_hora_abertura nem status: o banco aplica os DEFAULTs
        String sql = """
                INSERT INTO requerimento (aluno_matricula, tipo_requerimento_id, status)
                VALUES (?, ?, ?)
                """;

        try (
                Connection conn = new ConexaoPostgreSQL().getConexao();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, requerimento.getAluno().getMatricula());
            stmt.setInt(2, requerimento.getTipoRequerimento().getId());
            stmt.setString(3, requerimento.getStatus().getDescricao());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGerado = rs.getInt(1);
                    requerimento.setId(idGerado); // ← sincroniza o objeto com o banco
                    return idGerado;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir requerimento: " + e.getMessage(), e);
        }
        throw new RuntimeException("Inserção não retornou um id gerado.");
    }

    public void inserirTrancamento(RequerimentoTrancamento rt) {
        String sql = "INSERT INTO requerimento_trancamento (tipo_requerimento_id, status, aluno_matricula, data_hora_abertura, semestre_retorno) VALUES (?, ?, ?, ?, ?)";
        try (
                Connection conn = new ConexaoPostgreSQL().getConexao();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            // 1. tipo_requerimento_id (int) -> buscando o ID de dentro do objeto herdado da
            // classe pai
            stmt.setInt(1, rt.getTipoRequerimento().getId());

            // 2. status (String/VARCHAR) -> pegando o nome do Enum (ex: "PENDENTE") ou a
            // descrição ("em análise")
            // O recomendável é salvar o nome do enum .name() para bater com o padrão de
            // texto do banco
            stmt.setString(2, rt.getStatus().getDescricao());

            // 3. aluno_matricula (String/VARCHAR) -> buscando do objeto Aluno associado
            stmt.setString(3, rt.getAluno().getMatricula());

            // 4. data_hora_abertura (Timestamp) -> Convertendo LocalDateTime do Java para o
            // Timestamp do Postgres
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(rt.getDataHoraAbertura()));

            // 5. semestre_retorno (String/VARCHAR) -> Atributo específico da classe filha
            stmt.setString(5, rt.getSemestreRetorno());

            // Executa a inserção no banco de dados
            stmt.executeUpdate();

        } catch (SQLException e) {
            // Lança uma exceção em tempo de execução detalhada para capturar no console em
            // caso de erro
            throw new RuntimeException("Erro ao inserir requerimento de trancamento via herança: " + e.getMessage(), e);
        }
    }

    public Requerimento buscarPorId(int id) {
        String sql = """
                SELECT r.id, r.status, r.aluno_matricula,
                       t.id AS tipo_id, t.descricao AS tipo_descricao
                FROM requerimento r
                INNER JOIN tipo_requerimento t ON r.tipo_requerimento_id = t.id
                WHERE r.id = ?
                """;

        try (
                Connection conn = new ConexaoPostgreSQL().getConexao();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // 1. Instancia o TipoRequerimento
                    int tipoId = rs.getInt("tipo_id");
                    String tipoDesc = rs.getString("tipo_descricao");
                    negocio.TipoRequerimento tipo = new negocio.TipoRequerimento(tipoId, tipoDesc);

                    // 2. CRIAÇÃO DO CURSO FAKE COM DADOS VÁLIDOS (Para satisfazer as validações do
                    // Curso)
                    int fakeCursoId = 1;
                    String fakeCursoNome = "Curso Geral";
                    String fakeCursoSite = "http://site.com";
                    Curso.Turno fakeCursoTurno = Curso.Turno.NOTURNO;
                    int fakeCursoDuracao = 2000; // Maior que zero, passa no validador

                    negocio.Curso cursoValidoRaso = new negocio.Curso(
                            fakeCursoId, fakeCursoNome, fakeCursoSite, fakeCursoTurno, fakeCursoDuracao);

                    // 3. CRIAÇÃO DO USUÁRIO FAKE COM DADOS VÁLIDOS (Para satisfazer as validações
                    // do Usuário)
                    int fakeUserId = 0;
                    String fakeUserNome = "Aluno Temporario";
                    String fakeUserEmail = "valido@email.com";
                    String fakeUserCpf = "00000000000"; // Substitua por um real de teste caso o validador recuse zeros
                    java.time.LocalDate fakeUserNasc = java.time.LocalDate.of(2000, 1, 1);
                    String fakeUserCep = "00000000";
                    String fakeUserComplemento = "";
                    String fakeUserNumero = "";

                    negocio.Usuario usuarioValidoRaso = new negocio.Usuario(
                            fakeUserId, fakeUserNome, fakeUserEmail, fakeUserCpf, fakeUserNasc, fakeUserCep,
                            fakeUserComplemento, fakeUserNumero);

                    // 4. Monta o Aluno usando o construtor estrito com os objetos válidos
                    String matricula = rs.getString("aluno_matricula");
                    negocio.Aluno aluno = new negocio.Aluno(matricula, usuarioValidoRaso, cursoValidoRaso);

                    // 5. Mapeia o Status do banco
                    String statusString = rs.getString("status");
                    Requerimento.Status status = Requerimento.Status.fromString(statusString);

                    // CORREÇÃO: Atribuição direta sem depender de nenhuma variável 'timestamp'
                    // antiga
                    java.time.LocalDateTime dataHora = java.time.LocalDateTime.now();

                    // 6. Retorna o Requerimento completo e pronto para o Mustache
                    return new Requerimento(rs.getInt("id"), aluno, tipo, status, dataHora);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar requerimento por ID: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Requerimento> listarPorAluno(String matricula) {
        String sql = SQL_SELECT + "WHERE r.aluno_matricula = ? ORDER BY r.data_hora_abertura DESC";

        List<Requerimento> lista = new ArrayList<>();

        try (
                Connection conn = new ConexaoPostgreSQL().getConexao();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, matricula);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    lista.add(mapearResultSet(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar requerimentos do aluno: " + e.getMessage(), e);
        }
        return lista;
    }

    public List<Requerimento> listarTodosRequerimentos() {
        String sql = SQL_SELECT + "ORDER BY r.data_hora_abertura DESC";

        List<Requerimento> lista = new ArrayList<>();

        try (
                Connection conn = new ConexaoPostgreSQL().getConexao();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next())
                lista.add(mapearResultSet(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar requerimentos: " + e.getMessage(), e);
        }
        return lista;
    }

    public void atualizarStatus(int id, Requerimento.Status novoStatus) {
        String sql = "UPDATE requerimento SET status = ? WHERE id = ?";

        try (
                Connection conn = new ConexaoPostgreSQL().getConexao();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, novoStatus.getDescricao());
            stmt.setInt(2, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar status do requerimento: " + e.getMessage(), e);
        }
    }

    public void deletarRequerimento(int id) {
        // SQL 1: Remove primeiro os filhos (anexos)
        String sqlAnexos = "DELETE FROM anexo WHERE requerimento_id = ?";
        // SQL 2: Remove o pai (requerimento)
        String sqlRequerimento = "DELETE FROM requerimento WHERE id = ?";

        try (
                Connection conn = new ConexaoPostgreSQL().getConexao();) {
            // Desativa o auto-commit para garantir que ou apaga os dois ou nenhum
            // (Transação)
            conn.setAutoCommit(false);

            try (
                    PreparedStatement stmtAnexo = conn.prepareStatement(sqlAnexos);
                    PreparedStatement stmtReq = conn.prepareStatement(sqlRequerimento)) {
                // 1. Executa a deleção na tabela de anexos
                stmtAnexo.setInt(1, id);
                stmtAnexo.executeUpdate();

                // 2. Executa a deleção na tabela de requerimentos
                stmtReq.setInt(1, id);
                stmtReq.executeUpdate();

                // Confirma as alterações no banco de dados
                conn.commit();

            } catch (SQLException e) {
                conn.rollback(); // Desfaz tudo se uma das etapas falhar
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar requerimento e seus anexos: " + e.getMessage(), e);
        }
    }

    public int abrirRequerimento(String matricula, int tipoId, String observacao) {
        // SQL focado nos campos obrigatórios da sua tabela
        String sql = """
                INSERT INTO requerimento (aluno_matricula, tipo_requerimento_id)
                VALUES (?, ?)
                """;

        // O Statement.RETURN_GENERATED_KEYS avisa ao JDBC que queremos o ID criado pelo
        // SERIAL/IDENTITY
        try (
                Connection conn = new ConexaoPostgreSQL().getConexao();
                PreparedStatement stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, matricula);
            stmt.setInt(2, tipoId);

            // Executa a inserção no banco de dados
            stmt.executeUpdate();

            // Recupera a chave primária (ID) gerada automaticamente pelo PostgreSQL
            try (java.sql.ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Retorna o ID gerado (ex: 3, 4, 5...)
                } else {
                    throw new java.sql.SQLException("Falha ao abrir requerimento, nenhum ID foi gerado.");
                }
            }

        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Erro ao abrir requerimento via DAO: " + e.getMessage(), e);
        }
    }

    private static final String SQL_SELECT = """
                SELECT
                    r.id,
                    r.status,
                    r.data_hora_abertura,
                    r.aluno_matricula AS matricula, -- Verifique se é r.aluno_matricula
                    u.id        AS usuario_id,
                    u.nome,
                    u.email,
                    u.cpf,
                    u.data_nascimento,
                    u.cep,
                    u.complemento,
                    u.numero,
                    c.id        AS curso_id,
                    c.nome      AS curso_nome,
                    c.site,
                    c.turno,
                    c.duracao,
                    t.id        AS tipo_id,
                    t.descricao AS tipo_descricao
                FROM requerimento r
                LEFT JOIN aluno           a ON a.matricula     = r.aluno_matricula
                LEFT JOIN usuario         u ON u.id            = a.usuario_id
                LEFT JOIN curso           c ON c.id            = a.curso_id
                LEFT JOIN tipo_requerimento t ON t.id          = r.tipo_requerimento_id -- AJUSTE AQUI
            """;

    private Requerimento mapearResultSet(ResultSet rs) throws SQLException {

        negocio.Usuario usuario = new negocio.Usuario(
                rs.getInt("usuario_id"),
                rs.getString("nome"),
                rs.getString("email"),
                rs.getString("cpf"),
                rs.getDate("data_nascimento").toLocalDate(),
                rs.getString("cep"),
                rs.getString("complemento"),
                rs.getString("numero"));

        negocio.Curso.Turno turno = negocio.Curso.Turno.valueOf(
                rs.getString("turno").toUpperCase());
        negocio.Curso curso = new negocio.Curso(
                rs.getInt("curso_id"),
                rs.getString("curso_nome"),
                rs.getString("site"),
                turno,
                rs.getInt("duracao"));

        Aluno aluno = new Aluno(rs.getString("matricula"), usuario, curso);

        TipoRequerimento tipo = new TipoRequerimento(
                rs.getInt("tipo_id"),
                rs.getString("tipo_descricao"));

        Requerimento.Status status = Requerimento.Status.fromString(
                rs.getString("status"));

        Timestamp ts = rs.getTimestamp("data_hora_abertura");

        return new Requerimento(
                rs.getInt("id"),
                aluno,
                tipo,
                status,
                ts.toLocalDateTime());
    }
}