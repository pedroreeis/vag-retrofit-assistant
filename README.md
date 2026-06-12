# VAG Retrofit Assistant V2

Uma ferramenta especializada para engenharia reversa, análise e aplicação segura de patches em memórias EEPROM de painéis do grupo VAG (Volkswagen, Audi, Seat, Skoda) — projetada com uma interface moderna inspirada no **VCDS** e foco extremo em **segurança e validação**.

![Screenshot do VAG Retrofit Assistant](https://raw.githubusercontent.com/pedroreeis/vag-retrofit-assistant/refs/heads/main/screenshots/1.png)

## ⚠️ Aviso de Responsabilidade
> [!CAUTION]
> Este software modifica dados de memória não volátil (EEPROM) que contêm informações críticas de imobilizador (IMMO) e chaves do veículo. **Sempre tenha um backup físico e evite escrever em módulos cujos dados não foram previamente lidos e salvos.**

---

## 🛠 Funcionalidades Principais

* **Modificação Guiada de EEPROM**: Aplica patches conhecidos (como *Needle Sweep / Staging*, *Welcome Message*, *Luzes de Painel*, etc.) em dumps de painéis (ex: Golf Mk4, Passat B5, Bora). ![Screenshot do VAG Retrofit Assistant](https://raw.githubusercontent.com/pedroreeis/vag-retrofit-assistant/refs/heads/main/screenshots/2.png)
* **Gerenciador de Base de Conhecimento (KB)**: Uma biblioteca interativa de padrões hexadecimais, separada por endereço de módulo VAG (ex: `17 - Instruments`, `46 - Comfort System`).
* **Hex Viewer**: Um visualizador de hexadecimais para visualização rápida do dump. ![Screenshot do VAG Retrofit Assistant](https://raw.githubusercontent.com/pedroreeis/vag-retrofit-assistant/refs/heads/main/screenshots/3.png)
* **Custom Patches do Operador**: Permite aos usuários cadastrarem seus próprios patches testados e salvá-los localmente em banco de dados SQLite embutido.
* **Comunicação Direta via K-Line (KW1281)**: Não é necessário remover o painel ou dessoldar a EEPROM! Usando um simples cabo **KKL USB (FTDI/CH340)**, você pode ler e escrever no módulo através da porta OBD2.
* **Sistema de Auditoria em Tempo Real**: Todo patch aplicado e toda sessão OBD tem os logs e hashes gravados localmente em `~/.vagretrofit/audit` para histórico de segurança.

---

## 🛡 Sistema de Proteções (Fail-Safe)

O VAG Retrofit Assistant foi projetado com a filosofia **Zero-Tolerance & Fail-Safe**, prevenindo ativamente operações que possam causar corrupção de memória ou erros tipo `dEF`.

### 1. OperationGuard (Proteção de Áreas Críticas)
A classe `OperationGuard` atua como um gatekeeper que intercepta toda operação:
- Validação cruzada (cross-check) de compatibilidade entre o dump lido e o patch desejado (bloqueando patches de IMMO2 em IMMO3, por exemplo).
- Bloqueia gravação nas áreas protegidas, hardcoded e em breve dinâmicas pela tabela `EEPROM_MAP`.
- Bloqueia acesso a part-numbers ou IDs de ROM desconhecidos não homologados.

### 2. Motor de Checksum Avançado
Painéis VDO e Bosch possuem assinaturas internas que devem ser atualizadas ao modificar os dados da EEPROM.
- **VDO PQ34 (IMMO 3):** Valida a integridade geral, com engine tolerante a offsets dinâmicos baseados no mapeamento de patches catalogados.
- **VDO PQ34 (IMMO 2):** Bloqueio Automático (*REQUIRES_FLASH*). Em painéis IMMO 2, o checksum não fica na EEPROM, mas sim na memória FLASH do microcontrolador (MCU Motorola HC08). O VRA bloqueia o patch preventivamente pois regravar a EEPROM sem atualizar a flash do MCU levaria a erro `dEF`.

### 3. Backup Automático
**Regra 12**: Antes de qualquer byte modificado ir para o disco ou para a K-Line, um backup `SHA-256` inalterável do estado original é gravado no diretório seguro de backups (`~/.vagretrofit/backups`).

---

## 🔌 K-Line e Protocolo KW1281

A **Versão 2** traz integração nativa de hardware, comunicando em nível de bloco via barramento K. Todo o esforço para adaptar as funções clássicas de leitura/escrita de DOS e C# foi portado para **Java** nativo via `jSerialComm`.

* **5-Baud Init**: Implementação manual (bit-banging) de *Wakeup/Handshake* K-Line a 5 bauds seguido da sincronização em 10400 bps via o byte mágico `0x55`.
* **Motor Block-Level**: Transação limpa do stack KW1281 gerenciando *Block Title*, *Counter*, e complemento XOR bit a bit para envio de acks seguros.
* **Comandos Suportados**: `ReadEeprom`, `WriteEeprom` (com verificação), `DumpEeprom` (download contínuo) e extração de código PIN (`GetSKC`).

---

## 📚 Base de Conhecimento e Fontes

A base de dados embarcada em SQLite foi alimentada mediante estudo minucioso de engenharia reversa por décadas no ecossistema Volkswagen, compilando conhecimento de fóruns e sites legados que documentaram offsets e tabelas Hex de painéis VDO:

- **Graeme's Webspace**: Referência lendária para clusters Mk4 VDO, offsets de Immo 3, patches de iluminção de agulha independente e welcome messages.
- **hayperek.pl**: Ferramenta histórica que nos forneceu bases e referências estáticas para validação de comportamentos (VDO, Bosch, Motometer).
- A comunidade de tuning e retrofit do VAG-COM / VCDS.

---

## 🚀 Como Executar

### Pré-requisitos
- **Java JDK 17** ou superior instalado no Windows.
- Cabo **KKL VAG-COM 409.1** (opcional, para funções K-Line).

### Compilar e Rodar

O projeto usa o `Maven Wrapper` incluído, você não precisa instalar o Maven manualmente.

```powershell
# Compilar e empacotar
.\mvnw.cmd clean package

# Rodar a aplicação
.\mvnw.cmd javafx:run
```

Se desejar, na pasta `target/` um `.exe` e `.jar` estarão disponíveis para uso independente após o build.

### Notas de Ambiente
A primeira execução irá gerar a pasta local `%USERPROFILE%\.vagretrofit` onde o DB SQLite de conhecimento (`knowledge-base-v2.0.0.db`), as bases do usuário, backups e logs de auditoria serão persistidos e armazenados com segurança.

---

> Desenvolvido para a comunidade VAG. Dirija com cuidado.
