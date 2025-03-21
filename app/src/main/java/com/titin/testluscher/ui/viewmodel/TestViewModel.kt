package com.titin.testluscher.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.titin.testluscher.data.repository.TestRepository
import com.titin.testluscher.model.TestResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestViewModel @Inject constructor(
    private val repository: TestRepository
) : ViewModel() {

    private val _testResult = MutableLiveData<TestResult?>()  // Permite valores nulos
    val testResult: LiveData<TestResult?> = _testResult  // Permite valores nulos

    private val _errorMessage = MutableLiveData<String?>()  // Permite valores nulos
    val errorMessage: LiveData<String?> = _errorMessage  // Permite valores nulos

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Añadir un estado para gestionar el flujo de la aplicación
    private val _testState = MutableLiveData<TestState>()
    val testState: LiveData<TestState> = _testState

    init {
        _testState.value = TestState.IDLE
    }

    fun generateResult(orderSelection: List<String>) {
        _isLoading.value = true

        viewModelScope.launch {
            repository.getTestResult(orderSelection)
                .onSuccess { result ->
                    _testResult.value = result
                    _testState.value = TestState.COMPLETED
                    _isLoading.value = false
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Error desconocido"
                    _isLoading.value = false
                }
        }
    }

    fun resetTest() {
        _testState.value = TestState.RESET
        _errorMessage.value = null
        _testResult.value = null
        _isLoading.value = false // Asegúrate de que el estado de carga también se reinicie
    }
}

// Enum para manejar el estado del test
enum class TestState {
    IDLE,       // Estado inicial
    RESET,      // Reiniciar el test
    COMPLETED   // Test completado
}