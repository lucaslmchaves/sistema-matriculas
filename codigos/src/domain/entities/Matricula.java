package domain.entities;

import domain.enums.StatusMatricula;
import domain.enums.TipoMatricula;

/**
 * Entidade que representa a Matrícula de um Aluno em uma Disciplina.
 * Mantém o estado desta relação e blinda suas transações contra inconsistências.
 */
public class Matricula {
    
    private final Aluno aluno;
    private final Disciplina disciplina;
    private final TipoMatricula tipo;
    private StatusMatricula status;

    /**
     * Construtor da entidade Matrícula com validação de estado inicial (Fail-Fast).
     * 
     * @param aluno O aluno vinculado à matrícula.
     * @param disciplina A disciplina vinculada à matrícula.
     * @param tipo O tipo de matrícula (OBRIGATORIA ou OPTATIVA).
     * @throws IllegalArgumentException Se o aluno, a disciplina ou o tipo forem nulos.
     */
    public Matricula(Aluno aluno, Disciplina disciplina, TipoMatricula tipo) {
        if (aluno == null) {
            throw new IllegalArgumentException("O aluno da matrícula não pode ser nulo.");
        }
        if (disciplina == null) {
            throw new IllegalArgumentException("A disciplina da matrícula não pode ser nula.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("O tipo de matrícula não pode ser nulo.");
        }

        this.aluno = aluno;
        this.disciplina = disciplina;
        this.tipo = tipo;
        this.status = StatusMatricula.ATIVA;
    }

    //#region getters e setters para a interface
    public Aluno getAluno() { return aluno; }
    public Disciplina getDisciplina() { return disciplina; }
    public TipoMatricula getTipo() { return tipo; }
    //#endregion

    /**
     * Verifica se a matrícula encontra-se ativa no sistema.
     * Método semântico utilizado em regras de negócio de outras entidades (como Disciplina e Aluno).
     * 
     * @return true se o status for ATIVA, false se for CANCELADA.
     */
    public boolean isAtiva() { 
        return this.status == StatusMatricula.ATIVA; 
    }

    /**
     * Cancela a matrícula do aluno na disciplina.
     * Aplica o princípio "Tell, Don't Ask", gerenciando a transição de status internamente.
     * 
     * @return true se a matrícula foi cancelada com sucesso, false se já estava previamente cancelada.
     */
    public boolean cancelar() { 
        if (this.status == StatusMatricula.CANCELADA) {
            return false;
        }
        
        this.status = StatusMatricula.CANCELADA;
        return true; 
    }
}