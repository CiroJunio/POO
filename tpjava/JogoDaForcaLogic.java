import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Classe principal que gerencia a lógica do Jogo da Forca.
 */
class JogoDaForcaLogic {
    private List<Palavra> palavras;          // Lista de palavras disponíveis
    private Palavra palavraAtual;            // Palavra atual do jogo
    private Forca forca;                     // Estado da forca
    private Set<Character> letrasDigitadas;  // Letras já tentadas
    private List<Jogador> jogadores;         // Lista de jogadores registrados
    private Jogador jogadorAtual;            // Jogador atualmente ativo
    private int tamanhoPalavra;              // Tamanho da palavra a ser usada
    private String bancoPalavrasPath;        // Caminho do arquivo de palavras

    /**
     * Construtor da lógica do jogo.
     * 
     * @param bancoPalavrasPath Caminho do arquivo contendo as palavras
     */
    public JogoDaForcaLogic(String bancoPalavrasPath) {
        this.bancoPalavrasPath = bancoPalavrasPath;
        palavras = new ArrayList<>();
        forca = new Forca();
        letrasDigitadas = new HashSet<>();
        jogadores = new ArrayList<>();
        carregarPalavras();
        carregarJogadores();
        tamanhoPalavra = -1;
    }

    /**
     * Carrega as palavras do arquivo especificado no bancoPalavrasPath.
     * 
     * @throws IllegalStateException se houver erro na leitura ou nenhuma palavra válida for encontrada
     */
    private void carregarPalavras() {
        try (BufferedReader br = new BufferedReader(new FileReader(bancoPalavrasPath))) {
            String linha;
            int linhaNumero = 0;
            int palavrasCarregadas = 0;
            
            while ((linha = br.readLine()) != null) {
                linhaNumero++;
                String palavra = linha.split(",")[0].trim().toUpperCase();
                            
                if (palavra.length() >= 3 && palavra.length() <= 14) {
                    palavras.add(new Palavra(palavra));
                    palavrasCarregadas++;
                } 
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Erro ao carregar palavras do arquivo: " + bancoPalavrasPath);
        }
        
        if (palavras.isEmpty()) {
            throw new IllegalStateException("Nenhuma palavra válida encontrada no arquivo: " + bancoPalavrasPath);
        }
    }

    /**
     * Carrega os jogadores salvos do arquivo "jogadores.txt".
     */
    private void carregarJogadores() {
        try (BufferedReader br = new BufferedReader(new FileReader("jogadores.txt"))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(";");
                if (partes.length >= 4) {
                    String nome = partes[0];
                    int vitorias = Integer.parseInt(partes[1]);
                    int derrotas = Integer.parseInt(partes[2]);
                    int totalScore = Integer.parseInt(partes[3]);
                    Jogador jogador = new Jogador(nome);
                    jogador.setVitorias(vitorias);
                    jogador.setDerrotas(derrotas);
                    jogador.setTotalScore(totalScore);
                    if (partes.length > 4) {
                        String[] palavrasUsadasArray = partes[4].split(",");
                        Set<String> palavrasUsadas = new HashSet<>(Arrays.asList(palavrasUsadasArray));
                        jogador.setPalavrasUsadas(palavrasUsadas);
                    }
                    jogadores.add(jogador);
                }
            }
        } catch (IOException e) {
            // Arquivo pode não existir na primeira execução, ignorar exceção
        }
    }

