package plugin.questsExtras.quests;

import net.advancedplugins.bp.impl.actions.containers.ExternalActionContainer;
import org.bukkit.Location;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Quest type: questsextras_minecart_ride
 * Tracks the distance (in blocks) a player travels inside a minecart.
 * <p>
 * Uses VehicleMoveEvent to accumulate traveled distance and sends progress
 * every time a full block is reached.
 * <p>
 * Example quest config in BattlePass:
 * <pre>
 *   type: questsextras_minecart_ride
 *   variable: none
 *   required-progress: 500
 *   points: 43
 *   info:
 *     - "&7Viaje &e500 blocos em um minecart."
 * </pre>
 * This would require the player to ride 500 blocks in a minecart to earn 43 points.
 */
public class MinecartRideQuest extends ExternalActionContainer {

    private final Map<UUID, Location> ridingPlayers = new HashMap<>();
    private final Map<UUID, Double> pendingDistance = new HashMap<>();

    public MinecartRideQuest(JavaPlugin plugin) {
        super(plugin, "questsextras");
    }

    /**
     * When a player enters a minecart, start tracking their position.
     */
    @EventHandler(ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof Player player)) return;
        if (!(event.getVehicle() instanceof Minecart)) return;

        ridingPlayers.put(player.getUniqueId(), player.getLocation().clone());
        pendingDistance.putIfAbsent(player.getUniqueId(), 0.0D);
    }

    /**
     * Track minecart movement in real-time and award progress when whole blocks are reached.
     */
    @EventHandler(ignoreCancelled = true)
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!(event.getVehicle() instanceof Minecart minecart)) return;
        if (!(minecart.getPassenger() instanceof Player player)) return;

        UUID uuid = player.getUniqueId();
        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getWorld() == null || to.getWorld() == null || !from.getWorld().equals(to.getWorld())) {
            ridingPlayers.put(uuid, to.clone());
            pendingDistance.put(uuid, 0.0D);
            return;
        }

        double moved = from.distance(to);
        if (moved <= 0.0D) return;

        double total = pendingDistance.getOrDefault(uuid, 0.0D) + moved;
        int wholeBlocks = (int) Math.floor(total);

        if (wholeBlocks > 0) {
            // Use progressSingle in a loop to mirror BattlePass internal quests behavior.
            for (int i = 0; i < wholeBlocks; i++) {
                executionBuilder("minecart_ride")
                        .player(player)
                        .progressSingle()
                        .canBeAsync()
                        .buildAndExecute();
            }
        }

        pendingDistance.put(uuid, total - wholeBlocks);
        ridingPlayers.put(uuid, to.clone());
    }

    /**
     * When a player exits a minecart, stop tracking.
     */
    @EventHandler(ignoreCancelled = true)
    public void onVehicleExit(VehicleExitEvent event) {
        if (!(event.getExited() instanceof Player player)) return;
        if (!(event.getVehicle() instanceof Minecart)) return;

        ridingPlayers.remove(player.getUniqueId());
        pendingDistance.remove(player.getUniqueId());
    }

    @EventHandler
    public void onQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        ridingPlayers.remove(uuid);
        pendingDistance.remove(uuid);
    }
}

