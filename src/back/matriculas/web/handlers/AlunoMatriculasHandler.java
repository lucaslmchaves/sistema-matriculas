package matriculas.web.handlers;

import com.sun.net.httpserver.HttpExchange;
import matriculas.enums.StatusMatricula;
import matriculas.enums.TipoMatricula;
import matriculas.excecoes.RegraDeNegocioException;
import matriculas.models.Aluno;
import matriculas.models.Disciplina;
import matriculas.models.Matricula;
import matriculas.models.PeriodoMatricula;
import matriculas.persistencia.Persistencia;
import matriculas.services.ServicoCadastro;
import matriculas.services.ServicoMatricula;
import matriculas.web.GerenciadorSessao;
import matriculas.web.GerenciadorSessao.Sessao;
import matriculas.web.HttpUtils;
import matriculas.web.Json;
import matriculas.web.PerfilUsuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Rotas /api/aluno/matriculas (só aluno): listar as próprias matrículas, efetuar uma matrícula
 * e cancelar. O aluno é sempre o da sessão, então um aluno nunca mexe nas matrículas de outro.
 */
public class AlunoMatriculasHandler extends HandlerComPersistencia {
    private final ServicoMatricula servicoMatricula;

    public AlunoMatriculasHandler(GerenciadorSessao gerenciadorSessao, ServicoMatricula servicoMatricula, ServicoCadastro servicoCadastro, PeriodoMatricula periodo, Persistencia persistencia) {
        super(gerenciadorSessao, servicoCadastro, periodo, persistencia, PerfilUsuario.ALUNO);
        this.servicoMatricula = servicoMatricula;
    }

    @Override
    protected void executar(HttpExchange exchange, Sessao sessao) throws Exception {
        String caminho = exchange.getRequestURI().getPath();
        Aluno aluno = (Aluno) sessao.getUsuario();

        if (caminho.equals("/api/aluno/matriculas") && ehMetodo(exchange, "GET")) {
            listarMatriculas(exchange, aluno);
        } else if (caminho.equals("/api/aluno/matriculas") && ehMetodo(exchange, "POST")) {
            efetuarMatricula(exchange, aluno);
        } else if (caminho.equals("/api/aluno/matriculas/cancelar") && ehMetodo(exchange, "POST")) {
            cancelarMatricula(exchange, aluno);
        } else {
            rotaNaoEncontrada(exchange);
        }
    }

    private void listarMatriculas(HttpExchange exchange, Aluno aluno) throws Exception {
        List<String> matriculasJson = new ArrayList<>();
        for (Matricula matricula : aluno.getMatriculas()) {
            matriculasJson.add(Json.objeto()
                    .texto("disciplina", matricula.getDisciplina().getNome())
                    .texto("tipo", matricula.getTipo().name())
                    .texto("status", matricula.getStatus().name())
                    .texto("dataMatricula", matricula.getDataMatricula().toString())
                    .montar());
        }
        HttpUtils.enviarJson(exchange, 200, Json.arrayJson(matriculasJson));
    }

    private void efetuarMatricula(HttpExchange exchange, Aluno aluno) throws Exception {
        Map<String, String> params = HttpUtils.lerParametrosForm(exchange);
        exigirParametros(params, "Disciplina e tipo de matrícula são obrigatórios.", "disciplina", "tipo");

        String nomeDisciplina = params.get("disciplina").trim();
        Disciplina disciplina = servicoCadastro.buscarDisciplinaPorNome(nomeDisciplina);
        if (disciplina == null) {
            throw new RegraDeNegocioException("Disciplina não encontrada: " + nomeDisciplina);
        }

        Matricula matricula = servicoMatricula.efetuarMatricula(aluno, disciplina, lerTipo(params.get("tipo")));
        salvar();

        HttpUtils.enviarJson(exchange, 200, Json.objeto()
                .texto("status", "ok")
                .texto("disciplina", matricula.getDisciplina().getNome())
                .texto("tipo", matricula.getTipo().name())
                .montar());
    }

    /** Localiza a matrícula ATIVA do aluno na disciplina informada e a cancela. */
    private void cancelarMatricula(HttpExchange exchange, Aluno aluno) throws Exception {
        Map<String, String> params = HttpUtils.lerParametrosForm(exchange);
        String nomeDisciplina = params.get("disciplina");
        if (nomeDisciplina == null || nomeDisciplina.trim().isEmpty()) {
            throw new RegraDeNegocioException("Nome da disciplina é obrigatório.");
        }

        Matricula ativa = buscarMatriculaAtiva(aluno, nomeDisciplina.trim());
        if (ativa == null) {
            throw new RegraDeNegocioException("O aluno não possui matrícula ativa na disciplina: " + nomeDisciplina.trim());
        }

        servicoMatricula.cancelarMatricula(ativa);
        salvar();
        HttpUtils.enviarJson(exchange, 200, Json.status("ok"));
    }

    private Matricula buscarMatriculaAtiva(Aluno aluno, String nomeDisciplina) {
        for (Matricula matricula : aluno.getMatriculas()) {
            if (matricula.getDisciplina().getNome().equalsIgnoreCase(nomeDisciplina)
                    && matricula.getStatus() == StatusMatricula.ATIVA) {
                return matricula;
            }
        }
        return null;
    }

    private TipoMatricula lerTipo(String texto) {
        try {
            return TipoMatricula.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RegraDeNegocioException("Tipo de matrícula inválido. Use OBRIGATORIA ou OPTATIVA.");
        }
    }
}
