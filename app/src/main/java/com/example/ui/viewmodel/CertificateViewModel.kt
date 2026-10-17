package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Certificate
import com.example.data.model.CertificateCategories
import com.example.data.model.UserProfile
import com.example.data.repository.CertificateRepository
import com.example.util.SampleData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class CertificateStats(
    val totalCount: Int = 0,
    val uniqueOrganizationsCount: Int = 0,
    val activeCount: Int = 0,
    val topSkills: List<String> = emptyList()
)

class CertificateViewModel(
    private val repository: CertificateRepository,
    private val userId: String
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(CertificateCategories.ALL)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage = _actionMessage.asStateFlow()

    val rawCertificates: StateFlow<UiState<List<Certificate>>> = repository.observeCertificates()
        .map<List<Certificate>, UiState<List<Certificate>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to load certificates")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val userProfile: StateFlow<UserProfile?> = repository.observeUserProfile()
        .catch { emit(null) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )

    val filteredCertificates: StateFlow<List<Certificate>> = combine(
        rawCertificates,
        searchQuery,
        selectedCategory
    ) { state, query, category ->
        if (state !is UiState.Success) return@combine emptyList()
        val all = state.data
        all.filter { cert ->
            val matchesCategory = (category == CertificateCategories.ALL) ||
                    cert.category.equals(category, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    cert.title.contains(query, ignoreCase = true) ||
                    cert.organization.contains(query, ignoreCase = true) ||
                    cert.description.contains(query, ignoreCase = true) ||
                    cert.credentialId.contains(query, ignoreCase = true) ||
                    cert.skills.any { it.contains(query, ignoreCase = true) }

            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    val stats: StateFlow<CertificateStats> = rawCertificates.map { state ->
        if (state !is UiState.Success) return@map CertificateStats()
        val list = state.data
        val orgs = list.map { it.organization.trim() }.filter { it.isNotBlank() }.distinct().size
        val allSkills = list.flatMap { it.skills }.filter { it.isNotBlank() }
        val topSkills = allSkills.groupingBy { it }.eachCount().entries
            .sortedByDescending { it.value }
            .take(6)
            .map { it.key }

        CertificateStats(
            totalCount = list.size,
            uniqueOrganizationsCount = orgs,
            activeCount = list.size,
            topSkills = topSkills
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = CertificateStats()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    fun addCertificate(certificate: Certificate) {
        viewModelScope.launch {
            val result = repository.createCertificate(certificate.copy(userId = userId))
            if (result.isSuccess) {
                _actionMessage.value = "Certificate added successfully!"
            } else {
                _actionMessage.value = "Failed to add certificate: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun updateCertificate(certificate: Certificate) {
        viewModelScope.launch {
            val result = repository.updateCertificate(certificate.copy(userId = userId))
            if (result.isSuccess) {
                _actionMessage.value = "Certificate updated successfully!"
            } else {
                _actionMessage.value = "Failed to update certificate: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun deleteCertificate(certificateId: String) {
        viewModelScope.launch {
            val result = repository.deleteCertificate(certificateId)
            if (result.isSuccess) {
                _actionMessage.value = "Certificate deleted."
            } else {
                _actionMessage.value = "Failed to delete: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            val result = repository.saveUserProfile(profile.copy(userId = userId))
            if (result.isSuccess) {
                _actionMessage.value = "Profile updated for PDF export!"
            } else {
                _actionMessage.value = "Failed to update profile: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun populateStarterCertificates() {
        viewModelScope.launch {
            val sample = SampleData.getStarterCertificates(userId)
            for (cert in sample) {
                repository.createCertificate(cert)
            }
            _actionMessage.value = "Loaded ${sample.size} starter credentials!"
        }
    }
}
