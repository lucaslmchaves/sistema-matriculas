package matriculas.web.handlers;

import com.sun.net.httpserver.HttpExchange;
import matriculas.excecoes.RegraDeNegocioException;
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

/**
 * Rotas do período de matrículas. Consultar (GET /api/periodo) é permitido a qualquer perfil
 * logado; alterar (adicionar disciplina, abrir, encerrar) é só da secretaria.
 */
public class PeriodoHandler extends HandlerComPersistencia {

    public PeriodoHandler(GerenciadorSessao gerenciadorSessao, PeriodoMatricula periodo, ServicoCadastro servicoCadastro, Persistencia persistencia) {
        super(gerenciadorSessao, servicoCadastro, periodo, persistencia);
    }

    @Override
    protected void executar(HttpExchange exchange, Sessao sessao) throws Exception {
        String caminho = exchange.getRequestURI().getPath();

        if (caminho.equals("/api/periodo") && ehMetodo(exchange, "GET")) {
            consultarPeriodo(exchange);
            return;
        }

        if (sessao.getPerfil() != PerfilUsuario.SECRETARIA) {
            HttpUtils.enviarErro(exchange, 403, "Apenas a secretaria pode alterar o período de matrículas.");
            return;
        }

        if (caminho.equals("/api/periodo/disciplinas") && ehMetodo(exchange, "POST")) {
            adicionarDisciplinaAoPeriodo(exchange);
        } else if (caminho.equals("/api/periodo/abrir") && ehMetodo(exchange, "POST")) {
            abrirPeriodo(exchange);
        } else if (caminho.equals("/api/periodo/encerrar") && ehMetodo(exchange, "POST")) {
            encerrarPeriodo(exchange);
        } else {
            rotaNaoEncontrada(exchange);
        }
    }

    private void consultarPeriodo(HttpExchange exchange) throws Exception {
        List<String> disciplinasJson = new ArrayList<>();
        for (Disciplina disciplina : periodo.getDisciplinas()) {
            disciplinasJson.add(Json.objeto()
                    .texto("nome", disciplina.getNome())
                    .texto("status", disciplina.getStatus().name())
                    .texto("professor", disciplina.getProfessor() != null ? disciplina.getProfessor().getNome() : "")
                    .numero("matriculados", disciplina.quantidadeMatriculados())
                    .numero("vagasRestantes", disciplina.vagasRestantes())
                    .montar());
        }

        HttpUtils.enviarJson(exchange, 200, Json.objeto()
                .booleano("aberto", periodo.isAberto())
                .texto("dataInicio", periodo.getDataInicio() != null ? periodo.getDataInicio().toString() : null)
                .texto("dataFim", periodo.getDataFim() != null ? periodo.getDataFim().toString() : null)
                .bruto("disciplinas", Json.arrayJson(disciplinasJson))
                .montar());
    }

    private void adicionarDisciplinaAoPeriodo(HttpExchange exchange) throws Exception {
        Map<String, String> params = HttpUtils.lerParametrosForm(exchange);
        String nome = params.get("nome");
        if (nome == null || nome.trim().isEmpty()) {
            throw new RegraDeNegocioException("Nome da disciplina é obrigatório.");
        }

        Disciplina disciplina = servicoCadastro.buscarDisciplinaPorNome(nome);
        if (disciplina == null) {
            throw new RegraDeNegocioException("Disciplina não encontrada: " + nome.trim());
        }

        periodo.adicionarDisciplina(disciplina);
        salvar();
        HttpUtils.enviarJson(exchange, 200, Json.status("ok"));
    }

    private void abrirPeriodo(HttpExchange exchange) throws Exception {
        if (!periodo.abrir()) {
            throw new RegraDeNegocioException("O período de matrículas já está aberto.");
        }
        salvar();
        HttpUtils.enviarJson(exchange, 200, Json.status("ok"));
    }

    /** Encerra o período e devolve os nomes das disciplinas canceladas por falta de alunos. */
    private void encerrarPeriodo(HttpExchange exchange) throws Exception {
        List<Disciplina> canceladas = periodo.encerrar();
        salvar();

        List<String> nomesCanceladas = new ArrayList<>();
        for (Disciplina disciplina : canceladas) {
            nomesCanceladas.add(disciplina.getNome());
        }

        HttpUtils.enviarJson(exchange, 200, Json.objeto()
                .texto("status", "ok")
                .lista("canceladas", nomesCanceladas)
                .montar());
    }
}
