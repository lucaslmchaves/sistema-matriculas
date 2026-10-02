package matriculas.models;

import matriculas.enums.StatusDisciplina;
import matriculas.enums.StatusMatricula;
import matriculas.excecoes.RegraDeNegocioException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Disciplina de um curso. Conhece a capacidade máxima (60 alunos) e o mínimo
 * para ser confirmada (3 alunos), por isso responde sozinha se tem vaga e se
 * atingiu o mínimo. Todas as disciplinas ficam no registro estático {@code disciplinas}.
 */
public class Disciplina {
    private static final int CAPACIDADE_MAXIMA = 60;
    private static final int NUMERO_MINIMO_ALUNOS = 3;

    private static final List<Disciplina> disciplinas = new ArrayList<>();

    private String nome;
    private StatusDisciplina status;
    private Professor professor;
    private Curso curso;

    public Disciplina(String nome) {
        Validacao.texto(nome, "Nome da disciplina");
        this.nome = nome.trim();
        this.status = StatusDisciplina.EM_ABERTO;
    }

    public Disciplina(String nome, Curso curso) {
        this(nome);
        this.curso = curso;
    }

    /** Construtor usado ao carregar do arquivo, quando o status já foi definido antes. */
    public Disciplina(String nome, Curso curso, StatusDisciplina status) {
        this(nome, curso);
        if (status == null) {
            throw new RegraDeNegocioException("Status da disciplina não pode ser nulo.");
        }
        this.status = status;
    }

    /** Lista os alunos com matrícula ativa nesta disciplina, sem repetir nenhum. */
    public List<Aluno> listarAlunosMatriculados() {
        List<Aluno> alunosMatriculados = new ArrayList<>();
        for (Matricula matricula : matriculasAtivas()) {
            Aluno aluno = matricula.getAluno();
            if (!alunosMatriculados.contains(aluno)) {
                alunosMatriculados.add(aluno);
            }
        }
        return Collections.unmodifiableList(alunosMatriculados);
    }

    public int quantidadeMatriculados() {
        return matriculasAtivas().size();
    }

    /** Verdadeiro enquanto a disciplina não atingiu a capacidade máxima. */
    public boolean temVaga() {
        return quantidadeMatriculados() < CAPACIDADE_MAXIMA;
    }

    public int vagasRestantes() {
        return CAPACIDADE_MAXIMA - quantidadeMatriculados();
    }

    /** Verdadeiro se há alunos suficientes para a disciplina ser confirmada. */
    public boolean atingiuMinimoDeAlunos() {
        return quantidadeMatriculados() >= NUMERO_MINIMO_ALUNOS;
    }

    public void atribuirProfessor(Professor professor) {
        if (professor == null) {
            throw new RegraDeNegocioException("Professor não pode ser nulo.");
        }
        this.professor = professor;
    }

    public String getNome() {
        return nome;
    }

    public StatusDisciplina getStatus() {
        return status;
    }

    /** Só o período de matrículas (mesmo pacote) confirma ou cancela uma disciplina. */
    void setStatus(StatusDisciplina status) {
        if (status == null) {
            throw new RegraDeNegocioException("Status da disciplina não pode ser nulo.");
        }
        this.status = status;
    }

    public Professor getProfessor() {
        return professor;
    }

    public Curso getCurso() {
        return curso;
    }

    /** Devolve todas as disciplinas cadastradas (lista somente leitura). */
    public static List<Disciplina> listarTodos() {
        return Collections.unmodifiableList(disciplinas);
    }

    /** Adiciona uma disciplina ao registro geral. */
    public static void registrar(Disciplina disciplina) {
        if (disciplina != null && !disciplinas.contains(disciplina)) {
            disciplinas.add(disciplina);
        }
    }

    /** Esvazia o registro (usado antes de recarregar os arquivos e nos testes). */
    public static void limparRegistro() {
        disciplinas.clear();
    }

    /** Matrículas ativas desta disciplina; base para contar vagas e listar alunos. */
    List<Matricula> matriculasAtivas() {
        List<Matricula> ativas = new ArrayList<>();
        for (Matricula matricula : Matricula.listarTodos()) {
            if (this.equals(matricula.getDisciplina()) && matricula.getStatus() == StatusMatricula.ATIVA) {
                ativas.add(matricula);
            }
        }
        return ativas;
    }
}
