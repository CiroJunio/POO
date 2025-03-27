import java.util.HashSet;
import java.util.Set;

public class Jogador {
    private String nome;
    private int vitorias;
    private int derrotas;
    private int totalScore;
    private Set<String> palavrasUsadas;

    public Jogador(String nome) {
        this.nome = nome;
        this.vitorias = 0;
        this.derrotas = 0;
        this.totalScore = 0;
        this.palavrasUsadas = new HashSet<>();
    }

    public String getNome() {
        return nome;
    }

    public int getVitorias() {
        return vitorias;
    }

    public void setVitorias(int vitorias) {
        this.vitorias = vitorias;
    }

    public int getDerrotas() {
        return derrotas;
    }

    public void setDerrotas(int derrotas) {
        this.derrotas = derrotas;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public Set<String> getPalavrasUsadas() {
        return palavrasUsadas;
    }

    public void setPalavrasUsadas(Set<String> palavrasUsadas) {
        this.palavrasUsadas = palavrasUsadas;
    }

    @Override
    public String toString() {
        return nome;
    }
}
