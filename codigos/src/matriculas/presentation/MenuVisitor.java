package matriculas.presentation;

import matriculas.domain.entities.Aluno;
import matriculas.domain.entities.Professor;
import matriculas.domain.entities.Secretaria;

/**
 * Padrão Visitor para resolver a UI dinamicamente, eliminando instaceof (OCP).
 */
public interface MenuVisitor {
    void exibirMenuSecretaria(Secretaria secretaria);
    void exibirMenuProfessor(Professor professor);
    void exibirMenuAluno(Aluno aluno);
}