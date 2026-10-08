package test;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import application.services.ServicoAcademico;
import domain.entities.*;
import infrastructure.BancoDeDados;

public class GestaoAcademicaTest {
    private BancoDeDados db;
    private ServicoAcademico academico;

    @BeforeEach
    public void setup(){
        db = new BancoDeDados();
        academico = new ServicoAcademico(db);
    }

    /**
     * Valida o fluxo de criacao de um curso, uma discipina e a alocacao de um professor.
     */
    @Test
    public void testFluxoCompletoDeCriacaoEVinculoAcademico(){
        assertTrue(academico.cadastrarCurso("Sistemas de Informação",240));
        Curso cursoSalvo = db.cursos.get(0);

        assertTrue(academico.cadastrarDisciplina("Banco de Dados", cursoSalvo));
        Disciplina disciplinaSalva = db.disciplinas.get(0);

        assertTrue(academico.cadastrarProfessor("Kakashi Hatake", "kakashi", "senha123"));
        Professor profSalvo = db.professores.get(0);

        assertTrue(academico.vincularProfessor(disciplinaSalva, profSalvo));

        assertEquals("Kakashi Hatake", disciplinaSalva.getProfessor().getNome(), 
                     "O professor deve estar corretamente alocado à disciplina.");

        assertEquals(1, cursoSalvo.listarDisciplinas().size(), 
                    "A diciplina deve ter sido atrelada ao curso automaticamente.");
    }

    /**
     * Valida o cadastro de alunos pela Secretaria, garantindo que o usuario
     * va para as duas listas, Alunos e Usuarios.  
     */
    public void testCadastroDeAlunoAlimentaListasCorretamente(){
        boolean salvo = academico.cadastrarAluno("Salura Haruno","sakura","senha123","RA99");

        assertTrue(salvo);
        assertEquals(1, db.alunos.size(), "O aluno deve estar na lista de alunos.");
        assertEquals(1, db.usuarios.size(), "O aluno deve estar na lista generica de usuarios.");
    }

    /**
     * Testa a segurança do encapsulamento garantindo que a interface
     * nao pode adicionar dados nas listas por fora
     */
    @Test
    public void testListagensExpostasDevemSerImutaveis(){
        academico.cadastrarProfessor("Ero Sennin", "jiraya", "RA22");

        List<Professor> listaExposta = academico.listarProfessores();

        assertThrows(UnsupportedOperationException.class, () -> {
            listaExposta.add(new Professor("Invasor", "hacker", "123"));
        }, "As listas retornadas pelo serviço devem usar Collections.unmodifiableList() para reijeitar modificações externas.");
    }
}
