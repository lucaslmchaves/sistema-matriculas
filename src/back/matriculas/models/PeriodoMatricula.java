package matriculas.models;

import matriculas.enums.StatusDisciplina;
import matriculas.excecoes.RegraDeNegocioException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Período em que os alunos podem se matricular. Agrega as disciplinas
 * ofertadas e, ao ser encerrado, confirma as que atingiram o mínimo de alunos
 * e cancela as demais.
 */
public class PeriodoMatricula {
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private boolean aberto;
    private final List<Disciplina> disciplinas;

    public PeriodoMatricula() {
        this.disciplinas = new ArrayList<>();
        this.aberto = false;
    }

    /** Oferta uma disciplina no período. Só vale com o período fechado e a disciplina já com professor. */
    public void adicionarDisciplina(Disciplina disciplina) {
        if (disciplina == null) {
            throw new RegraDeNegocioException("Disciplina não pode ser nula.");
        }
        if (this.aberto) {
            throw new RegraDeNegocioException("Não é possível adicionar disciplinas com o período de matrículas aberto.");
        }
        if (disciplina.getProfessor() == null) {
            throw new RegraDeNegocioException("A disciplina deve possuir um professor atribuído para entrar no período.");
        }
        if (disciplinas.contains(disciplina)) {
            throw new RegraDeNegocioException("A disciplina já foi adicionada a este período.");
        }
        disciplinas.add(disciplina);
    }

    public List<Disciplina> getDisciplinas() {
        return Collections.unmodifiableList(disciplinas);
    }

    /** Abre as matrículas. Devolve false se já estava aberto; exige ao menos uma disciplina. */
    public boolean abrir() {
        if (this.aberto) {
            return false;
        }
        if (this.disciplinas.isEmpty()) {
            throw new RegraDeNegocioException("Não é possível abrir período de matrículas sem disciplinas cadastradas.");
        }
        this.aberto = true;
        this.dataInicio = LocalDate.now();
        this.dataFim = null;
        return true;
    }

    /**
     * Encerra as matrículas. Disciplinas com o mínimo de alunos ficam ATIVAS; as outras
     * ficam CANCELADAS, e as matrículas ativas delas também são canceladas.
     * Devolve as disciplinas canceladas.
     */
    public List<Disciplina> encerrar() {
        if (!this.aberto) {
            throw new RegraDeNegocioException("O período de matrículas não está aberto para encerramento.");
        }
        this.aberto = false;
        this.dataFim = LocalDate.now();

        List<Disciplina> canceladas = new ArrayList<>();
        for (Disciplina disciplina : disciplinas) {
            if (disciplina.atingiuMinimoDeAlunos()) {
                disciplina.setStatus(StatusDisciplina.ATIVA);
            } else {
                cancelarDisciplina(disciplina);
                canceladas.add(disciplina);
            }
        }
        return Collections.unmodifiableList(canceladas);
    }

    public boolean isAberto() {
        return aberto;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    /** Devolve o período ao estado salvo em arquivo, sem passar pelas regras de abrir/encerrar. */
    public void restaurar(LocalDate dataInicio, LocalDate dataFim, boolean aberto, List<Disciplina> disciplinasRestauradas) {
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.aberto = aberto;
        this.disciplinas.clear();
        if (disciplinasRestauradas != null) {
            this.disciplinas.addAll(disciplinasRestauradas);
        }
    }

    private void cancelarDisciplina(Disciplina disciplina) {
        disciplina.setStatus(StatusDisciplina.CANCELADA);
        for (Matricula matricula : disciplina.matriculasAtivas()) {
            matricula.cancelar();
        }
    }
}
