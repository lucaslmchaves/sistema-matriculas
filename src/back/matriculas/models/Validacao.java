package matriculas.models;

import matriculas.excecoes.RegraDeNegocioException;

/**
 * Validações de texto reaproveitadas pelas classes do domínio.
 * O ponto e vírgula e as quebras de linha são proibidos porque os dados são
 * gravados em arquivos com ";" como separador e uma linha por registro.
 */
public final class Validacao {

    private Validacao() {
    }

    /** Garante que o texto não é nulo nem vazio e devolve o próprio texto. */
    public static String obrigatorio(String valor, String rotulo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new RegraDeNegocioException(rotulo + " não pode ser nulo ou vazio.");
        }
        return valor;
    }

    /** Garante que o texto não tem caracteres que quebrariam o formato dos arquivos e devolve o próprio texto. */
    public static String semSeparadores(String valor, String rotulo) {
        if (valor != null && (valor.contains(";") || valor.contains("\n") || valor.contains("\r"))) {
            throw new RegraDeNegocioException(rotulo + " não pode conter ponto e vírgula ou quebras de linha.");
        }
        return valor;
    }

    /** Valida um campo de texto comum (obrigatório e sem separadores) e devolve o texto sem espaços nas pontas. */
    public static String texto(String valor, String rotulo) {
        obrigatorio(valor, rotulo);
        semSeparadores(valor, rotulo);
        return valor.trim();
    }
}
