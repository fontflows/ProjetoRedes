package model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Gerencia a configuração de todos os UCSAPs da rede.
 * <p>
 * Mantém um mapa de entradas UCSAP indexadas por ID,
 * permitindo busca eficiente e acesso a todas as entradas.
 * </p>
 */
public class UCSAPConfig {
    /** Mapa de entradas UCSAP indexadas por ID */
    private final Map<Short, UCSAPEntry> entries;

    /**
     * Cria uma nova configuração UCSAP vazia.
     */
    public UCSAPConfig() {
        this.entries = new HashMap<>();
    }

    /**
     * Adiciona uma entrada UCSAP à configuração.
     *
     * @param entry a entrada a ser adicionada
     */
    public void addEntry(UCSAPEntry entry) {
        entries.put(entry.getUcsapId(), entry);
    }

    /**
     * Busca uma entrada UCSAP por ID.
     *
     * @param ucsapId o identificador do UCSAP
     * @return a entrada correspondente ou {@code null} se não encontrada
     */
    public UCSAPEntry getEntry(short ucsapId) {
        return entries.get(ucsapId);
    }

    /**
     * Verifica se existe um UCSAP com o ID especificado.
     *
     * @param ucsapId o identificador a ser verificado
     * @return {@code true} se o UCSAP existe, {@code false} caso contrário
     */
    public boolean exists(short ucsapId) {
        return entries.containsKey(ucsapId);
    }

    /**
     * Retorna o número total de entradas UCSAP.
     *
     * @return o tamanho da configuração
     */
    public int size() {
        return entries.size();
    }

    /**
     * Retorna todas as entradas UCSAP.
     *
     * @return um mapa não modificável contendo todas as entradas
     */
    public Map<Short, UCSAPEntry> getAllEntries() {
        return Collections.unmodifiableMap(entries);
    }
}