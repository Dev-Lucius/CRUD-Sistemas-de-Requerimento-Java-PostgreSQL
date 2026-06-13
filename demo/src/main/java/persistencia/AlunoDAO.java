package persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import negocio.Aluno;
import negocio.Curso;
import negocio.Usuario;

/**
 * DAO da Tabela Aluno
 * 
 *  CREATE TABLE aluno(
 *     matricula CHAR(10) PRIMARY KEY,
 *     usuario_id INTEGER REFERENCES usuario(id),
 *     curso_id   INTEGER REFERENCES curso(id)
 *  );
 */

/**
 * Conceito --> Relacionamento no DAO
 * 
 * A tabela Aluno tem FKs para Usuário e Curso.
 *  - No Insert, gravamos apenas os IDs
 *  - No Select, fazemos JOIN para construir os Objetos Completos (Aluno com Usuário / Curso)
 */

public class AlunoDAO {


    // Método para Inserir Alunos
    public void InserirAlunos(Aluno aluno){
        String sql = "INSERT INTO aluno (matricula, usuario_id, curso_id) VALUES (?, ?, ?)";

        try(
            Connection connection = new ConexaoPostgreSQL().getConexao();
            PreparedStatement stmt = connection.prepareStatement(sql);
        ){
            stmt.setString(1, aluno.getMatricula());            
            stmt.setInt(2, aluno.getUsuario().getId());            
            stmt.setInt(3, aluno.getCurso().getId()); 
            
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao Inserir um Novo Aluno: " + e.getMessage(), e);
        }
    }

    // Método para Buscar um Aluno Pela sua Matrícula
    /**
     * JOIN no DAO
     * A ideia é ...
     * - Ao invés de construir 3 Select Separados, iremos usar dois JOINs para
     *   trazer todos os dados numa só ida ao Banco!
     * - Isso vai nós dar mais eficiência no código
     * - O mapearResultSet() monta os três objetos juntos
     */
    public Aluno BuscarPorMatricula(String matricula){
        String sql = """
                SELECT 
                    a.matricula,
                    u.id AS usuario_id,
                    u.nome, u.email, u.cpf,
                    u.data_nascimento, u.cep, u.complemento, u.numero,
                    c.id AS curso_id,
                    c.nome AS curso_nome,
                    c.site, c.turno, c.duracao
                FROM aluno a
                LEFT JOIN usuario u 
                    ON u.id = a.usuario_id
                LEFT JOIN curso c
                    ON c.id = a.curso_id
                WHERE a.matricula = ?
                """;
        try(
            Connection connection = new ConexaoPostgreSQL().getConexao();
            PreparedStatement stmt = connection.prepareStatement(sql);
        ) {
            stmt.setString(1, matricula);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                return mapearResultSet(rs);
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao Buscar um Aluno pela sua Matrícula: " + e.getMessage(), e);
        }
        return null;
    }

