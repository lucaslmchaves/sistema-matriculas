package matriculas.services;

import matriculas.Verificador;
import matriculas.enums.StatusDisciplina;
import matriculas.enums.StatusMatricula;
import matriculas.enums.TipoMatricula;
import matriculas.excecoes.RegraDeNegocioException;
import matriculas.interfaces.NotificadorCobranca;
import matriculas.models.Aluno;
import matriculas.models.Curso;
import matriculas.models.Disciplina;
import matriculas.models.Matricula;
import matriculas.models.PeriodoMatricula;
import matriculas.models.Professor;

import java.util.concurrent.atomic.AtomicBoolean;

public class ServicoMatriculaTest {

    private static class NotificadorFalso implements NotificadorCobranca {
        private boolean deveRetornarSucesso = true;
        private int chamadas = 0;

        @Override
        public boolean notificar(Matricula matricula) {
            chamadas++;
            return deveRetornarSucesso;
        }

        public void setDeveRetornarSucesso(boolean sucesso) {
            this.deveRetornarSucesso = sucesso;
        }

        public int getChamadas() {
            return chamadas;
        }
    }

    public static void executar() {
        Aluno.limparRegistro();
        Disciplina.limparRegistro();
        Matricula.limparRegistro();

        ServicoCadastro cadastro = new ServicoCadastro();
        Curso curso = cadastro.cadastrarCurso("Engenharia", 200);
        Professor prof = cadastro.cadastrarProfessor("Prof Carlos", "profCarlos", "senha123");

        PeriodoMatricula periodo = new PeriodoMatricula();
        NotificadorFalso notificador = new NotificadorFalso();
        ServicoMatricula servico = new ServicoMatricula(notificador, periodo);

        Disciplina d1 = cadastro.cadastrarDisciplina("D1", curso);
        d1.atribuirProfessor(prof);
        periodo.adicionarDisciplina(d1);

        Aluno aluno = cadastro.cadastrarAluno("Aluno Teste", "001", "ateste", "senha123");

        // 1. Tentar matricular com período fechado
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            servico.efetuarMatricula(aluno, d1, TipoMatricula.OBRIGATORIA);
        }, "Matrícula com período fechado deve lançar exceção.");

        // Abrir período
        periodo.abrir();

        // 2. Tentar matricular em disciplina que não está no período
        Disciplina dFora = cadastro.cadastrarDisciplina("DFora", curso);
        dFora.atribuirProfessor(prof);
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            servico.efetuarMatricula(aluno, dFora, TipoMatricula.OBRIGATORIA);
        }, "Matrícula em disciplina fora do período deve lançar exceção.");

        // 3. Notificador de cobrança falhando: deve lançar exceção e não registrar
        notificador.setDeveRetornarSucesso(false);
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            servico.efetuarMatricula(aluno, d1, TipoMatricula.OBRIGATORIA);
        }, "Falha no notificador de cobrança deve abortar a matrícula.");
        Verificador.assertEquals(0, Matricula.listarTodos().size(), "Matrícula não deve ser registrada se a cobrança falhar.");
        Verificador.assertEquals(0, aluno.getMatriculas().size(), "Aluno não deve conter matrícula se a cobrança falhar.");

        // 4. Notificador funcionando: sucesso na matrícula
        notificador.setDeveRetornarSucesso(true);
        Matricula m1 = servico.efetuarMatricula(aluno, d1, TipoMatricula.OBRIGATORIA);
        Verificador.assertEquals(StatusMatricula.ATIVA, m1.getStatus(), "Matrícula deve ser criada com status ATIVA.");
        Verificador.assertEquals(1, Matricula.listarTodos().size(), "Matrícula deve constar no registro geral.");
        Verificador.assertEquals(1, aluno.getMatriculas().size(), "Aluno deve conter a matrícula.");

        // 5. Matrícula duplicada do mesmo aluno na mesma disciplina
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            servico.efetuarMatricula(aluno, d1, TipoMatricula.OPTATIVA);
        }, "Matrícula duplicada na mesma disciplina deve lançar exceção.");

        // 6. Testar limite de 4 obrigatórias e 2 optativas
        Disciplina d2 = cadastro.cadastrarDisciplina("D2", curso);
        Disciplina d3 = cadastro.cadastrarDisciplina("D3", curso);
        Disciplina d4 = cadastro.cadastrarDisciplina("D4", curso);
        Disciplina d5 = cadastro.cadastrarDisciplina("D5", curso);
        Disciplina d6 = cadastro.cadastrarDisciplina("D6", curso);
        Disciplina d7 = cadastro.cadastrarDisciplina("D7", curso);

        d2.atribuirProfessor(prof);
        d3.atribuirProfessor(prof);
        d4.atribuirProfessor(prof);
        d5.atribuirProfessor(prof);
        d6.atribuirProfessor(prof);
        d7.atribuirProfessor(prof);

        // Forçar adição ao período para teste de limite de disciplinas
        periodo.restaurar(periodo.getDataInicio(), null, true, java.util.List.of(d1, d2, d3, d4, d5, d6, d7));

        // Aluno já tem d1 (obrigatória). Vamos matricular em d2, d3, d4 (total 4 obrigatórias)
        servico.efetuarMatricula(aluno, d2, TipoMatricula.OBRIGATORIA);
        servico.efetuarMatricula(aluno, d3, TipoMatricula.OBRIGATORIA);
        servico.efetuarMatricula(aluno, d4, TipoMatricula.OBRIGATORIA);

        // A 5ª obrigatória deve falhar
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            servico.efetuarMatricula(aluno, d5, TipoMatricula.OBRIGATORIA);
        }, "5ª matrícula obrigatória deve lançar exceção de limite.");

        // Matricular 2 optativas
        servico.efetuarMatricula(aluno, d5, TipoMatricula.OPTATIVA);
        servico.efetuarMatricula(aluno, d6, TipoMatricula.OPTATIVA);

        // A 3ª optativa deve falhar
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            servico.efetuarMatricula(aluno, d7, TipoMatricula.OPTATIVA);
        }, "3ª matrícula optativa deve lançar exceção de limite.");

        // 7. Cancelar matrícula com período aberto
        boolean cancelou = servico.cancelarMatricula(m1);
        Verificador.assertTrue(cancelou, "Cancelamento com período aberto deve ter sucesso.");
        Verificador.assertEquals(StatusMatricula.CANCELADA, m1.getStatus(), "Status da matrícula deve ser CANCELADA.");

        // Agora aluno tem 3 obrigatórias ativas, pode matricular em d5 como obrigatória?
        // d5 já é optativa ativa do aluno. Mas em d7 pode!
        Matricula m7 = servico.efetuarMatricula(aluno, d7, TipoMatricula.OBRIGATORIA);
        Verificador.assertEquals(StatusMatricula.ATIVA, m7.getStatus(), "Após cancelamento, nova obrigatória deve ser permitida.");

        // 8. Cancelar com período fechado deve falhar
        periodo.encerrar();
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            servico.cancelarMatricula(m7);
        }, "Cancelar matrícula com período fechado deve lançar exceção.");
    }
}
