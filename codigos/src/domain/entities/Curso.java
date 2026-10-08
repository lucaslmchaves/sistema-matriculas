package domain.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entidade que representa um Curso na instituição.
 * Agrupa um conjunto de disciplinas e define os créditos base.
 */
public class Curso {
    
    private final String nome;
    private final int creditos;
    private final List<Disciplina> disciplinas;

    /**
     * Construtor da entidade Curso com validação de estado inicial (Fail-Fast).
     * 
     * @param nome Nome do curso.
     * @param creditos Quantidade de créditos do curso.
     * @throws IllegalArgumentException Se o nome for nulo/vazio ou se os créditos forem menores ou iguais a zero.
     */
    public Curso(String nome, int creditos) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do curso é obrigatório e não pode ser vazio.");
        }
        
        if (creditos <= 0) {
            throw new IllegalArgumentException("Os créditos devem ser maiores que zero.");
        }
        
        this.nome = nome;
        this.creditos = creditos;
        this.disciplinas = new ArrayList<>();
    }

    /**
     * @return O nome do curso.
     */
    public String getNome() { 
        return nome; 
    }
    
    /**
     * @return A quantidade de créditos associada ao curso.
     */
    public int getCreditos() { 
        return creditos; 
    }
    
    /**
     * Adiciona uma disciplina à grade curricular do curso.
     * 
     * @param d A disciplina a ser adicionada.
     * @return true se a disciplina for adicionada com sucesso à lista.
     * @throws IllegalArgumentException Se a disciplina fornecida for nula.
     */
    public boolean adicionarDisciplina(Disciplina d) { 
        if (d == null) {
            throw new IllegalArgumentException("A disciplina não pode ser nula.");
        }
        
        return this.disciplinas.add(d); 
    }

    /**
     * Retorna a lista de disciplinas vinculadas a este curso
     * @return Lista imutável de disciplinas.
     */
    public List<Disciplina> listarDisciplinas(){
        return Collections.unmodifiableList(this.disciplinas);
    }
}