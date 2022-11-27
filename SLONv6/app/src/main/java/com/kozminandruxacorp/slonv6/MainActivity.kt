package com.kozminandruxacorp.slonv6

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Handler
import android.provider.Settings
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import com.kozminandruxacorp.slonv6.databinding.ActivityMainBinding
import kotlin.math.pow

class MainActivity : AppCompatActivity(), LocationListener, SensorEventListener {
    lateinit var activityMain: ActivityMainBinding

    val exitArr = arrayOf(1, "")

    val flag = arrayOf(10, {false})

    val latArr = Array(10, {0.0})
    val lonArr = Array(10, {0.0})

    var latitude = 0.0
    var longitude = 0.0
    var oldLatitude = 0.0
    var oldLongitude = 0.0

    var gpsStatus: Boolean = false

// -------------------------------------------------- GPS --------------------------------------------------

    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2

// -------------------------------------------------- GPS --------------------------------------------------

// -------------------------------------------------- TIMER --------------------------------------------------

    var handler: Handler = Handler()
    var runnable: Runnable? = null
    var delay = 1000

// -------------------------------------------------- TIMER --------------------------------------------------


// -------------------------------------------------- COMPAS --------------------------------------------------

    var manager: SensorManager? = null
    var current_degree:Int = 0

// -------------------------------------------------- COMPAS --------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMain = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMain.root)
        manager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        checkPermissions()
    }

    override fun onResume() {
        handler.postDelayed(Runnable {
            handler.postDelayed(runnable!!, delay.toLong())
            if (!checkGpsStatus()) {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            getLocation()
            if ((latitude == oldLatitude) && (longitude == oldLongitude) && (flag[0] == false)) {
                activityMain.progressBar.visibility = View.VISIBLE
                activityMain.tvConnecting.visibility = View.VISIBLE
                flag[0] = true
            }
            if ((latitude != oldLatitude) && (longitude != oldLongitude)) {
                activityMain.progressBar.visibility = View.GONE
                activityMain.tvConnecting.visibility = View.GONE
                flag[0] = false
            }
            activityMain.tvNowLatitude.text = "%.6f".format(latitude)
            activityMain.tvNowLongitude.text = "%.6f".format(longitude)
            activityMain.tvDistanceBetweenAB.text = "%.2f".format(geodesicDistance(latitude, longitude, latArr[0], lonArr[0]))
            activityMain.tvDegreeRelativeNorth.text = "%.2f".format(putevoyUgol(latitude, longitude, latArr[0], lonArr[0]))

            oldLatitude = latitude
            oldLongitude = longitude

        }.also { runnable = it }, delay.toLong())
        super.onResume()
        manager?.registerListener(this,manager?.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable!!)
        manager?.unregisterListener(this)
    }

// -------------------------------------------------- Check permissions --------------------------------------------------

    fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) !==
            PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                )
            } else {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                )
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    if ((ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) ===
                                PackageManager.PERMISSION_GRANTED)
                    ) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                    exit()
                }
                return
            }
        }
    }

// -------------------------------------------------- Check permissions --------------------------------------------------

// -------------------------------------------------- GPS --------------------------------------------------

    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
    }

    override fun onLocationChanged(location: Location) {
        latitude = location.latitude
        longitude = location.longitude

    }

    private fun checkGpsStatus(): Boolean {
        val locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (gpsStatus) return true
        return false
    }


// -------------------------------------------------- GPS --------------------------------------------------

// -------------------------------------------------- MATH --------------------------------------------------


    fun geodesicDistance(alatitude: Double, alongitude: Double, blatitude: Double, blongitude: Double) : Double {
        val earthRadiusInMeters: Double = 6372797.560856
        val degreeesToRad: Double = 0.017453292519943295769236907684886

        var dtheta: Double = (alatitude - blatitude) * degreeesToRad
        var dlambda: Double = (alongitude - blongitude) * degreeesToRad
        var mean_t: Double = (alatitude + blatitude) * degreeesToRad / 2.0
        var cos_meant: Double = kotlin.math.cos(mean_t)

        return earthRadiusInMeters * kotlin.math.sqrt(dtheta * dtheta + cos_meant * cos_meant * dlambda * dlambda)
    }

    fun putevoyUgol(alatitude: Double, alongitude: Double, blatitude: Double, blongitude: Double) : Double {
        val e = 0.081819190842622 // первый эксцентриситет ( WGS84 )	е	\ simeq

        var longdelta = getDeltaLong(alongitude, blongitude)

        return radToGrad(kotlin.math.atan(longdelta / (calcLatitude(blatitude, e) - calcLatitude(alatitude, e))))
    }

    fun getDeltaLong(alongitude: Double, blongitude: Double) : Double {
        var longdiff = blongitude - alongitude
        var longdelta = 0.0

        if( longdiff > 180 ) {
            longdelta = longdiff - 360
        }
        else if (longdiff < -180) {
            longdelta = 360 + longdiff
        }
        else {
            longdelta = longdiff
        }
        return longdelta
    }

    fun calcLatitude(latitude: Double, extentricitet: Double) : Double {
        val latinRadians = gradToRad(latitude)
        val sinlat = kotlin.math.sin(latinRadians)
        val div = (1 - extentricitet * sinlat) / (1 + extentricitet * sinlat)
        val divinpow = div.pow(extentricitet / 2)

        val tan = kotlin.math.tan(kotlin.math.PI / 4 + latinRadians / 2) * divinpow

        return kotlin.math.ln(tan)
    }

    fun gradToRad(grad: Double) : Double {
        return grad * kotlin.math.PI / 180.0
    }

    fun radToGrad(rad: Double) : Double {
        return rad * 180.0 / kotlin.math.PI
    }

// -------------------------------------------------- MATH --------------------------------------------------

// -------------------------------------------------- COMPAS --------------------------------------------------

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun onSensorChanged(p0: SensorEvent?) {
        val degree:Int = p0?.values?.get(0)?.toInt()!!
        activityMain.tvAzimutDiff.text = degree.toString()/*
        val rotationAnim = RotateAnimation(current_degree.toFloat(),(-degree).toFloat(),Animation.RELATIVE_TO_SELF,
            0.5f, Animation.RELATIVE_TO_SELF,0.5f)
        rotationAnim.duration = 210
        rotationAnim.fillAfter = true
        current_degree = -degree
        imDinamic.startAnimation(rotationAnim)*/

    }

// -------------------------------------------------- COMPAS --------------------------------------------------

    fun buttonSave(view: View) {
        latArr[0] = latitude
        lonArr[0] = longitude
        activityMain.tvSavedLatitude.text = "%.6f".format(latArr[0])
        activityMain.tvSavedLongitude.text = "%.6f".format(lonArr[0])
    }

    fun exit() {
        exitArr[3] = "" as Nothing
    }
}