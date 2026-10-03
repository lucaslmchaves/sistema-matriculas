package matriculas.models;

import matriculas.Verificador;
import matriculas.excecoes.RegraDeNegocioException;

public class UsuarioTest {
    public static void executar() {
        Aluno.limparRegistro();
        Disciplina.limparRegistro();
        Matricula.limparRegistro();

        // Autenticação com senha correta e incorreta
        Aluno aluno = new Aluno("João Silva", "202601", "jsilva", "senha123");
        Verificador.assertTrue(aluno.autenticar("senha123"), "Autenticação deve ter sucesso com a senha correta.");
        Verificador.assertFalse(aluno.autenticar("senhaErrada"), "Autenticação deve falhar com senha incorreta.");
        Verificador.assertFalse(aluno.autenticar(null), "Autenticação deve retornar false quando a senha fornecida for null.");

        // Validação de senha menor que 4 caracteres
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            new Aluno("Maria", "202602", "maria", "123");
        }, "Criação de usuário com senha menor que 4 caracteres deve lançar exceção.");

        // Validação de login nulo ou vazio
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            new Aluno("Maria", "202602", "", "senha123");
        }, "Criação de usuário com login vazio deve lançar exceção.");
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            new Aluno("Maria", "202602", null, "senha123");
        }, "Criação de usuário com login null deve lançar exceção.");

        // Validação de caracteres inválidos (ponto e vírgula e quebras de linha)
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            new Aluno("Maria", "202602", "login;invalido", "senha123");
        }, "Criação de usuário com login contendo ';' deve lançar exceção.");
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            new Aluno("Maria", "202602", "login", "senha;invalida");
        }, "Criação de usuário com senha contendo ';' deve lançar exceção.");
        Verificador.assertLanca(RegraDeNegocioException.class, () -> {
            new Aluno("Maria", "202602", "login\ninvalido", "senha123");
        }, "Criação de usuário com login contendo '\\n' deve lançar exceção.");

        // Restauração com senha já criptografada
        String hash = aluno.getSenhaCriptografada();
        Aluno alunoRestaurado = new Aluno("João Restaurado", "202603", "jrestaurado", hash, true);
        Verificador.assertTrue(alunoRestaurado.autenticar("senha123"), "Usuário restaurado com hash prévio deve autenticar normalmente.");
    }
}
