package pdu;

/**
 * PDU RIP para notificar custo de enlace.
 * <p>
 * Formato: "RIPNTF &lt;A&gt; &lt;B&gt; &lt;custo&gt;"
 * </p>
 */
public class RIPNotificationPDU {
    /** Tamanho máximo permitido para PDUs RIP */
    private static final int MAX_PDU_SIZE = 512;

    /** Nó A */
    private final short a;

    /** Nó B */
    private final short b;

    /** Custo do enlace */
    private final int cost;

    /**
     * Constrói uma PDU RIPNTF.
     *
     * @param a nó A
     * @param b nó B
     * @param cost custo
     */
    public RIPNotificationPDU(short a, short b, int cost) {
        this.a = a;
        this.b = b;
        this.cost = cost;
    }

    /**
     * Analisa uma string RIPNTF.
     *
     * @param s string PDU
     * @return instância de RIPNotificationPDU
     */
    public static RIPNotificationPDU parse(String s) {
        String t = s == null ? "" : s.trim();
        if (t.length() > MAX_PDU_SIZE) {
            throw new IllegalArgumentException("PDU excede tamanho máximo de 512 bytes");
        }
        String[] p = t.split("\\s+");
        if (p.length != 4 || !"RIPNTF".equals(p[0])) {
            throw new IllegalArgumentException("Formato RIPNTF inválido");
        }
        short a = Short.parseShort(p[1]);
        short b = Short.parseShort(p[2]);
        int cost = Integer.parseInt(p[3]);
        return new RIPNotificationPDU(a, b, cost);
    }

    /**
     * Codifica a PDU.
     *
     * @return string codificada
     */
    public String encode() {
        String v = "RIPNTF " + a + " " + b + " " + cost;
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