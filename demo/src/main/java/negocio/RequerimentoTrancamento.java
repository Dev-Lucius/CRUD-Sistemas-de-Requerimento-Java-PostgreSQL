package negocio;

import java.time.LocalDateTime;

public class RequerimentoTrancamento extends Requerimento {

    private String semestreRetorno;

    /**
     * Construtor para novos requerimentos (Abertura via formulário).
     * A data/hora e o status PENDENTE são definidos automaticamente pelo super().
     */
    public RequerimentoTrancamento(Aluno aluno, TipoRequerimento tipoRequerimento, String semestreRetorno) {
        // Invoca o construtor padrão da classe pai
        super(aluno, tipoRequerimento);

        if (semestreRetorno == null || semestreRetorno.trim().isEmpty()) {
            throw new IllegalArgumentException("O semestre de retorno é obrigatório.");
        }
        this.semestreRetorno = semestreRetorno;
    }

    /**
     * Construtor completo para reconstruir o objeto a partir dos dados vindos do
     * Banco de Dados (DAO).
     */
    public RequerimentoTrancamento(int id, Aluno aluno, TipoRequerimento tipoRequerimento,
            Status status, LocalDateTime dataHoraAbertura, String semestreRetorno) {
        // Invoca o construtor completo da classe pai
        super(id, aluno, tipoRequerimento, status, dataHoraAbertura);

        if (semestreRetorno == null || semestreRetorno.trim().isEmpty()) {
            throw new IllegalArgumentException("O semestre de retorno é obrigatório.");
        }
        this.semestreRetorno = semestreRetorno;
    }

    // ── Getter e Setter ───────────────────────────────────────────────────────
    public String getSemestreRetorno() {
        return semestreRetorno;
    }

    public void setSemestreRetorno(String semestreRetorno) {
        if (semestreRetorno == null || semestreRetorno.trim().isEmpty()) {
            throw new IllegalArgumentException("O semestre de retorno não pode ser nulo ou vazio.");
        }
        this.semestreRetorno = semestreRetorno;
    }

    // ── Sobrescrita para enriquecer o log do sistema ──────────────────────────
    @Override
    public String toString() {
        return super.toString() + " | Semestre de Retorno: " + semestreRetorno;
    }
}