package matriculas.web;

import matriculas.Verificador;
import matriculas.excecoes.RegraDeNegocioException;
import matriculas.models.Aluno;
import matriculas.models.Disciplina;
import matriculas.models.Matricula;
import matriculas.models.PeriodoMatricula;
import matriculas.persistencia.Persistencia;
import matriculas.services.ServicoCadastro;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DadosDemonstracaoTest {
    public static void executar() {
        Aluno.limparRegistro();
        Disciplina.limparRegistro();
        Matricula.limparRegistro();

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("dados_demo_test_");
            Persistencia persistencia = new Persistencia(tempDir);
            ServicoCadastro servicoCadastro = new ServicoCadastro();
            PeriodoMatricula periodo = new PeriodoMatricula();

            // Execução inicial: deve criar dados
            DadosDemonstracao.ResultadoCarga resultado = DadosDemonstracao.carregar(servicoCadastro, periodo, persistencia);

            Verificador.assertEquals(1, servicoCadastro.listarCursos().size(), "Deve conter exatamente 1 curso.");
            Verificador.assertEquals("Engenharia de Software", servicoCadastro.listarCursos().get(0).getNome(), "Nome do curso correto.");

            Verificador.assertEquals(2, servicoCadastro.listarProfessores().size(), "Deve conter 2 professores.");
            Verificador.assertTrue(servicoCadastro.buscarProfessorPorLogin("ana") != null, "Professora Ana deve existir.");
            Verificador.assertTrue(servicoCadastro.buscarProfessorPorLogin("carlos") != null, "Professor Carlos deve existir.");

            Verificador.assertEquals(5, Aluno.listarTodos().size(), "Deve conter 5 alunos criados.");
            Verificador.assertTrue(servicoCadastro.buscarAlunoPorLogin("aluno1") != null, "Aluno 1 deve existir.");
            Verificador.assertTrue(servicoCadastro.buscarAlunoPorLogin("aluno5") != null, "Aluno 5 deve existir.");

            Verificador.assertEquals(4, Disciplina.listarTodos().size(), "Deve conter 4 disciplinas criadas.");
            Verificador.assertEquals(4, periodo.getDisciplinas().size(), "Todas as 4 disciplinas devem estar no período.");
            Verificador.assertFalse(periodo.isAberto(), "O período deve permanecer FECHADO após carga de exemplo.");

            // Segunda execução: deve rejeitar com RegraDeNegocioException
            Verificador.assertLanca(RegraDeNegocioException.class, () -> {
                DadosDemonstracao.carregar(servicoCadastro, periodo, persistencia);
            }, "Segunda execução de DadosDemonstracao com dados existentes deve lançar RegraDeNegocioException.");

        } catch (IOException e) {
            Verificador.assertTrue(false, "Erro de I/O em DadosDemonstracaoTest: " + e.getMessage());
        } finally {
            if (tempDir != null) {
                try {
                    Files.walk(tempDir)
                            .sorted((a, b) -> b.compareTo(a))
                            .forEach(p -> {
                                try {
                                    Files.deleteIfExists(p);
                                } catch (IOException ignored) {}
                            });
                } catch (IOException ignored) {}
            }
        }
    }
}
