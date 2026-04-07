package dam_A51736.coolweatherapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.net.URL
import java.io.InputStreamReader
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {
    // Define se é dia ou noite manualmente
    var day = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // A escolha do tema tem de acontecer ANTES das instruções super.onCreate e setContentView
        when (resources.configuration.orientation) {
            android.content.res.Configuration.ORIENTATION_PORTRAIT -> {
                if (day) {
                    setTheme(R.style.Theme_Day)
                } else {
                    setTheme(R.style.Theme_Night)
                }
            }
            android.content.res.Configuration.ORIENTATION_LANDSCAPE -> {
                if (day) {
                    setTheme(R.style.Theme_Day_Land)
                } else {
                    setTheme(R.style.Theme_Night_Land)
                }
            }
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun WeatherAPI_Call(lat: Float, long: Float): WeatherData {
        // Prepara o endereço URL com as coordenadas escolhidas
        val reqString = buildString {
            append("https://api.open-meteo.com/v1/forecast?")
            append("latitude=${lat}&longitude=${long}&")
            append("current_weather=true&")
            append("hourly=temperature_2m,weathercode,pressure_msl,windspeed_10m")
        }

        // Transforma o texto num endereço web "real"
        val url = URL(reqString.toString())

        // Abre a ligação, lê o texto que a internet devolve e usa o Gson para o transformar nos dados que precisamos (WeatherData)
        url.openStream().use {
            val request = Gson().fromJson(InputStreamReader(it, "UTF-8"), WeatherData::class.java)
            return request
        }
    }
}