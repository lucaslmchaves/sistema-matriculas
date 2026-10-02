package matriculas.cli;

import matriculas.enums.StatusMatricula;
import matriculas.enums.TipoMatricula;
import matriculas.excecoes.RegraDeNegocioException;
import matriculas.models.Aluno;
import matriculas.models.Disciplina;
import matriculas.models.Matricula;
import matriculas.models.PeriodoMatricula;
import matriculas.persistencia.Persistencia;
import matriculas.services.ServicoCadastro;
import matriculas.services.ServicoMatricula;

import java.util.ArrayList;
import java.util.List;

/** Painel do aluno no console: efetuar matrícula, cancelar matrícula e ver as próprias matrículas. */
public class MenuAluno {
    private final Aluno aluno;
    private final ServicoMatricula servicoMatricula;
    private final ServicoCadastro servicoCadastro;
    private final PeriodoMatricula periodo;
    private final Persistencia persistencia;
    private final EntradaConsole entrada;

    public MenuAluno(Aluno aluno, ServicoMatricula servicoMatricula, ServicoCadastro servicoCadastro, PeriodoMatricula periodo, Persistencia persistencia, EntradaConsole entrada) {
        this.aluno = aluno;
        this.servicoMatricula = servicoMatricula;
        this.servicoCadastro = servicoCadastro;
        this.periodo = periodo;
        this.persistencia = persistencia;
        this.entrada = entrada;
    }

    /** Repete o menu até o aluno voltar. Violações de regra viram mensagem, sem derrubar o programa. */
    public void exibir() {
        while (true) {
            exibirOpcoes();
            Integer opcao = entrada.lerInteiro("Opção");
            if (opcao == null || opcao == 0) {
                break;
            }
            try {
                executarOpcao(opcao);
            } catch (RegraDeNegocioException e) {
                System.out.println("[ERRO] " + e.getMessage());
            }
        }
    }

    private void exibirOpcoes() {
        System.out.println("\n=== Painel do Aluno (" + aluno.getNome() + " - Matrícula: " + aluno.getNumeroMatricula() + ") ===");
        System.out.println("1. Efetuar matrícula");
        System.out.println("2. Cancelar matrícula");
        System.out.println("3. Ver minhas matrículas");
        System.out.println("0. Voltar ao menu principal");
    }

    private void executarOpcao(int opcao) {
        switch (opcao) {
            case 1:
                efetuarMatricula();
                break;
            case 2:
                cancelarMatricula();
                break;
            case 3:
                verMinhasMatriculas();
                break;
            default:
                System.out.println("Opção inválida.");
        }
    }

    private void efetuarMatricula() {
        System.out.println("\n--- Efetuar Matrícula ---");
        if (!periodo.isAberto()) {
            System.out.println("[AVISO] O período de matrículas não está aberto no momento.");
            return;
        }

        List<Disciplina> disciplinasPeriodo = periodo.getDisciplinas();
        if (disciplinasPeriodo.isEmpty()) {
            System.out.println("[AVISO] Não há disciplinas ofertadas neste período.");
            return;
        }

        Disciplina disciplinaEscolhida = entrada.escolherDeLista(
            "Disciplinas Ofertadas no Período",
            disciplinasPeriodo,
            d -> d.getNome() + " | Status: " + d.getStatus()
                + " | Vagas restantes: " + d.vagasRestantes()
        );
        if (disciplinaEscolhida == null) {
            return;
        }

        TipoMatricula tipo = escolherTipo();
        if (tipo == null) {
            return;
        }

        Matricula matricula = servicoMatricula.efetuarMatricula(aluno, disciplinaEscolhida, tipo);
        persistencia.salvar(servicoCadastro, periodo);
        System.out.println("Matrícula realizada com sucesso na disciplina " + matricula.getDisciplina().getNome() + " como " + tipo + ".");
    }

    /** Pergunta se a matrícula é obrigatória ou optativa. Devolve null se o aluno cancelar. */
    private TipoMatricula escolherTipo() {
        System.out.println("\nEscolha o tipo de matrícula:");
        System.out.println("1. Obrigatória");
        System.out.println("2. Optativa");
        System.out.println("0. Cancelar");
        Integer opcao = entrada.lerInteiro("Tipo");
        if (opcao == null || opcao == 0) {
            return null;
        }
        if (opcao == 1) {
            return TipoMatricula.OBRIGATORIA;
        }
        if (opcao == 2) {
            return TipoMatricula.OPTATIVA;
        }
        System.out.println("Tipo inválido.");
        return null;
    }

    private void cancelarMatricula() {
        System.out.println("\n--- Cancelar Matrícula ---");
        if (!periodo.isAberto()) {
            System.out.println("[AVISO] Só é permitido cancelar matrículas com o período de matrículas aberto.");
            return;
        }

        List<Matricula> ativas = matriculasAtivas();
        if (ativas.isEmpty()) {
            System.out.println("Você não possui matrículas ativas no momento.");
            return;
        }

        Matricula escolhida = entrada.escolherDeLista(
            "Selecione a matrícula a cancelar",
            ativas,
            m -> m.getDisciplina().getNome() + " (" + m.getTipo() + " - realizada em " + m.getDataMatricula() + ")"
        );
        if (escolhida == null) {
            return;
        }

        if (servicoMatricula.cancelarMatricula(escolhida)) {
            persistencia.salvar(servicoCadastro, periodo);
            System.out.println("Matrícula cancelada com sucesso!");
        } else {
            System.out.println("[AVISO] Não foi possível cancelar a matrícula informada.");
        }
    }

    private void verMinhasMatriculas() {
        System.out.println("\n--- Minhas Matrículas ---");
        List<Matricula> matriculas = aluno.getMatriculas();
        if (matriculas.isEmpty()) {
            System.out.println("Nenhuma matrícula realizada.");
            return;
        }
        for (Matricula m : matriculas) {
            System.out.println("Disciplina: " + m.getDisciplina().getNome()
                + " | Tipo: " + m.getTipo()
                + " | Status: " + m.getStatus()
                + " | Data: " + m.getDataMatricula());
        }
    }

    private List<Matricula> matriculasAtivas() {
        List<Matricula> ativas = new ArrayList<>();
        for (Matricula m : aluno.getMatriculas()) {
            if (m.getStatus() == StatusMatricula.ATIVA) {
                ativas.add(m);
            }
        }
        return ativas;
    }
}
