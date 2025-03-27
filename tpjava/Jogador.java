import java.util.HashSet;
import java.util.Set;

/**
 * Representa um jogador no Jogo da Forca, armazenando suas estatísticas e palavras usadas.
 */
public class Jogador {
    private String nome;              // Nome do jogador
    private int vitorias;             // Número de vitórias
    private int derrotas;             // Número de derrotas
    private int totalScore;           // Pontuação total acumulada
    private Set<String> palavrasUsadas; // Conjunto de palavras já utilizadas pelo jogador

    /**
     * Construtor que inicializa um novo jogador com o nome fornecido.
     * 
     * @param nome O nome do jogador
     */
    public Jogador(String nome) {
        this.nome = nome;
        this.vitorias = 0;
        this.derrotas = 0;
        this.totalScore = 0;
        this.palavrasUsadas = new HashSet<>();
    }

    /**
     * Retorna o nome do jogador.
     * 
     * @return O nome do jogador
     */
    public String getNome() {
        return nome;
    }

    /**
     * Retorna o número de vitórias do jogador.
     * 
     * @return O total de vitórias
     */
    public int getVitorias() {
        return vitorias;
    }

    /**
     * Define o número de vitórias do jogador.
     * 
     * @param vitorias O novo valor para o número de vitórias
     */
    public void setVitorias(int vitorias) {
        this.vitorias = vitorias;
    }

    /**
     * Retorna o número de derrotas do jogador.
     * 
     * @return O total de derrotas
     */
    public int getDerrotas() {
        return derrotas;
    }

    /**
     * Define o número de derrotas do jogador.
     * 
     * @param derrotas O novo valor para o número de derrotas
     */
    public void setDerrotas(int derrotas) {
        this.derrotas = derrotas;
    }

    /**
     * Retorna a pontuação total do jogador.
     * 
     * @return A pontuação total acumulada
     */
    public int getTotalScore() {
        return totalScore;
    }

    /**
     * Define a pontuação total do jogador.
     * 
     * @param totalScore O novo valor para a pontuação total
     */
    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    /**
     * Retorna o conjunto de palavras usadas pelo jogador.
     * 
     * @return O conjunto de palavras usadas
     */
    public Set<String> getPalavrasUsadas() {
        return palavrasUsadas;
    }

    /**
     * Define o conjunto de palavras usadas pelo jogador.
     * 
     * @param palavrasUsadas O novo conjunto de palavras usadas
     */
    public void setPalavrasUsadas(Set<String> palavrasUsadas) {
        this.palavrasUsadas = palavrasUsadas;
    }

    /**
     * Retorna uma representação em String do jogador (seu nome).
     * 
     * @return O nome do jogador como String
     */
    @Override
    public String toString() {
        return nome;
    }
}