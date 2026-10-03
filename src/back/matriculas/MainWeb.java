package matriculas;

import matriculas.interfaces.NotificadorCobranca;
import matriculas.models.PeriodoMatricula;
import matriculas.models.Secretaria;
import matriculas.persistencia.Persistencia;
import matriculas.services.ServicoCadastro;
import matriculas.services.ServicoMatricula;
import matriculas.services.SistemaCobrancaExterno;
import matriculas.web.ServidorWeb;

/**
 * Ponto de entrada da API web. Monta as mesmas peças da versão de console, mas
 * em vez do menu inicia um servidor HTTP que a tela do front consome.
 */
public class MainWeb {
    private static final int PORTA_PADRAO = 8080;

    public static void main(String[] args) {
        Persistencia persistencia = new Persistencia();
        ServicoCadastro servicoCadastro = new ServicoCadastro();
        PeriodoMatricula periodo = persistencia.carregar(servicoCadastro);
        NotificadorCobranca notificador = new SistemaCobrancaExterno();
        ServicoMatricula servicoMatricula = new ServicoMatricula(notificador, periodo);
        Secretaria secretaria = new Secretaria("Secretaria Acadêmica", "secretaria", "admin123");

        ServidorWeb servidor = new ServidorWeb(lerPorta(), servicoCadastro, servicoMatricula, periodo, persistencia, secretaria);

        try {
            servidor.iniciar();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Encerrando servidor web...");
                servidor.parar();
            }));
        } catch (Exception e) {
            System.err.println("Falha ao iniciar o servidor web: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /** Lê a porta da variável de ambiente PORT; se não existir ou for inválida, usa 8080. */
    private static int lerPorta() {
        String valor = System.getenv("PORT");
        if (valor == null || valor.trim().isEmpty()) {
            return PORTA_PADRAO;
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            System.err.println("Variável PORT inválida (" + valor + "), usando padrão: " + PORTA_PADRAO);
            return PORTA_PADRAO;
        }
    }
}
