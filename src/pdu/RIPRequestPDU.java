package pdu;

/**
 * PDU RIP para requisitar a tabela de distância de um nó.
 * <p>
 * Formato: "RIPRQT"
 * </p>
 */
public class RIPRequestPDU {
    /** Tamanho máximo permitido para PDUs RIP */
    private static final int MAX_PDU_SIZE = 512;

    /**
     * Analisa uma string RIPRQT.
     *
     * @param s string PDU
     * @return instância de RIPRequestPDU
     */
    public static RIPRequestPDU parse(String s) {
        String t = s == null ? "" : s.trim();
        if (t.length() > MAX_PDU_SIZE) {
            throw new IllegalArgumentException("PDU excede tamanho máximo de 512 bytes");
        }
        if (!"RIPRQT".equals(t)) {
            throw new IllegalArgumentException("Formato RIPRQT inválido");
        }
        return new RIPRequestPDU();
    }

    /**
     * Codifica a PDU.
     *
     * @return string codificada
     */
    public String encode() {
        String v = "RIPRQT";
        if (v.length() > MAX_PDU_SIZE) {
            throw new IllegalArgumentException("PDU excede tamanho máximo de 512 bytes");
        }
        return v;
    }
}