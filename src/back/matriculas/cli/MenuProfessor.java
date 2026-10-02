package matriculas.cli;

import matriculas.excecoes.RegraDeNegocioException;
import matriculas.models.Aluno;
import matriculas.models.Disciplina;
import matriculas.models.Professor;

import java.util.ArrayList;
import java.util.List;

/** Painel do professor no console: consultar os alunos matriculados nas disciplinas que leciona. */
public class MenuProfessor {
    private final Professor professor;
    private final EntradaConsole entrada;

    public MenuProfessor(Professor professor, EntradaConsole entrada) {
        this.professor = professor;
        this.entrada = entrada;
    }

    /** Repete o menu até o professor voltar. */
    public void exibir() {
        while (true) {
            System.out.println("\n=== Painel do Professor (" + professor.getNome() + ") ===");
            System.out.println("1. Consultar alunos matriculados em uma disciplina");
            System.out.println("0. Voltar ao menu principal");

            Integer opcao = entrada.lerInteiro("Opção");
            if (opcao == null || opcao == 0) {
                break;
            }

            try {
                if (opcao == 1) {
                    consultarAlunosMatriculados();
                } else {
                    System.out.println("Opção inválida.");
                }
            } catch (RegraDeNegocioException e) {
                System.out.println("[ERRO] " + e.getMessage());
            }
        }
    }

    private void consultarAlunosMatriculados() {
        System.out.println("\n--- Minhas Disciplinas ---");
        List<Disciplina> minhasDisciplinas = disciplinasDoProfessor();
        if (minhasDisciplinas.isEmpty()) {
            System.out.println("Nenhuma disciplina atribuída a este professor.");
            return;
        }

        Disciplina escolhida = entrada.escolherDeLista(
            "Selecione a disciplina para consulta",
            minhasDisciplinas,
            d -> d.getNome() + " (Status: " + d.getStatus() + ")"
        );
        if (escolhida == null) {
            return;
        }

        List<Aluno> alunos = escolhida.listarAlunosMatriculados();
        System.out.println("\nAlunos matriculados na disciplina " + escolhida.getNome() + ":");
        if (alunos.isEmpty()) {
            System.out.println("Nenhum aluno matriculado nesta disciplina.");
            return;
        }
        for (Aluno a : alunos) {
            System.out.println("- " + a.getNome() + " (Matrícula: " + a.getNumeroMatricula() + ")");
        }
        System.out.println("Total: " + alunos.size() + " aluno(s).");
    }

    private List<Disciplina> disciplinasDoProfessor() {
        List<Disciplina> disciplinas = new ArrayList<>();
        for (Disciplina d : Disciplina.listarTodos()) {
            if (professor.equals(d.getProfessor())) {
                disciplinas.add(d);
            }
        }
        return disciplinas;
    }
}
