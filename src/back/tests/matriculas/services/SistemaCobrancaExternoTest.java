package matriculas.services;

import matriculas.Verificador;
import matriculas.enums.StatusMatricula;
import matriculas.enums.TipoMatricula;
import matriculas.models.Aluno;
import matriculas.models.Disciplina;
import matriculas.models.Matricula;

import java.time.LocalDate;

public class SistemaCobrancaExternoTest {
    public static void executar() {
        Aluno.limparRegistro();
        Disciplina.limparRegistro();
        Matricula.limparRegistro();

        SistemaCobrancaExterno cobranca = new SistemaCobrancaExterno();

        Verificador.assertFalse(cobranca.notificar(null), "Notificação com matrícula null deve retornar false.");

        Aluno aluno = new Aluno("Aluno Teste", "001", "ateste", "senha123");
        Disciplina disciplina = new Disciplina("Disciplina Teste");
        Matricula m = new Matricula(LocalDate.now(), TipoMatricula.OBRIGATORIA, StatusMatricula.ATIVA, aluno, disciplina);

        Verificador.assertTrue(cobranca.notificar(m), "Notificação com matrícula válida deve retornar true.");
    }
}
