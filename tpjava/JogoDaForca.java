import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

// Classe principal que representa a interface gráfica do Jogo da Forca
public class JogoDaForca extends JFrame {
    // Componentes da interface gráfica
    private ForcaPanel forcaPanel;
    private JLabel dicaLabel;
    private JLabel palavraLabel;
    private JLabel letrasDigitadasLabel;
    private JButton novoJogoButton;
    private JButton voltarMenuButton;
    private JPanel teclado;
    private JLabel pontuacaoLabel;
    private JButton dicaButton;
    private JButton resetarPontuacaoButton;

    // Instância da lógica do jogo
    private Jogo jogo;

    // Construtor da classe JogoDaForca
    public JogoDaForca() {
        setTitle("Jogo da Forca");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Inicializa a lógica do jogo
        jogo = new Jogo();

        // Exibe o menu inicial
        mostrarMenuInicial();
    }

    // Método para exibir o menu inicial
    private void mostrarMenuInicial() {
        getContentPane().removeAll();
        setLayout(new GridLayout(3, 1));

        JButton jogarSozinhoButton = new JButton("Jogar Sozinho");
        JButton jogarEmDuplaButton = new JButton("Jogar em Dupla");

        jogarSozinhoButton.addActionListener(e -> mostrarSelecaoDificuldade());
        jogarEmDuplaButton.addActionListener(e -> mostrarSelecaoPalavra());

        add(new JLabel("Escolha o modo de jogo:", SwingConstants.CENTER));
        add(jogarSozinhoButton);
        add(jogarEmDuplaButton);

        revalidate();
        repaint();
    }

    // Método para exibir a seleção de dificuldade
    private void mostrarSelecaoDificuldade() {
        getContentPane().removeAll();
        setLayout(new GridLayout(5, 1));

        JButton facilButton = new JButton("Facil");
        JButton medioButton = new JButton("Medio");
        JButton dificilButton = new JButton("Dificil");
        JButton voltarButton = new JButton("Voltar ao Menu");

        facilButton.addActionListener(e -> iniciarJogo(0));
        medioButton.addActionListener(e -> iniciarJogo(1));
        dificilButton.addActionListener(e -> iniciarJogo(2));
        voltarButton.addActionListener(e -> mostrarMenuInicial());

        add(new JLabel("Escolha a dificuldade:", SwingConstants.CENTER));
        add(facilButton);
        add(medioButton);
        add(dificilButton);
        add(voltarButton);

        revalidate();
        repaint();
    }

    // Método para exibir a seleção de palavra (modo multijogador)
    private void mostrarSelecaoPalavra() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        JPanel palavrasPanel = new JPanel(new GridLayout(0, 5, 5, 5));
        JScrollPane scrollPane = new JScrollPane(palavrasPanel);

        JLabel instrucaoLabel = new JLabel("Escolha uma palavra:", SwingConstants.CENTER);
        add(instrucaoLabel, BorderLayout.NORTH);

        List<Palavra> palavras = jogo.getPalavras();
        for (Palavra palavra : palavras) {
            JButton palavraButton = new JButton(palavra.getPalavra());
            palavraButton.addActionListener(e -> {
                jogo.setModoMultijogador(true);
                jogo.setPalavraMultijogador(palavra);
                iniciarJogo(-1);
            });
            palavrasPanel.add(palavraButton);
        }

        JButton voltarButton = new JButton("Voltar ao Menu");
        voltarButton.addActionListener(e -> mostrarMenuInicial());

