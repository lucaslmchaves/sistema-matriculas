package matriculas.web.handlers;

import com.sun.net.httpserver.HttpExchange;
import matriculas.web.GerenciadorSessao;
import matriculas.web.GerenciadorSessao.Sessao;
import matriculas.web.HttpUtils;
import matriculas.web.Json;

/** Rota POST /api/logout: apaga a sessão do token enviado. Responde ok mesmo se o token já não existir. */
public class LogoutHandler extends BaseHandler {

    public LogoutHandler(GerenciadorSessao gerenciadorSessao) {
        super(gerenciadorSessao, false);
    }

    @Override
    protected void executar(HttpExchange exchange, Sessao sessao) throws Exception {
        if (!exigirMetodo(exchange, "POST")) {
            return;
        }

        gerenciadorSessao.removerSessao(gerenciadorSessao.extrairToken(exchange));
        HttpUtils.enviarJson(exchange, 200, Json.status("ok"));
    }
}
