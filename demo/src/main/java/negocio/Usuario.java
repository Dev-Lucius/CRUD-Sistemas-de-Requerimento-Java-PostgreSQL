package negocio;

import java.time.LocalDate;
import java.time.Period;

public final class Usuario {

    private int id;

    private final String nome;
    private String email;
    private String cpf;
    private LocalDate dataNascimento;
    private String cep;
    private String complemento;
    private String numero;

    /**
     * @param id             Gerado pelo banco (SERIAL). Usar 0 ao criar novo usuário.
     * @param nome           Nome completo. Obrigatório.
     * @param email          Email válido. Obrigatório.
     * @param cpf            CPF com 11 dígitos. Obrigatório.
     * @param dataNascimento Data de nascimento. Não pode ser futura.
     * @param cep            CEP com 8 dígitos. Opcional.
     * @param complemento    Complemento. Opcional.
     * @param numero         Número do endereço. Opcional.
     */
    public Usuario(int id, String nome, String email, String cpf, LocalDate dataNascimento, String cep, String complemento, String numero) {
        this.id             = id;
        this.nome           = validarObrigatorio(nome, "Nome");
        this.email          = validarEmail(email);
        this.cpf            = validarCpf(cpf);
        this.dataNascimento = validarDataNascimento(dataNascimento);
        this.cep            = validarCep(cep);
        this.complemento    = complemento;
        this.numero         = numero;
    }

    // ── Validações (privadas — contrato interno da classe) ──────────────────

    public String validarObrigatorio(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty())
            throw new IllegalArgumentException(campo + " inválido: não pode ser nulo ou vazio.");
        return valor.trim();
    }

    public String validarEmail(String email) {
        if (email == null || email.trim().isEmpty())
            throw new IllegalArgumentException("Email inválido: não pode ser nulo ou vazio.");
        String e = email.trim().toLowerCase();
        int arroba = e.indexOf("@");
        if (arroba < 1 || !e.substring(arroba).contains("."))
            throw new IllegalArgumentException("Email inválido: formato incorreto.");
        return e;
    }

    public String validarCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty())
            throw new IllegalArgumentException("CPF inválido: não pode ser nulo ou vazio.");
        String digitos = cpf.replaceAll("[^0-9]", "");
        if (digitos.length() != 11)
            throw new IllegalArgumentException("CPF inválido: deve conter 11 dígitos.");
        return digitos;
    }

    public LocalDate validarDataNascimento(LocalDate data) {
        if (data == null)
            throw new IllegalArgumentException("Data de nascimento não pode ser nula.");
        if (data.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Data de nascimento não pode ser no futuro.");
        return data;
    }

    public String validarCep(String cep) {
        if (cep == null || cep.trim().isEmpty()) return null;
        String digitos = cep.replaceAll("[^0-9]", "");
        if (digitos.length() != 8)
            throw new IllegalArgumentException("CEP inválido: deve conter 8 dígitos.");
        return digitos;
    }

    // ── Getters ─────────────────────────────────────────────────────────────

    public int       getId()             { return id; }
    public String    getNome()           { return nome; }
    public String    getEmail()          { return email; }
    public String    getCpf()            { return cpf; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public String    getCep()            { return cep; }
    public String    getComplemento()    { return complemento; }
    public String    getNumero()         { return numero; }

    /**
     * Calcula a idade dinamicamente.
     * Period.between() computa a diferença exata entre duas datas em anos.
     */
    public int getIdade() {
        if (dataNascimento == null) return 0;
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }

    // ── Setters ─────────────────────────────────────────────────────────────

    public void setId(int id)                          { this.id = id; }
    public void setEmail(String email)                 { this.email = validarEmail(email); }
    public void setCpf(String cpf)                     { this.cpf = validarCpf(cpf); }
    public void setDataNascimento(LocalDate data)      { this.dataNascimento = validarDataNascimento(data); }
    public void setCep(String cep)                     { this.cep = validarCep(cep); }
    public void setComplemento(String complemento)     { this.complemento = complemento; }
    public void setNumero(String numero)               { this.numero = numero; }

    // ── Exibição ─────────────────────────────────────────────────────────────

    public String toStringEndereco() {
        return """
               === Endere\u00e7o Completo ===
               CEP: """         + (cep         != null ? cep         : "não informado") +
               "\nNúmero: "      + (numero      != null ? numero      : "não informado") +
               "\nComplemento: " + (complemento != null ? complemento : "não informado");
    }

    @Override
    public String toString() {
        return "Usuario #" + id +
               " | Nome: "  + nome +
               " | Email: " + email +
               " | Idade: " + getIdade() + " anos";
    }
}