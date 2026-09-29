package com.example.settlementrpg.ui.screens

/**
 * FONTE ÚNICA DE VERDADE para alturas de referência de sprites (em pixels lógicos a zoom 1.0x).
 *
 * Hierarquia obrigatória (validada em SpriteHierarchyTest):
 *   GUILDA > EDIFÍCIOS MODULARES > HERÓIS
 *   Monstros, decorações e coletas podem ultrapassar o herói,
 *   mas NUNCA podem ultrapassar o menor edifício modular.
 *
 * ⚠️  Nunca adicionar constantes de refHeight fora deste objeto.
 * ⚠️  Qualquer alteração deve respeitar as invariantes de SpriteHierarchyTest.
 *
 * NOTA: refHeight controla APENAS a altura de renderização em tela.
 * A LARGURA é calculada automaticamente via trimSpriteSheet() + drawIsoSpriteBitmap().
 *
 * NOTA: As constantes GUILD_LVL* não são usadas em drawIsoSpriteBitmap() porque
 * a Guilda usa renderização procedural com geometria própria (drawIsoCastle).
 * Elas existem aqui como documentação da hierarquia e para os testes de invariante.
 */
object SpriteHeights {

    // -------------------------------------------------------------------------
    // GUILDA — estrutura central, sempre a maior entidade do mapa
    // (renderizada via drawIsoCastle — geometria procedural, não refHeight)
    // -------------------------------------------------------------------------
    const val GUILD_LVL1 = 80f   // Casebre rústico (Box 52f + Pirâmide 28f)
    const val GUILD_LVL2 = 110f  // Fortaleza de pedra (Torre 82f + Pirâmide 28f)
    const val GUILD_LVL3 = 140f  // Castelo 3 torres (Torre 107f + Pirâmide 33f)

    // -------------------------------------------------------------------------
    // EDIFÍCIOS MODULARES — menores que Guilda Lvl1, maiores que heróis
    // (refHeight usado em drawIsoSpriteBitmap quando sprite bitmap estiver disponível)
    // -------------------------------------------------------------------------
    const val FERRARIA = 70f    // Ferraria isométrica
    const val TABERNA  = 68f    // Taberna isométrica
    const val MERCADOR = 66f    // Mercador/Banca isométrica

    // -------------------------------------------------------------------------
    // HERÓIS — referência central da hierarquia visual
    // -------------------------------------------------------------------------
    const val HERO = 48f        // Todos os heróis jogáveis (Guerreiro, Maga, Arqueiro, Clériga)

    // -------------------------------------------------------------------------
    // MONSTROS — podem ser maiores ou menores que HERO visualmente,
    // mas nunca maiores que o menor edifício modular (MERCADOR = 66f)
    // -------------------------------------------------------------------------
    const val ORC    = 52f      // Orc — maior que herói para imponência
    const val LOBO   = 44f      // Lobo — ágil, menor que herói
    const val GOBLIN = 40f      // Goblin — menor criatura bípede
    const val SLIME  = 28f      // Slime — menor criatura

    // -------------------------------------------------------------------------
    // COLETAS (pseudo-monstros de missão) — objetos de terreno
    // Podem ultrapassar HERO, mas devem ficar < menor edifício (MERCADOR)
    // -------------------------------------------------------------------------
    const val COLETA_MADEIRA = 60f   // Árvore grande
    const val COLETA_PEDRA   = 45f   // Rocha
    const val COLETA_ERVAS   = 35f   // Ervas rasteiras
    const val COLETA_FERRO   = 48f   // Minério

    // -------------------------------------------------------------------------
    // DECORAÇÃO — elementos visuais de fundo
    // Podem ser de qualquer tamanho menor que o menor edifício
    // -------------------------------------------------------------------------
    const val DECOR_TREE = 52f  // Árvore decorativa
    const val DECOR_ROCK = 44f  // Pedra decorativa

    // Menor edifício modular — teto absoluto para monstros, coletas e decoração
    val EDIFICIO_MIN = minOf(FERRARIA, TABERNA, MERCADOR)  // = 66f (MERCADOR)
}
