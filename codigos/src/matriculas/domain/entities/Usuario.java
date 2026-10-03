package matriculas.domain.entities;

import matriculas.presentation.MenuVisitor;
import java.io.Serializable;

public abstract class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String nome;
    private final String login;
    private final String senha;

    public Usuario(String nome, String login, String senha) {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome é obrigatório.");
        if (login == null || login.isBlank()) throw new IllegalArgumentException("Login é obrigatório.");
        if (senha == null || senha.isBlank()) throw new IllegalArgumentException("Senha é obrigatória.");
        
        this.nome = nome;
        this.login = login;
        this.senha = senha;
    }

    public String getNome() { return nome; }
    public String getLogin() { return login; }
    
    public boolean autenticar(String senhaTentativa) { 
        return this.senha.equals(senhaTentativa); 
    }
    
    public abstract void interagir(MenuVisitor visitor);
}