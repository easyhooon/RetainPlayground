package com.easyhooon.retainplayground

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.compose.serialization.serializers.SnapshotStateListSerializer
import com.easyhooon.retainplayground.navigation.AppRoute
import com.easyhooon.retainplayground.feature.postdetail.PostDetailScreen
import com.easyhooon.retainplayground.feature.postdetail.PostDetailUiEvent
import com.easyhooon.retainplayground.feature.postdetail.postDetailPresenter
import com.easyhooon.retainplayground.feature.postlist.PostListScreen
import com.easyhooon.retainplayground.feature.postlist.PostListUiEvent
import com.easyhooon.retainplayground.feature.postlist.postListPresenter
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
    val backStack = rememberNavBackStack(PostListRoute)

    // retain vs remember 차이점:
    // - remember: configuration change(화면 회전) 시 초기화됨
    // - retain: configuration change 시에도 상태 유지됨
    // 테스트: 좋아요 누른 후 화면 회전 → retain이므로 좋아요 수 유지!
    val likeCounts: SnapshotStateMap<Long, Int> = retain {
        Log.d("PostApp", "Creating likeCounts map (retain)")
        mutableStateMapOf()
    }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        entryProvider = entryProvider {
            entry<PostListRoute> {
                val (uiState, _) = postListPresenter()
                PostListScreen(
                    uiState = uiState,
                    likeCounts = likeCounts,
                    onEvent = { event ->
                        when (event) {
                            is PostListUiEvent.OnPostClick -> {
                                backStack.add(PostDetailRoute(event.postId))
                            }
                        }
                    }
                )
            }

            entry<PostDetailRoute> {
                val postId = it.postId
                val likeCount = likeCounts[postId] ?: 0
                val uiState = postDetailPresenter(postId, likeCount)

                PostDetailScreen(
                    uiState = uiState,
                    onEvent = { event ->
                        when (event) {
                            PostDetailUiEvent.OnBackClick -> {
                                backStack.removeLastOrNull()
                            }
                            PostDetailUiEvent.OnLikeClick -> {
                                likeCounts[postId] = (likeCounts[postId] ?: 0) + 1
                                Log.d("PostApp", "Like clicked for post $postId: ${likeCounts[postId]}")
                            }
                        }
                    }
                )
            }
        },
    )
}

/**
 * KMP: rememberSerializable + SnapshotStateListSerializer (NavKey 불필요)
 */
//@Composable
//fun PostAppKmp(modifier: Modifier = Modifier) {
//    val backStack: MutableList<AppRoute> =
//        rememberSerializable(serializer = SnapshotStateListSerializer()) {
//            mutableStateListOf(PostListRoute)
//        }
//
//    val likeCounts: SnapshotStateMap<Long, Int> = retain {
//        mutableStateMapOf()
//    }
//
//    NavDisplay(
//        backStack = backStack,
//        modifier = modifier,
//        entryProvider = entryProvider {
//            entry<PostListRoute> {
//                val (uiState, _) = postListPresenter()
//                PostListScreen(
//                    uiState = uiState,
//                    likeCounts = likeCounts,
//                    onEvent = { event ->
//                        when (event) {
//                            is PostListUiEvent.OnPostClick -> {
//                                backStack.add(PostDetailRoute(event.postId))
//                            }
//                        }
//                    }
//                )
//            }
//
//            entry<PostDetailRoute> {
//                val postId = it.postId
//                val likeCount = likeCounts[postId] ?: 0
//                val uiState = postDetailPresenter(postId, likeCount)
//
//                PostDetailScreen(
//                    uiState = uiState,
//                    onEvent = { event ->
//                        when (event) {
//                            PostDetailUiEvent.OnBackClick -> {
//                                backStack.removeLastOrNull()
//                            }
//                            PostDetailUiEvent.OnLikeClick -> {
//                                likeCounts[postId] = (likeCounts[postId] ?: 0) + 1
//                            }
//                        }
//                    }
//                )
//            }
//        },
//    )
//}
