package matriculas.presentation;

import matriculas.domain.entities.*;
import matriculas.domain.enums.TipoMatricula;
import matriculas.app.services.*;
import matriculas.infrastructure.BancoDeDados;
import java.util.Scanner;
import java.util.List;

/**
 * Interface de usuário via Console (Terminal).
 * Utiliza o padrão comportamental Visitor (Double Dispatch) para renderizar o menu
 * correspondente ao tipo de usuário autenticado, mantendo o sistema Aberto/Fechado (OCP).
 * Esta classe é responsável exclusivamente pela camada de Apresentação, delegando 
 * as regras de negócio aos respectivos serviços da camada de Aplicação.
 */
public class InterfaceConsole implements MenuVisitor {
    private final ServicoAutenticacao auth;
    private final ServicoMatricula matricula;
    private final ServicoAcademico academico;
    private final BancoDeDados db;
    private final Scanner sc = new Scanner(System.in);

    // Códigos ANSI para cores e formatação no terminal
    private static final String RESET = "\u001B[0m";
    private static final String BLUE = "\u001B[34m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";

    /**
     * Inicializa a interface de console, injetando as dependências necessárias 
     * para comunicação com a camada de serviços e repositório.
     * 
     * @param a  Serviço de autenticação e validação de login.
     * @param m  Serviço de coordenação de matrículas, cancelamentos e regras de negócio do semestre.
     * @param ac Serviço de gestão acadêmica (cadastros de cursos, disciplinas e professores).
     * @param db Referência do estado atual dos dados (utilizado aqui apenas para leitura visual).
     */
    public InterfaceConsole(ServicoAutenticacao a, ServicoMatricula m, ServicoAcademico ac, BancoDeDados db) {
        this.auth = a; 
        this.matricula = m; 
        this.academico = ac; 
        this.db = db;
    }

    /**
     * Limpa a tela do terminal, reposicionando o cursor no topo esquerdo.
     * Utiliza códigos de escape ANSI padronizados para criar a sensação de "tela cheia" 
     * e atualização contínua, melhorando a experiência de navegação (UX) no protótipo.
     */
    private void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * Congela a execução da interface até que o usuário pressione ENTER.
     * Mecanismo essencial de feedback para que o usuário consiga ler os retornos 
     * (mensagens de sucesso ou exceções de erro) antes que a tela seja limpa pelo loop.
     */
    private void pausar() {
        System.out.print("\n" + YELLOW + "Pressione ENTER para continuar..." + RESET);
        sc.nextLine();
    }

    /**
     * Inicia o loop principal do ciclo de vida do programa. 
     * É o ponto de entrada visual. Responsável por capturar as credenciais iniciais, 
     * instanciar a sessão segura e disparar o roteamento polimórfico (Visitor) 
     * que decidirá qual menu será desenhado na tela.
     */
    public void iniciar() {
        while (true) {
            limparTela();
            System.out.println(BLUE + "  __  __      _        _               _           ");
            System.out.println(" |  \\/  |__ _| |_ _ _(_)__ _  _ | |__ _(_)_ _      ");
            System.out.println(" | |\\/| / _` |  _| '_| / _| || | / _` | | '_|     ");
            System.out.println(" |_|  |_\\__,_|\\__|_| |_\\__|\\_,_|_\\__,_|_|_|  ");
            System.out.println(" ═════════════════════════════════════════════════" + RESET);
            System.out.println("  Universidade - Acesso ao Sistema                 \n");
            
            System.out.print("  Login: "); 
            String l = sc.nextLine();
            
            // Possibilidade de encerrar o processo (graceful shutdown)
            if (l.equalsIgnoreCase("sair")) {
                System.out.println("\nEncerrando o sistema...");
                break;
            }

            System.out.print("  Senha: "); 
            String s = sc.nextLine();

            Usuario u = auth.autenticar(l, s);
            
            if (u != null) {
                // Inversão de Controle OCP: A entidade autenticada chama o próprio menu adequado
                u.interagir(this);
            } else {
                System.out.println(RED + "\n  [ERRO] Usuário ou senha inválidos!" + RESET);
                pausar();
            }
        }
    }

