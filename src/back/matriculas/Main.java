package matriculas;

import matriculas.cli.MenuPrincipal;
import matriculas.interfaces.NotificadorCobranca;
import matriculas.models.PeriodoMatricula;
import matriculas.models.Secretaria;
import matriculas.persistencia.Persistencia;
import matriculas.services.ServicoCadastro;
import matriculas.services.ServicoMatricula;
import matriculas.services.SistemaCobrancaExterno;

/**
 * Ponto de entrada da versão de console. Monta as peças do sistema (carrega os dados
 * salvos, cria os serviços e a secretaria padrão) e entrega ao menu principal.
 */
public class Main {
    public static void main(String[] args) {
        Persistencia persistencia = new Persistencia();
        ServicoCadastro servicoCadastro = new ServicoCadastro();
        PeriodoMatricula periodo = persistencia.carregar(servicoCadastro);
        NotificadorCobranca notificador = new SistemaCobrancaExterno();
        ServicoMatricula servicoMatricula = new ServicoMatricula(notificador, periodo);
        Secretaria secretaria = new Secretaria("Secretaria Acadêmica", "secretaria", "admin123");

        MenuPrincipal menuPrincipal = new MenuPrincipal(
            servicoCadastro,
            servicoMatricula,
            periodo,
            persistencia,
            secretaria
        );
        menuPrincipal.iniciar();
    }
}
