package matriculas.models;

import matriculas.Verificador;
import matriculas.enums.StatusDisciplina;
import matriculas.enums.StatusMatricula;
import matriculas.enums.TipoMatricula;

import java.time.LocalDate;

/** Testa os valores devolvidos pelos métodos que antes não devolviam nada (sem void). */
public class RetornosDoDominioTest {
    public static void executar() {
        Aluno.limparRegistro();
        Disciplina.limparRegistro();
        Matricula.limparRegistro();

        Verificador.assertEquals("Ana", Validacao.texto("  Ana ", "Nome"), "Validação de texto deve devolver o valor sem espaços nas pontas.");

        Aluno aluno = new Aluno("Carlos", "123", "carlos", "senha123");
        Disciplina disciplina = new Disciplina("Estrutura de Dados");
        Matricula matricula = new Matricula(LocalDate.now(), TipoMatricula.OBRIGATORIA, StatusMatricula.ATIVA, aluno, disciplina);

        Verificador.assertTrue(aluno.adicionarMatricula(matricula), "Primeira matrícula do aluno deve ser guardada.");
        Verificador.assertFalse(aluno.adicionarMatricula(matricula), "Matrícula repetida deve ser ignorada.");
        Verificador.assertFalse(aluno.adicionarMatricula(null), "Matrícula nula deve ser ignorada.");

        Verificador.assertTrue(Aluno.registrar(aluno), "Primeiro registro do aluno deve funcionar.");
        Verificador.assertFalse(Aluno.registrar(aluno), "Aluno repetido não deve ser registrado de novo.");
        Verificador.assertTrue(Disciplina.registrar(disciplina), "Primeiro registro da disciplina deve funcionar.");
        Verificador.assertFalse(Disciplina.registrar(null), "Disciplina nula não deve ser registrada.");
        Verificador.assertTrue(Matricula.registrar(matricula), "Primeiro registro da matrícula deve funcionar.");
        Verificador.assertFalse(Matricula.registrar(matricula), "Matrícula repetida não deve ser registrada de novo.");

        Curso curso = new Curso("Engenharia de Software", 240);
        Verificador.assertTrue(curso.adicionarDisciplina(disciplina), "Disciplina nova deve entrar no curso.");
        Verificador.assertFalse(curso.adicionarDisciplina(disciplina), "Disciplina repetida não deve entrar duas vezes no curso.");

        Professor professor = new Professor("Marta", "marta", "senha123");
        Verificador.assertTrue(disciplina.atribuirProfessor(professor), "Primeiro professor deve ser atribuído.");
        Verificador.assertFalse(disciplina.atribuirProfessor(professor), "Atribuir o mesmo professor não muda nada.");

        Verificador.assertTrue(disciplina.setStatus(StatusDisciplina.ATIVA), "Mudar o status deve devolver true.");
        Verificador.assertFalse(disciplina.setStatus(StatusDisciplina.ATIVA), "Repetir o status não muda nada.");

        PeriodoMatricula periodo = new PeriodoMatricula();
        Verificador.assertTrue(periodo.adicionarDisciplina(disciplina), "Disciplina deve entrar no período.");
        Verificador.assertEquals(1, periodo.restaurar(null, null, false, periodo.getDisciplinas().stream().toList()),
            "Restaurar deve devolver quantas disciplinas voltaram ao período.");

        Verificador.assertEquals(1, Matricula.limparRegistro(), "Limpar o registro deve devolver quantas matrículas saíram.");
        Verificador.assertEquals(0, Matricula.limparRegistro(), "Registro vazio não remove nada.");
        Verificador.assertEquals(1, Disciplina.limparRegistro(), "Limpar o registro deve devolver quantas disciplinas saíram.");
        Verificador.assertEquals(1, Aluno.limparRegistro(), "Limpar o registro deve devolver quantos alunos saíram.");
    }
}
