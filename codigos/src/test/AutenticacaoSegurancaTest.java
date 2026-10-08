package test;

import domain.entities.*;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;


import application.services.ServicoAutenticacao;
import infrastructure.BancoDeDados;

/**
 * - AutenticacaoSegurancaTest
 * Suíte com foco em segurança criptográfica, verificação de sessão
 * e testes defensivos dos construtores.
 * 
 */
public class AutenticacaoSegurancaTest {

    private BancoDeDados db;
    private ServicoAutenticacao auth;

    @BeforeEach 
    public void setup(){
        db = new BancoDeDados();
        auth = new ServicoAutenticacao(db);

        Aluno aluno = new Aluno("Naruto Uzumaki", "naruto","senha123","RA55");
        db.usuarios.add(aluno);
    }

    /**
     * Valida login bem sucedido.
     */
    @Test 
    public void testLoginComCredenciaisCorretas(){
        Usuario logado = auth.autenticar("naruto","senha123");
        assertNotNull(logado, "O usuário deveria ser autenticado com sucesso. ");
    }

    /**
     * Valida o bloqueio de senhas erradas ou variáveis nulas.
     */
    @Test
    public void testLoginRejeitaSenhaIncorretasOuNulas(){
        assertNull(auth.autenticar("naruto","senhaErrada"), "Deveria retornar nulo para senhae errada.");
        assertNull(auth.autenticar("naruto",null), "Deveria retornar nulo ao invés de lançar NullPointerException.");
    }

    /**
     * Valida se a função do sistema (Construtores) barra lixo de memória imediatamente.
     * Aqui é onde se checa se as EXCEÇÕES arquiteturais estão funcionando.
     */
    @Test 
    public void testConstrutorLacamExcecaoParaDadosInvalidos(){
        assertThrows(IllegalArgumentException.class, () -> {
            new Aluno(null, "login", "senha", "RA99");
            }, "O cosntrutor do Usuário deve lançar IllegalArgumentException para nomes nulos."
        );

        assertThrows(IllegalArgumentException.class, () -> {
            new application.services.ServicoAcademico(null);
            }, "O serviço não pode ser instanciado sem o Banco de Dados."
        );
    }

}
