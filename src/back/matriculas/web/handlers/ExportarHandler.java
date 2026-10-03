package matriculas.web.handlers;

import com.sun.net.httpserver.HttpExchange;
import matriculas.models.PeriodoMatricula;
import matriculas.services.ServicoCadastro;
import matriculas.web.ExportadorPlanilha;
import matriculas.web.GerenciadorSessao;
import matriculas.web.GerenciadorSessao.Sessao;
import matriculas.web.HttpUtils;
import matriculas.web.PerfilUsuario;

/** Rota GET /api/exportar (só secretaria): devolve a planilha .xlsx para download. Não altera nada. */
public class ExportarHandler extends BaseHandler {
    private final ServicoCadastro servicoCadastro;
    private final PeriodoMatricula periodo;

    public ExportarHandler(GerenciadorSessao gerenciadorSessao, ServicoCadastro servicoCadastro, PeriodoMatricula periodo) {
        super(gerenciadorSessao, true, PerfilUsuario.SECRETARIA);
        this.servicoCadastro = servicoCadastro;
        this.periodo = periodo;
    }

    @Override
    protected void executar(HttpExchange exchange, Sessao sessao) throws Exception {
        if (!exigirMetodo(exchange, "GET")) {
            return;
        }

        byte[] planilha = ExportadorPlanilha.gerar(servicoCadastro, periodo);
        HttpUtils.enviarBinario(exchange, 200, ExportadorPlanilha.TIPO_CONTEUDO, "matriculas.xlsx", planilha);
    }
}
