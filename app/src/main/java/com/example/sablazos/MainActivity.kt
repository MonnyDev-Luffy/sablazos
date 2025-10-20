package com.example.sablazos

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sablazos.ui.theme.AcelerometroTheme
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DueloCelularUI(this)
        }
    }
}

@Composable
fun DueloCelularUI(contexto: Context) {
    val sensorManager = remember {
        contexto.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    val acelerometro = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    var x by remember { mutableStateOf(0f) }
    var y by remember { mutableStateOf(0f) }
    var z by remember { mutableStateOf(0f) }

    var colorFondo by remember { mutableStateOf(Color.Black) }
    var mostrarMensaje by remember { mutableStateOf(true) }
    var mostrarSable by remember { mutableStateOf(false) }
    var tiempoUltimoGolpe by remember { mutableStateOf(System.currentTimeMillis()) }

    val mediaPlayer = remember {
        MediaPlayer.create(contexto, R.raw.sable)
    }

    val listener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    x = it.values[0]
                    y = it.values[1]
                    z = it.values[2]

                    val fuerza = sqrt(x * x + y * y + z * z)

                    if (fuerza > 18) {
                        // Sablazo detectado
                        colorFondo = Color(
                            red = (80..255).random(),
                            green = (80..255).random(),
                            blue = (80..255).random()
                        )
                        mediaPlayer.start()

                        mostrarSable = true
                        mostrarMensaje = false

                        tiempoUltimoGolpe = System.currentTimeMillis()
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    DisposableEffect(Unit) {
        sensorManager.registerListener(
            listener,
            acelerometro,
            SensorManager.SENSOR_DELAY_GAME
        )
        onDispose {
            sensorManager.unregisterListener(listener)
            mediaPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFondo),
        contentAlignment = Alignment.Center
    ) {
        if (mostrarSable) {
            Image(
                painter = painterResource(id = R.drawable.sable),
                contentDescription = "Sablazo activado",
                modifier = Modifier
                    .size(900.dp)
                    .align(Alignment.Center)
            )
        } else if (mostrarMensaje) {
            Text(
                text = "¡Sube el volumen y sacúdeme!",
                color = Color.White,
                fontSize = 25.sp
            )
        }
    }

    // Inactividad
    LaunchedEffect(tiempoUltimoGolpe) {
        while (true) {
            val ahora = System.currentTimeMillis()
            if (ahora - tiempoUltimoGolpe > 10_000) {
                // Pasaron 20 segundos sin agitar el celular
                mostrarMensaje = true
                mostrarSable = false
                colorFondo = Color.Black
            }
            kotlinx.coroutines.delay(1000)
        }
    }
}
