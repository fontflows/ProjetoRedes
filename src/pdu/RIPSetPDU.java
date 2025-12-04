package pdu;

/**
 * PDU RIP para definir o custo do enlace entre dois nós.
 * <p>
 * Formato: "RIPSET &lt;A&gt; &lt;B&gt; &lt;custo&gt;"
 * </p>
 */
public class RIPSetPDU {
    /** Tamanho máximo permitido para PDUs RIP */
    private static final int MAX_PDU_SIZE = 512;

    /** Nó A */
    private final short a;

    /** Nó B */
    private final short b;

    /** Custo do enlace */
    private final int cost;

    /**
     * Constrói uma PDU RIPSET.
     *
     * @param a nó A
     * @param b nó B
     * @param cost custo
     */
    public RIPSetPDU(short a, short b, int cost) {
        this.a = a;
        this.b = b;
        this.cost = cost;
    }

    /**
     * Analisa uma string RIPSET.
     *
     * @param s string PDU
     * @return instância de RIPSetPDU
     */
    public static RIPSetPDU parse(String s) {
        String t = s == null ? "" : s.trim();
        if (t.length() > MAX_PDU_SIZE) {
            throw new IllegalArgumentException("PDU excede tamanho máximo de 512 bytes");
        }
        String[] p = t.split("\\s+");
        if (p.length != 4 || !"RIPSET".equals(p[0])) {
            throw new IllegalArgumentException("Formato RIPSET inválido");
        }
        short a = Short.parseShort(p[1]);
        short b = Short.parseShort(p[2]);
        int cost = Integer.parseInt(p[3]);
        if (!(cost == -1 || (cost >= 1 && cost <= 15))) {
            throw new IllegalArgumentException("Custo inválido");
        }
        return new RIPSetPDU(a, b, cost);
    }

    /**
     * Codifica a PDU.
     *
     * @return string codificada
     */
    public String encode() {
        String v = "RIPSET " + a + " " + b + " " + cost;
        if (v.length() > MAX_PDU_SIZE) {
            throw new IllegalArgumentException("PDU excede tamanho máximo de 512 bytes");
        }
        return v;
    }

    /**
     * Retorna nó A.
     *
     * @return nó A
     */
    public short getA() {
        return a;
    }

    /**
     * Retorna nó B.
     *
     * @return nó B
     */
    public short getB() {
        return b;
    }

    /**
     * Retorna custo.
     *
     * @return custo
     */
    public int getCost() {
        return cost;
    }
}