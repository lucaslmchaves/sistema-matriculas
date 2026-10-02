"""Tela da secretaria: uma aba para cada cadastro, o período de matrículas e os dados do sistema."""
import streamlit as st

from telas.componentes import buscar, executar, mostrar_tabela


def renderizar_tela_secretaria(api):
    """Cria as abas e chama a função que desenha cada uma."""
    st.subheader("Painel da Secretaria")
    abas = st.tabs(["Cursos", "Professores", "Alunos", "Disciplinas", "Período de Matrículas", "Dados"])
    for aba, desenhar in zip(abas, (_aba_cursos, _aba_professores, _aba_alunos, _aba_disciplinas, _aba_periodo, _aba_dados)):
        with aba:
            desenhar(api)


def _aba_cursos(api):
    """Lista os cursos e permite cadastrar um novo."""
    st.markdown("#### Cursos cadastrados")
    cursos = buscar(api.listar_cursos)
    mostrar_tabela(cursos, {"nome": "Curso", "creditos": "Créditos", "disciplinas": "Disciplinas"}, "Nenhum curso cadastrado.")

    st.markdown("#### Novo curso")
    with st.form("form_curso"):
        nome = st.text_input("Nome do curso", key="curso_nome")
        creditos = st.number_input("Número de créditos", min_value=1, value=240, step=1, key="curso_creditos")
        if st.form_submit_button("Cadastrar curso"):
            executar(lambda: api.cadastrar_curso(nome, creditos), f"Curso '{nome}' cadastrado.", limpar=("curso_nome",))


def _aba_professores(api):
    """Lista os professores e permite cadastrar um novo."""
    st.markdown("#### Professores cadastrados")
    professores = buscar(api.listar_professores)
    mostrar_tabela(professores, {"nome": "Nome", "login": "Login"}, "Nenhum professor cadastrado.")

    st.markdown("#### Novo professor")
    with st.form("form_professor"):
        nome = st.text_input("Nome do professor", key="prof_nome")
        login = st.text_input("Login", key="prof_login")
        senha = st.text_input("Senha (mínimo 4 caracteres)", type="password", key="prof_senha")
        if st.form_submit_button("Cadastrar professor"):
            executar(
                lambda: api.cadastrar_professor(nome, login, senha),
                f"Professor '{nome}' cadastrado.",
                limpar=("prof_nome", "prof_login", "prof_senha"),
            )


def _aba_alunos(api):
    """Lista os alunos e permite cadastrar um novo."""
    st.markdown("#### Alunos cadastrados")
    alunos = buscar(api.listar_alunos)
    mostrar_tabela(alunos, {"nome": "Nome", "numeroMatricula": "Matrícula", "login": "Login"}, "Nenhum aluno cadastrado.")

    st.markdown("#### Novo aluno")
    with st.form("form_aluno"):
        nome = st.text_input("Nome do aluno", key="aluno_nome")
        matricula = st.text_input("Número de matrícula", key="aluno_matricula")
        login = st.text_input("Login", key="aluno_login")
        senha = st.text_input("Senha (mínimo 4 caracteres)", type="password", key="aluno_senha")
        if st.form_submit_button("Cadastrar aluno"):
            executar(
                lambda: api.cadastrar_aluno(nome, matricula, login, senha),
                f"Aluno '{nome}' cadastrado.",
                limpar=("aluno_nome", "aluno_matricula", "aluno_login", "aluno_senha"),
            )


def _aba_disciplinas(api):
    """Lista as disciplinas e cadastra uma nova (exige curso e professor já cadastrados)."""
    st.markdown("#### Disciplinas cadastradas")
    disciplinas = buscar(api.listar_disciplinas)
    colunas = {
        "nome": "Disciplina",
        "status": "Status",
        "curso": "Curso",
        "professor": "Professor",
        "matriculados": "Matriculados",
        "vagasRestantes": "Vagas restantes",
    }
    mostrar_tabela(disciplinas, colunas, "Nenhuma disciplina cadastrada.")

    st.markdown("#### Nova disciplina")
    cursos = buscar(api.listar_cursos)
    professores = buscar(api.listar_professores)
    if not cursos or not professores:
        st.warning("Cadastre pelo menos um curso e um professor antes de criar disciplinas.")
        return

    logins_por_rotulo = {f"{p['nome']} ({p['login']})": p["login"] for p in professores}
    with st.form("form_disciplina"):
        nome = st.text_input("Nome da disciplina", key="disc_nome")
        curso = st.selectbox("Curso", [c["nome"] for c in cursos], key="disc_curso")
        rotulo_professor = st.selectbox("Professor", list(logins_por_rotulo), key="disc_professor")
        if st.form_submit_button("Cadastrar disciplina"):
            executar(
                lambda: api.cadastrar_disciplina(nome, curso, logins_por_rotulo[rotulo_professor]),
                f"Disciplina '{nome}' cadastrada.",
                limpar=("disc_nome",),
            )


