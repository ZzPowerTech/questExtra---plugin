package plugin.questsExtras.quests;

import net.advancedplugins.bp.impl.actions.ActionExecution;
import net.advancedplugins.bp.impl.actions.ActionRegistry;
import net.advancedplugins.bp.impl.actions.ActionsReader;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroundItemCollectQuestTest {

    private GroundItemCollectQuest quest;
    private Set<UUID> playerDroppedItems;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        JavaPlugin mockPlugin = mock(JavaPlugin.class);
        quest = new GroundItemCollectQuest(mockPlugin);

        Field field = GroundItemCollectQuest.class.getDeclaredField("playerDroppedItems");
        field.setAccessible(true);
        playerDroppedItems = (Set<UUID>) field.get(quest);
    }

    // ─── onPlayerDrop ──────────────────────────────────────────────────────

    @Test
    @DisplayName("onPlayerDrop deve registrar UUID do item largado pelo player")
    void playerDrop_addsItemUuid() {
        UUID itemId = UUID.randomUUID();
        Item dropItem = mock(Item.class);
        when(dropItem.getUniqueId()).thenReturn(itemId);

        PlayerDropItemEvent event = mock(PlayerDropItemEvent.class);
        when(event.getItemDrop()).thenReturn(dropItem);

        quest.onPlayerDrop(event);

        assertTrue(playerDroppedItems.contains(itemId),
            "UUID do item largado deve estar na lista de rastreamento");
    }

    @Test
    @DisplayName("onPlayerDrop não deve duplicar o mesmo UUID")
    void playerDrop_doesNotDuplicateUuid() {
        UUID itemId = UUID.randomUUID();
        Item dropItem = mock(Item.class);
        when(dropItem.getUniqueId()).thenReturn(itemId);

        PlayerDropItemEvent event = mock(PlayerDropItemEvent.class);
        when(event.getItemDrop()).thenReturn(dropItem);

        quest.onPlayerDrop(event);
        quest.onPlayerDrop(event);

        assertEquals(1, playerDroppedItems.size(),
            "HashSet não deve duplicar entradas");
    }

    // ─── onGroundItemPickup: itens largados por player ─────────────────────

    @Test
    @DisplayName("Pickup de item largado pelo player NÃO deve contar progresso")
    void pickup_playerDroppedItem_doesNotCountProgress() {
        UUID itemId = UUID.randomUUID();
        playerDroppedItems.add(itemId);

        Item groundItem = mock(Item.class);
        when(groundItem.getUniqueId()).thenReturn(itemId);

        Player player = mock(Player.class);
        EntityPickupItemEvent event = mock(EntityPickupItemEvent.class);
        when(event.getEntity()).thenReturn(player);
        when(event.getItem()).thenReturn(groundItem);

        try (MockedStatic<ActionRegistry> mockedRegistry = mockStatic(ActionRegistry.class)) {
            quest.onGroundItemPickup(event);

            mockedRegistry.verify(() -> ActionRegistry.getRegistry(), never());
        }

        assertFalse(playerDroppedItems.contains(itemId),
            "UUID deve ser removido do set após pickup para não vazar memória");
    }

    @Test
    @DisplayName("UUID deve ser removido do set após filtrar item largado por player")
    void pickup_playerDroppedItem_removesUuidFromTracking() {
        UUID itemId = UUID.randomUUID();
        playerDroppedItems.add(itemId);

        Item groundItem = mock(Item.class);
        when(groundItem.getUniqueId()).thenReturn(itemId);

        Player player = mock(Player.class);
        EntityPickupItemEvent event = mock(EntityPickupItemEvent.class);
        when(event.getEntity()).thenReturn(player);
        when(event.getItem()).thenReturn(groundItem);

        try (MockedStatic<ActionRegistry> mockedRegistry = mockStatic(ActionRegistry.class)) {
            quest.onGroundItemPickup(event);
        }

        assertFalse(playerDroppedItems.contains(itemId),
            "UUID deve ser limpo do set depois de filtrado");
    }

    // ─── onGroundItemPickup: itens naturais ────────────────────────────────

    @Test
    @DisplayName("Pickup de item natural DEVE contar progresso usando stack.getAmount()")
    void pickup_naturalItem_countsFullProgress() {
        UUID itemId = UUID.randomUUID();

        ItemStack stack = mock(ItemStack.class);
        when(stack.getAmount()).thenReturn(5);
        when(stack.getType()).thenReturn(Material.GHAST_TEAR);

        Item groundItem = mock(Item.class);
        when(groundItem.getUniqueId()).thenReturn(itemId);
        when(groundItem.getItemStack()).thenReturn(stack);

        Player player = mock(Player.class);
        EntityPickupItemEvent event = mock(EntityPickupItemEvent.class);
        when(event.getEntity()).thenReturn(player);
        when(event.getItem()).thenReturn(groundItem);

        ActionsReader mockReader = mock(ActionsReader.class);
        ActionRegistry mockRegistry = mock(ActionRegistry.class);
        when(mockRegistry.getReader()).thenReturn(mockReader);

        try (MockedStatic<ActionRegistry> mockedRegistry = mockStatic(ActionRegistry.class)) {
            mockedRegistry.when(ActionRegistry::getRegistry).thenReturn(mockRegistry);

            quest.onGroundItemPickup(event);

            verify(mockReader, times(1)).onAction(any(ActionExecution.class));
        }
    }

    @Test
    @DisplayName("collectedAmount usa stack.getAmount() diretamente (sem getRemaining())")
    void pickup_collectedAmountUsesStackAmount() {
        UUID itemId = UUID.randomUUID();

        ItemStack stack = mock(ItemStack.class);
        when(stack.getAmount()).thenReturn(10);
        when(stack.getType()).thenReturn(Material.GUNPOWDER);

        Item groundItem = mock(Item.class);
        when(groundItem.getUniqueId()).thenReturn(itemId);
        when(groundItem.getItemStack()).thenReturn(stack);

        Player player = mock(Player.class);
        EntityPickupItemEvent event = mock(EntityPickupItemEvent.class);
        when(event.getEntity()).thenReturn(player);
        when(event.getItem()).thenReturn(groundItem);

        ActionsReader mockReader = mock(ActionsReader.class);
        ActionRegistry mockRegistry = mock(ActionRegistry.class);
        when(mockRegistry.getReader()).thenReturn(mockReader);

        try (MockedStatic<ActionRegistry> mockedRegistry = mockStatic(ActionRegistry.class)) {
            mockedRegistry.when(ActionRegistry::getRegistry).thenReturn(mockRegistry);

            quest.onGroundItemPickup(event);

            verify(mockReader, times(1)).onAction(any(ActionExecution.class));
        }
    }

    @Test
    @DisplayName("stack.getAmount() = 0 não deve chamar buildAndExecute")
    void pickup_zeroStackAmount_doesNotCountProgress() {
        UUID itemId = UUID.randomUUID();

        ItemStack stack = mock(ItemStack.class);
        when(stack.getAmount()).thenReturn(0);

        Item groundItem = mock(Item.class);
        when(groundItem.getUniqueId()).thenReturn(itemId);
        when(groundItem.getItemStack()).thenReturn(stack);

        Player player = mock(Player.class);
        EntityPickupItemEvent event = mock(EntityPickupItemEvent.class);
        when(event.getEntity()).thenReturn(player);
        when(event.getItem()).thenReturn(groundItem);

        try (MockedStatic<ActionRegistry> mockedRegistry = mockStatic(ActionRegistry.class)) {
            quest.onGroundItemPickup(event);

            mockedRegistry.verify(() -> ActionRegistry.getRegistry(), never());
        }
    }

    @Test
    @DisplayName("Entidade não-player pegando item não deve contar progresso")
    void pickup_nonPlayerEntity_doesNotCountProgress() {
        // Entidade genérica (mob), não um Player
        org.bukkit.entity.LivingEntity mob = mock(org.bukkit.entity.LivingEntity.class);
        EntityPickupItemEvent event = mock(EntityPickupItemEvent.class);
        when(event.getEntity()).thenReturn(mob);

        try (MockedStatic<ActionRegistry> mockedRegistry = mockStatic(ActionRegistry.class)) {
            quest.onGroundItemPickup(event);

            mockedRegistry.verify(() -> ActionRegistry.getRegistry(), never());
        }
    }

    // ─── onItemDespawn ─────────────────────────────────────────────────────

    @Test
    @DisplayName("onItemDespawn deve remover UUID do set para evitar vazamento de memória")
    void itemDespawn_removesUuidFromTracking() {
        UUID itemId = UUID.randomUUID();
        playerDroppedItems.add(itemId);

        Item entity = mock(Item.class);
        when(entity.getUniqueId()).thenReturn(itemId);

        ItemDespawnEvent event = mock(ItemDespawnEvent.class);
        when(event.getEntity()).thenReturn(entity);

        quest.onItemDespawn(event);

        assertFalse(playerDroppedItems.contains(itemId),
            "UUID deve ser removido do set quando o item desaparece do mundo");
    }

    @Test
    @DisplayName("onItemDespawn com UUID não rastreado não deve lançar exceção")
    void itemDespawn_unknownUuid_doesNotThrow() {
        UUID unknownId = UUID.randomUUID();

        Item entity = mock(Item.class);
        when(entity.getUniqueId()).thenReturn(unknownId);

        ItemDespawnEvent event = mock(ItemDespawnEvent.class);
        when(event.getEntity()).thenReturn(entity);

        assertDoesNotThrow(() -> quest.onItemDespawn(event));
    }

    // ─── cenários combinados ───────────────────────────────────────────────

    @Test
    @DisplayName("Segundo player pegando item largado por player1 não deve receber progresso")
    void pickup_itemDroppedByPlayer1_pickedByPlayer2_doesNotCount() {
        // Player1 larga o item
        UUID itemId = UUID.randomUUID();
        Item dropItem = mock(Item.class);
        when(dropItem.getUniqueId()).thenReturn(itemId);

        PlayerDropItemEvent dropEvent = mock(PlayerDropItemEvent.class);
        when(dropEvent.getItemDrop()).thenReturn(dropItem);
        quest.onPlayerDrop(dropEvent);

        // Player2 pega o item
        Item groundItem = mock(Item.class);
        when(groundItem.getUniqueId()).thenReturn(itemId);

        Player player2 = mock(Player.class);
        EntityPickupItemEvent pickupEvent = mock(EntityPickupItemEvent.class);
        when(pickupEvent.getEntity()).thenReturn(player2);
        when(pickupEvent.getItem()).thenReturn(groundItem);

        try (MockedStatic<ActionRegistry> mockedRegistry = mockStatic(ActionRegistry.class)) {
            quest.onGroundItemPickup(pickupEvent);

            mockedRegistry.verify(() -> ActionRegistry.getRegistry(), never());
        }
    }

    @Test
    @DisplayName("Múltiplos items naturais devem acumular no set independentemente")
    void multipleNaturalItems_canBePickedUpIndependently() {
        UUID itemId1 = UUID.randomUUID();
        UUID itemId2 = UUID.randomUUID();

        ActionsReader mockReader = mock(ActionsReader.class);
        ActionRegistry mockRegistry = mock(ActionRegistry.class);
        when(mockRegistry.getReader()).thenReturn(mockReader);

        try (MockedStatic<ActionRegistry> mockedRegistry = mockStatic(ActionRegistry.class)) {
            mockedRegistry.when(ActionRegistry::getRegistry).thenReturn(mockRegistry);

            for (UUID itemId : new UUID[]{itemId1, itemId2}) {
                ItemStack stack = mock(ItemStack.class);
                when(stack.getAmount()).thenReturn(1);
                when(stack.getType()).thenReturn(Material.BONE);

                Item groundItem = mock(Item.class);
                when(groundItem.getUniqueId()).thenReturn(itemId);
                when(groundItem.getItemStack()).thenReturn(stack);

                Player player = mock(Player.class);
                EntityPickupItemEvent event = mock(EntityPickupItemEvent.class);
                when(event.getEntity()).thenReturn(player);
                when(event.getItem()).thenReturn(groundItem);

                quest.onGroundItemPickup(event);
            }

            verify(mockReader, times(2)).onAction(any(ActionExecution.class));
        }
    }
}
