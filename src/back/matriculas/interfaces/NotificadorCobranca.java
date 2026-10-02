package matriculas.interfaces;

import matriculas.models.Matricula;

/** Contrato de quem recebe o aviso de uma nova matrícula para gerar a cobrança do aluno. */
public interface NotificadorCobranca {

    /** Avisa o sistema de cobrança. Devolve false se a notificação falhar. */
    boolean notificar(Matricula matricula);
}
