package com.easyhooon.retainplayground.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Android: NavKey 상속 (rememberNavBackStack 사용 시 필요)
 */
@Serializable
sealed interface AppRoute : NavKey

@Serializable
@SerialName("PostList")
data object PostListRoute : AppRoute

@Serializable
@SerialName("PostDetail")
data class PostDetailRoute(val postId: Long) : AppRoute

/**
 * KMP: NavKey 상속 없음 (rememberSerializable 사용 시)
 */
//@Serializable
//sealed interface AppRoute
//
//@Serializable
//@SerialName("PostList")
//data object PostListRoute : AppRoute
//
//@Serializable
//@SerialName("PostDetail")
//data class PostDetailRoute(val postId: Long) : AppRoute