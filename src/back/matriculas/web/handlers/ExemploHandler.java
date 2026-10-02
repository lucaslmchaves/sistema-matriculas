package matriculas.web.handlers;

import com.sun.net.httpserver.HttpExchange;
import matriculas.models.PeriodoMatricula;
import matriculas.persistencia.Persistencia;
import matriculas.services.ServicoCadastro;
import matriculas.web.DadosDemonstracao;
import matriculas.web.GerenciadorSessao;
import matriculas.web.GerenciadorSessao.Sessao;
import matriculas.web.HttpUtils;
import matriculas.web.Json;
import matriculas.web.PerfilUsuario;

/** Rota POST /api/exemplo (só secretaria): carrega os dados de demonstração e devolve os logins criados. */
public class ExemploHandler extends HandlerComPersistencia {

    public ExemploHandler(GerenciadorSessao gerenciadorSessao, ServicoCadastro servicoCadastro, PeriodoMatricula periodo, Persistencia persistencia) {
        super(gerenciadorSessao, servicoCadastro, periodo, persistencia, PerfilUsuario.SECRETARIA);
    }

    @Override
    protected void executar(HttpExchange exchange, Sessao sessao) throws Exception {
        if (!exigirMetodo(exchange, "POST")) {
            return;
        }

        DadosDemonstracao.ResultadoCarga resultado = DadosDemonstracao.carregar(servicoCadastro, periodo, persistencia);

        HttpUtils.enviarJson(exchange, 200, Json.objeto()
                .texto("status", "ok")
                .texto("mensagem", "Dados de demonstração carregados com sucesso.")
                .lista("professores", resultado.getProfessores())
                .lista("alunos", resultado.getAlunos())
                .lista("disciplinas", resultado.getDisciplinas())
                .montar());
    }
}