    // Método para Listar os Alunos
    public List<Aluno> listarPorCurso(int cursoId) {
    String sql = """
            SELECT a.matricula, u.id AS usuario_id, u.nome, u.email, u.cpf,
                   u.data_nascimento, u.cep, u.complemento, u.numero,
                   c.id AS curso_id, c.nome AS curso_nome, c.site, c.turno, c.duracao
            FROM aluno a
            JOIN usuario u ON u.id = a.usuario_id
            JOIN curso c ON c.id = a.curso_id
            WHERE c.id = ?
            ORDER BY u.nome
            """;
    List<Aluno> lista = new ArrayList<>();
    try (Connection conn = new ConexaoPostgreSQL().getConexao();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, cursoId);
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        }
    } catch (SQLException e) {
        throw new RuntimeException("Erro ao listar por curso: " + e.getMessage());
    }
    return lista;
}

    // Método para Atualizar o Curso de um ALuno
    // --> Caso de Transferência
    public void atualizarCurso(String matricula, int novoCursoId){
        String sql = "UPDATE aluno SET curso_id = ? WHERE matricula = ?";

        try(
            Connection connection = new ConexaoPostgreSQL().getConexao();
            PreparedStatement stmt = connection.prepareStatement(sql);
        ) {
            stmt.setInt(1, novoCursoId);
            stmt.setString(2, matricula);
            stmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao Atualiza o Curso do Alunos: " + e.getMessage(), e);
        }
    }

    // Método para Deletar um Curso do Aluno
    public void deletarPorUsuarioId(int usuarioId) {
        String sql = "DELETE FROM aluno WHERE usuario_id = ?";

        try (
            Connection connection = new ConexaoPostgreSQL().getConexao();
            PreparedStatement stmt = connection.prepareStatement(sql);
        ) {
            stmt.setInt(1, usuarioId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar vínculo de aluno: " + e.getMessage(), e);
        }
    }

    // Método para Listar TODOS os Alunos (necessário para a rota /aluno)
    public List<Aluno> listarTodos() {
        String sql = """
                SELECT a.matricula, u.id AS usuario_id, u.nome, u.email, u.cpf,
                       u.data_nascimento, u.cep, u.complemento, u.numero,
                       c.id AS curso_id, c.nome AS curso_nome, c.site, c.turno, c.duracao
                FROM aluno a
                JOIN usuario u ON u.id = a.usuario_id
                JOIN curso c ON c.id = a.curso_id
                ORDER BY u.nome
                """;
        List<Aluno> lista = new ArrayList<>();
        try (Connection conn = new ConexaoPostgreSQL().getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar todos os alunos: " + e.getMessage());
        }
        return lista;
    }

    // Método para Deletar o Aluno pela Matrícula (necessário para o botão "Excluir")
    public void deletarAluno(String matricula) {
        // 1. Deleta anexos que pertencem aos requerimentos deste aluno
        String sqlAnexos = "DELETE FROM anexo WHERE requerimento_id IN (SELECT id FROM requerimento WHERE aluno_matricula = ?)";
        // 2. Deleta os requerimentos do aluno
        String sqlReq = "DELETE FROM requerimento WHERE aluno_matricula = ?";
        // 3. Deleta o aluno
        String sqlAluno = "DELETE FROM aluno WHERE matricula = ?";

        try (Connection connection = new ConexaoPostgreSQL().getConexao()) {
            connection.setAutoCommit(false);

            try {
                // Ordem: Anexos -> Requerimentos -> Aluno
                try (PreparedStatement stmtAnexo = connection.prepareStatement(sqlAnexos)) {
                    stmtAnexo.setString(1, matricula);
                    stmtAnexo.executeUpdate();
                }

                try (PreparedStatement stmtReq = connection.prepareStatement(sqlReq)) {
                    stmtReq.setString(1, matricula);
                    stmtReq.executeUpdate();
                }

                try (PreparedStatement stmtAluno = connection.prepareStatement(sqlAluno)) {
                    stmtAluno.setString(1, matricula);
                    stmtAluno.executeUpdate();
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro na limpeza profunda: " + e.getMessage(), e);
        }
}

    // Mapeamento
    /**
     * Reconstrói os três objetos (Aluno, Usuario, Curso)
     * a partir das colunas de um ResultSet de JOIN.
     */
    private Aluno mapearResultSet(ResultSet rs) throws SQLException {
    Usuario usuario = new Usuario(
        rs.getInt("usuario_id"),
        rs.getString("nome"),
        rs.getString("email"),
        rs.getString("cpf"),
        rs.getDate("data_nascimento").toLocalDate(),
        rs.getString("cep"),
        rs.getString("complemento"),
        rs.getString("numero") // Adicionado o campo número que faltava
    );

    Curso.Turno turno = Curso.Turno.valueOf(rs.getString("turno").toUpperCase());
    Curso curso = new Curso(
        rs.getInt("curso_id"),
        rs.getString("curso_nome"),
        rs.getString("site"),
        turno,
        rs.getInt("duracao")
    );

        return new Aluno(rs.getString("matricula"), usuario, curso);
    }
}
