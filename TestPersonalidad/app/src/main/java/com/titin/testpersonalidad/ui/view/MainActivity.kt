package com.titin.testpersonalidad.ui.view

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.titin.testpersonalidad.databinding.ActivityMainBinding
import com.titin.testpersonalidad.ui.viewmodel.TestViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.titin.testpersonalidad.model.TestResult


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: TestViewModel by viewModels()

    private val orderSelection = mutableListOf<String>()
    private val firstSelectionCards = mutableMapOf<String, CardView>()
    private val secondSelectionCards = mutableMapOf<String, CardView>()

    private var counter1 = 0
    private var counter2 = 0

    private val colorMap = mapOf(
        "A" to Color.parseColor("#757166"),
        "B" to Color.parseColor("#1b1b5b"),
        "C" to Color.parseColor("#254842"),
        "D" to Color.parseColor("#df4523"),
        "E" to Color.parseColor("#f3d93a"),
        "F" to Color.parseColor("#9c234e"),
        "G" to Color.parseColor("#af5e29"),
        "H" to Color.BLACK
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeCardMappings()
        setupListeners()
        observeViewModel()
    }

    private fun initializeCardMappings() {
        // Mapear los CardView a su id correspondiente
        firstSelectionCards.apply {
            put("A", binding.cardA1)
            put("B", binding.cardB1)
            put("C", binding.cardC1)
            put("D", binding.cardD1)
            put("E", binding.cardE1)
            put("F", binding.cardF1)
            put("G", binding.cardG1)
            put("H", binding.cardH1)
        }

        secondSelectionCards.apply {
            put("A", binding.cardA2)
            put("B", binding.cardB2)
            put("C", binding.cardC2)
            put("D", binding.cardD2)
            put("E", binding.cardE2)
            put("F", binding.cardF2)
            put("G", binding.cardG2)
            put("H", binding.cardH2)
        }
    }

    private fun setupListeners() {
        binding.start.setOnClickListener { startTest() }
        binding.result.setOnClickListener { viewModel.generateResult(orderSelection) }
    }

    private fun observeViewModel() {
        viewModel.testResult.observe(this) { result ->
            binding.apply {
                resultLabel.visibility = View.VISIBLE
                rpLabel.visibility = View.VISIBLE
                rp.text = result.personality
                rp.visibility = View.VISIBLE
                rxLabel.visibility = View.VISIBLE
                rx.text = result.currentState
                rx.visibility = View.VISIBLE
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            binding.apply {
                resultLabel.visibility = View.VISIBLE
                rpLabel.visibility = View.VISIBLE
                rp.text = error
                rp.visibility = View.VISIBLE
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun startTest() {
        binding.apply {
            start.visibility = View.GONE
            descriptionText.visibility = View.GONE
            instructionsText.visibility = View.VISIBLE

            // Mostrar primera selección
            selection1Layout.visibility = View.VISIBLE
            selection1LayoutRow2.visibility = View.VISIBLE
            selection1Layout2.visibility = View.VISIBLE
            selection1Layout2Row2.visibility = View.VISIBLE
        }

        val shuffledFirstDeck = shuffleDeck()
        applyColorsToCards(shuffledFirstDeck, firstSelectionCards)

        firstSelectionCards.forEach { (key, cardView) ->
            cardView.setOnClickListener { handleFirstSelectionClick(key) }
        }
    }

    private fun shuffleDeck(): List<String> {
        return listOf("A", "B", "C", "D", "E", "F", "G", "H").shuffled()
    }

    private fun applyColorsToCards(shuffledDeck: List<String>, cards: Map<String, CardView>) {
        shuffledDeck.forEachIndexed { index, cardKey ->
            cards.values.elementAtOrNull(index)?.apply {
                setCardBackgroundColor(colorMap[cardKey] ?: Color.GRAY)
                tag = cardKey
            }
        }
    }

    private fun handleFirstSelectionClick(cardKey: String) {
        firstSelectionCards[cardKey]?.apply {
            visibility = View.INVISIBLE
            orderSelection.add(cardKey) // Añadir selección a la lista

            if (counter1 < 7) {
                counter1++
            } else {
                counter1 = 0
                transitionToSecondSelection()
            }
        }
    }

    private fun transitionToSecondSelection() {
        // Limpiar la lista para comenzar con las segundas selecciones
        orderSelection.clear()

        binding.apply {
            selection1Layout.visibility = View.GONE
            selection1LayoutRow2.visibility = View.GONE
            selection1Layout2.visibility = View.GONE
            selection1Layout2Row2.visibility = View.GONE

            instructionsText.text = "Selecciona nuevamente los colores en orden de preferencia"

            val shuffledSecondDeck = shuffleDeck()
            applyColorsToCards(shuffledSecondDeck, secondSelectionCards)

            selection2Layout.visibility = View.VISIBLE
            selection2LayoutRow2.visibility = View.VISIBLE
            selection2Layout2.visibility = View.VISIBLE
            selection2Layout2Row2.visibility = View.VISIBLE
        }

        secondSelectionCards.values.forEach { cardView ->
            cardView.setOnClickListener { handleSecondSelectionClick(cardView) }
        }
    }

    private fun handleSecondSelectionClick(cardView: CardView) {
        (cardView.tag as? String)?.let { cardKey ->
            orderSelection.add(cardKey)
            cardView.visibility = View.INVISIBLE

            if (counter2 < 7) {
                counter2++
            } else {
                finishTest()
            }
        }
    }

    private fun finishTest() {
        binding.apply {
            selection2Layout.visibility = View.GONE
            selection2LayoutRow2.visibility = View.GONE
            selection2Layout2.visibility = View.GONE
            selection2Layout2Row2.visibility = View.GONE
            instructionsText.visibility = View.GONE
            result.visibility = View.VISIBLE
        }
    }
}