package application;

import interfaces.RoutingProtocolManagementServiceUserInterface;
import interfaces.UnicastServiceUserInterface;
import model.TopologyConfig;
import protocol.RoutingInformationProtocolManager;
import protocol.UnicastProtocol;
import util.TopologyConfigReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Aplicação de gerência de roteamento.
 * <p>
 * Interage com o gerente RIP, oferecendo comandos para GetLinkCost, SetLinkCost e GetDistanceTable.
 * </p>
 */
public class RoutingManagementApplication implements RoutingProtocolManagementServiceUserInterface, UnicastServiceUserInterface {
    /** Gerente RIP */
    private RoutingInformationProtocolManager ripManager;

    /** Protocolo Unicast */
    private UnicastProtocol up;

    /**
     * Inicializa a aplicação de gerência.
     *
     * @param configFile caminho do arquivo de topologia
     * @throws Exception se houver erro de inicialização
     */
    public RoutingManagementApplication(String configFile) throws Exception {
        TopologyConfig topo = TopologyConfigReader.read(configFile);
        this.up = new UnicastProtocol((short) 0, util.ConfigReader.readConfig("config/unicast_config.txt"), this);
        this.up.start();
        this.ripManager = new RoutingInformationProtocolManager(up, this, topo);
    }

    /**
     * Callback de chegada de mensagem do serviço unicast.
     *
     * @param source UCSAP origem
     * @param message mensagem recebida
     */
    @Override
    public void UPDataInd(short source, String message) {
        if (ripManager != null) {
            ripManager.UPDataInd(source, message);
        }
    }

    /**
     * Loop principal de comandos.
     */
    public void run() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] p = line.split("\\s+");
                if (p.length >= 3 && p[0].equals("get")) {
                    short a = Short.parseShort(p[1]);
                    short b = Short.parseShort(p[2]);
                    ripManager.GetLinkCost(a, b);
                } else if (p.length >= 4 && p[0].equals("set")) {
                    short a = Short.parseShort(p[1]);
                    short b = Short.parseShort(p[2]);
                    int c = Integer.parseInt(p[3]);
                    ripManager.SetLinkCost(a, b, c);
                } else if (p.length >= 2 && p[0].equals("table")) {
                    short node = Short.parseShort(p[1]);
                    ripManager.GetDistanceTable(node);
                } else if (p[0].equals("quit")) {
                    break;
                }
            }
        } catch (Exception e) {
        } finally {
            if (up != null) {
                up.shutdown();
            }
        }
    }

    /**
     * Indicação de custo de enlace.
     *
     * @param nodeA nó A
     * @param nodeB nó B
     * @param cost custo
     */
    @Override
    public void LinkCostInd(short nodeA, short nodeB, int cost) {
        System.out.println("Custo entre " + nodeA + " e " + nodeB + ": " + cost);
    }

    /**
     * Indicação de tabela de distância.
     *
     * @param node nó origem
     * @param table tabela recebida
     */
    @Override
    public void DistanceTableInd(short node, model.DistanceTable table) {
        System.out.println("Tabela de distância do nó " + node + ":");
        for (var v : table.getVectors()) {
            int[] a = v.toArray();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < a.length; i++) {
                if (i > 0) sb.append(" ");
                sb.append(a[i]);
            }
            System.out.println(sb.toString());
        }
    }

    /**
     * Ponto de entrada.
     *
     * @param args argumentos: 'topology_config'
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java application.RoutingManagementApplication <topology_config>");
            System.exit(1);
        }
        try {
            RoutingManagementApplication app = new RoutingManagementApplication(args[0]);
            app.run();
        } catch (Exception e) {
            System.exit(1);
        }
    }
}