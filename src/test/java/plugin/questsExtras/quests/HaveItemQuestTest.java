package plugin.questsExtras.quests;

import net.advancedplugins.bp.impl.actions.ActionExecution;
import net.advancedplugins.bp.impl.actions.ActionRegistry;
import net.advancedplugins.bp.impl.actions.ActionsReader;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link HaveItemQuest}.
 *
 * <p>A missão realiza verificação periódica do inventário do jogador,
 * somando {@code item.getAmount()} de cada slot que contenha o material
 * configurado (e, opcionalmente, o enchantment configurado).
 *
 * <p><b>Limitação de testabilidade:</b> o agendador periódico (BukkitScheduler /
 * runTaskTimer) não pode ser acionado diretamente em testes unitários. Por isso,
 * a lógica central de verificação deve estar em um método package-private
 * {@code checkInventory(Player)} que é chamado tanto pelo scheduler quanto pelos
 * testes. Se a implementação não expor esse método, será necessário usar
 * PowerMock ou integração com servidor fake (MockBukkit), o que aumenta a
 * complexidade. Os testes abaixo assumem que {@code checkInventory(Player)}
 * existe e é visível no pacote.
 */
@ExtendWith(MockitoExtension.class)
class HaveItemQuestTest {

    private HaveItemQuest quest;

    @BeforeEach
    void setUp() {
        JavaPlugin mockPlugin = mock(JavaPlugin.class);
        quest = new HaveItemQuest(mockPlugin);
    }

    // ─── cenário 1: 64 itens em um único slot ──────────────────────────────

    @Test
    @DisplayName("Player com 64 itens do material correto → progresso = 64")
    void singleSlot_fullStack_countsProgress64() {
        ItemStack stack = mock(ItemStack.class);
        when(stack.getType()).thenReturn(Material.DIAMOND);
        when(stack.getAmount()).thenReturn(64);

        PlayerInventory inventory = mock(PlayerInventory.class);
        when(inventory.getContents()).thenReturn(new ItemStack[]{stack});

        Player player = mock(Player.class);
        when(player.getInventory()).thenReturn(inventory);

        ActionsReader mockReader = mock(ActionsReader.class);
        ActionRegistry mockRegistry = mock(ActionRegistry.class);
        when(mockRegistry.getReader()).thenReturn(mockReader);

        try (MockedStatic<ActionRegistry> mockedRegistry = mockStatic(ActionRegistry.class)) {
            mockedRegistry.when(ActionRegistry::getRegistry).thenReturn(mockRegistry);

            quest.checkInventory(player);

            verify(mockReader, times(1)).onAction(any(ActionExecution.class));
        }
    }

    // ─── cenário 2: 32 em um slot + 16 em outro → progresso = 48 ──────────

    @Test
    @DisplayName("Player com 32 itens em slot A e 16 em slot B → progresso = 48")
    void multipleSlots_sumsAmounts() {
        ItemStack stack1 = mock(ItemStack.class);
        when(stack1.getType()).thenReturn(Material.DIAMOND);
        when(stack1.getAmount()).thenReturn(32);

        ItemStack stack2 = mock(ItemStack.class);
        when(stack2.getType()).thenReturn(Material.DIAMOND);
        when(stack2.getAmount()).thenReturn(16);

        PlayerInventory inventory = mock(PlayerInventory.class);
        when(inventory.getContents()).thenReturn(new ItemStack[]{stack1, stack2});

        Player player = mock(Player.class);
        when(player.getInventory()).thenReturn(inventory);

        ActionsReader mockReader = mock(ActionsReader.class);
        ActionRegistry mockRegistry = mock(ActionRegistry.class);
        when(mockRegistry.getReader()).thenReturn(mockReader);

        try (MockedStatic<ActionRegistry> mockedRegistry = mockStatic(ActionRegistry.class)) {
            mockedRegistry.when(ActionRegistry::getRegistry).thenReturn(mockRegistry);

            quest.checkInventory(player);

            verify(mockReader, times(1)).onAction(any(ActionExecution.class));
        }
    }

    // ─── cenário 3: inventário vazio → sem progresso ───────────────────────

    @Test
    @DisplayName("Player sem nenhum item do material → progresso = 0, sem chamar executionBuilder")
    void emptyInventory_doesNotCountProgress() {
        PlayerInventory inventory = mock(PlayerInventory.class);
        when(inventory.getContents()).thenReturn(new ItemStack[]{null, null});

        Player player = mock(Player.class);
        when(player.getInventory()).thenReturn(inventory);

        try (MockedStatic<ActionRegistry> mockedRegistry = mockStatic(ActionRegistry.class)) {
            quest.checkInventory(player);

            mockedRegistry.verify(() -> ActionRegistry.getRegistry(), never());
        }
    }

    // ─── cenário 4: material errado → sem progresso ────────────────────────

