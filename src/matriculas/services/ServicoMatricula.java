package matriculas.services;

import matriculas.enums.TipoMatricula;
import matriculas.interfaces.NotificadorCobranca;
import matriculas.models.Aluno;
import matriculas.models.Disciplina;
import matriculas.models.Matricula;
import matriculas.models.PeriodoMatricula;

public class ServicoMatricula {
    private NotificadorCobranca notificador;
    private PeriodoMatricula periodo;

    public Matricula efetuarMatricula(Aluno aluno, Disciplina disciplina, TipoMatricula tipo) {
        // TODO: implementar
        return null;
    }

    public boolean cancelarMatricula(Matricula matricula) {
        // TODO: implementar
        return false;
    }
}
