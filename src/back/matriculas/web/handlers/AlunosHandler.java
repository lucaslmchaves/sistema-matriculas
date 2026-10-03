package matriculas.web.handlers;

import com.sun.net.httpserver.HttpExchange;
import matriculas.models.Aluno;
import matriculas.models.PeriodoMatricula;
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

/** Rota /api/alunos (só secretaria): GET lista os alunos, POST cadastra um aluno. */
public class AlunosHandler extends HandlerComPersistencia {

    public AlunosHandler(GerenciadorSessao gerenciadorSessao, ServicoCadastro servicoCadastro, PeriodoMatricula periodo, Persistencia persistencia) {
        super(gerenciadorSessao, servicoCadastro, periodo, persistencia, PerfilUsuario.SECRETARIA);
    }

    @Override
    protected void executar(HttpExchange exchange, Sessao sessao) throws Exception {
        if (ehMetodo(exchange, "GET")) {
            listarAlunos(exchange);
        } else if (ehMetodo(exchange, "POST")) {
            cadastrarAluno(exchange);
        } else {
            metodoNaoPermitido(exchange);
        }
    }

    private void listarAlunos(HttpExchange exchange) throws Exception {
        List<String> alunosJson = new ArrayList<>();
        for (Aluno aluno : Aluno.listarTodos()) {
            alunosJson.add(Json.objeto()
                    .texto("nome", aluno.getNome())
                    .texto("numeroMatricula", aluno.getNumeroMatricula())
                    .texto("login", aluno.getLogin())
                    .montar());
        }
        HttpUtils.enviarJson(exchange, 200, Json.arrayJson(alunosJson));
    }

    private void cadastrarAluno(HttpExchange exchange) throws Exception {
        Map<String, String> params = HttpUtils.lerParametrosForm(exchange);
        exigirParametros(params, "Nome, matrícula, login e senha do aluno são obrigatórios.",
                "nome", "numeroMatricula", "login", "senha");

        Aluno aluno = servicoCadastro.cadastrarAluno(params.get("nome"), params.get("numeroMatricula"),
                params.get("login"), params.get("senha"));
        salvar();

        HttpUtils.enviarJson(exchange, 200, Json.objeto()
                .texto("status", "ok")
                .texto("nome", aluno.getNome())
                .texto("numeroMatricula", aluno.getNumeroMatricula())
                .texto("login", aluno.getLogin())
                .montar());
    }
}
