package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa a tabela de distância de um nó.
 * <p>
 * Contém o próprio vetor e os vetores dos vizinhos, ordenados.
 * </p>
 */
public class DistanceTable {
    /** Vetores em ordem: primeiro o vetor do nó, seguido dos vizinhos */
    private final List<DistanceVector> vectors;

    /**
     * Cria uma tabela vazia.
     */
    public DistanceTable() {
        this.vectors = new ArrayList<>();
    }

    /**
     * Adiciona um vetor de distância.
     *
     * @param vector vetor a adicionar
     */
    public void addVector(DistanceVector vector) {
        this.vectors.add(vector);
    }

    /**
     * Retorna os vetores.
     *
     * @return lista imutável de vetores
     */
    public List<DistanceVector> getVectors() {
        return Collections.unmodifiableList(vectors);
    }
}