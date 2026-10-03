package matriculas.infrastructure;

import matriculas.domain.entities.*;
import java.util.ArrayList;
import java.util.List;

public class BancoDeDados {
    public final List<Usuario> usuarios = new ArrayList<>();
    public final List<Curso> cursos = new ArrayList<>();
    public final List<Disciplina> disciplinas = new ArrayList<>();
    public final List<Aluno> alunos = new ArrayList<>();
    public final List<Professor> professores = new ArrayList<>();
    
    public boolean periodoAberto = true; 
}