    /**
     * Roteamento de interface exclusivo para usuários com o papel de Secretaria.
     * Consome o ServicoAcademico para gerenciar a estrutura da universidade (criar cursos, 
     * alocar professores) e o ServicoMatricula para abrir ou encerrar semestres letivos.
     * 
     * @param s Objeto Entidade da Secretaria autenticado na sessão atual.
     */
    @Override 
    public void exibirMenuSecretaria(Secretaria s) {
        while(true) {
            limparTela();
            System.out.println(CYAN + "   ___               _            _      " + RESET);
            System.out.println(CYAN + "  / __| ___ __ _ _ _| |_ __ _ _ _(_)__ _ " + RESET);
            System.out.println(CYAN + "  \\__ \\/ -_) _| '_|  _/ -_) '_| |/ _` |" + RESET);
            System.out.println(CYAN + "  |___/\\___\\__|_|  \\__\\___|_| |_|\\__,_|" + RESET);
            System.out.println(CYAN + " ═══════════════════════════════════════" + RESET);
            System.out.println("  Olá, " + s.getNome() + " | Status do Semestre: " + (db.periodoAberto ? GREEN+"ABERTO"+RESET : RED+"FECHADO"+RESET) + "\n");
            
            System.out.println("  1. Listar Cursos e Disciplinas");
            System.out.println("  2. Cadastrar Novo Curso");
            System.out.println("  3. Vincular Professor a Disciplina");
            System.out.println("  4. Abrir Período de Matrículas");
            System.out.println("  5. Encerrar Período de Matrículas (Gerar Currículo)");
            System.out.println("  0. Sair (Logout)");
            System.out.print("\n  Opção: ");
            
            String op = sc.nextLine();
            
            if (op.equals("0")) {
                return;
            } 
            else if (op.equals("1")) {
                System.out.println("\n--- Cursos e Disciplinas ---");
                academico.listarCursos().forEach(c -> {
                    System.out.println(BLUE + "\nCurso: " + c.getNome() + " (" + c.getCreditos() + " créditos)" + RESET);
                    academico.listarDisciplinas().stream()
                        .filter(d -> d.getCurso().getNome().equals(c.getNome()))
                        .forEach(d -> System.out.println("  - " + d.getNome() + " | Prof: " + (d.getProfessor() != null ? d.getProfessor().getNome() : "N/A")));
                });
                pausar();
            } 
            else if (op.equals("2")) {
                System.out.print("\n  Nome do Curso: "); 
                String nome = sc.nextLine();
                System.out.print("  Créditos: "); 
                try {
                    int cred = Integer.parseInt(sc.nextLine());
                    academico.cadastrarCurso(nome, cred);
                    System.out.println(GREEN + "  [SUCESSO] Curso cadastrado na instituição." + RESET);
                } catch (NumberFormatException e) {
                    System.out.println(RED + "  [ERRO] Valor de créditos deve ser um número inteiro." + RESET);
                } catch (IllegalArgumentException e) {
                    System.out.println(RED + "  [ERRO] " + e.getMessage() + RESET);
                }
                pausar();
            }
            else if (op.equals("3")) {
                System.out.print("\n  Nome exato da Disciplina: "); 
                String nomeDisc = sc.nextLine();
                System.out.print("  Login exato do Professor: "); 
                String loginProf = sc.nextLine();
                
                Disciplina disc = academico.listarDisciplinas().stream().filter(d -> d.getNome().equalsIgnoreCase(nomeDisc)).findFirst().orElse(null);
                Professor prof = academico.listarProfessores().stream().filter(p -> p.getLogin().equalsIgnoreCase(loginProf)).findFirst().orElse(null);
                
                if (disc != null && prof != null) {
                    academico.vincularProfessor(disc, prof);
                    System.out.println(GREEN + "  [SUCESSO] Professor vinculado à disciplina com sucesso!" + RESET);
                } else {
                    System.out.println(RED + "  [ERRO] Disciplina ou Professor não encontrados. Verifique os nomes." + RESET);
                }
                pausar();
            }
            else if (op.equals("4")) {
                matricula.abrirPeriodo();
                System.out.println(GREEN + "  [SUCESSO] O Período de matrículas foi ABERTO aos alunos." + RESET);
                pausar();
            }
            else if (op.equals("5")) {
                System.out.println("\n--- Processando fechamento do semestre ---");
                matricula.encerrarPeriodo();
                System.out.println(GREEN + "  [SUCESSO] Período ENCERRADO. As disciplinas sem quórum foram canceladas." + RESET);
                pausar();
            }
            else {
                System.out.println(RED + "  [ERRO] Opção inválida!" + RESET);
                pausar();
            }
        }
    }

