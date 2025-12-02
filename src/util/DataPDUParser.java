package util;

import pdu.UPDataRequestPDU;

/**
 * Utilitário para validação e extração de dados de PDUs.
 * <p>
 * Fornece métodos auxiliares para verificar se uma string representa
 * uma PDU válida e para extrair os dados contidos na PDU.
 * </p>
 */
public class DataPDUParser {

    /**
     * Valida se uma string representa uma PDU válida.
     *
     * @param pdu a string a ser validada
     * @return {@code true} se a string for uma PDU válida, {@code false} caso contrário
     */
    public static boolean isValidPDU(String pdu) {
        if (pdu == null || pdu.isEmpty()) {
            return false;
        }

        try {
            UPDataRequestPDU.parse(pdu);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extrai os dados de uma PDU.
     *
     * @param pdu a string PDU da qual extrair os dados
     * @return os dados contidos na PDU
     * @throws IllegalArgumentException se a PDU for inválida
     */
    public static String extractData(String pdu) throws IllegalArgumentException {
        UPDataRequestPDU parsed = UPDataRequestPDU.parse(pdu);
        return parsed.getData();
    }
}