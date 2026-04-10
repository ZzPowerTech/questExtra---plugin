# Criar Nova Missão em Paralelo

Você foi chamado para criar um novo tipo de missão customizada para o plugin questsExtras.

**Solicite ao usuário (se ainda não informado):**
1. Nome da missão em snake_case (ex: `block_break`, `fish_catch`)
2. Comportamento: qual evento Bukkit dispara e o que conta como progresso
3. Se usa `variable` (material específico) ou `none` (qualquer item)

---

**Dispare os dois agentes abaixo EM PARALELO (uma única mensagem com dois Agent tool calls):**

---

## Agente 1 — Implementação + Registro

**Instrução para o agente:**

Você vai implementar um novo tipo de missão no plugin questsExtras (Bukkit/Paper 1.21.x + BattlePass 5.x).

Antes de escrever qualquer código:
1. Leia todos os arquivos de quest existentes em `src/main/java/plugin/questsExtras/quests/` para entender o padrão
2. Leia `src/main/java/plugin/questsExtras/QuestsExtras.java`

Depois crie `src/main/java/plugin/questsExtras/quests/<NomeCamelCase>Quest.java`:
- Estender `ExternalActionContainer`
- Construtor: `super(plugin, "questsextras")`
- Nome no executionBuilder: o snake_case da missão (ex: `"block_break"`)
- `@EventHandler(ignoreCancelled = true)` em todos os event handlers
- Javadoc com `Quest type: questsextras_<nome>` e exemplo de config YAML
- Seguir exatamente a estrutura dos arquivos existentes

Adicione a nova classe ao `actionRegistry.quest(...)` em `QuestsExtras.java`.

**Obrigatório:** incremente o **minor** da versão em `build.gradle` (ex: `3.6` → `3.7`) antes de finalizar.

Ao finalizar, reporte:
- Tipo completo para o BattlePass: `questsextras_<nome>`
- Exemplo de config YAML para o servidor

---

## Agente 2 — Testes Unitários

**Instrução para o agente:**

Você vai escrever os testes unitários para um novo tipo de missão no plugin questsExtras.

Antes de escrever qualquer código:
1. Leia `src/test/java/plugin/questsExtras/quests/GroundItemCollectQuestTest.java` como referência principal
2. Leia a implementação da missão (será fornecida pelo Agente 1, ou leia o arquivo se já existir)

Crie `src/test/java/plugin/questsExtras/quests/<NomeCamelCase>QuestTest.java`:

Cenários obrigatórios:
- Evento disparado pelo contexto correto → progresso é contado
- Entidade não-player (mob, hopper) → sem progresso
- Evento cancelado → sem progresso (quando aplicável)
- Condições inválidas específicas da missão → sem progresso
- Edge cases (valores limítrofes, estado intermediário)

Regras críticas para os testes:
- Usar `@ExtendWith(MockitoExtension.class)` (strict stubs — vai falhar se criar stub não usado)
- Se o método tem `instanceof Player` ou outro early return, NÃO criar stubs de objetos que só seriam usados após esse ponto
- Verificar que `ActionRegistry.getRegistry()` nunca é chamado nos cenários "sem progresso" via `mockedRegistry.verify(() -> ActionRegistry.getRegistry(), never())`

---

**Após ambos concluírem:**

Execute `./gradlew test` para confirmar que todos os testes passam.  
Reporte o tipo completo da missão para configurar no BattlePass.
