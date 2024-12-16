package com.dicoding.picodiploma.loginwithanimation.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.api.ListStoryItem
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
class MainViewModel(private val repository: UserRepository) : ViewModel() {
    private val _storiesFlow = MutableStateFlow<List<ListStoryItem>>(emptyList())
    val storiesFlow: StateFlow<List<ListStoryItem>> = _storiesFlow

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading = _isLoading

    // We use LiveData for session, which will provide the token dynamically
    val token: LiveData<String> = getSessionToken()

    val stories: LiveData<PagingData<ListStoryItem>> = token.switchMap { token ->
        liveData {
            if (token.isNotEmpty()) {
                emitSource(repository.getPagingStories(token).cachedIn(viewModelScope))
            }
        }
    }

    init {
        getSession()
    }

    // Retrieve session data to get the token
    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    // Fetch the token from session
    private fun getSessionToken(): LiveData<String> {
        val result = MutableLiveData<String>()
        viewModelScope.launch {
            repository.getSession().collect { user ->
                result.postValue(user.token)  // Update the token from UserModel session
            }
        }
        return result
    }

    // Logout user and clear session
    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}
