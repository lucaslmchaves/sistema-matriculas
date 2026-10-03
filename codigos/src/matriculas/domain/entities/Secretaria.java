package matriculas.domain.entities;
import matriculas.presentation.MenuVisitor;

public class Secretaria extends Usuario {
    public Secretaria(String nome, String login, String senha) {
        super(nome, login, senha);
    }
    @Override public void interagir(MenuVisitor visitor) { visitor.exibirMenuSecretaria(this); }
}
