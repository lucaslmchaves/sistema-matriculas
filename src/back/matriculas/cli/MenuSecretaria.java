package matriculas.cli;

import matriculas.excecoes.RegraDeNegocioException;
import matriculas.models.Aluno;
import matriculas.models.Curso;
import matriculas.models.Disciplina;
import matriculas.models.PeriodoMatricula;
import matriculas.models.Professor;
import matriculas.persistencia.Persistencia;
import matriculas.services.ServicoCadastro;

import java.util.List;

/**
 * Painel da secretaria no console: cadastros, controle do período de matrículas e
 * listagens. Toda operação que altera dados grava os arquivos em seguida.
 */
public class MenuSecretaria {
    private final ServicoCadastro servicoCadastro;
    private final PeriodoMatricula periodo;
    private final Persistencia persistencia;
    private final EntradaConsole entrada;

    public MenuSecretaria(ServicoCadastro servicoCadastro, PeriodoMatricula periodo, Persistencia persistencia, EntradaConsole entrada) {
        this.servicoCadastro = servicoCadastro;
        this.periodo = periodo;
        this.persistencia = persistencia;
        this.entrada = entrada;
    }

    /** Repete o menu até a secretaria voltar. Violações de regra viram mensagem, sem derrubar o programa. */
    public void exibir() {
        while (true) {
            exibirOpcoes();
            Integer opcao = entrada.lerInteiro("Opção");
            if (opcao == null || opcao == 0) {
                break;
            }
            try {
                executarOpcao(opcao);
            } catch (RegraDeNegocioException e) {
                System.out.println("[ERRO] " + e.getMessage());
            }
        }
    }

    private void exibirOpcoes() {
        System.out.println("\n=== Painel da Secretaria ===");
        System.out.println("1. Cadastrar curso");
        System.out.println("2. Cadastrar professor");
        System.out.println("3. Cadastrar aluno");
        System.out.println("4. Cadastrar disciplina");
        System.out.println("5. Adicionar disciplina ao período de matrículas");
        System.out.println("6. Abrir período de matrículas");
        System.out.println("7. Encerrar período de matrículas");
        System.out.println("8. Listar cursos");
        System.out.println("9. Listar disciplinas");
        System.out.println("10. Listar professores");
        System.out.println("11. Listar alunos");
        System.out.println("0. Voltar ao menu principal");
    }

    private void executarOpcao(int opcao) {
        switch (opcao) {
            case 1:
                cadastrarCurso();
                break;
            case 2:
                cadastrarProfessor();
                break;
            case 3:
                cadastrarAluno();
                break;
            case 4:
                cadastrarDisciplina();
                break;
            case 5:
                adicionarDisciplinaAoPeriodo();
                break;
            case 6:
                abrirPeriodo();
                break;
            case 7:
                encerrarPeriodo();
                break;
            case 8:
                listarCursos();
                break;
            case 9:
                listarDisciplinas();
                break;
            case 10:
                listarProfessores();
                break;
            case 11:
                listarAlunos();
                break;
            default:
                System.out.println("Opção inválida.");
        }
    }

    // ---------- cadastros ----------

    private void cadastrarCurso() {
        System.out.println("\n--- Novo Curso ---");
        String nome = entrada.lerTexto("Nome do curso");
        if (nome == null) {
            return;
        }
        Integer creditos = entrada.lerInteiro("Número de créditos");
        if (creditos == null) {
            return;
        }

        Curso curso = servicoCadastro.cadastrarCurso(nome, creditos);
        salvar();
        System.out.println("Curso cadastrado com sucesso: " + curso.getNome() + " (" + curso.getNumeroCreditos() + " créditos)");
    }

    private void cadastrarProfessor() {
        System.out.println("\n--- Novo Professor ---");
        String nome = entrada.lerTexto("Nome do professor");
        if (nome == null) {
            return;
        }
        String login = entrada.lerTexto("Login");
        if (login == null) {
            return;
        }
        String senha = entrada.lerTexto("Senha (mínimo 4 caracteres)");
        if (senha == null) {
            return;
        }

        Professor professor = servicoCadastro.cadastrarProfessor(nome, login, senha);
        salvar();
        System.out.println("Professor cadastrado com sucesso: " + professor.getNome() + " (Login: " + professor.getLogin() + ")");
    }

    private void cadastrarAluno() {
        System.out.println("\n--- Novo Aluno ---");
        String nome = entrada.lerTexto("Nome do aluno");
        if (nome == null) {
            return;
        }
        String matricula = entrada.lerTexto("Número de matrícula");
        if (matricula == null) {
            return;
        }
        String login = entrada.lerTexto("Login");
        if (login == null) {
            return;
        }
        String senha = entrada.lerTexto("Senha (mínimo 4 caracteres)");
        if (senha == null) {
            return;
        }

        Aluno aluno = servicoCadastro.cadastrarAluno(nome, matricula, login, senha);
        salvar();
        System.out.println("Aluno cadastrado com sucesso: " + aluno.getNome() + " (Matrícula: " + aluno.getNumeroMatricula() + ")");
    }

