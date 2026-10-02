package matriculas.services;

import matriculas.Verificador;
import matriculas.excecoes.RegraDeNegocioException;
import matriculas.models.Aluno;
import matriculas.models.Curso;
import matriculas.models.Disciplina;
import matriculas.models.Matricula;
import matriculas.models.Professor;

public class ServicoCadastroTest {
    public static void executar() {
        Aluno.limparRegistro();
        Disciplina.limparRegistro();
        Matricula.limparRegistro();

        ServicoCadastro servico = new ServicoCadastro();

        // 1. Cursos
        Curso c1 = servico.cadastrarCurso("Engenharia de Software", 240);
        Verificador.assertEquals("Engenharia de Software", c1.getNome(), "Nome do curso deve ser cadastrado.");
        Verificador.assertEquals(240, c1.getNumeroCreditos(), "Créditos do curso devem ser cadastrados.");

        // Rejeitar créditos <= 0
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            servico.cadastrarCurso("Curso Inválido", 0);
        }, "Créditos <= 0 deve lançar exceção.");

        // Rejeitar duplicata de nome de curso case-insensitive
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            servico.cadastrarCurso("engenharia de software", 180);
        }, "Nome de curso duplicado (case-insensitive) deve lançar exceção.");

        // 2. Professores
        Professor p1 = servico.cadastrarProfessor("Prof Carlos", "pcarlos", "senha123");
        Verificador.assertEquals("Prof Carlos", p1.getNome(), "Nome do professor deve ser cadastrado.");

        // Rejeitar login duplicado de professor
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            servico.cadastrarProfessor("Outro Carlos", "pcarlos", "senha456");
        }, "Login de professor duplicado deve lançar exceção.");

        // 3. Alunos
        Aluno a1 = servico.cadastrarAluno("Aluno Lucas", "MAT001", "llucas", "senha123");
        Verificador.assertEquals("Aluno Lucas", a1.getNome(), "Nome do aluno deve ser cadastrado.");

        // Rejeitar número de matrícula duplicado
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            servico.cadastrarAluno("Outro Lucas", "MAT001", "outro", "senha123");
        }, "Matrícula de aluno duplicada deve lançar exceção.");

        // Rejeitar login duplicado
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            servico.cadastrarAluno("Outro Lucas", "MAT002", "llucas", "senha123");
        }, "Login de aluno duplicado deve lançar exceção.");

        // 4. Disciplinas
        Disciplina d1 = servico.cadastrarDisciplina("Arquitetura de Software", c1);
        Verificador.assertEquals("Arquitetura de Software", d1.getNome(), "Nome da disciplina deve ser cadastrado.");
        Verificador.assertTrue(c1.getDisciplinas().contains(d1), "Disciplina deve ser adicionada à lista do curso.");
        Verificador.assertTrue(Disciplina.listarTodos().contains(d1), "Disciplina deve constar no registro geral.");

        // Rejeitar disciplina com nome duplicado
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            servico.cadastrarDisciplina("arquitetura de software", c1);
        }, "Nome de disciplina duplicado deve lançar exceção.");

        // Rejeitar disciplina com curso nulo
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            servico.cadastrarDisciplina("Outra", null);
        }, "Cadastrar disciplina com curso null deve lançar exceção.");
    }
}
