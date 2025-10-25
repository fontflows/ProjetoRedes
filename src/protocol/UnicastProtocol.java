package protocol;

import interfaces.UnicastServiceInterface;
import interfaces.UnicastServiceUserInterface;
import model.UCSAPConfig;
import model.UCSAPEntry;
import pdu.UPDataRequestPDU;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementação do protocolo de comunicação unicast sobre UDP.
 * <p>
 * Esta classe gerencia o envio e recebimento de mensagens entre UCSAPs,
 * utilizando DatagramSockets para comunicação UDP. Opera em duas threads:
 * uma para envio e outra para recebimento de mensagens.
 * </p>
 */
public class UnicastProtocol extends Thread implements UnicastServiceInterface {
    /** Identificador do UCSAP local */
    private final short myUcsapId;

    /** Configuração de todos os UCSAPs na rede */
    private final UCSAPConfig config;

    /** Socket UDP para comunicação */
    private final DatagramSocket socket;

    /** Interface de callback para notificar a aplicação */
    private final UnicastServiceUserInterface user;

    /** Fila de mensagens a serem enviadas */
    private final BlockingQueue<SendRequest> sendQueue;

    /** Flag que controla a execução das threads */
    private volatile boolean running;

    /** Contador de mensagens enviadas */
    private final AtomicLong messagesSent;

    /** Contador de mensagens recebidas */
    private final AtomicLong messagesReceived;

    /** Cache de endereços IP resolvidos para evitar lookups DNS repetidos */
    private final Map<String, InetAddress> addressCache;

    /** Cache de mapeamento porta→UCSAP ID para lookup rápido em localhost */
    private final Map<Integer, Short> portToUcsapCache;

    /** Pool de buffers reutilizáveis para reduzir alocações de memória */
    private final BlockingQueue<byte[]> bufferPool;

    /** Tamanho máximo do pool de buffers */
    private static final int MAX_POOL_SIZE = 10;

    /**
     * Classe interna que representa uma requisição de envio.
     */
    private static class SendRequest {
        /** UCSAP de destino */
        final short destination;

        /** Mensagem a ser enviada */
        final String message;

        /**
         * Cria uma nova requisição de envio.
         *
         * @param destination o UCSAP de destino
         * @param message a mensagem a ser enviada
         */
        SendRequest(short destination, String message) {
            this.destination = destination;
            this.message = message;
        }
    }

    /**
     * Constrói e inicializa o protocolo unicast.
     *
     * @param myUcsapId o identificador do UCSAP local
     * @param config a configuração contendo todos os UCSAPs
     * @param user a interface de callback para notificações
     * @throws IOException se houver erro ao criar o socket ou se o UCSAP não existir na configuração
     */
    public UnicastProtocol(short myUcsapId, UCSAPConfig config, UnicastServiceUserInterface user) throws IOException {
        this.myUcsapId = myUcsapId;
        this.config = config;
        this.user = user;
        this.sendQueue = new LinkedBlockingQueue<>();
        this.running = true;
        this.messagesSent = new AtomicLong(0);
        this.messagesReceived = new AtomicLong(0);

        this.addressCache = new HashMap<>();
        this.portToUcsapCache = new HashMap<>();
        this.bufferPool = new LinkedBlockingQueue<>(MAX_POOL_SIZE);

        for (int i = 0; i < MAX_POOL_SIZE; i++) {
            bufferPool.offer(new byte[1024]);
        }

        for (UCSAPEntry entry : config.getAllEntries().values()) {
            portToUcsapCache.put(entry.getPort(), entry.getUcsapId());
        }

        UCSAPEntry myEntry = config.getEntry(myUcsapId);
        if (myEntry == null) {
            throw new IOException("UCSAP ID " + myUcsapId + " não encontrado no arquivo de configuração");
        }

        this.socket = new DatagramSocket(myEntry.getPort());
        this.socket.setSoTimeout(100);

        log("Protocolo iniciado: " + myEntry);
    }