def _aba_periodo(api):
    """Situação do período, botões de abrir/encerrar e escolha das disciplinas ofertadas."""
    periodo = buscar(api.obter_periodo)
    if periodo is not None:
        st.markdown("#### Situação do período")
        col_status, col_inicio, col_fim = st.columns(3)
        col_status.metric("Status", "Aberto" if periodo["aberto"] else "Fechado")
        col_inicio.metric("Início", periodo["dataInicio"] or "-")
        col_fim.metric("Fim", periodo["dataFim"] or "-")

        st.markdown("#### Disciplinas do período")
        colunas = {
            "nome": "Disciplina",
            "status": "Status",
            "professor": "Professor",
            "matriculados": "Matriculados",
            "vagasRestantes": "Vagas restantes",
        }
        mostrar_tabela(periodo["disciplinas"], colunas, "Nenhuma disciplina adicionada ao período.")

    col_abrir, col_encerrar = st.columns(2)
    with col_abrir:
        if st.button("Abrir período de matrículas", key="btn_abrir", use_container_width=True):
            executar(api.abrir_periodo, "Período de matrículas aberto.")
    with col_encerrar:
        if st.button("Encerrar período de matrículas", key="btn_encerrar", use_container_width=True):
            executar(api.encerrar_periodo, _mensagem_encerramento)

    st.markdown("#### Adicionar disciplina ao período")
    todas = buscar(api.listar_disciplinas) or []
    ja_no_periodo = {d["nome"] for d in (periodo["disciplinas"] if periodo else [])}
    disponiveis = [d["nome"] for d in todas if d["nome"] not in ja_no_periodo and d["professor"]]
    if not disponiveis:
        st.info("Nenhuma disciplina com professor disponível para adicionar.")
        return
    with st.form("form_periodo_disciplina"):
        escolhida = st.selectbox("Disciplina", disponiveis, key="periodo_disciplina")
        if st.form_submit_button("Adicionar ao período"):
            executar(lambda: api.adicionar_disciplina_periodo(escolhida), f"Disciplina '{escolhida}' adicionada ao período.")


def _mensagem_encerramento(resultado):
    """Texto exibido ao encerrar o período, citando as disciplinas canceladas, se houver."""
    canceladas = resultado.get("canceladas", [])
    if canceladas:
        return "Período encerrado. Canceladas por falta de alunos: " + ", ".join(canceladas) + "."
    return "Período encerrado. Todas as disciplinas foram confirmadas."


def _aba_dados(api):
    """Dados de exemplo e exportação. O botão de download entrega a planilha pelo navegador (pasta Downloads)."""
    st.markdown("#### Dados de exemplo")
    st.write("Cria 1 curso, 2 professores, 5 alunos e 4 disciplinas já adicionadas ao período (que continua fechado).")
    if st.button("Carregar dados de exemplo", key="btn_exemplo"):
        executar(
            api.carregar_exemplo,
            "Dados de exemplo carregados.",
            ao_concluir=lambda resultado: st.session_state.update(logins_exemplo=resultado),
        )

    logins = st.session_state.get("logins_exemplo")
    if logins:
        st.markdown("**Logins criados**")
        st.code("\n".join(logins["professores"] + logins["alunos"]), language=None)

    st.markdown("#### Exportar dados")
    st.write("Planilha do Excel (.xlsx) com uma aba para cada tabela: cursos, professores, alunos, disciplinas, matrículas e período.")
    planilha = buscar(api.exportar_planilha)
    if planilha is not None:
        st.download_button(
            "Baixar planilha (.xlsx)",
            data=planilha,
            file_name="matriculas.xlsx",
            mime="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            key="btn_baixar_planilha",
        )
