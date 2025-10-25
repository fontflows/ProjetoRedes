package model;

/**
 * Representa uma entrada de configuração UCSAP.
 * <p>
 * Armazena as informações necessárias para comunicação com um
 * ponto de acesso ao serviço unicast: identificador, hostname e porta.
 * </p>
 */
public class UCSAPEntry {
    /** Identificador único do UCSAP */
    private final short ucsapId;

    /** Nome do host onde o UCSAP está localizado */
    private final String hostname;

    /** Porta UDP utilizada pelo UCSAP */
    private final int port;

    /**
     * Cria uma nova entrada UCSAP.
     *
     * @param ucsapId o identificador do UCSAP
     * @param hostname o nome do host
     * @param port a porta UDP
     */
    public UCSAPEntry(short ucsapId, String hostname, int port) {
        this.ucsapId = ucsapId;
        this.hostname = hostname;
        this.port = port;
    }

    /**
     * Retorna o identificador do UCSAP.
     *
     * @return o UCSAP ID
     */
    public short getUcsapId() {
        return ucsapId;
    }

    /**
     * Retorna o hostname do UCSAP.
     *
     * @return o hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Retorna a porta do UCSAP.
     *
     * @return a porta UDP
     */
    public int getPort() {
        return port;
    }

    /**
     * Retorna uma representação em string desta entrada.
     *
     * @return string no formato "UCSAP[id=X, host=Y, port=Z]"
     */
    @Override
    public String toString() {
        return "UCSAP[id=" + ucsapId + ", host=" + hostname + ", port=" + port + "]";
    }
}