"""Ponto de entrada da tela (streamlit run app.py): decide entre login e painel do perfil logado."""
import streamlit as st

from api_cliente import ApiCliente, ErroApi, SessaoExpirada
from estilo import ROTULOS_PERFIL, aplicar_estilo, renderizar_cabecalho, renderizar_logo_login
from telas.aluno import renderizar_tela_aluno
from telas.professor import renderizar_tela_professor
from telas.secretaria import renderizar_tela_secretaria

# cada perfil tem a sua tela; o perfil vem da API no login
TELAS_POR_PERFIL = {
    "ALUNO": renderizar_tela_aluno,
    "PROFESSOR": renderizar_tela_professor,
    "SECRETARIA": renderizar_tela_secretaria,
}


def exibir_avisos():
    """Mostra (uma única vez) as mensagens guardadas em session_state antes de recarregar a tela."""
    if "msg_sucesso" in st.session_state:
        st.success(st.session_state.pop("msg_sucesso"))
    if "msg_erro" in st.session_state:
        st.error(st.session_state.pop("msg_erro"))


def sair(api, aviso=None):
    """Encerra a sessão na API, limpa o estado da tela e volta ao login (com um aviso opcional)."""
    try:
        api.logout()
    except ErroApi:
        pass
    st.session_state.clear()
    if aviso:
        st.session_state["msg_erro"] = aviso
    st.rerun()


def tela_login(api):
    """Formulário de login; no sucesso guarda token, perfil e nome na sessão da tela."""
    renderizar_logo_login()
    _, centro, _ = st.columns([1, 2, 1])
    with centro.container(border=True):
        st.markdown("### Acesso ao sistema")
        with st.form("form_login", border=False):
            perfil = st.selectbox("Perfil", list(ROTULOS_PERFIL), format_func=ROTULOS_PERFIL.get, key="login_perfil")
            login = st.text_input("Login", key="login_usuario")
            senha = st.text_input("Senha", type="password", key="login_senha")
            if st.form_submit_button("Entrar", use_container_width=True):
                try:
                    dados = api.login(perfil, login, senha)
                except ErroApi as erro:
                    st.error(erro.mensagem)
                    return
                st.session_state.update(token=dados["token"], perfil=dados["perfil"], nome=dados["nome"])
                st.session_state["msg_sucesso"] = f"Bem-vindo(a), {dados['nome']}!"
                st.rerun()


def tela_principal(api):
    """Cabeçalho com o botão Sair e a tela correspondente ao perfil do usuário."""
    if renderizar_cabecalho(st.session_state["nome"], st.session_state["perfil"]):
        sair(api)
    TELAS_POR_PERFIL[st.session_state["perfil"]](api)


# O Streamlit executa este arquivo inteiro, de cima a baixo, a cada clique ou digitação.
# Por isso o que precisa sobreviver entre execuções (token, perfil, avisos) fica em st.session_state.
st.set_page_config(page_title="Sistema de Matrículas - PUC Minas", layout="wide")
aplicar_estilo()

api = ApiCliente(token=st.session_state.get("token"))
exibir_avisos()

if st.session_state.get("token"):
    try:
        tela_principal(api)
    except SessaoExpirada as erro:
        sair(api, aviso=erro.mensagem)
else:
    tela_login(api)
