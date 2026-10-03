package matriculas.web;

import com.sun.net.httpserver.HttpExchange;
import matriculas.models.Usuario;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controla quem está logado na API. No login é gerado um token aleatório que o front
 * envia em cada requisição ({@code Authorization: Bearer <token>}).
 * As sessões ficam só em memória: não expiram e somem se a API reiniciar, o que é
 * suficiente para este protótipo.
 */
public class GerenciadorSessao {

    /** Dados de um usuário logado. */
    public static class Sessao {
        private final String token;
        private final PerfilUsuario perfil;
        private final Usuario usuario;
        private final String nome;

        public Sessao(String token, PerfilUsuario perfil, Usuario usuario, String nome) {
            this.token = token;
            this.perfil = perfil;
            this.usuario = usuario;
            this.nome = nome;
        }

        public String getToken() {
            return token;
        }

        public PerfilUsuario getPerfil() {
            return perfil;
        }

        public Usuario getUsuario() {
            return usuario;
        }

        public String getNome() {
            return nome;
        }
    }

    private final Map<String, Sessao> sessoes = new HashMap<>();

    /** Cria a sessão de um usuário que acabou de se autenticar. */
    public synchronized Sessao criarSessao(PerfilUsuario perfil, Usuario usuario, String nome) {
        String token = UUID.randomUUID().toString();
        Sessao sessao = new Sessao(token, perfil, usuario, nome);
        sessoes.put(token, sessao);
        return sessao;
    }

    /** Devolve a sessão do token, ou null se o token não existir. */
    public synchronized Sessao obterSessao(String token) {
        if (token == null) {
            return null;
        }
        return sessoes.get(token);
    }

    public synchronized void removerSessao(String token) {
        if (token != null) {
            sessoes.remove(token);
        }
    }

    /** Extrai o token do cabeçalho "Authorization: Bearer ...", ou null se não houver. */
    public String extrairToken(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7).trim();
    }
}
