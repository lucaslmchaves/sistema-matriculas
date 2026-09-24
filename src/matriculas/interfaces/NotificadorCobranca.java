package matriculas.interfaces;

import matriculas.models.Matricula;

public interface NotificadorCobranca {
    boolean notificar(Matricula matricula);
}
