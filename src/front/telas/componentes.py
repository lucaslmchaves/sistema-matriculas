"""Peças reaproveitadas pelas telas: consulta e ação com tratamento de erro, tabela e rótulos."""
import streamlit as st

from api_cliente import ErroApi, SessaoExpirada

# a API devolve os valores em maiúsculas (EM_ABERTO, OBRIGATORIA...); aqui viram texto para o usuário
ROTULOS_VALOR = {
    "EM_ABERTO": "Em aberto",
    "ATIVA": "Ativa",
    "CANCELADA": "Cancelada",
    "OBRIGATORIA": "Obrigatória",
    "OPTATIVA": "Optativa",
}


def rotulo(valor):
    """Texto amigável para um valor da API; valores desconhecidos voltam como vieram."""
    return ROTULOS_VALOR.get(valor, valor)


def buscar(chamada, *argumentos):
    """Executa uma consulta à API; mostra o erro na tela e devolve None se falhar."""
    try:
        return chamada(*argumentos)
    except SessaoExpirada:
        raise
    except ErroApi as erro:
        st.error(erro.mensagem)
        return None


def executar(acao, mensagem_sucesso, ao_concluir=None, limpar=()):
    """Executa uma ação que altera dados.

    O Streamlit refaz a tela a cada interação, então no sucesso o aviso fica em session_state
    (texto ou função do resultado), os campos de limpar são apagados e a tela é recarregada.
    Se a API recusar a ação, o erro aparece e os campos preenchidos são mantidos.
    """
    try:
        resultado = acao()
    except SessaoExpirada:
        raise
    except ErroApi as erro:
        st.error(erro.mensagem)
        return
    if ao_concluir:
        ao_concluir(resultado)
    for chave in limpar:
        st.session_state.pop(chave, None)
    mensagem = mensagem_sucesso(resultado) if callable(mensagem_sucesso) else mensagem_sucesso
    st.session_state["msg_sucesso"] = mensagem
    st.rerun()


def mostrar_tabela(registros, colunas, vazio):
    """Mostra uma lista de registros da API com títulos em português; colunas = {campo: título}."""
    if not registros:
        st.info(vazio)
        return
    linhas = []
    for registro in registros:
        linha = {}
        for campo, titulo in colunas.items():
            valor = registro.get(campo)
            if isinstance(valor, list):
                valor = ", ".join(valor)
            elif campo in ("status", "tipo"):
                valor = rotulo(valor)
            linha[titulo] = valor
        linhas.append(linha)
    st.dataframe(linhas, use_container_width=True, hide_index=True)
