package application.services;

import domain.entities.Usuario;
import infrastructure.BancoDeDados;

/**
 * Serviço da camada de aplicação responsável pela segurança e acesso ao Sistema de Matrículas.
 * Ele atua como uma ponte de leitura: conecta-se ao BancoDeDados para recuperar a lista de 
 * usuários registrados e delega a validação criptográfica para a própria entidade Usuario.
 */
public class ServicoAutenticacao {
    
    private final BancoDeDados db;

    /**
     * Construtor da classe com injeção da fonte de dados.
     * 
     * @param db A instância do banco de dados em memória que contém as listas do sistema.
     * @throws IllegalArgumentException Se a instância do banco de dados fornecida for nula.
     */
    public ServicoAutenticacao(BancoDeDados db) { 
        if (db == null) {
            throw new IllegalArgumentException("O banco de dados não pode ser nulo.");
        }
        this.db = db; 
    }

    /**
     * Valida as credenciais de acesso informadas pela camada de apresentação.
     * 
     * @param login A string correspondente ao nome de usuário (ex: matrícula ou email).
     * @param senha A string de texto contendo a senha digitada na interface.
     * @return A instância do objeto Usuario correspondente caso as credenciais estejam corretas, 
     *         ou null caso o usuário não seja encontrado, a senha esteja errada ou os parâmetros sejam nulos.
     */
    public Usuario autenticar(String login, String senha) {
        if (login == null || senha == null) {
            return null;
        }

        return db.usuarios.stream()
            .filter(u -> u.getLogin().equals(login) && u.autenticar(senha))
            .findFirst()
            .orElse(null);
    }
}