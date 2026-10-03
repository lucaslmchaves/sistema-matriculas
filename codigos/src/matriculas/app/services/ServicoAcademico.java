package matriculas.app.services;

import matriculas.infrastructure.BancoDeDados;
import matriculas.domain.entities.*;
import java.util.List;

/**
 * Serviço responsável por manter a estrutura acadêmica da universidade (Cursos, Disciplinas, Usuários).
 * Focado no Princípio de Responsabilidade Única (SRP).
 */
public class ServicoAcademico {
    private final BancoDeDados db;

    public ServicoAcademico(BancoDeDados db) { 
        this.db = db; 
    }

    public void cadastrarCurso(String nome, int creditos) {
        db.cursos.add(new Curso(nome, creditos));
    }

    public void cadastrarDisciplina(String nome, Curso curso) {
        Disciplina d = new Disciplina(nome, curso);
        db.disciplinas.add(d);
        curso.adicionarDisciplina(d);
    }

    public void cadastrarAluno(String nome, String matricula, String login, String senha) {
        Aluno a = new Aluno(nome, matricula, login, senha);
        db.alunos.add(a);
        db.usuarios.add(a);
    }

    public void cadastrarProfessor(String nome, String login, String senha) {
        Professor p = new Professor(nome, login, senha);
        db.professores.add(p);
        db.usuarios.add(p);
    }

    public void vincularProfessor(Disciplina disciplina, Professor professor) {
        disciplina.setProfessor(professor);
    }

    public List<Curso> listarCursos() { return db.cursos; }
    public List<Disciplina> listarDisciplinas() { return db.disciplinas; }
    public List<Professor> listarProfessores() { return db.professores; }
    public List<Aluno> listarAlunos() { return db.alunos; }
}