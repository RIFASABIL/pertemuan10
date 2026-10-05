package com.example.logisticstrackerapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var etResi: EditText
    private lateinit var btnLacak: Button
    private lateinit var tvValidationError: TextView

    private lateinit var btnExp8801: Button
    private lateinit var btnExp8802: Button
    private lateinit var btnExp8803: Button
    private lateinit var btnExp8804: Button

    private lateinit var progressBar: ProgressBar
    private lateinit var layoutError: LinearLayout
    private lateinit var tvErrorMessage: TextView

    private lateinit var cardResult: View
    private lateinit var tvResultResi: TextView
    private lateinit var tvResultStatus: TextView
    private lateinit var tvResultCourier: TextView
    private lateinit var tvResultService: TextView
    private lateinit var tvResultLocation: TextView
    private lateinit var tvResultRecipient: TextView
    private lateinit var tvResultDelivery: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupListeners()
        showInitialState()
    }

    private fun initViews() {
        etResi = findViewById(R.id.etResi)
        btnLacak = findViewById(R.id.btnLacak)
        tvValidationError = findViewById(R.id.tvValidationError)

        btnExp8801 = findViewById(R.id.btnExp8801)
        btnExp8802 = findViewById(R.id.btnExp8802)
        btnExp8803 = findViewById(R.id.btnExp8803)
        btnExp8804 = findViewById(R.id.btnExp8804)

        progressBar = findViewById(R.id.progressBar)
        layoutError = findViewById(R.id.layoutError)
        tvErrorMessage = findViewById(R.id.tvErrorMessage)

        cardResult = findViewById(R.id.cardResult)
        tvResultResi = findViewById(R.id.tvResultResi)
        tvResultStatus = findViewById(R.id.tvResultStatus)
        tvResultCourier = findViewById(R.id.tvResultCourier)
        tvResultService = findViewById(R.id.tvResultService)
        tvResultLocation = findViewById(R.id.tvResultLocation)
        tvResultRecipient = findViewById(R.id.tvResultRecipient)
        tvResultDelivery = findViewById(R.id.tvResultDelivery)
    }

    private fun setupListeners() {
        btnLacak.setOnClickListener {
            val resiNumber = etResi.text.toString().trim()
            if (resiNumber.isEmpty()) {
                tvValidationError.text = "Masukkan nomor resi terlebih dahulu."
                tvValidationError.visibility = View.VISIBLE
                showInitialState()
            } else {
                tvValidationError.visibility = View.GONE
                performTracking(resiNumber)
            }
        }

        btnExp8801.setOnClickListener { setQuickResi("EXP-8801") }
        btnExp8802.setOnClickListener { setQuickResi("EXP-8802") }
        btnExp8803.setOnClickListener { setQuickResi("EXP-8803") }
        btnExp8804.setOnClickListener { setQuickResi("EXP-8804") }
    }

    private fun setQuickResi(resi: String) {
        etResi.setText(resi)
        etResi.setSelection(resi.length)
        tvValidationError.visibility = View.GONE
    }

    private fun showInitialState() {
        cardResult.visibility = View.GONE
        progressBar.visibility = View.GONE
        layoutError.visibility = View.GONE
        btnLacak.isEnabled = true
    }

    private fun showLoadingState() {
        progressBar.visibility = View.VISIBLE
        cardResult.visibility = View.GONE
        layoutError.visibility = View.GONE
        btnLacak.isEnabled = false
    }

    private fun showSuccessState(data: TrackingResponse, requestedResi: String) {
        progressBar.visibility = View.GONE
        layoutError.visibility = View.GONE
        cardResult.visibility = View.VISIBLE
        btnLacak.isEnabled = true

        val trackingNum = data.trackingNumber ?: data.id ?: requestedResi
        tvResultResi.text = "Resi: $trackingNum"

        val status = data.status ?: "-"
        tvResultStatus.text = status
        applyStatusBadgeStyle(status)

        tvResultCourier.text = data.courierName ?: "-"
        tvResultService.text = data.serviceType ?: "-"
        tvResultLocation.text = data.lastLocation ?: "-"
        tvResultRecipient.text = data.recipientName ?: "-"
        tvResultDelivery.text = data.estimatedDelivery ?: "-"
    }

    private fun showErrorState(message: String) {
        progressBar.visibility = View.GONE
        cardResult.visibility = View.GONE
        layoutError.visibility = View.VISIBLE
        tvErrorMessage.text = message
        btnLacak.isEnabled = true
    }

    private fun applyStatusBadgeStyle(status: String) {
        when (status.uppercase()) {
            "IN_TRANSIT" -> {
                tvResultStatus.background = ContextCompat.getDrawable(this, R.drawable.bg_status_in_transit)
                tvResultStatus.setTextColor(android.graphics.Color.parseColor("#B45309"))
            }
            "DELIVERED" -> {
                tvResultStatus.background = ContextCompat.getDrawable(this, R.drawable.bg_status_delivered)
                tvResultStatus.setTextColor(android.graphics.Color.parseColor("#15803D"))
            }
            "PENDING" -> {
                tvResultStatus.background = ContextCompat.getDrawable(this, R.drawable.bg_status_pending)
                tvResultStatus.setTextColor(android.graphics.Color.parseColor("#374151"))
            }
            else -> {
                tvResultStatus.background = ContextCompat.getDrawable(this, R.drawable.bg_status_pending)
                tvResultStatus.setTextColor(android.graphics.Color.parseColor("#374151"))
            }
        }
    }

    private fun performTracking(resiNumber: String) {
        showLoadingState()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.apiService.getTrackingDetail(resiNumber)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            showSuccessState(body, resiNumber)
                        } else {
                            showErrorState("Nomor resi $resiNumber tidak ditemukan pada database server (HTTP 404)")
                        }
                    } else if (response.code() == 404) {
                        showErrorState("Nomor resi $resiNumber tidak ditemukan pada database server (HTTP 404)")
                    } else {
                        showErrorState("Nomor resi $resiNumber tidak ditemukan pada database server (HTTP ${response.code()})")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showErrorState("Koneksi internet bermasalah: Gagal terhubung ke server")
                }
            }
        }
    }
}
