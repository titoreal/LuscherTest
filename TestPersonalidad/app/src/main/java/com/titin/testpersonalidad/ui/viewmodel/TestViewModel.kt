package com.titin.testpersonalidad.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.titin.testpersonalidad.data.repository.TestRepository
import com.titin.testpersonalidad.model.TestResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestViewModel @Inject constructor(
    private val repository: TestRepository
) : ViewModel() {

    private val _testResult = MutableLiveData<TestResult>()
    val testResult: LiveData<TestResult> = _testResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun generateResult(orderSelection: List<String>) {
        _isLoading.value = true

        viewModelScope.launch {
            repository.getTestResult(orderSelection)
                .onSuccess { result ->
                    _testResult.value = result
                    _isLoading.value = false
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Error desconocido"
                    _isLoading.value = false
                }
        }
    }
}