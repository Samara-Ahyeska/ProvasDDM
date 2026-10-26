package com.example.prova3

enum class Jogador(val simbolo: String) {
    X("X"),
    O("O")
}

enum class ModoJogo {
    PVP,
    VS_IA
}

enum class Dificuldade {
    FACIL,
    INTERMEDIARIO,
    DIFICIL
}

data class PlacarModo(
    val vitoriasX: Int = 0,
    val vitoriasOOrAi: Int = 0,
    val empates: Int = 0
)

data class HistoricoAnterior(
    val vitoriasJogador1: Int = 0,
    val vitoriasJogador2OuIa: Int = 0,
    val empates: Int = 0,
    val nomeModo: String = ""
)

data class EstadoJogo(
    val tabuleiro: List<Jogador?> = List(9) { null },
    val jogadorAtual: Jogador = Jogador.X,
    val simboloHumano: Jogador = Jogador.X,
    val proximoSimboloInicial: Jogador = Jogador.O,
    val vencedor: Jogador? = null,
    val indicesVitoria: List<Int> = emptyList(),
    val ehEmpate: Boolean = false,
    val jogoFinalizado: Boolean = false,
    val iaPensando: Boolean = false,
    val modoJogo: ModoJogo = ModoJogo.VS_IA,
    val dificuldade: Dificuldade = Dificuldade.DIFICIL,
    val mostrarAlertaTroca: Boolean = false,

    val placarVsAi: PlacarModo = PlacarModo(),
    val placarPvp: PlacarModo = PlacarModo()
) {
    val vitoriasJogadorX: Int
        get() = if (modoJogo == ModoJogo.VS_IA) placarVsAi.vitoriasX else placarPvp.vitoriasX

    val vitoriasJogadorO: Int
        get() = if (modoJogo == ModoJogo.VS_IA) placarVsAi.vitoriasOOrAi else placarPvp.vitoriasOOrAi

    val empates: Int
        get() = if (modoJogo == ModoJogo.VS_IA) placarVsAi.empates else placarPvp.empates
}