        add(scrollPane, BorderLayout.CENTER);
        add(voltarButton, BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    // Método para iniciar o jogo
    private void iniciarJogo(int dificuldade) {
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        if (dificuldade >= 0) {
            jogo.setModoMultijogador(false);
            jogo.setDificuldade(dificuldade);
        } else {
            jogo.setModoMultijogador(true);
        }
        
        jogo.novoJogo();

        criarComponentes();

        revalidate();
        repaint();
    }

    // Método para criar os componentes da interface do jogo
    private void criarComponentes() {
        // Cria o painel superior
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 50, 10, 10);

        forcaPanel = new ForcaPanel(jogo.getForca());
        forcaPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        forcaPanel.setPreferredSize(new Dimension(350, 350)); // Ajustado para o tamanho atual
        topPanel.add(forcaPanel, gbc);

        add(topPanel, BorderLayout.NORTH);

        // Cria o painel de informações
        JPanel infoPanel = new JPanel(new GridLayout(8, 1));
        infoPanel.setBorder(BorderFactory.createTitledBorder("INFORMACOES"));

        dicaLabel = new JLabel("Dica: ");
        palavraLabel = new JLabel("Palavra: ");
        letrasDigitadasLabel = new JLabel("Letras Digitadas:");
        pontuacaoLabel = new JLabel("Pontuacao: ");
        novoJogoButton = new JButton("Novo Jogo");
        dicaButton = new JButton("Usar Dica");
        voltarMenuButton = new JButton("Voltar ao Menu");
        resetarPontuacaoButton = new JButton("Resetar Pontuacao");

        infoPanel.add(dicaLabel);
        infoPanel.add(palavraLabel);
        infoPanel.add(letrasDigitadasLabel);
        infoPanel.add(pontuacaoLabel);
        infoPanel.add(novoJogoButton);
        infoPanel.add(dicaButton);
        infoPanel.add(voltarMenuButton);
        infoPanel.add(resetarPontuacaoButton);

        add(infoPanel, BorderLayout.WEST);

        // Cria o teclado virtual
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

        // Adiciona listeners aos botões
        novoJogoButton.addActionListener(e -> reiniciarJogo());
        dicaButton.addActionListener(e -> usarDica());
        voltarMenuButton.addActionListener(e -> mostrarMenuInicial());
        resetarPontuacaoButton.addActionListener(e -> resetarPontuacao());

        atualizarInterface();
    }

    // Método para reiniciar o jogo
    private void reiniciarJogo() {
        if (jogo.isModoMultijogador()) {
            mostrarSelecaoPalavra();
        } else {
            jogo.novoJogo();
            atualizarInterface();
            habilitarTeclado(true);
            dicaButton.setEnabled(true);
        }
    }

    // Método para atualizar a interface do jogo
    private void atualizarInterface() {
        forcaPanel.atualizar(jogo.getForca());
        dicaLabel.setText("Dica: " + jogo.getDica());
        palavraLabel.setText("Palavra: " + jogo.getPalavraEscondida());
        letrasDigitadasLabel.setText("Letras Digitadas: " + jogo.getLetrasDigitadas());
        pontuacaoLabel.setText(jogo.getPontuacao()); // Agora exibe Total Score, Hits, Fails e Guesses
        dicaButton.setEnabled(!jogo.jogoAcabou());
    }

    // Método para habilitar ou desabilitar o teclado virtual
    private void habilitarTeclado(boolean habilitar) {
        for (Component c : teclado.getComponents()) {
            if (c instanceof JButton) {
                c.setEnabled(habilitar);
            }
        }
    }

    // Método para usar uma dica
    private void usarDica() {
        char letraRevelada = jogo.usarDica();
        if (letraRevelada != ' ') {
            JOptionPane.showMessageDialog(this, "A dica revelou a letra: " + letraRevelada);
            atualizarInterface();
            if (jogo.jogoAcabou()) {
                fimDeJogo();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Não há mais letras para revelar!");
            dicaButton.setEnabled(false);
        }
    }

    // Método chamado quando o jogo termina
    private void fimDeJogo() {
        habilitarTeclado(false);
        dicaButton.setEnabled(false);
        boolean vitoria = !jogo.getPalavraEscondida().contains("_");
        jogo.atualizarPontuacao();
        JOptionPane.showMessageDialog(this,
            vitoria ? "Parabéns, você ganhou!" : "Você perdeu!");
        atualizarInterface();
    }

    // Método para resetar a pontuação
    private void resetarPontuacao() {
        jogo.resetarPontuacao();
        atualizarInterface();
        JOptionPane.showMessageDialog(this, "Pontuação resetada.");
    }

    // Classe interna para lidar com os cliques nas letras do teclado virtual
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

    // Método principal para iniciar o jogo
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JogoDaForca jogo = new JogoDaForca();
            jogo.setVisible(true);
        });
    }
}

// Painel personalizado para desenhar a forca
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

        // Ajusta as coordenadas para centralizar o boneco
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