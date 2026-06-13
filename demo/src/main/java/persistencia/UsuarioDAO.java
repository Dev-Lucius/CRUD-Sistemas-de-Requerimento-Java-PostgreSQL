package persistencia;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import negocio.Usuario;

/**
 * DAO da Tabela Usuario
 *
 * CREATE TABLE usuario(
 *     id              SERIAL PRIMARY KEY,
 *     nome            VARCHAR(200) NOT NULL,
 *     email           VARCHAR(200) UNIQUE,
 *     cpf             CHAR(11)     UNIQUE,
 *     data_nascimento DATE,
 *     cep             CHAR(8),
 *     complemento     TEXT,
 *     numero          VARCHAR(10)
 * );
 */
public class UsuarioDAO {

    // ────────────────────────────────────────────────────────────────────────
    // INSERT — retorna o id gerado pelo banco
    // ────────────────────────────────────────────────────────────────────────
    public boolean inserirUsuario(Usuario usuario) {
        String sql = """
                INSERT INTO usuario (nome, email, cpf, data_nascimento, cep, complemento, numero)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (
            Connection conn        = new ConexaoPostgreSQL().getConexao();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getCpf());
            stmt.setDate  (4, Date.valueOf(usuario.getDataNascimento()));
            stmt.setString(5, usuario.getCep());
            stmt.setString(6, usuario.getComplemento());
            stmt.setString(7, usuario.getNumero());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) rs.getInt(1);
                return true;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir usuário: " + e.getMessage(), e);
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // SELECT por id
    // ────────────────────────────────────────────────────────────────────────
    public Usuario buscarPorId(int id) {
        String sql = "SELECT * FROM usuario WHERE id = ?";

        try (
            Connection conn        = new ConexaoPostgreSQL().getConexao();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) { // ← fechado garantidamente
                if (rs.next()) return mapearUsuario(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuário: " + e.getMessage(), e);
        }
        return null;
    }

    // ────────────────────────────────────────────────────────────────────────
    // SELECT todos
    // ────────────────────────────────────────────────────────────────────────
    public List<Usuario> listarUsuarios() {
        String sql = "SELECT * FROM usuario ORDER BY nome ASC";
        List<Usuario> lista = new ArrayList<>();

        try (
            Connection conn        = new ConexaoPostgreSQL().getConexao();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs           = stmt.executeQuery()
        ) {
            while (rs.next()) lista.add(mapearUsuario(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar usuários: " + e.getMessage(), e);
        }
        return lista;
    }

    // ────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ────────────────────────────────────────────────────────────────────────
    public boolean atualizarUsuario(Usuario usuario) {
        String sql = """
                UPDATE usuario
                SET nome=?, email=?, cpf=?, data_nascimento=?, cep=?, complemento=?, numero=?
                WHERE id=?
                """;

        try (
            Connection conn        = new ConexaoPostgreSQL().getConexao();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getCpf());
            stmt.setDate  (4, Date.valueOf(usuario.getDataNascimento()));
            stmt.setString(5, usuario.getCep());
            stmt.setString(6, usuario.getComplemento());
            stmt.setString(7, usuario.getNumero());
            stmt.setInt   (8, usuario.getId());
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar usuário: " + e.getMessage(), e);
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // DELETE
    // ────────────────────────────────────────────────────────────────────────
    public void deletarUsuario(int id) {
        String sql = "DELETE FROM usuario WHERE id = ?";

        try (
            Connection conn        = new ConexaoPostgreSQL().getConexao();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar usuário: " + e.getMessage(), e);
        }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        return new Usuario(
            rs.getInt   ("id"),
            rs.getString("nome"),
            rs.getString("email"),
            rs.getString("cpf"),
            rs.getDate  ("data_nascimento").toLocalDate(),
            rs.getString("cep"),
            rs.getString("complemento"),
            rs.getString("numero")
        );
    }
}