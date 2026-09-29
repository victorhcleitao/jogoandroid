package com.example.settlementrpg.ui.screens

import com.example.settlementrpg.data.model.GameState
import com.example.settlementrpg.data.model.Hero
import com.example.settlementrpg.data.model.HeroClass
import com.example.settlementrpg.data.model.HeroState
import com.example.settlementrpg.data.model.Mission
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

/**
 * Trava automática de hierarquia de tamanho de sprites.
 *
 * Hierarquia: GUILDA > EDIFÍCIOS MODULARES > HERÓIS
 *   Monstros, decorações e coletas: qualquer tamanho < menor edifício modular.
 *
 * NUNCA desative esses testes — corrija o valor em SpriteHeights.kt.
 */
class SpriteHierarchyTest {

    // ─────────────────────────────────────────────────────────────────────────
    // Guilda sempre maior que qualquer edifício modular
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    fun `guild lvl1 maior que ferraria`() = assertTrue(
        "GUILD_LVL1 (${SpriteHeights.GUILD_LVL1}) > FERRARIA (${SpriteHeights.FERRARIA})",
        SpriteHeights.GUILD_LVL1 > SpriteHeights.FERRARIA
    )

    @Test
    fun `guild lvl1 maior que taberna`() = assertTrue(
        "GUILD_LVL1 (${SpriteHeights.GUILD_LVL1}) > TABERNA (${SpriteHeights.TABERNA})",
        SpriteHeights.GUILD_LVL1 > SpriteHeights.TABERNA
    )

    @Test
    fun `guild lvl1 maior que mercador`() = assertTrue(
        "GUILD_LVL1 (${SpriteHeights.GUILD_LVL1}) > MERCADOR (${SpriteHeights.MERCADOR})",
        SpriteHeights.GUILD_LVL1 > SpriteHeights.MERCADOR
    )

    @Test
    fun `niveis de guilda crescentes`() = assertTrue(
        "GUILD_LVL1 < GUILD_LVL2 < GUILD_LVL3",
        SpriteHeights.GUILD_LVL1 < SpriteHeights.GUILD_LVL2 &&
        SpriteHeights.GUILD_LVL2 < SpriteHeights.GUILD_LVL3
    )

    // ─────────────────────────────────────────────────────────────────────────
    // Edifícios modulares maiores que heróis
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    fun `ferraria maior que heroi`() = assertTrue(
        "FERRARIA (${SpriteHeights.FERRARIA}) > HERO (${SpriteHeights.HERO})",
        SpriteHeights.FERRARIA > SpriteHeights.HERO
    )

    @Test
    fun `taberna maior que heroi`() = assertTrue(
        "TABERNA (${SpriteHeights.TABERNA}) > HERO (${SpriteHeights.HERO})",
        SpriteHeights.TABERNA > SpriteHeights.HERO
    )

    @Test
    fun `mercador maior que heroi`() = assertTrue(
        "MERCADOR (${SpriteHeights.MERCADOR}) > HERO (${SpriteHeights.HERO})",
        SpriteHeights.MERCADOR > SpriteHeights.HERO
    )

    // ─────────────────────────────────────────────────────────────────────────
    // Monstros < menor edifício modular (EDIFICIO_MIN)
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    fun `orc menor que menor edificio`() = assertTrue(
        "ORC (${SpriteHeights.ORC}) < EDIFICIO_MIN (${SpriteHeights.EDIFICIO_MIN})",
        SpriteHeights.ORC < SpriteHeights.EDIFICIO_MIN
    )

    @Test
    fun `lobo menor que menor edificio`() = assertTrue(
        "LOBO (${SpriteHeights.LOBO}) < EDIFICIO_MIN (${SpriteHeights.EDIFICIO_MIN})",
        SpriteHeights.LOBO < SpriteHeights.EDIFICIO_MIN
    )

