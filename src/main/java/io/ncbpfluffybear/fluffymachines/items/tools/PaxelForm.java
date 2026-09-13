package io.ncbpfluffybear.fluffymachines.items.tools;

import java.util.Set;
import java.util.function.Predicate;
import org.bukkit.Material;

/**
 * Decide que forma vanilla (pico, hacha o pala) debe adoptar el paxel.
 *
 * <p>Vive aparte del listener y no toca el servidor para poder cubrirse con pruebas: los
 * conjuntos de bloques se inyectan desde {@link Paxel}, que si depende de los tags de Bukkit
 * y de Slimefun.
 */
public final class PaxelForm {

    /** Las seis formas vanilla que puede tomar un paxel. */
    private static final Set<Material> FORMAS = Set.of(
            Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.DIAMOND_SHOVEL,
            Material.NETHERITE_PICKAXE, Material.NETHERITE_AXE, Material.NETHERITE_SHOVEL);

    /** Formas del paxel mejorado a netherita. */
    private static final Set<Material> FORMAS_NETHERITA = Set.of(
            Material.NETHERITE_PICKAXE, Material.NETHERITE_AXE, Material.NETHERITE_SHOVEL);

    private PaxelForm() {}

    /** Descarte barato antes de mirar el PDC del item: solo estas formas pueden ser un paxel. */
    public static boolean esFormaDePaxel(Material forma) {
        return forma != null && FORMAS.contains(forma);
    }

    /** Un paxel de netherita conserva la netherita al cambiar de forma. */
    public static boolean esNetherita(Material forma) {
        return forma != null && FORMAS_NETHERITA.contains(forma);
    }

    /**
     * Forma adecuada para romper {@code bloque}. Devuelve el pico cuando el bloque es
     * desconocido o nulo, que es el comportamiento historico del paxel.
     */
    public static Material formaPara(Material bloque, boolean netherita,
            Predicate<Material> bloquesDePala, Set<Material> bloquesDeHacha) {

        if (bloque != null && bloquesDePala.test(bloque)) {
            return netherita ? Material.NETHERITE_SHOVEL : Material.DIAMOND_SHOVEL;
        }

        if (bloque != null && bloquesDeHacha.contains(bloque)) {
            return netherita ? Material.NETHERITE_AXE : Material.DIAMOND_AXE;
        }

        return netherita ? Material.NETHERITE_PICKAXE : Material.DIAMOND_PICKAXE;
    }

    /** Golpear entidades siempre usa el hacha, que es la forma con mas dano. */
    public static Material formaDeCombate(boolean netherita) {
        return netherita ? Material.NETHERITE_AXE : Material.DIAMOND_AXE;
    }
}
