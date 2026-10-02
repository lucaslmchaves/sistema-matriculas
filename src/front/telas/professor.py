"""Tela do professor: suas disciplinas e os alunos matriculados em cada uma."""
import streamlit as st

from telas.componentes import buscar, mostrar_tabela


def renderizar_tela_professor(api):
    """Lista as disciplinas do professor e, para a escolhida, os alunos matriculados."""
    st.subheader("Painel do Professor")

    st.markdown("#### Minhas disciplinas")
    disciplinas = buscar(api.professor_listar_disciplinas)
    if disciplinas is None:
        return
    colunas = {"nome": "Disciplina", "status": "Status", "curso": "Curso", "matriculados": "Matriculados"}
    mostrar_tabela(disciplinas, colunas, "Nenhuma disciplina atribuída ao seu perfil.")
    if not disciplinas:
        return

    st.markdown("#### Alunos matriculados")
    escolhida = st.selectbox("Disciplina", [d["nome"] for d in disciplinas], key="professor_disciplina")
    alunos = buscar(api.professor_listar_alunos, escolhida)
    if alunos is None:
        return
    if alunos:
        st.write(f"Total: **{len(alunos)}** aluno(s)")
    mostrar_tabela(alunos, {"nome": "Aluno", "numeroMatricula": "Matrícula"}, "Nenhum aluno matriculado nesta disciplina.")
