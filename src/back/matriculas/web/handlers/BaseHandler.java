package matriculas.web.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import matriculas.excecoes.RegraDeNegocioException;
import matriculas.web.GerenciadorSessao;
import matriculas.web.GerenciadorSessao.Sessao;
import matriculas.web.HttpUtils;
import matriculas.web.PerfilUsuario;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Base de todos os handlers da API (padrão Template Method). Aqui ficam as tarefas
 * comuns: conferir o token e o perfil do usuário e transformar erros em respostas HTTP.
 * Cada handler filho implementa só {@link #executar}, com a lógica da sua rota.
 *
 * Códigos usados: 401 (sem login), 403 (perfil sem permissão), 400 (regra de negócio
 * violada ou dado inválido), 413 (corpo grande demais) e 500 (erro inesperado).
 */
public abstract class BaseHandler implements HttpHandler {
    protected final GerenciadorSessao gerenciadorSessao;
    private final boolean requerAutenticacao;
    private final List<PerfilUsuario> perfisPermitidos;

    /** Handler de rota pública (sem login). */
    public BaseHandler(GerenciadorSessao gerenciadorSessao) {
        this(gerenciadorSessao, false);
    }

    /**
     * Se requerAutenticacao for true, exige token válido; se perfisPermitidos não for vazio,
     * só esses perfis passam.
     */
    public BaseHandler(GerenciadorSessao gerenciadorSessao, boolean requerAutenticacao, PerfilUsuario... perfisPermitidos) {
        this.gerenciadorSessao = gerenciadorSessao;
        this.requerAutenticacao = requerAutenticacao;
        this.perfisPermitidos = Arrays.asList(perfisPermitidos);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            Sessao sessao = requerAutenticacao ? autenticar(exchange) : null;
            if (requerAutenticacao && sessao == null) {
                return;
            }
            executar(exchange, sessao);
        } catch (HttpUtils.LimiteCorpoExcedidoException e) {
            HttpUtils.enviarErro(exchange, 413, e.getMessage());
        } catch (RegraDeNegocioException | IllegalArgumentException e) {
            HttpUtils.enviarErro(exchange, 400, e.getMessage());
        } catch (Throwable t) {
            System.err.println("[ERRO INESPERADO HTTP] " + t.getMessage());
            t.printStackTrace(System.err);
            HttpUtils.enviarErro(exchange, 500, "Erro interno no servidor.");
        }
    }

    /** Lógica da rota. A sessão vem preenchida nas rotas autenticadas e é null nas públicas. */
    protected abstract void executar(HttpExchange exchange, Sessao sessao) throws Exception;

    /** Confere token e perfil. Se algo estiver errado, já envia 401/403 e devolve null. */
    private Sessao autenticar(HttpExchange exchange) throws IOException {
        Sessao sessao = gerenciadorSessao.obterSessao(gerenciadorSessao.extrairToken(exchange));
        if (sessao == null) {
            HttpUtils.enviarErro(exchange, 401, "Acesso não autorizado: token ausente ou inválido.");
            return null;
        }
        if (!perfisPermitidos.isEmpty() && !perfisPermitidos.contains(sessao.getPerfil())) {
            HttpUtils.enviarErro(exchange, 403, "Acesso negado para o perfil do usuário.");
            return null;
        }
        return sessao;
    }

    protected static boolean ehMetodo(HttpExchange exchange, String metodo) {
        return metodo.equalsIgnoreCase(exchange.getRequestMethod());
    }

    /** Se a requisição não usa o método esperado, responde 405 e devolve false. */
    protected static boolean exigirMetodo(HttpExchange exchange, String metodo) throws IOException {
        if (ehMetodo(exchange, metodo)) {
            return true;
        }
        metodoNaoPermitido(exchange);
        return false;
    }

    protected static void metodoNaoPermitido(HttpExchange exchange) throws IOException {
        HttpUtils.enviarErro(exchange, 405, "Método não permitido.");
    }

    protected static void rotaNaoEncontrada(HttpExchange exchange) throws IOException {
        HttpUtils.enviarErro(exchange, 404, "Rota não encontrada.");
    }

    /** Lança RegraDeNegocioException (resposta 400) com a mensagem se algum parâmetro estiver ausente. */
    protected static void exigirParametros(Map<String, String> parametros, String mensagem, String... nomes) {
        for (String nome : nomes) {
            if (parametros.get(nome) == null) {
                throw new RegraDeNegocioException(mensagem);
            }
        }
    }
}
