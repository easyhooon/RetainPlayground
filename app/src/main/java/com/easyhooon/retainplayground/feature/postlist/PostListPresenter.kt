package com.easyhooon.retainplayground.feature.postlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.easyhooon.retainplayground.model.Post
import com.easyhooon.retainplayground.model.samplePosts

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
 * 순수 Composable Presenter 함수
 */
@Composable
fun postListPresenter(): PostListUiState {
    val posts = samplePosts
    return PostListUiState(posts = posts)
}
