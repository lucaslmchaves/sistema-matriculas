package matriculas.web;

import matriculas.Verificador;
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
import matriculas.services.SistemaCobrancaExterno;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/** Testa o conjunto completo de dados de exemplo, a geração de matrículas e a lotação de uma disciplina. */
public class DadosDemonstracaoMatriculasTest {
    public static void executar() {
        Aluno.limparRegistro();
        Disciplina.limparRegistro();
        Matricula.limparRegistro();

        Verificador.assertEquals(DadosDemonstracao.Conjunto.BASICO, DadosDemonstracao.Conjunto.de(null), "Sem conjunto informado, vale o básico.");
        Verificador.assertEquals(DadosDemonstracao.Conjunto.COMPLETO, DadosDemonstracao.Conjunto.de("completo"), "Conjunto 'completo' deve ser reconhecido.");
        Verificador.assertLanca(RegraDeNegocioException.class, () -> DadosDemonstracao.Conjunto.de("enorme"), "Conjunto desconhecido deve ser recusado.");

        Path pasta = null;
        try {
            pasta = Files.createTempDirectory("dados_demo_matriculas_");
            Persistencia persistencia = new Persistencia(pasta);
            ServicoCadastro servico = new ServicoCadastro();
            PeriodoMatricula periodo = new PeriodoMatricula();
            ServicoMatricula servicoMatricula = new ServicoMatricula(new SistemaCobrancaExterno(), periodo);

            DadosDemonstracao.carregar(servico, periodo, persistencia, DadosDemonstracao.Conjunto.COMPLETO);
            Verificador.assertEquals(2, servico.listarCursos().size(), "Conjunto completo deve ter 2 cursos.");
            Verificador.assertEquals(4, servico.listarProfessores().size(), "Conjunto completo deve ter 4 professores.");
            Verificador.assertEquals(20, Aluno.listarTodos().size(), "Conjunto completo deve ter 20 alunos.");
            Verificador.assertEquals(8, periodo.getDisciplinas().size(), "Conjunto completo deve ter 8 disciplinas no período.");

            Verificador.assertLanca(RegraDeNegocioException.class,
                () -> DadosDemonstracao.gerarMatriculas(servico, servicoMatricula, periodo, persistencia),
                "Gerar matrículas com o período fechado deve ser recusado.");
            Verificador.assertEquals(0, Matricula.listarTodos().size(), "Recusa não deve deixar matrículas pela metade.");

            periodo.abrir();
            int criadas = DadosDemonstracao.gerarMatriculas(servico, servicoMatricula, periodo, persistencia);
            Verificador.assertTrue(criadas > 0, "Deve criar matrículas de exemplo.");
            List<Disciplina> disciplinas = periodo.getDisciplinas();
            Verificador.assertEquals(20, disciplinas.get(0).quantidadeMatriculados(), "1ª disciplina recebe todos os alunos.");
            Verificador.assertEquals(10, disciplinas.get(1).quantidadeMatriculados(), "2ª disciplina recebe metade dos alunos.");
            Verificador.assertEquals(3, disciplinas.get(2).quantidadeMatriculados(), "3ª disciplina recebe o mínimo para ser confirmada.");
            Verificador.assertEquals(2, disciplinas.get(3).quantidadeMatriculados(), "4ª disciplina fica abaixo do mínimo.");
            for (Aluno aluno : Aluno.listarTodos()) {
                long ativas = aluno.getMatriculas().stream().filter(m -> m.getStatus() == StatusMatricula.ATIVA).count();
                Verificador.assertTrue(ativas <= 6, "Nenhum aluno pode passar de 4 obrigatórias + 2 optativas.");
            }
            Verificador.assertEquals(0, DadosDemonstracao.gerarMatriculas(servico, servicoMatricula, periodo, persistencia),
                "Gerar de novo não deve duplicar matrículas.");

            Disciplina alvo = disciplinas.get(3);
            int alunosAntes = Aluno.listarTodos().size();
            int vagas = alvo.vagasRestantes();
            int criados = DadosDemonstracao.lotarDisciplina(servico, servicoMatricula, periodo, persistencia, alvo.getNome());
            Verificador.assertEquals(vagas, criados, "Deve criar um aluno para cada vaga restante.");
            Verificador.assertEquals(0, alvo.vagasRestantes(), "Disciplina deve ficar lotada.");
            Verificador.assertEquals(alunosAntes + vagas, Aluno.listarTodos().size(), "Os alunos criados devem ser cadastrados.");

            Verificador.assertLanca(RegraDeNegocioException.class,
                () -> DadosDemonstracao.lotarDisciplina(servico, servicoMatricula, periodo, persistencia, alvo.getNome()),
                "Lotar uma disciplina já lotada deve ser recusado.");
            Aluno extra = servico.cadastrarAluno("Aluno Extra", "X1", "extra", "1234");
            Verificador.assertLanca(RegraDeNegocioException.class,
                () -> servicoMatricula.efetuarMatricula(extra, alvo, TipoMatricula.OBRIGATORIA),
                "Matrícula em disciplina lotada deve ser recusada.");

            periodo.encerrar();
            Disciplina outra = disciplinas.get(1);
            int alunosDepois = Aluno.listarTodos().size();
            Verificador.assertLanca(RegraDeNegocioException.class,
                () -> DadosDemonstracao.lotarDisciplina(servico, servicoMatricula, periodo, persistencia, outra.getNome()),
                "Lotar com o período fechado deve ser recusado.");
            Verificador.assertEquals(alunosDepois, Aluno.listarTodos().size(), "Recusa não deve criar alunos soltos.");
        } catch (IOException e) {
            Verificador.assertTrue(false, "Erro de I/O em DadosDemonstracaoMatriculasTest: " + e.getMessage());
        } finally {
            apagar(pasta);
        }
    }

    private static void apagar(Path pasta) {
        if (pasta == null) {
            return;
        }
        try (Stream<Path> arquivos = Files.walk(pasta)) {
            arquivos.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        } catch (IOException e) {
            // pasta temporária: se não der para apagar, o sistema operacional limpa depois
        }
    }
}
