package matriculas.infrastructure;

import matriculas.domain.entities.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class CsvDataLoader {
    public static void carregarDados(BancoDeDados db, String dirPath) throws Exception {
        // Cursos
        Path cursosPath = Path.of(dirPath, "cursos.csv");
        if (Files.exists(cursosPath)) {
            try (BufferedReader br = new BufferedReader(new FileReader(cursosPath.toFile()))) {
                String line = br.readLine();
                while ((line = br.readLine()) != null) {
                    String[] cols = line.split(",");
                    Curso c = new Curso(cols[1], Integer.parseInt(cols[2]));
                    db.cursos.add(c);
                }
            }
        }
        // Alunos
        Path alunosPath = Path.of(dirPath, "alunos.csv");
        if (Files.exists(alunosPath)) {
            try (BufferedReader br = new BufferedReader(new FileReader(alunosPath.toFile()))) {
                String line = br.readLine();
                while ((line = br.readLine()) != null) {
                    String[] cols = line.split(",");
                    Aluno a = new Aluno(cols[0], cols[1], cols[2], cols[3]);
                    db.alunos.add(a);
                    db.usuarios.add(a);
                }
            }
        }
        // Professores
        Path profPath = Path.of(dirPath, "professores.csv");
        if (Files.exists(profPath)) {
            try (BufferedReader br = new BufferedReader(new FileReader(profPath.toFile()))) {
                String line = br.readLine();
                while ((line = br.readLine()) != null) {
                    String[] cols = line.split(",");
                    Professor p = new Professor(cols[0], cols[1], cols[2]);
                    db.professores.add(p);
                    db.usuarios.add(p);
                }
            }
        }
        // Secretaria
        Path secPath = Path.of(dirPath, "secretaria.csv");
        if (Files.exists(secPath)) {
            try (BufferedReader br = new BufferedReader(new FileReader(secPath.toFile()))) {
                String line = br.readLine();
                while ((line = br.readLine()) != null) {
                    String[] cols = line.split(",");
                    Secretaria s = new Secretaria(cols[0], cols[1], cols[2]);
                    db.usuarios.add(s);
                }
            }
        }
        // Disciplinas
        Path discPath = Path.of(dirPath, "disciplinas.csv");
        if (Files.exists(discPath)) {
            try (BufferedReader br = new BufferedReader(new FileReader(discPath.toFile()))) {
                String line = br.readLine();
                while ((line = br.readLine()) != null) {
                    String[] cols = line.split(",");
                    Curso c = db.cursos.get(0); // simplificado
                    Disciplina d = new Disciplina(cols[0], c);
                    db.disciplinas.add(d);
                    c.adicionarDisciplina(d);
                }
            }
        }
    }
}
