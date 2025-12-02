package model;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap; // Usado no toString para ordenar a saída

/**
 * Tabela de Distância (Distance Table).
 * <p>
 * Armazena o estado de roteamento do nó, incluindo:
 * 1. Vetor de distâncias (Custos).
 * 2. Próximos saltos (Next Hops) para encaminhamento de dados.
 * 3. Custos dos enlaces diretos.
 * 4. Vetores recebidos dos vizinhos.
 * </p>
 */
public class DistanceTable {
    
    public static final int INFINITY = -1;
    public static final int MAX_COST = 15;

    private final short nodeId;
    
    // Dx: Mapeia Destino -> Custo Total
    private final Map<Short, Integer> nodeDistanceVector;
    
    // Forwarding Table: Mapeia Destino -> Próximo Salto (Vizinho)
    private final Map<Short, Short> nextHops;
    
    // c(x,v): Mapeia Vizinho -> Custo do Enlace
    private final Map<Short, Integer> linkCosts;
    
    // Dv: Mapeia Vizinho -> (Destino -> Custo)
    private final Map<Short, Map<Short, Integer>> neighborDistanceVectors;

    public DistanceTable(short nodeId) {
        this.nodeId = nodeId;
        this.nodeDistanceVector = new HashMap<>();
        this.nextHops = new HashMap<>();
        this.linkCosts = new HashMap<>();
        this.neighborDistanceVectors = new HashMap<>();
        
        // Rota para si mesmo: Custo 0, NextHop é ele mesmo (ou 0/null)
        this.nodeDistanceVector.put(nodeId, 0);
        this.nextHops.put(nodeId, nodeId);
    }

    public synchronized void addNeighbor(short neighborId, int cost) {
        int finalCost = (cost == -1) ? INFINITY : cost;
        
        linkCosts.put(neighborId, finalCost);
        
        // Inicialmente, o vizinho é o próprio next hop para ele mesmo
        nodeDistanceVector.put(neighborId, finalCost);
        if (finalCost != INFINITY) {
            nextHops.put(neighborId, neighborId);
        }
        
        neighborDistanceVectors.put(neighborId, new HashMap<>());
    }

    public synchronized boolean updateLinkCost(short neighborId, int newCost) {
        int finalCost = (newCost == -1) ? INFINITY : newCost;
        linkCosts.put(neighborId, finalCost);
        return recalculate();
    }

    public synchronized boolean updateFromNeighbor(short neighborId, Map<Short, Integer> neighborVector) {
        neighborDistanceVectors.put(neighborId, neighborVector);
        return recalculate();
    }

    /**
     * Executa Bellman-Ford e atualiza Custo E NextHop.
     */
    private boolean recalculate() {
        boolean changed = false;

        // Itera sobre todos os destinos possíveis (1 a 15)
        for (short dest = 1; dest <= 15; dest++) {
            if (dest == nodeId) continue;

            int currentCost = nodeDistanceVector.getOrDefault(dest, INFINITY);
            short currentNextHop = nextHops.getOrDefault(dest, (short) -1);
            
            int bestNewCost = INFINITY;
            short bestNextHop = -1;

            // Itera sobre os vizinhos para achar o melhor caminho
            for (Short neighbor : linkCosts.keySet()) {
                int costToNeighbor = linkCosts.get(neighbor); // c(x,v)
                
                // sem conexão ao vizinho, ignora
                if (costToNeighbor == INFINITY) continue;

                Map<Short, Integer> vVector = neighborDistanceVectors.get(neighbor);
                if (vVector == null) continue;

                int distNeighborToDest = vVector.getOrDefault(dest, INFINITY); // Dv(y)

                if (distNeighborToDest == INFINITY) continue;

                int totalCost = costToNeighbor + distNeighborToDest; 

                // Regra do limite máximo (15)
                if (totalCost > MAX_COST) {
                    totalCost = INFINITY;
                    continue;
                }

                    // Lógica de Minimização
                if (bestNewCost == INFINITY || totalCost < bestNewCost) {
                    bestNewCost = totalCost;
                    bestNextHop = neighbor;
                }
                
            }

            // Atualiza se houve mudança no Custo
            if (bestNewCost != currentCost) {
                nodeDistanceVector.put(dest, bestNewCost);
                nextHops.put(dest, bestNextHop);
                changed = true;
            } else if (bestNewCost == currentCost && bestNewCost != INFINITY) {
                // Se o custo é o mesmo, mas achamos um nextHop diferente
                // podemos atualizar o nextHop localmente, mas não precisamos propagar RIPIND.
                if (bestNextHop != currentNextHop) {
                    nextHops.put(dest, bestNextHop);
                }
            }
            
            // Se o caminho se tornou inalcançável (Infinity)
            if (bestNewCost == INFINITY && currentCost != INFINITY) {
                nodeDistanceVector.put(dest, INFINITY);
                nextHops.remove(dest);
                changed = true;
            }
        }
        return changed;
    }

    // --- Métodos de Acesso ---

    public synchronized Map<Short, Integer> getNodeDistanceVector() {
        return new HashMap<>(nodeDistanceVector);
    }
    
    public synchronized Map<Short, Map<Short, Integer>> getNeighborDistanceVectors() {
        return new HashMap<>(neighborDistanceVectors);
    }

    public synchronized int getLinkCost(short neighborId) {
        return linkCosts.getOrDefault(neighborId, INFINITY);
    }

    /**
     * Retorna o Próximo Salto para um destino.
     * Usado no encaminhamento de mensagens de dados.
     * @return ID do vizinho ou -1 se não houver rota.
     */
    public synchronized short getNextHop(short dest) {
        return nextHops.getOrDefault(dest, (short) -1);
    }

    /**
     * Gera uma representação em String da tabela para visualização local.
     */
    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== Tabela de Roteamento (Nó ").append(nodeId).append(") ===\n");
        sb.append(String.format("%-10s | %-10s | %-10s\n", "Destino", "Custo", "Next Hop"));
        sb.append("--------------------------------------\n");

        // Usar TreeMap para ordenar por Destino (1, 2, 3...)
        Map<Short, Integer> sortedVector = new TreeMap<>(nodeDistanceVector);

        for (Map.Entry<Short, Integer> entry : sortedVector.entrySet()) {
            short dest = entry.getKey();
            int cost = entry.getValue();
            short hop = nextHops.getOrDefault(dest, (short) -1);
            
            String costStr = (cost == INFINITY) ? "INF" : String.valueOf(cost);
            String hopStr = (hop == -1) ? "-" : String.valueOf(hop);
            
            // Destaca o próprio nó
            if (dest == nodeId) hopStr = "Local";

            sb.append(String.format("%-10d | %-10s | %-10s\n", dest, costStr, hopStr));
        }
        sb.append("======================================\n");
        return sb.toString();
    }
}