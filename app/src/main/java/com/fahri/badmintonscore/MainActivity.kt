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
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var player1Name = "Pemain 1"
    private var player2Name = "Pemain 2"

    private var player1Score = 0
    private var player2Score = 0

    private var player1Games = 0
    private var player2Games = 0

    private var currentGame = 1
    private var isPlayer1Serving = true

    // Views
    private lateinit var tvMatchStatus: TextView
    private lateinit var tvPlayer1Name: TextView
    private lateinit var tvPlayer2Name: TextView
    private lateinit var tvPlayer1Score: TextView
    private lateinit var tvPlayer2Score: TextView
    private lateinit var tvP1Games: TextView
    private lateinit var tvP2Games: TextView
    private lateinit var tvServeP1: TextView
    private lateinit var tvServeP2: TextView
    private lateinit var btnSwap: Button
    private lateinit var btnReset: Button
    private lateinit var cardPlayer1: View
    private lateinit var cardPlayer2: View
    private lateinit var btnP1Plus: Button
    private lateinit var btnP1Minus: Button
    private lateinit var btnP2Plus: Button
    private lateinit var btnP2Minus: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Pastikan layar selalu menyala saat pertandingan
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_main)

        initViews()
        setupListeners()
        updateUI()
    }

    private fun initViews() {
        tvMatchStatus = findViewById(R.id.tvMatchStatus)
        tvPlayer1Name = findViewById(R.id.tvPlayer1Name)
        tvPlayer2Name = findViewById(R.id.tvPlayer2Name)
        tvPlayer1Score = findViewById(R.id.tvPlayer1Score)
        tvPlayer2Score = findViewById(R.id.tvPlayer2Score)
        tvP1Games = findViewById(R.id.tvP1Games)
        tvP2Games = findViewById(R.id.tvP2Games)
        tvServeP1 = findViewById(R.id.tvServeP1)
        tvServeP2 = findViewById(R.id.tvServeP2)
        btnSwap = findViewById(R.id.btnSwap)
        btnReset = findViewById(R.id.btnReset)
        cardPlayer1 = findViewById(R.id.cardPlayer1)
        cardPlayer2 = findViewById(R.id.cardPlayer2)
        btnP1Plus = findViewById(R.id.btnP1Plus)
        btnP1Minus = findViewById(R.id.btnP1Minus)
        btnP2Plus = findViewById(R.id.btnP2Plus)
        btnP2Minus = findViewById(R.id.btnP2Minus)
    }

    private fun setupListeners() {
        // Ganti Nama Pemain 1
        tvPlayer1Name.setOnClickListener { showEditNameDialog(1) }
        // Ganti Nama Pemain 2
        tvPlayer2Name.setOnClickListener { showEditNameDialog(2) }

        // Tambah Skor Pemain 1 (Tap Card atau Tombol Plus)
        cardPlayer1.setOnClickListener { addScore(1) }
        btnP1Plus.setOnClickListener { addScore(1) }
        btnP1Minus.setOnClickListener { minusScore(1) }

        // Tambah Skor Pemain 2 (Tap Card atau Tombol Plus)
        cardPlayer2.setOnClickListener { addScore(2) }
        btnP2Plus.setOnClickListener { addScore(2) }
        btnP2Minus.setOnClickListener { minusScore(2) }

        // Toggle Servis Manual
        tvServeP1.setOnClickListener {
            isPlayer1Serving = true
            updateUI()
        }
        tvServeP2.setOnClickListener {
            isPlayer1Serving = false
            updateUI()
        }

        // Tombol Tukar Sisi / Lapangan
        btnSwap.setOnClickListener { swapSides() }

        // Tombol Reset
        btnReset.setOnClickListener { showResetOptionsDialog() }
    }

    private fun addScore(player: Int) {
        vibrate()
        if (player == 1) {
            if (player1Score < 30) {
                player1Score++
                isPlayer1Serving = true
            }
        } else {
            if (player2Score < 30) {
                player2Score++
                isPlayer1Serving = false
            }
        }

        updateUI()
        checkGameWinner()
    }

    private fun minusScore(player: Int) {
        vibrate()
        if (player == 1) {
            if (player1Score > 0) player1Score--
        } else {
            if (player2Score > 0) player2Score--
        }
        updateUI()
    }

    private fun checkGameWinner() {
        // Aturan Badminton BWF:
        // Menang jika >= 21 poin dan selisih >= 2 poin, atau siapa yang duluan 30 poin.
        val p1Won = (player1Score >= 21 && player1Score - player2Score >= 2) || (player1Score == 30)
        val p2Won = (player2Score >= 21 && player2Score - player1Score >= 2) || (player2Score == 30)

        if (p1Won) {
            showWinnerDialog(player1Name, player1Score, player2Score, 1)
        } else if (p2Won) {
            showWinnerDialog(player2Name, player2Score, player1Score, 2)
        }
    }

    private fun showWinnerDialog(winnerName: String, winScore: Int, loseScore: Int, winnerId: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("🏆 GAME $currentGame SELESAI!")
        builder.setMessage("$winnerName memenangkan Game $currentGame dengan skor $winScore - $loseScore!\n\nLanjut ke game berikutnya?")
        builder.setCancelable(false)
        builder.setPositiveButton("Lanjut Game Berikutnya") { _, _ ->
            if (winnerId == 1) player1Games++ else player2Games++
            currentGame++
            player1Score = 0
            player2Score = 0
            // Otomatis tukar sisi untuk game berikutnya
            swapSides(prompt = false)
        }
        builder.setNegativeButton("Tetap di Game Ini") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun swapSides(prompt: Boolean = true) {
        vibrate()
        // Tukar posisi Nama, Skor, dan Game
        val tempName = player1Name
        player1Name = player2Name
        player2Name = tempName

        val tempScore = player1Score
        player1Score = player2Score
        player2Score = tempScore

        val tempGames = player1Games
        player1Games = player2Games
        player2Games = tempGames

        isPlayer1Serving = !isPlayer1Serving
        updateUI()
    }

    private fun showEditNameDialog(player: Int) {
        val input = EditText(this)
        input.setSingleLine(true)
        val currentName = if (player == 1) player1Name else player2Name
        input.setText(currentName)
        input.setSelection(currentName.length)

        AlertDialog.Builder(this)
            .setTitle("Ganti Nama Pemain $player")
            .setView(input)
            .setPositiveButton("Simpan") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    if (player == 1) player1Name = newName else player2Name = newName
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
        tvPlayer1Name.text = player1Name
        tvPlayer2Name.text = player2Name

        tvPlayer1Score.text = player1Score.toString()
        tvPlayer2Score.text = player2Score.toString()

        tvP1Games.text = "Game Menang: $player1Games"
        tvP2Games.text = "Game Menang: $player2Games"

        // Status Servis
        if (isPlayer1Serving) {
            tvServeP1.visibility = View.VISIBLE
            tvServeP2.visibility = View.INVISIBLE
        } else {
            tvServeP1.visibility = View.INVISIBLE
            tvServeP2.visibility = View.VISIBLE
        }

        // Status Pertandingan (Game Point / Deuce / Setting)
        val isDeuce = player1Score >= 20 && player2Score >= 20
        val isMatchPoint = (player1Score >= 20 && player1Score > player2Score) || (player2Score >= 20 && player2Score > player1Score)

        val statusText = when {
            player1Score == 29 && player2Score == 29 -> "SUDDEN DEATH (30 POIN MENANG!)"
            isDeuce -> "DEUCE (Selisih 2 Poin, Max 30)"
            isMatchPoint -> "GAME POINT!"
            else -> "GAME $currentGame (Sets: $player1Games - $player2Games)"
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
