

import application.services.ServicoAcademico;
import application.services.ServicoAutenticacao;
import application.services.ServicoMatricula;
import infrastructure.BancoDeDados;
import infrastructure.CsvDataLoader;
import infrastructure.SistemaCobrancaExterno;
import presentation.InterfaceConsole;

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