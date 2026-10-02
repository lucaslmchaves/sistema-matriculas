package matriculas.web.handlers;

import com.sun.net.httpserver.HttpExchange;
import matriculas.excecoes.RegraDeNegocioException;
import matriculas.models.Aluno;
import matriculas.models.Disciplina;
import matriculas.models.Professor;
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
 * Rotas do professor (só leitura): /api/professor/disciplinas lista as disciplinas que ele
 * leciona e /api/professor/alunos?disciplina=... lista os alunos matriculados em uma delas.
 */
public class ProfessorDisciplinasHandler extends BaseHandler {
    private final ServicoCadastro servicoCadastro;

    public ProfessorDisciplinasHandler(GerenciadorSessao gerenciadorSessao, ServicoCadastro servicoCadastro) {
        super(gerenciadorSessao, true, PerfilUsuario.PROFESSOR);
        this.servicoCadastro = servicoCadastro;
    }

    @Override
    protected void executar(HttpExchange exchange, Sessao sessao) throws Exception {
        if (!exigirMetodo(exchange, "GET")) {
            return;
        }

        String caminho = exchange.getRequestURI().getPath();
        Professor professor = (Professor) sessao.getUsuario();
        if (caminho.equals("/api/professor/disciplinas")) {
            listarDisciplinasDoProfessor(exchange, professor);
        } else if (caminho.equals("/api/professor/alunos")) {
            listarAlunosDaDisciplina(exchange, professor);
        } else {
            rotaNaoEncontrada(exchange);
        }
    }

    private void listarDisciplinasDoProfessor(HttpExchange exchange, Professor professor) throws Exception {
        List<String> disciplinasJson = new ArrayList<>();
        for (Disciplina disciplina : Disciplina.listarTodos()) {
            if (professor.equals(disciplina.getProfessor())) {
                disciplinasJson.add(Json.objeto()
                        .texto("nome", disciplina.getNome())
                        .texto("status", disciplina.getStatus().name())
                        .texto("curso", disciplina.getCurso() != null ? disciplina.getCurso().getNome() : "")
                        .numero("matriculados", disciplina.quantidadeMatriculados())
                        .montar());
            }
        }
        HttpUtils.enviarJson(exchange, 200, Json.arrayJson(disciplinasJson));
    }

    /** Um professor só vê os alunos das disciplinas que leciona; nas demais recebe 403. */
    private void listarAlunosDaDisciplina(HttpExchange exchange, Professor professor) throws Exception {
        Map<String, String> query = HttpUtils.lerQueryParams(exchange);
        String nomeDisciplina = query.get("disciplina");
        if (nomeDisciplina == null || nomeDisciplina.trim().isEmpty()) {
            throw new RegraDeNegocioException("Parâmetro 'disciplina' é obrigatório.");
        }

        Disciplina disciplina = servicoCadastro.buscarDisciplinaPorNome(nomeDisciplina);
        if (disciplina == null) {
            HttpUtils.enviarErro(exchange, 404, "Disciplina não encontrada: " + nomeDisciplina.trim());
            return;
        }
        if (!professor.equals(disciplina.getProfessor())) {
            HttpUtils.enviarErro(exchange, 403, "Acesso negado: professor não leciona esta disciplina.");
            return;
        }

        List<String> alunosJson = new ArrayList<>();
        for (Aluno aluno : disciplina.listarAlunosMatriculados()) {
            alunosJson.add(Json.objeto()
                    .texto("nome", aluno.getNome())
                    .texto("numeroMatricula", aluno.getNumeroMatricula())
                    .montar());
        }
        HttpUtils.enviarJson(exchange, 200, Json.arrayJson(alunosJson));
    }
}
