package infrastructure;

import domain.entities.*;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Utilitário responsável por popular o BancoDeDados inicial lendo arquivos CSV.
 * Mapeia as colunas exatas dos arquivos de texto para os construtores das entidades de domínio.
 */
public class CsvDataLoader {
    

    /**
     * Método utilitário que isola a lógica I/O (leitura, descarte de cabeçalho e fechamento de arquivo).
     * 
     * 
     * @param dirPath O caminho da pasta onde o arquivo está localizado.
     * @param fileName O nome do arquivo a ser lido ( ex: "cursos.csv" ).
     * @param linhaProcessador Função (Consumer) que recebe um array de colunas geradas pelo
     * split(",") e o converte na entidade correspondente.
     */
    private static void lerCsv(String dirPath, String fileName, Consumer<String[]> linhaProcessador){
        Path path = Path.of(dirPath, fileName);
        if(!Files.exists(path)) return;

        try (BufferedReader br = Files.newBufferedReader(path)) {
            br.readLine();
            String line;
            while((line = br.readLine()) != null){
                linhaProcessador.accept(line.split(","));
            }
        }catch(Exception e){
            System.err.println("Erro ao processar " + fileName + ": " + e.getMessage());
        }
    }

    /**
     * Lê os arquivos .csv do diretório fornecido e converte suas linhas em objetos.
     * 
     * @param db A instância do banco de dados em memória.
     * @param dirPath O caminho da pasta onde os arquivos estão (ex: "dados").
     * @return true se a leitura ocorrer sem erros, false em caso de falha de I/O.
     */
    public static boolean carregarDados(BancoDeDados db, String dirPath) {

        if (db == null || dirPath == null || dirPath.trim().isEmpty()) 
            return false;
        
        lerCsv(dirPath, "cursos.csv", cols -> 
            db.cursos.add(new Curso(cols[1],Integer.parseInt(cols[2])))
        );

        lerCsv(dirPath,"alunos.csv", cols -> {
            Aluno a = new Aluno(cols[0], cols[2], cols[3], cols[1]);
            db.alunos.add(a);
            db.usuarios.add(a);
        });

        lerCsv(dirPath, "professores.csv", cols -> {
            Professor p = new Professor(cols[0], cols[1], cols[2]);
            db.professores.add(p);
            db.usuarios.add(p);
        });
        
        lerCsv(dirPath, "disciplinas.csv", cols -> {
            int indexCurso = Integer.parseInt(cols[1]) - 1;
            if(indexCurso >= 0 && indexCurso < db.cursos.size()){
                Curso c = db.cursos.get(indexCurso);
                Disciplina d = new Disciplina(cols[0],c);

                if(cols.length > 2 && !cols[2].trim().isEmpty()){
                    db.professores.stream()
                        .filter(p -> p.getLogin().equals(cols[2]))
                        .findFirst()
                        .ifPresent(d::setProfessor);
                }
                db.disciplinas.add(d);
                c.adicionarDisciplina(d);
            }
        });
        return true;
    }
}