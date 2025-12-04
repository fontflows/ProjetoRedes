package model;

/**
 * Enlace entre dois nós e seu custo.
 * <p>
 * Mantém identificação dos nós e custo atual.
 * </p>
 */
public class Link {
    /** Nó A */
    private final short nodeA;

    /** Nó B */
    private final short nodeB;

    /** Custo atual */
    private int cost;

    /**
     * Cria um enlace.
     *
     * @param nodeA nó A
     * @param nodeB nó B
     * @param cost custo
     */
    public Link(short nodeA, short nodeB, int cost) {
        this.nodeA = nodeA;
        this.nodeB = nodeB;
        this.cost = cost;
    }

    /**
     * Retorna nó A.
     *
     * @return nó A
     */
    public short getNodeA() {
        return nodeA;
    }

    /**
     * Retorna nó B.
     *
     * @return nó B
     */
    public short getNodeB() {
        return nodeB;
    }

    /**
     * Retorna custo.
     *
     * @return custo
     */
    public int getCost() {
        return cost;
    }

    /**
     * Define custo.
     *
     * @param cost novo custo
     */
    public void setCost(int cost) {
        this.cost = cost;
    }
}