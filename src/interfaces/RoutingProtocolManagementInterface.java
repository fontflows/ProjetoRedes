package interfaces;

/**
 * Interface de gerenciamento do protocolo de troca de informações de roteamento.
 * <p>
 * Define as operações de consulta e alteração de custos de enlace, além da requisição de tabela de distância.
 * </p>
 */
public interface RoutingProtocolManagementInterface {
    /**
     * Requisita o custo do enlace entre dois nós.
     *
     * @param nodeA identificador do nó A
     * @param nodeB identificador do nó B
     * @return {@code true} se a requisição for válida, {@code false} caso contrário
     */
    boolean GetLinkCost(short nodeA, short nodeB);

    /**
     * Redefine o custo do enlace entre dois nós.
     *
     * @param nodeA identificador do nó A
     * @param nodeB identificador do nó B
     * @param cost novo custo do enlace
     * @return {@code true} se a operação for válida, {@code false} caso contrário
     */
    boolean SetLinkCost(short nodeA, short nodeB, int cost);

    /**
     * Requisita a tabela de distância de um nó.
     *
     * @param nodeId identificador do nó
     * @return {@code true} se o nó for válido, {@code false} caso contrário
     */
    boolean GetDistanceTable(short nodeId);
}