package pdu;

/**
 * PDU RIP para consultar o custo do enlace entre dois nós.
 * <p>
 * Formato: "RIPGET &lt;A&gt; &lt;B&gt;"
 * </p>
 */
public class RIPGetPDU {
    /** Tamanho máximo permitido para PDUs RIP */
    private static final int MAX_PDU_SIZE = 512;

    /** Nó A */
    private final short a;

    /** Nó B */
    private final short b;

    /**
     * Constrói uma PDU RIPGET.
     *
     * @param a nó A
     * @param b nó B
     */
    public RIPGetPDU(short a, short b) {
        this.a = a;
        this.b = b;
    }

    /**
     * Analisa uma string RIPGET.
     *
     * @param s string PDU
     * @return instância de RIPGetPDU
     */
    public static RIPGetPDU parse(String s) {
        String t = s == null ? "" : s.trim();
        if (t.length() > MAX_PDU_SIZE) {
            throw new IllegalArgumentException("PDU excede tamanho máximo de 512 bytes");
        }
        String[] p = t.split("\\s+");
        if (p.length != 3 || !"RIPGET".equals(p[0])) {
            throw new IllegalArgumentException("Formato RIPGET inválido");
        }
        short a = Short.parseShort(p[1]);
        short b = Short.parseShort(p[2]);
        return new RIPGetPDU(a, b);
    }

    /**
     * Codifica a PDU.
     *
     * @return string codificada
     */
    public String encode() {
        String v = "RIPGET " + a + " " + b;
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
}