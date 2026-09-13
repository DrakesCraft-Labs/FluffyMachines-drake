package io.ncbpfluffybear.fluffymachines.items.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.function.Predicate;
import org.bukkit.Material;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Forma vanilla que adopta el paxel segun el bloque apuntado (ticket 358). */
class PaxelFormTest {

    /** Muestra de los bloques que en produccion llevan pala. */
    private static final Predicate<Material> PALA =
        Set.of(Material.DIRT, Material.GRASS_BLOCK, Material.SAND, Material.GRAVEL)::contains;

    /** Muestra de los bloques que el paxel rompe con hacha. */
    private static final Set<Material> HACHA =
        Set.of(Material.OAK_LOG, Material.OAK_PLANKS, Material.CHEST, Material.OAK_LEAVES);

    @Test
    @DisplayName("cada familia de bloque elige su herramienta")
    void cadaFamiliaEligeSuHerramienta() {
        assertEquals(Material.DIAMOND_SHOVEL, PaxelForm.formaPara(Material.GRAVEL, false, PALA, HACHA));
        assertEquals(Material.DIAMOND_AXE, PaxelForm.formaPara(Material.OAK_LOG, false, PALA, HACHA));
        assertEquals(Material.DIAMOND_PICKAXE, PaxelForm.formaPara(Material.STONE, false, PALA, HACHA));
    }

    @Test
    @DisplayName("el paxel de netherita nunca degrada a diamante al cambiar de forma")
    void laNetheritaSeConserva() {
        assertEquals(Material.NETHERITE_SHOVEL, PaxelForm.formaPara(Material.SAND, true, PALA, HACHA));
        assertEquals(Material.NETHERITE_AXE, PaxelForm.formaPara(Material.CHEST, true, PALA, HACHA));
        assertEquals(Material.NETHERITE_PICKAXE, PaxelForm.formaPara(Material.DEEPSLATE, true, PALA, HACHA));
        assertEquals(Material.NETHERITE_AXE, PaxelForm.formaDeCombate(true));
        assertEquals(Material.DIAMOND_AXE, PaxelForm.formaDeCombate(false));
    }

    @Test
    @DisplayName("la pala gana al hacha cuando un bloque esta en ambos conjuntos")
    void laPalaTienePrioridadSobreElHacha() {
        Set<Material> hachaConGrava = Set.of(Material.OAK_LOG, Material.GRAVEL);

        assertEquals(Material.DIAMOND_SHOVEL,
            PaxelForm.formaPara(Material.GRAVEL, false, PALA, hachaConGrava));
    }

    @Test
    @DisplayName("un bloque nulo o desconocido cae en el pico, como hacia el paxel original")
    void elBloqueDesconocidoCaeEnElPico() {
        assertEquals(Material.DIAMOND_PICKAXE, PaxelForm.formaPara(null, false, PALA, HACHA));
        assertEquals(Material.NETHERITE_PICKAXE, PaxelForm.formaPara(null, true, PALA, HACHA));
        assertEquals(Material.DIAMOND_PICKAXE, PaxelForm.formaPara(Material.OBSIDIAN, false, PALA, HACHA));
    }

    @Test
    @DisplayName("solo las seis formas vanilla del paxel pasan el descarte barato")
    void soloLasSeisFormasPasanElDescarte() {
        for (Material forma : Set.of(Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.DIAMOND_SHOVEL,
            Material.NETHERITE_PICKAXE, Material.NETHERITE_AXE, Material.NETHERITE_SHOVEL)) {
            assertTrue(PaxelForm.esFormaDePaxel(forma), forma.name());
        }

        assertFalse(PaxelForm.esFormaDePaxel(Material.IRON_PICKAXE));
        assertFalse(PaxelForm.esFormaDePaxel(Material.DIAMOND_HOE));
        assertFalse(PaxelForm.esFormaDePaxel(Material.AIR));
        assertFalse(PaxelForm.esFormaDePaxel(null));
    }

    @Test
    @DisplayName("la netherita se detecta por la forma actual del item")
    void laNetheritaSeDetectaPorLaFormaActual() {
        assertTrue(PaxelForm.esNetherita(Material.NETHERITE_SHOVEL));
        assertTrue(PaxelForm.esNetherita(Material.NETHERITE_PICKAXE));
        assertTrue(PaxelForm.esNetherita(Material.NETHERITE_AXE));
        assertFalse(PaxelForm.esNetherita(Material.DIAMOND_AXE));
        assertFalse(PaxelForm.esNetherita(null));
    }
}
