package matriculas.cli;

import matriculas.models.PeriodoMatricula;
import matriculas.models.Secretaria;
import matriculas.models.Usuario;
import matriculas.persistencia.Persistencia;
import matriculas.services.ServicoCadastro;
import matriculas.services.ServicoMatricula;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Menu inicial do console: o usuário escolhe o perfil, faz login (até 3 tentativas)
 * e é levado ao painel correspondente.
 */
public class MenuPrincipal {
    private static final int MAXIMO_TENTATIVAS = 3;

    private final ServicoCadastro servicoCadastro;
    private final ServicoMatricula servicoMatricula;
    private final PeriodoMatricula periodo;
    private final Persistencia persistencia;
    private final Secretaria secretaria;
    private final EntradaConsole entrada;

    public MenuPrincipal(ServicoCadastro servicoCadastro, ServicoMatricula servicoMatricula, PeriodoMatricula periodo, Persistencia persistencia, Secretaria secretaria) {
        this.servicoCadastro = servicoCadastro;
        this.servicoMatricula = servicoMatricula;
        this.periodo = periodo;
        this.persistencia = persistencia;
        this.secretaria = secretaria;
        this.entrada = new EntradaConsole();
    }

    /** Repete o menu de perfis até o usuário escolher sair. */
    public void iniciar() {
        while (true) {
            exibirOpcoes();
            Integer opcao = entrada.lerInteiro("Opção");
            if (opcao == null || opcao == 0) {
                System.out.println("Encerrando o sistema. Até logo!");
                break;
            }
            escolherPerfil(opcao);
        }
    }

    private void exibirOpcoes() {
        System.out.println("\n=================================");
        System.out.println("  SISTEMA DE MATRÍCULAS - PUC MINAS");
        System.out.println("=================================");
        System.out.println("Selecione o perfil de acesso:");
        System.out.println("1. Aluno");
        System.out.println("2. Professor");
        System.out.println("3. Secretaria");
        System.out.println("0. Sair");
    }

    private void escolherPerfil(int opcao) {
        switch (opcao) {
            case 1:
                autenticarAluno();
                break;
            case 2:
                autenticarProfessor();
                break;
            case 3:
                autenticarSecretaria();
                break;
            default:
                System.out.println("Opção inválida. Tente novamente.");
        }
    }

    private void autenticarAluno() {
        autenticar("Login do Aluno", servicoCadastro::buscarAlunoPorLogin, aluno -> {
            System.out.println("Autenticado com sucesso! Bem-vindo(a), " + aluno.getNome() + ".");
            new MenuAluno(aluno, servicoMatricula, servicoCadastro, periodo, persistencia, entrada).exibir();
        });
    }

    private void autenticarProfessor() {
        autenticar("Login do Professor", servicoCadastro::buscarProfessorPorLogin, professor -> {
            System.out.println("Autenticado com sucesso! Bem-vindo(a), Prof(a). " + professor.getNome() + ".");
            new MenuProfessor(professor, entrada).exibir();
        });
    }

    private void autenticarSecretaria() {
        Function<String, Secretaria> buscar = login -> secretaria.getLogin().equalsIgnoreCase(login) ? secretaria : null;
        autenticar("Login da Secretaria", buscar, logada -> {
            System.out.println("Autenticado com sucesso! Bem-vindo(a), " + logada.getNome() + ".");
            new MenuSecretaria(servicoCadastro, periodo, persistencia, entrada).exibir();
        });
    }

    /**
     * Fluxo de login comum aos três perfis: pede login e senha, localiza o usuário e, se a
     * senha confere, abre o painel. Muda só a forma de buscar o usuário e o painel aberto.
     */
    private <T extends Usuario> void autenticar(String titulo, Function<String, T> buscarPorLogin, Consumer<T> abrirPainel) {
        for (int tentativa = 1; tentativa <= MAXIMO_TENTATIVAS; tentativa++) {
            System.out.println("\n--- " + titulo + " (Tentativa " + tentativa + " de " + MAXIMO_TENTATIVAS + ") ---");
            String login = entrada.lerTexto("Login");
            if (login == null) {
                return;
            }
            String senha = entrada.lerTexto("Senha");
            if (senha == null) {
                return;
            }

            T usuario = buscarPorLogin.apply(login);
            if (usuario != null && usuario.autenticar(senha)) {
                abrirPainel.accept(usuario);
                return;
            }
            System.out.println("[ERRO] Login ou senha inválidos.");
        }
        System.out.println("Número máximo de tentativas atingido. Retornando ao menu principal.");
    }
}
