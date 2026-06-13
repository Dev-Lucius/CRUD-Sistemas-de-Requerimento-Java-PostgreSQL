package negocio;


public final class Curso {

    
    private int id;
    private final String nome;
    private final String site;
    private final int duracao;

    public enum Turno {
        NOTURNO, DIURNO, VESPERTINO;

        
        public static Turno fromString(String valor) {
            if (valor == null)
                throw new IllegalArgumentException("Turno não pode ser nulo.");
            return switch (valor.trim().toLowerCase()) {
                case "noturno"    -> NOTURNO;
                case "diurno"     -> DIURNO;
                case "vespertino" -> VESPERTINO;
                default -> throw new IllegalArgumentException(
                    "Turno inválido: '" + valor + "'. Use noturno, diurno ou vespertino."
                );
            };
        }
    }
    private final Turno turno;


   
    public Curso(String nome, String site, Turno turno, int duracao) {
        this.nome    = validarTexto(nome, "Nome");
        this.site    = validarTexto(site, "Site");

        if (turno == null)
            throw new IllegalArgumentException("Turno inválido.");
        this.turno = turno;

        if (duracao <= 0)
            throw new IllegalArgumentException("Duração deve ser maior que zero.");
        this.duracao = duracao;
    }

   
    public Curso(int id, String nome, String site, Turno turno, int duracao) {
        this(nome, site, turno, duracao);
        this.id = id;
    }

    public String validarTexto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty())
            throw new IllegalArgumentException(campo + " não pode ser vazio.");
        return valor.trim();
    }

    
    public void setId(int id) { this.id = id; }
    public int    getId()      { return id; }
    public String getNome()    { return nome; }
    public String getSite()    { return site; }
    public Turno  getTurno()   { return turno; }
    public int    getDuracao() { return duracao; }

    @Override
    public String toString() {
        return "Curso #" + id +
               " | Nome: "    + nome +
               " | Turno: "   + turno.toString().toLowerCase() +
               " | Duração: " + duracao + "h";
    }
}