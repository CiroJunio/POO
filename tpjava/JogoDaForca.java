import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

/**
 * Classe principal da interface gráfica do Jogo da Forca.
 */
public class JogoDaForca extends JFrame {
    private ForcaPanel forcaPanel;              // Painel para desenho da forca
    private JLabel palavraLabel;                // Exibe a palavra escondida
    private JLabel letrasDigitadasLabel;        // Mostra letras já digitadas
    private JLabel vitoriasLabel;               // Contador de vitórias
    private JLabel derrotasLabel;               // Contador de derrotas
    private JLabel pontuacaoLabel;              // Pontuação do jogador
    private JButton novoJogoButton;             // Botão para iniciar novo jogo
    private JButton voltarMenuButton;           // Botão para voltar ao menu
    private JButton sairJogoButton;             // Botão para sair do jogo
    private JPanel teclado;                     // Painel do teclado virtual
    private JButton resetarPontuacaoButton;     // Botão para resetar pontuação

    private JogoDaForcaLogic jogo;              // Lógica do jogo

    /**
     * Construtor da interface do Jogo da Forca.
     * 
     * @param bancoPalavraspath Caminho do arquivo de palavras
     */
    public JogoDaForca(String bancoPalavraspath) {
        setTitle("Jogo da Forca");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        jogo = new JogoDaForcaLogic(bancoPalavraspath);

        // Listener para salvar dados ao fechar a janela
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                jogo.salvarJogadores();
            }
        });

        mostrarTelaLogin();
    }

    /**
     * Exibe a tela inicial de login e cadastro de jogadores.
     */
    private void mostrarTelaLogin() {
        getContentPane().removeAll();
        setLayout(new GridLayout(5, 1));

        JLabel instrucaoLabel = new JLabel("Selecione ou cadastre um jogador:", SwingConstants.CENTER);
        add(instrucaoLabel);

        // ComboBox com lista de jogadores existentes
        JComboBox<Jogador> jogadoresComboBox = new JComboBox<>();
        List<Jogador> jogadores = jogo.getJogadores();
        for (Jogador jogador : jogadores) {
            jogadoresComboBox.addItem(jogador);
        }
        add(jogadoresComboBox);

        // Campo para cadastrar novo jogador
        JTextField novoJogadorField = new JTextField("Digite o nome do novo jogador");
        novoJogadorField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (novoJogadorField.getText().equals("Digite o nome do novo jogador")) {
                    novoJogadorField.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (novoJogadorField.getText().isEmpty()) {
                    novoJogadorField.setText("Digite o nome do novo jogador");
                }
            }
        });
        add(novoJogadorField);

        // Botão de login
        JButton loginButton = new JButton("Entrar");
        loginButton.addActionListener(e -> {
            String novoJogadorNome = novoJogadorField.getText().trim();
            if (!novoJogadorNome.isEmpty() && !novoJogadorNome.equals("Digite o nome do novo jogador")) {
                jogo.cadastrarJogador(novoJogadorNome);
                jogo.setJogadorAtual(novoJogadorNome);
            } else if (jogadoresComboBox.getSelectedItem() != null) {
                Jogador jogadorSelecionado = (Jogador) jogadoresComboBox.getSelectedItem();
                jogo.setJogadorAtual(jogadorSelecionado.getNome());
            } else {
                JOptionPane.showMessageDialog(this, "Por favor, selecione um jogador ou cadastre um novo.");
                return;
            }
            mostrarMenuInicial();
        });
        add(loginButton);

        revalidate();
        repaint();
    }

    /**
     * Exibe o menu inicial com opções para jogar ou sair.
     */
    private void mostrarMenuInicial() {
        getContentPane().removeAll();
        setLayout(new GridLayout(3, 1));

        JButton jogarButton = new JButton("Jogar");
        JButton sairButton = new JButton("Sair do Jogo");

        jogarButton.addActionListener(e -> mostrarSelecaoTamanhoPalavra());
        sairButton.addActionListener(e -> sairDoJogo());

        add(new JLabel("Bem-vindo ao Jogo da Forca, " + jogo.getJogadorAtual().getNome() + "!", SwingConstants.CENTER));
        add(jogarButton);
        add(sairButton);

        revalidate();
        repaint();
    }

    /**
     * Exibe a tela de seleção do tamanho da palavra.
     */
    private void mostrarSelecaoTamanhoPalavra() {
        getContentPane().removeAll();
        setLayout(new GridLayout(0, 1));

        JLabel instrucaoLabel = new JLabel("Escolha o tamanho da palavra (3 a 14 letras):", SwingConstants.CENTER);
        add(instrucaoLabel);

        // Botões para cada tamanho de palavra
        for (int i = 3; i <= 14; i++) {
            JButton tamanhoButton = new JButton(i + " letras");
            final int tamanho = i;
            tamanhoButton.addActionListener(e -> iniciarJogoComTamanho(tamanho));
            add(tamanhoButton);
        }

        JButton resetarPontuacaoButton = new JButton("Resetar Pontuacao");
        resetarPontuacaoButton.addActionListener(e -> resetarPontuacao());
        add(resetarPontuacaoButton);

        JButton voltarButton = new JButton("Voltar ao Menu");
        voltarButton.addActionListener(e -> mostrarMenuInicial());
        add(voltarButton);

        revalidate();
        repaint();
    }

    /**
     * Inicia o jogo com o tamanho de palavra especificado.
     * 
     * @param tamanho Tamanho da palavra escolhida
     */
    private void iniciarJogoComTamanho(int tamanho) {
        jogo.setTamanhoPalavra(tamanho);
        iniciarJogo(-1);
    }

    /**
     * Inicia um novo jogo, criando a interface principal.
     * 
     * @param unused Parâmetro não utilizado (mantido por compatibilidade)
     */
    private void iniciarJogo(int unused) {
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        try {
            jogo.novoJogo();
            criarComponentes();
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            mostrarSelecaoTamanhoPalavra();
            return;
        }

        revalidate();
        repaint();
    }

    /**
     * Cria e organiza os componentes da interface do jogo.
     */
    private void criarComponentes() {
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 50, 10, 10);

        forcaPanel = new ForcaPanel(jogo.getForca());
        forcaPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        forcaPanel.setPreferredSize(new Dimension(350, 350));
        topPanel.add(forcaPanel, gbc);

        add(topPanel, BorderLayout.NORTH);

        // Painel de informações
        JPanel infoPanel = new JPanel(new GridLayout(8, 1));
        infoPanel.setBorder(BorderFactory.createTitledBorder("INFORMACOES"));

        palavraLabel = new JLabel("Palavra: ");
        letrasDigitadasLabel = new JLabel("Guesses: ");
        vitoriasLabel = new JLabel("Hits: ");
        derrotasLabel = new JLabel("Fails: ");
        pontuacaoLabel = new JLabel("Pontuacao: ");
        novoJogoButton = new JButton("Novo Jogo");
        voltarMenuButton = new JButton("Voltar ao Menu");
        sairJogoButton = new JButton("Sair do Jogo");
        resetarPontuacaoButton = new JButton("Resetar Pontuacao");

        infoPanel.add(palavraLabel);
        infoPanel.add(letrasDigitadasLabel);
        infoPanel.add(vitoriasLabel);
        infoPanel.add(derrotasLabel);
        infoPanel.add(pontuacaoLabel);
        infoPanel.add(novoJogoButton);
        infoPanel.add(voltarMenuButton);
        infoPanel.add(sairJogoButton);
        infoPanel.add(resetarPontuacaoButton);

        add(infoPanel, BorderLayout.WEST);

        // Criação do teclado virtual
        teclado = new JPanel(new GridLayout(7, 4, 5, 5));
        teclado.setBorder(BorderFactory.createTitledBorder("ESCOLHA UMA LETRA"));
        String[] letras = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("");
        for (String letra : letras) {
            JButton button = new JButton(letra);
            button.setMargin(new Insets(1, 1, 1, 1));
            button.addActionListener(new LetraListener(letra.charAt(0)));
            teclado.add(button);
        }

        add(teclado, BorderLayout.CENTER);

        // Ações dos botões
        novoJogoButton.addActionListener(e -> reiniciarJogo());
        voltarMenuButton.addActionListener(e -> mostrarMenuInicial());
        sairJogoButton.addActionListener(e -> sairDoJogo());
        resetarPontuacaoButton.addActionListener(e -> resetarPontuacao());

        atualizarInterface();
    }

    /**
     * Reinicia o jogo atual com uma nova palavra.
     */
    private void reiniciarJogo() {
        try {
            jogo.marcarPalavraComoUsada();
            jogo.novoJogo();
            atualizarInterface();
            habilitarTeclado(true);
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            mostrarSelecaoTamanhoPalavra();
        }
    }

    /**
     * Atualiza todos os elementos visuais da interface com o estado atual do jogo.
     */
    private void atualizarInterface() {
        forcaPanel.atualizar(jogo.getForca());
        palavraLabel.setText("Palavra: " + jogo.getPalavraEscondida());
        letrasDigitadasLabel.setText("Guesses: " + jogo.getLetrasDigitadas());
        vitoriasLabel.setText("Hits: " + jogo.getVitorias());
        derrotasLabel.setText("Fails: " + jogo.getDerrotas());
        pontuacaoLabel.setText("Pontuacao: " + jogo.getPontuacao());
    }

    /**
     * Habilita ou desabilita os botões do teclado virtual.
     * 
     * @param habilitar True para habilitar, false para desabilitar
     */
    private void habilitarTeclado(boolean habilitar) {
        for (Component c : teclado.getComponents()) {
            if (c instanceof JButton) {
                c.setEnabled(habilitar);
            }
        }
    }

    /**
     * Finaliza o jogo, atualiza a pontuação e exibe o resultado.
     */
    private void fimDeJogo() {
        habilitarTeclado(false);
        boolean vitoria = !jogo.getPalavraEscondida().contains("_");
        jogo.atualizarPontuacao();
        jogo.marcarPalavraComoUsada();
        JOptionPane.showMessageDialog(this,
            vitoria ? "Parabéns, você ganhou!" : "Você perdeu!");
        atualizarInterface();
    }

    /**
     * Reseta a pontuação do jogador e limpa as palavras usadas.
     */
    private void resetarPontuacao() {
        jogo.resetarPontuacao();
        jogo.limparPalavrasUsadas();
        JOptionPane.showMessageDialog(this, "Pontuação e palavras usadas resetadas.");
        atualizarInterface();
    }

    /**
     * Encerra o jogo, exibindo as estatísticas finais.
     */
    private void sairDoJogo() {
        String estatisticas = "Estatísticas do Jogo:\n" +
                             "Vitórias: " + jogo.getVitorias() + "\n" +
                             "Derrotas: " + jogo.getDerrotas() + "\n" +
                             "Pontuação Total: " + jogo.getPontuacao();
        JOptionPane.showMessageDialog(this, estatisticas, "Estatísticas", JOptionPane.INFORMATION_MESSAGE);
        jogo.salvarJogadores();
        System.exit(0);
    }

    /**
     * Classe interna que trata os eventos de clique nas letras do teclado virtual.
     */
    private class LetraListener implements ActionListener {
        private char letra;  // Letra associada ao botão

        /**
         * Construtor do listener.
         * 
         * @param letra A letra do botão
         */
        public LetraListener(char letra) {
            this.letra = letra;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            jogo.adivinharLetra(letra);
            atualizarInterface();

            if (jogo.jogoAcabou()) {
                fimDeJogo();
            }

            ((JButton)e.getSource()).setEnabled(false);
        }
    }

    /**
     * Método principal que inicia o jogo.
     * 
     * @param args Argumentos da linha de comando (espera o caminho do arquivo de palavras)                  
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            JOptionPane.showMessageDialog(null, 
                "Por favor, forneça o caminho do arquivo de palavras.", 
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        String bancoPalavrasPath = args[0];
        File bancoPalavrasFile = new File(bancoPalavrasPath);
        
        if (!bancoPalavrasFile.exists() || !bancoPalavrasFile.isFile()) {
            JOptionPane.showMessageDialog(null, 
                "Arquivo de palavras não encontrado: " + bancoPalavrasPath, 
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> {
            JogoDaForca jogo = new JogoDaForca(bancoPalavrasPath);
            jogo.setVisible(true);
        });
    }
}

/**
 * Painel personalizado para desenhar a forca graficamente.
 */
