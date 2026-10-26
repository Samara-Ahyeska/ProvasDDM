package com.example.prova3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class ResultadoChecagem(
    val vencedor: Jogador?,
    val indices: List<Int>
)

class ViewModelJogoDaVelha : ViewModel() {

    private val _estadoUi = MutableStateFlow(EstadoJogo())
    val estadoUi: StateFlow<EstadoJogo> = _estadoUi.asStateFlow()

    private val combinacoesVitoria = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
        listOf(0, 4, 8), listOf(2, 4, 6)
    )

    fun aoClicarNaCelula(indice: Int) {
        val estado = _estadoUi.value
        if (estado.jogoFinalizado || estado.iaPensando || estado.tabuleiro[indice] != null) return

        realizarJogada(indice)

        val estadoAtualizado = _estadoUi.value
        if (estadoAtualizado.modoJogo == ModoJogo.VS_IA && !estadoAtualizado.jogoFinalizado && estadoAtualizado.jogadorAtual != estadoAtualizado.simboloHumano) {
            executarJogadaIa()
        }
    }

    private fun realizarJogada(indice: Int) {
        _estadoUi.update { estado ->
            val novoTabuleiro = estado.tabuleiro.toMutableList().apply { set(indice, estado.jogadorAtual) }
            val resultado = checarVencedorComDetalhes(novoTabuleiro)
            val vencedor = resultado.vencedor
            val indicesVitoria = resultado.indices
            val ehEmpate = vencedor == null && novoTabuleiro.none { it == null }
            val jogoFinalizado = vencedor != null || ehEmpate

            val placarAtual = if (estado.modoJogo == ModoJogo.VS_IA) estado.placarVsAi else estado.placarPvp

            var novasVitoriasX = placarAtual.vitoriasX
            var novasVitoriasO = placarAtual.vitoriasOOrAi
            var novosEmpates = placarAtual.empates

            if (vencedor != null) {
                if (estado.modoJogo == ModoJogo.VS_IA) {
                    if (vencedor == estado.simboloHumano) {
                        novasVitoriasX++
                    } else {
                        novasVitoriasO++
                    }
                } else {
                    if (vencedor == Jogador.X) novasVitoriasX++
                    else if (vencedor == Jogador.O) novasVitoriasO++
                }
            } else if (ehEmpate) {
                novosEmpates++
            }

            val novoPlacar = PlacarModo(novasVitoriasX, novasVitoriasO, novosEmpates)

            estado.copy(
                tabuleiro = novoTabuleiro,
                jogadorAtual = if (estado.jogadorAtual == Jogador.X) Jogador.O else Jogador.X,
                vencedor = vencedor,
                indicesVitoria = indicesVitoria,
                ehEmpate = ehEmpate,
                jogoFinalizado = jogoFinalizado,
                placarVsAi = if (estado.modoJogo == ModoJogo.VS_IA) novoPlacar else estado.placarVsAi,
                placarPvp = if (estado.modoJogo == ModoJogo.PVP) novoPlacar else estado.placarPvp
            )
        }
    }

    private fun executarJogadaIa() {
        viewModelScope.launch {
            _estadoUi.update { it.copy(iaPensando = true) }
            delay(500)

            val estado = _estadoUi.value
            val melhorJogada = when (estado.dificuldade) {
                Dificuldade.FACIL -> obterJogadaIaFacil(estado.tabuleiro)
                Dificuldade.INTERMEDIARIO -> obterJogadaIaIntermediaria(estado.tabuleiro)
                Dificuldade.DIFICIL -> obterJogadaIaDificil(estado.tabuleiro)
            }

            _estadoUi.update { it.copy(iaPensando = false) }

            if (melhorJogada != -1 && !_estadoUi.value.jogoFinalizado) {
                realizarJogada(melhorJogada)
            }
        }
    }

    private fun obterJogadaIaFacil(tabuleiro: List<Jogador?>): Int {
        val indicesDisponiveis = tabuleiro.indices.filter { tabuleiro[it] == null }
        return if (indicesDisponiveis.isNotEmpty()) indicesDisponiveis[Random.nextInt(indicesDisponiveis.size)] else -1
    }

    private fun obterJogadaIaIntermediaria(tabuleiro: List<Jogador?>): Int {
        val simboloIa = if (_estadoUi.value.simboloHumano == Jogador.X) Jogador.O else Jogador.X
        val simboloHumano = _estadoUi.value.simboloHumano

        for (i in tabuleiro.indices) {
            if (tabuleiro[i] == null) {
                val temp = tabuleiro.toMutableList()
                temp[i] = simboloIa
                if (checarVencedorComDetalhes(temp).vencedor == simboloIa) return i
            }
        }
        for (i in tabuleiro.indices) {
            if (tabuleiro[i] == null) {
                val temp = tabuleiro.toMutableList()
                temp[i] = simboloHumano
                if (checarVencedorComDetalhes(temp).vencedor == simboloHumano) return i
            }
        }
        return obterJogadaIaFacil(tabuleiro)
    }

    private fun obterJogadaIaDificil(tabuleiro: List<Jogador?>): Int {
        val simboloIa = if (_estadoUi.value.simboloHumano == Jogador.X) Jogador.O else Jogador.X
        var melhorPontuacao = Int.MIN_VALUE
        var melhorJogada = -1

        for (i in tabuleiro.indices) {
            if (tabuleiro[i] == null) {
                val tabuleiroTemp = tabuleiro.toMutableList()
                tabuleiroTemp[i] = simboloIa
                val pontuacao = minimax(tabuleiroTemp, 0, false, simboloIa)
                if (pontuacao > melhorPontuacao) {
                    melhorPontuacao = pontuacao
                    melhorJogada = i
                }
            }
        }
        return melhorJogada
    }

    private fun minimax(tabuleiro: List<Jogador?>, profundidade: Int, ehMaximizando: Boolean, simboloIa: Jogador): Int {
        val simboloHumano = if (simboloIa == Jogador.X) Jogador.O else Jogador.X
        val vencedor = checarVencedorComDetalhes(tabuleiro).vencedor
        if (vencedor == simboloIa) return 10 - profundidade
        if (vencedor == simboloHumano) return profundidade - 10
        if (tabuleiro.none { it == null }) return 0

        return if (ehMaximizando) {
            var melhorPontuacao = Int.MIN_VALUE
            for (i in tabuleiro.indices) {
                if (tabuleiro[i] == null) {
                    val tabuleiroTemp = tabuleiro.toMutableList()
                    tabuleiroTemp[i] = simboloIa
                    val pontuacao = minimax(tabuleiroTemp, profundidade + 1, false, simboloIa)
                    melhorPontuacao = maxOf(pontuacao, melhorPontuacao)
                }
            }
            melhorPontuacao
        } else {
            var melhorPontuacao = Int.MAX_VALUE
            for (i in tabuleiro.indices) {
                if (tabuleiro[i] == null) {
                    val tabuleiroTemp = tabuleiro.toMutableList()
                    tabuleiroTemp[i] = simboloHumano
                    val pontuacao = minimax(tabuleiroTemp, profundidade + 1, true, simboloIa)
                    melhorPontuacao = minOf(pontuacao, melhorPontuacao)
                }
            }
            melhorPontuacao
        }
    }

    private fun checarVencedorComDetalhes(tabuleiro: List<Jogador?>): ResultadoChecagem {
        for (combo in combinacoesVitoria) {
            val (a, b, c) = combo
            if (tabuleiro[a] != null && tabuleiro[a] == tabuleiro[b] && tabuleiro[a] == tabuleiro[c]) {
                return ResultadoChecagem(tabuleiro[a], combo)
            }
        }
        return ResultadoChecagem(null, emptyList())
    }

    fun reiniciarTabuleiro() {
        _estadoUi.update { estado ->
            val novoSimboloHumano = if (estado.simboloHumano == Jogador.X) Jogador.O else Jogador.X
            val proximoInicial = novoSimboloHumano

            estado.copy(
                tabuleiro = List(9) { null },
                jogadorAtual = proximoInicial,
                simboloHumano = novoSimboloHumano,
                proximoSimboloInicial = proximoInicial,
                vencedor = null,
                indicesVitoria = emptyList(),
                ehEmpate = false,
                jogoFinalizado = false,
                iaPensando = false
            ).also { novoEstado ->
                if (novoEstado.modoJogo == ModoJogo.VS_IA && novoEstado.jogadorAtual != novoEstado.simboloHumano) {
                    executarJogadaIa()
                }
            }
        }
    }

    fun reiniciarJogoEPlacar() {
        _estadoUi.update {
            it.copy(
                tabuleiro = List(9) { null },
                jogadorAtual = Jogador.X,
                simboloHumano = Jogador.X,
                proximoSimboloInicial = Jogador.O,
                vencedor = null,
                indicesVitoria = emptyList(),
                ehEmpate = false,
                jogoFinalizado = false,
                iaPensando = false,
                placarVsAi = PlacarModo(),
                placarPvp = PlacarModo()
            )
        }
    }

    fun alterarModoJogo(modo: ModoJogo) {
        val atual = _estadoUi.value
        if (atual.modoJogo == modo) return


        if (!atual.jogoFinalizado && atual.tabuleiro.any { it != null }) {
            _estadoUi.update { it.copy(mostrarAlertaTroca = true) }
            return
        }

        _estadoUi.update {
            it.copy(
                modoJogo = modo,
                tabuleiro = List(9) { null },
                jogadorAtual = Jogador.X,
                simboloHumano = Jogador.X,
                vencedor = null,
                indicesVitoria = emptyList(),
                ehEmpate = false,
                jogoFinalizado = false,
                iaPensando = false
            )
        }
    }

    fun alterarDificuldade(dificuldade: Dificuldade) {
        val atual = _estadoUi.value
        if (atual.dificuldade == dificuldade) return

        if (!atual.jogoFinalizado && atual.tabuleiro.any { it != null }) {
            _estadoUi.update { it.copy(mostrarAlertaTroca = true) }
            return
        }

        _estadoUi.update {
            it.copy(dificuldade = dificuldade)
        }
    }

    fun fecharAlertaTroca() {
        _estadoUi.update { it.copy(mostrarAlertaTroca = false) }
    }
}