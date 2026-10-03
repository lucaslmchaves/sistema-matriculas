package matriculas.domain.entities;

import matriculas.domain.enums.StatusDisciplina;
import java.util.ArrayList;
import java.util.List;

public class Disciplina {
    private final String nome;
    private final Curso curso;
    private Professor professor;
    private StatusDisciplina status;
    private final List<Matricula> matriculas;

    public Disciplina(String nome, Curso curso) {
        this.nome = nome;
        this.curso = curso;
        this.status = StatusDisciplina.EM_ABERTO;
        this.matriculas = new ArrayList<>();
    }

    public String getNome() { return nome; }
    public Curso getCurso() { return curso; }
    public Professor getProfessor() { return professor; }
    public StatusDisciplina getStatus() { return status; }
    public boolean isAberta() { return status == StatusDisciplina.EM_ABERTO; }

    public void setProfessor(Professor p) { this.professor = p; }
    
    public void registrarMatricula(Matricula m) {
        if (!isAberta()) throw new IllegalStateException("A disciplina não está aberta.");
        if (matriculas.size() >= 60) throw new IllegalStateException("Capacidade máxima cheia.");
        this.matriculas.add(m);
    }
    
    public List<Aluno> listarAlunos() {
        return matriculas.stream().filter(Matricula::isAtiva).map(Matricula::getAluno).toList();
    }
}