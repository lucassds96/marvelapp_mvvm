package daniel.lop.io.marvelappstarter.ui.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import daniel.lop.io.marvelappstarter.R
import daniel.lop.io.marvelappstarter.databinding.FragmentFavoriteCharacterBinding
import daniel.lop.io.marvelappstarter.ui.adapters.CharacterAdapter
import daniel.lop.io.marvelappstarter.ui.base.BaseFragment
import daniel.lop.io.marvelappstarter.ui.state.ResourceState
import daniel.lop.io.marvelappstarter.util.hide
import daniel.lop.io.marvelappstarter.util.show
import daniel.lop.io.marvelappstarter.util.toast
import org.koin.android.viewmodel.ext.android.viewModel

class FavoriteCharacterFragment: BaseFragment<FragmentFavoriteCharacterBinding, FavoriteCharacterViewModel>() {

    override val viewModel: FavoriteCharacterViewModel by viewModel()
    private val characterAdapter by lazy { CharacterAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        clickAdapter()
        observer()
    }

    private fun observer() {
        viewModel.favorites.observe( viewLifecycleOwner, Observer {
            when(it){
                is ResourceState.Success -> {
                    it.data?.let { character ->
                        binding.tvEmptyList.hide()
                        characterAdapter.characters = character.toList()
                    }
                }
                is ResourceState.Empty -> {
                    binding.tvEmptyList.show()
                }
                else -> {}
            }
        })
    }

    private fun itemTouchHelperCallback(): ItemTouchHelper.SimpleCallback {
        return object : ItemTouchHelper.SimpleCallback (0, ItemTouchHelper.LEFT
                or ItemTouchHelper.RIGHT ){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val character = characterAdapter.getCharacterPosition(viewHolder.adapterPosition)
                viewModel.delete(character).also {
                    toast(getString(R.string.message_delete_character))
                }
            }
        }
    }

    private fun clickAdapter() {
        characterAdapter.setOnCLickListener {
            val action = FavoriteCharacterFragmentDirections
                .actionFavoriteCharacterFragmentToDetailsCharacterFragment(it)
            findNavController().navigate(action)
        }
    }

    private fun setupRecyclerView() = with(binding){
        rvFavoriteCharacter.apply {
            adapter = characterAdapter
            layoutManager = LinearLayoutManager(context)
        }
        ItemTouchHelper(itemTouchHelperCallback()).attachToRecyclerView(rvFavoriteCharacter)
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentFavoriteCharacterBinding? =
        FragmentFavoriteCharacterBinding.inflate(inflater, container, false)
}