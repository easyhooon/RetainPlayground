package com.easyhooon.retainplayground.feature.postlist

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.easyhooon.retainplayground.model.Post
import com.easyhooon.retainplayground.model.samplePosts

/**
 * PostList의 UI 상태
 */
@Immutable
data class PostListUiState(
    val posts: List<Post> = emptyList(),
    val loadCount: Int = 0, // 데이터 로드 횟수 (retain 효과 확인용)
)

/**
 * PostList의 UI 이벤트
 */
sealed interface PostListUiEvent {
    data class OnPostClick(val postId: Long) : PostListUiEvent
}

/**
 * 순수 Composable Presenter 함수
 * - ViewModel 없이 순수 Composable 함수로 상태를 관리
 * - retain을 사용하여 Navigation 백스택에서 제거되어도 상태 유지
 */
@Composable
fun postListPresenter(): PostListUiState {
    // retain을 사용하여 로드 카운트 유지
    // 화면 회전이나 네비게이션으로 돌아와도 값이 유지됨
    val loadCount by retain {
        Log.d("PostListPresenter", "Initial load - creating mutableIntStateOf")
        mutableIntStateOf(1)
    }

    // retain을 사용하여 posts 데이터 캐싱
    // 실제 앱에서는 여기서 API 호출 결과를 캐싱할 수 있음
    val posts = retain {
        Log.d("PostListPresenter", "Loading posts data (retain)")
        // 여기서 네트워크 요청이나 DB 조회를 시뮬레이션
        samplePosts
    }

    return PostListUiState(
        posts = posts,
        loadCount = loadCount,
    )
}

/**
 * PostList 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListScreen(
    uiState: PostListUiState,
    onEvent: (PostListUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("게시글 목록")
                        Text(
                            text = "로드 횟수: ${uiState.loadCount} (retain으로 유지됨)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = uiState.posts,
                key = { it.id }
            ) { post ->
                PostItem(
                    post = post,
                    onClick = { onEvent(PostListUiEvent.OnPostClick(post.id)) }
                )
            }
        }
    }
}

@Composable
private fun PostItem(
    post: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${post.author} | ${post.createdAt}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}