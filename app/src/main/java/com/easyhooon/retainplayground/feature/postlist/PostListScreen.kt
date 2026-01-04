package com.easyhooon.retainplayground.feature.postlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
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
fun postListPresenter(): Pair<PostListUiState, Unit> {
    val posts = samplePosts
    return PostListUiState(posts = posts) to Unit
}

/**
 * PostList 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListScreen(
    uiState: PostListUiState,
    likeCounts: SnapshotStateMap<Long, Int>,
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
                            text = "상세에서 좋아요 누르고 돌아와도 유지됨",
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
                    likeCount = likeCounts[post.id] ?: 0,
                    onClick = { onEvent(PostListUiEvent.OnPostClick(post.id)) }
                )
            }
        }
    }
}

@Composable
private fun PostItem(
    post: Post,
    likeCount: Int,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${post.author} | ${post.createdAt}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
                if (likeCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Text(
                            text = "$likeCount",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}
