package plugin.questsExtras;

import io.github.battlepass.BattlePlugin;
import net.advancedplugins.bp.impl.actions.ActionRegistry;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.questsExtras.quests.DragonBreathCollectQuest;
import plugin.questsExtras.quests.GroundItemCollectQuest;
import plugin.questsExtras.quests.HaveItemQuest;
import plugin.questsExtras.quests.MinecartRideQuest;

public final class QuestsExtras extends JavaPlugin {

    @Override
    public void onEnable() {
        if (!getServer().getPluginManager().isPluginEnabled("BattlePass")) {
            getLogger().severe("BattlePass not found! Disabling questsExtras...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("BattlePass found! Aguardando inicialização completa para registrar quest types...");

        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onServerLoad(ServerLoadEvent event) {
                registerQuests();
            }
        }, this);
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
