package com.easyhooon.retainplayground.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute

@Serializable
@SerialName("PostList")
data object PostListRoute : AppRoute

@Serializable
@SerialName("PostDetail")
data class PostDetailRoute(val postId: Long) : AppRoute