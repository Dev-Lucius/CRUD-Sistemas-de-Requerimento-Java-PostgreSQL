package negocio;

import java.time.LocalDateTime;

public class Requerimento {

    private int id; // não-final: gerado pelo banco no INSERT
    private final Aluno aluno;
    private final LocalDateTime dataHoraAbertura;
    private Status status;
    private final TipoRequerimento tipoRequerimento;

    public enum Status {
        PENDENTE("em análise"),
        DEFERIDO("deferido"),
        INDEFERIDO("indeferido");

        private final String descricao;

        Status(String d) { this.descricao = d; }
        public String getDescricao() { return descricao; }

        public static Status fromString(String texto) {
            if (texto == null) return PENDENTE;
            
            String t = texto.toLowerCase().trim();
            
            // Se contiver "an" e "lise", tratamos como PENDENTE independente do acento
            if (t.contains("an") && t.contains("lise")) return PENDENTE;
            if (t.contains("deferido")) return DEFERIDO;
            if (t.contains("indeferido")) return INDEFERIDO;
            
            throw new IllegalArgumentException("Status inválido: '" + texto + "'");
        }
    }

    public Requerimento(Aluno aluno, TipoRequerimento tipoRequerimento) {
        if (aluno == null)
            throw new IllegalArgumentException("Aluno inválido: não pode ser nulo.");
        if (tipoRequerimento == null)
            throw new IllegalArgumentException("Tipo de requerimento inválido: não pode ser nulo.");

        this.aluno             = aluno;
        this.tipoRequerimento  = tipoRequerimento;
        this.dataHoraAbertura  = LocalDateTime.now(); 
        this.status            = Status.PENDENTE;
    }


    public Requerimento(int id, Aluno aluno, TipoRequerimento tipoRequerimento,
                        Status status, LocalDateTime dataHoraAbertura) {
        if (aluno == null)
            throw new IllegalArgumentException("Aluno inválido: não pode ser nulo.");
        if (tipoRequerimento == null)
            throw new IllegalArgumentException("Tipo de requerimento inválido: não pode ser nulo.");

        this.id                = id;
        this.aluno             = aluno;
        this.tipoRequerimento  = tipoRequerimento;
        this.status            = status;
        this.dataHoraAbertura  = dataHoraAbertura; // ← timestamp real do banco
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public int              getId()               { return id; }
    public Aluno            getAluno()            { return aluno; }
    public LocalDateTime    getDataHoraAbertura() { return dataHoraAbertura; }
    public Status           getStatus()           { return status; }
    public TipoRequerimento getTipoRequerimento() { return tipoRequerimento; }

    
    public void setId(int id) { this.id = id; }
    public void setStatus(Status novoStatus) {
        if (novoStatus == null)
            throw new IllegalArgumentException("Status inválido: não pode ser nulo.");
        if (this.status == Status.DEFERIDO || this.status == Status.INDEFERIDO)
            throw new IllegalStateException(
                "Não é possível alterar o status: requerimento já finalizado como " +
                this.status.getDescricao() + "."
            );
        this.status = novoStatus;
    }

    public static Status fromString(String texto) {
        if (texto == null) return Status.PENDENTE;
        
        String t = texto.toLowerCase().trim();
        
        // Usando contains para ignorar se o 'á' quebrou ou virou espaço
        if (t.contains("an") && t.contains("lise")) return Status.PENDENTE;
        if (t.equals("deferido")) return Status.DEFERIDO;
        if (t.equals("indeferido")) return Status.INDEFERIDO;
        
        return Status.PENDENTE; // Fallback para não quebrar a listagem se vier algo estranho
    }

    @Override
    public String toString() {
        return "Requerimento #"  + id +
               " | Aluno: "      + aluno.getNomeAluno() +
               " | Tipo: "       + tipoRequerimento.getDescricao() +
               " | Status: "     + status.getDescricao() +
               " | Aberto em: "  + dataHoraAbertura;
    }
}