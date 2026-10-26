package com.example.prova3

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaJogoDaVelha(viewModel: ViewModelJogoDaVelha = viewModel()) {
    val estado by viewModel.estadoUi.collectAsState()

    var mostrarDialogoReiniciar by remember { mutableStateOf(false) }
    var mostrarDialogoZerar by remember { mutableStateOf(false) }

    var exibirRobo by remember { mutableStateOf(false) }

    val iaVenceu = estado.modoJogo == ModoJogo.VS_IA &&
            estado.vencedor != null &&
            estado.vencedor != estado.simboloHumano

    LaunchedEffect(estado.vencedor) {
        if (iaVenceu) {
            exibirRobo = true
            delay(3000L)
            exibirRobo = false
        } else {
            exibirRobo = false
        }
    }

    if (estado.mostrarAlertaTroca) {
        AlertDialog(
            onDismissRequest = { viewModel.fecharAlertaTroca() },
            title = { Text("Partida em Andamento") },
            text = { Text("Termine a partida atual (com um vencedor ou empate) antes de alterar o modo ou a dificuldade.") },
            confirmButton = {
                TextButton(onClick = { viewModel.fecharAlertaTroca() }) {
                    Text("OK", color = Color(0xFF00B0FF))
                }
            },
            containerColor = Color(0xFF1A1A1A),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }

    if (mostrarDialogoReiniciar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoReiniciar = false },
            title = { Text("Reiniciar Partida") },
            text = { Text("Deseja realmente reiniciar a partida atual? O placar será mantido.") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoReiniciar = false
                    viewModel.reiniciarTabuleiro()
                }) {
                    Text("Sim", color = Color(0xFF00B0FF))
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoReiniciar = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1A1A1A),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }

    if (mostrarDialogoZerar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoZerar = false },
            title = { Text("Zerar Placar") },
            text = { Text("Deseja realmente apagar todo o placar e histórico? Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoZerar = false
                    viewModel.reiniciarJogoEPlacar()
                }) {
                    Text("Zerar", color = Color(0xFF00BFA5))
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoZerar = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1A1A1A),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Jogo da Velha IA",
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black
                    )
                )
            },
            containerColor = Color.Black
        ) { paddingInterno ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(paddingInterno)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                ControlesJogo(estado, viewModel)
                PainelPlacar(estado)

                val historicoOutroModo: HistoricoAnterior? = if (estado.modoJogo == ModoJogo.VS_IA) {
                    if (estado.placarPvp.vitoriasX > 0 || estado.placarPvp.vitoriasOOrAi > 0 || estado.placarPvp.empates > 0) {
                        HistoricoAnterior(
                            vitoriasJogador1 = estado.placarPvp.vitoriasX,
                            vitoriasJogador2OuIa = estado.placarPvp.vitoriasOOrAi,
                            empates = estado.placarPvp.empates,
                            nomeModo = "Modo Player vs Player"
                        )
                    } else null
                } else {
                    if (estado.placarVsAi.vitoriasX > 0 || estado.placarVsAi.vitoriasOOrAi > 0 || estado.placarVsAi.empates > 0) {
                        HistoricoAnterior(
                            vitoriasJogador1 = estado.placarVsAi.vitoriasX,
                            vitoriasJogador2OuIa = estado.placarVsAi.vitoriasOOrAi,
                            empates = estado.placarVsAi.empates,
                            nomeModo = "Modo vs I.A."
                        )
                    } else null
                }

                historicoOutroModo?.let { historico ->
                    CartaoHistorico(historico)
                }

                PainelStatus(estado)

                GradeTabuleiro(
                    tabuleiro = estado.tabuleiro,
                    indicesVitoria = estado.indicesVitoria,
                    jogoFinalizado = estado.jogoFinalizado || estado.iaPensando,
                    aoClicarNaCelula = { indice -> viewModel.aoClicarNaCelula(indice) }
                )

                BotoesAcao(
                    aoReiniciarPartida = { mostrarDialogoReiniciar = true },
                    aoZerarTudo = { mostrarDialogoZerar = true }
                )
            }
        }

        EfeitoVitoriaComRobo(
            vencedor = estado.vencedor,
            mostrarRobo = exibirRobo
        )
    }
}

data class Particula(
    val x: Float,
    val yInicio: Float,
    val velocidadeY: Float,
    val velocidadeX: Float,
    val cor: Color,
    val tamanho: Float
)

