package matriculas.excecoes;

/**
 * Lançada quando uma regra do sistema é violada (por exemplo, matricular fora do
 * período). A mensagem é escrita para o usuário e pode ser exibida como está.
 */
public class RegraDeNegocioException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
