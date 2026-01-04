package com.easyhooon.retainplayground

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.easyhooon.retainplayground.feature.postdetail.PostDetailScreen
import com.easyhooon.retainplayground.feature.postdetail.PostDetailUiEvent
import com.easyhooon.retainplayground.feature.postdetail.postDetailPresenter
import com.easyhooon.retainplayground.feature.postlist.PostListScreen
import com.easyhooon.retainplayground.feature.postlist.PostListUiEvent
import com.easyhooon.retainplayground.feature.postlist.postListPresenter
import com.easyhooon.retainplayground.navigation.AppRoute
import com.easyhooon.retainplayground.navigation.PostDetailRoute
import com.easyhooon.retainplayground.navigation.PostListRoute
import com.easyhooon.retainplayground.ui.theme.RetainPlaygroundTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RetainPlaygroundTheme {
                PostApp()
            }
        }
    }
}

@Composable
fun PostApp(modifier: Modifier = Modifier) {
    // Navigation3 BackStack
    val backStack = remember { mutableStateListOf<AppRoute>(PostListRoute) }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = { key ->
            when (key) {
                is PostListRoute -> NavEntry(key) {
                    val uiState = postListPresenter()

                    PostListScreen(
                        uiState = uiState,
                        onEvent = { event ->
                            when (event) {
                                is PostListUiEvent.OnPostClick -> {
                                    backStack.add(PostDetailRoute(event.postId))
                                }
                            }
                        }
                    )
                }

                is PostDetailRoute -> NavEntry(key) {
                    val uiState = postDetailPresenter(key.postId)

                    PostDetailScreen(
                        uiState = uiState,
                        onEvent = { event ->
                            when (event) {
                                PostDetailUiEvent.OnBackClick -> {
                                    backStack.removeLastOrNull()
                                }
                            }
                        }
                    )
                }
            }
        }
    )
}
