package matriculas.web;

import matriculas.enums.StatusDisciplina;
import matriculas.enums.StatusMatricula;
import matriculas.enums.TipoMatricula;
import matriculas.excecoes.RegraDeNegocioException;
import matriculas.models.Aluno;
import matriculas.models.Curso;
import matriculas.models.Disciplina;
import matriculas.models.Matricula;
import matriculas.models.PeriodoMatricula;
import matriculas.models.Professor;
import matriculas.persistencia.Persistencia;
import matriculas.services.ServicoCadastro;
import matriculas.services.ServicoMatricula;

import java.util.ArrayList;
import java.util.List;

/**
 * Dados de exemplo para demonstrar e testar o sistema sem cadastrar tudo na mão.
 * Tudo passa pelos serviços do domínio, então as mesmas regras do uso normal valem aqui
 * (limites de matrículas, período aberto, vagas...).
 * Há três operações: carregar um conjunto de cadastros, gerar matrículas para os alunos
 * existentes e lotar uma disciplina para testar o limite de vagas.
 */
public class DadosDemonstracao {
    private static final String SENHA_PADRAO = "1234";
    private static final int MINIMO_PARA_CONFIRMAR = 3;

    /** Tamanho do conjunto de cadastros de exemplo. */
    public enum Conjunto {
        BASICO,
        COMPLETO;

        /** Converte o texto recebido pela API ("basico" ou "completo"). */
        public static Conjunto de(String texto) {
            if (texto == null || texto.trim().isEmpty()) {
                return BASICO;
            }
            try {
                return valueOf(texto.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RegraDeNegocioException("Conjunto inválido. Use basico ou completo.");
            }
        }
    }

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

    /** Carrega o conjunto básico (1 curso, 2 professores, 5 alunos e 4 disciplinas). */
    public static ResultadoCarga carregar(ServicoCadastro servicoCadastro, PeriodoMatricula periodo, Persistencia persistencia) {
        return carregar(servicoCadastro, periodo, persistencia, Conjunto.BASICO);
    }

    /**
     * Cadastra o conjunto escolhido e já oferece as disciplinas no período (que continua
     * fechado). Só funciona com o sistema vazio, para não misturar com dados reais.
     * O conjunto completo tem 2 cursos, 4 professores, 20 alunos e 8 disciplinas.
     */
    public static ResultadoCarga carregar(ServicoCadastro servicoCadastro, PeriodoMatricula periodo,
                                          Persistencia persistencia, Conjunto conjunto) {
        if (!sistemaVazio(servicoCadastro)) {
            throw new RegraDeNegocioException("Já existem dados cadastrados no sistema.");
        }

        ResultadoCarga resultado = new ResultadoCarga();
        Curso engenharia = servicoCadastro.cadastrarCurso("Engenharia de Software", 240);
        Professor ana = cadastrarProfessor(servicoCadastro, resultado, "Ana Souza", "ana");
        Professor carlos = cadastrarProfessor(servicoCadastro, resultado, "Carlos Lima", "carlos");

        boolean completo = conjunto == Conjunto.COMPLETO;
        for (int i = 1; i <= (completo ? 20 : 5); i++) {
            cadastrarAluno(servicoCadastro, resultado, i);
        }

        cadastrarDisciplina(servicoCadastro, periodo, resultado, engenharia, "Projeto de Software", ana);
        cadastrarDisciplina(servicoCadastro, periodo, resultado, engenharia, "Engenharia de Requisitos", ana);
        cadastrarDisciplina(servicoCadastro, periodo, resultado, engenharia, "Algoritmos e Estruturas de Dados", carlos);
        cadastrarDisciplina(servicoCadastro, periodo, resultado, engenharia, "Banco de Dados", carlos);

        if (completo) {
            Curso computacao = servicoCadastro.cadastrarCurso("Ciência da Computação", 240);
            Professor beatriz = cadastrarProfessor(servicoCadastro, resultado, "Beatriz Alves", "beatriz");
            Professor diego = cadastrarProfessor(servicoCadastro, resultado, "Diego Rocha", "diego");
            cadastrarDisciplina(servicoCadastro, periodo, resultado, computacao, "Redes de Computadores", beatriz);
            cadastrarDisciplina(servicoCadastro, periodo, resultado, computacao, "Sistemas Operacionais", beatriz);
            cadastrarDisciplina(servicoCadastro, periodo, resultado, computacao, "Inteligência Artificial", diego);
            cadastrarDisciplina(servicoCadastro, periodo, resultado, computacao, "Compiladores", diego);
        }

        persistencia.salvar(servicoCadastro, periodo);
        return resultado;
    }

