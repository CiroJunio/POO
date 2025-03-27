import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

class JogoDaForcaLogic {
    private List<Palavra> palavras; // Lista de palavras disponíveis para o jogo
    private Palavra palavraAtual;   // Palavra sendo jogada atualmente
    private Forca forca;            // Objeto que gerencia o estado da forca
    private Set<Character> letrasDigitadas; // Conjunto de letras já tentadas
    private int vitorias;           // Contador de vitórias
    private int derrotas;           // Contador de derrotas
    private int totalScore;         // Pontuação acumulada
    private Set<String> palavrasUsadas; // Conjunto de palavras já utilizadas
    private int tamanhoPalavra;     // Tamanho desejado da palavra no jogo atual
    private String bancoPalavrasPath; // Caminho do arquivo com banco de palavras

    // Construtor: inicializa as estruturas de dados e carrega informações do disco
    public JogoDaForcaLogic(String bancoPalavrasPath) {
        this.bancoPalavrasPath = bancoPalavrasPath;
        palavras = new ArrayList<>();       // ArrayList para fácil acesso randômico
        forca = new Forca();                // Instancia o objeto da forca
        letrasDigitadas = new HashSet<>();  // HashSet para O(1) na verificação de letras
        palavrasUsadas = new HashSet<>();   // HashSet para evitar duplicatas
        vitorias = 0;
        derrotas = 0;
        totalScore = 0;
        carregarPalavras();         // Carrega o banco de palavras do arquivo
        carregarPalavrasUsadas();   // Carrega palavras já usadas, se existirem
        carregarPontuacao();        // Carrega a pontuação salva
        tamanhoPalavra = -1;        // Valor inicial inválido para forçar definição
    }

    // Carrega palavras do arquivo, filtrando por tamanho (3 a 14 letras)
    private void carregarPalavras() {
        try (BufferedReader br = new BufferedReader(new FileReader(bancoPalavrasPath))) {
            String linha;
            int linhaNumero = 0;        // Para debug, caso precise rastrear linhas
            int palavrasCarregadas = 0; // Contador de palavras válidas
            
            while ((linha = br.readLine()) != null) {
                linhaNumero++;
                // Assume formato "palavra,descrição" e pega apenas a palavra
                String palavra = linha.split(",")[0].trim().toUpperCase();
                            
                // Filtra palavras fora do intervalo permitido
                if (palavra.length() >= 3 && palavra.length() <= 14) {
                    palavras.add(new Palavra(palavra));
                    palavrasCarregadas++;
                } 
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            // Exceção personalizada para facilitar depuração
            throw new IllegalStateException("Erro ao carregar palavras do arquivo: " + bancoPalavrasPath);
        }
        
        // Validação crítica: sem palavras, o jogo não funciona
        if (palavras.isEmpty()) {
            throw new IllegalStateException("Nenhuma palavra válida encontrada no arquivo: " + bancoPalavrasPath);
        }
    }

    // Carrega palavras usadas de um arquivo persistente
    private void carregarPalavrasUsadas() {
        try (BufferedReader br = new BufferedReader(new FileReader("palavras_usadas.txt"))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                palavrasUsadas.add(linha.trim().toUpperCase());
            }
        } catch (IOException e) {
            // Silencia a exceção: arquivo pode não existir na primeira execução
        }
    }

