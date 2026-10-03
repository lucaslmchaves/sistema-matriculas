"""Cliente HTTP da API Java. É a única parte do front que conhece as rotas do back-end."""
import os

import requests


class ErroApi(Exception):
    """Erro devolvido pela API (regra de negócio violada, dado inválido) ou falha de conexão."""

    def __init__(self, mensagem):
        super().__init__(mensagem)
        self.mensagem = mensagem


class SessaoExpirada(ErroApi):
    """O token não vale mais (por exemplo, a API reiniciou); o usuário precisa entrar de novo."""


class ApiCliente:
    """Um método por rota da API. O token do usuário logado é enviado em todas as chamadas."""

    def __init__(self, base_url=None, token=None):
        self.base_url = (base_url or os.getenv("API_URL", "http://api:8080")).rstrip("/")
        self.token = token

    def _requisitar(self, metodo, endpoint, params=None, dados=None, binario=False):
        """Faz a chamada HTTP e converte as falhas em ErroApi (ou SessaoExpirada, no caso de 401)."""
        cabecalhos = {}
        if self.token:
            cabecalhos["Authorization"] = f"Bearer {self.token}"

        try:
            resposta = requests.request(
                method=metodo,
                url=f"{self.base_url}{endpoint}",
                params=params,
                data=dados,
                headers=cabecalhos,
                timeout=10,
            )
        except requests.exceptions.RequestException:
            raise ErroApi("Back-end iniciando ou indisponível. Tente novamente em instantes.")

        # 401 com token enviado significa sessão perdida; sem token é só login inválido
        if resposta.status_code == 401 and self.token:
            raise SessaoExpirada("Sua sessão expirou. Entre novamente.")

        if resposta.status_code >= 400:
            raise ErroApi(self._mensagem_de_erro(resposta))

        if binario:
            return resposta.content
        if not resposta.content:
            return {}
        try:
            return resposta.json()
        except ValueError:
            return {}

    @staticmethod
    def _mensagem_de_erro(resposta):
        """Pega a mensagem do campo "erro" do JSON da API, ou uma mensagem genérica."""
        try:
            return resposta.json().get("erro", f"Erro HTTP {resposta.status_code}")
        except ValueError:
            return f"Erro na requisição (código {resposta.status_code})"

    def verificar_saude(self):
        """Confere se a API está no ar."""
        return self._requisitar("GET", "/api/saude")

    def login(self, perfil, login, senha):
        """Autentica e guarda o token recebido para as próximas chamadas."""
        dados = self._requisitar("POST", "/api/login", dados={"perfil": perfil, "login": login, "senha": senha})
        self.token = dados.get("token")
        return dados

    def logout(self):
        """Encerra a sessão na API e esquece o token."""
        try:
            return self._requisitar("POST", "/api/logout")
        finally:
            self.token = None

    # --- período de matrículas ---

    def obter_periodo(self):
        """Situação do período e as disciplinas ofertadas (qualquer perfil)."""
        return self._requisitar("GET", "/api/periodo")

    def adicionar_disciplina_periodo(self, nome_disciplina):
        """Oferece uma disciplina no período (secretaria)."""
        return self._requisitar("POST", "/api/periodo/disciplinas", dados={"nome": nome_disciplina})

    def abrir_periodo(self):
        """Abre as matrículas (secretaria)."""
        return self._requisitar("POST", "/api/periodo/abrir")

    def encerrar_periodo(self):
        """Encerra as matrículas e devolve as disciplinas canceladas (secretaria)."""
        return self._requisitar("POST", "/api/periodo/encerrar")

    # --- cadastros (secretaria) ---

    def listar_cursos(self):
        return self._requisitar("GET", "/api/cursos")

    def cadastrar_curso(self, nome, creditos):
        return self._requisitar("POST", "/api/cursos", dados={"nome": nome, "creditos": str(creditos)})

    def listar_professores(self):
        return self._requisitar("GET", "/api/professores")

    def cadastrar_professor(self, nome, login, senha):
        return self._requisitar("POST", "/api/professores", dados={"nome": nome, "login": login, "senha": senha})

    def listar_alunos(self):
        return self._requisitar("GET", "/api/alunos")

    def cadastrar_aluno(self, nome, numero_matricula, login, senha):
        dados = {"nome": nome, "numeroMatricula": numero_matricula, "login": login, "senha": senha}
        return self._requisitar("POST", "/api/alunos", dados=dados)

    def listar_disciplinas(self):
        return self._requisitar("GET", "/api/disciplinas")

    def cadastrar_disciplina(self, nome, curso, login_professor):
        dados = {"nome": nome, "curso": curso, "professor": login_professor}
        return self._requisitar("POST", "/api/disciplinas", dados=dados)

    # --- dados de exemplo e planilha (secretaria) ---

    def carregar_exemplo(self, conjunto="basico"):
        """Cria os cadastros de demonstração, "basico" ou "completo" (só funciona com o sistema vazio)."""
        return self._requisitar("POST", "/api/exemplo", dados={"conjunto": conjunto})

    def gerar_matriculas_exemplo(self):
        """Matricula os alunos existentes nas disciplinas do período (exige o período aberto)."""
        return self._requisitar("POST", "/api/exemplo/matriculas")

    def lotar_disciplina_exemplo(self, disciplina):
        """Cria alunos e os matricula até a disciplina ficar sem vagas."""
        return self._requisitar("POST", "/api/exemplo/lotacao", dados={"disciplina": disciplina})

    def exportar_planilha(self):
        """Baixa a planilha .xlsx com os dados do sistema; devolve os bytes do arquivo."""
        return self._requisitar("GET", "/api/exportar", binario=True)

    # --- aluno ---

    def aluno_listar_matriculas(self):
        return self._requisitar("GET", "/api/aluno/matriculas")

    def aluno_efetuar_matricula(self, disciplina, tipo):
        return self._requisitar("POST", "/api/aluno/matriculas", dados={"disciplina": disciplina, "tipo": tipo})

    def aluno_cancelar_matricula(self, disciplina):
        return self._requisitar("POST", "/api/aluno/matriculas/cancelar", dados={"disciplina": disciplina})

    # --- professor ---

    def professor_listar_disciplinas(self):
        return self._requisitar("GET", "/api/professor/disciplinas")

    def professor_listar_alunos(self, disciplina):
        return self._requisitar("GET", "/api/professor/alunos", params={"disciplina": disciplina})
