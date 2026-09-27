package com.example.settlementrpg.ui.screens

import com.example.settlementrpg.ui.screens.SpriteHeights
import junit.framework.TestCase.assertTrue
import org.junit.Test

/**
 * Trava automática de hierarquia de tamanho de sprites.
 *
 * Regra: GUILDA > EDIFÍCIOS MODULARES > HERÓIS ≥ MONSTROS (coletas fora da hierarquia visual)
 *        DECORAÇÃO ≤ DECOR_MAX (1.3× HERO)
 *
 * Se qualquer teste aqui falhar, significa que uma constante em SpriteHeights
 * foi alterada de forma que viola a hierarquia de design do jogo.
 * NUNCA desative esses testes — corrija o valor em SpriteHeights.kt.
 */
class SpriteHierarchyTest {

    // -------------------------------------------------------------------------
    // Guilda sempre maior que qualquer edifício modular
    // -------------------------------------------------------------------------
    @Test
    fun `guild lvl1 maior que ferraria`() {
        assertTrue(
            "GUILD_LVL1 (${SpriteHeights.GUILD_LVL1}) deve ser > FERRARIA (${SpriteHeights.FERRARIA})",
            SpriteHeights.GUILD_LVL1 > SpriteHeights.FERRARIA
        )
    }

    @Test
    fun `guild lvl1 maior que mercador`() {
        assertTrue(
            "GUILD_LVL1 (${SpriteHeights.GUILD_LVL1}) deve ser > MERCADOR (${SpriteHeights.MERCADOR})",
            SpriteHeights.GUILD_LVL1 > SpriteHeights.MERCADOR
        )
    }

    @Test
    fun `guild lvl1 maior que taberna`() {
        assertTrue(
            "GUILD_LVL1 (${SpriteHeights.GUILD_LVL1}) deve ser > TABERNA (${SpriteHeights.TABERNA})",
            SpriteHeights.GUILD_LVL1 > SpriteHeights.TABERNA
        )
    }

    @Test
    fun `niveis de guilda crescentes`() {
        assertTrue(
            "GUILD_LVL1 < GUILD_LVL2 < GUILD_LVL3",
            SpriteHeights.GUILD_LVL1 < SpriteHeights.GUILD_LVL2 &&
            SpriteHeights.GUILD_LVL2 < SpriteHeights.GUILD_LVL3
        )
    }

    // -------------------------------------------------------------------------
    // Monstros não podem ultrapassar o porte do herói
    // -------------------------------------------------------------------------
    @Test
    fun `orc nao ultrapassa heroi`() {
        assertTrue(
            "ORC (${SpriteHeights.ORC}) deve ser ≤ HERO (${SpriteHeights.HERO})",
            SpriteHeights.ORC <= SpriteHeights.HERO
        )
    }

    @Test
    fun `lobo nao ultrapassa heroi`() {
        assertTrue(
            "LOBO (${SpriteHeights.LOBO}) deve ser ≤ HERO (${SpriteHeights.HERO})",
            SpriteHeights.LOBO <= SpriteHeights.HERO
        )
    }

    @Test
    fun `goblin nao ultrapassa heroi`() {
        assertTrue(
            "GOBLIN (${SpriteHeights.GOBLIN}) deve ser ≤ HERO (${SpriteHeights.HERO})",
            SpriteHeights.GOBLIN <= SpriteHeights.HERO
        )
    }

    @Test
    fun `slime nao ultrapassa heroi`() {
        assertTrue(
            "SLIME (${SpriteHeights.SLIME}) deve ser ≤ HERO (${SpriteHeights.HERO})",
            SpriteHeights.SLIME <= SpriteHeights.HERO
        )
    }

    // -------------------------------------------------------------------------
    // Decoração tem teto de 1.3× o herói
    // -------------------------------------------------------------------------
    @Test
    fun `decoracao nao ultrapassa teto de 1-3x heroi`() {
        assertTrue(
            "DECOR_MAX (${SpriteHeights.DECOR_MAX}) deve ser ≤ HERO × 1.3f (${SpriteHeights.HERO * 1.3f})",
            SpriteHeights.DECOR_MAX <= SpriteHeights.HERO * 1.3f
        )
    }

    @Test
    fun `arvore decorativa dentro do teto`() {
        assertTrue(
            "DECOR_TREE (${SpriteHeights.DECOR_TREE}) deve ser ≤ DECOR_MAX (${SpriteHeights.DECOR_MAX})",
            SpriteHeights.DECOR_TREE <= SpriteHeights.DECOR_MAX
        )
    }

    @Test
    fun `rocha decorativa dentro do teto`() {
        assertTrue(
            "DECOR_ROCK (${SpriteHeights.DECOR_ROCK}) deve ser ≤ DECOR_MAX (${SpriteHeights.DECOR_MAX})",
            SpriteHeights.DECOR_ROCK <= SpriteHeights.DECOR_MAX
        )
    }

    // -------------------------------------------------------------------------
    // Edifícios modulares menores que a Guilda e maiores que zero
    // -------------------------------------------------------------------------
    @Test
    fun `edificios modulares tem altura positiva`() {
        assertTrue(SpriteHeights.FERRARIA > 0f)
        assertTrue(SpriteHeights.MERCADOR > 0f)
        assertTrue(SpriteHeights.TABERNA > 0f)
    }

    // -------------------------------------------------------------------------
    // Todos os monstros têm altura positiva
    // -------------------------------------------------------------------------
    @Test
    fun `monstros tem altura positiva`() {
        assertTrue(SpriteHeights.ORC > 0f)
        assertTrue(SpriteHeights.LOBO > 0f)
        assertTrue(SpriteHeights.GOBLIN > 0f)
        assertTrue(SpriteHeights.SLIME > 0f)
    }
}
