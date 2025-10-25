import model.UCSAPConfig;
import model.UCSAPEntry;

/**
 * Testes unitários para a classe UCSAPConfig.
 */
public class UCSAPConfigTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("=== Testes UCSAPConfig ===\n");

        testAddAndRetrieve();
        testExists();
        testSize();
        testGetAllEntries();
        testNullEntry();
        testMultipleEntries();
        testOverwriteEntry();
        testNonExistentEntry();

        printResults();
    }

    private static void testAddAndRetrieve() {
        try {
            UCSAPConfig config = new UCSAPConfig();
            UCSAPEntry entry = new UCSAPEntry((short) 1, "localhost", 1150);
            config.addEntry(entry);

            UCSAPEntry retrieved = config.getEntry((short) 1);
            assertEqual(retrieved != null, true, "Entrada deveria existir");
            assertEqual(retrieved.getUcsapId(), (short) 1, "UCSAP ID incorreto");
            assertEqual(retrieved.getHostname(), "localhost", "Hostname incorreto");
            assertEqual(retrieved.getPort(), 1150, "Porta incorreta");

            pass("testAddAndRetrieve");
        } catch (AssertionError e) {
            fail("testAddAndRetrieve", e.getMessage());
        }
    }

    private static void testExists() {
        try {
            UCSAPConfig config = new UCSAPConfig();
            config.addEntry(new UCSAPEntry((short) 5, "localhost", 2000));

            assertEqual(config.exists((short) 5), true, "UCSAP 5 deveria existir");
            assertEqual(config.exists((short) 10), false, "UCSAP 10 não deveria existir");

            pass("testExists");
        } catch (AssertionError e) {
            fail("testExists", e.getMessage());
        }
    }

    private static void testSize() {
        try {
            UCSAPConfig config = new UCSAPConfig();
            assertEqual(config.size(), 0, "Config vazia deveria ter tamanho 0");

            config.addEntry(new UCSAPEntry((short) 1, "localhost", 1100));
            assertEqual(config.size(), 1, "Config deveria ter tamanho 1");

            config.addEntry(new UCSAPEntry((short) 2, "localhost", 1200));
            assertEqual(config.size(), 2, "Config deveria ter tamanho 2");

            pass("testSize");
        } catch (AssertionError e) {
            fail("testSize", e.getMessage());
        }
    }

    private static void testGetAllEntries() {
        try {
            UCSAPConfig config = new UCSAPConfig();
            config.addEntry(new UCSAPEntry((short) 1, "localhost", 1100));
            config.addEntry(new UCSAPEntry((short) 2, "192.168.1.10", 1200));

            var entries = config.getAllEntries();
            assertEqual(entries.size(), 2, "Deveria retornar 2 entradas");
            assertEqual(entries.containsKey((short) 1), true, "Deveria conter UCSAP 1");
            assertEqual(entries.containsKey((short) 2), true, "Deveria conter UCSAP 2");

            pass("testGetAllEntries");
        } catch (AssertionError e) {
            fail("testGetAllEntries", e.getMessage());
        }
    }

    private static void testNullEntry() {
        try {
            UCSAPConfig config = new UCSAPConfig();
            UCSAPEntry result = config.getEntry((short) 999);

            assertEqual(result, null, "Deveria retornar null para UCSAP inexistente");

            pass("testNullEntry");
        } catch (AssertionError e) {
            fail("testNullEntry", e.getMessage());
        }
    }

    private static void testMultipleEntries() {
        try {
            UCSAPConfig config = new UCSAPConfig();

            for (short i = 0; i < 10; i++) {
                config.addEntry(new UCSAPEntry(i, "host" + i, 1100 + i));
            }

            assertEqual(config.size(), 10, "Deveria ter 10 entradas");

            for (short i = 0; i < 10; i++) {
                UCSAPEntry entry = config.getEntry(i);
                assertEqual(entry != null, true, "Entrada " + i + " deveria existir");
                assertEqual(entry.getUcsapId(), i, "UCSAP ID incorreto para entrada " + i);
            }

            pass("testMultipleEntries");
        } catch (AssertionError e) {
            fail("testMultipleEntries", e.getMessage());
        }
    }

    private static void testOverwriteEntry() {
        try {
            UCSAPConfig config = new UCSAPConfig();
            config.addEntry(new UCSAPEntry((short) 1, "localhost", 1100));
            config.addEntry(new UCSAPEntry((short) 1, "newhost", 2000));

            UCSAPEntry entry = config.getEntry((short) 1);
            assertEqual(entry.getHostname(), "newhost", "Hostname deveria ser atualizado");
            assertEqual(entry.getPort(), 2000, "Porta deveria ser atualizada");
            assertEqual(config.size(), 1, "Tamanho deveria continuar 1");

            pass("testOverwriteEntry");
        } catch (AssertionError e) {
            fail("testOverwriteEntry", e.getMessage());
        }
    }

    private static void testNonExistentEntry() {
        try {
            UCSAPConfig config = new UCSAPConfig();
            config.addEntry(new UCSAPEntry((short) 0, "localhost", 1100));

            assertEqual(config.exists((short) -1), false, "UCSAP negativo não deveria existir");
            assertEqual(config.exists((short) 100), false, "UCSAP 100 não deveria existir");
            assertEqual(config.getEntry((short) 50), null, "Deveria retornar null");

            pass("testNonExistentEntry");
        } catch (AssertionError e) {
            fail("testNonExistentEntry", e.getMessage());
        }
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