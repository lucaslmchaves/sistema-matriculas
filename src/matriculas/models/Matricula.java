package matriculas.models;

import matriculas.enums.TipoMatricula;
import matriculas.enums.StatusMatricula;

import java.time.LocalDate;
import java.util.List;

public class Matricula {
    private LocalDate dataMatricula;
    private TipoMatricula tipo;
    private StatusMatricula status;
    private List<Matricula> matriculas;

    public boolean cancelar() {
        // TODO: implementar
        return false;
    }
}
