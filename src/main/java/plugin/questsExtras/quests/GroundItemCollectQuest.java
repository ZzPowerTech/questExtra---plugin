package plugin.questsExtras.quests;

import net.advancedplugins.bp.impl.actions.containers.ExternalActionContainer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Quest type: questsextras_collect_ground_item
 *
 * Counts progress when a player picks up items dropped naturally
 * (block break, mob drop, farm output). Items dropped manually by
 * any player are ignored.
 */
public class GroundItemCollectQuest extends ExternalActionContainer {

    private final Set<UUID> playerDroppedItems = new HashSet<>();

    public GroundItemCollectQuest(JavaPlugin plugin) {
        super(plugin, "questsextras");
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDrop(PlayerDropItemEvent event) {
        playerDroppedItems.add(event.getItemDrop().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onGroundItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        UUID itemId = event.getItem().getUniqueId();
        if (playerDroppedItems.remove(itemId)) return;

        ItemStack stack = event.getItem().getItemStack();
        int collectedAmount = stack.getAmount() - event.getRemaining();
        if (collectedAmount <= 0) return;

        executionBuilder("collect_ground_item")
                .player(player)
                .root(stack.getType())
                .progress(collectedAmount)
                .buildAndExecute();
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        playerDroppedItems.remove(event.getEntity().getUniqueId());
    }
}
