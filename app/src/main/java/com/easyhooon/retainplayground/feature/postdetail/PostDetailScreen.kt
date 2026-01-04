package com.easyhooon.retainplayground.feature.postdetail

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.easyhooon.retainplayground.model.Post
import com.easyhooon.retainplayground.model.samplePosts

/**
 * PostDetail의 UI 상태
 */
@Immutable
data class PostDetailUiState(
    val post: Post? = null,
    val viewCount: Int = 0, // 조회 횟수 (retain 효과 확인용)
)

/**
 * PostDetail의 UI 이벤트
 */
sealed interface PostDetailUiEvent {
    data object OnBackClick : PostDetailUiEvent
}

/**
 * 순수 Composable Presenter 함수
 * - postId를 key로 사용하여 다른 게시글에 대해 새로운 상태 생성
 * - retain을 사용하여 동일 게시글에 대해서는 상태 유지
 */
@Composable
fun postDetailPresenter(postId: Long): PostDetailUiState {
    // postId를 key로 사용하여 retain
    // 같은 postId로 돌아오면 기존 상태 유지
    val viewCount by retain(postId) {
        Log.d("PostDetailPresenter", "First view for post $postId")
        mutableIntStateOf(1)
    }

    // postId에 해당하는 게시글 조회
    val post = retain(postId) {
        Log.d("PostDetailPresenter", "Loading post $postId (retain)")
        // 실제 앱에서는 여기서 API 호출
        samplePosts.find { it.id == postId }
    }

    return PostDetailUiState(
        post = post,
        viewCount = viewCount,
    )
}

/**
 * PostDetail 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    uiState: PostDetailUiState,
    onEvent: (PostDetailUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("게시글 상세") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(PostDetailUiEvent.OnBackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            uiState.post?.let { post ->
                // retain 효과 표시 카드
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "조회 횟수: ${uiState.viewCount} (retain으로 유지됨)",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // 제목
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.headlineMedium,
                )

                // 작성자 정보
                Text(
                    text = "${post.author} | ${post.createdAt}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 본문
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyLarge,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 추가 설명
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "retain API 동작 확인",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = "1. 뒤로가기 후 다시 이 게시글로 돌아오면 조회 횟수가 유지됩니다.\n" +
                                "2. 화면을 회전해도 조회 횟수가 유지됩니다.\n" +
                                "3. 다른 게시글을 선택하면 새로운 조회 횟수가 시작됩니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } ?: run {
                Text(
                    text = "게시글을 찾을 수 없습니다.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}