package matriculas.domain.entities;
import matriculas.presentation.MenuVisitor;

public class Professor extends Usuario {
    public Professor(String nome, String login, String senha) {
        super(nome, login, senha);
    }
    @Override public void interagir(MenuVisitor visitor) { visitor.exibirMenuProfessor(this); }
}
