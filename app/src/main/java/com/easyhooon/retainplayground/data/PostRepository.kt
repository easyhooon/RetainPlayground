package com.easyhooon.retainplayground.data

import com.easyhooon.retainplayground.model.Post
import com.easyhooon.retainplayground.model.samplePosts
import dev.zacsweers.metro.Inject

/**
 * Post 데이터를 관리하는 Repository Interface
 */
interface PostRepository {
    fun getPosts(): List<Post>
    fun getPost(postId: Long): Post?
}

/**
 * PostRepository 구현체
 * Metro의 @Inject로 의존성 주입
 */
@Inject
class DefaultPostRepository : PostRepository {

    override fun getPosts(): List<Post> {
        return samplePosts
    }

    override fun getPost(postId: Long): Post? {
        return samplePosts.find { it.id == postId }
    }
}
