package com.assesment.weatherfinal

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WeatherForeCast : AppCompatActivity() {
    private lateinit var txtLocation: TextView
    private lateinit var txtDate1: TextView
    private lateinit var textDescription1: TextView
    private lateinit var imageView1: ImageView
    private lateinit var txtDate2: TextView
    private lateinit var textDescription4: TextView
    private lateinit var imageView4: ImageView
    private lateinit var textDescription2: TextView
    private lateinit var imageView2: ImageView
    private lateinit var txtDate3: TextView
    private lateinit var textDescription3: TextView
    private lateinit var imageView3: ImageView
    private lateinit var txtDate4: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var searchBar: EditText


    private val apiKey = "d4717c0542a1ed5f4673838cc6182b12"


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1002
    }

    private var isCurrentLocationForecast: Boolean = true

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_fore_cast)



        txtLocation = findViewById(R.id.txt_location)
        txtDate1 = findViewById(R.id.txtDate1)
        textDescription1 = findViewById(R.id.textDescription1)
        imageView1 = findViewById(R.id.imageView1)
        txtDate2 = findViewById(R.id.textDate2)
        textDescription2 = findViewById(R.id.textDescription2)
        imageView2 = findViewById(R.id.imageView2)
        txtDate3 = findViewById(R.id.textDate3)
        textDescription3 = findViewById(R.id.textDescription3)
        imageView3 = findViewById(R.id.imageView3)
        txtDate4 = findViewById(R.id.textDate4)
        textDescription4 = findViewById(R.id.textDescription4)
        imageView4 = findViewById(R.id.imageView4)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        searchBar = findViewById(R.id.searchbar)

        searchBar.setOnTouchListener { _, event ->
            val DRAWABLE_RIGHT = 2

            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (searchBar.right - searchBar.compoundDrawables[DRAWABLE_RIGHT].bounds.width())) {
                    onSearchButtonClick()
                    return@setOnTouchListener true
                }
            }
            false
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        getCurrentLocation()
    }


    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val forecastCityName = getCityName(location.latitude, location.longitude)
                        txtLocation.text = forecastCityName
                        getForecastWeatherData(location.latitude, location.longitude)
                    } else {
                        txtLocation.text = "Unknown Location"
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error Getiing Location: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun getForecastWeatherData(latitude: Double, longitude: Double) {
        val apiUrl =
            "https://api.openweathermap.org/data/2.5/forecast?lat=$latitude&lon=$longitude&appid=$apiKey"

        val request = JsonObjectRequest(
            Request.Method.GET, apiUrl, null,
            { data ->
                try {
                    if (isCurrentLocationForecast) {
                        parseForecastData(data)
                    } else {
                        parseCityForecastData(data)
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Error Parsing Forecast Details",
                        Toast.LENGTH_SHORT
                    ).show()
                    e.printStackTrace()
                }
            },
            { error ->
                Toast.makeText(
                    this,
                    "Error Loading Forecast Details",
                    Toast.LENGTH_SHORT
                ).show()
                error.printStackTrace()
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun onSearchButtonClick() {
        val cityName = searchBar.text.toString()
        if (cityName.isNotEmpty()) {
            isCurrentLocationForecast = false
            getForecastForCity(cityName)
        } else {
            isCurrentLocationForecast = true
            getCurrentLocation()
        }
    }

    private fun getForecastForCity(cityName: String) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocationName(cityName, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val location = addresses[0]
                val latitude = location.latitude
                val longitude = location.longitude
                txtLocation.text = location.locality ?: location.adminArea ?: "Unknown"
                getForecastWeatherData(latitude, longitude)
            } else {
                Toast.makeText(this, "City Not Found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun parseForecastData(data: JSONObject) {
        val forecastList = data.getJSONArray("list")
        val currentDate = Calendar.getInstance()

        for (i in 1 until 5) {
            val forecastItem = forecastList.getJSONObject(i * 8)
            val main = forecastItem.getJSONObject("main")
            val temperature = main.getDouble("temp")
            val weatherArray = forecastItem.getJSONArray("weather")
            val weather = weatherArray.getJSONObject(0).getString("description").toUpperCase(Locale.getDefault())
            val icon = weatherArray.getJSONObject(0).getString("icon")

            val forecastDate = currentDate.clone() as Calendar
            forecastDate.add(Calendar.DAY_OF_MONTH, i)

            val date = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(forecastDate.time)

            when (i) {
                1 -> {
                    txtDate1.text = "$date: ${temperature/10}°C"
                    textDescription1.text = weather
                    loadWeatherIcon(icon, imageView1)
                }
                2 -> {
                    txtDate2.text = "$date: ${temperature/10}°C"
                    textDescription2.text = weather
                    loadWeatherIcon(icon, imageView2)
                }
                3 -> {
                    txtDate3.text = "$date: ${temperature/10}°C"
                    textDescription3.text = weather
                    loadWeatherIcon(icon, imageView3)
                }
                4 -> {
                    txtDate4.text = "$date: ${temperature/10}°C"
                    textDescription4.text = weather
                    loadWeatherIcon(icon, imageView4)
                }
            }
        }
    }


    private fun parseCityForecastData(data: JSONObject) {
        try {
            val forecastList = data.getJSONArray("list")

            val currentDate = Calendar.getInstance()

            for (i in 0 until 3) {
                val forecastItem = forecastList.getJSONObject(i * 8)
                val main = forecastItem.getJSONObject("main")
                val temperature = main.getDouble("temp")
                val weatherArray = forecastItem.getJSONArray("weather")
                val weather = weatherArray.getJSONObject(0).getString("description").toUpperCase(
                    Locale.getDefault())
                val icon = weatherArray.getJSONObject(0).getString("icon")

                val forecastDate = currentDate.clone() as Calendar
                forecastDate.add(Calendar.DAY_OF_MONTH, i + 1)

                val date = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(forecastDate.time)

                when (i) {
                    0 -> {
                        txtDate1.text = "$date: ${temperature/10}°C"
                        textDescription1.text = weather
                        loadWeatherIcon(icon, imageView1)
                    }
                    1 -> {
                        txtDate2.text = "$date: ${temperature/10}°C"
                        textDescription2.text = weather
                        loadWeatherIcon(icon, imageView2)
                    }
                    2 -> {
                        txtDate3.text = "$date: ${temperature/10}°C"
                        textDescription3.text = weather
                        loadWeatherIcon(icon, imageView3)
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error parsing forecast information",
                Toast.LENGTH_SHORT
            ).show()
            e.printStackTrace()
        }
    }

    private fun loadWeatherIcon(icon: String, imageView: ImageView) {
        val iconUrl = "https://openweathermap.org/img/w/$icon.png"
        Picasso.get().load(iconUrl).into(imageView)
    }

    private fun getCityName(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val cityName = addresses[0]?.locality
                    return cityName ?: "Unknown"
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return "Unknown"
    }
}