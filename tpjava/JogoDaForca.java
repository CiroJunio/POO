import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

public class JogoDaForca extends JFrame {
    private ForcaPanel forcaPanel;
    private JLabel palavraLabel;
    private JLabel letrasDigitadasLabel;
    private JLabel vitoriasLabel;
    private JLabel derrotasLabel;
    private JLabel pontuacaoLabel;
    private JButton novoJogoButton;
    private JButton voltarMenuButton;
    private JButton sairJogoButton;
    private JPanel teclado;
    private JButton resetarPontuacaoButton;

    private JogoDaForcaLogic jogo;

    public JogoDaForca(String bancoPalavraspath) {
        setTitle("Jogo da Forca");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        jogo = new JogoDaForcaLogic(bancoPalavraspath);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                jogo.salvarJogadores();
            }
        });

        mostrarTelaLogin();
    }

    private void mostrarTelaLogin() {
        getContentPane().removeAll();
        setLayout(new GridLayout(5, 1));

        JLabel instrucaoLabel = new JLabel("Selecione ou cadastre um jogador:", SwingConstants.CENTER);
        add(instrucaoLabel);

        JComboBox<Jogador> jogadoresComboBox = new JComboBox<>();
        List<Jogador> jogadores = jogo.getJogadores();
        for (Jogador jogador : jogadores) {
            jogadoresComboBox.addItem(jogador);
        }
        add(jogadoresComboBox);

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

    private void mostrarSelecaoTamanhoPalavra() {
        getContentPane().removeAll();
        setLayout(new GridLayout(0, 1));

        JLabel instrucaoLabel = new JLabel("Escolha o tamanho da palavra (3 a 14 letras):", SwingConstants.CENTER);
        add(instrucaoLabel);

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

    private void iniciarJogoComTamanho(int tamanho) {
        jogo.setTamanhoPalavra(tamanho);
        iniciarJogo(-1);
    }

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

        novoJogoButton.addActionListener(e -> reiniciarJogo());
        voltarMenuButton.addActionListener(e -> mostrarMenuInicial());
        sairJogoButton.addActionListener(e -> sairDoJogo());
        resetarPontuacaoButton.addActionListener(e -> resetarPontuacao());

        atualizarInterface();
    }

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

    private void atualizarInterface() {
        forcaPanel.atualizar(jogo.getForca());
        palavraLabel.setText("Palavra: " + jogo.getPalavraEscondida());
        letrasDigitadasLabel.setText("Guesses: " + jogo.getLetrasDigitadas());
        vitoriasLabel.setText("Hits: " + jogo.getVitorias());
        derrotasLabel.setText("Fails: " + jogo.getDerrotas());
        pontuacaoLabel.setText("Pontuacao: " + jogo.getPontuacao());
    }

    private void habilitarTeclado(boolean habilitar) {
        for (Component c : teclado.getComponents()) {
            if (c instanceof JButton) {
                c.setEnabled(habilitar);
            }
        }
    }

    private void fimDeJogo() {
        habilitarTeclado(false);
        boolean vitoria = !jogo.getPalavraEscondida().contains("_");
        jogo.atualizarPontuacao();
        jogo.marcarPalavraComoUsada();
        JOptionPane.showMessageDialog(this,
            vitoria ? "Parabéns, você ganhou!" : "Você perdeu!");
        atualizarInterface();
    }

    private void resetarPontuacao() {
        jogo.resetarPontuacao();
        jogo.limparPalavrasUsadas();
        JOptionPane.showMessageDialog(this, "Pontuação e palavras usadas resetadas.");
        atualizarInterface();
    }

    private void sairDoJogo() {
        String estatisticas = "Estatísticas do Jogo:\n" +
                             "Vitórias: " + jogo.getVitorias() + "\n" +
                             "Derrotas: " + jogo.getDerrotas() + "\n" +
                             "Pontuação Total: " + jogo.getPontuacao();
        JOptionPane.showMessageDialog(this, estatisticas, "Estatísticas", JOptionPane.INFORMATION_MESSAGE);
        jogo.salvarJogadores();
        System.exit(0);
    }

    private class LetraListener implements ActionListener {
        private char letra;

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

class ForcaPanel extends JPanel {
    private Forca forca;

    public ForcaPanel(Forca forca) {
        this.forca = forca;
        setPreferredSize(new Dimension(350, 350));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(55, 305, 195, 305);
        g2d.drawLine(125, 305, 125, 85);
        g2d.drawLine(125, 85, 175, 85);
        g2d.drawLine(175, 85, 175, 115);

        int erros = forca.getErros();
        if (erros >= 1) g2d.drawLine(175, 145, 175, 205);
        if (erros >= 2) g2d.drawOval(155, 115, 40, 40);
        if (erros >= 3) g2d.drawLine(175, 155, 145, 175);
        if (erros >= 4) g2d.drawLine(175, 155, 205, 175);
        if (erros >= 5) g2d.drawLine(175, 205, 145, 235);
        if (erros >= 6) g2d.drawLine(175, 205, 205, 235);
        if (erros >= 7) g2d.drawLine(155, 135, 195, 135);
    }

    public void atualizar(Forca forca) {
        this.forca = forca;
        repaint();
    }
}
