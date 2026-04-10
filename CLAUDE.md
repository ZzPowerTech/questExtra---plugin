# questsExtras

Plugin Bukkit/Paper 1.21.x que estende o BattlePass 5.x com tipos de missão customizados.

## Stack

- Java 21 · Paper API 1.21.6 · BattlePass 5.0.5
- Testes: JUnit 5 + Mockito 5 (strict stubs via `MockitoExtension`)

## Estrutura do Projeto

```
src/main/java/plugin/questsExtras/
├── QuestsExtras.java                    # onEnable → ServerLoadEvent → registerQuests()
└── quests/
    ├── GroundItemCollectQuest.java      # questsextras_collect_ground_item
    ├── DragonBreathCollectQuest.java    # questsextras_collect_dragon_breath
    └── MinecartRideQuest.java           # questsextras_minecart_ride
```

## Adicionando Nova Missão

### 1. Criar a classe em `src/main/java/plugin/questsExtras/quests/`

```java
/**
 * Quest type: questsextras_minha_nova_quest
 * Descrição do comportamento da missão.
 */
public class MinhaNovaQuest extends ExternalActionContainer {

    public MinhaNovaQuest(JavaPlugin plugin) {
        super(plugin, "questsextras");
    }

    @EventHandler(ignoreCancelled = true)
    public void onEvento(SomeBukkitEvent event) {
        // 1. Validar contexto (player, condições específicas)
        // 2. Calcular progresso
        executionBuilder("minha_nova_quest")
                .player(player)
                .root(algumMaterial)   // opcional — filtra pelo `variable` do config
                .progress(quantidade)  // ou .progressSingle()
                .buildAndExecute();
    }
}
```

### 2. Registrar em `QuestsExtras.java`

```java
actionRegistry.quest(
    MinecartRideQuest::new,
    DragonBreathCollectQuest::new,
    GroundItemCollectQuest::new,
    MinhaNovaQuest::new   // ← adicionar aqui
);
```

### 3. Configurar no BattlePass

```yaml
type: questsextras_minha_nova_quest   # prefixo SEMPRE minúsculo
variable: DIAMOND                      # ou "none" para qualquer item
required-progress: 10
points: 50
info:
  - "&7Descrição da missão."
```

## Regras de Nomenclatura

| Regra | Correto | Errado |
|---|---|---|
| Prefixo do tipo | `questsextras_` | `questsExtras_` |
| Argumento do executionBuilder | `"collect_ground_item"` | `"collectGroundItem"` |
| Tipo completo no YAML | `questsextras_collect_ground_item` | qualquer outra caixa |

O tipo completo é: `"questsextras_"` + argumento do `executionBuilder(arg)`.  
A comparação do ActionsReader é **case-sensitive** — sempre usar minúsculo.

## Testes

Cada missão deve ter testes em `src/test/java/.../quests/<Nome>QuestTest.java`.

- Usar `@ExtendWith(MockitoExtension.class)` (strict stubs)
- **Nunca criar stubs que o código não vai chamar** — causa `UnnecessaryStubbingException`
- Se o método tem early return (ex: `instanceof Player`), stubs após esse ponto são desnecessários

Rodar: `./gradlew test`

## Versionamento

A versão fica **somente em `build.gradle`** → `version = '3.6'`.  
O `plugin.yml` usa o placeholder `${version}` e é preenchido automaticamente no build.

### Regra: toda alteração de código sobe a versão

| Tipo de mudança | Parte a incrementar | Exemplo |
|---|---|---|
| Nova missão | **minor** (segundo número) | `3.6` → `3.7` |
| Bugfix em missão existente | **patch** (terceiro número) | `3.6` → `3.6.1` |
| Mudança de infra/config/testes | não sobe versão | — |

> Versão atual: verificar `version = '...'` em `build.gradle`

**Passos obrigatórios ao alterar código de missão:**

1. Incrementar `version` em `build.gradle`
2. Fazer as alterações de código
3. Rodar `./gradlew build`
4. Commitar **incluindo** o `build.gradle` com a nova versão

## Padrão de Commits

```
✨ Quest type: <nome da missão>          → nova missão
🐛 Correção <missão>: <descrição>       → bugfix
✅ <descrição>                           → atualização/mudança de infra
```

## Desenvolvimento Paralelo

Use o skill `/nova-missao` para criar implementação + testes com dois subagentes em paralelo.  
Use o skill `/corrigir-missao` para investigar + corrigir bug de uma missão existente.
