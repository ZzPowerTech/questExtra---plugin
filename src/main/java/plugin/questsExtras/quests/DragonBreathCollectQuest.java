package plugin.questsExtras.quests;

import net.advancedplugins.bp.impl.actions.containers.ExternalActionContainer;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;

import java.util.UUID;

/**
 * Quest type: questsextras_collect_dragon_breath
 * Counts progress when a player right-clicks with a glass bottle near
 * a dragon breath cloud.
 */
public class DragonBreathCollectQuest extends ExternalActionContainer {

    public DragonBreathCollectQuest(JavaPlugin plugin) {
        super(plugin, "questsextras");
    }

    @EventHandler(ignoreCancelled = true)
    public void onCollectDragonBreath(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() != Material.GLASS_BOTTLE) return;

        boolean foundDragonBreathCloud = false;
        for (Entity entity : player.getNearbyEntities(2.0D, 2.0D, 2.0D)) {
            if (!(entity instanceof AreaEffectCloud cloud)) continue;
            if (!isDragonBreathCloud(cloud)) continue;

            foundDragonBreathCloud = true;
            break;
        }

        if (!foundDragonBreathCloud) return;

        executionBuilder("collect_dragon_breath")
                .player(player)
                .progressSingle()
                .buildAndExecute();
    }

    private boolean isDragonBreathCloud(AreaEffectCloud cloud) {
        ProjectileSource source = cloud.getSource();
        if (source instanceof EnderDragon) {
            return true;
        }

        UUID owner = cloud.getOwnerUniqueId();
        if (owner != null) {
            Entity ownerEntity = cloud.getWorld().getEntity(owner);
            if (ownerEntity instanceof EnderDragon) {
                return true;
            }
        }

        return cloud.getParticle() == Particle.DRAGON_BREATH;
    }
}

