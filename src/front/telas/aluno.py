"""Tela do aluno: ver as disciplinas ofertadas, matricular-se e cancelar matrículas."""
import streamlit as st

from telas.componentes import buscar, executar, mostrar_tabela, rotulo


def renderizar_tela_aluno(api):
    """Monta o painel do aluno: disciplinas ofertadas no alto, matrícula e minhas matrículas lado a lado."""
    st.subheader("Painel do Aluno")
    periodo = buscar(api.obter_periodo)
    _disciplinas_ofertadas(periodo)

    col_matricula, col_minhas = st.columns(2)
    with col_matricula:
        _formulario_matricula(api, periodo)
    with col_minhas:
        _minhas_matriculas(api)


def _disciplinas_ofertadas(periodo):
    """Mostra se o período está aberto e a tabela de disciplinas ofertadas."""
    st.markdown("#### Disciplinas ofertadas no período")
    if periodo is None:
        return

    if periodo["aberto"]:
        st.success("O período de matrículas está aberto.")
    else:
        st.warning("O período de matrículas está fechado.")

    colunas = {
        "nome": "Disciplina",
        "status": "Status",
        "professor": "Professor",
        "matriculados": "Matriculados",
        "vagasRestantes": "Vagas restantes",
    }
    mostrar_tabela(periodo["disciplinas"], colunas, "Nenhuma disciplina ofertada neste período.")


def _formulario_matricula(api, periodo):
    """Formulário de matrícula; só aparece com o período aberto e disciplinas não canceladas."""
    st.markdown("#### Efetuar matrícula")
    if periodo is None:
        return
    disponiveis = [d["nome"] for d in periodo["disciplinas"] if d["status"] != "CANCELADA"]
    if not periodo["aberto"] or not disponiveis:
        st.info("A matrícula fica disponível quando o período está aberto e há disciplinas ofertadas.")
        return

    with st.form("form_matricula"):
        disciplina = st.selectbox("Disciplina", disponiveis, key="matricula_disciplina")
        tipo = st.selectbox("Tipo de matrícula", ["OBRIGATORIA", "OPTATIVA"], format_func=rotulo, key="matricula_tipo")
        if st.form_submit_button("Matricular"):
            executar(
                lambda: api.aluno_efetuar_matricula(disciplina, tipo),
                f"Matrícula em '{disciplina}' ({rotulo(tipo).lower()}) realizada.",
            )


def _minhas_matriculas(api):
    """Tabela das matrículas do aluno e formulário para cancelar uma matrícula ativa."""
    st.markdown("#### Minhas matrículas")
    matriculas = buscar(api.aluno_listar_matriculas)
    if matriculas is None:
        return

    colunas = {"disciplina": "Disciplina", "tipo": "Tipo", "status": "Status", "dataMatricula": "Data"}
    mostrar_tabela(matriculas, colunas, "Você ainda não realizou nenhuma matrícula.")

    ativas = [m["disciplina"] for m in matriculas if m["status"] == "ATIVA"]
    if not ativas:
        return

    with st.form("form_cancelamento"):
        disciplina = st.selectbox("Cancelar matrícula em", ativas, key="cancelamento_disciplina")
        if st.form_submit_button("Cancelar matrícula"):
            executar(lambda: api.aluno_cancelar_matricula(disciplina), f"Matrícula em '{disciplina}' cancelada.")
