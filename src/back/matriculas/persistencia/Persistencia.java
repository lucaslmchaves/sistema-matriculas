package matriculas.persistencia;

import matriculas.enums.StatusDisciplina;
import matriculas.enums.StatusMatricula;
import matriculas.enums.TipoMatricula;
import matriculas.models.Aluno;
import matriculas.models.Curso;
import matriculas.models.Disciplina;
import matriculas.models.Matricula;
import matriculas.models.PeriodoMatricula;
import matriculas.models.Professor;
import matriculas.services.ServicoCadastro;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Grava e lê os dados do sistema em arquivos de texto (um por tipo de dado),
 * com um registro por linha e os campos separados por ";".
 * Os arquivos são lidos na ordem cursos, professores, alunos, disciplinas,
 * matrículas e período, porque cada um depende dos anteriores.
 */
public class Persistencia {
    private static final String SEPARADOR = ";";

    private static final String ARQUIVO_CURSOS = "cursos.txt";
    private static final String ARQUIVO_PROFESSORES = "professores.txt";
    private static final String ARQUIVO_ALUNOS = "alunos.txt";
    private static final String ARQUIVO_DISCIPLINAS = "disciplinas.txt";
    private static final String ARQUIVO_MATRICULAS = "matriculas.txt";
    private static final String ARQUIVO_PERIODO = "periodo.txt";

    private final Path diretorio;

    /** Usa a pasta "dados" do diretório onde o programa foi executado. */
    public Persistencia() {
        this(Paths.get("dados"));
    }

    public Persistencia(Path diretorio) {
        this.diretorio = diretorio;
    }

    /** Linha lida de um arquivo, já separada em campos, com a posição para mensagens de erro. */
    private record Linha(String[] campos, String local) {
        String campo(int indice) {
            return campos[indice].trim();
        }

        IllegalStateException malformada(Throwable causa) {
            return new IllegalStateException("Linha malformada " + local, causa);
        }

        IllegalStateException inconsistente(String descricao) {
            return new IllegalStateException(descricao + " " + local);
        }
    }

    // ---------- salvar ----------

    /** Grava todos os dados nos arquivos. Cada arquivo é substituído por inteiro. Devolve a pasta onde gravou. */
    public Path salvar(ServicoCadastro servicoCadastro, PeriodoMatricula periodo) {
        try {
            Files.createDirectories(diretorio);
            salvarArquivoAtomico(ARQUIVO_CURSOS, linhasCursos(servicoCadastro));
            salvarArquivoAtomico(ARQUIVO_PROFESSORES, linhasProfessores(servicoCadastro));
            salvarArquivoAtomico(ARQUIVO_ALUNOS, linhasAlunos());
            salvarArquivoAtomico(ARQUIVO_DISCIPLINAS, linhasDisciplinas());
            salvarArquivoAtomico(ARQUIVO_MATRICULAS, linhasMatriculas());
            salvarArquivoAtomico(ARQUIVO_PERIODO, linhasPeriodo(periodo));
            return diretorio;
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao persistir dados no diretório: " + diretorio, e);
        }
    }

    private List<String> linhasCursos(ServicoCadastro servicoCadastro) {
        List<String> linhas = new ArrayList<>();
        for (Curso c : servicoCadastro.listarCursos()) {
            linhas.add(String.join(SEPARADOR, c.getNome(), String.valueOf(c.getNumeroCreditos())));
        }
        return linhas;
    }

    private List<String> linhasProfessores(ServicoCadastro servicoCadastro) {
        List<String> linhas = new ArrayList<>();
        for (Professor p : servicoCadastro.listarProfessores()) {
            linhas.add(String.join(SEPARADOR, p.getNome(), p.getLogin(), p.getSenhaCriptografada()));
        }
        return linhas;
    }

    private List<String> linhasAlunos() {
        List<String> linhas = new ArrayList<>();
        for (Aluno a : Aluno.listarTodos()) {
            linhas.add(String.join(SEPARADOR, a.getNome(), a.getNumeroMatricula(), a.getLogin(), a.getSenhaCriptografada()));
        }
        return linhas;
    }

    private List<String> linhasDisciplinas() {
        List<String> linhas = new ArrayList<>();
        for (Disciplina d : Disciplina.listarTodos()) {
            String nomeCurso = (d.getCurso() != null) ? d.getCurso().getNome() : "";
            String loginProfessor = (d.getProfessor() != null) ? d.getProfessor().getLogin() : "";
            linhas.add(String.join(SEPARADOR, d.getNome(), d.getStatus().name(), nomeCurso, loginProfessor));
        }
        return linhas;
    }

