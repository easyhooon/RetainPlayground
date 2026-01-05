package com.easyhooon.retainplayground.di

import com.easyhooon.retainplayground.data.PostRepository
import dev.zacsweers.metro.DependencyGraph

/**
 * Metro DependencyGraph
 * 앱 전체의 의존성 그래프 정의
 * - PostRepository: 데이터 레이어
 */
@DependencyGraph
interface AppGraph {
    val postRepository: PostRepository
}
