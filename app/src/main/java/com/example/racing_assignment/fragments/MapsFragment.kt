package com.example.racing_assignment.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.racing_assignment.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsFragment : Fragment(), OnMapReadyCallback {

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var score: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_maps, container, false)

        latitude = arguments?.getDouble("latitude", 0.0) ?: 0.0
        longitude = arguments?.getDouble("longitude", 0.0) ?: 0.0
        score = arguments?.getInt("score", 0) ?: 0

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        if (mapFragment == null) {
            val newMapFragment = SupportMapFragment.newInstance()
            childFragmentManager.beginTransaction()
                .replace(R.id.map, newMapFragment)
                .commit()
            newMapFragment.getMapAsync(this)
        } else {
            mapFragment.getMapAsync(this)
        }

        view.findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val location = LatLng(latitude, longitude)

        googleMap.addMarker(
            MarkerOptions()
                .position(location)
                .title("Score: $score")
        )

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }
}