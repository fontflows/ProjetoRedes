package application;

import interfaces.UnicastServiceUserInterface;
import model.UCSAPConfig;
import model.UCSAPEntry;
import protocol.UnicastProtocol;
import util.ConfigReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Aplicação de teste interativa para o protocolo unicast.
 * <p>
 * Permite enviar e receber mensagens através de uma interface de linha de comando.
 * Comandos disponíveis:
 * </p>
 * <ul>
 *   <li>send &lt;destino&gt; &lt;mensagem&gt; - envia uma mensagem</li>
 *   <li>list - lista os UCSAPs disponíveis</li>
 *   <li>exit - encerra a aplicação</li>
 * </ul>
 *
 * @author Felipe Silveira Miotto - 13750398
 * @author Francisco Eduardo Fontenele - 15452569
 */
public class UnicastTestApplication implements UnicastServiceUserInterface {
    /** Instância do protocolo unicast */
    private UnicastProtocol protocol;

    /** Configuração de UCSAPs */
    private UCSAPConfig config;

    /** ID do UCSAP local */
    private short myId;

    /** Flag para indicar se o shutdown já foi executado */
    private volatile boolean shutdownExecuted = false;

    /**
     * Inicializa a aplicação com um UCSAP específico.
     *
     * @param myId o identificador do UCSAP local
     * @param configFile o caminho do arquivo de configuração
     * @throws Exception se houver erro ao ler a configuração ou iniciar o protocolo
     */
    public UnicastTestApplication(short myId, String configFile) throws Exception {
        this.myId = myId;
        this.config = ConfigReader.readConfig(configFile);
        this.protocol = new UnicastProtocol(myId, config, this);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!shutdownExecuted) {
                System.out.println("\nEncerrando aplicação...");
                shutdownExecuted = true;
                protocol.shutdown();
            }
        }));

        protocol.start();

        System.out.println("=== Aplicação iniciada para UCSAP " + myId + " ===");
        System.out.println("Comandos disponíveis:");
        System.out.println("  send <destino> <mensagem>");
        System.out.println("  list");
        System.out.println("  exit");
        System.out.println();
    }

    /**
     * Callback invocado ao receber uma mensagem.
     *
     * @param source o UCSAP de origem
     * @param message a mensagem recebida
     */
    @Override
    public void UPDataInd(short source, String message) {
        System.out.println("\n[RECEBIDO de " + source + "]: " + message);
        System.out.print("> ");
        System.out.flush();
    }

    /**
     * Loop principal da aplicação que processa comandos do usuário.
     */
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            System.out.print("> ");

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty()) {
                    System.out.print("> ");
                    continue;
                }

                if (line.equals("exit")) {
                    break;
                }

                if (line.equals("list")) {
                    System.out.println("Nós disponíveis:");
                    for (UCSAPEntry entry : config.getAllEntries().values()) {
                        if (entry.getUcsapId() != myId) {
                            System.out.println("  " + entry);
                        }
                    }
                } else if (line.startsWith("send ")) {
                    String[] parts = line.split(" ", 3);
                    if (parts.length < 3) {
                        System.out.println("Uso: send <destino> <mensagem>");
                    } else {
                        try {
                            short dest = Short.parseShort(parts[1]);
                            String msg = parts[2];

                            if (protocol.UPDataReq(dest, msg)) {
                                System.out.println("Mensagem enviada para " + dest);
                            } else {
                                System.out.println("Falha ao enviar mensagem");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("UCSAP ID inválido");
                        }
                    }
                } else {
                    System.out.println("Comando desconhecido. Use: send, list ou exit");
                }

                System.out.print("> ");
            }

            if (!shutdownExecuted) {
                shutdownExecuted = true;
                protocol.shutdown();
                System.out.println("Aplicação encerrada");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Ponto de entrada da aplicação.
     *
     * @param args argumentos da linha de comando: &lt;ucsap_id&gt; &lt;config_file&gt;
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Uso: java application.UnicastTestApplication <ucsap_id> <config_file>");
            System.exit(1);
        }

        try {
            short ucsapId = Short.parseShort(args[0]);
            String configFile = args[1];

            UnicastTestApplication app = new UnicastTestApplication(ucsapId, configFile);
            app.run();

        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}