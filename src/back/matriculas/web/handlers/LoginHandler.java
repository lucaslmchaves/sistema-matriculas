package matriculas.web.handlers;

import com.sun.net.httpserver.HttpExchange;
import matriculas.models.Aluno;
import matriculas.models.Professor;
import matriculas.models.Secretaria;
import matriculas.models.Usuario;
import matriculas.services.ServicoCadastro;
import matriculas.web.GerenciadorSessao;
import matriculas.web.GerenciadorSessao.Sessao;
import matriculas.web.HttpUtils;
import matriculas.web.Json;
import matriculas.web.PerfilUsuario;

import java.util.Map;

/**
 * Rota POST /api/login. Recebe perfil, login e senha e, se estiverem corretos, devolve
 * o token da sessão. A mensagem de erro é a mesma para login inexistente e senha errada,
 * para não revelar quais logins existem.
 */
public class LoginHandler extends BaseHandler {
    private final ServicoCadastro servicoCadastro;
    private final Secretaria secretaria;

    public LoginHandler(GerenciadorSessao gerenciadorSessao, ServicoCadastro servicoCadastro, Secretaria secretaria) {
        super(gerenciadorSessao, false);
        this.servicoCadastro = servicoCadastro;
        this.secretaria = secretaria;
    }

    @Override
    protected void executar(HttpExchange exchange, Sessao sessaoAtual) throws Exception {
        if (!exigirMetodo(exchange, "POST")) {
            return;
        }

        Map<String, String> params = HttpUtils.lerParametrosForm(exchange);
        String perfilTexto = params.get("perfil");
        String login = params.get("login");
        String senha = params.get("senha");
        if (perfilTexto == null || login == null || senha == null) {
            HttpUtils.enviarErro(exchange, 400, "Os campos perfil, login e senha são obrigatórios.");
            return;
        }

        PerfilUsuario perfil;
        try {
            perfil = PerfilUsuario.valueOf(perfilTexto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            HttpUtils.enviarErro(exchange, 400, "Perfil inválido. Use ALUNO, PROFESSOR ou SECRETARIA.");
            return;
        }

        Conta conta = buscarConta(perfil, login.trim());
        if (conta == null || !conta.usuario().autenticar(senha)) {
            HttpUtils.enviarErro(exchange, 401, "Login ou senha inválidos.");
            return;
        }

        Sessao sessao = gerenciadorSessao.criarSessao(perfil, conta.usuario(), conta.nome());
        HttpUtils.enviarJson(exchange, 200, Json.objeto()
                .texto("token", sessao.getToken())
                .texto("perfil", sessao.getPerfil().name())
                .texto("nome", sessao.getNome())
                .montar());
    }

    /** Usuário encontrado junto com o nome de exibição (Usuario em si não guarda nome). */
    private record Conta(Usuario usuario, String nome) {
    }

    /** Procura a conta do perfil informado; null se não existir. */
    private Conta buscarConta(PerfilUsuario perfil, String login) {
        switch (perfil) {
            case SECRETARIA:
                return secretaria.getLogin().equalsIgnoreCase(login) ? new Conta(secretaria, secretaria.getNome()) : null;
            case PROFESSOR:
                Professor professor = servicoCadastro.buscarProfessorPorLogin(login);
                return professor != null ? new Conta(professor, professor.getNome()) : null;
            default:
                Aluno aluno = servicoCadastro.buscarAlunoPorLogin(login);
                return aluno != null ? new Conta(aluno, aluno.getNome()) : null;
        }
    }
}