@Composable
fun EfeitoVitoriaComRobo(vencedor: Jogador?, mostrarRobo: Boolean) {
    if (vencedor == null) return

    val cores = listOf(
        Color(0xFF00E676),
        Color(0xFFFF3D00),
        Color(0xFFFFD700),
        Color(0xFF00B0FF),
        Color(0xFF8E24AA)
    )

    val particulas = remember(vencedor) {
        List(60) {
            Particula(
                x = Random.nextFloat(),
                yInicio = Random.nextFloat() * -0.4f,
                velocidadeY = Random.nextFloat() * 0.012f + 0.004f,
                velocidadeX = (Random.nextFloat() - 0.5f) * 0.003f,
                cor = cores.random(),
                tamanho = Random.nextFloat() * 10f + 6f
            )
        }
    }

    val animacaoProgresso = remember { Animatable(0f) }

    LaunchedEffect(vencedor) {
        animacaoProgresso.snapTo(0f)
        animacaoProgresso.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3500, easing = LinearEasing)
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val progresso = animacaoProgresso.value

        if (progresso < 1f) {
            particulas.forEach { p ->
                val yCorrente = (p.yInicio + progresso * (1.3f + p.velocidadeY * 40)) * size.height
                val xCorrente = (p.x + progresso * p.velocidadeX * 15) * size.width

                if (yCorrente < size.height) {
                    drawCircle(
                        color = p.cor,
                        radius = p.tamanho,
                        center = Offset(xCorrente, yCorrente)
                    )
                }
            }
        }

        if (mostrarRobo) {
            val centroX = size.width / 2f
            val centroY = size.height / 2f
            desenharRoboGigante(centroX, centroY)
        }
    }
}

private fun DrawScope.desenharRoboGigante(x: Float, y: Float) {
    val corMetal = Color(0xFF90A4AE)
    val corMetalEscuro = Color(0xFF546E7A)
    val corOlhos = Color(0xFF00E676)
    val corAntena = Color(0xFFFF5722)
    val corLuz = Color(0xFFFFD700)

    drawLine(
        color = corAntena,
        start = Offset(x, y - 130f),
        end = Offset(x, y - 80f),
        strokeWidth = 12f
    )
    drawCircle(
        color = corAntena,
        radius = 20f,
        center = Offset(x, y - 140f)
    )

    drawRect(
        color = corMetal,
        topLeft = Offset(x - 90f, y - 80f),
        size = Size(180f, 130f)
    )
    drawRect(
        color = corMetalEscuro,
        topLeft = Offset(x - 90f, y - 80f),
        size = Size(180f, 130f),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
    )

    drawRect(
        color = Color.Black,
        topLeft = Offset(x - 70f, y - 50f),
        size = Size(140f, 55f)
    )
    drawCircle(
        color = corOlhos,
        radius = 16f,
        center = Offset(x - 35f, y - 22f)
    )
    drawCircle(
        color = corOlhos,
        radius = 16f,
        center = Offset(x + 35f, y - 22f)
    )

    drawRect(
        color = corMetal,
        topLeft = Offset(x - 110f, y + 55f),
        size = Size(220f, 160f)
    )
    drawRect(
        color = corMetalEscuro,
        topLeft = Offset(x - 110f, y + 55f),
        size = Size(220f, 160f),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
    )

    drawCircle(
        color = corLuz,
        radius = 22f,
        center = Offset(x, y + 135f)
    )
    drawCircle(
        color = Color.Black,
        radius = 22f,
        center = Offset(x, y + 135f),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
    )

    drawRect(
        color = corMetalEscuro,
        topLeft = Offset(x - 145f, y + 70f),
        size = Size(30f, 90f)
    )
    drawRect(
        color = corMetalEscuro,
        topLeft = Offset(x + 115f, y + 70f),
        size = Size(30f, 90f)
    )
}

@Composable
fun ControlesJogo(estado: EstadoJogo, viewModel: ViewModelJogoDaVelha) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1A1A1A))
                .padding(4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            TabButtonPersonalizado(
                text = "Contra I.A.",
                selected = estado.modoJogo == ModoJogo.VS_IA,
                activeColor = Color(0xFF8E24AA),
                textColor = Color.White,
                onClick = { viewModel.alterarModoJogo(ModoJogo.VS_IA) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            TabButtonPersonalizado(
                text = "2 Jogadores",
                selected = estado.modoJogo == ModoJogo.PVP,
                activeColor = Color(0xFFFFD54F),
                textColor = Color.Black,
                onClick = { viewModel.alterarModoJogo(ModoJogo.PVP) },
                modifier = Modifier.weight(1f)
            )
        }

        AnimatedVisibility(visible = estado.modoJogo == ModoJogo.VS_IA) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Dificuldade:",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = estado.dificuldade == Dificuldade.FACIL,
                        onClick = { viewModel.alterarDificuldade(Dificuldade.FACIL) },
                        label = { Text("Fácil") },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    FilterChip(
                        selected = estado.dificuldade == Dificuldade.INTERMEDIARIO,
                        onClick = { viewModel.alterarDificuldade(Dificuldade.INTERMEDIARIO) },
                        label = { Text("Intermediário") },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    FilterChip(
                        selected = estado.dificuldade == Dificuldade.DIFICIL,
                        onClick = { viewModel.alterarDificuldade(Dificuldade.DIFICIL) },
                        label = { Text("Difícil") }
                    )
                }
            }
        }
    }
}

