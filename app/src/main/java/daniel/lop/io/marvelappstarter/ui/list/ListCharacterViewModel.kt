package daniel.lop.io.marvelappstarter.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import daniel.lop.io.marvelappstarter.data.model.character.CharacterModelResponse
import daniel.lop.io.marvelappstarter.repository.MarvelRepository
import daniel.lop.io.marvelappstarter.ui.state.ResourceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class ListCharacterViewModel (private val repository: MarvelRepository): ViewModel() {

    private val _list = MutableStateFlow<ResourceState<CharacterModelResponse>>(ResourceState.Loading())
    val list: StateFlow<ResourceState<CharacterModelResponse>> = _list

    init {
        fetch()
    }

    private fun fetch() = viewModelScope.launch {
        safeFetch()
    }

    private suspend fun safeFetch() {
        try {
           val response = repository.list()
           _list.value = handleResponse(response)
        }catch (t: Throwable) {
            when (t) {
                is IOException -> _list.value = ResourceState.Error("Erro de conexão com a internet")
                else -> _list.value = ResourceState.Error("Falha na conversão de dados")
            }
        }
    }

    private fun handleResponse(response: Response<CharacterModelResponse>): ResourceState<CharacterModelResponse> {
        if(response.isSuccessful){
            response.body()?.let {
                return ResourceState.Success(it)
            }
        }
        return ResourceState.Error(response.message())
    }
}