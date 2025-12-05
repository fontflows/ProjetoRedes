package application;

import interfaces.UnicastServiceUserInterface;
import model.TopologyConfig;
import protocol.RoutingInformationProtocolNode;
import protocol.UnicastProtocol;
import util.ConfigReader;
import util.TopologyConfigReader;

/**
 * Aplicação de nó de roteamento baseado em vetor de distância.
 * <p>
 * Inicializa o serviço unicast, cria o nó RIP com a topologia fornecida
 * e inicia a propagação periódica do vetor de distância.
 * </p>
 */
public class RoutingNodeApplication implements UnicastServiceUserInterface {
    /** Protocolo Unicast */
    private UnicastProtocol up;

    /** Nó RIP */
    private RoutingInformationProtocolNode node;

    /**
     * Constrói a aplicação do nó de roteamento.
     *
     * @param nodeId identificador do nó
     * @param topoConfigPath caminho do arquivo de topologia
     * @param unicastConfigPath caminho do arquivo de configuração de UCSAPs
     * @param propagationTimeoutMillis período de propagação em milissegundos
     * @throws Exception se ocorrer erro na inicialização
     */
    public RoutingNodeApplication(short nodeId, String topoConfigPath, String unicastConfigPath, long propagationTimeoutMillis) throws Exception {
        TopologyConfig topo = TopologyConfigReader.read(topoConfigPath);
        this.up = new UnicastProtocol(nodeId, ConfigReader.readConfig(unicastConfigPath), this);
        this.node = new RoutingInformationProtocolNode(nodeId, up, topo);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                shutdown();
            } catch (Exception ignored) {
            }
        }));

        this.up.start();
        this.node.init(propagationTimeoutMillis);
    }

    /**
     * Callback de indicação de dados do serviço unicast.
     *
     * @param source UCSAP de origem
     * @param message mensagem recebida
     */
    @Override
    public void UPDataInd(short source, String message) {
        if (node != null) {
            node.UPDataInd(source, message);
        }
    }

    /**
     * Encerra a aplicação de forma ordenada.
     */
    private void shutdown() {
        if (node != null) {
            node.shutdown();
        }
        if (up != null) {
            up.shutdown();
        }
    }

    /**
     * Ponto de entrada da aplicação.
     *
     * @param args argumentos: &lt;node_id&gt; &lt;topology_config&gt; &lt;unicast_config&gt; &lt;propagation_timeout_ms&gt;
     */
    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Uso: java -cp out application.RoutingNodeApplication <node_id> <topology_config> <unicast_config> <propagation_timeout_ms>");
            System.exit(1);
        }
        try {
            short nodeId = Short.parseShort(args[0]);
            String topo = args[1];
            String ucfg = args[2];
            long timeout = Long.parseLong(args[3]);
            new RoutingNodeApplication(nodeId, topo, ucfg, timeout);
            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("Erro ao iniciar RoutingNodeApplication: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}