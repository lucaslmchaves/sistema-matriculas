package infrastructure;

import java.util.ArrayList;
import java.util.List;

import domain.entities.*;

/**
 * Repositório em memória que centraliza o armazenamento das entidades do sistema[cite: 3].
 * Ele é injetado e consumido por todos os Serviços de Aplicação (ServicoAcademico, 
 * ServicoAutenticacao e ServicoMatricula) para realizar leituras e persistências simples durante a execução[cite: 3].
 */
public class BancoDeDados {
    
    public final List<Usuario> usuarios = new ArrayList<>();
    public final List<Curso> cursos = new ArrayList<>();
    public final List<Disciplina> disciplinas = new ArrayList<>();
    public final List<Aluno> alunos = new ArrayList<>();
    public final List<Professor> professores = new ArrayList<>();
    
    public boolean periodoAberto = true; 
}