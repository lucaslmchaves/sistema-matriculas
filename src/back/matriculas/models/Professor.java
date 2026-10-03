package matriculas.models;

/** Professor da universidade. Consulta os alunos matriculados nas disciplinas que leciona. */
public class Professor extends Usuario {
    private String nome;

    public Professor(String nome, String login, String senha) {
        super(login, senha);
        this.nome = Validacao.texto(nome, "Nome do professor");
    }

    /** Construtor usado ao carregar do arquivo, onde a senha já está em hash. */
    public Professor(String nome, String login, String senhaCriptografada, boolean jaCriptografada) {
        super(login, senhaCriptografada, jaCriptografada);
        this.nome = Validacao.texto(nome, "Nome do professor");
    }

    public String getNome() {
        return nome;
    }
}
