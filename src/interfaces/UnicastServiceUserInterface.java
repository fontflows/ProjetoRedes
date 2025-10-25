package interfaces;

/**
 * Interface implementada pela aplicação para receber notificações do protocolo unicast.
 * <p>
 * Define o callback que será invocado quando uma mensagem for recebida.
 * </p>
 */
public interface UnicastServiceUserInterface {
    /**
     * Notifica a chegada de uma mensagem.
     *
     * @param source o UCSAP de origem da mensagem
     * @param message o conteúdo da mensagem recebida
     */
    void UPDataInd(short source, String message);
}