package com.easyhooon.retainplayground

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.easyhooon.retainplayground.common.rememberEventFlow
import com.easyhooon.retainplayground.di.AppGraph
import dev.zacsweers.metro.createGraph
import com.easyhooon.retainplayground.feature.postdetail.PostDetailPresenter
import com.easyhooon.retainplayground.feature.postdetail.PostDetailScreen
import com.easyhooon.retainplayground.feature.postdetail.PostDetailUiEvent
import com.easyhooon.retainplayground.feature.postlist.PostListPresenter
import com.easyhooon.retainplayground.feature.postlist.PostListScreen
import com.easyhooon.retainplayground.feature.postlist.PostListUiEvent
import com.easyhooon.retainplayground.navigation.PostDetailRoute
import com.easyhooon.retainplayground.navigation.PostListRoute
import com.easyhooon.retainplayground.ui.theme.RetainPlaygroundTheme
import kotlinx.coroutines.launch

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
    // Metro DependencyGraph 생성 (앱 전체에서 싱글톤으로 사용)
    val graph = retain { createGraph<AppGraph>() }

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
                // Metro Top-level Function Injection: Presenter를 직접 호출
                val uiState = PostListPresenter(
                    postRepository = graph.postRepository
                )
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

                // EventFlow: 이벤트를 presenter로 전달하는 채널
                val eventFlow = rememberEventFlow<PostDetailUiEvent>()
                val scope = rememberCoroutineScope()

                // Metro Top-level Function Injection: Presenter를 직접 호출 (@Assisted 파라미터 전달)
                val uiState = PostDetailPresenter(
                    postRepository = graph.postRepository,
                    postId = postId,
                    likeCount = likeCount,
                    eventFlow = eventFlow,
                    onBackClick = { backStack.removeLastOrNull() },
                    onLikeClick = {
                        likeCounts[postId] = (likeCounts[postId] ?: 0) + 1
                        Log.d("PostApp", "Like clicked for post $postId: ${likeCounts[postId]}")
                    },
                )

                PostDetailScreen(
                    uiState = uiState,
                    onEvent = { event -> scope.launch { eventFlow.emit(event) } },
                )
            }
        },
    )
}

/**
 * KMP: rememberSerializable + SnapshotStateListSerializer (NavKey 불필요)
 * Metro DI 사용 버전
 */
//@Composable
//fun PostAppKmp(modifier: Modifier = Modifier) {
//    val graph = retain { createGraph<AppGraph>() }
//
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
//                val uiState = graph.postListPresenter()
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
//
//                val eventFlow = rememberEventFlow<PostDetailUiEvent>()
//                val scope = rememberCoroutineScope()
//
//                val uiState = graph.postDetailPresenter(
//                    postId = postId,
//                    likeCount = likeCount,
//                    eventFlow = eventFlow,
//                    onBackClick = { backStack.removeLastOrNull() },
//                    onLikeClick = { likeCounts[postId] = (likeCounts[postId] ?: 0) + 1 },
//                )
//
//                PostDetailScreen(
//                    uiState = uiState,
//                    onEvent = { event -> scope.launch { eventFlow.emit(event) } },
//                )
//            }
//        },
//    )
//}
