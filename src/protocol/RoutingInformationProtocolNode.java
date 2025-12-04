// Java
package protocol;

import interfaces.UnicastServiceInterface;
import interfaces.UnicastServiceUserInterface;
import model.DistanceTable;
import model.DistanceVector;
import model.TopologyConfig;
import pdu.RIPGetPDU;
import pdu.RIPIndicationPDU;
import pdu.RIPNotificationPDU;
import pdu.RIPRequestPDU;
import pdu.RIPResponsePDU;
import pdu.RIPSetPDU;
import util.RIPParser;

import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * Entidade nó do protocolo de troca de informações de roteamento.
 * <p>
 * Mantém vetor de distância local, tabela de distância com vetores dos vizinhos, processa PDUs
 * e propaga atualizações periodicamente.
 * </p>
 */
public class RoutingInformationProtocolNode implements UnicastServiceUserInterface, Runnable {
    /** Identificador do nó */
    private final short nodeId;

    /** Serviço de unicast */
    private final UnicastServiceInterface up;

    /** Topologia */
    private final TopologyConfig topology;

    /** Lista ordenada dos nós para indexação consistente */
    private final List<Short> orderedNodes;

    /** Vetor de distância local */
    private DistanceVector localVector;

    /** Tabela de distância: primeiro vetor local, depois vetores de vizinhos por ordem de nó */
    private final DistanceTable table;

    /** Próximo salto por destino, armazenado como índice ordenado do nó; -1 quando desconhecido */
    private int[] nextHopIdx;

    /** Mapeia nó vizinho para seu último vetor conhecido */
    private final Map<Short, DistanceVector> neighborVectors;

    /** Semáforo para região crítica de atualização de tabela e vetor */
    private final Semaphore sem;

    /** Controle de execução */
    private volatile boolean running;

    /** Timeout de propagação em ms */
    private long propagationTimeoutMillis;

    /**
     * Cria a entidade nó.
     *
     * @param nodeId identificador do nó
     * @param up serviço de unicast
     * @param topology topologia conhecida
     */
    public RoutingInformationProtocolNode(short nodeId, UnicastServiceInterface up, TopologyConfig topology) {
        this.nodeId = nodeId;
        this.up = up;
        this.topology = topology;
        this.table = new DistanceTable();
        this.neighborVectors = new TreeMap<>();
        this.sem = new Semaphore(1);
        this.running = false;
        this.orderedNodes = new ArrayList<>(topology.getNodes());
        this.orderedNodes.sort(Comparator.comparingInt(Short::intValue));
    }

    /**
     * Inicializa tempo de propagação e estado local.
     *
     * @param propagationTimeoutMillis timeout de propagação
     */
    public void init(long propagationTimeoutMillis) {
        this.propagationTimeoutMillis = propagationTimeoutMillis;
        int size = orderedNodes.size();
        int[] costs = new int[size];
        Arrays.fill(costs, -1);
        int idxSelf = indexOf(nodeId);
        if (idxSelf >= 0) {
            costs[idxSelf] = 0;
        }
        localVector = new DistanceVector(costs);
        nextHopIdx = new int[size];
        Arrays.fill(nextHopIdx, -1);
        table.addVector(localVector);
        Map<Short, Integer> neighbors = topology.getNeighbors(nodeId);
        for (Map.Entry<Short, Integer> e : neighbors.entrySet()) {
            short neighbor = e.getKey();
            int[] nv = new int[size];
            Arrays.fill(nv, -1);
            int idxNeighbor = indexOf(neighbor);
            if (idxNeighbor >= 0) {
                nv[idxNeighbor] = 0;
            }
            neighborVectors.put(neighbor, new DistanceVector(nv));
            table.addVector(neighborVectors.get(neighbor));
        }
        running = true;
        Thread t = new Thread(this, "RIPNode-" + nodeId);
        t.setDaemon(true);
        t.start();
        recomputeLocalVector();
        propagateDistanceVector();
    }

    /**
     * Loop de propagação periódica do vetor de distância.
     */
    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(propagationTimeoutMillis);
                propagateDistanceVector();
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * Encerra o nó.
     */
    public void shutdown() {
        running = false;
    }

