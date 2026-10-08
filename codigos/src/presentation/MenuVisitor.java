package presentation;

import domain.entities.Aluno;
import domain.entities.Professor;
import domain.entities.Secretaria;

/**
 * Padrão Visitor para resolver a UI dinamicamente, eliminando instaceof (OCP).
 */
public interface MenuVisitor {
    void exibirMenuSecretaria(Secretaria secretaria);
    void exibirMenuProfessor(Professor professor);
    void exibirMenuAluno(Aluno aluno);
}