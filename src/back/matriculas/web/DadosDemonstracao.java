package matriculas.web;

import matriculas.excecoes.RegraDeNegocioException;
import matriculas.models.Aluno;
import matriculas.models.Curso;
import matriculas.models.Disciplina;
import matriculas.models.PeriodoMatricula;
import matriculas.models.Professor;
import matriculas.persistencia.Persistencia;
import matriculas.services.ServicoCadastro;

import java.util.ArrayList;
import java.util.List;

/**
 * Cria um conjunto de dados de exemplo (1 curso, 2 professores, 5 alunos e 4 disciplinas)
 * para demonstrar o sistema sem precisar cadastrar tudo na mão. Usa o ServicoCadastro,
 * então as mesmas validações do cadastro normal valem aqui.
 */
public class DadosDemonstracao {
    private static final String SENHA_PADRAO = "1234";

    /** Descrição do que foi criado, para a tela mostrar os logins de exemplo. */
    public static class ResultadoCarga {
        private final List<String> professores = new ArrayList<>();
        private final List<String> alunos = new ArrayList<>();
        private final List<String> disciplinas = new ArrayList<>();

        public List<String> getProfessores() {
            return professores;
        }

        public List<String> getAlunos() {
            return alunos;
        }

        public List<String> getDisciplinas() {
            return disciplinas;
        }
    }

    /** Carrega os dados de exemplo. Só funciona com o sistema vazio, para não misturar com dados reais. */
    public static ResultadoCarga carregar(ServicoCadastro servicoCadastro, PeriodoMatricula periodo, Persistencia persistencia) {
        if (!sistemaVazio(servicoCadastro)) {
            throw new RegraDeNegocioException("Já existem dados cadastrados no sistema.");
        }

        ResultadoCarga resultado = new ResultadoCarga();
        Curso curso = servicoCadastro.cadastrarCurso("Engenharia de Software", 240);

        Professor ana = cadastrarProfessor(servicoCadastro, resultado, "Ana Souza", "ana");
        Professor carlos = cadastrarProfessor(servicoCadastro, resultado, "Carlos Lima", "carlos");

        for (int i = 1; i <= 5; i++) {
            cadastrarAluno(servicoCadastro, resultado, i);
        }

        cadastrarDisciplina(servicoCadastro, periodo, resultado, curso, "Projeto de Software", ana);
        cadastrarDisciplina(servicoCadastro, periodo, resultado, curso, "Engenharia de Requisitos", ana);
        cadastrarDisciplina(servicoCadastro, periodo, resultado, curso, "Algoritmos e Estruturas de Dados", carlos);
        cadastrarDisciplina(servicoCadastro, periodo, resultado, curso, "Banco de Dados", carlos);

        persistencia.salvar(servicoCadastro, periodo);
        return resultado;
    }

    private static boolean sistemaVazio(ServicoCadastro servicoCadastro) {
        return servicoCadastro.listarCursos().isEmpty()
                && servicoCadastro.listarProfessores().isEmpty()
                && Aluno.listarTodos().isEmpty()
                && Disciplina.listarTodos().isEmpty();
    }

    private static Professor cadastrarProfessor(ServicoCadastro servicoCadastro, ResultadoCarga resultado, String nome, String login) {
        Professor professor = servicoCadastro.cadastrarProfessor(nome, login, SENHA_PADRAO);
        resultado.professores.add(login + " (senha: " + SENHA_PADRAO + ")");
        return professor;
    }

    private static void cadastrarAluno(ServicoCadastro servicoCadastro, ResultadoCarga resultado, int numero) {
        String matricula = String.valueOf(1000 + numero);
        String login = "aluno" + numero;
        servicoCadastro.cadastrarAluno("Aluno Exemplo " + numero, matricula, login, SENHA_PADRAO);
        resultado.alunos.add(login + " (matrícula: " + matricula + ", senha: " + SENHA_PADRAO + ")");
    }

    /** Cadastra a disciplina, atribui o professor e já a oferece no período (que continua fechado). */
    private static void cadastrarDisciplina(ServicoCadastro servicoCadastro, PeriodoMatricula periodo, ResultadoCarga resultado,
                                            Curso curso, String nome, Professor professor) {
        Disciplina disciplina = servicoCadastro.cadastrarDisciplina(nome, curso);
        disciplina.atribuirProfessor(professor);
        periodo.adicionarDisciplina(disciplina);
        resultado.disciplinas.add(nome + " (Prof. " + professor.getNome() + ")");
    }
}
