package domain.entities;

import presentation.MenuVisitor;

/**
 * Entidade que representa a Secretaria no sistema.
 * Atua como um dos atores de domínio com privilégios administrativos 
 * para gerenciar o semestre e o currículo.
 */
public class Secretaria extends Usuario {

    /**
     * Construtor da entidade Secretaria.
     * A validação de estado inicial (Fail-Fast) dos atributos de identidade 
     * é estritamente delegada à classe pai (Usuario).
     * 
     * @param nome Nome do funcionário ou setor da secretaria.
     * @param login Login de acesso administrativo ao sistema.
     * @param senha Senha de autenticação.
     */
    public Secretaria(String nome, String login, String senha) {
        super(nome, login, senha);
    }

    /**
     * Implementação central do padrão comportamental Visitor (Double Dispatch).
     * Garante o roteamento da interface sem ferir o princípio OCP (Open/Closed Principle)
     * e sem o uso de instanceof na camada de apresentação.
     * 
     * @param visitor O componente responsável por desenhar a interface do menu.
     * @throws IllegalArgumentException Se o visitor injetado for nulo.
     */
    @Override 
    public void interagir(MenuVisitor visitor) { 
        if (visitor == null) {
            throw new IllegalArgumentException("O componente de interface (MenuVisitor) não pode ser nulo.");
        }
        
        visitor.exibirMenuSecretaria(this); 
    }
}