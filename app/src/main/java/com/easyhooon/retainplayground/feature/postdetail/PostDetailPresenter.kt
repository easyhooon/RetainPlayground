package com.easyhooon.retainplayground.feature.postdetail

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import com.easyhooon.retainplayground.common.EventEffect
import com.easyhooon.retainplayground.common.EventFlow
import com.easyhooon.retainplayground.model.Post
import com.easyhooon.retainplayground.model.samplePosts

/**
 * PostDetail의 UI 상태
 */
@Immutable
data class PostDetailUiState(
    val post: Post? = null,
    val likeCount: Int = 0,
    val commentDraft: String = "",
)

/**
 * PostDetail의 UI 이벤트
 */
sealed interface PostDetailUiEvent {
    data object OnBackClick : PostDetailUiEvent
    data object OnLikeClick : PostDetailUiEvent
    data class OnCommentDraftChange(val text: String) : PostDetailUiEvent
}

/**
 * 순수 Composable Presenter 함수 (DroidKaigi 스타일)
 * - EventFlow: 이벤트를 받아서 처리
 * - EventEffect: 이벤트 구독 및 처리
 * - retain: 화면 회전해도 상태 유지
 * - RetainedEffect: 백스택에서 완전히 제거될 때만 정리
 */
@Composable
fun postDetailPresenter(
    postId: Long,
    likeCount: Int,
    eventFlow: EventFlow<PostDetailUiEvent>,
    onBackClick: () -> Unit,
    onLikeClick: () -> Unit,
): PostDetailUiState {
    val post = samplePosts.find { it.id == postId }

    // retain: 댓글 작성 중인 텍스트 - 화면 회전해도 유지됨
    // remember였다면 화면 회전 시 초기화됨
    var commentDraft by retain(postId) {
        mutableStateOf("")
    }

    // RetainedEffect: DisposableEffect와 달리 백스택에서 완전히 제거될 때만 onRetire 실행
    // 화면 회전 시에는 dispose되지 않음
    RetainedEffect(postId) {
        Log.d("PostDetailPresenter", "RetainedEffect: 게시글 $postId 상세 화면 진입")
        onRetire {
            Log.d("PostDetailPresenter", "RetainedEffect onRetire: 게시글 $postId 상세 화면에서 완전히 제거됨")
            // 여기서 리소스 정리 (예: 네트워크 요청 취소, 스트림 닫기 등)
        }
    }

    // EventEffect: 이벤트 구독 및 처리 (DroidKaigi 스타일)
    EventEffect(eventFlow) { event ->
        when (event) {
            is PostDetailUiEvent.OnBackClick -> onBackClick()
            is PostDetailUiEvent.OnLikeClick -> onLikeClick()
            is PostDetailUiEvent.OnCommentDraftChange -> {
                commentDraft = event.text
            }
        }
    }

    return PostDetailUiState(
        post = post,
        likeCount = likeCount,
        commentDraft = commentDraft,
    )
}
