package matriculas.persistencia;

import matriculas.Verificador;
import matriculas.enums.StatusDisciplina;
import matriculas.enums.StatusMatricula;
import matriculas.enums.TipoMatricula;
import matriculas.models.Aluno;
import matriculas.models.Curso;
import matriculas.models.Disciplina;
import matriculas.models.Matricula;
import matriculas.models.PeriodoMatricula;
import matriculas.models.Professor;
import matriculas.services.ServicoCadastro;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

public class PersistenciaTest {
    public static void executar() {
        Aluno.limparRegistro();
        Disciplina.limparRegistro();
        Matricula.limparRegistro();

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("matriculas_test_");
            Persistencia persistencia = new Persistencia(tempDir);
            ServicoCadastro cadastro = new ServicoCadastro();

            Curso curso = cadastro.cadastrarCurso("Engenharia de Computação", 250);
            Professor prof = cadastro.cadastrarProfessor("Ada Lovelace", "alovelace", "senha123");
            Aluno aluno = cadastro.cadastrarAluno("Linus Torvalds", "202699", "linus", "kernel456");

            Disciplina disc = cadastro.cadastrarDisciplina("Sistemas Operacionais", curso);
            disc.atribuirProfessor(prof);

            PeriodoMatricula periodo = new PeriodoMatricula();
            periodo.adicionarDisciplina(disc);
            periodo.abrir();

            Matricula matricula = new Matricula(LocalDate.now(), TipoMatricula.OBRIGATORIA, StatusMatricula.ATIVA, aluno, disc);
            Matricula.registrar(matricula);
            aluno.adicionarMatricula(matricula);

            // Salvar
            persistencia.salvar(cadastro, periodo);

            // Limpar tudo da memória
            Aluno.limparRegistro();
            Disciplina.limparRegistro();
            Matricula.limparRegistro();
            cadastro.limpar();

            Verificador.assertEquals(0, Aluno.listarTodos().size(), "Memória de alunos deve estar vazia após limpar.");

            // Carregar
            ServicoCadastro novoCadastro = new ServicoCadastro();
            PeriodoMatricula novoPeriodo = persistencia.carregar(novoCadastro);

            // Verificar Cursos
            Verificador.assertEquals(1, novoCadastro.listarCursos().size(), "Deve carregar 1 curso.");
            Curso cursoCarregado = novoCadastro.buscarCursoPorNome("Engenharia de Computação");
            Verificador.assertTrue(cursoCarregado != null, "Curso deve ter sido restaurado.");
            Verificador.assertEquals(250, cursoCarregado.getNumeroCreditos(), "Créditos do curso devem ser 250.");

            // Verificar Professores
            Verificador.assertEquals(1, novoCadastro.listarProfessores().size(), "Deve carregar 1 professor.");
            Professor profCarregado = novoCadastro.buscarProfessorPorLogin("alovelace");
            Verificador.assertTrue(profCarregado != null, "Professor deve ter sido restaurado.");
            Verificador.assertTrue(profCarregado.autenticar("senha123"), "Senha do professor deve autenticar com o hash restaurado.");

            // Verificar Alunos
            Verificador.assertEquals(1, Aluno.listarTodos().size(), "Deve carregar 1 aluno.");
            Aluno alunoCarregado = novoCadastro.buscarAlunoPorMatricula("202699");
            Verificador.assertTrue(alunoCarregado != null, "Aluno deve ter sido restaurado.");
            Verificador.assertTrue(alunoCarregado.autenticar("kernel456"), "Senha do aluno deve autenticar com o hash restaurado.");

            // Verificar Disciplinas
            Verificador.assertEquals(1, Disciplina.listarTodos().size(), "Deve carregar 1 disciplina.");
            Disciplina discCarregada = novoCadastro.buscarDisciplinaPorNome("Sistemas Operacionais");
            Verificador.assertTrue(discCarregada != null, "Disciplina deve ter sido restaurada.");
            Verificador.assertEquals(StatusDisciplina.EM_ABERTO, discCarregada.getStatus(), "Status da disciplina deve ser restaurado.");
            Verificador.assertEquals("alovelace", discCarregada.getProfessor().getLogin(), "Professor da disciplina deve estar vinculado.");
            Verificador.assertEquals("Engenharia de Computação", discCarregada.getCurso().getNome(), "Curso da disciplina deve estar vinculado.");

            // Verificar Matrículas
            Verificador.assertEquals(1, Matricula.listarTodos().size(), "Deve carregar 1 matrícula.");
            Matricula matCarregada = Matricula.listarTodos().get(0);
            Verificador.assertEquals(StatusMatricula.ATIVA, matCarregada.getStatus(), "Status da matrícula deve ser ATIVA.");
            Verificador.assertEquals(alunoCarregado, matCarregada.getAluno(), "Aluno da matrícula deve ser a instância carregada.");
            Verificador.assertEquals(1, alunoCarregado.getMatriculas().size(), "Aluno deve ter a matrícula em sua coleção.");

            // Verificar Período
            Verificador.assertTrue(novoPeriodo.isAberto(), "Período deve estar aberto.");
            Verificador.assertEquals(1, novoPeriodo.getDisciplinas().size(), "Período deve conter 1 disciplina.");

            // Testar arquivo com linha malformada
            Path arqMalformado = tempDir.resolve("cursos.txt");
            Files.writeString(arqMalformado, "CursoIncompletoSemCreditos\n");
            Verificador.assertLanca(IllegalStateException.class, () -> {
                persistencia.carregar(new ServicoCadastro());
            }, "Arquivo com linha malformada deve lançar IllegalStateException.");

        } catch (IOException e) {
            Verificador.assertTrue(false, "Erro de I/O no teste de persistência: " + e.getMessage());
        } finally {
            if (tempDir != null) {
                deletarRecursivo(tempDir);
            }
        }
    }

    private static void deletarRecursivo(Path path) {
        try {
            if (Files.isDirectory(path)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                    for (Path filho : stream) {
                        deletarRecursivo(filho);
                    }
                }
            }
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }
}
