package matriculas.web.handlers;

import com.sun.net.httpserver.HttpExchange;
import matriculas.web.GerenciadorSessao;
import matriculas.web.GerenciadorSessao.Sessao;
import matriculas.web.HttpUtils;
import matriculas.web.Json;

/** Rota GET /api/saude: responde {"status":"ok"} sem login. O Docker usa para saber se a API subiu. */
public class SaudeHandler extends BaseHandler {

    public SaudeHandler(GerenciadorSessao gerenciadorSessao) {
        super(gerenciadorSessao, false);
    }

    @Override
    protected void executar(HttpExchange exchange, Sessao sessao) throws Exception {
        if (!exigirMetodo(exchange, "GET")) {
            return;
        }
        HttpUtils.enviarJson(exchange, 200, Json.status("ok"));
    }
}
