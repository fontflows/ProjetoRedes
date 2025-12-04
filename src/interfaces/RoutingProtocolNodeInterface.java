package interfaces;

/**
 * Interface do protocolo de roteamento no contexto do nó.
 * <p>
 * Define operações internas do nó para processamento de PDUs e propagação.
 * </p>
 */
public interface RoutingProtocolNodeInterface {
    /**
     * Inicializa o nó com timeout de propagação.
     *
     * @param propagationTimeoutMillis período de propagação em milissegundos
     */
    void init(long propagationTimeoutMillis);

    /**
     * Encerra o nó de forma ordenada.
     */
    void shutdown();
}