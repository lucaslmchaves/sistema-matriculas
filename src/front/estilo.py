"""Aparência da tela: CSS, símbolo da PUC Minas e cabeçalho. O tema (cores) fica em .streamlit/config.toml."""
import base64
import html
import mimetypes
from functools import lru_cache
from pathlib import Path

import streamlit as st

PASTA_ASSETS = Path(__file__).parent / "assets"
ROTULOS_PERFIL = {"ALUNO": "Aluno", "PROFESSOR": "Professor", "SECRETARIA": "Secretaria"}

CSS = """
<style>
.logo-caixa {
    background: #ffffff;
    border-radius: 10px;
    padding: 6px 12px;
    display: inline-block;
    line-height: 0;
}
.logo-centro {
    text-align: center;
    margin: 12px 0 20px 0;
}
.cabecalho-titulo {
    color: #e0e1dd;
    font-size: 1.3rem;
    font-weight: 600;
    margin: 0;
}
.cabecalho-usuario {
    color: #d4af37;
    font-size: 0.9rem;
    margin: 0;
}
.stButton > button, .stDownloadButton > button {
    border-radius: 6px;
}
/* esconde o aviso "Press Enter to submit form" que o Streamlit mostra nos campos de formulário */
[data-testid="InputInstructions"] {
    display: none;
}
</style>
"""


@lru_cache(maxsize=1)
def _logo_em_base64():
    """Lê o símbolo da pasta assets e o converte em data URI, para ir embutido no HTML (sem servir arquivo). Lido uma vez só."""
    for nome in ("puc-minas.png", "puc-minas.svg"):
        caminho = PASTA_ASSETS / nome
        if caminho.exists():
            tipo = mimetypes.guess_type(nome)[0]
            conteudo = base64.b64encode(caminho.read_bytes()).decode("ascii")
            return f"data:{tipo};base64,{conteudo}"
    return None


def _logo_html(altura_px):
    """HTML do símbolo num cartão branco (o fundo do PNG é branco e destoaria no tema escuro); texto se não houver imagem."""
    fonte = _logo_em_base64()
    if fonte is None:
        return '<span class="cabecalho-titulo">PUC Minas</span>'
    return f'<div class="logo-caixa"><img src="{fonte}" style="height: {altura_px}px;" alt="PUC Minas"></div>'


def aplicar_estilo():
    """Injeta o CSS na página. Chamado uma vez a cada execução do script."""
    st.markdown(CSS, unsafe_allow_html=True)


def renderizar_logo_login():
    """Símbolo grande, centralizado, para a tela de login."""
    st.markdown(f'<div class="logo-centro">{_logo_html(110)}</div>', unsafe_allow_html=True)


def renderizar_cabecalho(nome, perfil):
    """Desenha o topo da página e devolve True se o usuário clicou em Sair."""
    col_logo, col_texto, col_sair = st.columns([1, 6, 1.3], vertical_alignment="center")
    with col_logo:
        st.markdown(_logo_html(56), unsafe_allow_html=True)
    with col_texto:
        st.markdown(
            '<p class="cabecalho-titulo">Sistema de Matrículas</p>'
            f'<p class="cabecalho-usuario">{html.escape(nome)} &middot; {ROTULOS_PERFIL.get(perfil, perfil)}</p>',
            unsafe_allow_html=True,
        )
    with col_sair:
        sair = st.button("Sair", key="btn_sair", use_container_width=True)
    st.divider()
    return sair
