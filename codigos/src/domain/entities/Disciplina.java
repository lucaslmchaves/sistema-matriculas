package domain.entities;

import java.util.ArrayList;
import java.util.List;

import domain.enums.StatusDisciplina;

/**
 * Entidade que representa uma Disicplina na grande curricular.
 * É responsável por gerenciar seus alunos e garantir as regras
 * de lotação de turma.
 */
public class Disciplina {

    /** Limite máximo de vagas permitidas para uma turma. */
    public static final int LIMITE_MAXIMO_ALUNOS = 60;
    
    /** Quórum mínimo de alunos para a disciplina não ser cancelada. */
    public static final int QUORUM_MINIMO_ALUNOS = 3;

    private final String nome;
    private final Curso curso;
    private Professor professor;
    private final List<Matricula> matriculas;
    
    private StatusDisciplina status;

    /**
     * Construtor da entidade com validação de estado incial (Fail-Fast).
     * 
     * @param nome Nome da diciplina
     * @param curso Curso associado
     * @throws IllegalArgumentException se o nome for nulo/vazio ou se o curso for nulo.
     */
    public Disciplina(String nome, Curso curso){

        if(nome == null || nome.trim().isEmpty())
            throw new IllegalArgumentException("O nome da disciplina não pode ser nulo ou vazio");

        if(curso == null)
            throw new IllegalArgumentException("A disciplina deve obrigatoriamente pertencer um curso");

        this.nome = nome;
        this.curso = curso;
        this.status = StatusDisciplina.EM_ABERTO;
        this.matriculas = new ArrayList<>();
    }

    //#region getters e setters para a interface
    public String getNome() { return nome; }
    public Curso getCurso() { return curso; }
    public Professor getProfessor() { return professor; }
    public StatusDisciplina getStatus() { return status; }
    //#endregion
    
    /**
     * @return true se a disciplina aceita novas matrículas (status EM_ABERTO).
     */
    public boolean isAberta() { 
        return this.status == StatusDisciplina.EM_ABERTO; 
    }

    /**
     * Atrubui um professor à disciplina.
     * 
     * @param p O professor a ser designado.
     * @return true se o professor for atribuído, false se o parâmetro for nulo.
     */
    public boolean setProfessor(Professor p){
        if(p == null) return false;
        this.professor = p;
        return true;
    }

    /**
     * Valida se o quórum mínimo foi antigindo com alunos ativos.
     * 
     * @return true se houver 3 ou mais matrículas ativas.
     */
    public boolean isConfirmada(){
        long alunosAtivos = this.matriculas.stream()
                        .filter(Matricula::isAtiva)
                        .count();
        
        return alunosAtivos >= QUORUM_MINIMO_ALUNOS;
    }

    /**
     * Tentar registrar uma matrícula garantindo regras de status e lotação.
     * 
     * @param matricula Objeto de matrícula do aluno.
     * @return true se a matrícula for adicionada com sucesso à lista.
     * @throws IllegalArgumentException Se a matrícula fornecida for nula.
     * @throws IllegalStateException Se a a disciplina não estiver aberta ou se o limite
     * de vagas for atingido.
     */
    public boolean registrarMatricula(Matricula matricula){
        if(matricula == null)
            throw new IllegalArgumentException("A matrícula não pode ser nula. ");

        if(!isAberta())
            return false;

        
        long alunosAtivos = this.matriculas.stream()
                                .filter(Matricula::isAtiva)
                                .count();

        if(alunosAtivos >= LIMITE_MAXIMO_ALUNOS)
            return false;
        
        return this.matriculas.add(matricula);
    }
    
    /**
     * Lista todos os alunos vinculados a matrículas ativas.
     * @return Lista de entidades Aluno.
     */
    public List<Aluno> listarAlunos() {
        return this.matriculas.stream()
                            .filter(Matricula::isAtiva)
                            .map(Matricula::getAluno)
                            .toList();
    }
}