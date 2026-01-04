package com.easyhooon.retainplayground.model

data class Post(
    val id: Long,
    val title: String,
    val content: String,
    val author: String,
    val createdAt: String,
)

// 더미 데이터
val samplePosts = listOf(
    Post(
        id = 1,
        title = "Compose Retain API 소개",
        content = "retain API는 Compose Navigation에서 화면이 제거될 때 상태 손실 문제를 해결합니다. remember와 달리 Navigation 백스택에서 화면이 제거되어도 상태가 유지됩니다.",
        author = "Android Developer",
        createdAt = "2025-01-04"
    ),
    Post(
        id = 2,
        title = "Navigation3 시작하기",
        content = "Navigation 3는 타입 안전한 내비게이션을 제공합니다. NavEntry, BackStack, NavDisplay 등의 핵심 개념을 이해해야 합니다.",
        author = "Jetpack Team",
        createdAt = "2025-01-03"
    ),
    Post(
        id = 3,
        title = "Composable Presenter 패턴",
        content = "순수 Composable 함수로 UI 로직을 관리하는 방식입니다. ViewModel 대신 Composable 함수가 presenter 역할을 수행합니다.",
        author = "DroidKaigi",
        createdAt = "2025-01-02"
    ),
    Post(
        id = 4,
        title = "remember vs retain 비교",
        content = "remember는 Recomposition 중에만 상태를 유지하지만, retain은 Activity 재생성 후에도 상태를 유지합니다. 단, Process Death 후에는 retain도 상태가 손실됩니다.",
        author = "Compose Expert",
        createdAt = "2025-01-01"
    ),
    Post(
        id = 5,
        title = "UiState와 UiEvent 패턴",
        content = "단방향 데이터 흐름을 위한 패턴입니다. UiEvent는 사용자 상호작용을, UiState는 화면에 표시할 상태를 나타냅니다.",
        author = "Architecture Guide",
        createdAt = "2024-12-31"
    ),
)
