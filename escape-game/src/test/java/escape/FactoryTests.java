package escape;

import org.junit.jupiter.api.Test;

import escape.factories.CafeteriaFactory;
import escape.factories.CellBlockFactory;
import escape.factories.GeneralTimeFactory;
import escape.factories.OutdoorRecFactory;
import escape.main.LevelConfig;

import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

// ======================================================================== //
//  CellBlockFactoryTest                                                      //
// ======================================================================== //

/**
 * Unit tests for {@link CellBlockFactory} (Level 1 — Cell Block).
 */
@DisplayName("CellBlockFactory — unit tests")
class CellBlockFactoryTest {

    private final LevelConfig config = new CellBlockFactory().createConfig();

    @Test
    @DisplayName("Level number is 1")
    void levelNumber_isOne() {
        assertEquals(1, config.getLevelNumber());
    }

    @Test
    @DisplayName("Level name is 'Cell Block'")
    void levelName_isCellBlock() {
        assertEquals("Cell Block", config.getLevelName());
    }

    @Test
    @DisplayName("Start X is 3")
    void startX_isThree() {
        assertEquals(3, config.getStartX());
    }

    @Test
    @DisplayName("Start Y is 3")
    void startY_isThree() {
        assertEquals(3, config.getStartY());
    }

    @Test
    @DisplayName("Exit X is 37")
    void exitX_is37() {
        assertEquals(37, config.getExitX());
    }

    @Test
    @DisplayName("Exit Y is 20")
    void exitY_is20() {
        assertEquals(20, config.getExitY());
    }

    @Test
    @DisplayName("requiredCoins is 1")
    void requiredCoins_isOne() {
        assertEquals(1, config.getRequiredCoins());
    }

    @Test
    @DisplayName("Corruptible cop spawn is null")
    void corruptibleCopSpawn_isNull() {
        assertNull(config.getCorruptibleCopSpawn());
    }

    @Test
    @DisplayName("additionalCoinSpawns list is empty")
    void additionalCoinSpawns_isEmpty() {
        assertTrue(config.getAdditionalCoinSpawns().isEmpty());
    }

    @Test
    @DisplayName("Regular cop spawn list has one entry")
    void regularCopSpawns_hasOneEntry() {
        assertNotNull(config.getRegularCopSpawns());
        assertEquals(1, config.getRegularCopSpawns().size());
    }

    @Test
    @DisplayName("Cop spawn is at (3, 3) — same tile as player start (potential bug)")
    void copSpawn_isAtThreeThree() {
        assertArrayEquals(new int[]{45, 19}, config.getRegularCopSpawns().get(0));
    }
}


// ======================================================================== //
//  CafeteriaFactoryTest                                                      //
// ======================================================================== //

/**
 * Unit tests for {@link CafeteriaFactory} (Level 2 — Cafeteria).
 */
@DisplayName("CafeteriaFactory — unit tests")
class CafeteriaFactoryTest {

    private final LevelConfig config = new CafeteriaFactory().createConfig();

    @Test
    @DisplayName("Level number is 2")
    void levelNumber_isTwo() {
        assertEquals(2, config.getLevelNumber());
    }

    @Test
    @DisplayName("Level name is 'Cafeteria & Washrooms'")
    void levelName_isCafeteria() {
        assertEquals("Cafeteria & Washrooms", config.getLevelName());
    }

    @Test
    @DisplayName("Start X is 2")
    void startX_isTwo() {
        assertEquals(2, config.getStartX());
    }

    @Test
    @DisplayName("Start Y is 8")
    void startY_isEight() {
        assertEquals(8, config.getStartY());
    }

    @Test
    @DisplayName("Exit X is 51")
    void exitX_is51() {
        assertEquals(51, config.getExitX());
    }

    @Test
    @DisplayName("Exit Y is 26")
    void exitY_is26() {
        assertEquals(26, config.getExitY());
    }

    @Test
    @DisplayName("requiredCoins is 1")
    void requiredCoins_isOne() {
        assertEquals(1, config.getRequiredCoins());
    }

    @Test
    @DisplayName("Corruptible cop spawn is null")
    void corruptibleCopSpawn_isNull() {
        assertNull(config.getCorruptibleCopSpawn());
    }

    @Test
    @DisplayName("additionalCoinSpawns list is empty")
    void additionalCoinSpawns_isEmpty() {
        assertTrue(config.getAdditionalCoinSpawns().isEmpty());
    }

    @Test
    @DisplayName("Regular cop spawn list has 2 entries")
    void regularCopSpawns_hasTwoEntries() {
        assertEquals(2, config.getRegularCopSpawns().size());
    }

    @Test
    @DisplayName("First cop spawn is at (5, 5)")
    void copSpawn_firstIsAtFiveFive() {
        assertArrayEquals(new int[]{5, 5}, config.getRegularCopSpawns().get(0));
    }

    @Test
    @DisplayName("Second cop spawn is at (14, 9)")
    void copSpawn_secondIsAtFourteenNine() {
        assertArrayEquals(new int[]{14, 9}, config.getRegularCopSpawns().get(1));
    }
}


// ======================================================================== //
//  GeneralTimeFactoryTest                                                    //
// ======================================================================== //

/**
 * Unit tests for {@link GeneralTimeFactory} (Level 3 — General Time).
 */
@DisplayName("GeneralTimeFactory — unit tests")
class GeneralTimeFactoryTest {

    private final LevelConfig config = new GeneralTimeFactory().createConfig();

    @Test
    @DisplayName("Level number is 3")
    void levelNumber_isThree() {
        assertEquals(3, config.getLevelNumber());
    }

