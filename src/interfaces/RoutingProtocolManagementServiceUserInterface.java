package interfaces;

import model.DistanceTable;

/**
 * Interface implementada pela aplicação de gerência para receber notificações do protocolo de roteamento.
 * <p>
 * Define callbacks para tabela de distância e custo de enlace.
 * </p>
 */
public interface RoutingProtocolManagementServiceUserInterface {
    /**
     * Notifica a chegada de uma tabela de distância previamente requisitada.
     *
     * @param nodeId identificador do nó
     * @param table tabela de distância
     */
    void DistanceTableInd(short nodeId, DistanceTable table);

    /**
     * Notifica os custos de um enlace requisitado ou definido.
     *
     * @param nodeA identificador do nó A
     * @param nodeB identificador do nó B
     * @param cost custo do enlace
     */
    void LinkCostInd(short nodeA, short nodeB, int cost);
}