    /**
     * Callback de chegada de mensagem do serviço unicast.
     *
     * @param source UCSAP origem
     * @param message mensagem recebida
     */
    @Override
    public void UPDataInd(short source, String message) {
        String type = RIPParser.identify(message);
        if (type == null) {
            return;
        }
        try {
            sem.acquire();
            switch (type) {
                case "RIPGET" -> handleGet(RIPGetPDU.parse(message));
                case "RIPSET" -> handleSet(RIPSetPDU.parse(message));
                case "RIPIND" -> handleIndication(RIPIndicationPDU.parse(message));
                case "RIPRQT" -> handleRequest(RIPRequestPDU.parse(message));
                case "RIPNTF" -> handleNotification(RIPNotificationPDU.parse(message));
                default -> { }
            }
        } catch (Exception ignored) {
        } finally {
            sem.release();
        }
    }

    /**
     * Trata RIPGET enviando RIPNTF com custo atual do enlace.
     *
     * @param pdu PDU RIPGET
     */
    private void handleGet(RIPGetPDU pdu) {
        short a = pdu.getA();
        short b = pdu.getB();
        if (a != nodeId) {
            return;
        }
        var link = topology.findLink(a, b);
        int cost = link != null ? link.getCost() : -1;
        RIPNotificationPDU ntf = new RIPNotificationPDU(nodeId, b, cost);
        up.UPDataReq((short) 0, ntf.encode());
    }

    /**
     * Trata RIPSET atualizando custo do enlace, ajustando tabela e vetor, e propagando se necessário.
     *
     * @param pdu PDU RIPSET
     */
    private void handleSet(RIPSetPDU pdu) {
        short a = pdu.getA();
        short b = pdu.getB();
        if (a != nodeId) {
            return;
        }
        var link = topology.findLink(a, b);
        if (link == null) {
            return;
        }
        int newCost = pdu.getCost();
        if (!(newCost == -1 || (newCost >= 1 && newCost <= 15))) {
            return;
        }
        link.setCost(newCost);
        int idxB = indexOf(b);
        if (idxB >= 0) {
            if (newCost == -1) {
                localVector.set(idxB, -1);
                DistanceVector bv = neighborVectors.get(b);
                if (bv != null) {
                    for (int i = 0; i < bv.size(); i++) {
                        bv.set(i, -1);
                    }
                }
            } else {
                localVector.set(idxB, newCost);
                DistanceVector bv = neighborVectors.get(b);
                if (bv != null) {
                    int s = bv.size();
                    for (int i = 0; i < s; i++) {
                        bv.set(i, -1);
                    }
                    int idxNeighbor = indexOf(b);
                    if (idxNeighbor >= 0) {
                        bv.set(idxNeighbor, 0);
                    }
                }
            }
        }
        recomputeLocalVector();
        propagateDistanceVector();
        RIPNotificationPDU ntf = new RIPNotificationPDU(nodeId, b, newCost);
        up.UPDataReq(b, ntf.encode());
        up.UPDataReq((short) 0, ntf.encode());
    }

    /**
     * Trata RIPIND atualizando o vetor do vizinho e recomputando o vetor local.
     *
     * @param ind PDU RIPIND
     */
    private void handleIndication(RIPIndicationPDU ind) {
        short neighbor = ind.getNode();
        DistanceVector nv = neighborVectors.get(neighbor);
        if (nv == null) {
            return;
        }
        int[] costs = ind.getCosts();
        int size = nv.size();
        if (costs == null || costs.length != size) {
            for (int i = 0; i < size; i++) {
                nv.set(i, -1);
            }
            recomputeLocalVector();
            return;
        }
        for (int i = 0; i < size; i++) {
            int v = costs[i];
            if (!(v == -1 || (v >= 0 && v <= 15))) {
                v = -1;
            }
            nv.set(i, v);
        }
        int idxNeighbor = indexOf(neighbor);
        if (idxNeighbor >= 0) {
            nv.set(idxNeighbor, 0);
        }
        recomputeLocalVector();
    }

    /**
     * Trata RIPRQT enviando RIPRSP com a tabela de distância.
     *
     * @param rqt PDU RIPRQT
     */
    private void handleRequest(RIPRequestPDU rqt) {
        List<int[]> vectors = new ArrayList<>();
        for (DistanceVector v : table.getVectors()) {
            vectors.add(v.toArray());
        }
        RIPResponsePDU rsp = new RIPResponsePDU(nodeId, vectors);
        up.UPDataReq((short) 0, rsp.encode());
    }

