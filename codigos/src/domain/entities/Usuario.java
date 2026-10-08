package domain.entities;

import presentation.MenuVisitor;

/**
 * Classe abstrata base para todos os atores do sistema.
 * Centraliza a identidade, as credenciais de acesso e garante 
 * o contrato arquitetural para o roteamento da interface.
 */
public abstract class Usuario {

    private final String nome;
    private final String login;
    private final String senha;

    /**
     * Construtor base da entidade com validação rigorosa de estado inicial (Fail-Fast).
     * Impede a criação de qualquer usuário na memória sem credenciais válidas.
     * 
     * @param nome Nome completo ou de exibição do usuário.
     * @param login Credencial de login para acesso ao sistema.
     * @param senha Senha de autenticação.
     * @throws IllegalArgumentException Se nome, login ou senha forem nulos ou compostos apenas por espaços.
     */
    public Usuario(String nome, String login, String senha) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do usuário é obrigatório e não pode ser vazio.");
        }
        if (login == null || login.trim().isEmpty()) {
            throw new IllegalArgumentException("O login é obrigatório e não pode ser vazio.");
        }
        if (senha == null || senha.trim().isEmpty()) {
            throw new IllegalArgumentException("A senha é obrigatória e não pode ser vazia.");
        }
        
        this.nome = nome;
        this.login = login;
        this.senha = senha;
    }

    //#region getters e setters para a interface
    /**
     * @return O nome do usuário para exibição na tela de boas-vindas.
     */
    public String getNome() { 
        return nome; 
    }

    /**
     * @return O login do usuário (utilizado nos mapeamentos do banco de dados).
     */
    public String getLogin() { 
        return login; 
    }
    //#endregion
    
    /**
     * Compara a tentativa de senha com a credencial real armazenada.
     * O encapsulamento garante que a senha real (privada) jamais seja exposta 
     * para validações externas através de um getter.
     * 
     * @param senhaTentativa A senha digitada pelo usuário no momento do login.
     * @return true se as senhas coincidirem, false se forem diferentes ou se a tentativa for nula.
     */
    public boolean autenticar(String senhaTentativa) { 
        if (senhaTentativa == null) {
            return false;
        }
        return this.senha.equals(senhaTentativa); 
    }
    
    /**
     * Contrato do padrão comportamental Visitor (Double Dispatch).
     * Força todas as classes filhas (Aluno, Professor, Secretaria) a definirem 
     * explicitamente para qual menu da interface elas devem ser direcionadas.
     * 
     * @param visitor O componente responsável pela renderização visual dos menus.
     */
    public abstract void interagir(MenuVisitor visitor);
}