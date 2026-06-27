package com.example.scancaptureapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scancaptureapp.domain.model.ScanHistoryItem
import com.example.scancaptureapp.domain.usecase.GetScanByIdUseCase
import com.example.scancaptureapp.domain.usecase.GetScanHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    getScanHistoryUseCase: GetScanHistoryUseCase,
    private val getScanByIdUseCase: GetScanByIdUseCase
) : ViewModel() {

    val historyItems: StateFlow<List<ScanHistoryItem>> = getScanHistoryUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedScan = MutableStateFlow<ScanHistoryItem?>(null)
    val selectedScan: StateFlow<ScanHistoryItem?> = _selectedScan.asStateFlow()

    fun loadScanDetail(id: Long) {
        viewModelScope.launch {
            _selectedScan.value = getScanByIdUseCase(id)
        }
    }

    fun clearSelection() {
        _selectedScan.value = null
    }
}
