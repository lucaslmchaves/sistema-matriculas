package matriculas.web;

import matriculas.Verificador;

import java.util.Arrays;

public class JsonTest {
    public static void executar() {
        // Teste de escape de aspas e barras
        String texto = "Texto com \"aspas\" e \\barra\\ e /barra-normal/";
        String escapado = Json.escapar(texto);
        Verificador.assertEquals("Texto com \\\"aspas\\\" e \\\\barra\\\\ e \\/barra-normal\\/", escapado, "Escape de aspas e barras");

        // Teste de quebra de linha e tabulação
        String quebras = "Linha 1\nLinha 2\r\nLinha 3\tTab";
        String escapadoQuebras = Json.escapar(quebras);
        Verificador.assertEquals("Linha 1\\nLinha 2\\r\\nLinha 3\\tTab", escapadoQuebras, "Escape de quebras de linha e tabulações");

        // Teste de caracteres de controle (< 32)
        String controle = "Controle: " + ((char) 7);
        String escapadoControle = Json.escapar(controle);
        Verificador.assertEquals("Controle: \\u0007", escapadoControle, "Escape de caractere de controle ASCII");

        // Teste de caracteres acentuados (UTF-8 preservado)
        String acentos = "Engenharia de Software & Matrículas";
        Verificador.assertEquals("Engenharia de Software & Matrículas", Json.escapar(acentos), "Caracteres acentuados devem ser preservados");

        // Teste de Json.erro e Json.status
        Verificador.assertEquals("{\"erro\":\"Falha de validação\"}", Json.erro("Falha de validação"), "Json.erro formatação");
        Verificador.assertEquals("{\"status\":\"ok\"}", Json.status("ok"), "Json.status formatação");

        // Teste de arrayStrings
        String arrayStr = Json.arrayStrings(Arrays.asList("item1", "item2"));
        Verificador.assertEquals("[\"item1\",\"item2\"]", arrayStr, "Json.arrayStrings formatação");

        // Teste de arrayJson
        String arrayJson = Json.arrayJson(Arrays.asList("{\"id\":1}", "{\"id\":2}"));
        Verificador.assertEquals("[{\"id\":1},{\"id\":2}]", arrayJson, "Json.arrayJson formatação");
    }
}
