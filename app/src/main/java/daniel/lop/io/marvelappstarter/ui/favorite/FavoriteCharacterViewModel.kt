package daniel.lop.io.marvelappstarter.ui.favorite

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import daniel.lop.io.marvelappstarter.data.model.character.CharacterModel
import daniel.lop.io.marvelappstarter.repository.MarvelRepository
import daniel.lop.io.marvelappstarter.ui.state.ResourceState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavoriteCharacterViewModel(private val repository: MarvelRepository):ViewModel() {

    private val _favorites = MutableLiveData<ResourceState<List<CharacterModel>>>(ResourceState.Empty())
    val favorites: LiveData<ResourceState<List<CharacterModel>>> = _favorites

    init {
        fetch()
    }

    private fun fetch() = viewModelScope.launch{
        repository.getAll().collectLatest { character ->
            if(character.isNullOrEmpty()){
                _favorites.value = ResourceState.Empty()
            }else{
                _favorites.value = ResourceState.Success(character)
            }
        }
    }

    fun delete(character: CharacterModel) = viewModelScope.launch {
        repository.delete(character)
    }
}