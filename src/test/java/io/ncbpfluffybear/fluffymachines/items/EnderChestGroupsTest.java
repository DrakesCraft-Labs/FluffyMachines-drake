package io.ncbpfluffybear.fluffymachines.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Agrupacion de ender chests que usan los nodos de FluffyMachines (ticket 304). */
class EnderChestGroupsTest {

    /** Mundos gestionados por InvSwitcher en produccion. */
    private static final Set<String> MANAGED =
        Set.of("oneblock_world", "clasico", "laboratorio", "bskyblock_world");

    @Test
    @DisplayName("el nether y el end de una modalidad gestionada comparten grupo con su mundo base")
    void dimensionesDeMundoGestionadoCompartenGrupo() {
        assertEquals(EnderChestGroups.groupKey("clasico", MANAGED),
            EnderChestGroups.groupKey("clasico_nether", MANAGED));
        assertEquals(EnderChestGroups.groupKey("bskyblock_world", MANAGED),
            EnderChestGroups.groupKey("bskyblock_world_the_end", MANAGED));
    }

    @Test
    @DisplayName("los mundos no gestionados caen todos en el grupo del ender chest vanilla")
    void mundosNoGestionadosCompartenElCofreVanilla() {
        assertEquals(EnderChestGroups.UNMANAGED_GROUP,
            EnderChestGroups.groupKey("world", MANAGED));
        assertEquals(EnderChestGroups.groupKey("world", MANAGED),
            EnderChestGroups.groupKey("world_nether", MANAGED));
        assertEquals(EnderChestGroups.groupKey("world", MANAGED),
            EnderChestGroups.groupKey("world_galactifun_mars", MANAGED));
    }

    @Test
    @DisplayName("dos modalidades gestionadas nunca comparten grupo")
    void modalidadesDistintasNoCompartenGrupo() {
        assertNotEquals(EnderChestGroups.groupKey("laboratorio", MANAGED),
            EnderChestGroups.groupKey("clasico", MANAGED));
        assertNotEquals(EnderChestGroups.groupKey("laboratorio", MANAGED),
            EnderChestGroups.groupKey("world", MANAGED));
        assertNotEquals(EnderChestGroups.groupKey("oneblock_world_nether", MANAGED),
            EnderChestGroups.groupKey("bskyblock_world_nether", MANAGED));
    }

    @Test
    @DisplayName("el sufijo dimensional solo se retira si el mundo base esta gestionado")
    void sufijoSoloSeRetiraSobreMundoGestionado() {
        assertEquals(EnderChestGroups.UNMANAGED_GROUP,
            EnderChestGroups.groupKey("eventos_nether", MANAGED));
        assertEquals("laboratorio", EnderChestGroups.groupKey("LABORATORIO_Nether", MANAGED));
    }
}
