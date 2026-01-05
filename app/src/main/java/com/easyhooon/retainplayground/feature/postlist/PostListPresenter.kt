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
 * Composable Presenter (Metro Top-level Function Injection)
 * - @Inject: Metro가 이 함수를 DI 컨테이너에 등록
 * - PostRepository: Metro가 자동 주입
 * - 함수를 직접 호출하면 Metro가 의존성을 해결
 */
@Inject
@Composable
fun PostListPresenter(
    postRepository: PostRepository,
): PostListUiState {
    val posts = postRepository.getPosts()
    return PostListUiState(posts = posts)
}
