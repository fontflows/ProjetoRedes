package util;

import model.Link;
import model.TopologyConfig;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Leitor e validador de arquivo de configuração de topologia RIP.
 * <p>
 * Formato: "&lt;nodeA&gt; &lt;nodeB&gt; &lt;cost&gt;" com nós em [1..15] e custo em [1..15].
 * </p>
 */
public class TopologyConfigReader {
    /**
     * Lê e valida um arquivo de topologia.
     *
     * @param filePath caminho do arquivo de topologia
     * @return a topologia carregada
     * @throws IOException se houver erro de leitura ou validação
     */
    public static TopologyConfig read(String filePath) throws IOException {
        TopologyConfig topo = new TopologyConfig();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int ln = 0;
            while ((line = br.readLine()) != null) {
                ln++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split("\\s+");
                if (parts.length != 3) {
                    throw new IOException("Linha " + ln + ": formato inválido - esperado '<nodeA> <nodeB> <cost>'");
                }
                short a = Short.parseShort(parts[0]);
                short b = Short.parseShort(parts[1]);
                int cost = Integer.parseInt(parts[2]);
                if (a < 1 || a > 15 || b < 1 || b > 15) {
                    throw new IOException("Linha " + ln + ": nós devem estar em [1..15]");
                }
                if (cost < 1 || cost > 15) {
                    throw new IOException("Linha " + ln + ": custo deve estar em [1..15]");
                }
                if (a == b) {
                    throw new IOException("Linha " + ln + ": enlace não pode ser reflexivo");
                }
                if (topo.findLink(a, b) != null) {
                    throw new IOException("Linha " + ln + ": enlace duplicado entre " + a + " e " + b);
                }
                topo.addLink(new Link(a, b, cost));
            }
        }
        if (topo.getNodes().isEmpty()) {
            throw new IOException("Arquivo de topologia vazio");
        }
        return topo;
    }
}