package com.easyhooon.retainplayground.data

import com.easyhooon.retainplayground.model.Post
import com.easyhooon.retainplayground.model.samplePosts
import dev.zacsweers.metro.Inject

/**
 * Post 데이터를 관리하는 Repository
 * Metro의 @Inject로 의존성 주입
 */
@Inject
class PostRepository {

    fun getPosts(): List<Post> {
        return samplePosts
    }

    fun getPost(postId: Long): Post? {
        return samplePosts.find { it.id == postId }
    }
}
