package matriculas.web.handlers;

import com.sun.net.httpserver.HttpExchange;
import matriculas.models.PeriodoMatricula;
import matriculas.models.Professor;
import matriculas.persistencia.Persistencia;
import matriculas.services.ServicoCadastro;
import matriculas.web.GerenciadorSessao;
import matriculas.web.GerenciadorSessao.Sessao;
import matriculas.web.HttpUtils;
import matriculas.web.Json;
import matriculas.web.PerfilUsuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Rota /api/professores (só secretaria): GET lista os professores, POST cadastra um professor. */
public class ProfessoresHandler extends HandlerComPersistencia {

    public ProfessoresHandler(GerenciadorSessao gerenciadorSessao, ServicoCadastro servicoCadastro, PeriodoMatricula periodo, Persistencia persistencia) {
        super(gerenciadorSessao, servicoCadastro, periodo, persistencia, PerfilUsuario.SECRETARIA);
    }

    @Override
    protected void executar(HttpExchange exchange, Sessao sessao) throws Exception {
        if (ehMetodo(exchange, "GET")) {
            listarProfessores(exchange);
        } else if (ehMetodo(exchange, "POST")) {
            cadastrarProfessor(exchange);
        } else {
            metodoNaoPermitido(exchange);
        }
    }

    private void listarProfessores(HttpExchange exchange) throws Exception {
        List<String> professoresJson = new ArrayList<>();
        for (Professor professor : servicoCadastro.listarProfessores()) {
            professoresJson.add(Json.objeto()
                    .texto("nome", professor.getNome())
                    .texto("login", professor.getLogin())
                    .montar());
        }
        HttpUtils.enviarJson(exchange, 200, Json.arrayJson(professoresJson));
    }

    private void cadastrarProfessor(HttpExchange exchange) throws Exception {
        Map<String, String> params = HttpUtils.lerParametrosForm(exchange);
        exigirParametros(params, "Nome, login e senha do professor são obrigatórios.", "nome", "login", "senha");

        Professor professor = servicoCadastro.cadastrarProfessor(params.get("nome"), params.get("login"), params.get("senha"));
        salvar();

        HttpUtils.enviarJson(exchange, 200, Json.objeto()
                .texto("status", "ok")
                .texto("nome", professor.getNome())
                .texto("login", professor.getLogin())
                .montar());
    }
}
