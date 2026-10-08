package infrastructure;

import domain.entities.Matricula;

/**
 * Implementação concreta do contrato do NotificadorCobranca[cite: 6].
 * Simula a comunicação real de faturamento conectando os dados gerados pela 
 * camada de aplicação a um ambiente externo via terminal[cite: 6].
 */
public class SistemaCobrancaExterno implements NotificadorCobranca {
    
    /**
     * Simula o envio de dados da matrícula garantindo a integridade do objeto recebido[cite: 6].
     * 
     * @param m O objeto de matrícula efetivado pelo ServicoMatricula.
     * @return true sinalizando que o faturamento ocorreu normalmente, ou false caso a matrícula enviada seja nula.
     */
    @Override 
    public boolean notificar(Matricula m) {
        if (m == null) {
            return false;
        }
        
        System.out.println("[COBRANÇA] Notificação de faturamento simulada e enviada com sucesso para o aluno: " + m.getAluno().getNome());
        return true; 
    }
}