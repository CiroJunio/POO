import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

// Classe que representa uma palavra no jogo
class Palavra {
    private String palavra;
    private String dica;
    private Set<Character> letrasAdivinhadas;
    private int dificuldade;

    // Construtor da classe Palavra
    public Palavra(String palavra, String dica) {
        this.palavra = palavra.toUpperCase();
        this.dica = dica;
        this.letrasAdivinhadas = new HashSet<>();
        this.dificuldade = calcularDificuldade();
    }

    // Calcula a dificuldade da palavra baseado no seu comprimento
    private int calcularDificuldade() {
        if (palavra.length() <= 3) return 0; // Facil
        if (palavra.length() <= 7) return 1; // Medio
        return 2; // Dificil
    }

    // Getters
    public String getPalavra() {
        return palavra;
    }

    public String getDica() {
        return dica;
    }

    // Verifica se a letra adivinhada esta na palavra
    public boolean adivinharLetra(char letra) {
        letra = Character.toUpperCase(letra);
        letrasAdivinhadas.add(letra);
        return palavra.indexOf(letra) != -1;
    }

    // Retorna a palavra com as letras adivinhadas reveladas e as demais ocultas
    public String getPalavraEscondida() {
        StringBuilder sb = new StringBuilder();
        for (char c : palavra.toCharArray()) {
            if (letrasAdivinhadas.contains(c)) {
                sb.append(c);
            } else {
                sb.append("_");
            }
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    // Verifica se todas as letras da palavra foram adivinhadas
    public boolean palavraCompleta() {
        return getPalavraEscondida().replace(" ", "").equals(palavra);
    }

    // Reinicia o conjunto de letras adivinhadas
    public void reiniciar() {
        letrasAdivinhadas.clear();
    }

    // Retorna a dificuldade da palavra
    public int getDificuldade() {
        return dificuldade;
    }

    // Revela uma letra aleatoria que ainda nao foi adivinhada
    public char revelarLetraAleatoria() {
        List<Character> letrasNaoAdivinhadas = new ArrayList<>();
        for (char c : palavra.toCharArray()) {
            if (!letrasAdivinhadas.contains(c)) {
                letrasNaoAdivinhadas.add(c);
            }
        }
        if (!letrasNaoAdivinhadas.isEmpty()) {
            char letraRevelada = letrasNaoAdivinhadas.get(new Random().nextInt(letrasNaoAdivinhadas.size()));
            letrasAdivinhadas.add(letraRevelada);
            return letraRevelada;
        }
        return ' ';
    }
}

// Classe que representa a forca no jogo
class Forca {
    private int erros;
    private static final int MAX_ERROS = 5;

    // Construtor da classe Forca
    public Forca() {
        this.erros = 0;
    }

    // Incrementa o numero de erros
    public void incrementarErro() {
        erros++;
    }

    // Verifica se o jogo acabou (numero maximo de erros atingido)
    public boolean jogoAcabou() {
        return erros >= MAX_ERROS;
    }

    // Retorna o numero de erros
    public int getErros() {
        return erros;
    }

    // Retorna o nome do arquivo de imagem correspondente ao estado atual da forca
    public String getImagemForca() {
        return "forca" + erros + ".png";
    }

    // Reinicia o contador de erros
    public void reiniciar() {
        erros = 0;
    }
}

// Classe principal que gerencia a logica do jogo
class Jogo {
    private List<Palavra> palavras;
    private Palavra palavraAtual;
    private Forca forca;
    private Set<Character> letrasDigitadas;
    private int vitorias;
    private int derrotas;
    private boolean modoMultijogador;
    private int dificuldadeSelecionada;
    
    // Construtor da classe Jogo
    public Jogo() {
        palavras = new ArrayList<>();
        forca = new Forca();
        letrasDigitadas = new HashSet<>();
        carregarPalavras();
        carregarPontuacao();
        modoMultijogador = false;
        dificuldadeSelecionada = -1;
    }

    // Verifica se o jogo esta em modo multijogador
    public boolean isModoMultijogador() {
        return modoMultijogador;
    }

    // Carrega as palavras do arquivo "palavras.txt"
    private void carregarPalavras() {
        try (BufferedReader br = new BufferedReader(new FileReader("palavras.txt"))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(",");
                if (partes.length == 2) {
                    palavras.add(new Palavra(partes[0].trim(), partes[1].trim()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Carrega a pontuacao do arquivo "pontuacao.txt"
    private void carregarPontuacao() {
        try (BufferedReader br = new BufferedReader(new FileReader("pontuacao.txt"))) {
            String linha = br.readLine();
            if (linha != null) {
                String[] partes = linha.split(",");
                if (partes.length == 2) {
                    vitorias = Integer.parseInt(partes[0].trim());
                    derrotas = Integer.parseInt(partes[1].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Salva a pontuacao no arquivo "pontuacao.txt"
    public void salvarPontuacao() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("pontuacao.txt"))) {
            writer.println(vitorias + "," + derrotas);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Define o modo de jogo (single player ou multijogador)
    public void setModoMultijogador(boolean modo) {
        this.modoMultijogador = modo;
    }

    // Define a dificuldade do jogo
    public void setDificuldade(int dificuldade) {
        this.dificuldadeSelecionada = dificuldade;
    }

    // Define a palavra para o modo multijogador
    public void setPalavraMultijogador(Palavra palavra) {
        this.palavraAtual = palavra;
    }

    // Inicia um novo jogo
    public void novoJogo() {
        forca.reiniciar();
        letrasDigitadas.clear();
        if (!modoMultijogador) {
            List<Palavra> palavrasFiltradas = palavras.stream()
                .filter(p -> p.getDificuldade() == dificuldadeSelecionada)
                .collect(Collectors.toList());
            
            palavraAtual = palavrasFiltradas.get(new Random().nextInt(palavrasFiltradas.size()));
        }
        palavraAtual.reiniciar();
    }

    // Processa a tentativa de adivinhar uma letra
    public boolean adivinharLetra(char letra) {
        letrasDigitadas.add(letra);
        if (palavraAtual.adivinharLetra(letra)) {
            return true;
        } else {
            forca.incrementarErro();
            return false;
        }
    }

    // Verifica se o jogo acabou
    public boolean jogoAcabou() {
        return forca.jogoAcabou() || palavraAtual.palavraCompleta();
    }

    // Retorna a palavra atual com as letras adivinhadas reveladas
    public String getPalavraEscondida() {
        return palavraAtual.getPalavraEscondida();
    }

    // Retorna a dica da palavra atual
    public String getDica() {
        return palavraAtual.getDica();
    }

    // Retorna as letras ja digitadas
    public String getLetrasDigitadas() {
        return letrasDigitadas.stream().map(String::valueOf).collect(Collectors.joining(", "));
    }

    // Retorna a pontuacao atual
    public String getPontuacao() {
        return "Vitorias: " + vitorias + " | Derrotas: " + derrotas;
    }

    // Retorna o nome do arquivo de imagem da forca
    public String getImagemForca() {
        return forca.getImagemForca();
    }

    // Atualiza a pontuacao apos o fim do jogo
    public void atualizarPontuacao() {
        if (!palavraAtual.getPalavraEscondida().contains("_")) {
            vitorias++;
        } else {
            derrotas++;
        }
        salvarPontuacao();
    }

    // Revela uma letra aleatoria como dica
    public char usarDica() {
        return palavraAtual.revelarLetraAleatoria();
    }

    // Reseta a pontuacao
    public void resetarPontuacao() {
        vitorias = 0;
        derrotas = 0;
        salvarPontuacao();
    }

    // Retorna a lista de palavras
    public List<Palavra> getPalavras() {
        return palavras;
    }
}
