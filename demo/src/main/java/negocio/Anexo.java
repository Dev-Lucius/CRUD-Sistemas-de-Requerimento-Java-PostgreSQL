package negocio;

import java.util.Arrays;

public class Anexo {

    private int id; // Removido o 'final' e o 'proximoId' estático
    private final String descricao; // Aqui trafegará a String em formato JSON
    private byte[] arquivo;
    private Requerimento requerimento;

    /**
     * Construtor para criar um anexo vindo do Banco de Dados (Já possui ID)
     */
    public Anexo(int id, String descricao, byte[] arquivo, Requerimento requerimento) {
        this.id = id;
        this.descricao = validarDescricao(descricao);
        this.arquivo = arquivo != null ? Arrays.copyOf(arquivo, arquivo.length) : null;
        this.requerimento = requerimento;
    }

    /**
     * Construtor para novos anexos (O banco vai gerar o ID via SERIAL)
     */
    public Anexo(String descricao, byte[] arquivo, Requerimento requerimento) {
        this.descricao = validarDescricao(descricao);
        this.arquivo = arquivo != null ? Arrays.copyOf(arquivo, arquivo.length) : null;
        this.requerimento = requerimento;
    }

    private String validarDescricao(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()){
            throw new IllegalArgumentException("Descrição inválida: não pode ser nula ou vazia.");
        }
        return descricao.trim();
    }

    // GETTERS E SETTERS
    public int getId() { return id; }
    public void setId(int id) { this.id = id; } // Necessário para o DAO injetar o ID do SERIAL
    
    public String getDescricao() { return descricao; }
    public Requerimento getRequerimento() { return requerimento; }

    public byte[] getArquivo() {
        return arquivo != null ? Arrays.copyOf(arquivo, arquivo.length) : null;
    }

    public void setArquivo(byte[] arquivo) {
        if(arquivo != null){
            // Defensive Copy
            this.arquivo = Arrays.copyOf(arquivo, arquivo.length); 
        } else {
            this.arquivo = null;
        }
    }

    public void setRequerimento(Requerimento requerimento) {
        this.requerimento = requerimento;
    }

    // MÉTODOS UTILITÁRIOS
    public boolean temArquivo() {
        return arquivo != null && arquivo.length > 0;
    }

    public int getTamanhoArquivo() {
        if(temArquivo()){
            return arquivo.length;
        }
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Anexo # ").append(id);
        sb.append("\nDescrição (JSON): ").append(descricao);
        sb.append("\nArquivo: ").append(temArquivo() ? getTamanhoArquivo() + " bytes" : "Sem Arquivo");
        if(requerimento != null){
            sb.append("\nRequerimento # ").append(requerimento.getId());
        }
        return sb.toString();
    }
}