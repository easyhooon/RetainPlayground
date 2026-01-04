package com.easyhooon.retainplayground.feature.postdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
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
    val likeCount: Int = 0,
)

/**
 * PostDetail의 UI 이벤트
 */
sealed interface PostDetailUiEvent {
    data object OnBackClick : PostDetailUiEvent
    data object OnLikeClick : PostDetailUiEvent
}

/**
 * 순수 Composable Presenter 함수
 */
@Composable
fun postDetailPresenter(postId: Long, likeCount: Int): PostDetailUiState {
    val post = samplePosts.find { it.id == postId }

    return PostDetailUiState(
        post = post,
        likeCount = likeCount,
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
                // 좋아요 버튼 카드
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "좋아요: ${uiState.likeCount}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "뒤로가기 후 다시 와도 유지됨 (retain)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        FilledTonalButton(
                            onClick = { onEvent(PostDetailUiEvent.OnLikeClick) }
                        ) {
                            Icon(
                                imageVector = if (uiState.likeCount > 0) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = " +1",
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
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

                // 설명
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
                            text = "1. 좋아요 버튼을 여러 번 눌러보세요.\n" +
                                "2. 뒤로가기로 목록으로 돌아가세요.\n" +
                                "3. 같은 게시글을 다시 클릭하면 좋아요가 유지됩니다.\n" +
                                "4. 화면 회전해도 유지됩니다.",
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
