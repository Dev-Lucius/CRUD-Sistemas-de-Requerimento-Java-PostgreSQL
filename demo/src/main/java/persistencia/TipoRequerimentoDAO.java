package persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import negocio.TipoRequerimento;

public class TipoRequerimentoDAO {

    // INSERT
    public int inserir(TipoRequerimento tipo) {

        String sql = "INSERT INTO tipo_requerimento (descricao) VALUES (?)";

        try (
            Connection conn         = new ConexaoPostgreSQL().getConexao();
            PreparedStatement stmt  = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            stmt.setString(1, tipo.getDescricao());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGerado = rs.getInt(1);
                    tipo.setId(idGerado);
                    return idGerado;
                }
            }

            // INSERT executado mas nenhum id retornado — situação inesperada
            throw new RuntimeException("INSERT executado, mas nenhum id foi gerado.");

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir TipoRequerimento: " + e.getMessage(), e);
        }
    }

    // SELECT POR ID
    public TipoRequerimento buscarPorId(int id) {

        String sql = "SELECT id, descricao FROM tipo_requerimento WHERE id = ?";

        try (
            Connection conn        = new ConexaoPostgreSQL().getConexao();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }

            return null; // não encontrado

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar TipoRequerimento: " + e.getMessage(), e);
        }
    }

    //  SELECT TODOS
    public List<TipoRequerimento> listarTodos() {

        String sql = "SELECT id, descricao FROM tipo_requerimento ORDER BY descricao";
        List<TipoRequerimento> lista = new ArrayList<>();

        try (
            Connection conn        = new ConexaoPostgreSQL().getConexao();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs           = stmt.executeQuery()
        ) {
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar TipoRequerimento: " + e.getMessage(), e);
        }

        return lista;
    }

    // DELETE
    public void deletar(int id) {

        String sql = "DELETE FROM tipo_requerimento WHERE id = ?";

        try (
            Connection conn        = new ConexaoPostgreSQL().getConexao();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar TipoRequerimento: " + e.getMessage(), e);
        }
    }

    // MAPEAMENTO
    /**
     * Converte uma linha do ResultSet em um TipoRequerimento.
     * Centralizado aqui para não repetir lógica em cada método de busca.
     */
    private TipoRequerimento mapearResultSet(ResultSet rs) throws SQLException {
        return new TipoRequerimento(
            rs.getInt("id"),
            rs.getString("descricao")
        );
    }
}