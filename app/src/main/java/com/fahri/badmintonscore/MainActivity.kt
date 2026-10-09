package com.fahri.badmintonscore

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Mode: true = 2 Orang per skor (Ganda), false = 1 Orang per skor (Tunggal)
    private var isDoublesMode = true

    // Nama Pemain Tim 1
    private var p1_1_Name = "Pemain 1A"
    private var p1_2_Name = "Pemain 1B"

    // Nama Pemain Tim 2
    private var p2_1_Name = "Pemain 2A"
    private var p2_2_Name = "Pemain 2B"

    // Skor 0 - 30
    private var player1Score = 0
    private var player2Score = 0

    // Game Menang
    private var player1Games = 0
    private var player2Games = 0
    private var currentGame = 1

    // Server aktif: 1=P1A, 2=P1B, 3=P2A, 4=P2B
    private var activeServer = 1

    // Views
    private lateinit var tvMatchStatus: TextView
    private lateinit var btnToggleMode: Button
    private lateinit var btnSwap: Button
    private lateinit var btnReset: Button

    // Tim 1 Views
    private lateinit var cardPlayer1: View
    private lateinit var rowP1_1: LinearLayout
    private lateinit var rowP1_2: LinearLayout
    private lateinit var tvP1_1_Name: TextView
    private lateinit var tvP1_2_Name: TextView
    private lateinit var tvP1_1_Serve: TextView
    private lateinit var tvP1_2_Serve: TextView
    private lateinit var tvPlayer1Score: TextView
    private lateinit var btnP1Plus: Button
    private lateinit var btnP1Minus: Button
    private lateinit var tvP1Games: TextView

    // Tim 2 Views
    private lateinit var cardPlayer2: View
    private lateinit var rowP2_1: LinearLayout
    private lateinit var rowP2_2: LinearLayout
    private lateinit var tvP2_1_Name: TextView
    private lateinit var tvP2_2_Name: TextView
    private lateinit var tvP2_1_Serve: TextView
    private lateinit var tvP2_2_Serve: TextView
    private lateinit var tvPlayer2Score: TextView
    private lateinit var btnP2Plus: Button
    private lateinit var btnP2Minus: Button
    private lateinit var tvP2Games: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Layar selalu menyala selama pertandingan
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_main)

        initViews()
        setupListeners()
        updateUI()
    }

    private fun initViews() {
        tvMatchStatus = findViewById(R.id.tvMatchStatus)
        btnToggleMode = findViewById(R.id.btnToggleMode)
        btnSwap = findViewById(R.id.btnSwap)
        btnReset = findViewById(R.id.btnReset)

        // Tim 1
        cardPlayer1 = findViewById(R.id.cardPlayer1)
        rowP1_1 = findViewById(R.id.rowP1_1)
        rowP1_2 = findViewById(R.id.rowP1_2)
        tvP1_1_Name = findViewById(R.id.tvP1_1_Name)
        tvP1_2_Name = findViewById(R.id.tvP1_2_Name)
        tvP1_1_Serve = findViewById(R.id.tvP1_1_Serve)
        tvP1_2_Serve = findViewById(R.id.tvP1_2_Serve)
        tvPlayer1Score = findViewById(R.id.tvPlayer1Score)
        btnP1Plus = findViewById(R.id.btnP1Plus)
        btnP1Minus = findViewById(R.id.btnP1Minus)
        tvP1Games = findViewById(R.id.tvP1Games)

        // Tim 2
        cardPlayer2 = findViewById(R.id.cardPlayer2)
        rowP2_1 = findViewById(R.id.rowP2_1)
        rowP2_2 = findViewById(R.id.rowP2_2)
        tvP2_1_Name = findViewById(R.id.tvP2_1_Name)
        tvP2_2_Name = findViewById(R.id.tvP2_2_Name)
        tvP2_1_Serve = findViewById(R.id.tvP2_1_Serve)
        tvP2_2_Serve = findViewById(R.id.tvP2_2_Serve)
        tvPlayer2Score = findViewById(R.id.tvPlayer2Score)
        btnP2Plus = findViewById(R.id.btnP2Plus)
        btnP2Minus = findViewById(R.id.btnP2Minus)
        tvP2Games = findViewById(R.id.tvP2Games)
    }

    private fun setupListeners() {
        // Toggle Mode 1 Orang vs 2 Orang per Tim
        btnToggleMode.setOnClickListener {
            isDoublesMode = !isDoublesMode
            if (!isDoublesMode && (activeServer == 2 || activeServer == 4)) {
                activeServer = if (activeServer == 2) 1 else 3
            }
            updateUI()
        }

        // Edit Nama masing-masing orang
        tvP1_1_Name.setOnClickListener { showEditNameDialog("Pemain 1A (Orang 1)", p1_1_Name) { p1_1_Name = it } }
        tvP1_2_Name.setOnClickListener { showEditNameDialog("Pemain 1B (Orang 2)", p1_2_Name) { p1_2_Name = it } }
        tvP2_1_Name.setOnClickListener { showEditNameDialog("Pemain 2A (Orang 1)", p2_1_Name) { p2_1_Name = it } }
        tvP2_2_Name.setOnClickListener { showEditNameDialog("Pemain 2B (Orang 2)", p2_2_Name) { p2_2_Name = it } }

        // Pindah Servis Manual per Orang
        tvP1_1_Serve.setOnClickListener { activeServer = 1; updateUI() }
        tvP1_2_Serve.setOnClickListener { activeServer = 2; updateUI() }
        tvP2_1_Serve.setOnClickListener { activeServer = 3; updateUI() }
        tvP2_2_Serve.setOnClickListener { activeServer = 4; updateUI() }

        // Tambah / Kurang Skor Tim 1
        cardPlayer1.setOnClickListener { addScore(1) }
        btnP1Plus.setOnClickListener { addScore(1) }
        btnP1Minus.setOnClickListener { minusScore(1) }

        // Tambah / Kurang Skor Tim 2
        cardPlayer2.setOnClickListener { addScore(2) }
        btnP2Plus.setOnClickListener { addScore(2) }
        btnP2Minus.setOnClickListener { minusScore(2) }

        // Tukar Sisi Lapangan
        btnSwap.setOnClickListener { swapSides() }

        // Reset
        btnReset.setOnClickListener { showResetOptionsDialog() }
    }

    private fun addScore(team: Int) {
        vibrate()
        if (team == 1) {
            if (player1Score < 30) {
                player1Score++
                // Jika servis sebelumnya di Tim 2, pindah ke Tim 1
                if (activeServer == 3 || activeServer == 4) {
                    activeServer = 1
                }
            }
        } else {
            if (player2Score < 30) {
                player2Score++
                // Jika servis sebelumnya di Tim 1, pindah ke Tim 2
                if (activeServer == 1 || activeServer == 2) {
                    activeServer = 3
                }
            }
        }

        updateUI()
        checkGameWinner()
    }

    private fun minusScore(team: Int) {
        vibrate()
        if (team == 1) {
            if (player1Score > 0) player1Score--
        } else {
            if (player2Score > 0) player2Score--
        }
        updateUI()
    }

    private fun checkGameWinner() {
        // Murni target 30 poin (tidak berhenti di 21)
        if (player1Score >= 30) {
            val teamName = if (isDoublesMode) "$p1_1_Name & $p1_2_Name" else p1_1_Name
            showWinnerDialog(teamName, 1)
        } else if (player2Score >= 30) {
            val teamName = if (isDoublesMode) "$p2_1_Name & $p2_2_Name" else p2_1_Name
            showWinnerDialog(teamName, 2)
        }
    }

    private fun showWinnerDialog(winnerTeam: String, winnerId: Int) {
        AlertDialog.Builder(this)
            .setTitle("🏆 30 POIN TERCAPAI!")
            .setMessage("$winnerTeam memenangkan Game $currentGame dengan skor 30!\n\nLanjut ke Game berikutnya?")
            .setCancelable(false)
            .setPositiveButton("Lanjut Game Berikutnya") { _, _ ->
                if (winnerId == 1) player1Games++ else player2Games++
                currentGame++
                player1Score = 0
                player2Score = 0
                swapSides()
            }
            .setNegativeButton("Tetap di Game Ini") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun swapSides() {
        vibrate()
        // Tukar Tim 1 dan Tim 2
        val tmp1A = p1_1_Name; p1_1_Name = p2_1_Name; p2_1_Name = tmp1A
        val tmp1B = p1_2_Name; p1_2_Name = p2_2_Name; p2_2_Name = tmp1B

        val tmpScore = player1Score; player1Score = player2Score; player2Score = tmpScore
        val tmpGames = player1Games; player1Games = player2Games; player2Games = tmpGames

        activeServer = when (activeServer) {
            1 -> 3
            2 -> 4
            3 -> 1
            4 -> 2
            else -> 1
        }
        updateUI()
    }

    private fun showEditNameDialog(title: String, currentName: String, onSave: (String) -> Unit) {
        val input = EditText(this)
        input.setSingleLine(true)
        input.setText(currentName)
        input.setSelection(currentName.length)

        AlertDialog.Builder(this)
            .setTitle("Ganti Nama $title")
            .setView(input)
            .setPositiveButton("Simpan") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    onSave(newName)
                    updateUI()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showResetOptionsDialog() {
        val options = arrayOf("Reset Skor Game Ini (0 - 0)", "Mulai Pertandingan Baru (Reset Total)")
        AlertDialog.Builder(this)
            .setTitle("Pilih Jenis Reset")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        player1Score = 0
                        player2Score = 0
                        updateUI()
                    }
                    1 -> {
                        player1Score = 0
                        player2Score = 0
                        player1Games = 0
                        player2Games = 0
                        currentGame = 1
                        updateUI()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateUI() {
        // Mode Button Label
        btnToggleMode.text = if (isDoublesMode) "👥 2 ORANG" else "👤 1 ORANG"

        // Visibility Baris Orang 2
        rowP1_2.visibility = if (isDoublesMode) View.VISIBLE else View.GONE
        rowP2_2.visibility = if (isDoublesMode) View.VISIBLE else View.GONE

        // Set Nama
        tvP1_1_Name.text = if (isDoublesMode) "1. $p1_1_Name" else p1_1_Name
        tvP1_2_Name.text = "2. $p1_2_Name"

        tvP2_1_Name.text = if (isDoublesMode) "1. $p2_1_Name" else p2_1_Name
        tvP2_2_Name.text = "2. $p2_2_Name"

        // Skor 0 - 30
        tvPlayer1Score.text = player1Score.toString()
        tvPlayer2Score.text = player2Score.toString()

        // Game Won
        tvP1Games.text = "Game Menang: $player1Games"
        tvP2Games.text = "Game Menang: $player2Games"

        // Indikator Servis per Orang
        tvP1_1_Serve.visibility = if (activeServer == 1) View.VISIBLE else View.INVISIBLE
        tvP1_2_Serve.visibility = if (isDoublesMode && activeServer == 2) View.VISIBLE else View.INVISIBLE

        tvP2_1_Serve.visibility = if (activeServer == 3) View.VISIBLE else View.INVISIBLE
        tvP2_2_Serve.visibility = if (isDoublesMode && activeServer == 4) View.VISIBLE else View.INVISIBLE

        // Status Pertandingan (Target 30)
        val statusText = when {
            player1Score == 29 || player2Score == 29 -> "MATCH POINT MENUJU 30!"
            player1Score >= 25 || player2Score >= 25 -> "GAME KRUSIAL (MENUJU 30)"
            else -> "GAME $currentGame (TARGET 30 POIN)"
        }
        tvMatchStatus.text = statusText
    }

    private fun vibrate() {
        try {
            val v = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (v != null && v.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(35)
                }
            }
        } catch (_: Exception) {
        }
    }
}
