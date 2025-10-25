package util;

import model.UCSAPConfig;
import model.UCSAPEntry;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Leitor de arquivos de configuração UCSAP.
 * <p>
 * Realiza a leitura e validação de arquivos de configuração que definem
 * os pontos de acesso ao serviço (UCSAP). Cada linha do arquivo deve
 * conter: &lt;ucsap_id&gt; &lt;hostname&gt; &lt;port&gt;
 * </p>
 */
public class ConfigReader {

    /**
     * Lê e valida um arquivo de configuração UCSAP.
     *
     * @param filePath o caminho do arquivo de configuração
     * @return um objeto UCSAPConfig contendo todas as entradas válidas
     * @throws IOException se houver erro de leitura ou se o arquivo contiver entradas inválidas
     */
    public static UCSAPConfig readConfig(String filePath) throws IOException {
        UCSAPConfig config = new UCSAPConfig();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("\\s+");
                if (parts.length != 3) {
                    throw new IOException("Linha " + lineNumber + ": formato inválido - esperado '<ucsap_id> <hostname> <port>'");
                }

                try {
                    short ucsapId = Short.parseShort(parts[0]);
                    String hostname = parts[1];
                    int port = Integer.parseInt(parts[2]);

                    if (ucsapId < 0) {
                        throw new IOException("Linha " + lineNumber + ": UCSAP ID deve ser >= 0");
                    }

                    if (hostname.isEmpty()) {
                        throw new IOException("Linha " + lineNumber + ": hostname não pode ser vazio");
                    }

                    if (port <= 1024 || port > 65535) {
                        throw new IOException("Linha " + lineNumber + ": porta deve estar entre 1025 e 65535");
                    }

                    if (config.exists(ucsapId)) {
                        throw new IOException("Linha " + lineNumber + ": UCSAP ID " + ucsapId + " duplicado");
                    }

                    config.addEntry(new UCSAPEntry(ucsapId, hostname, port));

                } catch (NumberFormatException e) {
                    throw new IOException("Linha " + lineNumber + ": formato numérico inválido - " + e.getMessage());
                }
            }
        }

        validateConfig(config);
        return config;
    }

    /**
     * Valida a configuração carregada verificando duplicidade de portas.
     *
     * @param config a configuração a ser validada
     * @throws IOException se houver portas duplicadas no mesmo host ou se a configuração estiver vazia
     */
    private static void validateConfig(UCSAPConfig config) throws IOException {
        Map<Short, UCSAPEntry> entries = config.getAllEntries();

        if (entries.isEmpty()) {
            throw new IOException("Arquivo de configuração vazio ou sem entradas válidas");
        }

        Map<String, Set<Integer>> hostPorts = new HashMap<>();

        for (UCSAPEntry entry : entries.values()) {
            String host = entry.getHostname();
            int port = entry.getPort();

            String normalizedHost = normalizeHostname(host);

            hostPorts.putIfAbsent(normalizedHost, new HashSet<>());
            if (!hostPorts.get(normalizedHost).add(port)) {
                throw new IOException("Porta " + port + " duplicada no host " + host);
            }
        }
    }

    /**
     * Normaliza nomes de host para facilitar comparações.
     * <p>
     * Converte localhost e 127.0.0.1 para um formato padrão.
     * </p>
     *
     * @param hostname o nome do host a ser normalizado
     * @return o hostname normalizado
     */
    private static String normalizeHostname(String hostname) {
        if ("localhost".equalsIgnoreCase(hostname) || "127.0.0.1".equals(hostname)) {
            return "localhost";
        }
        return hostname.toLowerCase();
    }
}