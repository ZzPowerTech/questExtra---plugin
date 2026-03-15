package plugin.questsExtras.quests;

import net.advancedplugins.bp.impl.actions.containers.ExternalActionContainer;
import org.bukkit.Material;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Quest type: questsextras_collect_dragon_breath
 *
 * Counts 1 progress whenever a player successfully uses a glass bottle
 * on a dragon breath cloud.
 */
public class DragonBreathCollectQuest extends ExternalActionContainer {

    public DragonBreathCollectQuest(JavaPlugin plugin) {
        super(plugin, "questsextras");
    }

    @EventHandler(ignoreCancelled = true)
    public void onCollectDragonBreath(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof AreaEffectCloud)) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() != Material.GLASS_BOTTLE) return;

        executionBuilder("collect_dragon_breath")
                .player(player)
                .progressSingle()
                .canBeAsync()
                .buildAndExecute();
    }
}

