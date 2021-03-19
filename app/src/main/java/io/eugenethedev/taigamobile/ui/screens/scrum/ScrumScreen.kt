package io.eugenethedev.taigamobile.ui.screens.scrum

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.navigate
import androidx.navigation.NavController
import io.eugenethedev.taigamobile.ui.theme.TaigaMobileTheme
import io.eugenethedev.taigamobile.R
import io.eugenethedev.taigamobile.domain.entities.Sprint
import io.eugenethedev.taigamobile.domain.entities.Status
import io.eugenethedev.taigamobile.domain.entities.CommonTask
import io.eugenethedev.taigamobile.ui.components.*
import io.eugenethedev.taigamobile.ui.screens.main.Routes
import io.eugenethedev.taigamobile.ui.theme.mainHorizontalScreenPadding
import io.eugenethedev.taigamobile.ui.utils.*
import java.text.SimpleDateFormat
import java.util.*

@ExperimentalAnimationApi
@Composable
fun ScrumScreen(
    navController: NavController,
    onError: @Composable (message: Int) -> Unit = {},
) {
    val viewModel: ScrumViewModel = viewModel()
    remember {
        viewModel.start()
        null
    }
    val statuses by viewModel.statuses.observeAsState()
    statuses?.subscribeOnError(onError)
    val stories by viewModel.stories.observeAsState()
    stories?.subscribeOnError(onError)
    val sprints by viewModel.sprints.observeAsState()
    sprints?.subscribeOnError(onError)
    val loadingStatusIds by viewModel.loadingStatusIds.observeAsState()
    val visibleStatusIds by viewModel.visibleStatusIds.observeAsState()

    ScrumScreenContent(
        projectName = viewModel.projectName,
        onTitleClick = {
            navController.navigate(Routes.projectsSelector)
            viewModel.reset()
        },
        statuses = statuses?.data.orEmpty(),
        commonTasks = stories?.data.orEmpty(),
        sprints = sprints?.data.orEmpty(),
        isStoriesLoading = statuses?.resultStatus == ResultStatus.LOADING,
        isSprintsLoading = sprints?.resultStatus == ResultStatus.LOADING,
        loadingStatusIds = loadingStatusIds.orEmpty(),
        loadStories = viewModel::loadStories,
        loadSprints = viewModel::loadSprints,
        visibleStatusIds = visibleStatusIds.orEmpty(),
        onStatusClick = viewModel::statusClick,
        navigateToBoard = {
            navController.navigate(Routes.sprint, Routes.Arguments.sprint to it)
        },
        navigateToTask = navController::navigateToTaskScreen
    )
}

@ExperimentalAnimationApi
@Composable
fun ScrumScreenContent(
    projectName: String,
    onTitleClick: () -> Unit = {},
    statuses: List<Status> = emptyList(),
    commonTasks: List<CommonTask> = emptyList(),
    sprints: List<Sprint> = emptyList(),
    isStoriesLoading: Boolean = false,
    isSprintsLoading: Boolean = false,
    loadingStatusIds: List<Long> = emptyList(),
    loadStories: (Status) -> Unit = {},
    loadSprints: () -> Unit = {},
    visibleStatusIds: List<Long> = emptyList(),
    onStatusClick: (Long) -> Unit = {},
    navigateToBoard: (Sprint) -> Unit = {},
    navigateToTask: NavigateToTask = { _, _, _, _ -> }
) = Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.Start
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickableUnindicated(onClick = onTitleClick)
            ) {
                Text(
                    text = projectName.takeIf { it.isNotEmpty() }
                        ?: stringResource(R.string.choose_project_title),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 250.dp)
                )
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_down),
                    contentDescription = null
                )
            }
        },
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 0.dp
    )

    if (projectName.isNotEmpty()) {

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {

            item {
                Text(
                    text = stringResource(R.string.backlog),
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(horizontal = mainHorizontalScreenPadding)
                )
            }

            if (isStoriesLoading) {
                item {
                    DotsLoader()
                }
            } else {
                CommonTasksList(
                    statuses = statuses,
                    commonTasks = commonTasks,
                    loadingStatusIds = loadingStatusIds,
                    visibleStatusIds = visibleStatusIds,
                    onStatusClick = onStatusClick,
                    loadData = loadStories,
                    navigateToTask = navigateToTask
                )
            }

            item {
                Spacer(Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.sprints_title),
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(horizontal = mainHorizontalScreenPadding)
                )
            }

            itemsIndexed(sprints) {  index, item ->
                SprintItem(
                    sprint = item,
                    navigateToBoard = navigateToBoard
                )

                if (index == sprints.lastIndex) {
                    SideEffect {
                        loadSprints()
                    }
                }
            }

            item {
                if (isSprintsLoading) {
                    DotsLoader()
                } else if (sprints.isEmpty()) {
                    NothingToSeeHereText()
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SprintItem(
    sprint: Sprint,
    navigateToBoard: (Sprint) -> Unit = {}
) = ContainerBox(clickEnabled = false) {
    val dateFormatter = remember { SimpleDateFormat.getDateInstance() }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.weight(0.7f)) {
            Text(
                text = sprint.name,
                style = MaterialTheme.typography.subtitle1
            )

            Text(
                stringResource(R.string.sprint_dates_template).format(
                    dateFormatter.format(sprint.start),
                    dateFormatter.format(sprint.finish)
                )
            )

            Row {
                Text(
                    text = stringResource(R.string.stories_count_template).format(sprint.storiesCount),
                    color = MaterialTheme.colors.primary,
                    style = MaterialTheme.typography.body2
                )

                Spacer(Modifier.width(6.dp))

                if (sprint.isClosed) {
                    Text(
                        text = stringResource(R.string.closed),
                        color = Color.Gray,
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }

        Button(
            onClick = { navigateToBoard(sprint) },
            modifier = Modifier.weight(0.3f),
            colors = buttonColors(
                backgroundColor = if (!sprint.isClosed) {
                    MaterialTheme.colors.primary
                } else {
                    MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                        .compositeOver(MaterialTheme.colors.surface)
                }
            )
        ) {
            Text(stringResource(R.string.taskboard))
        }
        
    }
}

@Preview(showBackground = true)
@Composable
fun SprintPreview() = TaigaMobileTheme {
    SprintItem(
        Sprint(
            id = 0L,
            name = "1 sprint",
            order = 0,
            start = Date(),
            finish = Date(),
            storiesCount = 4,
            isClosed = true
        )
    )
}

@ExperimentalAnimationApi
@Preview(showBackground = true)
@Composable
fun ScrumScreenPreview() = TaigaMobileTheme {
    ScrumScreenContent("Lol")
}