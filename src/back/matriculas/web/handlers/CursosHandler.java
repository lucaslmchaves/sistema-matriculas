package matriculas.web.handlers;

import com.sun.net.httpserver.HttpExchange;
import matriculas.excecoes.RegraDeNegocioException;
import matriculas.models.Curso;
import matriculas.models.Disciplina;
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

/** Rota /api/cursos (só secretaria): GET lista os cursos, POST cadastra um curso. */
public class CursosHandler extends HandlerComPersistencia {

    public CursosHandler(GerenciadorSessao gerenciadorSessao, ServicoCadastro servicoCadastro, PeriodoMatricula periodo, Persistencia persistencia) {
        super(gerenciadorSessao, servicoCadastro, periodo, persistencia, PerfilUsuario.SECRETARIA);
    }

    @Override
    protected void executar(HttpExchange exchange, Sessao sessao) throws Exception {
        if (ehMetodo(exchange, "GET")) {
            listarCursos(exchange);
        } else if (ehMetodo(exchange, "POST")) {
            cadastrarCurso(exchange);
        } else {
            metodoNaoPermitido(exchange);
        }
    }

    private void listarCursos(HttpExchange exchange) throws Exception {
        List<String> cursosJson = new ArrayList<>();
        for (Curso curso : servicoCadastro.listarCursos()) {
            List<String> nomesDisciplinas = new ArrayList<>();
            for (Disciplina disciplina : curso.getDisciplinas()) {
                nomesDisciplinas.add(disciplina.getNome());
            }
            cursosJson.add(Json.objeto()
                    .texto("nome", curso.getNome())
                    .numero("creditos", curso.getNumeroCreditos())
                    .lista("disciplinas", nomesDisciplinas)
                    .montar());
        }
        HttpUtils.enviarJson(exchange, 200, Json.arrayJson(cursosJson));
    }

    private void cadastrarCurso(HttpExchange exchange) throws Exception {
        Map<String, String> params = HttpUtils.lerParametrosForm(exchange);
        exigirParametros(params, "Nome e créditos do curso são obrigatórios.", "nome", "creditos");

        Curso curso = servicoCadastro.cadastrarCurso(params.get("nome"), lerCreditos(params.get("creditos")));
        salvar();

        HttpUtils.enviarJson(exchange, 200, Json.objeto()
                .texto("status", "ok")
                .texto("nome", curso.getNome())
                .numero("creditos", curso.getNumeroCreditos())
                .montar());
    }

    private int lerCreditos(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new RegraDeNegocioException("Número de créditos inválido.");
        }
    }
}
