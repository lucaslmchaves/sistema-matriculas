package matriculas.app.services;

import matriculas.domain.entities.Usuario;
import matriculas.infrastructure.BancoDeDados;

public class ServicoAutenticacao {
    private final BancoDeDados db;
    public ServicoAutenticacao(BancoDeDados db) { this.db = db; }
    public Usuario autenticar(String login, String senha) {
        return db.usuarios.stream()
            .filter(u -> u.getLogin().equals(login) && u.autenticar(senha))
            .findFirst()
            .orElse(null);
    }
}
