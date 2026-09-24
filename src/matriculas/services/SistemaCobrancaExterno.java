package matriculas.services;

import matriculas.interfaces.NotificadorCobranca;
import matriculas.models.Matricula;

public class SistemaCobrancaExterno implements NotificadorCobranca {

    @Override
    public boolean notificar(Matricula matricula) {
        // TODO: implementar
        return false;
    }
}