@Composable
fun TabButtonPersonalizado(
    text: String,
    selected: Boolean,
    activeColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) activeColor else Color.Transparent
    val finalTextColor = if (selected) textColor else Color.Gray

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = finalTextColor
        )
    }
}

@Composable
fun PainelPlacar(estado: EstadoJogo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val rotuloP2 = if (estado.modoJogo == ModoJogo.VS_IA) "I.A. (O)" else "Jogador O"

            ColunaPlacar(rotulo = "Jogador X", pontuacao = estado.vitoriasJogadorX, corDestaque = Color(0xFF00E676))
            VerticalDivider(modifier = Modifier.height(30.dp), color = Color.DarkGray)
            ColunaPlacar(rotulo = "Velha", pontuacao = estado.empates, corDestaque = Color.LightGray)
            VerticalDivider(modifier = Modifier.height(30.dp), color = Color.DarkGray)
            ColunaPlacar(rotulo = rotuloP2, pontuacao = estado.vitoriasJogadorO, corDestaque = Color(0xFFFF5722))
        }
    }
}

@Composable
fun ColunaPlacar(rotulo: String, pontuacao: Int, corDestaque: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = rotulo.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = pontuacao.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = corDestaque
        )
    }
}

@Composable
fun CartaoHistorico(historico: HistoricoAnterior) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = historico.nomeModo,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Player: ${historico.vitoriasJogador1} • Velha: ${historico.empates} • I.A.: ${historico.vitoriasJogador2OuIa}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray
            )
        }
    }
}

@Composable
fun PainelStatus(estado: EstadoJogo) {
    val textoStatus = when {
        estado.iaPensando -> "A I.A. está pensando..."
        estado.vencedor != null -> {
            if (estado.modoJogo == ModoJogo.VS_IA) {
                if (estado.vencedor == estado.simboloHumano) "🎉 Você Venceu!" else "🤖 A I.A. Venceu!"
            } else {
                "🏆 Jogador ${estado.vencedor.simbolo} Venceu!"
            }
        }
        estado.ehEmpate -> "🤝 Deu Velha! (Empate)"
        else -> {
            if (estado.modoJogo == ModoJogo.VS_IA) {
                if (estado.jogadorAtual == estado.simboloHumano) "Sua Vez (${estado.simboloHumano.simbolo})" else "Vez da I.A."
            } else {
                "Vez do Jogador ${estado.jogadorAtual.simbolo}"
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(50.dp),
        color = Color(0xFF1A1A1A),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = textoStatus,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            color = Color.White
        )
    }
}

@Composable
fun GradeTabuleiro(
    tabuleiro: List<Jogador?>,
    indicesVitoria: List<Int>,
    jogoFinalizado: Boolean,
    aoClicarNaCelula: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .size(320.dp)
            .aspectRatio(1f)
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF2828F5))
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (linha in 0..2) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (coluna in 0..2) {
                        val indice = linha * 3 + coluna
                        val estaNaVitoria = indicesVitoria.contains(indice)
                        ComponenteCelula(
                            simbolo = tabuleiro[indice]?.simbolo ?: "",
                            habilitado = !jogoFinalizado && tabuleiro[indice] == null,
                            estaNaVitoria = estaNaVitoria,
                            aoClicar = { aoClicarNaCelula(indice) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ComponenteCelula(
    simbolo: String,
    habilitado: Boolean,
    estaNaVitoria: Boolean,
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val corFundo = if (estaNaVitoria) Color(0xFFFFD700) else Color(0xFF03A9F4)
    val corTexto = if (estaNaVitoria) Color.Black else when (simbolo) {
        "X" -> Color(0xFF00E676)
        "O" -> Color(0xFFFF3D00)
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(corFundo)
            .clickable(enabled = habilitado, onClick = aoClicar),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = simbolo,
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            color = corTexto
        )
    }
}

@Composable
fun BotoesAcao(
    aoReiniciarPartida: () -> Unit,
    aoZerarTudo: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = aoReiniciarPartida,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B0FF)),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                "Reiniciar",
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Button(
            onClick = aoZerarTudo,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA5)),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                "Zerar Placar",
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}