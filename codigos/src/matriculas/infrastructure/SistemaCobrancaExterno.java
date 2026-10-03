package matriculas.infrastructure;

import matriculas.domain.entities.Matricula;

public class SistemaCobrancaExterno implements NotificadorCobranca {
    
    @Override public boolean notificar(Matricula m) {
        return true; 
    }
}
