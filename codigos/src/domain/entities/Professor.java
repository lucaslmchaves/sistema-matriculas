package domain.entities;

import presentation.MenuVisitor;

/**
 * Entidade que representa um Professor no sistema.
 * Atua como um dos atores de domínio, possuindo permissões e menus específicos.
 */
public class Professor extends Usuario {

    /**
     * Construtor da entidade Professor.
     * A validação de estado inicial (Fail-Fast) dos atributos de identidade 
     * é estritamente delegada à classe pai (Usuario).
     * 
     * @param nome Nome completo do professor.
     * @param login Login de acesso ao sistema.
     * @param senha Senha de autenticação.
     */
    public Professor(String nome, String login, String senha) {
        super(nome, login, senha);
    }

    /**
     * Implementação central do padrão comportamental Visitor (Double Dispatch).
     * Garante o roteamento da interface sem ferir o princípio OCP (Open/Closed Principle)
     * e sem o uso de instaceof na camada de apresentação.
     * 
     * @param visitor O componente responsável por desenhar a interface do menu.
     * @throws IllegalArgumentException Se o visitor injetado for nulo.
     */
    @Override 
    public void interagir(MenuVisitor visitor) { 
        if (visitor == null) {
            throw new IllegalArgumentException("O componente de interface (MenuVisitor) não pode ser nulo.");
        }
        
        visitor.exibirMenuProfessor(this); 
    }
}