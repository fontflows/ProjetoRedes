package protocol;

import interfaces.RoutingProtocolManagementInterface;
import interfaces.RoutingProtocolManagementServiceUserInterface;
import interfaces.UnicastServiceInterface;
import interfaces.UnicastServiceUserInterface;
import model.DistanceTable;
import model.DistanceVector;
import model.TopologyConfig;
import pdu.RIPGetPDU;
import pdu.RIPResponsePDU;
import pdu.RIPSetPDU;
import pdu.RIPRequestPDU;
import util.RIPParser;

import java.util.concurrent.Semaphore;

/**
 * Entidade gerente do protocolo de troca de informações de roteamento.
 * <p>
 * Interage com o serviço de unicast e oferece primitivas de gerenciamento concorrentes protegidas por semáforo.
 * </p>
 */
public class RoutingInformationProtocolManager implements RoutingProtocolManagementInterface, UnicastServiceUserInterface {
    /** Serviço de unicast */
    private final UnicastServiceInterface up;

    /** Usuário do serviço de gerenciamento */
    private final RoutingProtocolManagementServiceUserInterface user;

    /** Topologia carregada para validação */
    private final TopologyConfig topology;

    /** Semáforo para região crítica das operações */
    private final Semaphore sem;

    /**
     * Cria o gerente do protocolo.
     *
     * @param up serviço de unicast
     * @param user usuário do serviço de gerenciamento
     * @param topology topologia para validação
     */
    public RoutingInformationProtocolManager(UnicastServiceInterface up, RoutingProtocolManagementServiceUserInterface user, TopologyConfig topology) {
        this.up = up;
        this.user = user;
        this.topology = topology;
        this.sem = new Semaphore(1);
    }

    /**
     * Requisita custo de enlace entre dois nós.
     *
     * @param nodeA nó A
     * @param nodeB nó B
     * @return {@code true} se válido
     */
    @Override
    public boolean GetLinkCost(short nodeA, short nodeB) {
        try {
            sem.acquire();
            if (!topology.getNodes().contains(nodeA) || !topology.getNodes().contains(nodeB)) {
                return false;
            }
            if (topology.findLink(nodeA, nodeB) == null) {
                return false;
            }
            RIPGetPDU pdu = new RIPGetPDU(nodeA, nodeB);
            return up.UPDataReq(nodeA, pdu.encode());
        } catch (InterruptedException e) {
            return false;
        } finally {
            sem.release();
        }
    }

    /**
     * Define custo de enlace entre dois nós.
     *
     * @param nodeA nó A
     * @param nodeB nó B
     * @param cost custo
     * @return {@code true} se válido
     */
    @Override
    public boolean SetLinkCost(short nodeA, short nodeB, int cost) {
        try {
            sem.acquire();
            if (!topology.getNodes().contains(nodeA) || !topology.getNodes().contains(nodeB)) {
                return false;
            }
            if (topology.findLink(nodeA, nodeB) == null) {
                return false;
            }
            if (!(cost == -1 || (cost >= 1 && cost <= 15))) {
                return false;
            }
            RIPSetPDU pdu = new RIPSetPDU(nodeA, nodeB, cost);
            return up.UPDataReq(nodeA, pdu.encode());
        } catch (InterruptedException e) {
            return false;
        } finally {
            sem.release();
        }
    }

    /**
     * Requisita tabela de distância de um nó.
     *
     * @param nodeId nó destino
     * @return {@code true} se válido
     */
    @Override
    public boolean GetDistanceTable(short nodeId) {
        try {
            sem.acquire();
            if (!topology.getNodes().contains(nodeId)) {
                return false;
            }
            RIPRequestPDU pdu = new RIPRequestPDU();
            return up.UPDataReq(nodeId, pdu.encode());
        } catch (InterruptedException e) {
            return false;
        } finally {
            sem.release();
        }
    }

    /**
     * Callback de chegada de mensagem do serviço unicast.
     *
     * @param source UCSAP origem
     * @param message mensagem recebida
     */
    @Override
    public void UPDataInd(short source, String message) {
        String t = util.RIPParser.identify(message);
        if ("RIPNTF".equals(t)) {
            try {
                var ntf = pdu.RIPNotificationPDU.parse(message);
                user.LinkCostInd(ntf.getA(), ntf.getB(), ntf.getCost());
            } catch (Exception ignored) {
            }
        } else if ("RIPRSP".equals(t)) {
            try {
                RIPResponsePDU rsp = RIPResponsePDU.parse(message);
                DistanceTable table = new DistanceTable();
                for (int[] v : rsp.getVectors()) {
                    table.addVector(new DistanceVector(v));
                }
                user.DistanceTableInd(rsp.getNode(), table);
            } catch (Exception ignored) {
            }
        }
    }
}