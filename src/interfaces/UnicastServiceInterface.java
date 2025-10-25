package interfaces;

/**
 * Interface do serviço de comunicação unicast.
 * <p>
 * Define as operações disponíveis para envio de mensagens no protocolo unicast.
 * </p>
 */
public interface UnicastServiceInterface {
    /**
     * Requisita o envio de uma mensagem para um UCSAP de destino.
     *
     * @param destination o UCSAP de destino
     * @param message a mensagem a ser enviada
     * @return {@code true} se a requisição foi aceita, {@code false} caso contrário
     */
    boolean UPDataReq(short destination, String message);
}