    private List<String> linhasMatriculas() {
        List<String> linhas = new ArrayList<>();
        for (Matricula m : Matricula.listarTodos()) {
            linhas.add(String.join(SEPARADOR,
                m.getDataMatricula().toString(),
                m.getTipo().name(),
                m.getStatus().name(),
                m.getAluno().getNumeroMatricula(),
                m.getDisciplina().getNome()));
        }
        return linhas;
    }

    /** O período ocupa uma única linha: início;fim;aberto;disciplina1;disciplina2... */
    private List<String> linhasPeriodo(PeriodoMatricula periodo) {
        List<String> linhas = new ArrayList<>();
        if (periodo == null) {
            return linhas;
        }
        List<String> campos = new ArrayList<>();
        campos.add(periodo.getDataInicio() != null ? periodo.getDataInicio().toString() : "");
        campos.add(periodo.getDataFim() != null ? periodo.getDataFim().toString() : "");
        campos.add(String.valueOf(periodo.isAberto()));
        for (Disciplina d : periodo.getDisciplinas()) {
            campos.add(d.getNome());
        }
        linhas.add(String.join(SEPARADOR, campos));
        return linhas;
    }

    /**
     * Escreve em um arquivo temporário e só depois troca pelo definitivo, para que uma
     * falha no meio da escrita não deixe o arquivo de dados pela metade. Devolve o caminho do arquivo gravado.
     */
    private Path salvarArquivoAtomico(String nomeArquivo, List<String> linhas) throws IOException {
        Path destino = diretorio.resolve(nomeArquivo);
        Path temporario = Files.createTempFile(diretorio, nomeArquivo, ".tmp");
        try {
            Files.write(temporario, linhas, StandardCharsets.UTF_8);
            try {
                Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporario);
        }
        return destino;
    }

    // ---------- carregar ----------

    /**
     * Lê os arquivos e reconstrói os dados em memória. Devolve o período de matrículas
     * salvo (ou um período novo, se não houver arquivo).
     */
    public PeriodoMatricula carregar(ServicoCadastro servicoCadastro) {
        Aluno.limparRegistro();
        Disciplina.limparRegistro();
        Matricula.limparRegistro();
        servicoCadastro.limpar();

        if (!Files.exists(diretorio)) {
            return new PeriodoMatricula();
        }

        try {
            carregarCursos(servicoCadastro);
            carregarProfessores(servicoCadastro);
            carregarAlunos();
            carregarDisciplinas(servicoCadastro);
            carregarMatriculas(servicoCadastro);
            return carregarPeriodo(servicoCadastro);
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao carregar dados do diretório: " + diretorio, e);
        }
    }

    // Cada carregarXxx devolve quantos registros leu do arquivo.

    private int carregarCursos(ServicoCadastro servicoCadastro) throws IOException {
        int carregados = 0;
        for (Linha linha : lerLinhas(ARQUIVO_CURSOS, 2)) {
            try {
                int creditos = Integer.parseInt(linha.campo(1));
                servicoCadastro.restaurarCurso(new Curso(linha.campo(0), creditos));
                carregados++;
            } catch (NumberFormatException e) {
                throw linha.malformada(e);
            }
        }
        return carregados;
    }

    private int carregarProfessores(ServicoCadastro servicoCadastro) throws IOException {
        int carregados = 0;
        for (Linha linha : lerLinhas(ARQUIVO_PROFESSORES, 3)) {
            servicoCadastro.restaurarProfessor(new Professor(linha.campo(0), linha.campo(1), linha.campo(2), true));
            carregados++;
        }
        return carregados;
    }

    private int carregarAlunos() throws IOException {
        int carregados = 0;
        for (Linha linha : lerLinhas(ARQUIVO_ALUNOS, 4)) {
            Aluno.registrar(new Aluno(linha.campo(0), linha.campo(1), linha.campo(2), linha.campo(3), true));
            carregados++;
        }
        return carregados;
    }

