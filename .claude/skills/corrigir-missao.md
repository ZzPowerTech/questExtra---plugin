# Corrigir Bug em Missão Existente

Você foi chamado para corrigir um bug em um tipo de missão do plugin questsExtras.

**Solicite ao usuário (se ainda não informado):**
1. Nome da missão com problema
2. Comportamento esperado vs comportamento atual
3. Se há erros no console (compartilhe o stacktrace)

---

**Dispare os dois agentes abaixo EM PARALELO (uma única mensagem com dois Agent tool calls):**

---

## Agente 1 — Diagnóstico

**Instrução para o agente:**

Você vai diagnosticar um bug em uma missão do plugin questsExtras (Bukkit/Paper 1.21.x + BattlePass 5.x).

1. Leia a implementação da missão em `src/main/java/plugin/questsExtras/quests/<Nome>Quest.java`
2. Leia os testes em `src/test/java/plugin/questsExtras/quests/<Nome>QuestTest.java`
3. Leia `src/main/java/plugin/questsExtras/QuestsExtras.java` para contexto de registro
4. Analise os outros arquivos de quest como referência de padrão correto

Trace o fluxo completo:
- Qual evento Bukkit dispara a missão?
- Quais condições fazem retornar cedo (early return)?
- Como o progresso é calculado?
- O `executionBuilder()` está sendo chamado com os parâmetros corretos?

Reporte sua análise com as hipóteses mais prováveis do bug.

---

## Agente 2 — Correção

**Instrução para o agente:**

Você vai corrigir um bug em uma missão do plugin questsExtras com base no diagnóstico.

1. Leia a implementação da missão problemática
2. Leia todos os outros arquivos de quest como referência de padrão correto
3. Identifique a causa raiz
4. Aplique a correção mínima necessária — não refatore código não relacionado ao bug
5. Atualize ou adicione testes cobrindo o cenário que falhava

**Obrigatório:** incremente o **patch** da versão em `build.gradle` (ex: `3.6` → `3.6.1`) como parte do fix.

Após corrigir, execute `./gradlew test` para confirmar que todos os testes passam.

---

**Após ambos concluírem:**

Combine o diagnóstico com a correção aplicada e confirme que `./gradlew test` passou.
