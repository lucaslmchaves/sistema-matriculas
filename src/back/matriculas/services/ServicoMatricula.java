package matriculas.services;

import matriculas.enums.StatusDisciplina;
import matriculas.enums.StatusMatricula;
import matriculas.enums.TipoMatricula;
import matriculas.excecoes.RegraDeNegocioException;
import matriculas.interfaces.NotificadorCobranca;
import matriculas.models.Aluno;
import matriculas.models.Disciplina;
import matriculas.models.Matricula;
import matriculas.models.PeriodoMatricula;

import java.time.LocalDate;

/**
 * Serviço que executa as regras de matrícula e cancelamento. Depende da
 * interface {@link NotificadorCobranca}, e não de uma classe concreta, para que
 * o sistema de cobrança possa ser trocado sem mexer aqui.
 */
public class ServicoMatricula {
    private NotificadorCobranca notificador;
    private PeriodoMatricula periodo;

    public ServicoMatricula(NotificadorCobranca notificador, PeriodoMatricula periodo) {
        if (notificador == null) {
            throw new RegraDeNegocioException("Notificador de cobrança não pode ser nulo.");
        }
        if (periodo == null) {
            throw new RegraDeNegocioException("Período de matrículas não pode ser nulo.");
        }
        this.notificador = notificador;
        this.periodo = periodo;
    }

    /**
     * Matricula o aluno na disciplina, na ordem: período aberto, disciplina ofertada e não
     * cancelada, aluno ainda sem matrícula ativa nela, vaga disponível e limite do tipo.
     * Se tudo passar, o sistema de cobrança é notificado e só então a matrícula é registrada.
     */
    public Matricula efetuarMatricula(Aluno aluno, Disciplina disciplina, TipoMatricula tipo) {
        if (aluno == null || disciplina == null || tipo == null) {
            throw new RegraDeNegocioException("Aluno, disciplina e tipo de matrícula são obrigatórios.");
        }
        validarDisciplinaDoPeriodo(disciplina);
        if (possuiMatriculaAtiva(aluno, disciplina)) {
            throw new RegraDeNegocioException("O aluno já possui matrícula ativa nesta disciplina.");
        }
        if (!disciplina.temVaga()) {
            throw new RegraDeNegocioException("A disciplina atingiu a capacidade máxima de alunos.");
        }
        if (!aluno.podeMatricular(tipo)) {
            throw new RegraDeNegocioException("O aluno atingiu o limite de matrículas do tipo " + tipo + ".");
        }

        Matricula matricula = new Matricula(LocalDate.now(), tipo, StatusMatricula.ATIVA, aluno, disciplina);
        if (!notificador.notificar(matricula)) {
            throw new RegraDeNegocioException("Falha ao notificar o sistema de cobrança. Matrícula cancelada.");
        }

        Matricula.registrar(matricula);
        aluno.adicionarMatricula(matricula);
        return matricula;
    }

    /** Cancela a matrícula. Só é permitido com o período de matrículas aberto. */
    public boolean cancelarMatricula(Matricula matricula) {
        if (matricula == null) {
            throw new RegraDeNegocioException("Matrícula não pode ser nula.");
        }
        if (!periodo.isAberto()) {
            throw new RegraDeNegocioException("Não é possível cancelar matrícula com o período de matrículas fechado.");
        }
        return matricula.cancelar();
    }

    private void validarDisciplinaDoPeriodo(Disciplina disciplina) {
        if (!periodo.isAberto()) {
            throw new RegraDeNegocioException("O período de matrículas não está aberto.");
        }
        if (!periodo.getDisciplinas().contains(disciplina)) {
            throw new RegraDeNegocioException("A disciplina informada não faz parte do período de matrículas atual.");
        }
        if (disciplina.getStatus() == StatusDisciplina.CANCELADA) {
            throw new RegraDeNegocioException("Não é possível matricular-se em uma disciplina cancelada.");
        }
    }

    private boolean possuiMatriculaAtiva(Aluno aluno, Disciplina disciplina) {
        for (Matricula matricula : aluno.getMatriculas()) {
            if (disciplina.equals(matricula.getDisciplina()) && matricula.getStatus() == StatusMatricula.ATIVA) {
                return true;
            }
        }
        return false;
    }
}