    /**
     * Roteamento de interface exclusivo para usuários com o papel de Professor.
     * Foca na consulta do estado atual das turmas, consumindo o serviço acadêmico
     * para buscar relações de alunos ativos em suas disciplinas.
     * 
     * @param p Objeto Entidade do Professor autenticado na sessão atual.
     */
    @Override 
    public void exibirMenuProfessor(Professor p) {
        while(true) {
            limparTela();
            System.out.println(YELLOW + "   ___          __                     " + RESET);
            System.out.println(YELLOW + "  | _ \\_ _ ___ / _|___ ______ ___ _ _  " + RESET);
            System.out.println(YELLOW + "  |  _/ '_/ _ \\  _/ -_)_-<_-</ _ \\ '_| " + RESET);
            System.out.println(YELLOW + "  |_| |_| \\___/_| \\___/__/__/\\___/_|   " + RESET);
            System.out.println(YELLOW + " ═════════════════════════════════════" + RESET);
            System.out.println("  Mestre " + p.getNome() + "\n");
            
            System.out.println("  1. Ver meus alunos matriculados");
            System.out.println("  0. Sair (Logout)");
            System.out.print("\n  Opção: ");
            
            String op = sc.nextLine();
            
            if(op.equals("0")) {
                return;
            }
            else if (op.equals("1")) {
                List<Disciplina> minhasDisciplinas = academico.listarDisciplinas().stream()
                    .filter(d -> d.getProfessor() != null && d.getProfessor().getLogin().equals(p.getLogin()))
                    .toList();
                    
                if (minhasDisciplinas.isEmpty()) {
                    System.out.println(YELLOW + "  Você não possui disciplinas vinculadas neste semestre." + RESET);
                } else {
                    minhasDisciplinas.forEach(d -> {
                        System.out.println("\n" + CYAN + "Disciplina: " + d.getNome() + RESET);
                        List<Aluno> matriculados = d.listarAlunos();
                        if (matriculados.isEmpty()) {
                            System.out.println("  - Nenhum aluno matriculado até o momento.");
                        } else {
                            matriculados.forEach(a -> System.out.println("  - " + a.getNome() + " (Matrícula: " + a.getNumeroMatricula() + ")"));
                        }
                    });
                }
                pausar();
            }
            else {
                System.out.println(RED + "  [ERRO] Opção inválida!" + RESET);
                pausar();
            }
        }
    }

