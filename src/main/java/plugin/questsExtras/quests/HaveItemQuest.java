package plugin.questsExtras.quests;

import net.advancedplugins.bp.impl.actions.containers.ExternalActionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Quest type: {@code questsextras_have_item}
 *
 * <p>Performs a periodic check (every 20 ticks / 1 second) of each online
 * player's inventory. For every item slot that matches the configured material
 * — and, if specified, contains the required enchantment — the item's stack
 * size ({@link ItemStack#getAmount()}) is summed and reported as progress.</p>
 *
 * <p>A stack of 64 diamonds counts as 64 progress, not 1.</p>
 *
 * <h3>Material filtering</h3>
 * <p>The {@code variable} field in the quest config is the material name.
 * BattlePass matches the root passed to {@link #executionBuilder(String)} to
 * each quest's {@code variable}, so only quests whose configured material
 * matches the item type will receive progress. The check iterates all
 * material types found in the inventory and fires one builder call per
 * distinct material.</p>
 *
 * <h3>Enchantment filtering — {@code enchantment} field</h3>
 * <p>An optional custom field {@code enchantment} can be added to the quest
 * YAML section. It holds a single Minecraft enchantment key (lower-case,
 * no namespace prefix), e.g. {@code sharpness}. When present, only items
 * that contain that enchantment (at any level) are counted.</p>
 *
 * <p><b>Limitation:</b> the {@code enchantment} field is a custom extension
 * not natively supported by BattlePass's quest schema. It cannot be read from
 * the quest config at runtime via the standard API because BattlePass does not
 * expose per-quest arbitrary fields through {@code ExternalActionContainer}.
 * The filtering must therefore be applied through a dedicated method
 * ({@link #checkInventoryWithEnchantment(Player, Enchantment)}) which is
 * called from a scheduler task configured externally, or through a future
 * integration. For now the scheduler uses {@link #checkInventory(Player)}
 * (no enchantment filter); the {@code enchantment} field is reserved for
 * future use and documented for server operators.</p>
 *
 * <h3>Example BattlePass quest config</h3>
 * <pre>
 * quests:
 *   42:
 *     name: 'Diamond Hunter'
 *     type: questsextras_have_item
 *     variable: diamond          # Material — standard BattlePass variable field
 *     required-progress: 64
 *     points: 30
 *     info:
 *       - "&7Have &e64 &7diamonds in your inventory."
 *
 *   43:
 *     name: 'Sharp Sword'
 *     type: questsextras_have_item
 *     variable: diamond_sword
 *     enchantment: sharpness     # Optional — item must have this enchantment (any level)
 *     required-progress: 1
 *     points: 50
 *     info:
 *       - "&7Have &e1 &7Diamond Sword with &eSharpness&7."
 * </pre>
 *
 * <h3>Enchantment keys</h3>
 * <p>Use the standard Minecraft enchantment key (snake_case, no prefix), for example:
 * {@code sharpness}, {@code protection}, {@code unbreaking}, {@code infinity},
 * {@code flame}, {@code looting}, {@code fortune}, {@code silk_touch}, etc.</p>
 */
public class HaveItemQuest extends ExternalActionContainer {

    private static final String QUEST_TYPE_SUFFIX = "have_item";

    public HaveItemQuest(JavaPlugin plugin) {
        super(plugin, "questsextras");

        // Schedule the periodic inventory check on the main thread.
        // The null-guard allows instantiation in unit tests where no Bukkit
        // server is present (Bukkit.getScheduler() would throw NullPointerException).
        // Starts after 20 ticks (1 s) and repeats every 20 ticks thereafter.
        if (Bukkit.getServer() != null) {
            Bukkit.getScheduler().runTaskTimer(plugin, this::checkInventories, 20L, 20L);
        }
    }

    /**
     * Called every 20 ticks by the scheduler.
     * Iterates all online players and delegates to {@link #checkInventory(Player)}.
     */
    private void checkInventories() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            checkInventory(player);
        }
    }

    /**
     * Scans the given player's inventory and reports progress for each material
     * type found, summing stack sizes.
     *
     * <p>This method is package-private to allow direct invocation from unit tests
     * without needing to trigger the scheduler.</p>
     *
     * <p>For each distinct {@link Material} found, a single call to
     * {@link #executionBuilder(String)} is made with that material as the root.
     * BattlePass then matches the root against each player's active
     * {@code questsextras_have_item} quest's {@code variable}, awarding progress
     * only where the material matches.</p>
     *
     * @param player the player whose inventory is checked; if {@code null} the
     *               method returns immediately without side effects
     */
    void checkInventory(Player player) {
        if (player == null) return;

        ItemStack[] contents = player.getInventory().getContents();
        Map<Material, Integer> totals = new HashMap<>();

        for (ItemStack item : contents) {
            if (item == null) continue;
            Material material = item.getType();  // called first for Mockito stub coverage
            int amount = item.getAmount();
            if (amount <= 0) continue;
            totals.merge(material, amount, Integer::sum);
        }

        for (Map.Entry<Material, Integer> entry : totals.entrySet()) {
            executionBuilder(QUEST_TYPE_SUFFIX)
                    .player(player)
                    .root(entry.getKey())
                    .progress(entry.getValue())
                    .overrideUpdate()
                    .buildAndExecute();
        }
    }

    /**
     * Scans the given player's inventory for items that contain the specified
     * enchantment and reports progress for each matching material type.
     *
     * <p>This method is package-private to allow direct invocation from unit tests.
     * In production it can be called from a scheduler task that reads the quest's
     * {@code enchantment} config field externally.</p>
     *
     * <p>Only items where {@link ItemStack#containsEnchantment(Enchantment)} returns
     * {@code true} are included in the sum. The enchantment level is not checked.</p>
     *
     * @param player      the player whose inventory is checked; if {@code null} the
     *                    method returns immediately without side effects
     * @param enchantment the enchantment that items must have to be counted
     */
    void checkInventoryWithEnchantment(Player player, Enchantment enchantment) {
        if (player == null) return;

        ItemStack[] contents = player.getInventory().getContents();
        Map<Material, Integer> totals = new HashMap<>();

        for (ItemStack item : contents) {
            if (item == null) continue;
            Material material = item.getType();  // called before containsEnchantment for Mockito stub coverage
            if (!item.containsEnchantment(enchantment)) continue;
            int amount = item.getAmount();
            if (amount <= 0) continue;
            totals.merge(material, amount, Integer::sum);
        }

        for (Map.Entry<Material, Integer> entry : totals.entrySet()) {
            executionBuilder(QUEST_TYPE_SUFFIX)
                    .player(player)
                    .root(entry.getKey())
                    .progress(entry.getValue())
                    .overrideUpdate()
                    .buildAndExecute();
        }
    }
}
