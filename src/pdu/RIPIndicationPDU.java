package pdu;

import java.util.Arrays;

/**
 * PDU RIP para propagar vetor de distância de um nó.
 * <p>
 * Formato: "RIPIND &lt;node&gt; &lt;c1:c2:...:cn&gt;"
 * </p>
 */
public class RIPIndicationPDU {
    /** Tamanho máximo permitido para PDUs RIP */
    private static final int MAX_PDU_SIZE = 512;

    /** Nó origem */
    private final short node;

    /** Vetor de custos */
    private final int[] costs;

    /**
     * Constrói uma PDU RIPIND.
     *
     * @param node nó origem
     * @param costs vetor de custos
     */
    public RIPIndicationPDU(short node, int[] costs) {
        this.node = node;
        this.costs = Arrays.copyOf(costs, costs.length);
    }

    /**
     * Analisa uma string RIPIND.
     *
     * @param s string PDU
     * @return instância de RIPIndicationPDU
     */
    public static RIPIndicationPDU parse(String s) {
        String t = s == null ? "" : s.trim();
        if (t.length() > MAX_PDU_SIZE) {
            throw new IllegalArgumentException("PDU excede tamanho máximo de 512 bytes");
        }
        String[] p = t.split("\\s+");
        if (p.length != 3 || !"RIPIND".equals(p[0])) {
            throw new IllegalArgumentException("Formato RIPIND inválido");
        }
        short node = Short.parseShort(p[1]);
        String[] cs = p[2].split(":");
        int[] costs = new int[cs.length];
        for (int i = 0; i < cs.length; i++) {
            costs[i] = Integer.parseInt(cs[i]);
        }
        return new RIPIndicationPDU(node, costs);
    }

    /**
     * Codifica a PDU.
     *
     * @return string codificada
     */
    public String encode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < costs.length; i++) {
            if (i > 0) sb.append(":");
            sb.append(costs[i]);
        }
        String v = "RIPIND " + node + " " + sb;
        if (v.length() > MAX_PDU_SIZE) {
            throw new IllegalArgumentException("PDU excede tamanho máximo de 512 bytes");
        }
        return v;
    }

    /**
     * Retorna nó origem.
     *
     * @return nó
     */
    public short getNode() {
        return node;
    }

    /**
     * Retorna os custos.
     *
     * @return vetor de custos
     */
    public int[] getCosts() {
        return Arrays.copyOf(costs, costs.length);
    }
}