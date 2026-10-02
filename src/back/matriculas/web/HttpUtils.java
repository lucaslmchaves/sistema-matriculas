package matriculas.web;

import com.sun.net.httpserver.HttpExchange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Funções de apoio para ler requisições e enviar respostas HTTP, usadas por todos os handlers. */
public class HttpUtils {
    public static final int LIMITE_CORPO_BYTES = 64 * 1024;

    /** Lançada quando o corpo da requisição passa do limite (resulta em resposta 413). */
    public static class LimiteCorpoExcedidoException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public LimiteCorpoExcedidoException() {
            super("Corpo da requisição excede o limite permitido de 64 KB.");
        }
    }

    /** Envia uma resposta JSON em UTF-8 com o código de status informado. */
    public static void enviarJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        enviarCorpo(exchange, statusCode, bytes);
    }

    /** Envia um arquivo para download (usado na exportação em CSV). */
    public static void enviarBinario(HttpExchange exchange, int statusCode, String contentType, String nomeArquivo, byte[] dados) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + nomeArquivo + "\"");
        enviarCorpo(exchange, statusCode, dados);
    }

    /** Envia um erro no formato {"erro": "mensagem"}. */
    public static void enviarErro(HttpExchange exchange, int statusCode, String mensagem) throws IOException {
        enviarJson(exchange, statusCode, Json.erro(mensagem));
    }

    /** Lê o corpo de um POST enviado como formulário (chave=valor&chave=valor), até 64 KB. */
    public static Map<String, String> lerParametrosForm(HttpExchange exchange) throws IOException {
        String corpo = lerCorpo(exchange.getRequestBody());
        if (corpo.trim().isEmpty()) {
            return Collections.emptyMap();
        }
        return decodificarPares(corpo);
    }

    /** Lê os parâmetros da URL (a parte depois de "?"). */
    public static Map<String, String> lerQueryParams(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyMap();
        }
        return decodificarPares(query);
    }

    private static void enviarCorpo(HttpExchange exchange, int statusCode, byte[] bytes) throws IOException {
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String lerCorpo(InputStream entrada) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] bloco = new byte[4096];
        int lidos;
        int total = 0;
        while ((lidos = entrada.read(bloco)) != -1) {
            total += lidos;
            if (total > LIMITE_CORPO_BYTES) {
                throw new LimiteCorpoExcedidoException();
            }
            buffer.write(bloco, 0, lidos);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    /** Transforma "a=1&b=2" em um mapa, decodificando caracteres especiais (%20, +, acentos). */
    private static Map<String, String> decodificarPares(String texto) {
        Map<String, String> mapa = new HashMap<>();
        for (String parte : texto.split("&")) {
            if (parte.isEmpty()) {
                continue;
            }
            int igual = parte.indexOf('=');
            String chave = (igual >= 0) ? parte.substring(0, igual) : parte;
            String valor = (igual >= 0) ? parte.substring(igual + 1) : "";
            mapa.put(decodificar(chave), decodificar(valor));
        }
        return mapa;
    }

    private static String decodificar(String texto) {
        return URLDecoder.decode(texto, StandardCharsets.UTF_8);
    }
}
