package matriculas.models;

import matriculas.Verificador;
import matriculas.enums.StatusMatricula;
import matriculas.enums.TipoMatricula;
import matriculas.excecoes.RegraDeNegocioException;

import java.time.LocalDate;

public class MatriculaTest {
    public static void executar() {
        Aluno.limparRegistro();
        Disciplina.limparRegistro();
        Matricula.limparRegistro();

        Aluno aluno = new Aluno("Carlos", "123", "carlos", "senha123");
        Disciplina disciplina = new Disciplina("Estrutura de Dados");
        Matricula matricula = new Matricula(LocalDate.now(), TipoMatricula.OBRIGATORIA, StatusMatricula.ATIVA, aluno, disciplina);

        Verificador.assertEquals(StatusMatricula.ATIVA, matricula.getStatus(), "Status inicial deve ser ATIVA.");
        Verificador.assertTrue(matricula.cancelar(), "Primeiro cancelamento deve retornar true.");
        Verificador.assertEquals(StatusMatricula.CANCELADA, matricula.getStatus(), "Status após cancelamento deve ser CANCELADA.");
        Verificador.assertFalse(matricula.cancelar(), "Segundo cancelamento de matrícula já cancelada deve retornar false.");

        // Validação de null no construtor
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            new Matricula(null, TipoMatricula.OBRIGATORIA, StatusMatricula.ATIVA, aluno, disciplina);
        }, "Matrícula com data nula deve lançar exceção.");
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            new Matricula(LocalDate.now(), null, StatusMatricula.ATIVA, aluno, disciplina);
        }, "Matrícula com tipo nulo deve lançar exceção.");
    }
}
