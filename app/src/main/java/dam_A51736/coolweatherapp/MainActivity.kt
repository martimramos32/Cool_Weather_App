package dam_A51736.coolweatherapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.net.URL
import java.io.InputStreamReader
import com.google.gson.Gson
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
class MainActivity : AppCompatActivity() {
    var day = true // Define se é dia ou noite (true = dia, false = noite)

    private lateinit var gps: FusedLocationProviderClient //esta variavel recebe um contrato lateinit, que significa que vai ser inicializada depois mais à frente no código. A abordagem do uso do campo NULL também seria possivel mas é menos eficiente pois iria exigir o uso do "?" todas as vezes ao usar esta variavel.

    @SuppressLint("DiscouragedApi")

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

        // Recebe os elementos da interface que serão alterados
        val btnUpdate = findViewById<Button>(R.id.buttonUpdate) // Botão para atualizar
        val editLat = findViewById<EditText>(R.id.editLatitude) // Caixa de texto para inserir a latitude
        val editLon = findViewById<EditText>(R.id.editLongitude) // Caixa de texto para inserir a longitude

        btnUpdate.setOnClickListener {
            // Recolhe o texto que está nas caixas e transforma em float, pois a funçao fetchWeatherData recebe paramentros do tipo float
            val lat = editLat.text.toString().toFloat()
            val lon = editLon.text.toString().toFloat()

            // Chama a função que faz a chamada à API e inicia-a, pois se trata de uma thread
            fetchWeatherData(lat, lon).start()
        }

        // Prepara a ferramenta de GPS
        gps = LocationServices.getFusedLocationProviderClient(this)
        // Arranca a função para descobrir onde estamos
        obterLocalizacao()
    }

    private fun WeatherAPI_Call(lat: Float, long: Float): WeatherData {
        // Prepara o endereço URL com as coordenadas escolhidas
        val reqString = buildString {
            append("https://api.open-meteo.com/v1/forecast?")
            append("latitude=${lat}&longitude=${long}&")
            append("current_weather=true&")
            append("timezone=auto&") //Permite que a API retorne a hora correta das coordenadas inseridas
            append("hourly=temperature_2m,weathercode,pressure_msl,windspeed_10m")
        }

        // Transforma o texto num endereço web
        val url = URL(reqString.toString())

        // Abre a ligação, lê o texto que a internet devolve e usa o Gson para o transformar nos dados que precisamos (WeatherData)
        url.openStream().use {
            val request = Gson().fromJson(InputStreamReader(it, "UTF-8"), WeatherData::class.java)
            return request
        }
    }

    private fun fetchWeatherData(lat: Float, long: Float) : Thread {
        return Thread {
            val weather = WeatherAPI_Call(lat,long)
            updateUI(weather)
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun updateUI(request: WeatherData) {
        runOnUiThread {
            // Preencher os campos com os valores recebidos da chamada à AP
            val weatherImage: ImageView = findViewById(R.id.weatherImage)
            val pressure: TextView = findViewById(R.id.pressureValue)
            val tempValue: TextView = findViewById(R.id.temperatureValue)
            val windSpeedValue: TextView = findViewById(R.id.windSpeedValue)
            val windDirValue: TextView = findViewById(R.id.windDirValue)
            val timeValue: TextView = findViewById(R.id.timeValue)

            pressure.text = request.hourly.pressure_msl.get(12).toString() + " hPa"
            tempValue.text = "${request.current_weather.temperature} °C"
            windSpeedValue.text = "${request.current_weather.windspeed} km/h"
            windDirValue.text = "${request.current_weather.winddirection}º"
            timeValue.text = request.current_weather.time.replace("T", " ")

            val mapt = getWeatherCodeMap() //Criamos o mapa que contém as informações das determinadas correspondencias (code -> imagem)
            val wCode = mapt.get(request.current_weather.weathercode) //recebe o determinado codigo

            val wImage = when (wCode) { //quando um determinado codigo é recebido precisamos de apresentar no display a imagem correta para esse mesmo codigo
                //Se for um destes 3 possiveis estados de tempo, entra no bloco seguinte e define a imagem
                WMO_WeatherCode.CLEAR_SKY,
                WMO_WeatherCode.MAINLY_CLEAR,
                WMO_WeatherCode.PARTLY_CLOUDY -> {
                    //queremos apenas o prefixo do nome da imagem correspondente ao codigo portanto queremos remover a parte "_day" ou "_night"
                    val base = wCode.image.replace("_day", "").replace("_night", "")
                    //Se a palavra for (clear) adiciona-lhe a palavra "_day" no fim caso contrário adiciona "_night"
                    if (day) base + "_day" else base + "_night"
                }
                else -> wCode?.image //se nao for nenhum destes 3 estados, como, por exemplo, nevar, chover ou nevoeiro, apenas retorna a imagem correspondente ao codigo fornecido pela API
            }

            val res = getResources()
            // Imagem de segurança caso a conexão à internet falhe
            weatherImage.setImageResource(R.drawable.fog)

            // Carregar a imagem final correta
            val resID = res.getIdentifier(wImage, "drawable", packageName)
            if (resID != 0) { // Só aplica se a imagem existir mesmo na pasta drawable
                val drawable = this.getDrawable(resID)
                weatherImage.setImageDrawable(drawable)
            }
        }
    }

    private fun obterLocalizacao() {
        // 1. Verifica se o utilizador já deu permissão
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Se não deu, faz aparecer a janela no ecrã a pedir
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        // 2. Se já tem permissão, vai buscar a última localização conhecida
        gps.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                // Descobriu onde estamos!
                val lat = location.latitude.toFloat()
                val lon = location.longitude.toFloat()

                // Atualiza os números nas caixas de texto do ecrã
                findViewById<EditText>(R.id.editLatitude).setText(lat.toString())
                findViewById<EditText>(R.id.editLongitude).setText(lon.toString())

                // Pede a meteorologia para este novo local
                fetchWeatherData(lat, lon).start()
            } else {
                // Se o GPS do telemóvel estiver desligado, usa Lisboa por defeito para não ficar vazio
                fetchWeatherData(38.76f, -9.12f).start()
            }
        }
    }
}