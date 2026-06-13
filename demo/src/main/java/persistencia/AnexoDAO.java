package persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.postgresql.util.PGobject;

import negocio.Anexo;
import negocio.Requerimento; // IMPORTANTE para o JSONB

/**
 * DAO da Tabela Anexo adaptada para JSONB
 */
public class AnexoDAO {

    private static final String SQL_SELECT = """
                SELECT
                    a.id,
                    a.descricao,
                    a.arquivo,
                    a.requerimento_id
                FROM anexo a
            """;

    // -------------------------------------------------------
    // CREATE
    // -------------------------------------------------------

    public int inserirAnexo(Anexo anexo) {
        String sql = """
                INSERT INTO anexo (descricao, arquivo, requerimento_id)
                VALUES (?, ?, ?)
                """;

        try (
                Connection conn = new ConexaoPostgreSQL().getConexao();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // CORREÇÃO DO BUG 1: Enviando String como objeto JSONB nativo do PostgreSQL
            PGobject jsonObject = new PGobject();
            jsonObject.setType("jsonb");
            jsonObject.setValue(anexo.getDescricao());
            stmt.setObject(1, jsonObject);

            // BYTEA
            byte[] arquivo = anexo.getArquivo();
            if (arquivo != null) {
                stmt.setBytes(2, arquivo);
            } else {
                stmt.setNull(2, Types.BINARY);
            }

            // FK requerimento
            if (anexo.getRequerimento() != null) {
                stmt.setInt(3, anexo.getRequerimento().getId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGerado = rs.getInt(1);
                    anexo.setId(idGerado); // CORREÇÃO DO BUG 2: Sincroniza o ID no objeto original
                    return idGerado;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir anexo: " + e.getMessage(), e);
        }
        throw new RuntimeException("Inserção não retornou um id gerado.");
    }

    // -------------------------------------------------------
    // READ
    // -------------------------------------------------------

    public Anexo buscarPorId(int id) {
        String sql = SQL_SELECT + " WHERE a.id = ?";

        try (
                Connection conn = new ConexaoPostgreSQL().getConexao();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return mapearResultSet(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar anexo por id: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Anexo> listarPorRequerimento(int requerimentoId) {
        String sql = SQL_SELECT + " WHERE a.requerimento_id = ?";
        List<Anexo> lista = new ArrayList<>();

        try (
                Connection conn = new ConexaoPostgreSQL().getConexao();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requerimentoId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    lista.add(mapearResultSet(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar anexos do requerimento: " + e.getMessage(), e);
        }
        return lista;
    }

    public List<Anexo> listarTodosAnexos() {
        String sql = SQL_SELECT + " ORDER BY a.id";
        List<Anexo> lista = new ArrayList<>();

        try (
                Connection conn = new ConexaoPostgreSQL().getConexao();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next())
                lista.add(mapearResultSet(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar anexos: " + e.getMessage(), e);
        }
        return lista;
    }

    // -------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------

    public void atualizarArquivo(int id, byte[] novoArquivo) {
        String sql = "UPDATE anexo SET arquivo = ? WHERE id = ?";

        try (
                Connection conn = new ConexaoPostgreSQL().getConexao();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (novoArquivo != null) {
                stmt.setBytes(1, novoArquivo);
            } else {
                stmt.setNull(1, Types.BINARY);
            }
            stmt.setInt(2, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar arquivo do anexo: " + e.getMessage(), e);
        }
    }

    public void atualizarRequerimento(int id, Integer requerimentoId) {
        String sql = "UPDATE anexo SET requerimento_id = ? WHERE id = ?";

        try (
                Connection conn = new ConexaoPostgreSQL().getConexao();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (requerimentoId != null) {
                stmt.setInt(1, requerimentoId);
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setInt(2, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar requerimento do anexo: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------
    // DELETE
    // -------------------------------------------------------

    public void deletarAnexo(int id) {
        String sql = "DELETE FROM anexo WHERE id = ?";

        try (
                Connection conn = new ConexaoPostgreSQL().getConexao();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar anexo: " + e.getMessage(), e);
        }
    }

    public void salvarAnexo(int idRequerimento, String nomeArquivo, byte[] arquivoBytes) {
        // Ajuste os nomes das colunas 'requerimento_id', 'nome' e 'dados' de acordo com
        // a sua tabela de anexos
        String sql = """
                INSERT INTO anexo (requerimento_id, nome, dados)
                VALUES (?, ?, ?)
                """;

        try (
                Connection conn = new ConexaoPostgreSQL().getConexao();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idRequerimento);
            stmt.setString(2, nomeArquivo);
            stmt.setBytes(3, arquivoBytes); // Grava os bytes brutos do PDF/Imagem no banco

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar o arquivo anexo no banco: " + e.getMessage(), e);
        }
    }

    public List<Anexo> listarPorMimeType(String mimeType) {
        // Usando o operador ->> do Postgres para ler o campo dentro do JSONB
        String sql = SQL_SELECT + " WHERE a.descricao->>'conteudo_tipo' = ? ORDER BY a.id";
        List<Anexo> lista = new ArrayList<>();

        try (
                Connection conn = new ConexaoPostgreSQL().getConexao();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, mimeType);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultSet(rs)); // Aproveita o mapeador que já corrigimos!
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar anexos por Mime Type via JSONB: " + e.getMessage(), e);
        }
        return lista;
    }

    // -------------------------------------------------------
    // MAPEAMENTO PRIVADO
    // -------------------------------------------------------

    private Anexo mapearResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String descricao = rs.getString("descricao");
        byte[] arquivo = rs.getBytes("arquivo");

        Requerimento requerimento = null;
        int requerimentoId = rs.getInt("requerimento_id");

        if (!rs.wasNull()) {
            // Criamos um requerimento raso apenas para satisfazer a associação do Anexo,
            // evitando recursão infinita com o RequerimentoDAO.
            negocio.Curso cursoFake = new negocio.Curso(1, "Geral", "http://site.com", negocio.Curso.Turno.NOTURNO,
                    2000);
            negocio.Usuario userFake = new negocio.Usuario(0, "Temp", "valido@email.com", "00000000000",
                    java.time.LocalDate.of(2000, 1, 1), "00000000", "", "");
            negocio.Aluno alunoFake = new negocio.Aluno("0000000000", userFake, cursoFake);
            negocio.TipoRequerimento tipoFake = new negocio.TipoRequerimento(1, "Tipo");

            requerimento = new Requerimento(requerimentoId, alunoFake, tipoFake, Requerimento.Status.PENDENTE,
                    java.time.LocalDateTime.now());
        }

        // Cria o objeto Anexo usando o construtor completo
        Anexo anexoObj = new Anexo(descricao, arquivo, requerimento);
        anexoObj.setId(id); // Garante que o ID vindo do banco está sincronizado para o Mustache ler {{id}}

        return anexoObj;
    }
}