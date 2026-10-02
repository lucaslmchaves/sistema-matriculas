package matriculas.models;

import matriculas.excecoes.RegraDeNegocioException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Curso oferecido pela universidade. É composto por disciplinas: cada
 * disciplina pertence a um único curso.
 */
public class Curso {
    private String nome;
    private int numeroCreditos;
    private final List<Disciplina> disciplinas;

    public Curso(String nome, int numeroCreditos) {
        Validacao.texto(nome, "Nome do curso");
        if (numeroCreditos <= 0) {
            throw new RegraDeNegocioException("Número de créditos deve ser maior que zero.");
        }
        this.nome = nome.trim();
        this.numeroCreditos = numeroCreditos;
        this.disciplinas = new ArrayList<>();
    }

    /** Liga uma disciplina ao curso (ignora se ela já estiver na lista). */
    public void adicionarDisciplina(Disciplina disciplina) {
        if (disciplina == null) {
            throw new RegraDeNegocioException("Disciplina não pode ser nula.");
        }
        if (!disciplinas.contains(disciplina)) {
            disciplinas.add(disciplina);
        }
    }

    public List<Disciplina> getDisciplinas() {
        return Collections.unmodifiableList(disciplinas);
    }

    public String getNome() {
        return nome;
    }

    public int getNumeroCreditos() {
        return numeroCreditos;
    }
}