    // Salva as palavras usadas em disco para persistência
    public void salvarPalavrasUsadas() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("palavras_usadas.txt"))) {
            for (String palavra : palavrasUsadas) {
                writer.println(palavra);
            }
        } catch (IOException e) {
            e.printStackTrace(); // Log apenas, não interrompe o jogo
        }
    }

    // Carrega pontuação de um arquivo (formato: vitorias,derrotas,totalScore)
    private void carregarPontuacao() {
        try (BufferedReader br = new BufferedReader(new FileReader("pontuacao.txt"))) {
            String linha = br.readLine();
            if (linha != null) {
                String[] partes = linha.split(",");
                if (partes.length == 3) { // Valida o formato esperado
                    vitorias = Integer.parseInt(partes[0].trim());
                    derrotas = Integer.parseInt(partes[1].trim());
                    totalScore = Integer.parseInt(partes[2].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Arquivo pode não existir ainda
        }
    }

    // Salva a pontuação em disco
    public void salvarPontuacao() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("pontuacao.txt"))) {
            writer.println(vitorias + "," + derrotas + "," + totalScore);
        } catch (IOException e) {
            e.printStackTrace(); // Log apenas, não crítico
        }
    }

    // Define o tamanho da palavra para o próximo jogo
    public void setTamanhoPalavra(int tamanho) {
        this.tamanhoPalavra = tamanho;
    }

    // Inicia um novo jogo, escolhendo uma palavra aleatória do tamanho especificado
    public void novoJogo() {
        if (tamanhoPalavra < 3 || tamanhoPalavra > 14) {
            throw new IllegalStateException("Tamanho da palavra inválido: " + tamanhoPalavra);
        }
    
        // Filtra palavras pelo tamanho e exclui as já usadas
        List<Palavra> palavrasFiltradas = palavras.stream()
            .filter(p -> p.getPalavra().length() == tamanhoPalavra)
            .filter(p -> !palavrasUsadas.contains(p.getPalavra()))
            .collect(Collectors.toList());
    
        // Caso todas as palavras tenham sido usadas, reinicia o banco
        if (palavrasFiltradas.isEmpty()) {
            limparPalavrasUsadas();
            
            // Refaz o filtro após limpeza
            palavrasFiltradas = palavras.stream()
                .filter(p -> p.getPalavra().length() == tamanhoPalavra)
                .collect(Collectors.toList());
        }
    
        // Se ainda assim não houver palavras, falha
        if (palavrasFiltradas.isEmpty()) {
            throw new IllegalStateException("Nenhuma palavra disponível com " + tamanhoPalavra + " letras.");
        }
    
        // Escolhe uma palavra aleatória da lista filtrada
        palavraAtual = palavrasFiltradas.get(new Random().nextInt(palavrasFiltradas.size()));
    
        forca.reiniciar();      // Reseta a forca
        letrasDigitadas.clear(); // Limpa as letras tentadas
        palavraAtual.reiniciar(); // Reseta o estado da palavra
    }

    // Marca a palavra atual como usada
    public void marcarPalavraComoUsada() {
        if (palavraAtual != null) {
            palavrasUsadas.add(palavraAtual.getPalavra());
            salvarPalavrasUsadas();
        }
    }

    // Limpa o conjunto de palavras usadas
    public void limparPalavrasUsadas() {
        palavrasUsadas.clear();
        salvarPalavrasUsadas();
    }

    // Tenta adivinhar uma letra, retorna true se acertou
    public boolean adivinharLetra(char letra) {
        letrasDigitadas.add(letra);
        if (palavraAtual.adivinharLetra(letra)) {
            return true;
        } else {
            forca.incrementarErro(); // Incrementa erro na forca
            return false;
        }
    }

    // Verifica se o jogo terminou (vitória ou derrota)
    public boolean jogoAcabou() {
        return forca.jogoAcabou() || palavraAtual.palavraCompleta();
    }

    // Retorna a palavra com letras adivinhadas visíveis
    public String getPalavraEscondida() {
        return palavraAtual.getPalavraEscondida();
    }

    // Retorna letras digitadas como string separada por vírgulas
    public String getLetrasDigitadas() {
        return letrasDigitadas.stream().map(String::valueOf).collect(Collectors.joining(", "));
    }

    // Retorna a pontuação total como string
    public String getPontuacao() {
        return String.valueOf(totalScore);
    }

    public int getVitorias() {
        return vitorias;
    }

    public int getDerrotas() {
        return derrotas;
    }

    // Atualiza a pontuação com base no resultado do jogo
    public void atualizarPontuacao() {
        if (!palavraAtual.getPalavraEscondida().contains("_")) { // Vitória
            vitorias++;
            int letras = palavraAtual.getPalavra().length();
            int erros = forca.getErros();
            totalScore += 10 + (2 * letras) - erros; // Fórmula de pontuação
        } else { // Derrota
            derrotas++;
        }
        salvarPontuacao();
    }

    // Reseta a pontuação para zero
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

class Palavra {
    private String palavra;             // Palavra original
    private Set<Character> letrasAdivinhadas; // Letras já acertadas

    public Palavra(String palavra) {
        this.palavra = palavra.toUpperCase();
        this.letrasAdivinhadas = new HashSet<>(); // HashSet para busca eficiente
    }

    public String getPalavra() {
        return palavra;
    }

    // Tenta adivinhar uma letra, considerando acentos
    public boolean adivinharLetra(char letra) {
        letra = Character.toUpperCase(letra);
        // Mapeamento de letras com acentos para equivalência
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
            // Verifica letra exata ou equivalência com acento
            if (c == letra || (mapeamentoAcentos.containsKey(letra) && mapeamentoAcentos.get(letra).contains(c))) {
                letrasAdivinhadas.add(c);
                acertou = true;
            }
            // Verifica se a letra digitada corresponde a um grupo de acentos
            for (Map.Entry<Character, Set<Character>> entry : mapeamentoAcentos.entrySet()) {
                if (entry.getValue().contains(c) && entry.getKey() == letra) {
                    letrasAdivinhadas.add(c);
                    acertou = true;
                }
            }
        }
        return acertou;
    }

    // Gera a palavra com "_" para letras não adivinhadas
    public String getPalavraEscondida() {
        StringBuilder sb = new StringBuilder();
        for (char c : palavra.toCharArray()) {
            if (letrasAdivinhadas.contains(c)) {
                sb.append(c);
            } else {
                sb.append("_");
            }
            sb.append(" "); // Espaço para legibilidade
        }
        return sb.toString().trim();
    }

    // Verifica se todas as letras foram adivinhadas
    public boolean palavraCompleta() {
        return getPalavraEscondida().replace(" ", "").equals(palavra);
    }

    // Reinicia o estado da palavra
    public void reiniciar() {
        letrasAdivinhadas.clear();
    }
}

class Forca {
    private int erros;          // Contador de erros
    private static final int MAX_ERROS = 7; // Limite de erros até derrota

    public Forca() {
        this.erros = 0;
    }

    public void incrementarErro() {
        erros++;
    }

    // Verifica se o limite de erros foi atingido
    public boolean jogoAcabou() {
        return erros >= MAX_ERROS;
    }

    public int getErros() {
        return erros;
    }

    public static int getMaxErros() {
        return MAX_ERROS;
    }

    // Retorna o nome do arquivo de imagem correspondente ao estado
    public String getImagemForca() {
        return "forca" + erros + ".png"; // Assume arquivos como forca0.png, forca1.png, etc.
    }

    // Descrição textual do progresso da forca
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
            default: return "Estado inválido"; // Caso de borda improvável
        }
    }

    // Reseta o estado da forca
    public void reiniciar() {
        erros = 0;
    }
}