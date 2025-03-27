import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

class JogoDaForcaLogic {
    private List<Palavra> palavras;
    private Palavra palavraAtual;
    private Forca forca;
    private Set<Character> letrasDigitadas;
    private List<Jogador> jogadores;
    private Jogador jogadorAtual;
    private int tamanhoPalavra;
    private String bancoPalavrasPath;

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
            // Arquivo pode não existir na primeira execução
        }
    }

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

    public void cadastrarJogador(String nome) {
        if (jogadores.stream().noneMatch(j -> j.getNome().equalsIgnoreCase(nome))) {
            Jogador novoJogador = new Jogador(nome);
            jogadores.add(novoJogador);
            salvarJogadores();
        }
    }

    public void setJogadorAtual(String nome) {
        jogadorAtual = jogadores.stream()
            .filter(j -> j.getNome().equalsIgnoreCase(nome))
            .findFirst()
            .orElse(null);
    }

    public Jogador getJogadorAtual() {
        return jogadorAtual;
    }

    public List<Jogador> getJogadores() {
        return jogadores;
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

    public void marcarPalavraComoUsada() {
        if (palavraAtual != null) {
            jogadorAtual.getPalavrasUsadas().add(palavraAtual.getPalavra());
            salvarJogadores();
        }
    }

    public void limparPalavrasUsadas() {
        jogadorAtual.getPalavrasUsadas().clear();
        salvarJogadores();
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
        return String.valueOf(jogadorAtual.getTotalScore());
    }

    public int getVitorias() {
        return jogadorAtual.getVitorias();
    }

    public int getDerrotas() {
        return jogadorAtual.getDerrotas();
    }

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

    public void resetarPontuacao() {
        jogadorAtual.setVitorias(0);
        jogadorAtual.setDerrotas(0);
        jogadorAtual.setTotalScore(0);
        salvarJogadores();
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
