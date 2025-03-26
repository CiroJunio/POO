import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

class JogoDaForcaLogic {
    private List<Palavra> palavras;
    private Palavra palavraAtual;
    private Forca forca;
    private Set<Character> letrasDigitadas;
    private int vitorias;
    private int derrotas;
    private int totalScore;
    private Set<String> palavrasUsadas;
    private int tamanhoPalavra;
    private String bancoPalavrasPath;

    public JogoDaForcaLogic(String bancoPalavrasPath) {
        this.bancoPalavrasPath = bancoPalavrasPath;
        palavras = new ArrayList<>();
        forca = new Forca();
        letrasDigitadas = new HashSet<>();
        palavrasUsadas = new HashSet<>();
        vitorias = 0;
        derrotas = 0;
        totalScore = 0;
        carregarPalavras();
        carregarPalavrasUsadas();
        carregarPontuacao();
        tamanhoPalavra = -1;
    }

    private void carregarPalavras() {
        System.out.println("Iniciando carregamento de palavras do arquivo: " + bancoPalavrasPath);
    try (BufferedReader br = new BufferedReader(new FileReader(bancoPalavrasPath))) {
        String linha;
        int linhaNumero = 0;
        int palavrasCarregadas = 0;
        System.out.println("Iniciando leitura das linhas:");
        
        while ((linha = br.readLine()) != null) {
            linhaNumero++;
            // Separa a palavra da descrição e pega só a primeira parte
            String palavra = linha.split(",")[0].trim().toUpperCase();
            
            System.out.println("Linha " + linhaNumero + ": '" + palavra + "' (comprimento: " + palavra.length() + ")");
            
            if (palavra.length() >= 3 && palavra.length() <= 14) {
                palavras.add(new Palavra(palavra));
                palavrasCarregadas++;
            } else {
                System.out.println("Palavra inválida na linha " + linhaNumero + ": " + palavra);
            }
        }
        
        System.out.println("Total de palavras carregadas: " + palavrasCarregadas);
        System.out.println("Total de linhas lidas: " + linhaNumero);
        
        // Imprimir a distribuição de tamanhos de palavras
        Map<Integer, Long> distribuicao = palavras.stream()
            .collect(Collectors.groupingBy(p -> p.getPalavra().length(), Collectors.counting()));
        
        System.out.println("Distribuição de tamanhos de palavras:");
        distribuicao.forEach((tamanho, quantidade) -> 
            System.out.println(tamanho + " letras: " + quantidade + " palavras")
        );
        
    } catch (IOException e) {
        e.printStackTrace();
        throw new IllegalStateException("Erro ao carregar palavras do arquivo: " + bancoPalavrasPath);
    }
    
    if (palavras.isEmpty()) {
        throw new IllegalStateException("Nenhuma palavra válida encontrada no arquivo: " + bancoPalavrasPath);
    }
    }

    private void carregarPalavrasUsadas() {
        try (BufferedReader br = new BufferedReader(new FileReader("palavras_usadas.txt"))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                palavrasUsadas.add(linha.trim().toUpperCase());
            }
        } catch (IOException e) {
            // Arquivo pode não existir na primeira execução
        }
    }

    public void salvarPalavrasUsadas() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("palavras_usadas.txt"))) {
            for (String palavra : palavrasUsadas) {
                writer.println(palavra);
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
                if (partes.length == 3) {
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

    public void setTamanhoPalavra(int tamanho) {
        this.tamanhoPalavra = tamanho;
    }

    public void novoJogo() {
        if (tamanhoPalavra < 3 || tamanhoPalavra > 14) {
            throw new IllegalStateException("Tamanho da palavra inválido: " + tamanhoPalavra);
        }
    
        List<Palavra> palavrasFiltradas = palavras.stream()
            .filter(p -> p.getPalavra().length() == tamanhoPalavra)
            .filter(p -> !palavrasUsadas.contains(p.getPalavra()))
            .collect(Collectors.toList());
    
        // Se não houver palavras disponíveis, limpe as palavras usadas automaticamente
        if (palavrasFiltradas.isEmpty()) {
            limparPalavrasUsadas();
            
            // Refiltra as palavras após limpar as usadas
            palavrasFiltradas = palavras.stream()
                .filter(p -> p.getPalavra().length() == tamanhoPalavra)
                .collect(Collectors.toList());
        }
    
        // Verifica novamente se há palavras disponíveis após limpar
        if (palavrasFiltradas.isEmpty()) {
            throw new IllegalStateException("Nenhuma palavra disponível com " + tamanhoPalavra + " letras.");
        }
    
        palavraAtual = palavrasFiltradas.get(new Random().nextInt(palavrasFiltradas.size()));
    
        forca.reiniciar();
        letrasDigitadas.clear();
        palavraAtual.reiniciar();
    }

    public void marcarPalavraComoUsada() {
        if (palavraAtual != null) {
            palavrasUsadas.add(palavraAtual.getPalavra());
            salvarPalavrasUsadas();
        }
    }

    public void limparPalavrasUsadas() {
        palavrasUsadas.clear();
        salvarPalavrasUsadas();
        System.out.println("Palavras usadas liberadas para o tamanho: " + tamanhoPalavra);
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

    public String getLetrasDigitadas() {
        return letrasDigitadas.stream().map(String::valueOf).collect(Collectors.joining(", "));
    }

    public String getPontuacao() {
        return String.valueOf(totalScore);
    }

    public int getVitorias() {
        return vitorias;
    }

    public int getDerrotas() {
        return derrotas;
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
    private String palavra;
    private Set<Character> letrasAdivinhadas;

    public Palavra(String palavra) {
        this.palavra = palavra.toUpperCase();
        this.letrasAdivinhadas = new HashSet<>();
    }

    public String getPalavra() {
        return palavra;
    }

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
}

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