    /**
     * Salva os dados dos jogadores no arquivo "jogadores.txt".
     */
    public void salvarJogadores() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("jogadores.txt"))) {
            for (Jogador jogador : jogadores) {
                String palavrasUsadasStr = String.join(",", jogador.getPalavrasUsadas());
                writer.println(jogador.getNome() + ";" + jogador.getVitorias() + ";" + jogador.getDerrotas() + ";" +
                               jogador.getTotalScore() + ";" + palavrasUsadasStr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cadastra um novo jogador se ele ainda não existir.
     * 
     * @param nome Nome do novo jogador
     */
    public void cadastrarJogador(String nome) {
        if (jogadores.stream().noneMatch(j -> j.getNome().equalsIgnoreCase(nome))) {
            Jogador novoJogador = new Jogador(nome);
            jogadores.add(novoJogador);
            salvarJogadores();
        }
    }

    /**
     * Define o jogador atual com base no nome fornecido.
     * 
     * @param nome Nome do jogador a ser definido como atual
     */
    public void setJogadorAtual(String nome) {
        jogadorAtual = jogadores.stream()
            .filter(j -> j.getNome().equalsIgnoreCase(nome))
            .findFirst()
            .orElse(null);
    }

    /**
     * Retorna o jogador atualmente ativo.
     * 
     * @return O jogador atual
     */
    public Jogador getJogadorAtual() {
        return jogadorAtual;
    }

    /**
     * Retorna a lista de todos os jogadores registrados.
     * 
     * @return Lista de jogadores
     */
    public List<Jogador> getJogadores() {
        return jogadores;
    }

    /**
     * Define o tamanho da palavra para o próximo jogo.
     * 
     * @param tamanho Tamanho desejado da palavra
     */
    public void setTamanhoPalavra(int tamanho) {
        this.tamanhoPalavra = tamanho;
    }

    /**
     * Inicia um novo jogo com uma palavra aleatória do tamanho especificado.
     * 
     * @throws IllegalStateException se o tamanho for inválido ou não houver palavras disponíveis
     */
    public void novoJogo() {
        if (tamanhoPalavra < 3 || tamanhoPalavra > 14) {
            throw new IllegalStateException("Tamanho da palavra inválido: " + tamanhoPalavra);
        }
    
        List<Palavra> palavrasFiltradas = palavras.stream()
            .filter(p -> p.getPalavra().length() == tamanhoPalavra)
            .filter(p -> !jogadorAtual.getPalavrasUsadas().contains(p.getPalavra()))
            .collect(Collectors.toList());
    
        if (palavrasFiltradas.isEmpty()) {
            throw new IllegalStateException("Nenhuma palavra disponível com " + tamanhoPalavra + " letras. Tente resetar a pontuação para liberar palavras usadas.");
        }
    
        palavraAtual = palavrasFiltradas.get(new Random().nextInt(palavrasFiltradas.size()));
    
        forca.reiniciar();
        letrasDigitadas.clear();
        palavraAtual.reiniciar();
    }

    /**
     * Marca a palavra atual como usada pelo jogador.
     */
    public void marcarPalavraComoUsada() {
        if (palavraAtual != null) {
            jogadorAtual.getPalavrasUsadas().add(palavraAtual.getPalavra());
            salvarJogadores();
        }
    }

    /**
     * Limpa o conjunto de palavras usadas pelo jogador atual.
     */
    public void limparPalavrasUsadas() {
        jogadorAtual.getPalavrasUsadas().clear();
        salvarJogadores();
    }

    /**
     * Tenta adivinhar uma letra na palavra atual.
     * 
     * @param letra A letra a ser adivinhada
     * @return true se a letra está na palavra, false caso contrário
     */
    public boolean adivinharLetra(char letra) {
        letrasDigitadas.add(letra);
        if (palavraAtual.adivinharLetra(letra)) {
            return true;
        } else {
            forca.incrementarErro();
            return false;
        }
    }

    /**
     * Verifica se o jogo terminou (vitória ou derrota).
     * 
     * @return true se o jogo acabou, false caso contrário
     */
    public boolean jogoAcabou() {
        return forca.jogoAcabou() || palavraAtual.palavraCompleta();
    }

    /**
     * Retorna a palavra atual com letras não adivinhadas ocultas.
     * 
     * @return A palavra escondida como String
     */
    public String getPalavraEscondida() {
        return palavraAtual.getPalavraEscondida();
    }

    /**
     * Retorna as letras já digitadas separadas por vírgula.
     * 
     * @return String com as letras digitadas
     */
    public String getLetrasDigitadas() {
        return letrasDigitadas.stream().map(String::valueOf).collect(Collectors.joining(", "));
    }

    /**
     * Retorna a pontuação total do jogador atual.
     * 
     * @return A pontuação como String
     */
    public String getPontuacao() {
        return String.valueOf(jogadorAtual.getTotalScore());
    }

    /**
     * Retorna o número de vitórias do jogador atual.
     * 
     * @return Total de vitórias
     */
    public int getVitorias() {
        return jogadorAtual.getVitorias();
    }

    /**
     * Retorna o número de derrotas do jogador atual.
     * 
     * @return Total de derrotas
     */
    public int getDerrotas() {
        return jogadorAtual.getDerrotas();
    }

    /**
     * Atualiza a pontuação do jogador com base no resultado do jogo.
     */
    public void atualizarPontuacao() {
        if (!palavraAtual.getPalavraEscondida().contains("_")) {
            jogadorAtual.setVitorias(jogadorAtual.getVitorias() + 1);
            int letras = palavraAtual.getPalavra().length();
            int erros = forca.getErros();
            jogadorAtual.setTotalScore(jogadorAtual.getTotalScore() + 10 + (2 * letras) - erros);
        } else {
            jogadorAtual.setDerrotas(jogadorAtual.getDerrotas() + 1);
        }
        salvarJogadores();
    }

    /**
     * Reseta as estatísticas do jogador atual (vitórias, derrotas e pontuação).
     */
    public void resetarPontuacao() {
        jogadorAtual.setVitorias(0);
        jogadorAtual.setDerrotas(0);
        jogadorAtual.setTotalScore(0);
        salvarJogadores();
    }

    /**
     * Retorna o objeto Forca atual.
     * 
     * @return O estado da forca
     */
    public Forca getForca() {
        return forca;
    }

    /**
     * Retorna a lista de palavras carregadas.
     * 
     * @return Lista de palavras
     */
    public List<Palavra> getPalavras() {
        return palavras;
    }
}

/**
 * Representa uma palavra no jogo, gerenciando letras adivinhadas.
 */
class Palavra {
    private String palavra;                 // A palavra em si
    private Set<Character> letrasAdivinhadas; // Letras já adivinhadas

    /**
     * Construtor da classe Palavra.
     * 
     * @param palavra A palavra a ser usada
     */
    public Palavra(String palavra) {
        this.palavra = palavra.toUpperCase();
        this.letrasAdivinhadas = new HashSet<>();
    }

    /**
     * Retorna a palavra original.
     * 
     * @return A palavra como String
     */
    public String getPalavra() {
        return palavra;
    }

    /**
     * Tenta adivinhar uma letra, considerando acentos.
     * 
     * @param letra A letra a ser adivinhada
     * @return true se a letra está na palavra, false caso contrário
     */
    public boolean adivinharLetra(char letra) {
        letra = Character.toUpperCase(letra);
        Map<Character, Set<Character>> mapeamentoAcentos = new HashMap<>();
        mapeamentoAcentos.put('A', new HashSet<>(Arrays.asList('A', 'Á', 'À', 'Ã', 'Â', 'Ä')));
        mapeamentoAcentos.put('E', new HashSet<>(Arrays.asList('E', 'É', 'È', 'Ê', 'Ë')));
        mapeamentoAcentos.put('I', new HashSet<>(Arrays.asList('I', 'Í', 'Ì', 'Î', 'Ï')));
        mapeamentoAcentos.put('O', new HashSet<>(Arrays.asList('O', 'Ó', 'Ò', 'Õ', 'Ô', 'Ö')));
        mapeamentoAcentos.put('U', new HashSet<>(Arrays.asList('U', 'Ú', 'Ù', 'Û', 'Ü')));
        mapeamentoAcentos.put('C', new HashSet<>(Arrays.asList('C', 'Ç')));

        letrasAdivinhadas.add(letra);
        boolean acertou = false;
        for (int i = 0; i < palavra.length(); i++) {
            char c = palavra.charAt(i);
            if (c == letra || (mapeamentoAcentos.containsKey(letra) && mapeamentoAcentos.get(letra).contains(c))) {
                letrasAdivinhadas.add(c);
                acertou = true;
            }
            for (Map.Entry<Character, Set<Character>> entry : mapeamentoAcentos.entrySet()) {
                if (entry.getValue().contains(c) && entry.getKey() == letra) {
                    letrasAdivinhadas.add(c);
                    acertou = true;
                }
            }
        }
        return acertou;
    }

    /**
     * Retorna a palavra com letras não adivinhadas substituídas por "_".
     * 
     * @return A palavra escondida como String
     */
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

    /**
     * Verifica se todas as letras da palavra foram adivinhadas.
     * 
     * @return true se a palavra está completa, false caso contrário
     */
    public boolean palavraCompleta() {
        return getPalavraEscondida().replace(" ", "").equals(palavra);
    }

    /**
     * Reinicia o estado da palavra, limpando letras adivinhadas.
     */
    public void reiniciar() {
        letrasAdivinhadas.clear();
    }
}

/**
 * Representa o estado da forca no jogo.
 */
class Forca {
    private int erros;                    // Número de erros atuais
    private static final int MAX_ERROS = 7; // Número máximo de erros permitido

    /**
     * Construtor da classe Forca.
     */
    public Forca() {
        this.erros = 0;
    }

    /**
     * Incrementa o número de erros.
     */
    public void incrementarErro() {
        erros++;
    }

    /**
     * Verifica se o jogo terminou por excesso de erros.
     * 
     * @return true se o limite de erros foi atingido, false caso contrário
     */
    public boolean jogoAcabou() {
        return erros >= MAX_ERROS;
    }

    /**
     * Retorna o número atual de erros.
     * 
     * @return Total de erros
     */
    public int getErros() {
        return erros;
    }

    /**
     * Retorna o número máximo de erros permitido.
     * 
     * @return O valor de MAX_ERROS
     */
    public static int getMaxErros() {
        return MAX_ERROS;
    }

    /**
     * Retorna o nome do arquivo de imagem correspondente ao estado da forca.
     * 
     * @return Nome do arquivo de imagem
     */
    public String getImagemForca() {
        return "forca" + erros + ".png";
    }

    /**
     * Retorna uma descrição textual do estado atual da forca.
     * 
     * @return Descrição do estado
     */
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

    /**
     * Reinicia o estado da forca, zerando os erros.
     */
    public void reiniciar() {
        erros = 0;
    }
}