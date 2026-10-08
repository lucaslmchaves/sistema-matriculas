package infrastructure;

import domain.entities.Matricula;

/**
 * Interface que dita o contrato de comunicação com o sistema financeiro externo[cite: 5].
 * Conecta-se diretamente ao ServicoMatricula, protegendo as regras de negócio 
 * contra mudanças bruscas na API de cobrança (Princípio da Inversão de Dependência)[cite: 5].
 */
public interface NotificadorCobranca {
    
    /**
     * Envia os dados de uma matrícula efetivada para o setor ou API de faturamento[cite: 5].
     * 
     * @param matricula O objeto da matrícula confirmada, contendo o aluno a ser cobrado e a disciplina.
     * @return true se o sistema externo recebeu e processou a notificação com sucesso.
     */
    boolean notificar(Matricula matricula);
}