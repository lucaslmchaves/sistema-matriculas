package matriculas.web;

import matriculas.Verificador;
import matriculas.enums.TipoMatricula;
import matriculas.models.Aluno;
import matriculas.models.Curso;
import matriculas.models.Disciplina;
import matriculas.models.Matricula;
import matriculas.models.PeriodoMatricula;
import matriculas.models.Professor;
import matriculas.services.ServicoCadastro;
import matriculas.services.ServicoMatricula;
import matriculas.services.SistemaCobrancaExterno;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ExportadorPlanilhaTest {
    public static void executar() {
        Aluno.limparRegistro();
        Disciplina.limparRegistro();
        Matricula.limparRegistro();

        ServicoCadastro servico = new ServicoCadastro();
        PeriodoMatricula periodo = new PeriodoMatricula();

        Curso curso = servico.cadastrarCurso("Engenharia & Software", 240);
        Professor professor = servico.cadastrarProfessor("Prof Carlos", "pcarlos", "segredo123");
        Aluno aluno = servico.cadastrarAluno("=SOMA(A1)", "202611", "ateste", "segredo456");
        Disciplina disciplina = servico.cadastrarDisciplina("Arquitetura <1>", curso);
        disciplina.atribuirProfessor(professor);
        periodo.adicionarDisciplina(disciplina);
        periodo.abrir();
        new ServicoMatricula(new SistemaCobrancaExterno(), periodo).efetuarMatricula(aluno, disciplina, TipoMatricula.OBRIGATORIA);

        try {
            Map<String, String> partes = lerPartes(ExportadorPlanilha.gerar(servico, periodo));

            // estrutura do pacote
            Verificador.assertTrue(partes.containsKey("[Content_Types].xml"), "Planilha deve ter [Content_Types].xml");
            Verificador.assertTrue(partes.containsKey("xl/workbook.xml"), "Planilha deve ter workbook.xml");
            Verificador.assertTrue(partes.containsKey("xl/styles.xml"), "Planilha deve ter styles.xml");
            for (int i = 1; i <= 6; i++) {
                Verificador.assertTrue(partes.containsKey("xl/worksheets/sheet" + i + ".xml"), "Deve existir a aba " + i);
            }

            // abas em português
            String pasta = partes.get("xl/workbook.xml");
            for (String nome : new String[]{"Cursos", "Professores", "Alunos", "Disciplinas", "Matrículas", "Período"}) {
                Verificador.assertTrue(pasta.contains("name=\"" + nome + "\""), "Deve existir a aba " + nome);
            }

            // conteúdo, com caracteres especiais escapados
            String cursos = partes.get("xl/worksheets/sheet1.xml");
            Verificador.assertTrue(cursos.contains("Engenharia &amp; Software"), "'&' deve ser escapado no XML");
            Verificador.assertTrue(cursos.contains("<v>240</v>"), "Créditos devem ser célula numérica");
            Verificador.assertTrue(cursos.contains("state=\"frozen\""), "Primeira linha deve ficar fixa");
            Verificador.assertTrue(cursos.contains("<autoFilter ref=\"A1:C2\"/>"), "Deve haver filtro cobrindo a tabela");

            String disciplinas = partes.get("xl/worksheets/sheet4.xml");
            Verificador.assertTrue(disciplinas.contains("Arquitetura &lt;1&gt;"), "'<' e '>' devem ser escapados no XML");
            Verificador.assertTrue(disciplinas.contains("Em aberto"), "Status deve sair em português");
            Verificador.assertTrue(disciplinas.contains("<v>59</v>"), "Vagas restantes devem refletir a matrícula feita");

            String alunos = partes.get("xl/worksheets/sheet3.xml");
            Verificador.assertTrue(alunos.contains("t=\"inlineStr\"") && alunos.contains(">=SOMA(A1)<"),
                "Texto iniciado por '=' deve ficar como texto, nunca como fórmula");
            Verificador.assertFalse(alunos.contains("<f>"), "Planilha não deve conter fórmulas");

            String matriculas = partes.get("xl/worksheets/sheet5.xml");
            Verificador.assertTrue(matriculas.contains("Obrigatória") && matriculas.contains("Ativa"), "Tipo e situação da matrícula em português");

            // senhas nunca saem na planilha
            for (Map.Entry<String, String> parte : partes.entrySet()) {
                Verificador.assertFalse(parte.getValue().contains("segredo"), "Senha não deve aparecer em " + parte.getKey());
                Verificador.assertFalse(parte.getValue().contains(professor.getSenhaCriptografada()), "Hash de senha não deve aparecer em " + parte.getKey());
            }

            // caracteres de controle são descartados para não invalidar o XML
            Verificador.assertEquals("ab", ExportadorPlanilha.escapar("a\u0001b"), "Caractere de controle deve ser removido");
        } catch (IOException e) {
            Verificador.assertTrue(false, "Erro de I/O em ExportadorPlanilhaTest: " + e.getMessage());
        }
    }

    /** Abre o .xlsx (que é um ZIP) e devolve o texto de cada arquivo interno. */
    private static Map<String, String> lerPartes(byte[] xlsx) throws IOException {
        Map<String, String> partes = new HashMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(xlsx), StandardCharsets.UTF_8)) {
            ZipEntry entrada;
            while ((entrada = zip.getNextEntry()) != null) {
                partes.put(entrada.getName(), new String(zip.readAllBytes(), StandardCharsets.UTF_8));
            }
        }
        return partes;
    }
}
