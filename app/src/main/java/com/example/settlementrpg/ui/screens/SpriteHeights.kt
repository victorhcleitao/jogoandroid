package com.example.settlementrpg.ui.screens

/**
 * FONTE ÚNICA DE VERDADE para alturas de referência de sprites (em pixels lógicos a zoom 1.0x).
 *
 * Regra de hierarquia obrigatória (validada em SpriteHierarchyTest):
 *   GUILDA > EDIFÍCIOS MODULARES > HERÓIS ≥ MONSTROS > DECORAÇÃO
 *
 * ⚠️  Nunca adicionar constantes de refHeight fora deste objeto.
 * ⚠️  Qualquer alteração aqui deve respeitar as invariantes do teste unitário.
 *
 * IMPORTANTE: refHeight controla APENAS a altura de renderização em tela.
 * A LARGURA do sprite é sempre calculada automaticamente pela proporção
 * trimada em trimSpriteSheet() + drawIsoSpriteBitmap() — não há constante de largura aqui.
 */
object SpriteHeights {

    // -------------------------------------------------------------------------
    // GUILDA — estrutura central, sempre a maior entidade do mapa
    // -------------------------------------------------------------------------
    const val GUILD_LVL1 = 80f   // Casebre rústico (Box 52f + Pirâmide 28f)
    const val GUILD_LVL2 = 110f  // Fortaleza de pedra (Torre 82f + Pirâmide 28f)
    const val GUILD_LVL3 = 140f  // Castelo 3 torres (Torre 107f + Pirâmide 33f)

    // -------------------------------------------------------------------------
    // EDIFÍCIOS MODULARES — menores que a Guilda nível 1, maiores que heróis
    // -------------------------------------------------------------------------
    const val FERRARIA  = 44f   // Ferraria isométrica procedural
    const val MERCADOR  = 44f   // Mercador/Banca procedural
    const val TABERNA   = 44f   // Taberna procedural

    // -------------------------------------------------------------------------
    // HERÓIS — referência central da hierarquia
    // -------------------------------------------------------------------------
    const val HERO = 48f        // Todos os heróis jogáveis (Guerreiro, Maga, Arqueiro, Clériga)

    // -------------------------------------------------------------------------
    // MONSTROS — devem ser ≤ HERO
    // -------------------------------------------------------------------------
    const val ORC    = 44f      // Orc (igual ao Lobo por simetria visual)
    const val LOBO   = 44f      // Lobo
    const val GOBLIN = 40f      // Goblin (menor que herói)
    const val SLIME  = 28f      // Slime (menor criatura)

    // -------------------------------------------------------------------------
    // COLETAS (pseudo-monstros de missão) — visualmente objetos de terreno
    // -------------------------------------------------------------------------
    const val COLETA_MADEIRA = 60f   // Árvore grande
    const val COLETA_PEDRA   = 45f   // Rocha
    const val COLETA_ERVAS   = 35f   // Ervas rasteiras
    const val COLETA_FERRO   = 48f   // Minério (tamanho de herói para destacar)

    // -------------------------------------------------------------------------
    // DECORAÇÃO — teto de 1.3× o herói para qualquer elemento decorativo
    // -------------------------------------------------------------------------
    const val DECOR_TREE  = 52f     // Árvore decorativa
    const val DECOR_ROCK  = 44f     // Pedra decorativa
    val DECOR_MAX = HERO * 1.3f     // ~62.4f — teto para qualquer decoração nova
}
