package model;

import java.util.*;

/**
 * Representa a topologia da rede para o protocolo de roteamento.
 * <p>
 * Mantém nós e enlaces, com validação e acesso eficiente a vizinhos.
 * </p>
 */
public class TopologyConfig {
    /** Conjunto de nós válidos */
    private final SortedSet<Short> nodes;

    /** Lista de enlaces existentes */
    private final List<Link> links;

    /**
     * Cria uma topologia vazia.
     */
    public TopologyConfig() {
        this.nodes = new TreeSet<>();
        this.links = new ArrayList<>();
    }

    /**
     * Adiciona um enlace e registra os nós.
     *
     * @param link o enlace a ser adicionado
     */
    public void addLink(Link link) {
        links.add(link);
        nodes.add(link.getNodeA());
        nodes.add(link.getNodeB());
    }

    /**
     * Retorna os nós existentes.
     *
     * @return conjunto ordenado de nós
     */
    public SortedSet<Short> getNodes() {
        return Collections.unmodifiableSortedSet(nodes);
    }

    /**
     * Retorna os enlaces existentes.
     *
     * @return lista imutável de enlaces
     */
    public List<Link> getLinks() {
        return Collections.unmodifiableList(links);
    }

    /**
     * Retorna os vizinhos de um nó.
     *
     * @param nodeId identificador do nó
     * @return mapa nó vizinho→custo
     */
    public Map<Short, Integer> getNeighbors(short nodeId) {
        Map<Short, Integer> neighbors = new TreeMap<>();
        for (Link l : links) {
            if (l.getNodeA() == nodeId) {
                neighbors.put(l.getNodeB(), l.getCost());
            } else if (l.getNodeB() == nodeId) {
                neighbors.put(l.getNodeA(), l.getCost());
            }
        }
        return neighbors;
    }

    /**
     * Busca um enlace entre dois nós.
     *
     * @param a nó A
     * @param b nó B
     * @return o enlace ou {@code null} se não existir
     */
    public Link findLink(short a, short b) {
        for (Link l : links) {
            if ((l.getNodeA() == a && l.getNodeB() == b) || (l.getNodeA() == b && l.getNodeB() == a)) {
                return l;
            }
        }
        return null;
    }
}