package pdu;

/**
 * Representa uma PDU (Protocol Data Unit) de requisição de dados unicast.
 * <p>
 * Esta classe encapsula os dados a serem transmitidos no protocolo unicast,
 * incluindo serialização e desserialização da PDU no formato:
 * "UPDREQPDU &lt;tamanho&gt; &lt;dados&gt;"
 * </p>
 */
public class UPDataRequestPDU {
    /** Tamanho máximo permitido para uma PDU em bytes */
    private static final int MAX_PDU_SIZE = 1024;

    /** Tamanho dos dados contidos nesta PDU */
    private final int size;

    /** Dados a serem transmitidos */
    private final String data;

    /**
     * Constrói uma nova PDU com os dados especificados.
     *
     * @param data os dados a serem encapsulados na PDU
     */
    public UPDataRequestPDU(String data) {
        if (data == null) {
            throw new IllegalArgumentException("Dados não podem ser null");
        }
        this.data = data;
        this.size = data.length();
    }

    /**
     * Analisa uma string PDU recebida e cria um objeto UPDataRequestPDU.
     *
     * @param pduString a string PDU no formato "UPDREQPDU &lt;tamanho&gt; &lt;dados&gt;"
     * @return uma nova instância de UPDataRequestPDU com os dados extraídos
     * @throws IllegalArgumentException se o formato da PDU for inválido ou se o tamanho
     *         declarado não corresponder ao tamanho real dos dados
     */
    public static UPDataRequestPDU parse(String pduString) throws IllegalArgumentException {
        if (pduString == null || !pduString.startsWith("UPDREQPDU ")) {
            throw new IllegalArgumentException("Formato PDU inválido");
        }

        if (pduString.length() > MAX_PDU_SIZE) {
            throw new IllegalArgumentException("PDU excede tamanho máximo de 1024 bytes");
        }

        int firstSpace = pduString.indexOf(' ');
        int secondSpace = pduString.indexOf(' ', firstSpace + 1);

        if (secondSpace == -1) {
            throw new IllegalArgumentException("Formato PDU inválido");
        }

        try {
            int size = Integer.parseInt(pduString.substring(firstSpace + 1, secondSpace));
            String data = pduString.substring(secondSpace + 1);

            if (data.length() != size) {
                throw new IllegalArgumentException("Tamanho não corresponde aos dados");
            }

            return new UPDataRequestPDU(data);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Tamanho inválido na PDU");
        }
    }

    /**
     * Codifica esta PDU em uma string para transmissão.
     *
     * @return a PDU codificada no formato "UPDREQPDU &lt;tamanho&gt; &lt;dados&gt;"
     * @throws IllegalArgumentException se a PDU codificada exceder o tamanho máximo de 1024 bytes
     */
    public String encode() {
        String encoded = "UPDREQPDU " + size + " " + data;
        if (encoded.length() > MAX_PDU_SIZE) {
            throw new IllegalArgumentException("PDU excede tamanho máximo de 1024 bytes");
        }
        return encoded;
    }

    /**
     * Retorna os dados contidos nesta PDU.
     *
     * @return os dados da PDU
     */
    public String getData() {
        return data;
    }

    /**
     * Retorna o tamanho dos dados contidos nesta PDU.
     *
     * @return o tamanho em bytes dos dados
     */
    public int getSize() {
        return size;
    }
}