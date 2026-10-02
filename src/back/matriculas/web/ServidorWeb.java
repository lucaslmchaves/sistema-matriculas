package matriculas.web;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import matriculas.models.PeriodoMatricula;
import matriculas.models.Secretaria;
import matriculas.persistencia.Persistencia;
import matriculas.services.ServicoCadastro;
import matriculas.services.ServicoMatricula;
import matriculas.web.handlers.AlunoMatriculasHandler;
import matriculas.web.handlers.AlunosHandler;
import matriculas.web.handlers.CursosHandler;
import matriculas.web.handlers.DisciplinasHandler;
import matriculas.web.handlers.ExemploHandler;
import matriculas.web.handlers.ExportarHandler;
import matriculas.web.handlers.LoginHandler;
import matriculas.web.handlers.LogoutHandler;
import matriculas.web.handlers.PeriodoHandler;
import matriculas.web.handlers.ProfessorDisciplinasHandler;
import matriculas.web.handlers.ProfessoresHandler;
import matriculas.web.handlers.SaudeHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Servidor HTTP da API, feito com o HttpServer que já vem no JDK. Liga cada rota ao
 * seu handler. Usa um executor de UMA thread: as classes do domínio guardam dados em
 * listas estáticas e não são thread-safe, então as requisições são atendidas uma por vez.
 */
public class ServidorWeb {
    private final int porta;
    private final ServicoCadastro servicoCadastro;
    private final ServicoMatricula servicoMatricula;
    private final PeriodoMatricula periodo;
    private final Persistencia persistencia;
    private final Secretaria secretaria;
    private final GerenciadorSessao gerenciadorSessao;
    private HttpServer server;
    private ExecutorService executor;

    public ServidorWeb(int porta, ServicoCadastro servicoCadastro, ServicoMatricula servicoMatricula,
                       PeriodoMatricula periodo, Persistencia persistencia, Secretaria secretaria) {
        this.porta = porta;
        this.servicoCadastro = servicoCadastro;
        this.servicoMatricula = servicoMatricula;
        this.periodo = periodo;
        this.persistencia = persistencia;
        this.secretaria = secretaria;
        this.gerenciadorSessao = new GerenciadorSessao();
    }

    public void iniciar() throws IOException {
        server = HttpServer.create(new InetSocketAddress(porta), 0);
        executor = Executors.newSingleThreadExecutor();
        server.setExecutor(executor);
        registrarRotas();
        server.start();
        System.out.println("Servidor Web HTTP iniciado na porta " + porta + " (executor de thread única).");
    }

    public void parar() {
        if (server != null) {
            server.stop(0);
        }
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private void registrarRotas() {
        HttpHandler professor = new ProfessorDisciplinasHandler(gerenciadorSessao, servicoCadastro);

        rota("/api/saude", new SaudeHandler(gerenciadorSessao));
        rota("/api/login", new LoginHandler(gerenciadorSessao, servicoCadastro, secretaria));
        rota("/api/logout", new LogoutHandler(gerenciadorSessao));
        rota("/api/periodo", new PeriodoHandler(gerenciadorSessao, periodo, servicoCadastro, persistencia));
        rota("/api/cursos", new CursosHandler(gerenciadorSessao, servicoCadastro, periodo, persistencia));
        rota("/api/professores", new ProfessoresHandler(gerenciadorSessao, servicoCadastro, periodo, persistencia));
        rota("/api/alunos", new AlunosHandler(gerenciadorSessao, servicoCadastro, periodo, persistencia));
        rota("/api/disciplinas", new DisciplinasHandler(gerenciadorSessao, servicoCadastro, periodo, persistencia));
        rota("/api/aluno/matriculas", new AlunoMatriculasHandler(gerenciadorSessao, servicoMatricula, servicoCadastro, periodo, persistencia));
        rota("/api/professor/disciplinas", professor);
        rota("/api/professor/alunos", professor);
        rota("/api/exemplo", new ExemploHandler(gerenciadorSessao, servicoCadastro, periodo, persistencia));
        rota("/api/exportar", new ExportarHandler(gerenciadorSessao, servicoCadastro, periodo));
    }

    private void rota(String caminho, HttpHandler handler) {
        server.createContext(caminho, handler);
    }
}
