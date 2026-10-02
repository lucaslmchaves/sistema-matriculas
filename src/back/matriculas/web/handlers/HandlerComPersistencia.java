package matriculas.web.handlers;

import matriculas.models.PeriodoMatricula;
import matriculas.persistencia.Persistencia;
import matriculas.services.ServicoCadastro;
import matriculas.web.GerenciadorSessao;
import matriculas.web.PerfilUsuario;

/**
 * Base dos handlers que alteram dados. Reúne o que eles têm em comum (serviço de cadastro,
 * período e persistência) e oferece {@link #salvar()}, chamado depois de cada alteração
 * para gravar os arquivos.
 */
public abstract class HandlerComPersistencia extends BaseHandler {
    protected final ServicoCadastro servicoCadastro;
    protected final PeriodoMatricula periodo;
    protected final Persistencia persistencia;

    protected HandlerComPersistencia(GerenciadorSessao gerenciadorSessao, ServicoCadastro servicoCadastro,
                                     PeriodoMatricula periodo, Persistencia persistencia, PerfilUsuario... perfisPermitidos) {
        super(gerenciadorSessao, true, perfisPermitidos);
        this.servicoCadastro = servicoCadastro;
        this.periodo = periodo;
        this.persistencia = persistencia;
    }

    protected void salvar() {
        persistencia.salvar(servicoCadastro, periodo);
    }
}
