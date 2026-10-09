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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Target Skor Pilihan: 30 atau 21
    private var targetScore = 30

    // Mode: true = Ganda (2 Orang per Skor), false = Tunggal (1 Orang)
    private var isDoublesMode = true

    // Nama Pemain Tim 1 (Biru)
    private var p1_1_Name = "Pemain 1A"
    private var p1_2_Name = "Pemain 1B"

    // Nama Pemain Tim 2 (Merah)
    private var p2_1_Name = "Pemain 2A"
    private var p2_2_Name = "Pemain 2B"

    // Skor 0 - 30
    private var player1Score = 0
    private var player2Score = 0

    // Status Game & Sets
    private var player1Games = 0
    private var player2Games = 0
    private var currentGame = 1
    private var hasNotifiedInterval = false

    // Server Aktif:
    // 1 = Tim 1 (Orang Ke-1)
    // 2 = Tim 1 (Orang Ke-2)
    // 3 = Tim 2 (Orang Ke-1)
    // 4 = Tim 2 (Orang Ke-2)
    private var activeServer = 1

    // Views Top Bar
    private lateinit var btnTargetScore: Button
    private lateinit var btnToggleMode: Button
    private lateinit var btnSwap: Button
    private lateinit var btnReset: Button
    private lateinit var tvServeBanner: TextView
    private lateinit var btnPindahBola: Button

    // Tim 1 Views
    private lateinit var rowP1_1: LinearLayout
    private lateinit var rowP1_2: LinearLayout
    private lateinit var tvP1_1_Name: TextView
    private lateinit var tvP1_2_Name: TextView
    private lateinit var btnEditP1_1: TextView
    private lateinit var btnEditP1_2: TextView
    private lateinit var tvP1_1_ServeTag: TextView
    private lateinit var tvP1_2_ServeTag: TextView
    private lateinit var tvPlayer1Score: TextView
    private lateinit var btnP1Plus: Button
    private lateinit var btnP1Minus: Button
    private lateinit var tvP1Games: TextView

    // Tim 2 Views
    private lateinit var rowP2_1: LinearLayout
    private lateinit var rowP2_2: LinearLayout
    private lateinit var tvP2_1_Name: TextView
    private lateinit var tvP2_2_Name: TextView
    private lateinit var btnEditP2_1: TextView
    private lateinit var btnEditP2_2: TextView
    private lateinit var tvP2_1_ServeTag: TextView
    private lateinit var tvP2_2_ServeTag: TextView
    private lateinit var tvPlayer2Score: TextView
    private lateinit var btnP2Plus: Button
    private lateinit var btnP2Minus: Button
    private lateinit var tvP2Games: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Layar selalu menyala terus saat pertandingan berlangsung
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_main)

        initViews()
        setupListeners()
        updateUI()
    }

    private fun initViews() {
        btnTargetScore = findViewById(R.id.btnTargetScore)
        btnToggleMode = findViewById(R.id.btnToggleMode)
        btnSwap = findViewById(R.id.btnSwap)
        btnReset = findViewById(R.id.btnReset)
        tvServeBanner = findViewById(R.id.tvServeBanner)
        btnPindahBola = findViewById(R.id.btnPindahBola)

        // Tim 1
        rowP1_1 = findViewById(R.id.rowP1_1)
        rowP1_2 = findViewById(R.id.rowP1_2)
        tvP1_1_Name = findViewById(R.id.tvP1_1_Name)
        tvP1_2_Name = findViewById(R.id.tvP1_2_Name)
        btnEditP1_1 = findViewById(R.id.btnEditP1_1)
        btnEditP1_2 = findViewById(R.id.btnEditP1_2)
        tvP1_1_ServeTag = findViewById(R.id.tvP1_1_ServeTag)
        tvP1_2_ServeTag = findViewById(R.id.tvP1_2_ServeTag)
        tvPlayer1Score = findViewById(R.id.tvPlayer1Score)
        btnP1Plus = findViewById(R.id.btnP1Plus)
        btnP1Minus = findViewById(R.id.btnP1Minus)
        tvP1Games = findViewById(R.id.tvP1Games)

        // Tim 2
        rowP2_1 = findViewById(R.id.rowP2_1)
        rowP2_2 = findViewById(R.id.rowP2_2)
        tvP2_1_Name = findViewById(R.id.tvP2_1_Name)
        tvP2_2_Name = findViewById(R.id.tvP2_2_Name)
        btnEditP2_1 = findViewById(R.id.btnEditP2_1)
        btnEditP2_2 = findViewById(R.id.btnEditP2_2)
        tvP2_1_ServeTag = findViewById(R.id.tvP2_1_ServeTag)
        tvP2_2_ServeTag = findViewById(R.id.tvP2_2_ServeTag)
        tvPlayer2Score = findViewById(R.id.tvPlayer2Score)
        btnP2Plus = findViewById(R.id.btnP2Plus)
        btnP2Minus = findViewById(R.id.btnP2Minus)
        tvP2Games = findViewById(R.id.tvP2Games)
    }

    private fun setupListeners() {
        // Toggle Target Skor 30 vs 21
        btnTargetScore.setOnClickListener {
            targetScore = if (targetScore == 30) 21 else 30
            Toast.makeText(this, "Target Skor Diubah ke: $targetScore Poin", Toast.LENGTH_SHORT).show()
            updateUI()
        }

        // Toggle Mode 1 Orang vs 2 Orang per Skor
        btnToggleMode.setOnClickListener {
            isDoublesMode = !isDoublesMode
            if (!isDoublesMode && (activeServer == 2 || activeServer == 4)) {
                activeServer = if (activeServer == 2) 1 else 3
            }
            updateUI()
        }

        // Tombol Wasit: Pindah Bola / Pindah Orang (HANYA GANTI SERVIS, TIDAK MENAMBAH POIN!)
        btnPindahBola.setOnClickListener {
            nextServerCycle()
        }

        // =========================================================================
        // KLIK ORANG 1 / ORANG 2 -> HANYA PINDAH SERVIS (TIDAK MENAMBAH POIN SAMA SEKALI!)
        // =========================================================================
        val setServerP1_1 = {
            vibrate()
            activeServer = 1
            Toast.makeText(this, "Servis: Tim 1 (Orang Ke-1)", Toast.LENGTH_SHORT).show()
            updateUI()
        }
        val setServerP1_2 = {
            vibrate()
            activeServer = 2
            Toast.makeText(this, "Servis: Tim 1 (Orang Ke-2)", Toast.LENGTH_SHORT).show()
            updateUI()
        }
        val setServerP2_1 = {
            vibrate()
            activeServer = 3
            Toast.makeText(this, "Servis: Tim 2 (Orang Ke-1)", Toast.LENGTH_SHORT).show()
            updateUI()
        }
        val setServerP2_2 = {
            vibrate()
            activeServer = 4
            Toast.makeText(this, "Servis: Tim 2 (Orang Ke-2)", Toast.LENGTH_SHORT).show()
            updateUI()
        }

        rowP1_1.setOnClickListener { setServerP1_1() }
        tvP1_1_Name.setOnClickListener { setServerP1_1() }
        tvP1_1_ServeTag.setOnClickListener { setServerP1_1() }

        rowP1_2.setOnClickListener { setServerP1_2() }
        tvP1_2_Name.setOnClickListener { setServerP1_2() }
        tvP1_2_ServeTag.setOnClickListener { setServerP1_2() }

        rowP2_1.setOnClickListener { setServerP2_1() }
        tvP2_1_Name.setOnClickListener { setServerP2_1() }
        tvP2_1_ServeTag.setOnClickListener { setServerP2_1() }

        rowP2_2.setOnClickListener { setServerP2_2() }
        tvP2_2_Name.setOnClickListener { setServerP2_2() }
        tvP2_2_ServeTag.setOnClickListener { setServerP2_2() }

        // =========================================================================
        // GANTI NAMA: Tombol Pensil (✏️) atau Tahan (Long Click) pada Nama
        // =========================================================================
        btnEditP1_1.setOnClickListener { showEditNameDialog("Pemain 1A (Orang 1)", p1_1_Name) { p1_1_Name = it } }
        btnEditP1_2.setOnClickListener { showEditNameDialog("Pemain 1B (Orang 2)", p1_2_Name) { p1_2_Name = it } }
        btnEditP2_1.setOnClickListener { showEditNameDialog("Pemain 2A (Orang 1)", p2_1_Name) { p2_1_Name = it } }
        btnEditP2_2.setOnClickListener { showEditNameDialog("Pemain 2B (Orang 2)", p2_2_Name) { p2_2_Name = it } }

        tvP1_1_Name.setOnLongClickListener { showEditNameDialog("Pemain 1A (Orang 1)", p1_1_Name) { p1_1_Name = it }; true }
        tvP1_2_Name.setOnLongClickListener { showEditNameDialog("Pemain 1B (Orang 2)", p1_2_Name) { p1_2_Name = it }; true }
        tvP2_1_Name.setOnLongClickListener { showEditNameDialog("Pemain 2A (Orang 1)", p2_1_Name) { p2_1_Name = it }; true }
        tvP2_2_Name.setOnLongClickListener { showEditNameDialog("Pemain 2B (Orang 2)", p2_2_Name) { p2_2_Name = it }; true }

        // =========================================================================
        // PENAMBAHAN SKOR: HANYA DARI TOMBOL +1 ATAU TAP ANGKA SKOR BESAR
        // =========================================================================
        tvPlayer1Score.setOnClickListener { addScore(1) }
        btnP1Plus.setOnClickListener { addScore(1) }
        btnP1Minus.setOnClickListener { minusScore(1) }

        tvPlayer2Score.setOnClickListener { addScore(2) }
        btnP2Plus.setOnClickListener { addScore(2) }
        btnP2Minus.setOnClickListener { minusScore(2) }

        // Tukar Sisi Lapangan
        btnSwap.setOnClickListener { swapSides() }

        // Reset
        btnReset.setOnClickListener { showResetOptionsDialog() }
    }

    // Rotasi Bulutangkis: Orang Ke-1 -> Orang Ke-2 -> Pindah Bola!
    // (MURNI GANTI SERVIS, TIDAK MENAMBAH SKOR)
    private fun nextServerCycle() {
        vibrate()
        if (isDoublesMode) {
            when (activeServer) {
                1 -> {
                    activeServer = 2
                    Toast.makeText(this, "Orang Kedua Servis!", Toast.LENGTH_SHORT).show()
                }
                2 -> {
                    activeServer = 3
                    Toast.makeText(this, "PINDAH BOLA ke Tim 2 (Orang Ke-1)!", Toast.LENGTH_SHORT).show()
                }
                3 -> {
                    activeServer = 4
                    Toast.makeText(this, "Orang Kedua Servis!", Toast.LENGTH_SHORT).show()
                }
                4 -> {
                    activeServer = 1
                    Toast.makeText(this, "PINDAH BOLA ke Tim 1 (Orang Ke-1)!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Mode Tunggal: Langsung Pindah Bola
            activeServer = if (activeServer == 1 || activeServer == 2) 3 else 1
            Toast.makeText(this, "PINDAH BOLA!", Toast.LENGTH_SHORT).show()
        }
        updateUI()
    }

    private fun addScore(team: Int) {
        vibrate()
        if (team == 1) {
            if (player1Score < 30) {
                player1Score++
            }
        } else {
            if (player2Score < 30) {
                player2Score++
            }
        }

        updateUI()
        checkInterval()
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

    private fun checkInterval() {
        val intervalPoint = if (targetScore == 30) 15 else 11
        if (!hasNotifiedInterval && (player1Score == intervalPoint || player2Score == intervalPoint)) {
            hasNotifiedInterval = true
            AlertDialog.Builder(this)
                .setTitle("🏸 INTERVAL $intervalPoint POIN!")
                .setMessage("Poin telah mencapai $intervalPoint. Saatnya istirahat sejenak / tukar sisi lapangan.")
                .setPositiveButton("Tukar Lapangan Sekarang") { _, _ -> swapSides() }
                .setNegativeButton("Lanjut Main", null)
                .show()
        }
    }

    private fun checkGameWinner() {
        var isP1Won = false
        var isP2Won = false

        if (targetScore == 30) {
            // Sistem Murni 30 Poin
            isP1Won = (player1Score >= 30)
            isP2Won = (player2Score >= 30)
        } else {
            // Sistem 21 Poin (Deuce selisih 2 angka, max 30)
            isP1Won = (player1Score >= 21 && player1Score - player2Score >= 2) || (player1Score == 30)
            isP2Won = (player2Score >= 21 && player2Score - player1Score >= 2) || (player2Score == 30)
        }

        if (isP1Won) {
            val teamName = if (isDoublesMode) "$p1_1_Name & $p1_2_Name" else p1_1_Name
            showWinnerDialog(teamName, player1Score, player2Score, 1)
        } else if (isP2Won) {
            val teamName = if (isDoublesMode) "$p2_1_Name & $p2_2_Name" else p2_1_Name
            showWinnerDialog(teamName, player2Score, player1Score, 2)
        }
    }

    private fun showWinnerDialog(winnerTeam: String, winScore: Int, loseScore: Int, winnerId: Int) {
        AlertDialog.Builder(this)
            .setTitle("🏆 GAME $currentGame SELESAI!")
            .setMessage("$winnerTeam memenangkan Game $currentGame dengan skor $winScore - $loseScore!\n\nLanjut ke game berikutnya?")
            .setCancelable(false)
            .setPositiveButton("Lanjut Game Berikutnya") { _, _ ->
                if (winnerId == 1) player1Games++ else player2Games++
                currentGame++
                player1Score = 0
                player2Score = 0
                hasNotifiedInterval = false
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
                        hasNotifiedInterval = false
                        updateUI()
                    }
                    1 -> {
                        player1Score = 0
                        player2Score = 0
                        player1Games = 0
                        player2Games = 0
                        currentGame = 1
                        hasNotifiedInterval = false
                        activeServer = 1
                        updateUI()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateUI() {
        // Target Button Label
        btnTargetScore.text = "🎯 TARGET: $targetScore"

        // Mode Button Label
        btnToggleMode.text = if (isDoublesMode) "👥 2 ORANG" else "👤 1 ORANG"

        // Visibility Baris Orang Ke-2
        rowP1_2.visibility = if (isDoublesMode) View.VISIBLE else View.GONE
        rowP2_2.visibility = if (isDoublesMode) View.VISIBLE else View.GONE

        // Set Nama Pemain
        tvP1_1_Name.text = if (isDoublesMode) "1. $p1_1_Name" else p1_1_Name
        tvP1_2_Name.text = "2. $p1_2_Name"

        tvP2_1_Name.text = if (isDoublesMode) "1. $p2_1_Name" else p2_1_Name
        tvP2_2_Name.text = "2. $p2_2_Name"

        // Skor 0 - 30
        tvPlayer1Score.text = player1Score.toString()
        tvPlayer2Score.text = player2Score.toString()

        // Game Won
        tvP1Games.text = "Game: $player1Games"
        tvP2Games.text = "Game: $player2Games"

        // Tag Servis di Kartu (High Contrast Green jika aktif)
        tvP1_1_ServeTag.visibility = if (activeServer == 1) View.VISIBLE else View.INVISIBLE
        tvP1_2_ServeTag.visibility = if (isDoublesMode && activeServer == 2) View.VISIBLE else View.INVISIBLE

        tvP2_1_ServeTag.visibility = if (activeServer == 3) View.VISIBLE else View.INVISIBLE
        tvP2_2_ServeTag.visibility = if (isDoublesMode && activeServer == 4) View.VISIBLE else View.INVISIBLE

        // Banner Status Wasit
        val bannerText = when (activeServer) {
            1 -> if (isDoublesMode) "🎾 SERVIS: TIM 1 (ORANG KE-1)" else "🎾 SERVIS: TIM 1"
            2 -> "🎾 SERVIS: TIM 1 (ORANG KE-2)"
            3 -> if (isDoublesMode) "🎾 SERVIS: TIM 2 (ORANG KE-1)" else "🎾 SERVIS: TIM 2"
            4 -> "🎾 SERVIS: TIM 2 (ORANG KE-2)"
            else -> "🎾 SERVIS"
        }
        tvServeBanner.text = bannerText
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