    /**
     * Trata RIPNTF atualizando o custo do enlace informado pelo vizinho,
     * ajustando o vetor local e propagando o novo vetor.
     *
     * @param ntf PDU RIPNTF
     */
    private void handleNotification(RIPNotificationPDU ntf) {
        short a = ntf.getA();
        short b = ntf.getB();
        if (b != nodeId) {
            return;
        }
        var link = topology.findLink(a, b);
        if (link == null) {
            return;
        }
        int newCost = ntf.getCost();
        if (!(newCost == -1 || (newCost >= 1 && newCost <= 15))) {
            return;
        }
        link.setCost(newCost);
        int idxA = indexOf(a);
        if (idxA >= 0) {
            if (newCost == -1) {
                localVector.set(idxA, -1);
                DistanceVector av = neighborVectors.get(a);
                if (av != null) {
                    for (int i = 0; i < av.size(); i++) {
                        av.set(i, -1);
                    }
                }
            } else {
                localVector.set(idxA, newCost);
            }
        }
        recomputeLocalVector();
        propagateDistanceVector();
    }

    /**
     * Propaga o vetor de distância para vizinhos, ignorando enlaces com custo infinito.
     */
    private void propagateDistanceVector() {
        Map<Short, Integer> neighbors = topology.getNeighbors(nodeId);
        int[] snapshot = localVector.toArray();
        for (Map.Entry<Short, Integer> e : neighbors.entrySet()) {
            short neighbor = e.getKey();
            int c = e.getValue();
            if (!(c == -1 || (c >= 1 && c <= 15))) {
                continue;
            }
            if (c == -1) {
                continue;
            }
            int[] payload = Arrays.copyOf(snapshot, snapshot.length);
            int nhIdx = indexOf(neighbor);
            if (nextHopIdx != null && nhIdx >= 0 && nextHopIdx.length == payload.length) {
                for (int i = 0; i < payload.length; i++) {
                    if (nextHopIdx[i] == nhIdx) {
                        payload[i] = -1;
                    }
                }
            }
            RIPIndicationPDU ind = new RIPIndicationPDU(nodeId, payload);
            up.UPDataReq(neighbor, ind.encode());
        }
    }

    /**
     * Recalcula o vetor de distância local seguindo a regra de Bellman-Ford.
     */
    private void recomputeLocalVector() {
        int n = orderedNodes.size();
        int[] newCosts = new int[n];
        int[] newNextHop = new int[n];
        Arrays.fill(newCosts, -1);
        Arrays.fill(newNextHop, -1);
        SortedMap<Short, Integer> neighborCosts = new TreeMap<>(topology.getNeighbors(nodeId));
        for (int destIdx = 0; destIdx < n; destIdx++) {
            short destNode = nodeAtIndex(destIdx);
            if (destNode == nodeId) {
                newCosts[destIdx] = 0;
                newNextHop[destIdx] = -1;
                continue;
            }
            int best = -1;
            int bestHopIdx = -1;
            for (Map.Entry<Short, Integer> e : neighborCosts.entrySet()) {
                short v = e.getKey();
                int cXv = e.getValue();
                if (!(cXv == -1 || (cXv >= 1 && cXv <= 15))) {
                    continue;
                }
                if (cXv == -1) {
                    continue;
                }
                DistanceVector dv = neighborVectors.get(v);
                if (dv == null) {
                    continue;
                }
                int dvY = dv.get(destIdx);
                if (!(dvY == -1 || (dvY >= 0 && dvY <= 15))) {
                    dvY = -1;
                }
                if (dvY == -1) {
                    continue;
                }
                int sum = cXv + dvY;
                if (sum > 15) {
                    continue;
                }
                int cand = sum;
                if (best == -1 || cand < best) {
                    best = cand;
                    bestHopIdx = indexOf(v);
                }
            }
            newCosts[destIdx] = best;
            newNextHop[destIdx] = bestHopIdx;
        }
        for (int i = 0; i < n; i++) {
            localVector.set(i, newCosts[i]);
        }
        nextHopIdx = newNextHop;
    }

    /**
     * Índice ordenado do nó na topologia.
     *
     * @param id nó
     * @return índice ou -1
     */
    private int indexOf(short id) {
        for (int i = 0; i < orderedNodes.size(); i++) {
            if (orderedNodes.get(i) == id) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Retorna nó na posição do índice ordenado.
     *
     * @param index índice
     * @return nó
     */
    private short nodeAtIndex(int index) {
        if (index < 0 || index >= orderedNodes.size()) {
            return (short) -1;
        }
        return orderedNodes.get(index);
    }
}