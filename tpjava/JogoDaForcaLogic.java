import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

// Classe que representa uma palavra no jogo
class Palavra {
    private String palavra;
    private String dica;
    private Set<Character> letrasAdivinhadas;
    private int dificuldade;

    public Palavra(String palavra, String dica) {
        this.palavra = palavra.toUpperCase();
        this.dica = dica;
        this.letrasAdivinhadas = new HashSet<>();
        this.dificuldade = calcularDificuldade();
    }

    private int calcularDificuldade() {
        if (palavra.length() <= 3) return 0; // Fácil
        if (palavra.length() <= 7) return 1; // Médio
        return 2; // Difícil
    }

    public String getPalavra() {
        return palavra;
    }

    public String getDica() {
        return dica;
    }

    public boolean adivinharLetra(char letra) {
        letra = Character.toUpperCase(letra);
        // Mapeamento básico para letras acentuadas
        Map<Character, Character> mapeamentoAcentos = new HashMap<>();
        mapeamentoAcentos.put('A', 'Á');
        mapeamentoAcentos.put('E', 'É');
        mapeamentoAcentos.put('I', 'Í');
        mapeamentoAcentos.put('O', 'Ó');
        mapeamentoAcentos.put('U', 'Ú');
        mapeamentoAcentos.put('C', 'Ç');

        letrasAdivinhadas.add(letra);
        // Verifica se a letra ou sua versão acentuada está na palavra
        for (int i = 0; i < palavra.length(); i++) {
            char c = palavra.charAt(i);
            if (c == letra || (mapeamentoAcentos.containsKey(letra) && c == mapeamentoAcentos.get(letra))) {
                return true;
            }
        }
        return false;
    }

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

    public boolean palavraCompleta() {
        return getPalavraEscondida().replace(" ", "").equals(palavra);
    }

    public void reiniciar() {
        letrasAdivinhadas.clear();
    }

    public int getDificuldade() {
        return dificuldade;
    }

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
    private static final int MAX_ERROS = 7;

    public Forca() {
        this.erros = 0;
    }

    public void incrementarErro() {
        erros++;
    }

    public boolean jogoAcabou() {
        return erros >= MAX_ERROS;
    }

    public int getErros() {
        return erros;
    }

    // Adicionado getter para MAX_ERROS
    public static int getMaxErros() {
        return MAX_ERROS;
    }

    public String getImagemForca() {
        return "forca" + erros + ".png";
    }

    public String getDescricaoErro() {
        switch (erros) {
            case 0: return "Forca vazia";
            case 1: return "Tronco desenhado";
            case 2: return "Cabeça desenhada";
            case 3: return "Braço esquerdo desenhado";
            case 4: return "Braço direito desenhado";
            case 5: return "Perna esquerda desenhada";
            case 6: return "Perna direita desenhada";
            case 7: return "Boneco riscado (derrota)";
            default: return "Estado inválido";
        }
    }
    
    public void reiniciar() {
        erros = 0;
    }
}

// Classe principal que gerencia a lógica do jogo
class Jogo {
    private List<Palavra> palavras;
    private Palavra palavraAtual;
    private Forca forca;
    private Set<Character> letrasDigitadas;
    private int vitorias;
    private int derrotas;
    private int totalScore; // Atributo para Total Score
    private Set<String> palavrasUsadas; // Para rastrear palavras já jogadas
    private boolean modoMultijogador;
    private int dificuldadeSelecionada;
    
    public Jogo() {
        palavras = new ArrayList<>();
        forca = new Forca();
        letrasDigitadas = new HashSet<>();
        palavrasUsadas = new HashSet<>(); // Inicializa o conjunto de palavras usadas
        vitorias = 0;
        derrotas = 0;
        totalScore = 0;
        carregarPalavras();
        carregarPontuacao();
        modoMultijogador = false;
        dificuldadeSelecionada = -1;
    }

    public boolean isModoMultijogador() {
        return modoMultijogador;
    }

