package io.ncbpfluffybear.fluffymachines.items;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * Resolves which worlds share one and the same ender chest.
 *
 * <p>El ender chest no se separa por mundo sino por grupo: InvSwitcher solo intercambia el
 * contenido al cruzar de grupo, y su {@code onWorldEnter} descarta el cambio cuando
 * {@code Util.sameWorld} da verdadero, de modo que un mundo gestionado comparte cofre con su
 * nether y su end. Los mundos que InvSwitcher no gestiona conservan el ender chest vanilla,
 * que es unico por jugador para todos ellos.
 *
 * <p>Ante cualquier duda (config ilegible, intercambio de ender chest desactivado) se responde
 * que no comparten, que es la respuesta segura: un falso negativo apaga el nodo, un falso
 * positivo lo convertiria en un puente entre modalidades.
 */
final class EnderChestGroups {

    /** Clave de grupo de los mundos que InvSwitcher no gestiona: comparten el cofre vanilla. */
    static final String UNMANAGED_GROUP = "";

    private static final String[] DIMENSION_SUFFIXES = {"_nether", "_the_end"};
    private static final long RELOAD_INTERVAL_MS = 60_000L;

    private static volatile Snapshot snapshot;
    private static volatile long lastLoadAttempt;

    private EnderChestGroups() {
    }

    /**
     * @return {@code true} solo si ambos mundos apuntan con certeza al mismo ender chest
     */
    static boolean shareEnderChest(@Nullable World a, @Nullable World b) {
        if (a == null || b == null) {
            return false;
        }

        if (a.equals(b)) {
            return true;
        }

        Snapshot current = currentSnapshot();

        if (current == null || !current.enderChestSwitching) {
            return false;
        }

        return groupKey(a.getName(), current.managedWorlds)
            .equals(groupKey(b.getName(), current.managedWorlds));
    }

    /**
     * Traduce un mundo a la clave del grupo de ender chest al que pertenece.
     *
     * <p>Solo se retira el sufijo dimensional cuando el mundo base figura entre los gestionados;
     * asi {@code world_nether} de un Survival no gestionado no se confunde con un
     * {@code world} que si lo estuviera.
     */
    @Nonnull
    static String groupKey(@Nonnull String worldName, @Nonnull Set<String> managedWorlds) {
        String name = worldName.toLowerCase(Locale.ROOT);

        if (managedWorlds.contains(name)) {
            return name;
        }

        for (String suffix : DIMENSION_SUFFIXES) {
            if (name.endsWith(suffix)) {
                String base = name.substring(0, name.length() - suffix.length());

                if (managedWorlds.contains(base)) {
                    return base;
                }
            }
        }

        return UNMANAGED_GROUP;
    }

    @Nullable
    private static Snapshot currentSnapshot() {
        long now = System.currentTimeMillis();

        if (snapshot != null && now - lastLoadAttempt < RELOAD_INTERVAL_MS) {
            return snapshot;
        }

        synchronized (EnderChestGroups.class) {
            if (snapshot != null && now - lastLoadAttempt < RELOAD_INTERVAL_MS) {
                return snapshot;
            }

            lastLoadAttempt = now;
            Snapshot loaded = load();

            if (loaded != null) {
                snapshot = loaded;
            }

            return snapshot;
        }
    }

    @Nullable
    private static Snapshot load() {
        File config = locateConfig();

        if (config == null || !config.isFile()) {
            return null;
        }

        try {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(config);
            List<String> worlds = yaml.getStringList("worlds");
            Set<String> managed = new HashSet<>();

            for (String world : worlds) {
                if (world != null && !world.isBlank()) {
                    managed.add(world.toLowerCase(Locale.ROOT));
                }
            }

            if (managed.isEmpty()) {
                return null;
            }

            return new Snapshot(Collections.unmodifiableSet(managed),
                yaml.getBoolean("options.ender-chest", false));
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    @Nullable
    private static File locateConfig() {
        try {
            Plugin bentoBox = Bukkit.getPluginManager().getPlugin("BentoBox");
            File base = bentoBox != null ? bentoBox.getDataFolder() : new File("plugins", "BentoBox");

            return new File(base, "addons" + File.separator + "InvSwitcher"
                + File.separator + "config.yml");
        } catch (RuntimeException | NoClassDefFoundError ignored) {
            return null;
        }
    }

    private static final class Snapshot {

        private final Set<String> managedWorlds;
        private final boolean enderChestSwitching;

        private Snapshot(Set<String> managedWorlds, boolean enderChestSwitching) {
            this.managedWorlds = managedWorlds;
            this.enderChestSwitching = enderChestSwitching;
        }
    }
}
