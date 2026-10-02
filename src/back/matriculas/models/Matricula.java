package matriculas.models;

import matriculas.enums.StatusMatricula;
import matriculas.enums.TipoMatricula;
import matriculas.excecoes.RegraDeNegocioException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Matrícula de um aluno em uma disciplina. É a classe associativa entre Aluno
 * e Disciplina: guarda a data, o tipo (obrigatória ou optativa) e o status.
 * Todas as matrículas ficam no registro estático {@code matriculas}.
 */
public class Matricula {
    private static final List<Matricula> matriculas = new ArrayList<>();

    private LocalDate dataMatricula;
    private TipoMatricula tipo;
    private StatusMatricula status;
    private Aluno aluno;
    private Disciplina disciplina;

    public Matricula(LocalDate dataMatricula, TipoMatricula tipo, StatusMatricula status, Aluno aluno, Disciplina disciplina) {
        if (dataMatricula == null || tipo == null || status == null || aluno == null || disciplina == null) {
            throw new RegraDeNegocioException("Nenhum campo da matrícula pode ser nulo.");
        }
        this.dataMatricula = dataMatricula;
        this.tipo = tipo;
        this.status = status;
        this.aluno = aluno;
        this.disciplina = disciplina;
    }

    /** Cancela a matrícula. Devolve false se ela já estava cancelada. */
    public boolean cancelar() {
        if (this.status == StatusMatricula.CANCELADA) {
            return false;
        }
        this.status = StatusMatricula.CANCELADA;
        return true;
    }

    public LocalDate getDataMatricula() {
        return dataMatricula;
    }

    public TipoMatricula getTipo() {
        return tipo;
    }

    public StatusMatricula getStatus() {
        return status;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    /** Devolve todas as matrículas feitas (lista somente leitura). */
    public static List<Matricula> listarTodos() {
        return Collections.unmodifiableList(matriculas);
    }

    /** Adiciona uma matrícula ao registro geral. */
    public static void registrar(Matricula matricula) {
        if (matricula != null && !matriculas.contains(matricula)) {
            matriculas.add(matricula);
        }
    }

    /** Esvazia o registro (usado antes de recarregar os arquivos e nos testes). */
    public static void limparRegistro() {
        matriculas.clear();
    }
}
