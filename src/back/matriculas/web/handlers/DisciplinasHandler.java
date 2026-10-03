package matriculas.web.handlers;

import com.sun.net.httpserver.HttpExchange;
import matriculas.excecoes.RegraDeNegocioException;
import matriculas.models.Curso;
import matriculas.models.Disciplina;
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

/** Rota /api/disciplinas (só secretaria): GET lista as disciplinas, POST cadastra uma disciplina. */
public class DisciplinasHandler extends HandlerComPersistencia {

    public DisciplinasHandler(GerenciadorSessao gerenciadorSessao, ServicoCadastro servicoCadastro, PeriodoMatricula periodo, Persistencia persistencia) {
        super(gerenciadorSessao, servicoCadastro, periodo, persistencia, PerfilUsuario.SECRETARIA);
    }

    @Override
    protected void executar(HttpExchange exchange, Sessao sessao) throws Exception {
        if (ehMetodo(exchange, "GET")) {
            listarDisciplinas(exchange);
        } else if (ehMetodo(exchange, "POST")) {
            cadastrarDisciplina(exchange);
        } else {
            metodoNaoPermitido(exchange);
        }
    }

    private void listarDisciplinas(HttpExchange exchange) throws Exception {
        List<String> disciplinasJson = new ArrayList<>();
        for (Disciplina disciplina : Disciplina.listarTodos()) {
            Professor professor = disciplina.getProfessor();
            disciplinasJson.add(Json.objeto()
                    .texto("nome", disciplina.getNome())
                    .texto("status", disciplina.getStatus().name())
                    .texto("curso", disciplina.getCurso() != null ? disciplina.getCurso().getNome() : "")
                    .texto("professor", professor != null ? professor.getNome() : "")
                    .texto("loginProfessor", professor != null ? professor.getLogin() : "")
                    .numero("matriculados", disciplina.quantidadeMatriculados())
                    .numero("vagasRestantes", disciplina.vagasRestantes())
                    .montar());
        }
        HttpUtils.enviarJson(exchange, 200, Json.arrayJson(disciplinasJson));
    }

    /** O front envia o nome do curso e o login do professor; aqui eles são convertidos nos objetos. */
    private void cadastrarDisciplina(HttpExchange exchange) throws Exception {
        Map<String, String> params = HttpUtils.lerParametrosForm(exchange);
        exigirParametros(params, "Nome, curso e professor da disciplina são obrigatórios.", "nome", "curso", "professor");

        String nomeCurso = params.get("curso").trim();
        Curso curso = servicoCadastro.buscarCursoPorNome(nomeCurso);
        if (curso == null) {
            throw new RegraDeNegocioException("Curso não encontrado: " + nomeCurso);
        }

        String loginProfessor = params.get("professor").trim();
        Professor professor = servicoCadastro.buscarProfessorPorLogin(loginProfessor);
        if (professor == null) {
            throw new RegraDeNegocioException("Professor não encontrado: " + loginProfessor);
        }

        Disciplina disciplina = servicoCadastro.cadastrarDisciplina(params.get("nome"), curso);
        disciplina.atribuirProfessor(professor);
        salvar();

        HttpUtils.enviarJson(exchange, 200, Json.objeto()
                .texto("status", "ok")
                .texto("nome", disciplina.getNome())
                .texto("curso", curso.getNome())
                .texto("professor", professor.getNome())
                .montar());
    }
}
