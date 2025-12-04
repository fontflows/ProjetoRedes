package pdu;

import java.util.ArrayList;
import java.util.List;

/**
 * PDU RIP para informar tabela de distância de um nó.
 * <p>
 * Formato: "RIPRSP &lt;node&gt; &lt;v1&gt; &lt;v2&gt; ...", onde cada vetor v é "c1:c2:...:cn".
 * </p>
 */
public class RIPResponsePDU {
    /** Tamanho máximo permitido para PDUs RIP */
    private static final int MAX_PDU_SIZE = 512;

    /** Nó origem */
    private final short node;

    /** Vetores de distância */
    private final List<int[]> vectors;

    /**
     * Constrói uma PDU RIPRSP.
     *
     * @param node nó origem
     * @param vectors lista de vetores
     */
    public RIPResponsePDU(short node, List<int[]> vectors) {
        this.node = node;
        this.vectors = new ArrayList<>(vectors);
    }

    /**
     * Analisa uma string RIPRSP.
     *
     * @param s string PDU
     * @return instância de RIPResponsePDU
     */
    public static RIPResponsePDU parse(String s) {
        String t = s == null ? "" : s.trim();
        if (t.length() > MAX_PDU_SIZE) {
            throw new IllegalArgumentException("PDU excede tamanho máximo de 512 bytes");
        }
        String[] p = t.split("\\s+");
        if (p.length < 3 || !"RIPRSP".equals(p[0])) {
            throw new IllegalArgumentException("Formato RIPRSP inválido");
        }
        short node = Short.parseShort(p[1]);
        List<int[]> vectors = new ArrayList<>();
        for (int i = 2; i < p.length; i++) {
            String[] cs = p[i].split(":");
            int[] v = new int[cs.length];
            for (int j = 0; j < cs.length; j++) {
                v[j] = Integer.parseInt(cs[j]);
            }
            vectors.add(v);
        }
        return new RIPResponsePDU(node, vectors);
    }

    /**
     * Codifica a PDU.
     *
     * @return string codificada
     */
    public String encode() {
        StringBuilder sb = new StringBuilder();
        sb.append("RIPRSP ").append(node);
        for (int[] v : vectors) {
            sb.append(" ");
            for (int i = 0; i < v.length; i++) {
                if (i > 0) sb.append(":");
                sb.append(v[i]);
            }
        }
        String out = sb.toString();
        if (out.length() > MAX_PDU_SIZE) {
            throw new IllegalArgumentException("PDU excede tamanho máximo de 512 bytes");
        }
        return out;
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
     * Retorna vetores.
     *
     * @return lista de vetores
     */
    public List<int[]> getVectors() {
        return new ArrayList<>(vectors);
    }
}