    /**
     * Requisita o envio de uma mensagem para um UCSAP de destino.
     *
     * @param destination o UCSAP de destino
     * @param message a mensagem a ser enviada
     * @return {@code true} se a requisição foi aceita, {@code false} caso contrário
     */
    @Override
    public boolean UPDataReq(short destination, String message) {
        if (message == null) {
            log("Erro: mensagem vazia");
            return false;
        }

        if (!config.exists(destination)) {
            log("Erro: destino " + destination + " não existe");
            return false;
        }

        try {
            UPDataRequestPDU testPdu = new UPDataRequestPDU(message);
            testPdu.encode();
        } catch (IllegalArgumentException e) {
            log("Erro: " + e.getMessage());
            return false;
        }

        try {
            sendQueue.put(new SendRequest(destination, message));
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * Loop principal da thread de envio.
     * <p>
     * Inicia a thread de recebimento e processa requisições de envio da fila.
     * </p>
     */
    @Override
    public void run() {
        Thread receiveThread = new Thread(this::receiveLoop, "ReceiveThread-" + myUcsapId);
        receiveThread.setDaemon(true);
        receiveThread.start();

        while (running) {
            try {
                SendRequest req = sendQueue.take();
                sendMessage(req.destination, req.message);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * Envia uma mensagem para o UCSAP de destino.
     * <p>
     * Utiliza cache de endereços para evitar resoluções DNS repetidas,
     * melhorando significativamente o desempenho em comunicações frequentes.
     * </p>
     *
     * @param destination o UCSAP de destino
     * @param message a mensagem a ser enviada
     */
    private void sendMessage(short destination, String message) {
        try {
            UCSAPEntry destEntry = config.getEntry(destination);
            if (destEntry == null) {
                return;
            }

            UPDataRequestPDU pdu = new UPDataRequestPDU(message);
            String encodedPDU = pdu.encode();
            byte[] data = encodedPDU.getBytes();

            InetAddress address = addressCache.get(destEntry.getHostname());
            if (address == null) {
                address = InetAddress.getByName(destEntry.getHostname());
                addressCache.put(destEntry.getHostname(), address);
            }

            DatagramPacket packet = new DatagramPacket(data, data.length, address, destEntry.getPort());
            socket.send(packet);
            messagesSent.incrementAndGet();
            log("Mensagem enviada para UCSAP " + destination);

        } catch (Exception e) {
            log("Erro ao enviar mensagem: " + e.getMessage());
        }
    }

    /**
     * Loop de recebimento de mensagens.
     * <p>
     * Recebe pacotes UDP, decodifica as PDUs e notifica a aplicação.
     * Utiliza pool de buffers reutilizáveis para reduzir drasticamente
     * a pressão sobre o garbage collector.
     * </p>
     */
    private void receiveLoop() {
        while (running) {
            byte[] buffer = bufferPool.poll();
            if (buffer == null) {
                buffer = new byte[1024];
            }

            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String received = new String(packet.getData(), 0, packet.getLength());
                UPDataRequestPDU pdu = UPDataRequestPDU.parse(received);

                String sourceAddress = normalizeAddress(packet.getAddress().getHostAddress());
                int sourcePort = packet.getPort();
                short source = findSourceByAddress(sourceAddress, sourcePort);

                if (source >= 0) {
                    messagesReceived.incrementAndGet();
                    log("Mensagem recebida de UCSAP " + source);
                    user.UPDataInd(source, pdu.getData());
                } else {
                    log("Mensagem de origem desconhecida: " + sourceAddress + ":" + sourcePort);
                }

            } catch (SocketTimeoutException e) {
            } catch (Exception e) {
                if (running) {
                    log("Erro ao receber mensagem: " + e.getMessage());
                }
            } finally {
                if (buffer != null && bufferPool.size() < MAX_POOL_SIZE) {
                    bufferPool.offer(buffer);
                }
            }
        }
    }

    /**
     * Normaliza endereços IP para facilitar comparações.
     *
     * @param address o endereço a ser normalizado
     * @return o endereço normalizado
     */
    private String normalizeAddress(String address) {
        if ("127.0.0.1".equals(address) || "localhost".equalsIgnoreCase(address)) {
            return "127.0.0.1";
        }
        if ("0:0:0:0:0:0:0:1".equals(address) || "::1".equals(address)) {
            return "127.0.0.1";
        }
        return address;
    }

    /**
     * Identifica o UCSAP de origem com base no endereço e porta.
     * <p>
     * Otimizado com cache para lookup O(1) em comunicações localhost,
     * que é o caso mais comum em testes e desenvolvimento.
     * </p>
     *
     * @param address o endereço IP de origem
     * @param port a porta de origem
     * @return o ID do UCSAP ou -1 se não encontrado
     */
    private short findSourceByAddress(String address, int port) {
        if ("127.0.0.1".equals(address)) {
            Short cached = portToUcsapCache.get(port);
            if (cached != null) {
                return cached;
            }
        }

        for (UCSAPEntry entry : config.getAllEntries().values()) {
            if (entry.getPort() == port) {
                try {
                    String entryAddress = InetAddress.getByName(entry.getHostname()).getHostAddress();
                    entryAddress = normalizeAddress(entryAddress);

                    if (entryAddress.equals(address)) {
                        return entry.getUcsapId();
                    }
                } catch (Exception e) {
                }
            }
        }
        return -1;
    }

    /**
     * Registra uma mensagem de log com o ID do UCSAP.
     *
     * @param message a mensagem a ser registrada
     */
    private void log(String message) {
        System.out.println("[UCSAP " + myUcsapId + "] " + message);
    }

    /**
     * Encerra o protocolo de forma ordenada.
     * <p>
     * Exibe estatísticas, fecha o socket e interrompe as threads.
     * </p>
     */
    public void shutdown() {
        log("Encerrando protocolo");
        log("Estatísticas - Enviadas: " + messagesSent.get() + ", Recebidas: " + messagesReceived.get());
        running = false;
        socket.close();
        this.interrupt();
    }
}