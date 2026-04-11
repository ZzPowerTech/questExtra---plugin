package plugin.questsExtras;

import io.github.battlepass.BattlePlugin;
import io.github.battlepass.api.events.server.PluginReloadEvent;
import net.advancedplugins.bp.impl.actions.ActionRegistry;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.questsExtras.quests.DragonBreathCollectQuest;
import plugin.questsExtras.quests.GroundItemCollectQuest;
import plugin.questsExtras.quests.HaveItemQuest;
import plugin.questsExtras.quests.MinecartRideQuest;

public final class QuestsExtras extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        if (!getServer().getPluginManager().isPluginEnabled("BattlePass")) {
            getLogger().severe("BattlePass not found! Disabling questsExtras...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(this, this);

        // runTaskLater com delay 1 garante que o BattlePass já terminou de inicializar,
        // e funciona tanto na inicialização do servidor quanto em reloads do plugin.
        getServer().getScheduler().runTaskLater(this, this::registerQuests, 1L);
    }

    @EventHandler
    public void onBattlePassReload(PluginReloadEvent event) {
        // O BattlePass recria o ActionRegistry no reload — re-registramos nossas quests.
        // Delay de 1 tick garante que o novo ActionRegistry já está pronto.
        getServer().getScheduler().runTaskLater(this, this::registerQuests, 1L);
    }

    private void registerQuests() {
        ActionRegistry actionRegistry = BattlePlugin.getPlugin().getActionRegistry();

        actionRegistry.quest(
                MinecartRideQuest::new,
                DragonBreathCollectQuest::new,
                GroundItemCollectQuest::new,
                HaveItemQuest::new
        );

        getLogger().info("questsExtras enabled! Custom quest types registered.");
    }

    @Override
    public void onDisable() {
        getLogger().info("questsExtras disabled.");
    }
}
