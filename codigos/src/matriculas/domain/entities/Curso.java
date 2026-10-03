package matriculas.domain.entities;

import java.util.ArrayList;
import java.util.List;

public class Curso {
    private final String nome;
    private final int creditos;
    private final List<Disciplina> disciplinas;

    public Curso(String nome, int creditos) {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("O nome do curso é obrigatório.");
        if (creditos <= 0) throw new IllegalArgumentException("Os créditos devem ser maiores que zero.");
        
        this.nome = nome;
        this.creditos = creditos;
        this.disciplinas = new ArrayList<>();
    }

    public String getNome() { return nome; }
    
    // Método adicionado para resolver o aviso "The value is not used"
    public int getCreditos() { return creditos; }
    
    public void adicionarDisciplina(Disciplina d) { 
        this.disciplinas.add(d); 
    }
}