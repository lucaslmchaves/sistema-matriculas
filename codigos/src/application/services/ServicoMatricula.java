package application.services;

import domain.entities.*;
import domain.enums.StatusDisciplina;
import domain.enums.TipoMatricula;
import infrastructure.*;

/**
 * Serviço orquestrador responsável pelo fluxo transacional de matrículas.
 * Ele conecta a interface do usuário às entidades de domínio (Aluno, Disciplina) para aplicar 
 * limites de vagas e créditos. Também se liga ao BancoDeDados para checar a validade do período 
 * e ao NotificadorCobranca para engatilhar o faturamento externo.
 */
public class ServicoMatricula {
    
    private final BancoDeDados db;
    private final NotificadorCobranca notificador;

    /**
     * Construtor da classe com injeção das dependências de infraestrutura necessárias para o fluxo.
     * 
     * @param db A instância do banco de dados que guarda o status do período letivo.
     * @param notificador O serviço (ou mock) responsável por enviar os dados ao sistema financeiro.
     * @throws IllegalArgumentException Se o banco de dados ou o notificador forem nulos.
     */
    public ServicoMatricula(BancoDeDados db, NotificadorCobranca notificador) {
        if (db == null) throw new IllegalArgumentException("Banco de dados é obrigatório.");
        if (notificador == null) throw new IllegalArgumentException("Notificador de cobrança é obrigatório.");
        
        this.db = db; 
        this.notificador = notificador;
    }

    /**
     * Efetiva o registro de um aluno em uma disciplina, orquestrando as validações de ambas as partes.
     * 
     * @param aluno A entidade Aluno que está solicitando a matrícula.
     * @param disciplina A entidade Disciplina na qual o aluno deseja ingressar.
     * @param tipo A classificação da matrícula (OBRIGATORIA ou OPTATIVA).
     * @return true se o período estiver aberto, houver vaga, o limite de créditos for respeitado 
     *         e a cobrança for notificada; false se qualquer uma dessas validações falhar.
     */
    public boolean efetuarMatricula(Aluno aluno, Disciplina disciplina, TipoMatricula tipo) {
        if (!db.periodoAberto || aluno == null || disciplina == null || tipo == null) {
            return false;
        }

        Matricula m = new Matricula(aluno, disciplina, tipo);
        
        if (!disciplina.registrarMatricula(m)) return false;
        if (!aluno.adicionarMatricula(m)) return false;
        
        return notificador.notificar(m);
    }

    /**
     * Remove o vínculo ativo de um aluno com uma disciplina, invalidando a matrícula.
     * 
     * @param aluno A entidade Aluno que deseja cancelar a inscrição.
     * @param disciplina A entidade Disciplina da qual o aluno será removido.
     * @return true se a matrícula existir, estiver ativa e o período do sistema permitir a ação; 
     *         false caso a matrícula não seja encontrada ou já esteja cancelada.
     */
    public boolean cancelarMatricula(Aluno aluno, Disciplina disciplina) {
        if (!db.periodoAberto || aluno == null || disciplina == null) {
            return false;
        }
        
        Matricula matriculaAtiva = aluno.getMatriculas().stream()
            .filter(m -> m.getDisciplina().equals(disciplina) && m.isAtiva())
            .findFirst()
            .orElse(null);
            
        if (matriculaAtiva == null) {
            return false;
        }
        
        return matriculaAtiva.cancelar();
    }

    /**
     * Altera o estado do banco de dados para permitir o processamento de novas matrículas e cancelamentos.
     * 
     * @return true confirmando que a operação de abertura foi concluída.
     */
    public boolean abrirPeriodo() {
        this.db.periodoAberto = true;
        return true;
    }

    /**
     * Trava o banco de dados para novas matrículas e varre a grade curricular executando as regras de quórum.
     * Disciplinas ativas que não atingiram a quantidade mínima de alunos são reportadas e canceladas.
     * 
     * @return true confirmando que o processamento do fechamento e auditoria de disciplinas terminou.
     */
    public boolean encerrarPeriodo() {
        this.db.periodoAberto = false;
        
        for (Disciplina d : db.disciplinas) {
            if (d.getStatus() == StatusDisciplina.EM_ABERTO) {
                if (!d.isConfirmada()) {
                    System.out.println("[SISTEMA] Disciplina CANCELADA por falta de quórum: " + d.getNome());
                } else {
                    System.out.println("[SISTEMA] Disciplina ATIVADA para o próximo semestre: " + d.getNome());
                }
            }
        }
        return true;
    }
}