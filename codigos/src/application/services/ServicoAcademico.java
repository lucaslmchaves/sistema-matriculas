package application.services;

import domain.entities.*;
import infrastructure.BancoDeDados;

import java.util.Collections;
import java.util.List;

/**
 * Serviço responsável por manter e orquestrar a estrutura acadêmica da universidade.
 * Focado no Princípio de Responsabilidade Única (SRP), atuando como ponte 
 * entre a interface de usuário e o banco de dados para operações de cadastro e consulta.
 */
public class ServicoAcademico {
    
    private final BancoDeDados db;

    /**
     * Construtor do serviço com injeção de dependência rigorosa (Fail-Fast).
     * 
     * @param db A instância do banco de dados (simulado em memória).
     * @throws IllegalArgumentException Se o banco de dados fornecido for nulo.
     */
    public ServicoAcademico(BancoDeDados db) { 
        if (db == null) {
            throw new IllegalArgumentException("O banco de dados não pode ser nulo.");
        }
        this.db = db; 
    }

    /**
     * Instancia e cadastra um novo curso no sistema.
     * 
     * @param nome Nome do curso.
     * @param creditos Quantidade de créditos do curso.
     * @return true se salvo com sucesso.
     */
    public boolean cadastrarCurso(String nome, int creditos) {
        Curso curso = new Curso(nome, creditos);
        return db.cursos.add(curso);
    }

    /**
     * Cria uma nova disciplina vinculando-a ao seu respectivo curso.
     * 
     * @param nome Nome da disciplina.
     * @param curso Objeto curso já persistido no banco de dados.
     * @return true se cadastrada e vinculada com sucesso.
     * @throws IllegalArgumentException se o curso fornecido for nulo.
     */
    public boolean cadastrarDisciplina(String nome, Curso curso) {
        if (curso == null) {
            throw new IllegalArgumentException("O curso associado não pode ser nulo.");
        }
        Disciplina d = new Disciplina(nome, curso);
        
        boolean salvaNoBanco = db.disciplinas.add(d);
        boolean vinculadaAoCurso = curso.adicionarDisciplina(d);
        
        return salvaNoBanco && vinculadaAoCurso;
    }

    /**
     * Cadastra um novo aluno no sistema.
     * A ordem dos parâmetros reflete a herança da classe Usuario (nome, login, senha).
     * 
     * @param nome Nome completo do aluno.
     * @param login Login de acesso ao sistema.
     * @param senha Senha de autenticação.
     * @param matricula Número de matrícula (RA) único do aluno.
     * @return true se o aluno for salvo com sucesso nas listas de Alunos e Usuários.
     */
    public boolean cadastrarAluno(String nome, String login, String senha, String matricula) {
        Aluno a = new Aluno(nome, login, senha, matricula);
        boolean salvoAluno = db.alunos.add(a);
        boolean salvoUsuario = db.usuarios.add(a);
        return salvoAluno && salvoUsuario;
    }

    /**
     * Cadastra um novo professor no sistema.
     * 
     * @param nome Nome completo do professor.
     * @param login Login de acesso ao sistema.
     * @param senha Senha de autenticação.
     * @return true se o professor for salvo com sucesso nas listas de Professores e Usuários.
     */
    public boolean cadastrarProfessor(String nome, String login, String senha) {
        Professor p = new Professor(nome, login, senha);
        boolean salvoProf = db.professores.add(p);
        boolean salvoUsuario = db.usuarios.add(p);
        return salvoProf && salvoUsuario;
    }

    /**
     * Atribui um professor responsável por uma disciplina.
     * 
     * @param disciplina A disciplina que receberá o docente.
     * @param professor O professor a ser vinculado.
     * @return true se o vínculo for estabelecido com sucesso.
     * @throws IllegalArgumentException se a disciplina ou o professor forem nulos.
     */
    public boolean vincularProfessor(Disciplina disciplina, Professor professor) {
        if (disciplina == null || professor == null) {
            throw new IllegalArgumentException("Disciplina e Professor não podem ser nulos no vínculo.");
        }
        return disciplina.setProfessor(professor);
    }

    //#region Listagens Blindadas (Read-Only) para a Interface
    
    /**
     * @return Uma lista imutável com todos os cursos cadastrados.
     */
    public List<Curso> listarCursos() { 
        return Collections.unmodifiableList(db.cursos); 
    }
    
    /**
     * @return Uma lista imutável com todas as disciplinas cadastradas.
     */
    public List<Disciplina> listarDisciplinas() { 
        return Collections.unmodifiableList(db.disciplinas); 
    }
    
    /**
     * @return Uma lista imutável com todos os professores do sistema.
     */
    public List<Professor> listarProfessores() { 
        return Collections.unmodifiableList(db.professores); 
    }
    
    /**
     * @return Uma lista imutável com todos os alunos matriculados na instituição.
     */
    public List<Aluno> listarAlunos() { 
        return Collections.unmodifiableList(db.alunos); 
    }
    
    //#endregion
}