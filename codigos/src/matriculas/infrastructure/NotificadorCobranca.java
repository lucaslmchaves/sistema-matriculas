package matriculas.infrastructure;

import matriculas.domain.entities.Matricula;

/**
 * Interface de notificação de sistema externo (Princípio da Inversão de Dependência).
 */
public interface NotificadorCobranca {
    boolean notificar(Matricula matricula);
}
