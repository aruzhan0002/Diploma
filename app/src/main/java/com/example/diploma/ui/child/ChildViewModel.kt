package com.example.diploma.ui.child

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diploma.data.remote.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChildViewModel : ViewModel() {
    init {
        println("🔥🔥🔥 ChildViewModel CREATED: $this")
    }

    // Храним все данные ребенка здесь
    private val _childData = MutableStateFlow(CreateChildRequest(
        name = "",
        age = 5,
        development_type = "",
        communication_style = "",
        understands_instructions = "",
        sensory_sensitivities = "",
        motor_difficulties = "",
        behavior_notices = "",
        motivators = "",
        interests = "",
        comfortable_duration = "5_min"
    ))
    val childData = _childData.asStateFlow()

    // Экран 3: имя и возраст
    fun updateChildInfo(name: String, age: Int) {
        println("🔥🔥🔥 updateChildInfo ВЫЗВАН!")
        println("🔥 name = $name")
        println("🔥 age = $age")

        _childData.value = _childData.value.copy(
            name = name,
            age = age
        )

        println("🔥 Данные сохранены: ${_childData.value}")
    }

    // Экран 4: тип развития
    fun updateDevelopmentType(type: String) {
        _childData.value = _childData.value.copy(development_type = type)
    }

    // Экран 5: коммуникация
    fun updateCommunication(communication: String, instruction: String) {
        _childData.value = _childData.value.copy(
            communication_style = communication,
            understands_instructions = instruction
        )
    }

    // Экран 6: сенсорика и моторика
    fun updateSensoryAndMotor(sensory: List<String>, motor: List<String>) {
        _childData.value = _childData.value.copy(
            sensory_sensitivities = sensory.joinToString(","),
            motor_difficulties = motor.joinToString(",")
        )
    }

    // Экран 7: поведение и мотивация
    fun updateBehaviorAndMotivation(behavior: List<String>, motivators: List<String>) {
        _childData.value = _childData.value.copy(
            behavior_notices = behavior.joinToString(","),
            motivators = motivators.joinToString(",")
        )
    }

    // Экран 8: интересы
    fun updateInterests(interests: List<String>) {
        _childData.value = _childData.value.copy(
            interests = interests.joinToString(",")
        )
    }

    // Экран 9: длительность
    fun updateDuration(duration: String) {
        _childData.value = _childData.value.copy(comfortable_duration = duration)
    }

    // Финальная отправка (на 9 экране)
    fun submitChildProfile(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.createChildProfile(_childData.value)
                if (response.isSuccessful) {
                    // Если нужно, можно получить данные из response.body()
                    val profileResponse = response.body()
                    println("Профиль ребенка создан: $profileResponse")
                    onSuccess()
                } else {
                    // Получаем текст ошибки из response.errorBody()
                    val errorBody = response.errorBody()?.string()
                    onError("Ошибка ${response.code()}: $errorBody")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Ошибка сети")
            }
        }
    }
}