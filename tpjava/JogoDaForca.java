import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class JogoDaForca extends JFrame {
    private ForcaPanel forcaPanel; // Painel que desenha a forca graficamente
    private JLabel palavraLabel; // Exibe a palavra com underscores para letras não adivinhadas
    private JLabel letrasDigitadasLabel; // Mostra as letras já tentadas pelo jogador
    private JLabel vitoriasLabel; // Contador de vitórias
    private JLabel derrotasLabel; // Contador de derrotas
    private JLabel pontuacaoLabel; // Exibe a pontuação acumulada
    private JButton novoJogoButton; // Botão para iniciar um novo jogo
    private JButton voltarMenuButton; // Botão para retornar ao menu inicial
    private JButton sairJogoButton; // Botão para encerrar o programa
    private JPanel teclado; // Painel contendo os botões de letras (A-Z)
    private JButton resetarPontuacaoButton; // Botão para zerar pontuação e palavras usadas

    private JogoDaForcaLogic jogo; // Instância da lógica do jogo (separada da interface)

    public JogoDaForca(String bancoPalavraspath) {
        setTitle("Jogo da Forca");
        setSize(1000, 750); // Define o tamanho da janela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Fecha o programa ao clicar no X
        setLayout(new BorderLayout()); // Layout principal da janela

        jogo = new JogoDaForcaLogic(bancoPalavraspath); // Inicializa a lógica com o arquivo de palavras

        // Listener para salvar palavras usadas quando o jogo é fechado
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                jogo.salvarPalavrasUsadas(); // Persiste as palavras usadas ao fechar
            }
        });

        mostrarMenuInicial(); // Exibe o menu inicial ao iniciar o programa
    }

    private void mostrarMenuInicial() {
        getContentPane().removeAll(); // Limpa a janela para exibir o menu
        setLayout(new GridLayout(3, 1)); // Layout em grade com 3 linhas

        JButton jogarButton = new JButton("Jogar");
        JButton sairButton = new JButton("Sair do Jogo");

        jogarButton.addActionListener(e -> mostrarSelecaoTamanhoPalavra()); // Vai para a seleção de tamanho
        sairButton.addActionListener(e -> sairDoJogo()); // Encerra o programa

        add(new JLabel("Bem-vindo ao Jogo da Forca!", SwingConstants.CENTER)); // Título centralizado
        add(jogarButton);
        add(sairButton);

        revalidate(); // Atualiza o layout
        repaint(); // Redesenha a janela
    }

    private void mostrarSelecaoTamanhoPalavra() {
        getContentPane().removeAll(); // Limpa a janela para a nova tela
        setLayout(new GridLayout(0, 1)); // Layout em coluna única

        JLabel instrucaoLabel = new JLabel("Escolha o tamanho da palavra (3 a 14 letras):", SwingConstants.CENTER);
        add(instrucaoLabel);

        // Cria botões para cada tamanho de palavra (3 a 14 letras)
        for (int i = 3; i <= 14; i++) {
            JButton tamanhoButton = new JButton(i + " letras");
            final int tamanho = i; // Variável final para uso no ActionListener
            tamanhoButton.addActionListener(e -> iniciarJogoComTamanho(tamanho)); // Inicia jogo com o tamanho escolhido
            add(tamanhoButton);
        }

        JButton resetarPontuacaoButton = new JButton("Resetar Pontuacao");
        resetarPontuacaoButton.addActionListener(e -> resetarPontuacao()); // Zera pontuação e palavras usadas
        add(resetarPontuacaoButton);

        JButton voltarButton = new JButton("Voltar ao Menu");
        voltarButton.addActionListener(e -> mostrarMenuInicial()); // Retorna ao menu inicial
        add(voltarButton);

        revalidate();
        repaint();
    }

    private void iniciarJogoComTamanho(int tamanho) {
        jogo.setTamanhoPalavra(tamanho); // Define o tamanho da palavra na lógica
        iniciarJogo(-1); // Inicia o jogo (o parâmetro unused é ignorado)
    }

    private void iniciarJogo(int unused) {
        getContentPane().removeAll(); // Limpa a janela para a tela do jogo
        setLayout(new BorderLayout());

        try {
            jogo.novoJogo(); // Inicia uma nova partida na lógica
            criarComponentes(); // Monta a interface do jogo
        } catch (IllegalStateException e) {
            // Caso não haja palavras disponíveis, exibe mensagem e volta à seleção
            JOptionPane.showMessageDialog(this, e.getMessage());
            mostrarSelecaoTamanhoPalavra();
            return;
        }

        revalidate();
        repaint();
    }

    private void criarComponentes() {
        // Painel superior com a forca
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 50, 10, 10); // Espaçamento ao redor do painel

        forcaPanel = new ForcaPanel(jogo.getForca()); // Painel que desenha a forca
        forcaPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Borda preta
        forcaPanel.setPreferredSize(new Dimension(350, 350)); // Tamanho fixo
        topPanel.add(forcaPanel, gbc);

        add(topPanel, BorderLayout.NORTH); // Adiciona o painel ao topo

        // Painel de informações à esquerda
        JPanel infoPanel = new JPanel(new GridLayout(8, 1)); // 8 linhas para todos os componentes
        infoPanel.setBorder(BorderFactory.createTitledBorder("INFORMACOES")); // Borda com título

        palavraLabel = new JLabel("Palavra: ");
        letrasDigitadasLabel = new JLabel("Letras Digitadas: ");
        vitoriasLabel = new JLabel("Hits: ");
        derrotasLabel = new JLabel("Fails: ");
        pontuacaoLabel = new JLabel("Pontuacao: ");
        novoJogoButton = new JButton("Novo Jogo");
        voltarMenuButton = new JButton("Voltar ao Menu");
        sairJogoButton = new JButton("Sair do Jogo");
        resetarPontuacaoButton = new JButton("Resetar Pontuacao");

        // Adiciona todos os componentes ao painel de informações
        infoPanel.add(palavraLabel);
        infoPanel.add(letrasDigitadasLabel);
        infoPanel.add(vitoriasLabel);
        infoPanel.add(derrotasLabel);
        infoPanel.add(pontuacaoLabel);
        infoPanel.add(novoJogoButton);
        infoPanel.add(voltarMenuButton);
        infoPanel.add(sairJogoButton);
        infoPanel.add(resetarPontuacaoButton);

        add(infoPanel, BorderLayout.WEST); // Adiciona o painel à esquerda

        // Painel do teclado com botões de letras
        teclado = new JPanel(new GridLayout(7, 4, 5, 5)); // 7 linhas, 4 colunas, espaçamento de 5px
        teclado.setBorder(BorderFactory.createTitledBorder("ESCOLHA UMA LETRA"));
        String[] letras = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split(""); // Array com letras A-Z
        for (String letra : letras) {
            JButton button = new JButton(letra);
            button.setMargin(new Insets(1, 1, 1, 1)); // Margem mínima nos botões
            button.addActionListener(new LetraListener(letra.charAt(0))); // Listener para cada letra
            teclado.add(button);
        }

        add(teclado, BorderLayout.CENTER); // Adiciona o teclado ao centro

        // Listeners dos botões de ação
        novoJogoButton.addActionListener(e -> reiniciarJogo());
        voltarMenuButton.addActionListener(e -> mostrarMenuInicial());
        sairJogoButton.addActionListener(e -> sairDoJogo());
        resetarPontuacaoButton.addActionListener(e -> resetarPontuacao());

        atualizarInterface(); // Atualiza a interface com os dados iniciais
    }

    private void reiniciarJogo() {
        try {
            jogo.marcarPalavraComoUsada(); // Marca a palavra atual como usada
            jogo.novoJogo(); // Inicia um novo jogo
            atualizarInterface(); // Atualiza a interface
            habilitarTeclado(true); // Reativa o teclado
        } catch (IllegalStateException e) {
            // Caso não haja mais palavras, exibe mensagem e volta à seleção
            JOptionPane.showMessageDialog(this, e.getMessage());
            mostrarSelecaoTamanhoPalavra();
        }
    }

    private void atualizarInterface() {
        forcaPanel.atualizar(jogo.getForca()); // Atualiza o desenho da forca
        palavraLabel.setText("Palavra: " + jogo.getPalavraEscondida()); // Mostra a palavra atual
        letrasDigitadasLabel.setText("Guesses: " + jogo.getLetrasDigitadas()); // Letras tentadas
        vitoriasLabel.setText("Hits: " + jogo.getVitorias()); // Número de vitórias
        derrotasLabel.setText("Fails: " + jogo.getDerrotas()); // Número de derrotas
        pontuacaoLabel.setText("Pontuacao: " + jogo.getPontuacao()); // Pontuação total
    }

    private void habilitarTeclado(boolean habilitar) {
        // Habilita ou desabilita todos os botões do teclado
        for (Component c : teclado.getComponents()) {
            if (c instanceof JButton) {
                c.setEnabled(habilitar);
            }
        }
    }

    private void fimDeJogo() {
        habilitarTeclado(false); // Desativa o teclado ao fim do jogo
        boolean vitoria = !jogo.getPalavraEscondida().contains("_"); // Verifica se o jogador ganhou
        jogo.atualizarPontuacao(); // Atualiza a pontuação com base no resultado
        jogo.marcarPalavraComoUsada(); // Marca a palavra como usada
        JOptionPane.showMessageDialog(this,
            vitoria ? "Parabéns, você ganhou!" : "Você perdeu!"); // Mensagem de vitória ou derrota
        atualizarInterface(); // Atualiza a interface com o estado final
    }

    private void resetarPontuacao() {
        jogo.resetarPontuacao(); // Zera a pontuação
        jogo.limparPalavrasUsadas(); // Limpa o registro de palavras usadas
        JOptionPane.showMessageDialog(this, "Pontuação e palavras usadas resetadas.");
        atualizarInterface(); // Atualiza a interface (se aplicável)
    }

    private void sairDoJogo() {
        // Exibe estatísticas finais antes de encerrar
        String estatisticas = "Estatísticas do Jogo:\n" +
                             "Vitórias: " + jogo.getVitorias() + "\n" +
                             "Derrotas: " + jogo.getDerrotas() + "\n" +
                             "Pontuação Total: " + jogo.getPontuacao();
        JOptionPane.showMessageDialog(this, estatisticas, "Estatísticas", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0); // Encerra o programa
    }

    private class LetraListener implements ActionListener {
        private char letra; // Letra associada ao botão

        public LetraListener(char letra) {
            this.letra = letra;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            jogo.adivinharLetra(letra); // Tenta adivinhar a letra na lógica
            atualizarInterface(); // Atualiza a interface com o novo estado

            if (jogo.jogoAcabou()) { // Verifica se o jogo terminou
                fimDeJogo(); // Executa a lógica de fim de jogo
            }

            ((JButton)e.getSource()).setEnabled(false); // Desativa o botão clicado
        }
    }

    public static void main(String[] args) {
        // Verifica se o caminho do  do arquivo de palavras foi fornecido
        if (args.length < 1) {
            JOptionPane.showMessageDialog(null, 
                "Por favor, forneça o caminho do arquivo de palavras.", 
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        String bancoPalavrasPath = args[0];
        File bancoPalavrasFile = new File(bancoPalavrasPath);
        
        // Verifica se o arquivo existe e é válido
        if (!bancoPalavrasFile.exists() || !bancoPalavrasFile.isFile()) {
            JOptionPane.showMessageDialog(null, 
                "Arquivo de palavras não encontrado: " + bancoPalavrasPath, 
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Inicia a interface gráfica em uma thread separada (padrão Swing)
        SwingUtilities.invokeLater(() -> {
            JogoDaForca jogo = new JogoDaForca(bancoPalavrasPath);
            jogo.setVisible(true); // Torna a janela visível
        });
    }
}

class ForcaPanel extends JPanel {
    private Forca forca; // Referência à lógica da forca

    public ForcaPanel(Forca forca) {
        this.forca = forca;
        setPreferredSize(new Dimension(350, 350)); // Tamanho fixo do painel
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(3)); // Define a espessura das linhas
        // Desenha a estrutura básica da forca
        g2d.drawLine(55, 305, 195, 305); // Base
        g2d.drawLine(125, 305, 125, 85); // Poste vertical
        g2d.drawLine(125, 85, 175, 85); // Trave horizontal
        g2d.drawLine(175, 85, 175, 115); // Corda

        // Desenha partes do boneco conforme o número de erros
        int erros = forca.getErros();
        if (erros >= 1) g2d.drawLine(175, 145, 175, 205); // Corpo
        if (erros >= 2) g2d.drawOval(155, 115, 40, 40); // Cabeça
        if (erros >= 3) g2d.drawLine(175, 155, 145, 175); // Braço esquerdo
        if (erros >= 4) g2d.drawLine(175, 155, 205, 175); // Braço direito
        if (erros >= 5) g2d.drawLine(175, 205, 145, 235); // Perna esquerda
        if (erros >= 6) g2d.drawLine(175, 205, 205, 235); // Perna direita
        if (erros >= 7) g2d.drawLine(155, 135, 195, 135); // Boca (opcional)
    }

    public void atualizar(Forca forca) {
        this.forca = forca; // Atualiza a referência da forca
        repaint(); // Redesenha o painel
    }
}