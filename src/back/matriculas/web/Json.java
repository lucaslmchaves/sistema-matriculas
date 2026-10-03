package matriculas.web;

import java.util.List;

/**
 * Montagem de JSON à mão. O JDK não traz biblioteca de JSON e o projeto não usa
 * dependências externas, então as respostas da API são geradas por esta classe.
 * Só gera JSON (a API não precisa ler), por isso não há parser.
 */
public class Json {

    /**
     * Construtor de objeto JSON: cada chamada acrescenta um campo e {@link #montar()}
     * devolve o texto final. Exemplo: {@code Json.objeto().texto("nome", "Ana").numero("idade", 20).montar()}.
     */
    public static class Objeto {
        private final StringBuilder texto = new StringBuilder("{");
        private boolean primeiroCampo = true;

        public Objeto texto(String chave, String valor) {
            return bruto(chave, string(valor));
        }

        public Objeto numero(String chave, long valor) {
            return bruto(chave, String.valueOf(valor));
        }

        public Objeto booleano(String chave, boolean valor) {
            return bruto(chave, String.valueOf(valor));
        }

        public Objeto lista(String chave, List<String> itens) {
            return bruto(chave, arrayStrings(itens));
        }

        /** Acrescenta um campo cujo valor já é JSON pronto (outro objeto ou array). */
        public Objeto bruto(String chave, String jsonPronto) {
            if (!primeiroCampo) {
                texto.append(",");
            }
            primeiroCampo = false;
            texto.append(string(chave)).append(":").append(jsonPronto);
            return this;
        }

        public String montar() {
            return texto + "}";
        }
    }

    public static Objeto objeto() {
        return new Objeto();
    }

    /** Escapa aspas, barras e caracteres de controle para o texto poder ficar dentro de uma string JSON. */
    public static String escapar(String texto) {
        if (texto == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '/':
                    sb.append("\\/");
                    break;
                default:
                    if (c < 32) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        return sb.toString();
    }

    /** Converte um texto em string JSON (com aspas); null vira o literal null. */
    public static String string(String texto) {
        if (texto == null) {
            return "null";
        }
        return "\"" + escapar(texto) + "\"";
    }

    public static String erro(String mensagem) {
        return objeto().texto("erro", mensagem).montar();
    }

    public static String status(String valor) {
        return objeto().texto("status", valor).montar();
    }

    /** Monta um array JSON de strings, ex.: ["a","b"]. */
    public static String arrayStrings(List<String> itens) {
        if (itens == null) {
            return "[]";
        }
        List<String> convertidos = itens.stream().map(Json::string).toList();
        return arrayJson(convertidos);
    }

    /** Monta um array JSON a partir de itens que já são JSON (objetos montados antes). */
    public static String arrayJson(List<String> objetosJson) {
        if (objetosJson == null) {
            return "[]";
        }
        return "[" + String.join(",", objetosJson) + "]";
    }
}