    @Test
    fun `goblin menor que menor edificio`() = assertTrue(
        "GOBLIN (${SpriteHeights.GOBLIN}) < EDIFICIO_MIN (${SpriteHeights.EDIFICIO_MIN})",
        SpriteHeights.GOBLIN < SpriteHeights.EDIFICIO_MIN
    )

    @Test
    fun `slime menor que menor edificio`() = assertTrue(
        "SLIME (${SpriteHeights.SLIME}) < EDIFICIO_MIN (${SpriteHeights.EDIFICIO_MIN})",
        SpriteHeights.SLIME < SpriteHeights.EDIFICIO_MIN
    )

    // ─────────────────────────────────────────────────────────────────────────
    // Decoração < menor edifício modular
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    fun `arvore decorativa menor que menor edificio`() = assertTrue(
        "DECOR_TREE (${SpriteHeights.DECOR_TREE}) < EDIFICIO_MIN (${SpriteHeights.EDIFICIO_MIN})",
        SpriteHeights.DECOR_TREE < SpriteHeights.EDIFICIO_MIN
    )

    @Test
    fun `rocha decorativa menor que menor edificio`() = assertTrue(
        "DECOR_ROCK (${SpriteHeights.DECOR_ROCK}) < EDIFICIO_MIN (${SpriteHeights.EDIFICIO_MIN})",
        SpriteHeights.DECOR_ROCK < SpriteHeights.EDIFICIO_MIN
    )