class ForcaPanel extends JPanel {
    private Forca forca;  // Estado da forca a ser desenhado

    /**
     * Construtor do painel da forca.
     * 
     * @param forca Objeto Forca contendo o estado atual
     */
    public ForcaPanel(Forca forca) {
        this.forca = forca;
        setPreferredSize(new Dimension(350, 350));
    }

    /**
     * Desenha os elementos da forca com base no número de erros.
     * 
     * @param g Objeto Graphics usado para desenho
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(55, 305, 195, 305);   // Base
        g2d.drawLine(125, 305, 125, 85);   // Poste
        g2d.drawLine(125, 85, 175, 85);    // Trave superior
        g2d.drawLine(175, 85, 175, 115);   // Corda

        int erros = forca.getErros();
        if (erros >= 1) g2d.drawLine(175, 145, 175, 205);  // Corpo
        if (erros >= 2) g2d.drawOval(155, 115, 40, 40);    // Cabeça
        if (erros >= 3) g2d.drawLine(175, 155, 145, 175);  // Braço esquerdo
        if (erros >= 4) g2d.drawLine(175, 155, 205, 175);  // Braço direito
        if (erros >= 5) g2d.drawLine(175, 205, 145, 235);  // Perna esquerda
        if (erros >= 6) g2d.drawLine(175, 205, 205, 235);  // Perna direita
        if (erros >= 7) g2d.drawLine(155, 135, 195, 135);  // Boca
    }

    /**
     * Atualiza o estado da forca e redesenha o painel.
     * 
     * @param forca Novo estado da forca
     */
    public void atualizar(Forca forca) {
        this.forca = forca;
        repaint();
    }
}