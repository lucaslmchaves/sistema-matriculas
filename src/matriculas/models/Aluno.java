package matriculas.models;

import java.util.List;

public class Aluno extends Usuario {
    private String nome;
    private String numeroMatricula;
    private List<Matricula> matriculas;
    private List<Aluno> alunos;
}