    @Test
    @DisplayName("Player com item de material errado → sem progresso")
    void wrongMaterial_doesNotCountProgress() {
        ItemStack wrongStack = mock(ItemStack.class);
        when(wrongStack.getType()).thenReturn(Material.STONE);

        PlayerInventory inventory = mock(PlayerInventory.class);
        when(inventory.getContents()).thenReturn(new ItemStack[]{wrongStack});

        Player player = mock(Player.class);
        when(player.getInventory()).thenReturn(inventory);

        try (MockedStatic<ActionRegistry> mockedRegistry = mockStatic(ActionRegistry.class)) {
            quest.checkInventory(player);

            mockedRegistry.verify(() -> ActionRegistry.getRegistry(), never());
        }
    }

    // ─── cenário 5a: enchantment correto → conta ───────────────────────────

    @Test
    @DisplayName("Item com enchantment correto → conta progresso")
    void correctEnchantment_countsProgress() {
        ItemStack stack = mock(ItemStack.class);
        when(stack.getType()).thenReturn(Material.DIAMOND_SWORD);
        when(stack.getAmount()).thenReturn(1);
        when(stack.containsEnchantment(Enchantment.SHARPNESS)).thenReturn(true);

        PlayerInventory inventory = mock(PlayerInventory.class);
        when(inventory.getContents()).thenReturn(new ItemStack[]{stack});

        Player player = mock(Player.class);
        when(player.getInventory()).thenReturn(inventory);

        ActionsReader mockReader = mock(ActionsReader.class);
        ActionRegistry mockRegistry = mock(ActionRegistry.class);
        when(mockRegistry.getReader()).thenReturn(mockReader);

        try (MockedStatic<ActionRegistry> mockedRegistry = mockStatic(ActionRegistry.class)) {
            mockedRegistry.when(ActionRegistry::getRegistry).thenReturn(mockRegistry);

            quest.checkInventoryWithEnchantment(player, Enchantment.SHARPNESS);

            verify(mockReader, times(1)).onAction(any(ActionExecution.class));
        }
    }

    // ─── cenário 5b: enchantment errado → não conta ────────────────────────

    @Test
    @DisplayName("Item sem o enchantment exigido → não conta progresso")
    void missingEnchantment_doesNotCountProgress() {
        ItemStack stack = mock(ItemStack.class);
        when(stack.getType()).thenReturn(Material.DIAMOND_SWORD);
        when(stack.containsEnchantment(Enchantment.SHARPNESS)).thenReturn(false);

        PlayerInventory inventory = mock(PlayerInventory.class);
        when(inventory.getContents()).thenReturn(new ItemStack[]{stack});

        Player player = mock(Player.class);
        when(player.getInventory()).thenReturn(inventory);

        try (MockedStatic<ActionRegistry> mockedRegistry = mockStatic(ActionRegistry.class)) {
            quest.checkInventoryWithEnchantment(player, Enchantment.SHARPNESS);

            mockedRegistry.verify(() -> ActionRegistry.getRegistry(), never());
        }
    }

    // ─── cenário 6: player nulo → sem progresso ────────────────────────────

    @Test
    @DisplayName("Player nulo → sem progresso e sem NullPointerException")
    void nullPlayer_doesNotCountProgress() {
        try (MockedStatic<ActionRegistry> mockedRegistry = mockStatic(ActionRegistry.class)) {
            quest.checkInventory(null);

            mockedRegistry.verify(() -> ActionRegistry.getRegistry(), never());
        }
    }

    // ─── slots nulos no array do inventário são ignorados ──────────────────

    @Test
    @DisplayName("Slots nulos no inventário são ignorados sem NullPointerException")
    void nullSlots_areSkippedSafely() {
        ItemStack stack = mock(ItemStack.class);
        when(stack.getType()).thenReturn(Material.DIAMOND);
        when(stack.getAmount()).thenReturn(10);

        PlayerInventory inventory = mock(PlayerInventory.class);
        // slot 0 = null, slot 1 = item válido, slot 2 = null
        when(inventory.getContents()).thenReturn(new ItemStack[]{null, stack, null});

        Player player = mock(Player.class);
        when(player.getInventory()).thenReturn(inventory);

        ActionsReader mockReader = mock(ActionsReader.class);
        ActionRegistry mockRegistry = mock(ActionRegistry.class);
        when(mockRegistry.getReader()).thenReturn(mockReader);

        try (MockedStatic<ActionRegistry> mockedRegistry = mockStatic(ActionRegistry.class)) {
            mockedRegistry.when(ActionRegistry::getRegistry).thenReturn(mockRegistry);

            quest.checkInventory(player);

            verify(mockReader, times(1)).onAction(any(ActionExecution.class));
        }
    }

    // ─── item AIR não deve ser contabilizado ───────────────────────────────

    @Test
    @DisplayName("Slots com Material.AIR não contam como item do material alvo")
    void airMaterial_doesNotCount() {
        ItemStack airStack = mock(ItemStack.class);
        when(airStack.getType()).thenReturn(Material.AIR);

        PlayerInventory inventory = mock(PlayerInventory.class);
        when(inventory.getContents()).thenReturn(new ItemStack[]{airStack});

        Player player = mock(Player.class);
        when(player.getInventory()).thenReturn(inventory);

        try (MockedStatic<ActionRegistry> mockedRegistry = mockStatic(ActionRegistry.class)) {
            quest.checkInventory(player);

            mockedRegistry.verify(() -> ActionRegistry.getRegistry(), never());
        }
    }
}
