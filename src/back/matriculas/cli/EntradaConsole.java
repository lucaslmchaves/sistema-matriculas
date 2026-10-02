package matriculas.cli;

import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

/**
 * Leitura de dados digitados no console. Centraliza o tratamento de entrada
 * inválida para que os menus não precisem repetir esse código.
 * Todos os métodos de leitura devolvem null quando a entrada termina (Ctrl+D / Ctrl+Z).
 */
public class EntradaConsole {
    private final Scanner scanner;

    public EntradaConsole() {
        this.scanner = new Scanner(System.in);
    }

    public EntradaConsole(Scanner scanner) {
        this.scanner = scanner;
    }

    /** Pede um texto e repete a pergunta até o usuário digitar algo não vazio. */
    public String lerTexto(String rotulo) {
        while (true) {
            String linha = lerLinha(rotulo);
            if (linha == null) {
                return null;
            }
            if (!linha.trim().isEmpty()) {
                return linha.trim();
            }
            System.out.println("Valor não pode ser vazio. Tente novamente.");
        }
    }

    /** Pede um número inteiro e repete a pergunta até o usuário digitar um número válido. */
    public Integer lerInteiro(String rotulo) {
        while (true) {
            String linha = lerLinha(rotulo);
            if (linha == null) {
                return null;
            }
            try {
                return Integer.parseInt(linha.trim());
            } catch (NumberFormatException e) {
                System.out.println("Número inválido. Digite um valor numérico inteiro.");
            }
        }
    }

    /**
     * Mostra uma lista numerada e devolve o item escolhido, ou null se o usuário
     * digitar 0 (voltar) ou se a lista estiver vazia.
     */
    public <T> T escolherDeLista(String titulo, List<T> itens, Function<T, String> formatador) {
        if (itens == null || itens.isEmpty()) {
            System.out.println("Nenhum item disponível para seleção.");
            return null;
        }
        System.out.println("\n--- " + titulo + " ---");
        for (int i = 0; i < itens.size(); i++) {
            System.out.println((i + 1) + ". " + formatador.apply(itens.get(i)));
        }
        System.out.println("0. Voltar/Cancelar");

        while (true) {
            Integer opcao = lerInteiro("Escolha uma opção");
            if (opcao == null || opcao == 0) {
                return null;
            }
            if (opcao >= 1 && opcao <= itens.size()) {
                return itens.get(opcao - 1);
            }
            System.out.println("Opção fora do intervalo válido (0 a " + itens.size() + ").");
        }
    }

    private String lerLinha(String rotulo) {
        System.out.print(rotulo + ": ");
        if (!scanner.hasNextLine()) {
            return null;
        }
        return scanner.nextLine();
    }
}
