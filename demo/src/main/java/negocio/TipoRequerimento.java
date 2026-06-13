package negocio;

public class TipoRequerimento {

    private static int proximoId = 1;
    private int id; // removido o final para permitir setId()
    private final String descricao;

    /**
     * Construtor para uso da aplicação.
     * ID gerado pelo contador estático — igual ao de antes.
     */
    public TipoRequerimento(String descricao) {
        this.id        = proximoId++;
        this.descricao = validarDescricao(descricao);
    }

    /**
     * Construtor para uso exclusivo do DAO.
     * Restaura um objeto com o id real vindo do banco,
     * sem incrementar o contador estático.
     */
    public TipoRequerimento(int id, String descricao) {
        this.id        = id;
        this.descricao = validarDescricao(descricao);
    }

    private String validarDescricao(String descricao) {
        if (descricao == null || descricao.trim().isEmpty())
            throw new IllegalArgumentException("Descrição inválida: não pode ser nula ou vazia.");
        return descricao.trim();
    }

    // Necessário para o inserir() do DAO setar o id gerado pelo SERIAL
    public void setId(int id) { this.id = id; }

    public int    getId()        { return id; }
    public String getDescricao() { return descricao; }

    @Override
    public String toString() {
        return "TipoRequerimento #" + id + " | " + descricao;
    }
}