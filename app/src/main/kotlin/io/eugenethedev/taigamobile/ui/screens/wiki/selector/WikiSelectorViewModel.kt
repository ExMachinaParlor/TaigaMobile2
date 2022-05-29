package io.eugenethedev.taigamobile.ui.screens.wiki.selector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.eugenethedev.taigamobile.TaigaApp
import io.eugenethedev.taigamobile.dagger.AppComponent
import io.eugenethedev.taigamobile.domain.entities.WikiLink
import io.eugenethedev.taigamobile.domain.entities.WikiPage
import io.eugenethedev.taigamobile.domain.repositories.IWikiRepository
import io.eugenethedev.taigamobile.state.Session
import io.eugenethedev.taigamobile.ui.utils.MutableResultFlow
import io.eugenethedev.taigamobile.ui.utils.loadOrError
import kotlinx.coroutines.launch
import javax.inject.Inject

class WikiSelectorViewModel(appComponent: AppComponent = TaigaApp.appComponent) : ViewModel() {

    @Inject
    lateinit var session: Session

    @Inject
    lateinit var wikiRepository: IWikiRepository

    val projectName by lazy { session.currentProjectName }

    val wikiPagesResult = MutableResultFlow<List<WikiPage>>()
    val wikiLinksResult = MutableResultFlow<List<WikiLink>>()

    init {
        appComponent.inject(this)
    }

    fun getWikiPage() = viewModelScope.launch {
        wikiPagesResult.loadOrError {
            val res = wikiRepository.getProjectWikiPages()
            res
        }
    }

    fun getWikiLinks() = viewModelScope.launch {
        wikiLinksResult.loadOrError {
            wikiRepository.getWikiLinks()
        }
    }
}