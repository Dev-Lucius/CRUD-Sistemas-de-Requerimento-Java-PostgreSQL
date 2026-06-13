package negocio;

public class Aluno {

    // Atributos de Matricula e Usuário São Imutáveis
    private final String matricula;
    private final Usuario usuario;
    private Curso curso;

    /**
     * Construtor do Aluno.
     *
     * @param matricula Matrícula com exatamente 10 caracteres.
     * @param usuario   Usuário vinculado. Não pode ser nulo.
     * @param curso     Curso do aluno. Não pode ser nulo.
     */
    public Aluno(String matricula, Usuario usuario, Curso curso) {
        this.matricula = validarMatricula(matricula);
        this.usuario   = validarUsuario(usuario);
        this.curso     = validarCurso(curso);
    }

    // Métodos de Validação
    /**
     * CHAR(10) no banco → matrícula deve ter exatamente 10 caracteres.
    */
    private String validarMatricula(String matricula) {
        if (matricula == null || matricula.trim().isEmpty()){
            throw new IllegalArgumentException("Matrícula inválida: não pode ser nula ou vazia.");
        }

        if (matricula.trim().length() != 10){
            throw new IllegalArgumentException("Matrícula inválida: deve ter exatamente 10 caracteres.");
        }
        return matricula.trim();
    }

    private Usuario validarUsuario(Usuario usuario) {
        if (usuario == null){
            throw new IllegalArgumentException("Usuário inválido: não pode ser nulo.");
        }
        return usuario;
    }

    private Curso validarCurso(Curso curso) {
        if (curso == null){
            throw new IllegalArgumentException("Curso inválido: não pode ser nulo.");
        }
        return curso;
    }

    // GETTERS
    public String  getMatricula() { return matricula; }
    public Usuario getUsuario()   { return usuario; }
    public Curso   getCurso()     { return curso; }

    /**
     * Retorna o nome do aluno navegando pela associação.
     *
     * CONCEITO — NAVEGAÇÃO DE ASSOCIAÇÃO:
     * Em vez de fazer um JOIN no banco para obter o nome,
     * navegamos diretamente: aluno → usuario → nome.
     * Equivalente SQL: JOIN usuario ON usuario.id = aluno.usuario_id
     *
     * @return nome do usuário vinculado a este aluno.
     */
    public String getNomeAluno() {
        return usuario.getNome();
    }

    
    // SETTERS
    /**
     * Atualiza o curso do aluno (ex: transferência de curso).
     *
     * @param curso Novo curso. Não pode ser nulo.
     */
    public void setCurso(Curso curso) {
        this.curso = validarCurso(curso);
    }

    @Override
    public String toString() {
        return "Aluno: "      + matricula         +
               " | Nome: "    + usuario.getNome() +
               " | Curso: "   + curso.getNome()   +
               " | Turno: "   + curso.getTurno().toString().toLowerCase();
    }
}