    /**
     * Matricula os alunos existentes nas disciplinas do período, com quantidades diferentes
     * por disciplina: a primeira recebe todos os alunos, a segunda metade, a terceira o
     * mínimo para ser confirmada e a quarta menos que isso (repetindo a cada quatro).
     * Assim, ao encerrar o período, há disciplinas confirmadas e canceladas.
     * Respeita o limite de cada aluno (4 obrigatórias e 2 optativas). Devolve quantas
     * matrículas foram criadas.
     */
    public static int gerarMatriculas(ServicoCadastro servicoCadastro, ServicoMatricula servicoMatricula,
                                      PeriodoMatricula periodo, Persistencia persistencia) {
        List<Aluno> alunos = Aluno.listarTodos();
        List<Disciplina> disciplinas = periodo.getDisciplinas();
        if (alunos.isEmpty() || disciplinas.isEmpty()) {
            throw new RegraDeNegocioException("Cadastre alunos e coloque disciplinas no período antes de gerar matrículas.");
        }

        int criadas = 0;
        for (int d = 0; d < disciplinas.size(); d++) {
            int quantos = quantidadeDeAlunos(d, alunos.size());
            for (int a = 0; a < quantos; a++) {
                if (matricular(servicoMatricula, alunos.get(a), disciplinas.get(d))) {
                    criadas++;
                }
            }
        }

        persistencia.salvar(servicoCadastro, periodo);
        return criadas;
    }

    /**
     * Cria alunos novos e os matricula na disciplina até ela atingir a capacidade máxima,
     * para testar o bloqueio de matrículas quando não há mais vagas. Devolve quantos
     * alunos foram criados.
     */
    public static int lotarDisciplina(ServicoCadastro servicoCadastro, ServicoMatricula servicoMatricula,
                                      PeriodoMatricula periodo, Persistencia persistencia, String nomeDisciplina) {
        Disciplina disciplina = servicoCadastro.buscarDisciplinaPorNome(nomeDisciplina);
        if (disciplina == null) {
            throw new RegraDeNegocioException("Disciplina não encontrada: " + nomeDisciplina);
        }
        if (!periodo.isAberto()) {
            throw new RegraDeNegocioException("O período de matrículas não está aberto.");
        }
        if (!periodo.getDisciplinas().contains(disciplina) || disciplina.getStatus() == StatusDisciplina.CANCELADA) {
            throw new RegraDeNegocioException("A disciplina precisa estar no período e não pode estar cancelada.");
        }
        if (!disciplina.temVaga()) {
            throw new RegraDeNegocioException("A disciplina já está lotada.");
        }

        int criados = 0;
        int numero = 0;
        while (disciplina.temVaga()) {
            numero = proximoNumeroLivre(servicoCadastro, numero);
            Aluno aluno = servicoCadastro.cadastrarAluno("Aluno Lotação " + numero, "L" + String.format("%03d", numero),
                    "lotacao" + numero, SENHA_PADRAO);
            servicoMatricula.efetuarMatricula(aluno, disciplina, TipoMatricula.OBRIGATORIA);
            criados++;
        }

        persistencia.salvar(servicoCadastro, periodo);
        return criados;
    }

    private static boolean sistemaVazio(ServicoCadastro servicoCadastro) {
        return servicoCadastro.listarCursos().isEmpty()
                && servicoCadastro.listarProfessores().isEmpty()
                && Aluno.listarTodos().isEmpty()
                && Disciplina.listarTodos().isEmpty();
    }

    /** Quantos alunos entram na disciplina de índice d, seguindo o padrão descrito em gerarMatriculas. */
    private static int quantidadeDeAlunos(int indiceDisciplina, int totalAlunos) {
        switch (indiceDisciplina % 4) {
            case 0:
                return totalAlunos;
            case 1:
                return totalAlunos / 2;
            case 2:
                return Math.min(MINIMO_PARA_CONFIRMAR, totalAlunos);
            default:
                return Math.min(MINIMO_PARA_CONFIRMAR - 1, totalAlunos);
        }
    }

    /**
     * Tenta matricular o aluno como obrigatória e, se o limite estiver cheio, como optativa.
     * Pula se ele já está matriculado ou não pode mais nenhum dos dois tipos.
     */
    private static boolean matricular(ServicoMatricula servicoMatricula, Aluno aluno, Disciplina disciplina) {
        if (jaMatriculado(aluno, disciplina)) {
            return false;
        }
        TipoMatricula tipo;
        if (aluno.podeMatricular(TipoMatricula.OBRIGATORIA)) {
            tipo = TipoMatricula.OBRIGATORIA;
        } else if (aluno.podeMatricular(TipoMatricula.OPTATIVA)) {
            tipo = TipoMatricula.OPTATIVA;
        } else {
            return false;
        }
        if (!disciplina.temVaga()) {
            return false;
        }
        servicoMatricula.efetuarMatricula(aluno, disciplina, tipo);
        return true;
    }

    private static boolean jaMatriculado(Aluno aluno, Disciplina disciplina) {
        for (Matricula matricula : aluno.getMatriculas()) {
            if (disciplina.equals(matricula.getDisciplina()) && matricula.getStatus() == StatusMatricula.ATIVA) {
                return true;
            }
        }
        return false;
    }

    /** Primeiro número depois de "atual" cujo login "lotacaoN" ainda não existe (permite lotar mais de uma vez). */
    private static int proximoNumeroLivre(ServicoCadastro servicoCadastro, int atual) {
        int numero = atual + 1;
        while (servicoCadastro.buscarAlunoPorLogin("lotacao" + numero) != null) {
            numero++;
        }
        return numero;
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
