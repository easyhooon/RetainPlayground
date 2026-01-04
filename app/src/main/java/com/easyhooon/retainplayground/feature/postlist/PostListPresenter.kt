package com.easyhooon.retainplayground.feature.postlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.easyhooon.retainplayground.data.PostRepository
import com.easyhooon.retainplayground.model.Post
import dev.zacsweers.metro.Inject

/**
 * PostList의 UI 상태
 */
@Immutable
data class PostListUiState(
    val posts: List<Post> = emptyList(),
)

/**
 * PostList의 UI 이벤트
 */
sealed interface PostListUiEvent {
    data class OnPostClick(val postId: Long) : PostListUiEvent
}

/**
 * Composable Presenter (Metro DI)
 * - PostRepository는 Metro가 자동 주입
 * - operator fun invoke()로 함수처럼 호출 가능: graph.postListPresenter()
 */
@Inject
class PostListPresenter(
    private val postRepository: PostRepository,
) {
    @Composable
    operator fun invoke(): PostListUiState {
        val posts = postRepository.getPosts()
        return PostListUiState(posts = posts)
    }
}
