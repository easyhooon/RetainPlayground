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
import com.easyhooon.retainplayground.data.PostRepository
import com.easyhooon.retainplayground.model.Post
import dev.zacsweers.metro.Inject

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
 * Composable Presenter (Metro DI + DroidKaigi 스타일)
 * - PostRepository: Metro가 자동 주입
 * - operator fun invoke(): 함수처럼 호출 가능
 * - EventFlow/EventEffect: 이벤트 처리
 * - retain/RetainedEffect: 상태 유지
 */
@Inject
class PostDetailPresenter(
    private val postRepository: PostRepository,
) {
    @Composable
    operator fun invoke(
        postId: Long,
        likeCount: Int,
        eventFlow: EventFlow<PostDetailUiEvent>,
        onBackClick: () -> Unit,
        onLikeClick: () -> Unit,
    ): PostDetailUiState {
        val post = postRepository.getPost(postId)

        // retain: 댓글 작성 중인 텍스트 - 화면 회전해도 유지됨
        var commentDraft by retain(postId) {
            mutableStateOf("")
        }

        // RetainedEffect: 백스택에서 완전히 제거될 때만 onRetire 실행
        RetainedEffect(postId) {
            Log.d("PostDetailPresenter", "RetainedEffect: 게시글 $postId 상세 화면 진입")
            onRetire {
                Log.d("PostDetailPresenter", "RetainedEffect onRetire: 게시글 $postId 상세 화면에서 완전히 제거됨")
            }
        }

        // EventEffect: 이벤트 구독 및 처리
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
}
