package model;

import java.util.Arrays;

/**
 * Representa um vetor de distância de um nó.
 * <p>
 * Armazena custos ordenados para todos os nós da rede.
 * </p>
 */
public class DistanceVector {
    /** Custos em ordem de nó 1..N, incluindo próprio nó com custo 0 */
    private final int[] costs;

    /**
     * Cria um vetor de distância.
     *
     * @param costs custos ordenados
     */
    public DistanceVector(int[] costs) {
        this.costs = Arrays.copyOf(costs, costs.length);
    }

    /**
     * Retorna os custos.
     *
     * @return cópia dos custos
     */
    public int[] toArray() {
        return Arrays.copyOf(costs, costs.length);
    }

    /**
     * Retorna custo para um índice.
     *
     * @param index índice base 0
     * @return custo
     */
    public int get(int index) {
        return costs[index];
    }

    /**
     * Define custo para um índice.
     *
     * @param index índice base 0
     * @param value custo
     */
    public void set(int index, int value) {
        costs[index] = value;
    }

    /**
     * Tamanho do vetor.
     *
     * @return número de nós
     */
    public int size() {
        return costs.length;
    }
}