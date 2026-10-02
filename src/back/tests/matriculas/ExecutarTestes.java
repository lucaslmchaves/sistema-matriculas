package matriculas;

import matriculas.models.DisciplinaTest;
import matriculas.models.MatriculaTest;
import matriculas.models.PeriodoMatriculaTest;
import matriculas.models.UsuarioTest;
import matriculas.persistencia.PersistenciaTest;
import matriculas.services.ServicoCadastroTest;
import matriculas.services.ServicoMatriculaTest;
import matriculas.services.SistemaCobrancaExternoTest;
import matriculas.web.DadosDemonstracaoMatriculasTest;
import matriculas.web.DadosDemonstracaoTest;
import matriculas.web.ExportadorPlanilhaTest;
import matriculas.web.JsonTest;

public class ExecutarTestes {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  INICIANDO SUÍTE DE TESTES (JAVA PURO)");
        System.out.println("==================================================");

        Verificador.resetar();

        executarTeste("UsuarioTest", UsuarioTest::executar);
        executarTeste("DisciplinaTest", DisciplinaTest::executar);
        executarTeste("MatriculaTest", MatriculaTest::executar);
        executarTeste("PeriodoMatriculaTest", PeriodoMatriculaTest::executar);
        executarTeste("ServicoCadastroTest", ServicoCadastroTest::executar);
        executarTeste("ServicoMatriculaTest", ServicoMatriculaTest::executar);
        executarTeste("SistemaCobrancaExternoTest", SistemaCobrancaExternoTest::executar);
        executarTeste("PersistenciaTest", PersistenciaTest::executar);
        executarTeste("JsonTest", JsonTest::executar);
        executarTeste("DadosDemonstracaoTest", DadosDemonstracaoTest::executar);
        executarTeste("DadosDemonstracaoMatriculasTest", DadosDemonstracaoMatriculasTest::executar);
        executarTeste("ExportadorPlanilhaTest", ExportadorPlanilhaTest::executar);

        System.out.println("==================================================");
        System.out.println("Total de asserções executadas: " + Verificador.getAssercoesExecutadas());
        if (Verificador.temFalhas()) {
            System.err.println("FALHAS ENCONTRADAS (" + Verificador.getFalhas().size() + "):");
            for (String falha : Verificador.getFalhas()) {
                System.err.println("- " + falha);
            }
            System.exit(1);
        } else {
            System.out.println("TODOS OS TESTES PASSARAM COM SUCESSO!");
        }
    }

    private static void executarTeste(String nome, Runnable teste) {
        try {
            System.out.print("[TESTE] " + nome + "... ");
            teste.run();
            System.out.println("OK");
        } catch (Throwable t) {
            System.out.println("ERRO INESPERADO");
            t.printStackTrace();
            Verificador.assertTrue(false, "Exceção inesperada durante " + nome + ": " + t.getMessage());
        }
    }
}