    /**
     * Roteamento de interface exclusivo para usuários com o papel de Aluno.
     * Centraliza as operações transacionais do sistema, permitindo a inscrição 
     * e o cancelamento de disciplinas, utilizando o tratamento de exceções (try/catch) 
     * para barrar ações fora das regras de negócio (capacidade, período fechado).
     * 
     * @param a Objeto Entidade do Aluno autenticado na sessão atual.
     */
    @Override 
    public void exibirMenuAluno(Aluno a) {
        while (true) {
            limparTela();
            System.out.println(GREEN + "     _  _                 " + RESET);
            System.out.println(GREEN + "    /_\\| |_  _ _ _  ___   " + RESET);
            System.out.println(GREEN + "   / _ \\ | || | ' \\/ _ \\  " + RESET);
            System.out.println(GREEN + "  /_/ \\_\\_\\_,_|_||_\\___/  " + RESET);
            System.out.println(GREEN + " ════════════════════════" + RESET);
            System.out.println("  Bem-vindo(a), " + a.getNome());
            System.out.println("  Matrícula: " + a.getNumeroMatricula() + " | Status do Semestre: " + (db.periodoAberto ? GREEN+"ABERTO"+RESET : RED+"FECHADO"+RESET) + "\n");
            
            System.out.println("  1. Listar Disciplinas Disponíveis");
            System.out.println("  2. Efetuar Matrícula");
            System.out.println("  3. Cancelar Matrícula");
            System.out.println("  4. Ver Minhas Matrículas");
            System.out.println("  0. Sair (Logout)");
            System.out.print("\n  Opção: ");
            
            String op = sc.nextLine();
            
            if (op.equals("0")) {
                return; // Encerra o escopo do método, devolvendo o usuário para o loop inicial de login
            } 
            else if (op.equals("1")) {
                System.out.println("\n--- Disciplinas Ofertadas no Semestre ---");
                db.disciplinas.forEach(d -> 
                    System.out.println(" - " + d.getNome() + " (Prof: " + 
                        (d.getProfessor() != null ? d.getProfessor().getNome() : "A definir") + ")")
                );
                pausar();
            } 
            else if (op.equals("2")) {
                System.out.print("\n  Digite o NOME EXATO da Disciplina: "); 
                String nome = sc.nextLine();
                System.out.print("  Tipo (1 = Obrigatória | 2 = Optativa): "); 
                String t = sc.nextLine();
                
                Disciplina d = db.disciplinas.stream()
                        .filter(x -> x.getNome().equalsIgnoreCase(nome))
                        .findFirst()
                        .orElse(null);
                        
                TipoMatricula tipo = "1".equals(t) ? TipoMatricula.OBRIGATORIA : TipoMatricula.OPTATIVA;
                        
                if (d != null) {
                    try {
                        // Tenta acionar a regra de negócio; as entidades Aluno e Disciplina 
                        // validarão internamente as restrições (limite 4 oblig, limite 60 vagas).
                        matricula.efetuarMatricula(a, d, tipo);
                        System.out.println(GREEN + "  [SUCESSO] Matrícula realizada e sistema financeiro notificado!" + RESET);
                    } catch(Exception e) {
                        System.out.println(RED + "  [ERRO DE NEGÓCIO] " + e.getMessage() + RESET);
                    }
                } else {
                    System.out.println(YELLOW + "  [AVISO] Disciplina não encontrada no catálogo atual." + RESET);
                }
                pausar();
            } 
            else if (op.equals("3")) {
                System.out.print("\n  Digite o NOME EXATO da Disciplina para cancelar a matrícula: "); 
                String nome = sc.nextLine();
                
                Disciplina d = db.disciplinas.stream()
                        .filter(x -> x.getNome().equalsIgnoreCase(nome))
                        .findFirst()
                        .orElse(null);
                
                if (d != null) {
                    try {
                        matricula.cancelarMatricula(a, d);
                        System.out.println(GREEN + "  [SUCESSO] Matrícula cancelada com sucesso!" + RESET);
                    } catch(Exception e) {
                        System.out.println(RED + "  [ERRO DE NEGÓCIO] " + e.getMessage() + RESET);
                    }
                } else {
                    System.out.println(YELLOW + "  [AVISO] Disciplina não encontrada no catálogo atual." + RESET);
                }
                pausar();
            }
            else if (op.equals("4")) {
                System.out.println("\n--- Histórico de Matrículas do Aluno ---");
                if (a.getMatriculas().isEmpty()) {
                    System.out.println("  Você não possui matrículas ativas ou canceladas.");
                } else {
                    a.getMatriculas().forEach(m -> 
                        System.out.println(" - " + m.getDisciplina().getNome() + " [" + m.getTipo() + "] -> " + 
                            (m.isAtiva() ? GREEN+"ATIVA"+RESET : RED+"CANCELADA"+RESET))
                    );
                }
                pausar();
            }
            else {
                System.out.println(RED + "  [ERRO] Opção de navegação inválida!" + RESET);
                pausar();
            }
        }
    }
}