import interfaces.UnicastServiceUserInterface;
import model.UCSAPConfig;
import model.UCSAPEntry;
import protocol.UnicastProtocol;

/**
 * Testes unitários para a classe UnicastProtocol.
 */
public class UnicastProtocolTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    private static class TestUser implements UnicastServiceUserInterface {
        public String lastMessage = null;
        public short lastSource = -1;
        public int messageCount = 0;

        @Override
        public void UPDataInd(short source, String message) {
            this.lastSource = source;
            this.lastMessage = message;
            this.messageCount++;
        }

        public void reset() {
            lastMessage = null;
            lastSource = -1;
            messageCount = 0;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Testes UnicastProtocol ===\n");

        testBasicSendReceive();
        testBidirectionalCommunication();
        testMultipleMessages();
        testInvalidDestination();
        testNullMessage();
        testLargeMessage();
        testMessageOrder();

        printResults();
    }

    private static void testBasicSendReceive() {
        try {
            UCSAPConfig config = createTestConfig();
            TestUser user0 = new TestUser();
            TestUser user1 = new TestUser();

            UnicastProtocol protocol0 = new UnicastProtocol((short) 0, config, user0);
            UnicastProtocol protocol1 = new UnicastProtocol((short) 1, config, user1);

            protocol0.start();
            protocol1.start();
            Thread.sleep(500);

            boolean sent = protocol0.UPDataReq((short) 1, "teste123");
            assertEqual(sent, true, "Envio deveria ter sucesso");

            Thread.sleep(1000);

            assertEqual(user1.lastMessage, "teste123", "Mensagem incorreta");
            assertEqual(user1.lastSource, (short) 0, "Origem incorreta");
            assertEqual(user1.messageCount, 1, "Deveria ter recebido 1 mensagem");

            protocol0.shutdown();
            protocol1.shutdown();

            pass("testBasicSendReceive");
        } catch (Exception e) {
            fail("testBasicSendReceive", e.getMessage());
        }
    }

    private static void testBidirectionalCommunication() {
        try {
            UCSAPConfig config = createTestConfig();
            TestUser user0 = new TestUser();
            TestUser user1 = new TestUser();

            UnicastProtocol protocol0 = new UnicastProtocol((short) 0, config, user0);
            UnicastProtocol protocol1 = new UnicastProtocol((short) 1, config, user1);

            protocol0.start();
            protocol1.start();
            Thread.sleep(500);

            protocol0.UPDataReq((short) 1, "msg de 0");
            Thread.sleep(500);
            protocol1.UPDataReq((short) 0, "msg de 1");
            Thread.sleep(500);

            assertEqual(user1.lastMessage, "msg de 0", "Mensagem 0->1 incorreta");
            assertEqual(user0.lastMessage, "msg de 1", "Mensagem 1->0 incorreta");

            protocol0.shutdown();
            protocol1.shutdown();

            pass("testBidirectionalCommunication");
        } catch (Exception e) {
            fail("testBidirectionalCommunication", e.getMessage());
        }
    }

    private static void testMultipleMessages() {
        try {
            UCSAPConfig config = createTestConfig();
            TestUser user1 = new TestUser();

            UnicastProtocol protocol0 = new UnicastProtocol((short) 0, config, new TestUser());
            UnicastProtocol protocol1 = new UnicastProtocol((short) 1, config, user1);

            protocol0.start();
            protocol1.start();
            Thread.sleep(500);

            int messageCount = 10;
            for (int i = 0; i < messageCount; i++) {
                protocol0.UPDataReq((short) 1, "mensagem " + i);
            }

            Thread.sleep(2000);

            assertEqual(user1.messageCount, messageCount, "Deveria receber todas as mensagens");

            protocol0.shutdown();
            protocol1.shutdown();

            pass("testMultipleMessages");
        } catch (Exception e) {
            fail("testMultipleMessages", e.getMessage());
        }
    }