    private void carregarPalavras() {
        try (BufferedReader br = new BufferedReader(new FileReader("palavras.txt"))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(",");
                if (partes.length == 2) {
                    String palavra = partes[0].trim();
                    // Verifica se a palavra tem entre 3 e 14 letras (requisito do PDF)
                    if (palavra.length() >= 3 && palavra.length() <= 14) {
                        palavras.add(new Palavra(palavra, partes[1].trim()));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void carregarPontuacao() {
        try (BufferedReader br = new BufferedReader(new FileReader("pontuacao.txt"))) {
            String linha = br.readLine();
            if (linha != null) {
                String[] partes = linha.split(",");
                if (partes.length == 3) { // Inclui totalScore
                    vitorias = Integer.parseInt(partes[0].trim());
                    derrotas = Integer.parseInt(partes[1].trim());
                    totalScore = Integer.parseInt(partes[2].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void salvarPontuacao() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("pontuacao.txt"))) {
            writer.println(vitorias + "," + derrotas + "," + totalScore);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setModoMultijogador(boolean modo) {
        this.modoMultijogador = modo;
    }

    public void setDificuldade(int dificuldade) {
        this.dificuldadeSelecionada = dificuldade;
    }

    public void setPalavraMultijogador(Palavra palavra) {
        this.palavraAtual = palavra;
    }

    public void novoJogo() {
        forca.reiniciar();
        letrasDigitadas.clear();
        if (!modoMultijogador) {
            List<Palavra> palavrasFiltradas = palavras.stream()
                .filter(p -> p.getDificuldade() == dificuldadeSelecionada)
                .filter(p -> !palavrasUsadas.contains(p.getPalavra())) // Evita palavras já usadas
                .collect(Collectors.toList());
            
            if (palavrasFiltradas.isEmpty()) {
                // Se não houver mais palavras disponíveis, reinicia o conjunto de palavras usadas
                palavrasUsadas.clear();
                palavrasFiltradas = palavras.stream()
                    .filter(p -> p.getDificuldade() == dificuldadeSelecionada)
                    .collect(Collectors.toList());
            }
            
            palavraAtual = palavrasFiltradas.get(new Random().nextInt(palavrasFiltradas.size()));
            palavrasUsadas.add(palavraAtual.getPalavra()); // Marca a palavra como usada
        }
        palavraAtual.reiniciar();
    }

    public boolean adivinharLetra(char letra) {
        letrasDigitadas.add(letra);
        if (palavraAtual.adivinharLetra(letra)) {
            return true;
        } else {
            forca.incrementarErro();
            return false;
        }
    }

    public boolean jogoAcabou() {
        return forca.jogoAcabou() || palavraAtual.palavraCompleta();
    }

    public String getPalavraEscondida() {
        return palavraAtual.getPalavraEscondida();
    }

    public String getDica() {
        return palavraAtual.getDica();
    }

    public String getLetrasDigitadas() {
        return letrasDigitadas.stream().map(String::valueOf).collect(Collectors.joining(", "));
    }

    public String getPontuacao() {
        int guesses = Forca.getMaxErros() - forca.getErros(); // Usando o getter
        return "Total Score: " + totalScore + " | Hits: " + vitorias + " | Fails: " + derrotas + " | Guesses: " + guesses;
    }

    public String getImagemForca() {
        return forca.getImagemForca();
    }

    public void atualizarPontuacao() {
        if (!palavraAtual.getPalavraEscondida().contains("_")) {
            vitorias++;
            int letras = palavraAtual.getPalavra().length();
            int erros = forca.getErros();
            totalScore += 10 + (2 * letras) - erros;
        } else {
            derrotas++;
        }
        salvarPontuacao();
    }

    public char usarDica() {
        return palavraAtual.revelarLetraAleatoria();
    }

    public void resetarPontuacao() {
        vitorias = 0;
        derrotas = 0;
        totalScore = 0;
        salvarPontuacao();
    }

    public Forca getForca() {
        return forca;
    }

    public List<Palavra> getPalavras() {
        return palavras;
    }
}