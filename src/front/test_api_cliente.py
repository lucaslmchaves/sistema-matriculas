import unittest
from unittest.mock import MagicMock, patch

import requests

from api_cliente import ApiCliente, ErroApi, SessaoExpirada


def resposta_falsa(status, corpo=None):
    resposta = MagicMock()
    resposta.status_code = status
    resposta.json.return_value = corpo if corpo is not None else {}
    resposta.content = b"{}" if corpo is not None else b""
    return resposta


class TestApiCliente(unittest.TestCase):

    @patch("requests.request")
    def test_falha_de_conexao_vira_erro_api(self, mock_request):
        mock_request.side_effect = requests.exceptions.ConnectionError("sem rede")

        with self.assertRaises(ErroApi) as contexto:
            ApiCliente("http://localhost:8080").verificar_saude()

        self.assertIn("Back-end iniciando ou indisponível", contexto.exception.mensagem)

    @patch("requests.request")
    def test_erro_400_usa_a_mensagem_da_api(self, mock_request):
        mock_request.return_value = resposta_falsa(400, {"erro": "Já existe um curso com este nome."})

        with self.assertRaises(ErroApi) as contexto:
            ApiCliente("http://localhost:8080").cadastrar_curso("Engenharia", 240)

        self.assertEqual("Já existe um curso com este nome.", contexto.exception.mensagem)

    @patch("requests.request")
    def test_login_guarda_o_token(self, mock_request):
        mock_request.return_value = resposta_falsa(200, {"token": "abc", "perfil": "SECRETARIA", "nome": "Secretaria"})

        cliente = ApiCliente("http://localhost:8080")
        dados = cliente.login("SECRETARIA", "secretaria", "admin123")

        self.assertEqual("abc", dados["token"])
        self.assertEqual("abc", cliente.token)

    @patch("requests.request")
    def test_token_vai_no_cabecalho_authorization(self, mock_request):
        mock_request.return_value = resposta_falsa(200, {"aberto": False})

        ApiCliente("http://localhost:8080", token="meu-token").obter_periodo()

        cabecalhos = mock_request.call_args.kwargs["headers"]
        self.assertEqual("Bearer meu-token", cabecalhos["Authorization"])

    @patch("requests.request")
    def test_401_com_token_vira_sessao_expirada(self, mock_request):
        mock_request.return_value = resposta_falsa(401, {"erro": "Acesso não autorizado."})

        with self.assertRaises(SessaoExpirada):
            ApiCliente("http://localhost:8080", token="vencido").listar_cursos()

    @patch("requests.request")
    def test_401_no_login_e_erro_comum(self, mock_request):
        mock_request.return_value = resposta_falsa(401, {"erro": "Login ou senha inválidos."})

        with self.assertRaises(ErroApi) as contexto:
            ApiCliente("http://localhost:8080").login("ALUNO", "x", "y")

        self.assertNotIsInstance(contexto.exception, SessaoExpirada)
        self.assertEqual("Login ou senha inválidos.", contexto.exception.mensagem)


if __name__ == "__main__":
    unittest.main()
