package matriculas.services;

import matriculas.excecoes.RegraDeNegocioException;
import matriculas.models.Aluno;
import matriculas.models.Curso;
import matriculas.models.Disciplina;
import matriculas.models.Professor;
import matriculas.models.Validacao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Serviço usado pela secretaria para cadastrar cursos, disciplinas, professores
 * e alunos, impedindo duplicidades (mesmo nome, login ou matrícula).
 * Também faz as buscas que as outras camadas precisam.
 */
public class ServicoCadastro {
    private final List<Curso> cursos = new ArrayList<>();
    private final List<Professor> professores = new ArrayList<>();

    /** Cadastra um curso novo. O nome não pode repetir (sem diferenciar maiúsculas). */
    public Curso cadastrarCurso(String nome, int creditos) {
        Validacao.obrigatorio(nome, "Nome do curso");
        if (buscarCursoPorNome(nome) != null) {
            throw new RegraDeNegocioException("Já existe um curso cadastrado com este nome: " + nome.trim());
        }
        Curso curso = new Curso(nome, creditos);
        cursos.add(curso);
        return curso;
    }

    /** Cadastra uma disciplina dentro de um curso e a coloca no registro geral. */
    public Disciplina cadastrarDisciplina(String nome, Curso curso) {
        Validacao.obrigatorio(nome, "Nome da disciplina");
        if (curso == null) {
            throw new RegraDeNegocioException("O curso associado à disciplina não pode ser nulo.");
        }
        if (buscarDisciplinaPorNome(nome) != null) {
            throw new RegraDeNegocioException("Já existe uma disciplina cadastrada com este nome: " + nome.trim());
        }
        Disciplina disciplina = new Disciplina(nome, curso);
        curso.adicionarDisciplina(disciplina);
        Disciplina.registrar(disciplina);
        return disciplina;
    }

    /** Cadastra um professor. O login não pode repetir entre professores. */
    public Professor cadastrarProfessor(String nome, String login, String senha) {
        Validacao.obrigatorio(login, "Login do professor");
        if (buscarProfessorPorLogin(login) != null) {
            throw new RegraDeNegocioException("Já existe um professor cadastrado com este login: " + login.trim());
        }
        Professor professor = new Professor(nome, login, senha);
        professores.add(professor);
        return professor;
    }

    /** Cadastra um aluno. A matrícula e o login não podem repetir entre alunos. */
    public Aluno cadastrarAluno(String nome, String numeroMatricula, String login, String senha) {
        Validacao.obrigatorio(numeroMatricula, "Número de matrícula");
        Validacao.obrigatorio(login, "Login do aluno");
        if (buscarAlunoPorMatricula(numeroMatricula) != null) {
            throw new RegraDeNegocioException("Já existe um aluno com a matrícula: " + numeroMatricula.trim());
        }
        if (buscarAlunoPorLogin(login) != null) {
            throw new RegraDeNegocioException("Já existe um aluno com o login: " + login.trim());
        }
        Aluno aluno = new Aluno(nome, numeroMatricula, login, senha);
        Aluno.registrar(aluno);
        return aluno;
    }

    public List<Curso> listarCursos() {
        return Collections.unmodifiableList(cursos);
    }

    public List<Professor> listarProfessores() {
        return Collections.unmodifiableList(professores);
    }

    /** Recoloca um curso lido do arquivo, sem repetir as validações do cadastro. Devolve false se for nulo ou repetido. */
    public boolean restaurarCurso(Curso curso) {
        if (curso == null || cursos.contains(curso)) {
            return false;
        }
        return cursos.add(curso);
    }

    /** Recoloca um professor lido do arquivo, sem repetir as validações do cadastro. Devolve false se for nulo ou repetido. */
    public boolean restaurarProfessor(Professor professor) {
        if (professor == null || professores.contains(professor)) {
            return false;
        }
        return professores.add(professor);
    }

    /** Esvazia as listas deste serviço (usado antes de recarregar os arquivos). Devolve quantos itens foram removidos. */
    public int limpar() {
        int removidos = cursos.size() + professores.size();
        cursos.clear();
        professores.clear();
        return removidos;
    }

    public Curso buscarCursoPorNome(String nome) {
        return buscarPor(cursos, Curso::getNome, nome, true);
    }

    public Professor buscarProfessorPorLogin(String login) {
        return buscarPor(professores, Professor::getLogin, login, true);
    }

    public Aluno buscarAlunoPorMatricula(String matricula) {
        return buscarPor(Aluno.listarTodos(), Aluno::getNumeroMatricula, matricula, false);
    }

    public Aluno buscarAlunoPorLogin(String login) {
        return buscarPor(Aluno.listarTodos(), Aluno::getLogin, login, true);
    }

    public Disciplina buscarDisciplinaPorNome(String nome) {
        return buscarPor(Disciplina.listarTodos(), Disciplina::getNome, nome, true);
    }

    /**
     * Busca genérica: devolve o primeiro item cuja chave (nome, login...) é igual ao valor
     * informado, ou null se não houver. Evita repetir o mesmo laço em cada busca.
     */
    private static <T> T buscarPor(List<T> itens, Function<T, String> chave, String valor, boolean ignorarMaiusculas) {
        if (valor == null) {
            return null;
        }
        String procurado = valor.trim();
        for (T item : itens) {
            String atual = chave.apply(item);
            if (ignorarMaiusculas ? atual.equalsIgnoreCase(procurado) : atual.equals(procurado)) {
                return item;
            }
        }
        return null;
    }
}
