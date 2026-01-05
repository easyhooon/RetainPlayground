package com.easyhooon.retainplayground.di

import com.easyhooon.retainplayground.data.PostRepository
import com.easyhooon.retainplayground.data.DefaultPostRepository
import com.easyhooon.retainplayground.feature.postdetail.PostDetailPresenter
import com.easyhooon.retainplayground.feature.postlist.PostListPresenter
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph

/**
 * Metro DependencyGraph
 * 앱 전체의 의존성 그래프 정의
 */
@DependencyGraph
interface AppGraph {
    val postListPresenter: PostListPresenter
    val postDetailPresenter: PostDetailPresenter

    /**
     * @Binds: Interface와 구현체 바인딩
     */
    @Binds
    fun bindPostRepository(impl: DefaultPostRepository): PostRepository
}
