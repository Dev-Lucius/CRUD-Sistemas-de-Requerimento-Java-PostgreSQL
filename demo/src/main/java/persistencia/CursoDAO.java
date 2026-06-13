package  persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import negocio.Curso;

    /**
     * DAO --> Data Acess Object
     * É a classe responsável por pelas operações de persistência da entidade no Banco de Dados
     * 
     * DAO PATTERN
     * A camada DAO isola toda a lógica de acesso ao banco de dados.
     * As demais camadas (negócio, apresentação) não sabem como os dados são armazenados.
     * Elas apenas chamam os métodos de DAO !!
     * 
     * try - with - resources
     * Garante que Connection, PreparedStatement e ResulSet sejam
     * fechados automaticamente ao final do bloco, mesmo em casos de 
     * exceção - evitando que resouce leak - vazamento de recursos
     * 
     */

public class CursoDAO{

    /**
     * Método --> Insere um Novo Curso no Banco de dados !!
     * 
     * PreparedStatement
     * Esse método usa "?" como placeholders em vez de concatenar em Strings, isso é útil pois:
     *  - Eveita SQL Injection 
     *  - Melhora a Performance do Algoritmo
     *  - Permite o banco reutilizar o plano de execução
     * 
     * @param curso Objeto com dados a inserir
     */
    public boolean inserirCurso(Curso curso){
        if(curso == null){
            throw new IllegalArgumentException("Curso Inválido: Não pode ser Nulo");
        }
        
        String sql = "INSERT INTO curso (nome, site, turno, duracao) VALUES (?, ?, ?, ?)";
        
        try(
            Connection connection = new ConexaoPostgreSQL().getConexao();
            PreparedStatement stmt = connection.prepareStatement(sql);
        ) {
            stmt.setString(1, curso.getNome());
            stmt.setString(2, curso.getSite());
            // Turno é enum, não String
            // Assim, é necessário converter para String para Salvar no Banco.
            // name() --> retorna o nome da Constante
            // toLowerCase() --> Converte os caracteres de maiúsculo para minúsculo 
            stmt.setString(3, curso.getTurno().name().toLowerCase());
            stmt.setInt(4, curso.getDuracao());

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao Inserir Curso: " + e.getMessage(), e);
        }
    }

    /** 
     * Método --> Retorna todos os cursos que foram cadastrados no curso
     * @return Retorna a Lista de Cursos, ou uma lista vazia
    */
    public List<Curso> listarCursos(){
        List<Curso> cursos = new ArrayList<>();

        String sql = "SELECT * FROM curso ORDER BY nome ASC";

        try(
            Connection connection = new ConexaoPostgreSQL().getConexao();
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
        ) {
            while(rs.next()){
                cursos.add(mapearCurso(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao Listar Curso: " + e.getMessage(), e);
        }
        
        return cursos;
    }

    /**
     * Método --> Usado para buscar um curso a partir do seu ID
     * @param id ID do curso a ser buscado
     * @return o Curso encontrado, ou null, se ele não existir
     */
    public Curso buscarPorId(int id){
        String sql = "SELECT * FROM curso WHERE id = ?";

        try(
            Connection connection = new ConexaoPostgreSQL().getConexao();
            PreparedStatement stmt = connection.prepareStatement(sql);
        ) {
            stmt.setInt(1, id);

            try(ResultSet rs = stmt.executeQuery()) {
              if(rs.next()){
                return mapearCurso(rs);
              }  
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao Buscar Curso por ID: " + e.getMessage(), e);
        }
        return null; // Não Encontrado
    }

    public boolean atualizarCurso(Curso curso){
        if(curso == null){
            throw new IllegalArgumentException("Curso Inválido: Não pode ser nulo");
        }

        String sql = "UPDATE curso SET nome = ?, site = ?, turno = ?, duracao = ? WHERE id = ?";

        try(
            Connection connection = new ConexaoPostgreSQL().getConexao();
            PreparedStatement stmt = connection.prepareStatement(sql);
        ){
            stmt.setString(1, curso.getNome());
            stmt.setString(2, curso.getSite());
            stmt.setString(3, curso.getTurno().name().toLowerCase());
            stmt.setInt(4, curso.getDuracao());
            stmt.setInt(5, curso.getId());

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao Atualizar Curso: " + e.getMessage(), e);
        }
    }

    /**
     * Método --> Remove um Curso a partir do seu ID
     * @param id
     */
    public void deletarCurso(int id){
        String sql = "DELETE FROM curso WHERE id = ?";

        try (
            Connection connection = new ConexaoPostgreSQL().getConexao();
            PreparedStatement stmt = connection.prepareStatement(sql);
        ){
            stmt.setInt(1, id);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao Deletar Curso: " + e.getMessage(), e);
        }
    }

    /**
     * Método --> Mapeia uma Linha do ResultSet para um objeto Curso
     * @param rs ResultSet posicionado na linha atual
     * @return Objeto Curso Mapeado
     * @throws SQLException Se ocorrer erro ao ler o ResultSet
     */
    public Curso mapearCurso(ResultSet rs) throws SQLException{
        String nomeTurno = rs.getString("turno").toUpperCase();

        // Conceito - Enum.valueOf()
        /**
         * Converte a String do banco de volta para a constante Enum (Curso.Turno.NOTURNO)
         * toUpperCase() é necessário pois os valores do banco estão em minúsculo e o
         * enum em maiúsculo
         */
        Curso.Turno turno = Curso.Turno.valueOf(nomeTurno);

        return new Curso(
            rs.getInt   ("id"),
            rs.getString("nome"),
            rs.getString("site"),
            turno,
            rs.getInt("duracao")
        );
    }
}