    /** Uma disciplina precisa de um curso e de um professor, então exige os dois já cadastrados. */
    private void cadastrarDisciplina() {
        System.out.println("\n--- Nova Disciplina ---");
        List<Curso> cursos = servicoCadastro.listarCursos();
        if (cursos.isEmpty()) {
            System.out.println("[AVISO] É necessário cadastrar pelo menos um curso antes de cadastrar disciplinas.");
            return;
        }
        List<Professor> professores = servicoCadastro.listarProfessores();
        if (professores.isEmpty()) {
            System.out.println("[AVISO] É necessário cadastrar pelo menos um professor antes de cadastrar disciplinas.");
            return;
        }

        String nome = entrada.lerTexto("Nome da disciplina");
        if (nome == null) {
            return;
        }
        Curso curso = entrada.escolherDeLista("Selecione o Curso", cursos, c -> c.getNome() + " (" + c.getNumeroCreditos() + " créditos)");
        if (curso == null) {
            return;
        }
        Professor professor = entrada.escolherDeLista("Selecione o Professor", professores, p -> p.getNome() + " (Login: " + p.getLogin() + ")");
        if (professor == null) {
            return;
        }

        Disciplina disciplina = servicoCadastro.cadastrarDisciplina(nome, curso);
        disciplina.atribuirProfessor(professor);
        salvar();
        System.out.println("Disciplina cadastrada com sucesso: " + disciplina.getNome() + " | Curso: " + curso.getNome() + " | Professor: " + professor.getNome());
    }

    // ---------- período de matrículas ----------

    private void adicionarDisciplinaAoPeriodo() {
        System.out.println("\n--- Adicionar Disciplina ao Período ---");
        List<Disciplina> todas = Disciplina.listarTodos();
        if (todas.isEmpty()) {
            System.out.println("Nenhuma disciplina cadastrada no sistema.");
            return;
        }
        Disciplina disciplina = entrada.escolherDeLista("Selecione a Disciplina", todas, d -> d.getNome() + " (Professor: " + nomeDoProfessor(d, "Sem professor") + ")");
        if (disciplina == null) {
            return;
        }

        periodo.adicionarDisciplina(disciplina);
        salvar();
        System.out.println("Disciplina " + disciplina.getNome() + " adicionada com sucesso ao período de matrículas.");
    }

    private void abrirPeriodo() {
        if (periodo.abrir()) {
            salvar();
            System.out.println("Período de matrículas aberto com sucesso na data: " + periodo.getDataInicio());
        } else {
            System.out.println("[AVISO] O período de matrículas já está aberto.");
        }
    }

    private void encerrarPeriodo() {
        List<Disciplina> canceladas = periodo.encerrar();
        salvar();
        System.out.println("Período de matrículas encerrado com sucesso na data: " + periodo.getDataFim());
        if (canceladas.isEmpty()) {
            System.out.println("Todas as disciplinas do período atingiram o número mínimo de alunos e foram confirmadas!");
            return;
        }
        System.out.println("As seguintes disciplinas foram canceladas por não atingirem o número mínimo de alunos:");
        for (Disciplina d : canceladas) {
            System.out.println("- " + d.getNome());
        }
    }

    // ---------- listagens ----------

    private void listarCursos() {
        System.out.println("\n--- Cursos Cadastrados ---");
        List<Curso> cursos = servicoCadastro.listarCursos();
        if (cursos.isEmpty()) {
            System.out.println("Nenhum curso cadastrado.");
            return;
        }
        for (Curso c : cursos) {
            System.out.println("Curso: " + c.getNome() + " | Créditos: " + c.getNumeroCreditos());
            for (Disciplina d : c.getDisciplinas()) {
                System.out.println("  - Disciplina: " + d.getNome() + " (Status: " + d.getStatus() + ")");
            }
        }
    }

    private void listarDisciplinas() {
        System.out.println("\n--- Disciplinas Cadastradas ---");
        List<Disciplina> disciplinas = Disciplina.listarTodos();
        if (disciplinas.isEmpty()) {
            System.out.println("Nenhuma disciplina cadastrada.");
            return;
        }
        for (Disciplina d : disciplinas) {
            String cursoNome = (d.getCurso() != null) ? d.getCurso().getNome() : "Sem curso";
            System.out.println("Nome: " + d.getNome() + " | Status: " + d.getStatus()
                + " | Curso: " + cursoNome + " | Professor: " + nomeDoProfessor(d, "Sem professor")
                + " | Matriculados: " + d.quantidadeMatriculados() + " | Vagas restantes: " + d.vagasRestantes());
        }
    }

    private void listarProfessores() {
        System.out.println("\n--- Professores Cadastrados ---");
        List<Professor> professores = servicoCadastro.listarProfessores();
        if (professores.isEmpty()) {
            System.out.println("Nenhum professor cadastrado.");
            return;
        }
        for (Professor p : professores) {
            System.out.println("Nome: " + p.getNome() + " | Login: " + p.getLogin());
        }
    }

    private void listarAlunos() {
        System.out.println("\n--- Alunos Cadastrados ---");
        List<Aluno> alunos = Aluno.listarTodos();
        if (alunos.isEmpty()) {
            System.out.println("Nenhum aluno cadastrado.");
            return;
        }
        for (Aluno a : alunos) {
            System.out.println("Nome: " + a.getNome() + " | Matrícula: " + a.getNumeroMatricula() + " | Login: " + a.getLogin());
        }
    }

    private String nomeDoProfessor(Disciplina disciplina, String padrao) {
        return disciplina.getProfessor() != null ? disciplina.getProfessor().getNome() : padrao;
    }

    private void salvar() {
        persistencia.salvar(servicoCadastro, periodo);
    }
}
