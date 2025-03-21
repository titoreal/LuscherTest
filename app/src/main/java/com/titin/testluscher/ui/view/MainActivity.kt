package com.titin.testluscher.ui.view

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.titin.testluscher.databinding.ActivityMainBinding
import com.titin.testluscher.databinding.LayoutColorSelectionBinding
import com.titin.testluscher.databinding.LayoutResultsBinding
import com.titin.testluscher.databinding.LayoutTestInstructionsBinding
import com.titin.testluscher.databinding.LayoutTestIntroBinding
import com.titin.testluscher.ui.viewmodel.TestViewModel
import com.titin.testluscher.ui.viewmodel.TestState
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: TestViewModel by viewModels()

    // Bindings para los layouts incluidos
    private lateinit var introBinding: LayoutTestIntroBinding
    private lateinit var instructionsBinding: LayoutTestInstructionsBinding
    private lateinit var selection1Binding: LayoutColorSelectionBinding
    private lateinit var selection2Binding: LayoutColorSelectionBinding
    private lateinit var resultsBinding: LayoutResultsBinding

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

        // Inicializar bindings de layouts incluidos
        initializeLayoutBindings()
        initializeCardMappings()
        setupListeners()
        observeViewModel()

        // Configurar visibilidad inicial
        setupInitialVisibility()
    }

    private fun setupInitialVisibility() {
        // Configurar visibilidad inicial usando la vista raíz de cada binding
        introBinding.root.visibility = View.VISIBLE
        instructionsBinding.root.visibility = View.GONE
        selection1Binding.root.visibility = View.GONE
        selection2Binding.root.visibility = View.GONE
        resultsBinding.root.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }

    private fun initializeLayoutBindings() {
        // La forma correcta de obtener el binding para layouts incluidos
        introBinding = binding.introLayout
        instructionsBinding = binding.instructionsLayout
        selection1Binding = binding.selection1
        selection2Binding = binding.selection2
        resultsBinding = binding.resultsLayout
    }

    private fun initializeCardMappings() {
        // Mapear CardViews del primer selector
        firstSelectionCards.apply {
            put("A", selection1Binding.cardA)
            put("B", selection1Binding.cardB)
            put("C", selection1Binding.cardC)
            put("D", selection1Binding.cardD)
            put("E", selection1Binding.cardE)
            put("F", selection1Binding.cardF)
            put("G", selection1Binding.cardG)
            put("H", selection1Binding.cardH)
        }

        // Mapear CardViews del segundo selector
        secondSelectionCards.apply {
            put("A", selection2Binding.cardA)
            put("B", selection2Binding.cardB)
            put("C", selection2Binding.cardC)
            put("D", selection2Binding.cardD)
            put("E", selection2Binding.cardE)
            put("F", selection2Binding.cardF)
            put("G", selection2Binding.cardG)
            put("H", selection2Binding.cardH)
        }
    }

    private fun setupListeners() {
        // Configurar listeners para el layout de introducción
        introBinding.start.setOnClickListener { startTest() }

        // Configurar listeners para el layout de resultados
        resultsBinding.result.setOnClickListener {
            viewModel.generateResult(orderSelection)
        }
        resultsBinding.retryButton.setOnClickListener { resetTest() }
        resultsBinding.exitButton.setOnClickListener { finishApp() }
    }

    private fun observeViewModel() {
        // Observar el resultado del test
        viewModel.testResult.observe(this) { result ->
            resultsBinding.resultLabel.visibility = View.VISIBLE
            resultsBinding.rpLabel.visibility = View.VISIBLE

            resultsBinding.rp.text = result?.personality ?: "No se pudo obtener la personalidad"
            resultsBinding.rp.visibility = View.VISIBLE

            resultsBinding.rxLabel.visibility = View.VISIBLE
            resultsBinding.rx.text = result?.currentState ?: "No se pudo obtener el estado actual"
            resultsBinding.rx.visibility = View.VISIBLE

            // Mostrar botones de acción
            resultsBinding.actionsContainer.visibility = View.VISIBLE
        }

        // Observar mensajes de error
        viewModel.errorMessage.observe(this) { error ->
            resultsBinding.resultLabel.visibility = View.VISIBLE
            resultsBinding.rpLabel.visibility = View.VISIBLE

            resultsBinding.rp.text = error ?: "Ocurrió un error desconocido"
            resultsBinding.rp.visibility = View.VISIBLE

            // Mostrar botones de acción incluso en caso de error
            resultsBinding.actionsContainer.visibility = View.VISIBLE
        }

        // Observar estado de carga
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observar estado del test para manejar el reinicio
        viewModel.testState.observe(this) { state ->
            when (state) {
                TestState.RESET -> {
                    // Reiniciar UI al estado inicial para comenzar un nuevo test
                    startTest()
                }
                else -> {} // No se requiere acción para otros estados
            }
        }
    }

    private fun startTest() {
        // Ocultar layout de introducción
        introBinding.root.visibility = View.GONE

        // Mostrar instrucciones
        instructionsBinding.root.visibility = View.VISIBLE
        instructionsBinding.instructionsText.apply {
            text = "Selecciona los colores en orden de preferencia"
            visibility = View.VISIBLE
        }

        // Mostrar primera selección
        selection1Binding.root.visibility = View.VISIBLE
        selection2Binding.root.visibility = View.GONE
        resultsBinding.root.visibility = View.GONE

        // Reiniciar contadores y selecciones
        counter1 = 0
        counter2 = 0
        orderSelection.clear()

        // Restablecer visibilidad de todas las tarjetas
        firstSelectionCards.values.forEach { it.visibility = View.VISIBLE }
        secondSelectionCards.values.forEach { it.visibility = View.VISIBLE }

        val shuffledFirstDeck = shuffleDeck()
        applyColorsToCards(shuffledFirstDeck, firstSelectionCards)

        // Configurar listeners para las tarjetas de la primera selección
        firstSelectionCards.forEach { (key, cardView) ->
            cardView.setOnClickListener { handleFirstSelectionClick(key) }
        }
    }

    private fun resetTest() {
        // Cancelar operaciones pendientes
        viewModel.resetTest()

        // Limpiar estructuras de datos
        orderSelection.clear()
        counter1 = 0
        counter2 = 0

        // Resetear todas las tarjetas
        firstSelectionCards.values.forEach { it.visibility = View.VISIBLE }
        secondSelectionCards.values.forEach { it.visibility = View.VISIBLE }

        // Restablecer UI completamente
        binding.apply {
            // Ocultar todo
            introBinding.root.visibility = View.GONE
            instructionsBinding.root.visibility = View.GONE
            selection1Binding.root.visibility = View.GONE
            selection2Binding.root.visibility = View.GONE
            resultsBinding.root.visibility = View.GONE
            progressBar.visibility = View.GONE

            // Pequeño retraso para asegurar que el ciclo de renderizado se complete
            mainContainer.postDelayed({
                // Mostrar solo los elementos iniciales
                introBinding.root.visibility = View.VISIBLE
            }, 100)
        }
    }

    private fun finishApp() {
        finish()
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
            orderSelection.add(cardKey) // Agregar selección a la lista

            if (counter1 < 7) {
                counter1++
            } else {
                counter1 = 0
                transitionToSecondSelection()
            }
        }
    }

    private fun transitionToSecondSelection() {
        // Limpiar lista para comenzar con segundas selecciones
        orderSelection.clear()

        // Ocultar primera selección, mostrar segunda selección
        selection1Binding.root.visibility = View.GONE
        selection2Binding.root.visibility = View.VISIBLE

        // Actualizar instrucciones
        instructionsBinding.instructionsText.text =
            "Selecciona nuevamente los colores en orden de preferencia"

        val shuffledSecondDeck = shuffleDeck()
        applyColorsToCards(shuffledSecondDeck, secondSelectionCards)

        // Configurar listeners para las tarjetas de la segunda selección
        secondSelectionCards.forEach { (key, cardView) ->
            cardView.setOnClickListener { handleSecondSelectionClick(key) }
        }
    }

    private fun handleSecondSelectionClick(cardKey: String) {
        secondSelectionCards[cardKey]?.apply {
            visibility = View.INVISIBLE
            orderSelection.add(cardKey)

            if (counter2 < 7) {
                counter2++
            } else {
                finishTest()
            }
        }
    }

    private fun finishTest() {
        selection2Binding.root.visibility = View.GONE
        instructionsBinding.root.visibility = View.GONE

        // Mostrar layout de resultados y botón para generar resultados
        resultsBinding.root.visibility = View.VISIBLE
        resultsBinding.result.visibility = View.VISIBLE

        // Asegurar que los elementos de resultado estén ocultos hasta que se genere el resultado
        resultsBinding.resultLabel.visibility = View.GONE
        resultsBinding.rpLabel.visibility = View.GONE
        resultsBinding.rp.visibility = View.GONE
        resultsBinding.rxLabel.visibility = View.GONE
        resultsBinding.rx.visibility = View.GONE
        resultsBinding.actionsContainer.visibility = View.GONE
    }
}