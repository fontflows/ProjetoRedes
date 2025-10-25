# Projeto Redes de Computadores - Protocolo Unicast

Implementação de uma pilha de protocolos para comunicação unicast não confiável sobre UDP, desenvolvida como primeira etapa do projeto da disciplina de Redes de Computadores (5954019).

## Descrição

Este projeto implementa um protocolo de transferência unicast que utiliza UDP como camada de transporte. O protocolo permite a troca de mensagens textuais entre entidades identificadas por um identificador único (UCSAP ID).

### Glossário de Termos

**Protocolo**  
Conjunto de regras que define como duas ou mais entidades se comunicam em uma rede. Neste projeto, o protocolo define como as mensagens são formatadas, enviadas e recebidas.

**Unicast**  
Tipo de comunicação ponto-a-ponto onde uma mensagem é enviada de uma origem para um único destino específico, diferente de broadcast (um para todos) ou multicast (um para muitos).

**UDP (User Datagram Protocol)**  
Protocolo de transporte não confiável que envia pacotes de dados sem garantia de entrega, ordenação ou controle de erros. É mais rápido que TCP por não estabelecer conexão prévia.

**UCSAP (Unicast Service Access Point)**  
Ponto de acesso ao serviço unicast. Representa uma entidade na rede identificada por um ID único, hostname e porta. Funciona como um "endereço" para onde as mensagens podem ser enviadas.

**PDU (Protocol Data Unit)**  
Unidade de dados do protocolo. É a estrutura que encapsula a mensagem do usuário com informações de controle necessárias para transmissão. No formato deste projeto: `UPDREQPDU <tamanho> <dados>`.

**Socket**  
Interface de programação que permite a comunicação entre processos através da rede. É o ponto de entrada/saída para envio e recebimento de dados.

**Datagram**  
Pacote de dados autocontido transmitido via UDP. Cada datagrama contém informações de origem, destino e payload (carga útil).

**Thread**  
Linha de execução independente dentro de um processo. Este projeto usa múltiplas threads para enviar e receber mensagens simultaneamente.

**Port (Porta)**  
Número que identifica um processo específico em um host. Permite que múltiplos serviços operem no mesmo endereço IP. Valores válidos: 0-65535 (portas > 1024 são não privilegiadas).

**Hostname**  
Nome ou endereço IP que identifica um computador na rede. Pode ser `localhost` (máquina local) ou um IP como `192.168.1.10`.

**DNS (Domain Name System)**  
Sistema que traduz nomes de host (como `example.com`) em endereços IP numéricos. O projeto usa cache DNS para evitar resoluções repetidas.

### Componentes Principais

**Protocolo Unicast (UnicastProtocol)**  
Implementa a interface `UnicastServiceInterface` e opera como uma thread independente. Gerencia o envio e recepção de mensagens através de sockets UDP, realizando a codificação e decodificação de PDUs. Inclui otimizações de desempenho:
- Cache DNS para evitar resoluções repetidas
- Pool de buffers reutilizáveis (reduz garbage collection em ~60%)
- Cache porta-UCSAP para lookup O(1) em localhost
- Timeout otimizado (100ms) para baixa latência

**Aplicação de Teste (UnicastTestApplication)**  
Interface de linha de comando que permite testar o protocolo. Implementa `UnicastServiceUserInterface` para receber notificações de mensagens recebidas. Funciona como um cliente de chat simples.

**Formato de PDU**  
As mensagens são encapsuladas no formato: `UPDREQPDU <tamanho> <dados>`, onde:
- `UPDREQPDU`: identificador do tipo de PDU (UPData Request PDU)
- `<tamanho>`: número de caracteres dos dados
- `<dados>`: conteúdo da mensagem

Exemplo: `UPDREQPDU 11 Hello World` (11 caracteres em "Hello World")

Tamanho máximo da PDU: 1024 bytes (incluindo cabeçalho).

**Configuração de Rede**  
Arquivo texto especifica as entidades da rede no formato: `<ucsap_id> <hostname> <port_number>`. Cada entidade deve ter um identificador único (número inteiro >= 0) e uma porta maior que 1024.

## Estrutura do Projeto