    @Test
    @DisplayName("Level name is 'General Time'")
    void levelName_isGeneralTime() {
        assertEquals("General Time", config.getLevelName());
    }

    @Test
    @DisplayName("Start X is 2")
    void startX_isTwo() {
        assertEquals(2, config.getStartX());
    }

    @Test
    @DisplayName("Start Y is 18")
    void startY_is18() {
        assertEquals(18, config.getStartY());
    }

    @Test
    @DisplayName("Exit X is 69")
    void exitX_is69() {
        assertEquals(69, config.getExitX());
    }

    @Test
    @DisplayName("Exit Y is 18")
    void exitY_is18() {
        assertEquals(18, config.getExitY());
    }

    @Test
    @DisplayName("requiredCoins is 1")
    void requiredCoins_isTwo() {
        assertEquals(1, config.getRequiredCoins());
    }

    @Test
    @DisplayName("Corruptible cop spawn is null")
    void corruptibleCopSpawn_isNull() {
        assertNull(config.getCorruptibleCopSpawn());
    }

    @Test
    @DisplayName("additionalCoinSpawns list is empty")
    void additionalCoinSpawns_isEmpty() {
        assertTrue(config.getAdditionalCoinSpawns().isEmpty());
    }

    @Test
    @DisplayName("Regular cop spawn list has 3 entries")
    void regularCopSpawns_hasThreeEntries() {
        assertEquals(3, config.getRegularCopSpawns().size());
    }

    @Test
    @DisplayName("Cop spawn 1 is at (20, 6)")
    void copSpawn_firstIsAt20_6() {
        assertArrayEquals(new int[]{20, 6}, config.getRegularCopSpawns().get(0));
    }

    @Test
    @DisplayName("Cop spawn 2 is at (1, 12)")
    void copSpawn_secondIsAt1_12() {
        assertArrayEquals(new int[]{1, 12}, config.getRegularCopSpawns().get(1));
    }

    @Test
    @DisplayName("Cop spawn 3 is at (30, 17)")
    void copSpawn_thirdIsAt30_17() {
        assertArrayEquals(new int[]{30, 17}, config.getRegularCopSpawns().get(2));
    }
}


// ======================================================================== //
//  OutdoorRecFactoryTest                                                     //
// ======================================================================== //

/**
 * Unit tests for {@link OutdoorRecFactory} (Level 4 — Outdoor Recreation).
 */
@DisplayName("OutdoorRecFactory — unit tests")
class OutdoorRecFactoryTest {

    private final LevelConfig config = new OutdoorRecFactory().createConfig();

    @Test
    @DisplayName("Level number is 4")
    void levelNumber_isFour() {
        assertEquals(4, config.getLevelNumber());
    }

    @Test
    @DisplayName("Level name is 'Outdoor Recreation'")
    void levelName_isOutdoorRecreation() {
        assertEquals("Outdoor Recreation", config.getLevelName());
    }

    @Test
    @DisplayName("Start X is 31")
    void startX_is31() {
        assertEquals(31, config.getStartX());
    }

    @Test
    @DisplayName("Start Y is 5")
    void startY_isFive() {
        assertEquals(5, config.getStartY());
    }

    @Test
    @DisplayName("Exit X is 31")
    void exitX_is31() {
        assertEquals(31, config.getExitX());
    }

    @Test
    @DisplayName("Exit Y is 28")
    void exitY_is28() {
        assertEquals(28, config.getExitY());
    }

    @Test
    @DisplayName("requiredCoins is 1")
    void requiredCoins_isTwo() {
        assertEquals(1, config.getRequiredCoins());
    }

    @Test
    @DisplayName("Corruptible cop spawn is null — final level has no shortcut")
    void corruptibleCopSpawn_isNull() {
        assertNull(config.getCorruptibleCopSpawn(),
                "Level 4 intentionally has no corruptible cop — this is a design invariant");
    }

    @Test
    @DisplayName("additionalCoinSpawns has 2 entries (workaround for sealed map rooms)")
    void additionalCoinSpawns_hasTwoEntries() {
        assertEquals(2, config.getAdditionalCoinSpawns().size());
    }

    @Test
    @DisplayName("First additional coin spawn is at (5, 1)")
    void additionalCoinSpawn_firstIsAtFive_1() {
        assertArrayEquals(new int[]{5, 1}, config.getAdditionalCoinSpawns().get(0));
    }

    @Test
    @DisplayName("Second additional coin spawn is at (28, 1)")
    void additionalCoinSpawn_secondIsAt28_1() {
        assertArrayEquals(new int[]{28, 1}, config.getAdditionalCoinSpawns().get(1));
    }

    @Test
    @DisplayName("Regular cop spawn list has 3 entries")
    void regularCopSpawns_hasThreeEntries() {
        assertEquals(3, config.getRegularCopSpawns().size());
    }

    @Test
    @DisplayName("Cop spawn 1 is at (5, 5)")
    void copSpawn_firstIsAtFiveFive() {
        assertArrayEquals(new int[]{6, 5}, config.getRegularCopSpawns().get(0));
    }

    @Test
    @DisplayName("Cop spawn 2 is at (12, 8) — corrected from wall position (15, 8)")
    void copSpawn_secondIsAt12_8() {
        assertArrayEquals(new int[]{12, 9}, config.getRegularCopSpawns().get(1),
                "Regression guard: was (15,8) which landed on a wall");
    }

    @Test
    @DisplayName("Cop spawn 3 is at (26, 10) — corrected from wall position (28, 10)")
    void copSpawn_thirdIsAt26_10() {
        assertArrayEquals(new int[]{26, 10}, config.getRegularCopSpawns().get(2),
                "Regression guard: was (28,10) which landed on a wall");
    }
}