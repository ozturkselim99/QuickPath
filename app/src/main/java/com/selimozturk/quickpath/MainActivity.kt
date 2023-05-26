package com.selimozturk.quickpath

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.errors.ApiException
import com.google.maps.model.DirectionsResult
import com.google.maps.model.LatLng
import com.selimozturk.quickpath.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val layoutList by lazy { mutableListOf<View>() }

    data class Coords(val id: Int, val lat: Double, val lng: Double)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnCreate.setOnClickListener {
            if (binding.edtTxtAddressCount.text.isNullOrEmpty()) {
                Toast.makeText(
                    this@MainActivity, "Please enter the count of address", Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            binding.btnClear.visibility = View.VISIBLE
            createAddressLayouts(binding.edtTxtAddressCount.text.toString().toInt(), it.context)
        }

        binding.btnClear.setOnClickListener {
            clear()
        }
    }

    private fun clear() {
        layoutList.forEach { layout ->
            layout.findViewById<TextInputEditText>(R.id.edt_txt_address_lat)?.text = null
            layout.findViewById<TextInputEditText>(R.id.edt_txt_address_lng)?.text = null
        }
    }


    private fun createAddressLayouts(numCoords: Int, context: Context) = with(binding) {
        clearAddressLayouts()
        repeat(numCoords) { i ->
            val layout = createLinearLayout(context, i)
            val slideInAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_in)
            layout.startAnimation(slideInAnimation)
            quickPath.addView(layout)
            layoutList.add(layout)
        }

        val textViewResult = createTextView(
            context, "Shortest Route:", 16f, ContextCompat.getColor(context, R.color.white)
        )
        textViewResult.id = R.id.txt_result
        textViewResult.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 24.dpToPx()
        }
        quickPath.addView(textViewResult)

        val btnFindShortestRoute = createMaterialButton(context, "Find Shortest Route") {
            val route = getRoute()
            findViewById<TextView>(R.id.txt_result)?.text = "Shortest Route: $route"
        }
        btnFindShortestRoute.id = R.id.btn_find_shortest_route
        btnFindShortestRoute.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 24.dpToPx()
        }
        quickPath.addView(btnFindShortestRoute)
    }

    private fun createLinearLayout(context: Context, i: Int): LinearLayout {
        val layout = LinearLayout(context)
        layout.id = View.generateViewId()
        layout.orientation = LinearLayout.HORIZONTAL
        layout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 24.dpToPx()
        }
        val textView = createTextView(
            context, "${i + 1}. Address", 16f, ContextCompat.getColor(context, R.color.white)
        )
        val addressLatLayout = createTextInputLayout(context, "Lat")
        val addressLatEditText = createTextInputEditText(
            context, InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        ).apply {
            id = R.id.edt_txt_address_lat
        }
        val addressLngLayout = createTextInputLayout(context, "Lng")
        val addressLngEditText = createTextInputEditText(
            context, InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        ).apply {
            id = R.id.edt_txt_address_lng
        }
        layout.addView(textView)
        layout.addView(addressLatLayout)
        addressLatLayout.addView(addressLatEditText)
        layout.addView(addressLngLayout)
        addressLngLayout.addView(addressLngEditText)
        return layout
    }

    private fun createTextInputLayout(context: Context, hint: String): TextInputLayout {
        val shapeDrawable = GradientDrawable()
        shapeDrawable.cornerRadius = 12f
        shapeDrawable.setColor(Color.WHITE)

        val addressLayout = TextInputLayout(context)
        addressLayout.placeholderText = hint
        addressLayout.boxBackgroundColor = ContextCompat.getColor(context, R.color.white)
        addressLayout.boxStrokeWidth = 0
        addressLayout.boxStrokeWidthFocused = 0
        addressLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            weight = 1f
            marginStart = 16.dpToPx()
        }
        addressLayout.background = shapeDrawable

        return addressLayout
    }

    private fun createTextInputEditText(context: Context, inputType: Int): TextInputEditText {

        val shapeDrawable = GradientDrawable()
        shapeDrawable.cornerRadius = 12f
        shapeDrawable.setColor(Color.WHITE)

        val editText = TextInputEditText(context)
        editText.inputType = inputType
        editText.setBackgroundResource(android.R.color.white)
        editText.background = shapeDrawable
        editText.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
        )
        editText.setPadding(24, 16, 24, 16)

        return editText
    }

    private fun createTextView(
        context: Context,
        text: String,
        textSize: Float,
        textColor: Int,
        typeface: Typeface? = ResourcesCompat.getFont(context, R.font.lucon),
        layoutParams: ViewGroup.LayoutParams? = null
    ): TextView {
        val textView = TextView(context)
        textView.text = text
        textView.textSize = textSize
        textView.setTextColor(textColor)
        typeface?.let {
            textView.typeface = it
        }
        layoutParams?.let {
            textView.layoutParams = it
        }
        return textView
    }

    private fun createMaterialButton(
        context: Context, text: String, onClickListener: View.OnClickListener?
    ): MaterialButton {
        val button = MaterialButton(context)
        button.text = text
        button.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        button.backgroundTintList = ContextCompat.getColorStateList(context, R.color.orange)
        button.setTextColor(ContextCompat.getColor(context, R.color.white))
        button.isAllCaps = false
        button.typeface = ResourcesCompat.getFont(context, R.font.lucon)
        button.insetBottom = 0
        button.insetTop = 0
        button.cornerRadius = 12.dpToPx()
        button.setOnClickListener(onClickListener)
        return button
    }

    private fun getRoute(): String = with(binding) {
        val addresses = mutableListOf<Coords>()
        var id = 1
        var isValid = true // Flag to track field validity
        for (layout in layoutList) {
            val latEditText = layout.findViewById<TextInputEditText>(R.id.edt_txt_address_lat)
            val lngEditText = layout.findViewById<TextInputEditText>(R.id.edt_txt_address_lng)
            if (!latEditText?.text.isNullOrEmpty() && !lngEditText?.text.isNullOrEmpty()) {
                val lat = latEditText.text.toString().toDouble()
                val lng = lngEditText.text.toString().toDouble()
                addresses.add(Coords(id, lat, lng))
                id += 1
            } else {
                isValid = false // Set flag to false if fields are not filled
                break // Exit the loop if any field is empty
            }
        }
        if (isValid && !binding.edtTxtDepoLat.text.isNullOrEmpty() && !binding.edtTxtDepoLng.text.isNullOrEmpty()) {
            val depotCoords = Coords(
                -1,
                binding.edtTxtDepoLat.text.toString().toDouble(),
                binding.edtTxtDepoLng.text.toString().toDouble()
            )
            val route = findShortestRoute(depotCoords, addresses).map {
                it.id
            }
            return route.joinToString(separator = " - ")
        } else {
            Toast.makeText(this@MainActivity, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return "" // Return an empty string or handle the invalid case accordingly
        }
    }

    private fun clearAddressLayouts() {
        val btnFindShortestRoute = findViewById<Button>(R.id.btn_find_shortest_route)
        val txtResult = findViewById<TextView>(R.id.txt_result)
        for (layout in layoutList) {
            binding.quickPath.removeView(layout)
        }
        binding.quickPath.removeView(btnFindShortestRoute)
        binding.quickPath.removeView(txtResult)
        layoutList.clear()
    }

    private fun findShortestRoute(
        depotCoords: Coords,
        deliveryCoords: List<Coords>,
    ): List<Coords> {

        val depot = LatLng(depotCoords.lat, depotCoords.lng)
        val deliveries = deliveryCoords.map { LatLng(it.lat, it.lng) }

        val context = GeoApiContext.Builder().apiKey(API_KEY).build()

        val request = DirectionsApi.newRequest(context).origin(depot).destination(depot)
            .waypoints(*deliveries.toTypedArray()).optimizeWaypoints(true)

        val result: DirectionsResult = try {
            request.await()
        } catch (e: ApiException) {
            throw Exception("Directions API error: ${e.message}")
        } catch (e: InterruptedException) {
            throw Exception("Directions API request interrupted")
        } catch (e: Exception) {
            throw Exception("Directions API request failed: ${e.message}")
        }

        val shortestRoute = ArrayList<Coords>(deliveryCoords.size)
        val order = result.routes.firstOrNull()?.waypointOrder ?: intArrayOf()

        val uniqueDeliveryCoords = HashSet(deliveryCoords)
        for (index in order) {
            if (index < 0 || index >= deliveryCoords.size) {
                throw Exception("Invalid index in waypoint order: $index")
            }
            val deliveryCoord = deliveryCoords[index]
            if (uniqueDeliveryCoords.contains(deliveryCoord) && !shortestRoute.contains(
                    deliveryCoord
                )
            ) {
                shortestRoute.add(deliveryCoord)
            }
        }
        return shortestRoute
    }

    private fun Int.dpToPx(): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), resources.displayMetrics
        ).toInt()
    }

    companion object {
        // Obtain an API key from the Google Cloud Console.
        const val API_KEY = ""
    }

}