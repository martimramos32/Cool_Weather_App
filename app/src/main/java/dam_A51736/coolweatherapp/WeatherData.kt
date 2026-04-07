package dam_A51736.coolweatherapp

//Classe responsável por receber os dados principais da API
data class WeatherData(
    var latitude: String,
    var longitude: String,
    var timezone: String,
    var current_weather: CurrentWeather,
    var hourly: Hourly
)

//Classe que guarda os detalhes específicos do tempo atual recebido pela classe WeatherData
data class CurrentWeather(
    var windspeed: Float,
    var temperature: Float,
    var winddirection: Int,
    var weathercode: Int,
    var time: String
)

//Classe que permite armazenar a previsão do tempo hora a hora para o resto do dia
data class Hourly(
    var time: ArrayList<String>,
    var temperature_2m: ArrayList<Float>,
    var weathercode: ArrayList<Int>,
    var pressure_msl: ArrayList<Double>
)

//Lista que serve como um dicionário que liga o "code" ao nome exato da imagem que está guardada na diretoria drawable, para que mais tarde o ecrã saiba logo qual é a imagem correta a apresentar no display
enum class WMO_WeatherCode(var code: Int, var image: String) {
    CLEAR_SKY(0, "clear_day"),
    MAINLY_CLEAR(1, "mostly_clear_day"),
    PARTLY_CLOUDY(2, "partly_cloudy_day"),
    OVERCAST(3, "cloudy"),
    FOG(45, "fog"),
    DEPOSITING_RIME_FOG(48, "fog"),
    DRIZZLE_LIGHT(51, "drizzle"),
    DRIZZLE_MODERATE(53, "drizzle"),
    DRIZZLE_DENSE(55, "drizzle"),
    FREEZING_DRIZZLE_LIGHT(56, "freezing_drizzle"),
    FREEZING_DRIZZLE_DENSE(57, "freezing_drizzle"),
    RAIN_SLIGHT(61, "rain_light"),
    RAIN_MODERATE(63, "rain"),
    RAIN_HEAVY(65, "rain_heavy"),
    FREEZING_RAIN_LIGHT(66, "freezing_rain_light"),
    FREEZING_RAIN_HEAVY(67, "freezing_rain_heavy"),
    SNOW_FALL_SLIGHT(71, "snow_light"),
    SNOW_FALL_MODERATE(73, "snow"),
    SNOW_FALL_HEAVY(75, "snow_heavy"),
    SNOW_GRAINS(77, "snow"),
    RAIN_SHOWERS_SLIGHT(80, "rain_light"),
    RAIN_SHOWERS_MODERATE(81, "rain"),
    RAIN_SHOWERS_VIOLENT(82, "rain_heavy"),
    SNOW_SHOWERS_SLIGHT(85, "snow_light"),
    SNOW_SHOWERS_HEAVY(86, "snow_heavy"),
    THUNDERSTORM_SLIGHT_MODERATE(95, "tstorm"),
    THUNDERSTORM_HAIL_SLIGHT(96, "tstorm"),
    THUNDERSTORM_HAIL_HEAVY(99, "tstorm")
}


//Uma vez que, já existe o enumerado com as informações que receberemos da API é necessário que a aplicação saiba qual a imagem a utilizar para um determinado código, portanto precisamoas de mapear estes valores
fun getWeatherCodeMap(): Map<Int, WMO_WeatherCode> {
    val weatherMap = HashMap<Int, WMO_WeatherCode>() //Mapa que para uma determinada chave(code) retornará um determinado valor(imagem correspondente)
    //lê todas as linhas do enumerado
    WMO_WeatherCode.values().forEach {
        weatherMap.put(it.code, it) //Se procurar por um determinado código, retorna essa mesmo linha completa do enumerado
    }
    return weatherMap //retorna um mapa completo com todas as correspondencias necessárias para todos os codigos recebidos da API
}