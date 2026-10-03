package matriculas;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Verificador {
    private static int assercoesExecutadas = 0;
    private static final List<String> falhas = new ArrayList<>();

    public static void assertTrue(boolean condicao, String mensagem) {
        assercoesExecutadas++;
        if (!condicao) {
            registrarFalha(mensagem + " [Esperava true, mas obteve false]");
        }
    }

    public static void assertFalse(boolean condicao, String mensagem) {
        assercoesExecutadas++;
        if (condicao) {
            registrarFalha(mensagem + " [Esperava false, mas obteve true]");
        }
    }

    public static void assertEquals(Object esperado, Object obtido, String mensagem) {
        assercoesExecutadas++;
        if (!Objects.equals(esperado, obtido)) {
            registrarFalha(mensagem + " [Esperado: " + esperado + ", mas obteve: " + obtido + "]");
        }
    }

    public static void assertLanca(Class<? extends Throwable> tipoEsperado, Runnable acao, String mensagem) {
        assercoesExecutadas++;
        try {
            acao.run();
            registrarFalha(mensagem + " [Esperava exceção " + tipoEsperado.getSimpleName() + ", mas nenhuma exceção foi lançada]");
        } catch (Throwable t) {
            if (!tipoEsperado.isInstance(t)) {
                registrarFalha(mensagem + " [Esperava exceção " + tipoEsperado.getSimpleName() + ", mas foi lançada " + t.getClass().getSimpleName() + ": " + t.getMessage() + "]");
            }
        }
    }

    private static void registrarFalha(String detalhe) {
        falhas.add(detalhe);
        System.err.println("  [FALHA] " + detalhe);
    }

    public static int getAssercoesExecutadas() {
        return assercoesExecutadas;
    }

    public static List<String> getFalhas() {
        return falhas;
    }

    public static boolean temFalhas() {
        return !falhas.isEmpty();
    }

    public static void resetar() {
        assercoesExecutadas = 0;
        falhas.clear();
    }
}