    private static void testInvalidDestination() {
        try {
            UCSAPConfig config = createTestConfig();
            TestUser user = new TestUser();

            UnicastProtocol protocol = new UnicastProtocol((short) 0, config, user);
            protocol.start();
            Thread.sleep(500);

            boolean sent = protocol.UPDataReq((short) 999, "teste");
            assertEqual(sent, false, "Envio para destino inválido deveria falhar");

            protocol.shutdown();

            pass("testInvalidDestination");
        } catch (Exception e) {
            fail("testInvalidDestination", e.getMessage());
        }
    }

    private static void testNullMessage() {
        try {
            UCSAPConfig config = createTestConfig();
            TestUser user = new TestUser();

            UnicastProtocol protocol = new UnicastProtocol((short) 0, config, user);
            protocol.start();
            Thread.sleep(500);

            boolean sent = protocol.UPDataReq((short) 1, null);
            assertEqual(sent, false, "Envio de mensagem null deveria falhar");

            protocol.shutdown();

            pass("testNullMessage");
        } catch (Exception e) {
            fail("testNullMessage", e.getMessage());
        }
    }

    private static void testLargeMessage() {
        try {
            UCSAPConfig config = createTestConfig();
            TestUser user1 = new TestUser();

            UnicastProtocol protocol0 = new UnicastProtocol((short) 0, config, new TestUser());
            UnicastProtocol protocol1 = new UnicastProtocol((short) 1, config, user1);

            protocol0.start();
            protocol1.start();
            Thread.sleep(500);

            StringBuilder largeMsg = new StringBuilder();
            for (int i = 0; i < 900; i++) {
                largeMsg.append("a");
            }

            boolean sent = protocol0.UPDataReq((short) 1, largeMsg.toString());
            assertEqual(sent, true, "Envio de mensagem grande deveria ter sucesso");

            Thread.sleep(1000);

            assertEqual(user1.lastMessage, largeMsg.toString(), "Mensagem grande incorreta");

            protocol0.shutdown();
            protocol1.shutdown();

            pass("testLargeMessage");
        } catch (Exception e) {
            fail("testLargeMessage", e.getMessage());
        }
    }

    private static void testMessageOrder() {
        try {
            UCSAPConfig config = createTestConfig();
            TestUser user1 = new TestUser();

            UnicastProtocol protocol0 = new UnicastProtocol((short) 0, config, new TestUser());
            UnicastProtocol protocol1 = new UnicastProtocol((short) 1, config, user1);

            protocol0.start();
            protocol1.start();
            Thread.sleep(500);

            protocol0.UPDataReq((short) 1, "primeira");
            Thread.sleep(200);
            protocol0.UPDataReq((short) 1, "segunda");
            Thread.sleep(200);
            protocol0.UPDataReq((short) 1, "terceira");
            Thread.sleep(500);

            assertEqual(user1.lastMessage, "terceira", "Última mensagem deveria ser 'terceira'");
            assertEqual(user1.messageCount, 3, "Deveria ter recebido 3 mensagens");

            protocol0.shutdown();
            protocol1.shutdown();

            pass("testMessageOrder");
        } catch (Exception e) {
            fail("testMessageOrder", e.getMessage());
        }
    }

    private static UCSAPConfig createTestConfig() {
        UCSAPConfig config = new UCSAPConfig();
        config.addEntry(new UCSAPEntry((short) 0, "localhost", 3000));
        config.addEntry(new UCSAPEntry((short) 1, "localhost", 3001));
        return config;
    }

    private static void assertEqual(Object actual, Object expected, String message) {
        if (actual == null && expected == null) return;
        if (actual == null || !actual.equals(expected)) {
            throw new AssertionError(message + " (esperado: " + expected + ", obtido: " + actual + ")");
        }
    }

    private static void pass(String testName) {
        testsPassed++;
        System.out.println("✓ " + testName);
    }

    private static void fail(String testName, String reason) {
        testsFailed++;
        System.out.println("✗ " + testName + ": " + reason);
    }

    private static void printResults() {
        System.out.println("\n--- Resultados ---");
        System.out.println("Testes executados: " + (testsPassed + testsFailed));
        System.out.println("Sucessos: " + testsPassed);
        System.out.println("Falhas: " + testsFailed);

        if (testsFailed == 0) {
            System.out.println("\nTodos os testes passaram!");
        } else {
            System.exit(1);
        }
    }
}