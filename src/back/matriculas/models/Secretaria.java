package matriculas.models;

/** Funcionário da secretaria. Cadastra cursos, disciplinas, professores e alunos e controla o período de matrículas. */
public class Secretaria extends Usuario {
    private String nome;

    public Secretaria(String nome, String login, String senha) {
        super(login, senha);
        Validacao.texto(nome, "Nome da secretaria");
        this.nome = nome.trim();
    }

    public String getNome() {
        return nome;
    }
}
