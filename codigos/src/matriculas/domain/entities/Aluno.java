package matriculas.domain.entities;

import matriculas.domain.enums.TipoMatricula;
import matriculas.presentation.MenuVisitor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Aluno extends Usuario {
    private final String numeroMatricula;
    private final List<Matricula> matriculas;

    public Aluno(String nome, String numeroMatricula, String login, String senha) {
        super(nome, login, senha);
        if (numeroMatricula == null || numeroMatricula.isBlank()) throw new IllegalArgumentException("Matricula obrigatoria.");
        this.numeroMatricula = numeroMatricula;
        this.matriculas = new ArrayList<>();
    }

    //#region

    //#endregion

    public String getNumeroMatricula() { return numeroMatricula; }

    public boolean podeAdicionarMatricula(TipoMatricula tipo) {
        long contagemAtivas = matriculas.stream().filter(Matricula::isAtiva).filter(m -> m.getTipo() == tipo).count();
        return tipo == TipoMatricula.OBRIGATORIA ? contagemAtivas < 4 : contagemAtivas < 2;
    }

    public void adicionarMatricula(Matricula matricula) {
        if (!podeAdicionarMatricula(matricula.getTipo())) throw new IllegalStateException("Limite de matriculas excedido.");
        this.matriculas.add(matricula);
    }

    public List<Matricula> getMatriculas() { return Collections.unmodifiableList(matriculas); }
    @Override public void interagir(MenuVisitor visitor) { visitor.exibirMenuAluno(this); }
}
