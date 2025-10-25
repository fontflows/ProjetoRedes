import pdu.UPDataRequestPDU;
import util.PDUParser;

/**
 * Testes unitários para a classe PDUParser e UPDataRequestPDU.
 */
public class PDUParserTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("=== Testes PDUParser ===\n");

        testEncodeBasic();
        testEncodeWithSpaces();
        testEncodeEmpty();
        testParseValid();
        testParseWithSpaces();
        testParseInvalidFormat();
        testParseInvalidSize();
        testParseNullOrEmpty();
        testValidationValid();
        testValidationInvalid();
        testExtractData();
        testMaxSize();
        testSpecialCharacters();

        printResults();
    }

    private static void testEncodeBasic() {
        try {
            UPDataRequestPDU pdu = new UPDataRequestPDU("teste");
            String encoded = pdu.encode();

            assertEqual(encoded, "UPDREQPDU 5 teste", "Codificação básica incorreta");
            assertEqual(pdu.getData(), "teste", "Dados incorretos");
            assertEqual(pdu.getSize(), 5, "Tamanho incorreto");

            pass("testEncodeBasic");
        } catch (Exception e) {
            fail("testEncodeBasic", e.getMessage());
        }
    }

    private static void testEncodeWithSpaces() {
        try {
            UPDataRequestPDU pdu = new UPDataRequestPDU("ola mundo");
            String encoded = pdu.encode();

            assertEqual(encoded, "UPDREQPDU 9 ola mundo", "Codificação com espaços incorreta");
            assertEqual(pdu.getSize(), 9, "Tamanho com espaços incorreto");

            pass("testEncodeWithSpaces");
        } catch (Exception e) {
            fail("testEncodeWithSpaces", e.getMessage());
        }
    }

    private static void testEncodeEmpty() {
        try {
            UPDataRequestPDU pdu = new UPDataRequestPDU("");
            String encoded = pdu.encode();

            assertEqual(encoded, "UPDREQPDU 0 ", "Codificação vazia incorreta");
            assertEqual(pdu.getSize(), 0, "Tamanho vazio deveria ser 0");

            pass("testEncodeEmpty");
        } catch (Exception e) {
            fail("testEncodeEmpty", e.getMessage());
        }
    }

    private static void testParseValid() {
        try {
            UPDataRequestPDU parsed = UPDataRequestPDU.parse("UPDREQPDU 5 teste");

            assertEqual(parsed.getData(), "teste", "Dados parseados incorretos");
            assertEqual(parsed.getSize(), 5, "Tamanho parseado incorreto");

            pass("testParseValid");
        } catch (Exception e) {
            fail("testParseValid", e.getMessage());
        }
    }

    private static void testParseWithSpaces() {
        try {
            UPDataRequestPDU parsed = UPDataRequestPDU.parse("UPDREQPDU 11 hello world");

            assertEqual(parsed.getData(), "hello world", "Dados com espaços incorretos");
            assertEqual(parsed.getSize(), 11, "Tamanho com espaços incorreto");

            pass("testParseWithSpaces");
        } catch (Exception e) {
            fail("testParseWithSpaces", e.getMessage());
        }
    }

    private static void testParseInvalidFormat() {
        try {
            UPDataRequestPDU.parse("INVALIDA 5 teste");
            fail("testParseInvalidFormat", "Deveria lançar exceção para formato inválido");
        } catch (IllegalArgumentException e) {
            pass("testParseInvalidFormat");
        } catch (Exception e) {
            fail("testParseInvalidFormat", "Exceção inesperada: " + e.getMessage());
        }
    }

    private static void testParseInvalidSize() {
        try {
            UPDataRequestPDU.parse("UPDREQPDU 10 teste");
            fail("testParseInvalidSize", "Deveria lançar exceção para tamanho incorreto");
        } catch (IllegalArgumentException e) {
            pass("testParseInvalidSize");
        } catch (Exception e) {
            fail("testParseInvalidSize", "Exceção inesperada: " + e.getMessage());
        }
    }

    private static void testParseNullOrEmpty() {
        try {
            PDUParser.isValidPDU(null);
            assertEqual(PDUParser.isValidPDU(null), false, "Null deveria ser inválido");
            assertEqual(PDUParser.isValidPDU(""), false, "String vazia deveria ser inválida");

            pass("testParseNullOrEmpty");
        } catch (Exception e) {
            fail("testParseNullOrEmpty", e.getMessage());
        }
    }

    private static void testValidationValid() {
        try {
            assertEqual(PDUParser.isValidPDU("UPDREQPDU 5 teste"), true, "PDU válida deveria passar");
            assertEqual(PDUParser.isValidPDU("UPDREQPDU 0 "), true, "PDU vazia deveria passar");
            assertEqual(PDUParser.isValidPDU("UPDREQPDU 11 hello world"), true, "PDU com espaços deveria passar");

            pass("testValidationValid");
        } catch (Exception e) {
            fail("testValidationValid", e.getMessage());
        }
    }

    private static void testValidationInvalid() {
        try {
            assertEqual(PDUParser.isValidPDU("INVALIDA 5 teste"), false, "Formato inválido");
            assertEqual(PDUParser.isValidPDU("UPDREQPDU abc teste"), false, "Tamanho não numérico");
            assertEqual(PDUParser.isValidPDU("UPDREQPDU 10 teste"), false, "Tamanho incorreto");
            assertEqual(PDUParser.isValidPDU(null), false, "Null");
            assertEqual(PDUParser.isValidPDU(""), false, "String vazia");

            pass("testValidationInvalid");
        } catch (Exception e) {
            fail("testValidationInvalid", e.getMessage());
        }
    }

    private static void testExtractData() {
        try {
            String data = PDUParser.extractData("UPDREQPDU 12 dados teste ");
            assertEqual(data, "dados teste ", "Extração de dados incorreta");

            pass("testExtractData");
        } catch (Exception e) {
            fail("testExtractData", e.getMessage());
        }
    }

    private static void testMaxSize() {
        try {
            StringBuilder largeData = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                largeData.append("a");
            }

            UPDataRequestPDU pdu = new UPDataRequestPDU(largeData.toString());
            String encoded = pdu.encode();

            assertEqual(encoded.length() <= 1024, true, "PDU não deveria exceder 1024 bytes");

            pass("testMaxSize");
        } catch (Exception e) {
            fail("testMaxSize", e.getMessage());
        }
    }

    private static void testSpecialCharacters() {
        try {
            UPDataRequestPDU pdu = new UPDataRequestPDU("teste@#$%123");
            String encoded = pdu.encode();
            UPDataRequestPDU parsed = UPDataRequestPDU.parse(encoded);

            assertEqual(parsed.getData(), "teste@#$%123", "Caracteres especiais perdidos");

            pass("testSpecialCharacters");
        } catch (Exception e) {
            fail("testSpecialCharacters", e.getMessage());
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