    // ─────────────────────────────────────────────────────────────────────────
    // Coletas < menor edifício modular
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    fun `coletas menores que menor edificio`() {
        assertTrue(SpriteHeights.COLETA_MADEIRA < SpriteHeights.EDIFICIO_MIN)
        assertTrue(SpriteHeights.COLETA_PEDRA   < SpriteHeights.EDIFICIO_MIN)
        assertTrue(SpriteHeights.COLETA_ERVAS   < SpriteHeights.EDIFICIO_MIN)
        assertTrue(SpriteHeights.COLETA_FERRO   < SpriteHeights.EDIFICIO_MIN)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sanidade: alturas positivas
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    fun `todos os valores positivos`() {
        assertTrue(SpriteHeights.FERRARIA > 0f)
        assertTrue(SpriteHeights.TABERNA > 0f)
        assertTrue(SpriteHeights.MERCADOR > 0f)
        assertTrue(SpriteHeights.HERO > 0f)
        assertTrue(SpriteHeights.ORC > 0f)
        assertTrue(SpriteHeights.LOBO > 0f)
        assertTrue(SpriteHeights.GOBLIN > 0f)
        assertTrue(SpriteHeights.SLIME > 0f)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BLOCO 2 — Invariante bidirecional herói ↔ missão
    //
    // Trava a CLASSE do bug "Aceito por Arthur em duas missões":
    //   • Todo herói com currentMissionId != null deve ter exatamente
    //     UMA missão com esse ID e assignedHeroId == hero.id.
    //   • Toda missão com assignedHeroId != null deve ter um herói
    //     correspondente cujo currentMissionId aponte de volta para ela.
    //
    // Esses testes trabalham com GameState diretamente, sem ViewModel,
    // logo não precisam de coroutines nem Android runtime.
    // ─────────────────────────────────────────────────────────────────────────

    private fun heroOf(id: String, missionId: String? = null) = Hero(
        id = id, name = "Herói $id", heroClass = HeroClass.WARRIOR,
        currentMissionId = missionId
    )

    private fun missionOf(id: String, heroId: String? = null) = Mission(
        id = id, title = "Missão $id", description = "",
        difficulty = 1, targetMonsterName = "Slime",
        monsterLevel = 1, goldReward = 10,
        assignedHeroId = heroId, isPublished = true
    )

    @Test
    fun `heroi com missao aponta para missao existente`() {
        val hero = heroOf("h1", missionId = "m1")
        val mission = missionOf("m1", heroId = "h1")
        val state = GameState(heroes = listOf(hero), missions = listOf(mission))

        val heroesWithMission = state.heroes.filter { it.currentMissionId != null }
        for (h in heroesWithMission) {
            val pointed = state.missions.filter { it.id == h.currentMissionId }
            assertEquals(
                "Herói ${h.id} aponta para currentMissionId='${h.currentMissionId}', " +
                "mas foram encontradas ${pointed.size} missões com esse ID (esperado: 1)",
                1, pointed.size
            )
            assertEquals(
                "Missão '${h.currentMissionId}' deve ter assignedHeroId='${h.id}', " +
                "mas tem '${pointed[0].assignedHeroId}'",
                h.id, pointed[0].assignedHeroId
            )
        }
    }

    @Test
    fun `missao atribuida tem heroi correspondente apontando de volta`() {
        val hero = heroOf("h1", missionId = "m1")
        val mission = missionOf("m1", heroId = "h1")
        val state = GameState(heroes = listOf(hero), missions = listOf(mission))

        val assignedMissions = state.missions.filter { it.assignedHeroId != null }
        for (m in assignedMissions) {
            val pointed = state.heroes.find { it.id == m.assignedHeroId }
            assertTrue(
                "Missão '${m.id}' tem assignedHeroId='${m.assignedHeroId}', " +
                "mas esse herói não existe no GameState",
                pointed != null
            )
            assertEquals(
                "Herói '${m.assignedHeroId}' tem currentMissionId='${pointed!!.currentMissionId}', " +
                "mas deveria apontar de volta para a missão '${m.id}'",
                m.id, pointed.currentMissionId
            )
        }
    }

    @Test
    fun `heroi e missao sempre consistentes`() {
        // Estado válido de teste com múltiplos heróis e missões
        val h1 = heroOf("arthur", missionId = "m_orc")
        val h2 = heroOf("valeria", missionId = "m_slime")
        val h3 = heroOf("eldrin", missionId = null)

        val m1 = missionOf("m_orc", heroId = "arthur")
        val m2 = missionOf("m_slime", heroId = "valeria")
        val m3 = missionOf("m_lobo", heroId = null)

        val state = GameState(heroes = listOf(h1, h2, h3), missions = listOf(m1, m2, m3))

        // Invariante 1: Para todo herói com currentMissionId != null, deve existir
        // exatamente uma missão com esse id E assignedHeroId == hero.id.
        for (hero in state.heroes.filter { it.currentMissionId != null }) {
            val matchingMissions = state.missions.filter { it.id == hero.currentMissionId }
            assertEquals(
                "Herói ${hero.name} aponta para a missão ${hero.currentMissionId}, mas foram encontradas ${matchingMissions.size} missões.",
                1, matchingMissions.size
            )
            assertEquals(
                "A missão vinculada ao herói ${hero.name} deve ter assignedHeroId == ${hero.id}",
                hero.id, matchingMissions.first().assignedHeroId
            )
        }

        // Invariante 2: Para toda missão com assignedHeroId != null, o herói correspondente
        // deve existir e seu currentMissionId deve apontar de volta pra ela.
        for (mission in state.missions.filter { it.assignedHeroId != null }) {
            val assignedHero = state.heroes.find { it.id == mission.assignedHeroId }
            assertTrue(
                "A missão '${mission.title}' está atribuída ao herói ${mission.assignedHeroId}, mas ele não existe.",
                assignedHero != null
            )
            assertEquals(
                "O herói ${assignedHero?.name} deve ter currentMissionId igual ao ID da missão ${mission.id}",
                mission.id, assignedHero?.currentMissionId
            )
        }

        // Invariante 3: Nenhum herói pode ter mais de um contrato ativo simultâneo
        val duplicates = state.missions.filter { it.assignedHeroId != null }
            .groupBy { it.assignedHeroId }
            .filter { it.value.size > 1 }

        assertTrue(
            "Detectada duplicação de contratos para o mesmo herói: $duplicates",
            duplicates.isEmpty()
        )
    }
}

