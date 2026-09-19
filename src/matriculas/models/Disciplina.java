package matriculas.models;

import matriculas.enums.StatusDisciplina;

import java.util.List;

public class Disciplina {
    private static final int CAPACIDADE_MAXIMA = 60;
    private static final int NUMERO_MINIMO_ALUNOS = 3;
    private String nome;
    private StatusDisciplina status;
    private List<Disciplina> disciplinas;

    public List<Aluno> listarAlunosMatriculados() {
        // TODO: implementar
        return null;
    }

    public int quantidadeMatriculados() {
        // TODO: implementar
        return 0;
    }
}
