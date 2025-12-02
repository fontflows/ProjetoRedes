package util;

import java.util.HashMap;
import java.util.Map;
import model.DistanceTable;

public class RoutingPDUParser {

    public enum PDUType {
        RIPIND,
        RIPNTF,
        RIPGET,
        RIPSET,
        RIPRQT,
        RIPRSP,
        UNKNOWN
    }

    public static PDUType getPDUType(String message) {
        if (message == null || message.trim().isEmpty()) return PDUType.UNKNOWN;
        
        if (message.startsWith("RIPIND")) return PDUType.RIPIND;
        if (message.startsWith("RIPNTF")) return PDUType.RIPNTF;
        if (message.startsWith("RIPSET")) return PDUType.RIPSET;
        if (message.startsWith("RIPGET")) return PDUType.RIPGET;
        if (message.startsWith("RIPRQT")) return PDUType.RIPRQT;
        if (message.startsWith("RIPRSP")) return PDUType.RIPRSP;
        
        return PDUType.UNKNOWN;
    }

    // ==========================================
    // MÉTODOS DE CRIAÇÃO (ENCODE)
    // ==========================================

    public static String createRIPIND(short ripNode, Map<Short, Integer> distanceVector) {
        StringBuilder sb = new StringBuilder();
        sb.append("RIPIND ").append(ripNode).append(" ");
        sb.append(serializeVector(distanceVector));
        return sb.toString();
    }

    public static String createRIPNTF(short ripNodeA, short ripNodeB, int cost) {
        return "RIPNTF " + ripNodeA + " " + ripNodeB + " " + cost;
    }

    public static String createRIPSET(short ripNodeA, short ripNodeB, int cost) {
        return "RIPSET " + ripNodeA + " " + ripNodeB + " " + cost;
    }

    public static String createRIPGET(short ripNodeA, short ripNodeB) {
        return "RIPGET " + ripNodeA + " " + ripNodeB;
    }

    public static String createRIPRQT() {
        return "RIPRQT";
    }

    public static String createRIPRSP(short ripNode, Map<Short, Integer> nodeVector, Map<Short, Map<Short, Integer>> neighborVectors) {
        StringBuilder sb = new StringBuilder();
        sb.append("RIPRSP ").append(ripNode);

        // 1. Vetor do próprio nó
        sb.append(" ").append(serializeVector(nodeVector));

        // 2. Vetores dos vizinhos (se existirem)
        if (neighborVectors != null) {
            for (Map<Short, Integer> vec : neighborVectors.values()) {
                sb.append(" ").append(serializeVector(vec));
            }
        }
        
        return sb.toString();
    }

    /**
     * Serializa um vetor no formato c1:c2:c3...
     * Garante a ordem 1 a 15 e preenche com -1 se não existir.
     */
    private static String serializeVector(Map<Short, Integer> vector) {
        StringBuilder sb = new StringBuilder();
        for (short i = 1; i <= 15; i++) {
            int cost = vector.getOrDefault(i, DistanceTable.INFINITY);
            sb.append(cost);
            
            if (i < 15) sb.append(":");
        }
        return sb.toString();
    }

    // ==========================================
    // MÉTODOS DE LEITURA (DECODE)
    // ==========================================

    public static short parseNodeId(String message) {
        try {
            String[] parts = message.split(" ");
            if (parts.length >= 2) {
                return Short.parseShort(parts[1]);
            }
        } catch (NumberFormatException e) {
        }
        return -1;
    }

    public static Map<Short, Integer> parseVectorFromRIPIND(String message) {
        Map<Short, Integer> vector = new HashMap<>();
        String[] parts = message.split(" ");
        
        // Formato: RIPIND <ID> <VETOR>
        if (parts.length < 3) return vector;

        String vectorStr = parts[2];
        return parseRawVectorString(vectorStr);
    }
    
    /**
     * Retorna uma matriz int[N][16] onde cada linha é um vetor recebido.
     * Usado pelo Gerente para exibir a tabela completa.
     */
    public static int[][] parseTableFromRIPRSP(String message) {
        String[] parts = message.split(" ");
        
        // parts[0]=RIPRSP, parts[1]=ID. Vetores começam no índice 2.
        int numVectors = parts.length - 2;
        if (numVectors <= 0) return new int[0][0];

        int[][] table = new int[numVectors][16];

        for (int i = 0; i < numVectors; i++) {
            String vectorStr = parts[i + 2];
            Map<Short, Integer> vecMap = parseRawVectorString(vectorStr);
            
            for (short node = 1; node <= 15; node++) {
                // Preenche a matriz (coluna 0 fica vazia ou pode ser usada se ajustar o índice)
                table[i][node] = vecMap.getOrDefault(node, DistanceTable.INFINITY);
            }
        }
        return table;
    }

    private static Map<Short, Integer> parseRawVectorString(String vectorStr) {
        Map<Short, Integer> vector = new HashMap<>();
        String[] costs = vectorStr.split(":");

        for (int i = 0; i < costs.length; i++) {
            try {
                int cost = Integer.parseInt(costs[i]);
                short nodeId = (short) (i + 1);
                vector.put(nodeId, cost);
            } catch (NumberFormatException e) {
            }
        }
        return vector;
    }

    /**
     * Lê parâmetros de RIPSET, RIPNTF, RIPGET.
     * Retorna [NodeA, NodeB, Custo]
     */
    public static int[] parseLinkParams(String message) {
        String[] parts = message.split(" ");
        int nodeA = 0, nodeB = 0, cost = 0;
        
        try {
            if (parts.length > 1) nodeA = Integer.parseInt(parts[1]);
            if (parts.length > 2) nodeB = Integer.parseInt(parts[2]);
            if (parts.length > 3) cost = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
        }
        
        return new int[]{nodeA, nodeB, cost};
    }
}