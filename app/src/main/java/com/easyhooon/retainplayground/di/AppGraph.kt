package com.easyhooon.retainplayground.di

import com.easyhooon.retainplayground.data.PostRepository
import com.easyhooon.retainplayground.feature.postdetail.PostDetailPresenter
import com.easyhooon.retainplayground.feature.postlist.PostListPresenter
import dev.zacsweers.metro.DependencyGraph

/**
 * Metro DependencyGraph
 * 앱 전체의 의존성 그래프 정의
 * - PostRepository: 데이터 레이어
 * - PostListPresenter, PostDetailPresenter: Composable Presenter
 */
@DependencyGraph
interface AppGraph {
    val postRepository: PostRepository
    val postListPresenter: PostListPresenter
    val postDetailPresenter: PostDetailPresenter
}
