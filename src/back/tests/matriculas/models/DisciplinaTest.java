package matriculas.models;

import matriculas.Verificador;
import matriculas.enums.StatusDisciplina;
import matriculas.enums.StatusMatricula;
import matriculas.enums.TipoMatricula;
import matriculas.excecoes.RegraDeNegocioException;

import java.time.LocalDate;
import java.util.List;

public class DisciplinaTest {
    public static void executar() {
        Aluno.limparRegistro();
        Disciplina.limparRegistro();
        Matricula.limparRegistro();

        Disciplina disciplina = new Disciplina("Cálculo 1");
        Verificador.assertEquals(StatusDisciplina.EM_ABERTO, disciplina.getStatus(), "Status inicial da disciplina deve ser EM_ABERTO.");
        Verificador.assertEquals(0, disciplina.quantidadeMatriculados(), "Disciplina nova deve ter zero matriculados.");
        Verificador.assertTrue(disciplina.temVaga(), "Disciplina com 0 matriculados deve ter vaga disponível.");

        // Atribuir professor
        Professor professor = new Professor("Alan Turing", "aturing", "senha123");
        disciplina.atribuirProfessor(professor);
        Verificador.assertEquals(professor, disciplina.getProfessor(), "Professor deve ser atribuído corretamente.");

        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            disciplina.atribuirProfessor(null);
        }, "Atribuir professor null deve lançar exceção.");

        // Adicionar matrículas e testar contagem e listagem
        Aluno aluno1 = new Aluno("Aluno 1", "001", "a1", "senha123");
        Aluno aluno2 = new Aluno("Aluno 2", "002", "a2", "senha123");
        Matricula m1 = new Matricula(LocalDate.now(), TipoMatricula.OBRIGATORIA, StatusMatricula.ATIVA, aluno1, disciplina);
        Matricula m2 = new Matricula(LocalDate.now(), TipoMatricula.OPTATIVA, StatusMatricula.ATIVA, aluno2, disciplina);
        Matricula.registrar(m1);
        Matricula.registrar(m2);

        Verificador.assertEquals(2, disciplina.quantidadeMatriculados(), "Quantidade de matriculados deve refletir as matrículas ativas.");
        List<Aluno> matriculados = disciplina.listarAlunosMatriculados();
        Verificador.assertEquals(2, matriculados.size(), "Lista de alunos deve conter 2 alunos.");
        Verificador.assertTrue(matriculados.contains(aluno1), "Lista deve conter aluno 1.");
        Verificador.assertTrue(matriculados.contains(aluno2), "Lista deve conter aluno 2.");

        // Matrícula cancelada não conta
        m2.cancelar();
        Verificador.assertEquals(1, disciplina.quantidadeMatriculados(), "Matrícula cancelada não deve constar na contagem de matriculados.");
        Verificador.assertEquals(1, disciplina.listarAlunosMatriculados().size(), "Matrícula cancelada não deve constar na listagem de alunos.");

        // Teste de 60 vagas
        Matricula.limparRegistro();
        for (int i = 0; i < 60; i++) {
            Aluno a = new Aluno("Aluno " + i, "M" + i, "user" + i, "senha123");
            Matricula m = new Matricula(LocalDate.now(), TipoMatricula.OBRIGATORIA, StatusMatricula.ATIVA, a, disciplina);
            Matricula.registrar(m);
        }
        Verificador.assertEquals(60, disciplina.quantidadeMatriculados(), "Deve contar 60 matrículas ativas.");
        Verificador.assertFalse(disciplina.temVaga(), "Disciplina com 60 alunos não deve ter mais vagas.");

        // Validação de nomes inválidos
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            new Disciplina("Física;1");
        }, "Nome de disciplina com ';' deve lançar exceção.");
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            new Disciplina("");
        }, "Nome de disciplina vazio deve lançar exceção.");
    }
}
