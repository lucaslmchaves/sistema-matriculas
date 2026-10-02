package matriculas.models;

import matriculas.excecoes.RegraDeNegocioException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Classe abstrata com o que todo usuário do sistema tem em comum: login e senha.
 * A senha nunca é guardada em texto puro, apenas o hash SHA-256 dela.
 * Aluno, Professor e Secretaria herdam daqui.
 */
public abstract class Usuario {
    private String login;
    private String senha;

    /** Cria um usuário novo a partir da senha digitada, que é convertida em hash. */
    protected Usuario(String login, String senha) {
        validarLogin(login);
        validarSenha(senha);
        this.login = login.trim();
        this.senha = gerarHash(senha);
    }

    /**
     * Recria um usuário lido de arquivo. Se jaCriptografada for true, o valor
     * recebido já é o hash e é guardado como está.
     */
    protected Usuario(String login, String senhaCriptografada, boolean jaCriptografada) {
        validarLogin(login);
        if (senhaCriptografada == null || senhaCriptografada.trim().isEmpty()) {
            throw new RegraDeNegocioException("Senha criptografada não pode ser nula ou vazia.");
        }
        this.login = login.trim();
        this.senha = jaCriptografada ? senhaCriptografada : gerarHash(senhaCriptografada);
    }

    /** Confere se a senha digitada corresponde à senha do usuário, comparando os hashes. */
    public boolean autenticar(String senha) {
        if (senha == null) {
            return false;
        }
        String hashTentativa = gerarHash(senha);
        return MessageDigest.isEqual(
            this.senha.getBytes(StandardCharsets.UTF_8),
            hashTentativa.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String getLogin() {
        return login;
    }

    /** Devolve o hash da senha (usado apenas pela persistência). */
    public String getSenhaCriptografada() {
        return senha;
    }

    private static String gerarHash(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 não disponível.", e);
        }
    }

    private static void validarLogin(String login) {
        Validacao.texto(login, "Login");
    }

    private static void validarSenha(String senha) {
        if (senha == null || senha.length() < 4) {
            throw new RegraDeNegocioException("A senha deve conter no mínimo 4 caracteres.");
        }
        Validacao.semSeparadores(senha, "A senha");
    }
}
