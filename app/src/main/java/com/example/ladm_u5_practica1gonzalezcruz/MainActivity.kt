package com.example.ladm_u5_practica1gonzalezcruz

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var baseRemota = FirebaseFirestore.getInstance()
    var posicion = ArrayList<Data>()
    var lugares = ArrayList<String>()
    lateinit var locacion : LocationManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),1)
        }

        baseRemota.collection("Tecnologico").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if(firebaseFirestoreException != null){
                textView2.setText("ERROR: "+firebaseFirestoreException.message)
                return@addSnapshotListener
            }
            //var resultado = ""
            posicion.clear()
            lugares.clear()
            for (document in querySnapshot!!){
                var data = Data()
                data.nombre = document.getString("nombre").toString()
                data.posicion1 = document.getGeoPoint("posicion1")!!
                data.posicion2 = document.getGeoPoint("posicion2")!!

                posicion.add(data)
                lugares.add(document.getString("nombre").toString())
            }
            var adaptador = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,lugares)
            lista.adapter = adaptador
        }

        lista.setOnItemClickListener { parent, view, position, id ->
            var latitud = posicion[position].posicion1.latitude
            var longitud = posicion[position].posicion1.longitude
            var nombre = lugares[position]
            var intent : Intent = Intent(this,MapsActivity::class.java)
            intent.putExtra("latitud",latitud)
            intent.putExtra("longitud",longitud)
            intent.putExtra("nombre",nombre)
            startActivity(intent)
        }

        locacion = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var oyente = Oyente(this)

        locacion.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,oyente)
    }
    class Oyente(puntero : MainActivity) : LocationListener{
        var p = puntero
        override fun onLocationChanged(location: Location) {
            p.textView2.setText("${location.latitude}, ${location.longitude}")
            p.textView3.setText("")
            var geoPosicion = GeoPoint(location.latitude, location.longitude)
            for (item in p.posicion){
                if(item.estoyEn(geoPosicion)){
                    p.textView3.setText("Estas en: ${item.nombre}")
                }
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

        }

        override fun onProviderEnabled(provider: String?) {

        }

        override fun onProviderDisabled(provider: String?) {

        }

    }
}