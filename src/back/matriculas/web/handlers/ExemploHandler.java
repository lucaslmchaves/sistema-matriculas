package matriculas.web.handlers;

import com.sun.net.httpserver.HttpExchange;
import matriculas.excecoes.RegraDeNegocioException;
import matriculas.models.PeriodoMatricula;
import matriculas.persistencia.Persistencia;
import matriculas.services.ServicoCadastro;
import matriculas.services.ServicoMatricula;
import matriculas.web.DadosDemonstracao;
import matriculas.web.GerenciadorSessao;
import matriculas.web.GerenciadorSessao.Sessao;
import matriculas.web.HttpUtils;
import matriculas.web.Json;
import matriculas.web.PerfilUsuario;

import java.util.Map;

/**
 * Rotas de dados de exemplo (só secretaria, todas POST):
 * /api/exemplo carrega cadastros (parâmetro conjunto=basico|completo),
 * /api/exemplo/matriculas gera matrículas para os alunos existentes e
 * /api/exemplo/lotacao (parâmetro disciplina) lota uma disciplina.
 */
public class ExemploHandler extends HandlerComPersistencia {
    private final ServicoMatricula servicoMatricula;

    public ExemploHandler(GerenciadorSessao gerenciadorSessao, ServicoCadastro servicoCadastro,
                          ServicoMatricula servicoMatricula, PeriodoMatricula periodo, Persistencia persistencia) {
        super(gerenciadorSessao, servicoCadastro, periodo, persistencia, PerfilUsuario.SECRETARIA);
        this.servicoMatricula = servicoMatricula;
    }

    @Override
    protected void executar(HttpExchange exchange, Sessao sessao) throws Exception {
        if (!exigirMetodo(exchange, "POST")) {
            return;
        }

        String caminho = exchange.getRequestURI().getPath();
        if (caminho.equals("/api/exemplo")) {
            carregarCadastros(exchange);
        } else if (caminho.equals("/api/exemplo/matriculas")) {
            gerarMatriculas(exchange);
        } else if (caminho.equals("/api/exemplo/lotacao")) {
            lotarDisciplina(exchange);
        } else {
            rotaNaoEncontrada(exchange);
        }
    }

    private void carregarCadastros(HttpExchange exchange) throws Exception {
        Map<String, String> params = HttpUtils.lerParametrosForm(exchange);
        DadosDemonstracao.Conjunto conjunto = DadosDemonstracao.Conjunto.de(params.get("conjunto"));
        DadosDemonstracao.ResultadoCarga resultado = DadosDemonstracao.carregar(servicoCadastro, periodo, persistencia, conjunto);

        HttpUtils.enviarJson(exchange, 200, Json.objeto()
                .texto("status", "ok")
                .texto("mensagem", "Dados de demonstração carregados com sucesso.")
                .lista("professores", resultado.getProfessores())
                .lista("alunos", resultado.getAlunos())
                .lista("disciplinas", resultado.getDisciplinas())
                .montar());
    }

    private void gerarMatriculas(HttpExchange exchange) throws Exception {
        int criadas = DadosDemonstracao.gerarMatriculas(servicoCadastro, servicoMatricula, periodo, persistencia);
        HttpUtils.enviarJson(exchange, 200, Json.objeto()
                .texto("status", "ok")
                .numero("matriculas", criadas)
                .montar());
    }

    private void lotarDisciplina(HttpExchange exchange) throws Exception {
        Map<String, String> params = HttpUtils.lerParametrosForm(exchange);
        String disciplina = params.get("disciplina");
        if (disciplina == null || disciplina.trim().isEmpty()) {
            throw new RegraDeNegocioException("Nome da disciplina é obrigatório.");
        }

        int criados = DadosDemonstracao.lotarDisciplina(servicoCadastro, servicoMatricula, periodo, persistencia, disciplina);
        HttpUtils.enviarJson(exchange, 200, Json.objeto()
                .texto("status", "ok")
                .texto("disciplina", disciplina.trim())
                .numero("alunosCriados", criados)
                .montar());
    }
}
