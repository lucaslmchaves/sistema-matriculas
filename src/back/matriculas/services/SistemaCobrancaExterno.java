package matriculas.services;

import matriculas.interfaces.NotificadorCobranca;
import matriculas.models.Matricula;

/**
 * Simulação do sistema de cobrança externo: em vez de chamar um serviço real,
 * apenas imprime a notificação no console.
 */
public class SistemaCobrancaExterno implements NotificadorCobranca {

    @Override
    public boolean notificar(Matricula matricula) {
        if (matricula == null || matricula.getAluno() == null || matricula.getDisciplina() == null) {
            return false;
        }
        System.out.println("[COBRANÇA] Notificação de matrícula enviada para o aluno: "
            + matricula.getAluno().getNome() + " | Disciplina: " + matricula.getDisciplina().getNome());
        return true;
    }
}
