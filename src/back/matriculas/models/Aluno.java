package matriculas.models;

import matriculas.enums.StatusMatricula;
import matriculas.enums.TipoMatricula;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aluno da universidade. Guarda as próprias matrículas e sabe se ainda pode
 * se matricular em mais disciplinas (até 4 obrigatórias e 2 optativas).
 * Todos os alunos cadastrados ficam no registro estático {@code alunos}.
 */
public class Aluno extends Usuario {
    private static final int LIMITE_OBRIGATORIAS = 4;
    private static final int LIMITE_OPTATIVAS = 2;

    private static final List<Aluno> alunos = new ArrayList<>();

    private String nome;
    private String numeroMatricula;
    private List<Matricula> matriculas;

    public Aluno(String nome, String numeroMatricula, String login, String senha) {
        super(login, senha);
        this.nome = Validacao.texto(nome, "Nome do aluno");
        this.numeroMatricula = Validacao.texto(numeroMatricula, "Número de matrícula");
        this.matriculas = new ArrayList<>();
    }

    /** Construtor usado ao carregar do arquivo, onde a senha já está em hash. */
    public Aluno(String nome, String numeroMatricula, String login, String senhaCriptografada, boolean jaCriptografada) {
        super(login, senhaCriptografada, jaCriptografada);
        this.nome = Validacao.texto(nome, "Nome do aluno");
        this.numeroMatricula = Validacao.texto(numeroMatricula, "Número de matrícula");
        this.matriculas = new ArrayList<>();
    }

    /** Diz se o aluno ainda não atingiu o limite de matrículas ativas do tipo informado. */
    public boolean podeMatricular(TipoMatricula tipo) {
        if (tipo == null) {
            return false;
        }
        long ativasDoTipo = matriculas.stream()
            .filter(m -> m.getStatus() == StatusMatricula.ATIVA && m.getTipo() == tipo)
            .count();
        if (tipo == TipoMatricula.OBRIGATORIA) {
            return ativasDoTipo < LIMITE_OBRIGATORIAS;
        }
        return ativasDoTipo < LIMITE_OPTATIVAS;
    }

    /** Guarda uma matrícula na lista do aluno. Devolve false se ela for nula ou repetida. */
    public boolean adicionarMatricula(Matricula matricula) {
        if (matricula == null || matriculas.contains(matricula)) {
            return false;
        }
        return matriculas.add(matricula);
    }

    public String getNome() {
        return nome;
    }

    public String getNumeroMatricula() {
        return numeroMatricula;
    }

    public List<Matricula> getMatriculas() {
        return Collections.unmodifiableList(matriculas);
    }

    /** Devolve todos os alunos cadastrados (lista somente leitura). */
    public static List<Aluno> listarTodos() {
        return Collections.unmodifiableList(alunos);
    }

    /** Adiciona um aluno ao registro geral. Devolve false se ele for nulo ou já estiver lá. */
    public static boolean registrar(Aluno aluno) {
        if (aluno == null || alunos.contains(aluno)) {
            return false;
        }
        return alunos.add(aluno);
    }

    /** Esvazia o registro (usado antes de recarregar os arquivos e nos testes). Devolve quantos foram removidos. */
    public static int limparRegistro() {
        int removidos = alunos.size();
        alunos.clear();
        return removidos;
    }
}
