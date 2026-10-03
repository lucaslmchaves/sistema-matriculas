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

    /** Garante que o texto não é nulo nem vazio. */
    public static void obrigatorio(String valor, String rotulo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new RegraDeNegocioException(rotulo + " não pode ser nulo ou vazio.");
        }
    }

    /** Garante que o texto não tem caracteres que quebrariam o formato dos arquivos. */
    public static void semSeparadores(String valor, String rotulo) {
        if (valor != null && (valor.contains(";") || valor.contains("\n") || valor.contains("\r"))) {
            throw new RegraDeNegocioException(rotulo + " não pode conter ponto e vírgula ou quebras de linha.");
        }
    }

    /** Valida um campo de texto comum: obrigatório e sem separadores. */
    public static void texto(String valor, String rotulo) {
        obrigatorio(valor, rotulo);
        semSeparadores(valor, rotulo);
    }
}
