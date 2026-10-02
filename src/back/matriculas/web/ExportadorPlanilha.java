package matriculas.web;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Gera uma planilha do Excel (.xlsx) com os dados do sistema, uma aba para cada tabela.
 * Um .xlsx é um arquivo ZIP com vários XMLs dentro (formato Office Open XML), então a
 * planilha é montada à mão, sem biblioteca externa. Cabeçalho colorido, linhas
 * zebradas, texto longo com quebra de linha, colunas com largura ajustada, primeira linha fixa e filtros nas colunas.
 * As senhas nunca são exportadas.
 */
public class ExportadorPlanilha {
    public static final String TIPO_CONTEUDO = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private static final String NS_PLANILHA = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
    private static final String NS_RELACOES = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // índices dos estilos definidos em estilos()
    private static final int ESTILO_CABECALHO = 1;
    private static final int ESTILO_TEXTO = 2;
    private static final int ESTILO_TEXTO_ZEBRA = 3;
    private static final int ESTILO_CENTRO = 4;
    private static final int ESTILO_CENTRO_ZEBRA = 5;

    /** Uma aba: nome, títulos das colunas e as linhas (texto ou número em cada célula). */
    private record Tabela(String nome, List<String> cabecalho, List<List<Object>> linhas) {
    }

    /** Monta o arquivo .xlsx completo e devolve seus bytes. */
    public static byte[] gerar(ServicoCadastro servicoCadastro, PeriodoMatricula periodo) throws IOException {
        List<Tabela> tabelas = List.of(
            tabelaCursos(servicoCadastro),
            tabelaProfessores(servicoCadastro),
            tabelaAlunos(),
            tabelaDisciplinas(),
            tabelaMatriculas(),
            tabelaPeriodo(periodo)
        );

        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(saida, StandardCharsets.UTF_8)) {
            adicionar(zip, "[Content_Types].xml", tiposDeConteudo(tabelas.size()));
            adicionar(zip, "_rels/.rels", relacoesDoPacote());
            adicionar(zip, "xl/workbook.xml", pasta(tabelas));
            adicionar(zip, "xl/_rels/workbook.xml.rels", relacoesDaPasta(tabelas.size()));
            adicionar(zip, "xl/styles.xml", estilos());
            for (int i = 0; i < tabelas.size(); i++) {
                adicionar(zip, "xl/worksheets/sheet" + (i + 1) + ".xml", aba(tabelas.get(i), i == 0));
            }
        }
        return saida.toByteArray();
    }

    // ---------- conteúdo de cada aba ----------

    private static Tabela tabelaCursos(ServicoCadastro servicoCadastro) {
        List<List<Object>> linhas = new ArrayList<>();
        for (Curso curso : servicoCadastro.listarCursos()) {
            List<String> nomes = new ArrayList<>();
            for (Disciplina disciplina : curso.getDisciplinas()) {
                nomes.add(disciplina.getNome());
            }
            linhas.add(List.of(curso.getNome(), curso.getNumeroCreditos(), String.join(", ", nomes)));
        }
        return new Tabela("Cursos", List.of("Curso", "Créditos", "Disciplinas"), linhas);
    }

    private static Tabela tabelaProfessores(ServicoCadastro servicoCadastro) {
        List<List<Object>> linhas = new ArrayList<>();
        for (Professor professor : servicoCadastro.listarProfessores()) {
            linhas.add(List.of(professor.getNome(), professor.getLogin()));
        }
        return new Tabela("Professores", List.of("Professor", "Login"), linhas);
    }

    private static Tabela tabelaAlunos() {
        List<List<Object>> linhas = new ArrayList<>();
        for (Aluno aluno : Aluno.listarTodos()) {
            linhas.add(List.of(aluno.getNome(), aluno.getNumeroMatricula(), aluno.getLogin()));
        }
        return new Tabela("Alunos", List.of("Aluno", "Matrícula", "Login"), linhas);
    }

    private static Tabela tabelaDisciplinas() {
        List<List<Object>> linhas = new ArrayList<>();
        for (Disciplina disciplina : Disciplina.listarTodos()) {
            linhas.add(List.of(
                disciplina.getNome(),
                rotulo(disciplina.getStatus()),
                disciplina.getCurso() != null ? disciplina.getCurso().getNome() : "",
                disciplina.getProfessor() != null ? disciplina.getProfessor().getNome() : "",
                disciplina.quantidadeMatriculados(),
                disciplina.vagasRestantes()));
        }
        return new Tabela("Disciplinas",
            List.of("Disciplina", "Status", "Curso", "Professor", "Matriculados", "Vagas restantes"), linhas);
    }

    private static Tabela tabelaMatriculas() {
        List<List<Object>> linhas = new ArrayList<>();
        for (Matricula matricula : Matricula.listarTodos()) {
            linhas.add(List.of(
                data(matricula.getDataMatricula()),
                rotulo(matricula.getTipo()),
                rotulo(matricula.getStatus()),
                matricula.getAluno().getNumeroMatricula(),
                matricula.getAluno().getNome(),
                matricula.getDisciplina().getNome()));
        }
        return new Tabela("Matrículas",
            List.of("Data", "Tipo", "Situação", "Matrícula do aluno", "Aluno", "Disciplina"), linhas);
    }

    private static Tabela tabelaPeriodo(PeriodoMatricula periodo) {
        List<List<Object>> linhas = new ArrayList<>();
        if (periodo != null) {
            List<String> nomes = new ArrayList<>();
            for (Disciplina disciplina : periodo.getDisciplinas()) {
                nomes.add(disciplina.getNome());
            }
            linhas.add(List.of(
                periodo.isAberto() ? "Aberto" : "Fechado",
                data(periodo.getDataInicio()),
                data(periodo.getDataFim()),
                String.join(", ", nomes)));
        }
        return new Tabela("Período", List.of("Situação", "Início", "Término", "Disciplinas ofertadas"), linhas);
    }

    // ---------- textos amigáveis ----------

    private static String rotulo(StatusDisciplina status) {
        switch (status) {
            case ATIVA:
                return "Ativa";
            case CANCELADA:
                return "Cancelada";
            default:
                return "Em aberto";
        }
    }

    private static String rotulo(StatusMatricula status) {
        return status == StatusMatricula.ATIVA ? "Ativa" : "Cancelada";
    }

    private static String rotulo(TipoMatricula tipo) {
        return tipo == TipoMatricula.OBRIGATORIA ? "Obrigatória" : "Optativa";
    }

    private static String data(LocalDate data) {
        return data != null ? data.format(FORMATO_DATA) : "";
    }

    // ---------- partes do arquivo .xlsx ----------

    private static void adicionar(ZipOutputStream zip, String nome, String conteudo) throws IOException {
        zip.putNextEntry(new ZipEntry(nome));
        zip.write(conteudo.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String tiposDeConteudo(int quantidadeAbas) {
        StringBuilder xml = new StringBuilder(cabecalhoXml());
        xml.append("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">");
        xml.append("<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>");
        xml.append("<Default Extension=\"xml\" ContentType=\"application/xml\"/>");
        xml.append("<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>");
        xml.append("<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>");
        for (int i = 1; i <= quantidadeAbas; i++) {
            xml.append("<Override PartName=\"/xl/worksheets/sheet").append(i)
               .append(".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>");
        }
        return xml.append("</Types>").toString();
    }

    private static String relacoesDoPacote() {
        return cabecalhoXml()
            + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
            + "<Relationship Id=\"rId1\" Type=\"" + NS_RELACOES + "/officeDocument\" Target=\"xl/workbook.xml\"/>"
            + "</Relationships>";
    }

    /** O workbook.xml lista as abas na ordem em que aparecem no Excel. */
    private static String pasta(List<Tabela> tabelas) {
        StringBuilder xml = new StringBuilder(cabecalhoXml());
        xml.append("<workbook xmlns=\"").append(NS_PLANILHA).append("\" xmlns:r=\"").append(NS_RELACOES).append("\"><sheets>");
        for (int i = 0; i < tabelas.size(); i++) {
            xml.append("<sheet name=\"").append(escapar(tabelas.get(i).nome()))
               .append("\" sheetId=\"").append(i + 1).append("\" r:id=\"rId").append(i + 1).append("\"/>");
        }
        return xml.append("</sheets></workbook>").toString();
    }

    private static String relacoesDaPasta(int quantidadeAbas) {
        StringBuilder xml = new StringBuilder(cabecalhoXml());
        xml.append("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">");
        for (int i = 1; i <= quantidadeAbas; i++) {
            xml.append("<Relationship Id=\"rId").append(i).append("\" Type=\"").append(NS_RELACOES)
               .append("/worksheet\" Target=\"worksheets/sheet").append(i).append(".xml\"/>");
        }
        xml.append("<Relationship Id=\"rId").append(quantidadeAbas + 1).append("\" Type=\"").append(NS_RELACOES)
           .append("/styles\" Target=\"styles.xml\"/>");
        return xml.append("</Relationships>").toString();
    }

    /**
     * Estilos usados pelas células: fontes, cores de fundo, bordas e as combinações
     * (cellXfs) referenciadas pelos índices ESTILO_*. As duas primeiras cores de fundo
     * (nenhuma e gray125) são exigidas pelo formato.
     */
    private static String estilos() {
        String borda = "<border><left style=\"thin\"><color rgb=\"FFBFBFBF\"/></left><right style=\"thin\"><color rgb=\"FFBFBFBF\"/></right>"
            + "<top style=\"thin\"><color rgb=\"FFBFBFBF\"/></top><bottom style=\"thin\"><color rgb=\"FFBFBFBF\"/></bottom><diagonal/></border>";
        return cabecalhoXml()
            + "<styleSheet xmlns=\"" + NS_PLANILHA + "\">"
            + "<fonts count=\"2\">"
            + "<font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
            + "<font><b/><sz val=\"11\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/></font>"
            + "</fonts>"
            + "<fills count=\"4\">"
            + "<fill><patternFill patternType=\"none\"/></fill>"
            + "<fill><patternFill patternType=\"gray125\"/></fill>"
            + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF1B263B\"/><bgColor indexed=\"64\"/></patternFill></fill>"
            + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFF2F4F7\"/><bgColor indexed=\"64\"/></patternFill></fill>"
            + "</fills>"
            + "<borders count=\"2\"><border><left/><right/><top/><bottom/><diagonal/></border>" + borda + "</borders>"
            + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
            + "<cellXfs count=\"6\">"
            + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
            + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\">"
            + "<alignment horizontal=\"center\" vertical=\"center\"/></xf>"
            + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyAlignment=\"1\">"
            + "<alignment vertical=\"center\" wrapText=\"1\"/></xf>"
            + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"3\" borderId=\"1\" xfId=\"0\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\">"
            + "<alignment vertical=\"center\" wrapText=\"1\"/></xf>"
            + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyAlignment=\"1\">"
            + "<alignment horizontal=\"center\" vertical=\"center\"/></xf>"
            + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"3\" borderId=\"1\" xfId=\"0\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\">"
            + "<alignment horizontal=\"center\" vertical=\"center\"/></xf>"
            + "</cellXfs>"
            + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
            + "</styleSheet>";
    }

    /**
     * XML de uma aba. A ordem dos elementos é exigida pelo formato: dimensão, visualização
     * (linha fixa), larguras das colunas, dados, filtro e configuração de impressão (paisagem, ajustada à largura da página).
     */
    private static String aba(Tabela tabela, boolean selecionada) {
        int colunas = tabela.cabecalho().size();
        int ultimaLinha = tabela.linhas().size() + 1;
        String intervalo = "A1:" + nomeColuna(colunas - 1) + ultimaLinha;

        StringBuilder xml = new StringBuilder(cabecalhoXml());
        xml.append("<worksheet xmlns=\"").append(NS_PLANILHA).append("\">");
        xml.append("<sheetPr><pageSetUpPr fitToPage=\"1\"/></sheetPr>");
        xml.append("<dimension ref=\"").append(intervalo).append("\"/>");
        xml.append("<sheetViews><sheetView workbookViewId=\"0\"").append(selecionada ? " tabSelected=\"1\"" : "").append(">");
        xml.append("<pane ySplit=\"1\" topLeftCell=\"A2\" activePane=\"bottomLeft\" state=\"frozen\"/>");
        xml.append("<selection pane=\"bottomLeft\"/></sheetView></sheetViews>");
        xml.append("<sheetFormatPr defaultRowHeight=\"15\"/>");

        xml.append("<cols>");
        for (int c = 0; c < colunas; c++) {
            xml.append("<col min=\"").append(c + 1).append("\" max=\"").append(c + 1)
               .append("\" width=\"").append(largura(tabela, c)).append("\" customWidth=\"1\"/>");
        }
        xml.append("</cols>");

        xml.append("<sheetData>");
        xml.append("<row r=\"1\" ht=\"24\" customHeight=\"1\">");
        for (int c = 0; c < colunas; c++) {
            xml.append(celulaTexto(c, 1, tabela.cabecalho().get(c), ESTILO_CABECALHO));
        }
        xml.append("</row>");
        for (int l = 0; l < tabela.linhas().size(); l++) {
            int numeroLinha = l + 2;
            boolean zebra = l % 2 == 1;
            xml.append("<row r=\"").append(numeroLinha).append("\">");
            List<Object> linha = tabela.linhas().get(l);
            for (int c = 0; c < colunas; c++) {
                xml.append(celula(c, numeroLinha, linha.get(c), zebra));
            }
            xml.append("</row>");
        }
        xml.append("</sheetData>");

        xml.append("<autoFilter ref=\"").append(intervalo).append("\"/>");
        xml.append("<pageMargins left=\"0.7\" right=\"0.7\" top=\"0.75\" bottom=\"0.75\" header=\"0.3\" footer=\"0.3\"/>");
        xml.append("<pageSetup orientation=\"landscape\" fitToWidth=\"1\" fitToHeight=\"0\"/>");
        return xml.append("</worksheet>").toString();
    }

    /**
     * Texto vai como "inlineStr" (texto dentro da própria célula), o que dispensa a tabela
     * de textos compartilhados e impede que o Excel trate "=..." como fórmula. Números ficam
     * centralizados, assim como colunas curtas de código.
     */
    private static String celula(int coluna, int linha, Object valor, boolean zebra) {
        if (valor instanceof Number) {
            return "<c r=\"" + nomeColuna(coluna) + linha + "\" s=\"" + (zebra ? ESTILO_CENTRO_ZEBRA : ESTILO_CENTRO)
                + "\"><v>" + valor + "</v></c>";
        }
        return celulaTexto(coluna, linha, String.valueOf(valor), zebra ? ESTILO_TEXTO_ZEBRA : ESTILO_TEXTO);
    }

    private static String celulaTexto(int coluna, int linha, String texto, int estilo) {
        return "<c r=\"" + nomeColuna(coluna) + linha + "\" s=\"" + estilo + "\" t=\"inlineStr\"><is><t xml:space=\"preserve\">"
            + escapar(texto) + "</t></is></c>";
    }

    /** Largura da coluna em caracteres: a do maior texto (com folga), entre 12 e 60. */
    private static int largura(Tabela tabela, int coluna) {
        int maior = tabela.cabecalho().get(coluna).length();
        for (List<Object> linha : tabela.linhas()) {
            maior = Math.max(maior, String.valueOf(linha.get(coluna)).length());
        }
        return Math.max(12, Math.min(60, maior + 4));
    }

    /** Converte o índice da coluna (0, 1, 2...) na letra do Excel (A, B, C... Z, AA). */
    private static String nomeColuna(int indice) {
        StringBuilder nome = new StringBuilder();
        for (int i = indice; i >= 0; i = i / 26 - 1) {
            nome.insert(0, (char) ('A' + i % 26));
        }
        return nome.toString();
    }

    /** Escapa os caracteres especiais do XML e descarta caracteres de controle, que tornariam o arquivo inválido. */
    static String escapar(String texto) {
        StringBuilder resultado = new StringBuilder();
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            switch (c) {
                case '&':
                    resultado.append("&amp;");
                    break;
                case '<':
                    resultado.append("&lt;");
                    break;
                case '>':
                    resultado.append("&gt;");
                    break;
                case '"':
                    resultado.append("&quot;");
                    break;
                default:
                    if (c >= 32 || c == '\t') {
                        resultado.append(c);
                    }
            }
        }
        return resultado.toString();
    }

    private static String cabecalhoXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
    }
}
