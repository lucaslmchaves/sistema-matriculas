package matriculas.domain.entities;
import matriculas.domain.enums.StatusMatricula;
import matriculas.domain.enums.TipoMatricula;

public class Matricula {
    private final Aluno aluno;
    private final Disciplina disciplina;
    private final TipoMatricula tipo;
    private StatusMatricula status;

    public Matricula(Aluno aluno, Disciplina disciplina, TipoMatricula tipo) {
        this.aluno = aluno;
        this.disciplina = disciplina;
        this.tipo = tipo;
        this.status = StatusMatricula.ATIVA;
    }

    public Aluno getAluno() { return aluno; }
    public Disciplina getDisciplina() { return disciplina; }
    public TipoMatricula getTipo() { return tipo; }
    public boolean isAtiva() { return status == StatusMatricula.ATIVA; }
    public void cancelar() { this.status = StatusMatricula.CANCELADA; }
}