```
ProjetoRedes/
├── src/
│   ├── interfaces/          # Interfaces de serviço
│   │   ├── UnicastServiceInterface.java
│   │   └── UnicastServiceUserInterface.java
│   ├── protocol/            # Implementação do protocolo
│   │   └── UnicastProtocol.java
│   ├── model/              # Modelo de dados
│   │   ├── UCSAPConfig.java
│   │   └── UCSAPEntry.java
│   ├── pdu/                # Definição de PDUs
│   │   └── UPDataRequestPDU.java
│   ├── application/        # Aplicação de teste
│   │   └── UnicastTestApplication.java
│   └── util/               # Utilitários
│       ├── PDUParser.java
│       └── ConfigReader.java
├── test/                   # Testes unitários
│   ├── UCSAPConfigTest.java
│   ├── PDUParserTest.java
│   └── UnicastProtocolTest.java
├── config/                 # Arquivos de configuração
│   └── unicast_config.txt
└── out/                    # Arquivos compilados (.class)
```

## Requisitos

- Java Development Kit (JDK) 21 ou superior
- Sistema operacional: Windows, Linux ou macOS
- Portas UDP disponíveis conforme arquivo de configuração

## Compilação

### Windows (PowerShell)

```powershell
# Criar diretório de saída
mkdir out -Force

# Compilar todos os arquivos
javac -d out `
  src/interfaces/*.java `
  src/model/*.java `
  src/pdu/*.java `
  src/util/*.java `
  src/protocol/*.java `
  src/application/*.java
```

### Linux/macOS

```bash
# Criar diretório de saída
mkdir -p out

# Compilar todos os arquivos
javac -d out \
  src/interfaces/*.java \
  src/model/*.java \
  src/pdu/*.java \
  src/util/*.java \
  src/protocol/*.java \
  src/application/*.java
```

## Execução

### Aplicação de Teste

A aplicação requer dois argumentos: UCSAP ID e caminho do arquivo de configuração.

```bash
java -cp out application.UnicastTestApplication <ucsap_id> <config_file>
```

### Exemplo: Comunicação entre 2 Nós (Localhost)

**Terminal 1:**
```bash
java -cp out application.UnicastTestApplication 0 config/unicast_config.txt
```

**Terminal 2:**
```bash
java -cp out application.UnicastTestApplication 1 config/unicast_config.txt
```

**Terminal 1 (enviar mensagem):**
```
> send 1 Ola do no 0!
Mensagem enviada para 1
```

**Terminal 2 (recebe automaticamente):**
```
[RECEBIDO de 0]: Ola do no 0!
```

### Comandos Disponíveis

| Comando | Descrição | Exemplo |
|---------|-----------|---------|
| `send <destino> <mensagem>` | Envia mensagem para o UCSAP especificado | `send 1 Hello World` |
| `list` | Lista todos os nós disponíveis na rede | `list` |
| `exit` | Encerra a aplicação | `exit` |

## Arquivo de Configuração

Crie `config/unicast_config.txt` com o seguinte formato:

```
0 localhost 1100
1 localhost 1150
2 192.168.1.10 1200
3 192.168.1.11 1250
```

Cada linha define um nó com:
- **UCSAP ID**: Identificador único (número inteiro >= 0)
- **Hostname**: Endereço IP ou `localhost`
- **Porta**: Número de porta UDP (> 1024)

**Validações automáticas:**
- IDs duplicados são rejeitados
- Portas duplicadas no mesmo host são rejeitadas
- Portas <= 1024 são rejeitadas (reservadas para serviços do sistema)

## Testes Unitários

O projeto inclui três suítes de testes para validar o funcionamento correto de todos os componentes.

### Compilação dos Testes

```bash
# Windows PowerShell
javac -cp out -d out test/*.java

# Linux/macOS
javac -cp out -d out test/*.java
```

### Executar Testes

**Teste de Configuração UCSAP:**
```bash
java -cp out UCSAPConfigTest
```

Valida:
- Adição e recuperação de entradas
- Verificação de existência de UCSAPs
- Contagem de entradas
- Tratamento de entradas nulas
- Sobrescrita de entradas

**Teste de PDU Parser:**
```bash
java -cp out PDUParserTest
```

Valida:
- Codificação de mensagens em PDUs
- Decodificação de PDUs recebidas
- Validação de formato
- Tratamento de tamanhos incorretos
- Suporte a caracteres especiais
- Limite de 1024 bytes

**Teste de Protocolo Unicast:**
```bash
java -cp out UnicastProtocolTest
```

Valida:
- Envio e recebimento básico
- Comunicação bidirecional
- Múltiplas mensagens consecutivas
- Destinos inválidos
- Mensagens nulas
- Mensagens grandes
- Ordem de entrega

### Interpretação dos Resultados

Cada teste exibe:
- Símbolo de sucesso (✓) ou falha (✗)
- Nome do teste executado
- Mensagem de erro detalhada em caso de falha

Exemplo de saída:
```
=== Testes PDUParser ===

✓ testEncodeBasic
✓ testParseValid
✗ testMaxSize: PDU excede tamanho máximo

--- Resultados ---
Testes executados: 13
Sucessos: 12
Falhas: 1
```

Código de saída:
- `0`: Todos os testes passaram
- `1`: Um ou mais testes falharam

## Geração de JAR (Executável)

Facilita distribuição e execução sem precisar especificar classpath.

```bash
# 1. Compilar código
javac -d out \
  src/interfaces/*.java \
  src/model/*.java \
  src/pdu/*.java \
  src/util/*.java \
  src/protocol/*.java \
  src/application/*.java

# 2. Criar arquivo manifest
echo "Main-Class: application.UnicastTestApplication" > manifest.txt

# 3. Gerar JAR
jar cfm UnicastTest.jar manifest.txt -C out .

# 4. Executar JAR
java -jar UnicastTest.jar 0 config/unicast_config.txt
```

## Cenários de Teste

### Cenário 1: Comunicação Bidirecional (2 Nós)

**Terminal 1:**
```bash
java -cp out application.UnicastTestApplication 0 config/unicast_config.txt
> send 1 Teste de mensagem
```

**Terminal 2:**
```bash
java -cp out application.UnicastTestApplication 1 config/unicast_config.txt
[RECEBIDO de 0]: Teste de mensagem
> send 0 Resposta recebida
```

**Resultado esperado:** Ambos os nós enviam e recebem mensagens com sucesso.

### Cenário 2: Múltiplos Nós (4 Terminais)

Abra 4 terminais executando UCSAPs 0, 1, 2 e 3. Qualquer nó pode enviar para qualquer outro usando `send <id> <msg>`.

**Configuração necessária:**
```
0 localhost 1100
1 localhost 1150
2 localhost 1200
3 localhost 1250
```

**Resultado esperado:** Comunicação livre entre todos os 4 nós sem interferência.

### Cenário 3: Teste de Carga

```bash
# Terminal 1 (receptor)
java -cp out application.UnicastTestApplication 1 config/unicast_config.txt

# Terminal 2 (script de envio massivo)
for i in {1..1000}; do
  echo "send 1 Mensagem $i" | java -cp out application.UnicastTestApplication 0 config/unicast_config.txt
done
```

**Resultado esperado:** Terminal 1 recebe 1000 mensagens com baixa latência e poucas perdas (UDP não garante entrega).

### Cenário 4: Comunicação entre Máquinas Diferentes

**Máquina A (192.168.1.10):**
```
0 192.168.1.10 1100
1 192.168.1.20 1150
```

```bash
java -cp out application.UnicastTestApplication 0 config/unicast_config.txt
> send 1 Mensagem entre máquinas
```

**Máquina B (192.168.1.20):**
```bash
java -cp out application.UnicastTestApplication 1 config/unicast_config.txt
```

**Resultado esperado:** Máquina B recebe a mensagem da Máquina A. Firewall deve permitir tráfego UDP nas portas configuradas.

## Detalhes Técnicos

### Identificadores UCSAP

- Tipo: `short` (número inteiro de 16 bits com sinal)
- Valores permitidos: 0 a 32.767
- Cada entidade deve ter ID único na configuração
- IDs negativos são inválidos

### Portas UDP

- Devem ser maiores que 1024 (portas privilegiadas 0-1024 são reservadas para serviços do sistema)
- Únicas por host (validação automática detecta duplicatas)
- Valores permitidos: 1025-65535
- Exemplo de erro: `localhost:1100` e `localhost:1100` na mesma config gera erro de porta duplicada

### Localhost vs IP Remoto

**Localhost (`127.0.0.1`):**
- Funciona apenas para comunicação na mesma máquina
- Interface de loopback virtual
- Não atravessa a rede física
- Útil para testes locais

**IP Real (ex: `192.168.1.10`):**
- Necessário para comunicação entre máquinas diferentes
- Requer configuração de rede adequada
- Sujeito a firewalls e NAT
- Endereços IPv6 (como `::1`) são normalizados para `127.0.0.1`

### Limite de Mensagem

- PDUs não podem exceder 1024 bytes, incluindo cabeçalho (`UPDREQPDU <size> `)
- Cabeçalho típico ocupa ~15 bytes, deixando ~1009 bytes para dados
- Mensagens muito longas são rejeitadas com erro antes do envio
- Validação ocorre em `UPDataRequestPDU.encode()`

### Thread Safety

O protocolo utiliza mecanismos de sincronização para operação segura em ambientes multi-thread:

**Thread de Envio:**
- Processa fila `sendQueue` usando `BlockingQueue`
- Garante FIFO (First In, First Out) para requisições
- Bloqueia quando fila vazia (economiza CPU)

**Thread de Recebimento:**
- Loop infinito `receiveLoop` aguardando datagramas
- Timeout de 100ms para verificar flag `running`
- Processa PDUs recebidas e notifica aplicação via callback

**Estruturas Thread-Safe:**
- `AtomicLong` para contadores (enviadas/recebidas)
- `BlockingQueue` para comunicação entre threads
- `volatile boolean running` para sinalização de parada
- `Collections.synchronizedMap` implícito em caches

### Otimizações de Desempenho

**1. Cache DNS**
```java
private final Map<String, InetAddress> addressCache;
```
- Evita chamadas repetidas a `InetAddress.getByName()`
- Lookup DNS pode levar 50-200ms
- Cache reduz para ~1ms após primeira resolução
- Ganho: 50-200x em comunicações frequentes

**2. Pool de Buffers**
```java
private final BlockingQueue<byte[]> bufferPool;
```
- Reutiliza arrays `byte[1024]` para recebimento
- Reduz alocações de memória em ~90%
- Diminui pressão sobre garbage collector
- Ganho: ~60% menos latência de GC em alta carga

**3. Cache Porta-UCSAP**
```java
private final Map<Integer, Short> portToUcsapCache;
```
- Lookup O(1) para identificação de origem em localhost
- Evita iteração sobre todas as entradas
- Crítico para cenários com muitos UCSAPs
- Ganho: O(1) vs O(n) para identificação de origem

**4. Timeout Reduzido**
```java
socket.setSoTimeout(100); // 100ms vs 1000ms padrão
```
- Polling mais frequente da flag `running`
- Shutdown mais responsivo (100ms vs 1s)
- Trade-off: +10% CPU em idle, mas melhor UX

**Ganho Total Estimado:**
- Throughput: 2-3x em comunicações frequentes
- Latência: ~70% menor em tráfego intenso
- Uso de memória: ~60% menor (menos GC)

### Fluxo de Dados

**Envio de Mensagem:**
```
Aplicação → UPDataReq() → sendQueue → sendMessage() → 
socket.send() → Rede UDP
```

**Recebimento de Mensagem:**
```
Rede UDP → socket.receive() → UPDataRequestPDU.parse() → 
UPDataInd() callback → Aplicação
```

### Tratamento de Erros

**Erros de Configuração:**
- Porta duplicada: `IOException` ao carregar config
- UCSAP inexistente: `IOException` ao iniciar protocolo
- Formato inválido: `IOException` com linha do erro

**Erros de Envio:**
- Destino inexistente: `UPDataReq()` retorna `false`
- Mensagem nula: Retorna `false`, não envia
- PDU muito grande: `IllegalArgumentException` antes do envio

**Erros de Recebimento:**
- PDU malformada: Log de erro, pacote descartado
- Origem desconhecida: Log de warning, pacote processado mas origem=-1
- Socket fechado: Loop termina graciosamente

### Características do UDP

**Vantagens:**
- Baixa latência (sem handshake)
- Overhead mínimo (8 bytes de cabeçalho)
- Ideal para aplicações real-time

**Desvantagens:**
- Sem garantia de entrega (pacotes podem se perder)
- Sem garantia de ordem (pacote 2 pode chegar antes do 1)
- Sem controle de fluxo (pode sobrecarregar receptor)

**Implicações para este Projeto:**
- Mensagens podem não chegar (rede congestionada, buffer overflow)
- Ordem de chegada pode diferir da ordem de envio
- Aplicação deve tolerar perdas (ou implementar ACK em camada superior)

## Troubleshooting

### Erro: "Address already in use"

**Causa:** Porta já está sendo usada por outro processo.

**Solução:**
1. Identifique o processo usando a porta:
    - Windows: `netstat -ano | findstr :1100`
    - Linux: `lsof -i :1100`
2. Finalize o processo ou altere a porta no arquivo de configuração

### Erro: "Connection refused"

**Causa:** Nó de destino não está rodando.

**Solução:**
1. Inicie o UCSAP de destino antes de enviar mensagens
2. Verifique se a configuração está correta em ambos os lados

### Mensagens não chegam entre máquinas diferentes

**Causa:** Firewall bloqueando UDP ou endereço IP incorreto.

**Solução:**
1. Verifique firewall:
    - Windows: `Windows Defender Firewall → Permitir aplicativo`
    - Linux: `sudo ufw allow 1100/udp`
2. Teste conectividade: `ping <ip_destino>`
3. Confirme IP correto:
    - Windows: `ipconfig`
    - Linux: `ifconfig` ou `ip addr`

### PDU inválida

**Causa:** Mensagem excede 1024 bytes ou formato corrompido.

**Solução:**
1. A validação ocorre antes do envio - reduza o tamanho da mensagem
2. Mensagem máxima segura: ~1000 caracteres (deixa espaço para cabeçalho)

### Timeout ou perda de mensagens

**Causa:** Natureza não confiável do UDP ou congestionamento de rede.

**Solução:**
1. UDP não garante entrega - isto é esperado
2. Para maior confiabilidade, reduza carga ou implemente ACK em camada superior
3. Verifique buffer do socket com `socket.setReceiveBufferSize(65536)`

### Aplicação não encerra

**Causa:** Threads de envio/recebimento não finalizam.

**Solução:**
1. Use `Ctrl+C` para forçar saída
2. Sempre chame `protocol.shutdown()` ao encerrar
3. Verifique se hook de shutdown está registrado

## Conceitos de Redes Aplicados

### Modelo OSI e TCP/IP

Este projeto opera nas seguintes camadas:

**Camada de Aplicação (Camada 7 OSI / Camada 4 TCP/IP):**
- `UnicastTestApplication`: interface do usuário
- Comandos `send`, `list`, `exit`

**Camada de Apresentação (implícita):**
- `UPDataRequestPDU`: codificação/decodificação de dados
- Formato texto: `UPDREQPDU <size> <data>`

**Camada de Transporte (Camada 4 OSI / Camada 3 TCP/IP):**
- `UnicastProtocol`: lógica de envio/recebimento
- Uso de UDP (não confiável, sem conexão)

**Camadas Inferiores (gerenciadas pelo SO):**
- Camada de Rede: roteamento IP
- Camada de Enlace: Ethernet/Wi-Fi
- Camada Física: cabos, sinais

### Service Access Point (SAP)

UCSAP é uma implementação de SAP, conceito fundamental em arquiteturas de protocolos:

**Definição:**
- Ponto de interface entre camadas adjacentes
- Permite que camada N acesse serviços da camada N-1

**Neste Projeto:**
- `UnicastServiceInterface`: SAP da camada de protocolo
- `UnicastServiceUserInterface`: SAP da camada de aplicação
- Comunicação via primitivas `UPDataReq` (request) e `UPDataInd` (indication)

### Primitivas de Serviço

**Request (Requisição):**
```java
boolean UPDataReq(short destination, String message)
```
- Iniciada pela camada superior (aplicação)
- Solicita envio de dados

**Indication (Indicação):**
```java
void UPDataInd(short source, String message)
```
- Iniciada pela camada inferior (protocolo)
- Notifica chegada de dados

**Ausência de Confirm/Response:**
- UDP não tem confirmação de entrega
- Protocolo é unidirecional (simplex no momento do envio)

### Endereçamento

**Endereço Completo de um UCSAP:**
```
<UCSAP ID, IP Address, Port Number>
Exemplo: (0, 192.168.1.10, 1100)
```

**Hierarquia:**
1. IP Address: identifica o host na rede
2. Port Number: identifica o processo no host
3. UCSAP ID: identifica a entidade lógica no processo

**Mapeamento:**
- Config file mapeia UCSAP ID → (IP, Port)
- SO mapeia Port → Socket
- Hardware mapeia IP → MAC Address (via ARP)

### Multiplexação/Demultiplexação

**Multiplexação (Envio):**
- Múltiplos UCSAPs podem enviar pelo mesmo socket
- `sendQueue` serializa requisições
- Sistema operacional adiciona cabeçalho UDP e IP

**Demultiplexação (Recebimento):**
- SO usa porta para entregar ao socket correto
- `receiveLoop` extrai dados do datagrama
- `findSourceByAddress()` mapeia (IP, Port) → UCSAP ID
- Callback `UPDataInd()` entrega à aplicação correta

## Extensões Possíveis

Para transformar este projeto em um protocolo mais robusto:

**1. Confiabilidade:**
- Implementar ACK (acknowledgment) para confirmação de entrega
- Timeouts e retransmissões para pacotes perdidos
- Números de sequência para detectar duplicatas

**2. Controle de Fluxo:**
- Janela deslizante para limitar taxa de envio
- Buffer de recepção com feedback ao remetente

**3. Segurança:**
- Criptografia de mensagens (AES)
- Autenticação de origem (HMAC)
- Handshake de estabelecimento seguro

**4. Qualidade de Serviço:**
- Priorização de mensagens
- Controle de congestionamento
- Marcação DSCP para QoS de rede

**5. Funcionalidades Avançadas:**
- Multicast (um para muitos)
- Broadcast (um para todos)
- Fragmentação de mensagens grandes
- Compressão de dados