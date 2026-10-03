package matriculas;

import matriculas.infrastructure.BancoDeDados;
import matriculas.infrastructure.CsvDataLoader;
import matriculas.infrastructure.SistemaCobrancaExterno;
import matriculas.app.services.ServicoAutenticacao;
import matriculas.app.services.ServicoMatricula;
import matriculas.app.services.ServicoAcademico;
import matriculas.presentation.InterfaceConsole;

public class Main {
    public static void main(String[] args) {
        BancoDeDados db = new BancoDeDados();
        
        try {
            System.out.println("Carregando dados dos arquivos CSV...");
            CsvDataLoader.carregarDados(db, "dados");
        } catch (Exception e) {
            System.err.println("Erro ao carregar CSV: " + e.getMessage());
        }

        ServicoAutenticacao auth = new ServicoAutenticacao(db);
        ServicoMatricula matricula = new ServicoMatricula(db, new SistemaCobrancaExterno());
        ServicoAcademico academico = new ServicoAcademico(db);

        InterfaceConsole ui = new InterfaceConsole(auth, matricula, academico, db);
        ui.iniciar();
    }
}