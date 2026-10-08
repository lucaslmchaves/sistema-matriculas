package test;

import application.services.*;
import domain.entities.*;
import domain.enums.TipoMatricula;
import infrastructure.*;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * - MatriculaRegrasTest
 * Suíte responsável exclusivamente por testar as regras transcacionais de limite,
 * quórum e lotação máxima do sistema.
 */
public class MatriculaRegrasTest {
    
    private static int LIMITE_MAX_ALUNOS = 60;
    private static int QUANT_MAX_DISCIPLINAS = 4;
    private BancoDeDados db;
    private ServicoMatricula servicoMatricula;
    private Aluno alunoTeste;
    private Disciplina disciplinaAlvo;

    @BeforeEach
    public void setup(){
        db = new BancoDeDados();

        servicoMatricula = new ServicoMatricula(db, matricula -> true);

        Curso curso = new Curso("Engeharia", 240);
        disciplinaAlvo = new Disciplina("Projeto de Software", curso);
        alunoTeste = new Aluno("Naruto Uzumaki", "naruto", "senha123", "RA01");


        db.cursos.add(curso);
        db.disciplinas.add(disciplinaAlvo);
        db.periodoAberto = true;
    }


    /**
     * Valida a regra de limite máximo: A turma não pode exceder 60 alunos.
     * O sistema deve processa ras primeiras 60 com sucesso e bloquear a 61ª.
     */
    @Test
    public void testDeveBloquearMatriculaAtingirLimiteDeSessentaAlunos(){

        // gera 60 alunos dinamicamente.
        for(int i = 1; i <= LIMITE_MAX_ALUNOS; i++){
            Aluno alunoGenerico = new Aluno("Aluno " + i, "login" + i, "senha", "MAT" + i);
            boolean matriculado = servicoMatricula.efetuarMatricula(alunoGenerico, disciplinaAlvo, TipoMatricula.OBRIGATORIA);
            assertTrue(matriculado, "O aluno " + i + " deveria ter sido matriculado com sucesso.");
        }

        // tenta alocar o aluno sessenta e um
        boolean matriculaExcedente = servicoMatricula.efetuarMatricula(alunoTeste, disciplinaAlvo, TipoMatricula.OBRIGATORIA);

        // a transacao deve ser negada em vez de explodir o sistema
        assertFalse(matriculaExcedente, "A 61ª matrícula deve ser bloqueada pela entidade Disciplina.");
        assertEquals(60, disciplinaAlvo.listarAlunos().size(), "A lista interna de disciplina deve conter exatamente 60 alunos.");
    }

    /**
     * Valida o bloqueio de uma 5ª disciplina obrigatoria para o mesmo aluno
     */
    @Test 
    public void testDeveBloquearMaisDeQuatroMatriculasObrigatorias(){
        Curso c = db.cursos.get(0);
        
        for(int i = 1; i <= QUANT_MAX_DISCIPLINAS; i++){
            Disciplina d = new Disciplina("Disciplina " + i, c);
            boolean matriculado = servicoMatricula.efetuarMatricula(alunoTeste, d, TipoMatricula.OBRIGATORIA);
            assertTrue(matriculado, "O alunod everia conseguir se matricular na disciplina " + i);
        }

        Disciplina disciplinaExtra = new Disciplina("Disciplina 5 (EXTRA)", c);
        Boolean matriculaExcedente = servicoMatricula.efetuarMatricula(alunoTeste, disciplinaExtra, TipoMatricula.OBRIGATORIA);

        assertFalse(matriculaExcedente, "O sistema não deve permiitr a 5ª disciplina obrigatória para o mesmo aluno.");

    }
}
