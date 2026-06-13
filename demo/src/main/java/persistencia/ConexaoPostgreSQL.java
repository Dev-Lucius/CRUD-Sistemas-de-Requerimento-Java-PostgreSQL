package persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoPostgreSQL {
    private final String host = "localhost";
    private final String port = "5432";
    private final String dbname = "sistema_requerimento_completo";

    // Método de Conexão
    public Connection getConexao() throws SQLException {
        // 🌟 AQUI ESTÁ O TRUQUE: Buscamos o usuário ativo e a senha dele definidos no Main
        String username = apresentacao.Main.usuarioBancoAtual;
        String password = apresentacao.Main.senhaBancoAtual;

        String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbname + "?charSet=UTF-8";

        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao Conectar ao Banco com o usuário: " + username, e);
        }
    }
}