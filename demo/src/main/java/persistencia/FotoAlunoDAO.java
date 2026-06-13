package persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FotoAlunoDAO {

    /**
     * Salva ou Substitui (Realtera) a foto do aluno no banco de dados.
     */
    public void salvarOuAtualizarFoto(String matricula, String nomeArquivo, String contentType, byte[] arquivoBytes) {
        // Forçando o uso explícito de public.foto_aluno em minúsculas
        String sql = """
                INSERT INTO public.foto_aluno (aluno_matricula, nome_arquivo, conteudo_tipo, arquivo)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (aluno_matricula)
                DO UPDATE SET nome_arquivo = EXCLUDED.nome_arquivo,
                              conteudo_tipo = EXCLUDED.conteudo_tipo,
                              arquivo = EXCLUDED.arquivo;
                """;

        try (
                Connection conn = new ConexaoPostgreSQL().getConexao();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, matricula);
            stmt.setString(2, nomeArquivo);
            stmt.setString(3, contentType);
            stmt.setBytes(4, arquivoBytes);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar/alterar a foto do aluno: " + e.getMessage(), e);
        }
    }

    /**
     * Deleta a foto do aluno do banco de dados.
     * @param matricula 
     * @return void 
     */
    public void deletarFoto(String matricula) {
        String sql = "DELETE FROM foto_aluno WHERE aluno_matricula = ?";

        try (
                Connection conn = new ConexaoPostgreSQL().getConexao();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, matricula);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir foto do aluno: " + e.getMessage(), e);
        }
    }

    /**
     * Busca os dados da foto para renderizar no navegador.
     */
    public byte[] buscarFotoPorMatricula(String matricula) {
        // Forçando o uso explícito de public.foto_aluno em minúsculas
        String sql = "SELECT arquivo FROM public.foto_aluno WHERE aluno_matricula = ?";
        try (
                Connection conn = new ConexaoPostgreSQL().getConexao();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, matricula);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("arquivo");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}