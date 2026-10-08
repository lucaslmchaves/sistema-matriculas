package domain.entities;

import java.util.ArrayList;
import java.util.List;

import domain.enums.TipoMatricula;
import presentation.MenuVisitor;

import java.util.Collections;

/**
 * Entidade que representa um Aluno.
 * Protege as invariantes de negócio e retorna confirmação das operações 
 * vitais para facilitar testes unitários e controle de fluxo.
 */
public class Aluno extends Usuario {

    public static final int MAX_OBRIGATORIAS = 4;
    public static final int MAX_OPTATIVAS = 2;

    private String numeroMatricula;
    private List<Matricula> matriculas;

    
    public Aluno(String nome, String login, String senha, String numeroMatricula){
        super(nome, login, senha);

        if(numeroMatricula == null || numeroMatricula.trim().isEmpty())
            throw new IllegalArgumentException("O número de matrícula não pode ser nulo ou vazio");

        this.numeroMatricula = numeroMatricula;
        this.matriculas = new ArrayList<>();
    }

    public String getNumeroMatricula() {
        return numeroMatricula;
    }

    /**
     * Tenta adicionar uma nova matrícula validando os limites.
     * 
     * @param matricula A matrícula a ser inserida.
     * @return true se a matrícula foi adicionada com sucesso, false caso viole as regras de limite.
     */
    public boolean adicionarMatricula(Matricula matricula) {
        if (matricula == null) {
            return false;
        }

        TipoMatricula tipo = matricula.getTipo();
        long totalAtivasDesteTipo = contarMatriculasAtivasPorTipo(tipo);

        if (tipo == TipoMatricula.OBRIGATORIA && totalAtivasDesteTipo >= MAX_OBRIGATORIAS) {
            return false;
        }

        if (tipo == TipoMatricula.OPTATIVA && totalAtivasDesteTipo >= MAX_OPTATIVAS) {
            return false;
        }

        return this.matriculas.add(matricula);
    }

    public List<Matricula> getMatriculas() {
        return Collections.unmodifiableList(matriculas);
    }

    @Override
    public void interagir(MenuVisitor visitor) {
        visitor.exibirMenuAluno(this);
    }

    private long contarMatriculasAtivasPorTipo(TipoMatricula tipo) {
        return this.matriculas.stream()
            .filter(m -> m.getTipo() == tipo && m.isAtiva())
            .count();
    }
}