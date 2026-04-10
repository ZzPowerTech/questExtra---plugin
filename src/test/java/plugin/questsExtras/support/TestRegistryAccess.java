package plugin.questsExtras.support;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Minimal {@link RegistryAccess} implementation that satisfies the Paper API's
 * {@link java.util.ServiceLoader} requirement in unit-test environments.
 *
 * <p>Paper 1.21.x loads the {@code RegistryAccess} service at JVM startup via
 * {@code RegistryAccessHolder}. Without a provider on the test classpath the
 * static initialiser of {@link org.bukkit.enchantments.Enchantment} throws
 * {@link IllegalStateException}, making any test that references
 * {@code Enchantment.SHARPNESS} (or similar constants) fail before even
 * reaching the class under test.</p>
 *
 * <p>This implementation returns a lightweight no-op {@link Registry} for every
 * key. All registry lookups return {@code null}, meaning
 * {@code Enchantment.SHARPNESS} and similar constants will be {@code null} in
 * tests. That is acceptable because the tests stub
 * {@code ItemStack.containsEnchantment(...)} directly; the enchantment reference
 * is only used as an identity key inside Mockito's argument matchers.</p>
 *
 * <p><strong>Important:</strong> no static fields that reference {@link Registry}
 * are declared here. Paper's class-loading order is:
 * {@code Registry.<clinit>} → {@code RegistryAccess} → ServiceLoader →
 * {@code TestRegistryAccess.<clinit>}. If {@code TestRegistryAccess.<clinit>}
 * itself referenced {@link Registry}, it would encounter the partially-initialised
 * class and throw a {@link NullPointerException}. The no-op registry is therefore
 * created inline in {@link #emptyRegistry()} each time it is called.</p>
 *
 * <p>Registered via
 * {@code src/test/resources/META-INF/services/io.papermc.paper.registry.RegistryAccess}.</p>
 */
public class TestRegistryAccess implements RegistryAccess {

    // No static fields that reference Registry — see Javadoc.

    @SuppressWarnings("deprecation")
    @Override
    public @Nullable <T extends Keyed> Registry<T> getRegistry(@NotNull Class<T> type) {
        return emptyRegistry();
    }

    @Override
    public @Nullable <T extends Keyed> Registry<T> getRegistry(@NotNull RegistryKey<T> key) {
        return emptyRegistry();
    }

    /** Creates a fresh no-op {@link Registry} each call to avoid static-init ordering issues. */
    @SuppressWarnings("unchecked")
    private static <T extends Keyed> Registry<T> emptyRegistry() {
        return (Registry<T>) new NoOpRegistry();
    }

    // Named inner class (not anonymous) to avoid synthetic static-field references.
    @SuppressWarnings("UnstableApiUsage")
    private static final class NoOpRegistry implements Registry<Keyed> {
        @Override public @Nullable Keyed get(@NotNull NamespacedKey key) { return null; }
        // Override getOrThrow to return null instead of throwing NoSuchElementException,
        // so that Enchantment.<clinit> can complete (enchantment constants will be null).
        @Override public @NotNull Keyed getOrThrow(@NotNull NamespacedKey key) { return null; }
        @Override public @Nullable NamespacedKey getKey(@NotNull Keyed value) { return null; }
        @Override public boolean hasTag(@NotNull TagKey<Keyed> key) { return false; }
        @Override public @Nullable Tag<Keyed> getTag(@NotNull TagKey<Keyed> key) { return null; }
        @Override public @NotNull Collection<Tag<Keyed>> getTags() { return Collections.emptyList(); }
        @Override public @NotNull Stream<Keyed> stream() { return Stream.empty(); }
        @Override public @NotNull Stream<NamespacedKey> keyStream() { return Stream.empty(); }
        @Override public int size() { return 0; }
        @Override public @NotNull Iterator<Keyed> iterator() { return Collections.emptyIterator(); }
    }
}
