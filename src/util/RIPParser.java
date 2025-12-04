package util;

import pdu.RIPGetPDU;
import pdu.RIPSetPDU;
import pdu.RIPNotificationPDU;
import pdu.RIPIndicationPDU;
import pdu.RIPRequestPDU;
import pdu.RIPResponsePDU;

/**
 * Utilitário para validação e extração de dados das PDUs do RIP.
 * <p>
 * Fornece métodos auxiliares para verificar e converter PDUs em objetos.
 * </p>
 */
public class RIPParser {
    /**
     * Identifica o tipo de PDU a partir da string.
     *
     * @param data string PDU
     * @return nome do tipo ou {@code null} se inválido
     */
    public static String identify(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        if (data.startsWith("RIPGET ")) return "RIPGET";
        if (data.startsWith("RIPSET ")) return "RIPSET";
        if (data.startsWith("RIPNTF ")) return "RIPNTF";
        if (data.startsWith("RIPIND ")) return "RIPIND";
        if (data.equals("RIPRQT")) return "RIPRQT";
        if (data.startsWith("RIPRSP ")) return "RIPRSP";
        return null;
    }

    /**
     * Valida uma PDU do RIP.
     *
     * @param data string PDU
     * @return {@code true} se válida, {@code false} caso contrário
     */
    public static boolean isValid(String data) {
        try {
            String t = identify(data);
            if (t == null) return false;
            switch (t) {
                case "RIPGET" -> RIPGetPDU.parse(data);
                case "RIPSET" -> RIPSetPDU.parse(data);
                case "RIPNTF" -> RIPNotificationPDU.parse(data);
                case "RIPIND" -> RIPIndicationPDU.parse(data);
                case "RIPRQT" -> RIPRequestPDU.parse(data);
                case "RIPRSP" -> RIPResponsePDU.parse(data);
                default -> { return false; }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}