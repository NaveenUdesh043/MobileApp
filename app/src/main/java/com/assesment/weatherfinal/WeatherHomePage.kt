package com.assesment.weatherfinal

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WeatherHomePage : AppCompatActivity() {
    private lateinit var iddateandtime: TextView
    private lateinit var txtCountry: TextView
    private lateinit var imgWeatherImg: ImageView
    private lateinit var txtPressureDetails: TextView
    private lateinit var txtHumidityDetails: TextView
    private lateinit var txtTempDetails: TextView
    private lateinit var txtWeatherDetails: TextView
    private lateinit var txtCelcius2: TextView
    private lateinit var txtDescription: TextView

    private lateinit var fusedLocationClient: FusedLocationProviderClient


    private val apiKey = "d4717c0542a1ed5f4673838cc6182b12"


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    @SuppressLint("ClickableViewAccessibility", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_home_page)

        val button: Button = findViewById(R.id.idbtn)

        button.setOnClickListener(View.OnClickListener {

            val intent = Intent(this@WeatherHomePage, WeatherForeCast::class.java)

            startActivity(intent)
        })
        iddateandtime = findViewById(R.id.iddateandtimetxt)
        txtCountry = findViewById(R.id.idcountry)
        txtCelcius2 = findViewById(R.id.txtcelcius2)
        txtHumidityDetails = findViewById(R.id.idhumiditydetails)
        txtTempDetails = findViewById(R.id.idtempdetails)
        txtWeatherDetails = findViewById(R.id.idairspeeddetails)
        txtDescription = findViewById(R.id.iddescriptiontxt)
        imgWeatherImg = findViewById(R.id.idweatherimg)
        txtPressureDetails = findViewById(R.id.idpressuredetails)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getCurrentDateTime()

        getCurrentLocation()

        val searchBar = findViewById<EditText>(R.id.searchbar)
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
    }


    private fun onSearchButtonClick() {
        val cityName = findViewById<EditText>(R.id.searchbar).text.toString()
        if (cityName.isNotEmpty()) {
            getWeatherForCity(cityName)
        } else {
            Toast.makeText(this, "Please Enter Your City", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentDateTime() {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("MMMM dd (EEE) | hh:mm a", Locale.getDefault())
        val formattedDate = sdf.format(calendar.time)

        iddateandtime.text = formattedDate
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
                        getWeatherData(location.latitude, location.longitude)
                    } else {

                        txtCountry.text = "Location: Unknown"
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error Getting Location: ${e.message}",
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

    private fun getWeatherData(latitude: Double, longitude: Double) {
        val apiUrl =
            "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&appid=$apiKey"
        val request = JsonObjectRequest(
            Request.Method.GET, apiUrl, null,
            { data ->
                try {
                    val cityName = data.getString("name")
                    val temperature =String.format( "%.2f",data.getJSONObject("main").getDouble("temp") - 273.15).toDouble()
                    val pressure = data.getJSONObject("main").getDouble("pressure")
                    val humidity = data.getJSONObject("main").getDouble("humidity")
                    val windSpeed = data.getJSONObject("wind").getDouble("speed")
                    val weatherArray = data.getJSONArray("weather")
                    val description = if (weatherArray.length() > 0) {
                        weatherArray.getJSONObject(0).getString("description")
                    } else {
                        ""
                    }
                    val iconCode = if (weatherArray.length() > 0) {
                        weatherArray.getJSONObject(0).getString("icon")
                    } else {
                        ""
                    }

                    txtCountry.text = cityName
                    txtCelcius2.text = "${temperature}°C"
                    txtHumidityDetails.text = "$humidity%"
                    txtTempDetails.text = "$temperature°C"
                    txtDescription.text = description.toUpperCase()
                    txtPressureDetails.text = "$pressure hPa"
                    txtWeatherDetails.text = "${windSpeed} m/s"

                    displayWeatherIcon(iconCode)
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Error Parsing Weather Details",
                        Toast.LENGTH_SHORT
                    ).show()
                    e.printStackTrace()
                }
            },
            { error ->
                Toast.makeText(
                    this,
                    "Error Loading Weather Details",
                    Toast.LENGTH_SHORT
                ).show()
                error.printStackTrace()
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun getWeatherForCity(cityName: String) {
        val apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&appid=$apiKey"
        val request = JsonObjectRequest(
            Request.Method.GET, apiUrl, null,
            { data ->

                try {
                    val temperature =String.format( "%.2f",data.getJSONObject("main").getDouble("temp") - 273.15).toDouble()
                    val pressure = data.getJSONObject("main").getDouble("pressure")
                    val humidity = data.getJSONObject("main").getDouble("humidity")
                    val windSpeed = data.getJSONObject("wind").getDouble("speed")
                    val weatherArray = data.getJSONArray("weather")
                    val description = if (weatherArray.length() > 0) {
                        weatherArray.getJSONObject(0).getString("description")
                    } else {
                        ""
                    }
                    val iconCode = if (weatherArray.length() > 0) {
                        weatherArray.getJSONObject(0).getString("icon")
                    } else {
                        ""
                    }

                    txtCountry.text = cityName
                    txtCelcius2.text = "${temperature}°C"
                    txtHumidityDetails.text = "$humidity%"
                    txtTempDetails.text = "$temperature°C"
                    txtWeatherDetails.text = "${windSpeed} m/s"
                    txtDescription.text = description.toUpperCase()
                    txtPressureDetails.text = "$pressure hPa"


                    displayWeatherIcon(iconCode)
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Error Parsing Weather Details",
                        Toast.LENGTH_SHORT
                    ).show()
                    e.printStackTrace()
                }
            },
            { error ->
                Toast.makeText(
                    this,
                    "Error Loading Weather Details",
                    Toast.LENGTH_SHORT
                ).show()
                error.printStackTrace()
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun displayWeatherIcon(iconCode: String) {
        val iconUrl = "https://openweathermap.org/img/w/$iconCode.png"
        Picasso.get().load(iconUrl).into(imgWeatherImg)
    }
}