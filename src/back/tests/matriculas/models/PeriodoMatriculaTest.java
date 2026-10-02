package matriculas.models;

import matriculas.Verificador;
import matriculas.enums.StatusDisciplina;
import matriculas.enums.StatusMatricula;
import matriculas.enums.TipoMatricula;
import matriculas.excecoes.RegraDeNegocioException;

import java.time.LocalDate;
import java.util.List;

public class PeriodoMatriculaTest {
    public static void executar() {
        Aluno.limparRegistro();
        Disciplina.limparRegistro();
        Matricula.limparRegistro();

        PeriodoMatricula periodo = new PeriodoMatricula();
        Professor professor = new Professor("Ada Lovelace", "alovelace", "senha123");

        Disciplina d1 = new Disciplina("Programação Modular");
        Disciplina d2 = new Disciplina("Algoritmos");

        // Tentar abrir sem disciplinas deve lançar exceção
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            periodo.abrir();
        }, "Abrir período sem disciplinas deve lançar exceção.");

        // Tentar adicionar disciplina sem professor deve lançar exceção
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            periodo.adicionarDisciplina(d1);
        }, "Adicionar disciplina sem professor deve lançar exceção.");

        d1.atribuirProfessor(professor);
        d2.atribuirProfessor(professor);

        periodo.adicionarDisciplina(d1);
        periodo.adicionarDisciplina(d2);

        // Adicionar disciplina duplicada deve lançar exceção
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            periodo.adicionarDisciplina(d1);
        }, "Adicionar disciplina duplicada deve lançar exceção.");

        // Abrir período
        Verificador.assertTrue(periodo.abrir(), "Primeira abertura deve retornar true.");
        Verificador.assertTrue(periodo.isAberto(), "Período deve constar como aberto.");
        Verificador.assertEquals(LocalDate.now(), periodo.getDataInicio(), "Data de início deve ser a data atual.");
        Verificador.assertEquals(null, periodo.getDataFim(), "Data de fim deve ser null com o período aberto.");

        // Abrir novamente deve retornar false
        Verificador.assertFalse(periodo.abrir(), "Abrir período já aberto deve retornar false.");

        // Adicionar disciplina com período aberto deve lançar exceção
        Disciplina d3 = new Disciplina("Banco de Dados");
        d3.atribuirProfessor(professor);
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            periodo.adicionarDisciplina(d3);
        }, "Adicionar disciplina com o período aberto deve lançar exceção.");

        // Matricular alunos para testar a regra do mínimo de 3 no encerramento:
        // d1 terá 3 alunos (deve ficar ATIVA)
        // d2 terá 2 alunos (deve ficar CANCELADA)
        Aluno a1 = new Aluno("Aluno 1", "01", "u1", "senha123");
        Aluno a2 = new Aluno("Aluno 2", "02", "u2", "senha123");
        Aluno a3 = new Aluno("Aluno 3", "03", "u3", "senha123");

        Matricula m1 = new Matricula(LocalDate.now(), TipoMatricula.OBRIGATORIA, StatusMatricula.ATIVA, a1, d1);
        Matricula m2 = new Matricula(LocalDate.now(), TipoMatricula.OBRIGATORIA, StatusMatricula.ATIVA, a2, d1);
        Matricula m3 = new Matricula(LocalDate.now(), TipoMatricula.OBRIGATORIA, StatusMatricula.ATIVA, a3, d1);
        Matricula.registrar(m1);
        Matricula.registrar(m2);
        Matricula.registrar(m3);

        Matricula m4 = new Matricula(LocalDate.now(), TipoMatricula.OPTATIVA, StatusMatricula.ATIVA, a1, d2);
        Matricula m5 = new Matricula(LocalDate.now(), TipoMatricula.OPTATIVA, StatusMatricula.ATIVA, a2, d2);
        Matricula.registrar(m4);
        Matricula.registrar(m5);

        // Encerrar período
        List<Disciplina> canceladas = periodo.encerrar();
        Verificador.assertFalse(periodo.isAberto(), "Período deve constar como fechado.");
        Verificador.assertEquals(LocalDate.now(), periodo.getDataFim(), "Data de fim deve ser a data atual.");

        Verificador.assertEquals(StatusDisciplina.ATIVA, d1.getStatus(), "Disciplina com 3 alunos deve se tornar ATIVA.");
        Verificador.assertEquals(StatusDisciplina.CANCELADA, d2.getStatus(), "Disciplina com 2 alunos deve se tornar CANCELADA.");

        Verificador.assertEquals(1, canceladas.size(), "Deve haver 1 disciplina cancelada na lista retornada.");
        Verificador.assertTrue(canceladas.contains(d2), "Lista de canceladas deve conter d2.");

        // Matrículas de d2 devem ser canceladas automaticamente
        Verificador.assertEquals(StatusMatricula.CANCELADA, m4.getStatus(), "Matrícula de disciplina cancelada deve ser cancelada.");
        Verificador.assertEquals(StatusMatricula.CANCELADA, m5.getStatus(), "Matrícula de disciplina cancelada deve ser cancelada.");
        Verificador.assertEquals(StatusMatricula.ATIVA, m1.getStatus(), "Matrícula de disciplina ativa deve continuar ativa.");

        // Encerrar período já fechado deve lançar exceção
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            periodo.encerrar();
        }, "Encerrar período já fechado deve lançar exceção.");
    }
}