    private int carregarDisciplinas(ServicoCadastro servicoCadastro) throws IOException {
        int carregados = 0;
        for (Linha linha : lerLinhas(ARQUIVO_DISCIPLINAS, 4)) {
            StatusDisciplina status;
            try {
                status = StatusDisciplina.valueOf(linha.campo(1));
            } catch (IllegalArgumentException e) {
                throw linha.malformada(e);
            }

            String nomeCurso = linha.campo(2);
            Curso curso = servicoCadastro.buscarCursoPorNome(nomeCurso);
            if (curso == null && !nomeCurso.isEmpty()) {
                throw linha.inconsistente("Curso não encontrado");
            }

            Disciplina disciplina = new Disciplina(linha.campo(0), curso, status);
            Professor professor = servicoCadastro.buscarProfessorPorLogin(linha.campo(3));
            if (professor != null) {
                disciplina.atribuirProfessor(professor);
            }
            if (curso != null) {
                curso.adicionarDisciplina(disciplina);
            }
            Disciplina.registrar(disciplina);
            carregados++;
        }
        return carregados;
    }

    private int carregarMatriculas(ServicoCadastro servicoCadastro) throws IOException {
        int carregados = 0;
        for (Linha linha : lerLinhas(ARQUIVO_MATRICULAS, 5)) {
            LocalDate data;
            TipoMatricula tipo;
            StatusMatricula status;
            try {
                data = LocalDate.parse(linha.campo(0));
                tipo = TipoMatricula.valueOf(linha.campo(1));
                status = StatusMatricula.valueOf(linha.campo(2));
            } catch (DateTimeParseException | IllegalArgumentException e) {
                throw linha.malformada(e);
            }

            Aluno aluno = servicoCadastro.buscarAlunoPorMatricula(linha.campo(3));
            if (aluno == null) {
                throw linha.inconsistente("Aluno não encontrado");
            }
            Disciplina disciplina = servicoCadastro.buscarDisciplinaPorNome(linha.campo(4));
            if (disciplina == null) {
                throw linha.inconsistente("Disciplina não encontrada");
            }

            Matricula matricula = new Matricula(data, tipo, status, aluno, disciplina);
            Matricula.registrar(matricula);
            aluno.adicionarMatricula(matricula);
            carregados++;
        }
        return carregados;
    }

    private PeriodoMatricula carregarPeriodo(ServicoCadastro servicoCadastro) throws IOException {
        PeriodoMatricula periodo = new PeriodoMatricula();
        List<Linha> linhas = lerLinhas(ARQUIVO_PERIODO, 3);
        if (linhas.isEmpty()) {
            return periodo;
        }

        Linha linha = linhas.get(0);
        try {
            LocalDate dataInicio = linha.campo(0).isEmpty() ? null : LocalDate.parse(linha.campo(0));
            LocalDate dataFim = linha.campo(1).isEmpty() ? null : LocalDate.parse(linha.campo(1));
            boolean aberto = Boolean.parseBoolean(linha.campo(2));

            List<Disciplina> disciplinas = new ArrayList<>();
            for (int i = 3; i < linha.campos().length; i++) {
                Disciplina disciplina = servicoCadastro.buscarDisciplinaPorNome(linha.campo(i));
                if (disciplina != null) {
                    disciplinas.add(disciplina);
                }
            }
            periodo.restaurar(dataInicio, dataFim, aberto, disciplinas);
        } catch (DateTimeParseException e) {
            throw linha.malformada(e);
        }
        return periodo;
    }

    /**
     * Lê um arquivo e devolve suas linhas separadas em campos, ignorando linhas em branco.
     * Se o arquivo não existir devolve lista vazia; se uma linha tiver menos campos que o
     * esperado, lança IllegalStateException indicando o arquivo e o número da linha.
     */
    private List<Linha> lerLinhas(String nomeArquivo, int camposMinimos) throws IOException {
        Path arquivo = diretorio.resolve(nomeArquivo);
        List<Linha> resultado = new ArrayList<>();
        if (!Files.exists(arquivo)) {
            return resultado;
        }

        List<String> textos = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        for (int i = 0; i < textos.size(); i++) {
            String texto = textos.get(i);
            if (texto.trim().isEmpty()) {
                continue;
            }
            Linha linha = new Linha(texto.split(SEPARADOR, -1), "no arquivo " + nomeArquivo + ", linha " + (i + 1));
            if (linha.campos().length < camposMinimos) {
                throw linha.malformada(null);
            }
            resultado.add(linha);
        }
        return resultado;
    }
}
