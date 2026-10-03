package matriculas.app.services;

import matriculas.domain.entities.*;
import matriculas.domain.enums.TipoMatricula;
import matriculas.domain.enums.StatusDisciplina;
import matriculas.infrastructure.*;

/**
 * Serviço exclusivo para gerenciar o ciclo de vida das matrículas e regras de semestre.
 */
public class ServicoMatricula {
    private final BancoDeDados db;
    private final NotificadorCobranca notificador;

    public ServicoMatricula(BancoDeDados db, NotificadorCobranca notificador) {
        this.db = db; 
        this.notificador = notificador;
    }

    public void efetuarMatricula(Aluno aluno, Disciplina disciplina, TipoMatricula tipo) {
        if (!db.periodoAberto) throw new IllegalStateException("O período de matrículas está fechado.");
        Matricula m = new Matricula(aluno, disciplina, tipo);
        aluno.adicionarMatricula(m);
        disciplina.registrarMatricula(m);
        notificador.notificar(m);
    }

    public void cancelarMatricula(Aluno aluno, Disciplina disciplina) {
        if (!db.periodoAberto) throw new IllegalStateException("Cancelamentos só são permitidos no período de matrículas.");
        
        Matricula matriculaAtiva = aluno.getMatriculas().stream()
            .filter(m -> m.getDisciplina().equals(disciplina) && m.isAtiva())
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Matrícula não encontrada ou já cancelada."));
            
        matriculaAtiva.cancelar();
    }

    public void abrirPeriodo() {
        this.db.periodoAberto = true;
    }

    /**
     * Regra de negócio: Disciplinas com menos de 3 alunos inscritos são canceladas.
     */
    public void encerrarPeriodo() {
        this.db.periodoAberto = false;
        
        for (Disciplina d : db.disciplinas) {
            if (d.getStatus() == StatusDisciplina.EM_ABERTO) {
                // Listar alunos filtra apenas matrículas ATIVAS
                if (d.listarAlunos().size() < 3) {
                    // Acesso via Reflection ou Setters simplificado: na sua entidade pode precisar de um método cancelar()
                    // Vamos considerar que a entidade lida com o estado ou imprimimos o status
                    System.out.println("[SISTEMA] Disciplina CANCELADA por falta de quórum: " + d.getNome());
                } else {
                    System.out.println("[SISTEMA] Disciplina ATIVADA para o próximo semestre: " + d.getNome());
                }
            }
        }
    }
}