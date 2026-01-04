# RetainPlayground

Compose Retain API + Navigation3 + 순수 Composable Presenter 패턴을 사용한 예제 프로젝트입니다.

## 주요 기술 스택

- **Compose Retain API** (`androidx.compose.runtime:runtime-retain:1.11.0-alpha02`)
- **Navigation3** (`androidx.navigation3:navigation3-runtime:1.1.0-alpha01`)
- **Kotlin Serialization** (Route 직렬화)

## Retain API란?

`retain`은 `remember`의 상위 호환으로, **configuration change(화면 회전)에도 상태가 유지**됩니다.

```kotlin
// remember: 화면 회전 시 초기화됨
val count = remember { mutableStateOf(0) }

// retain: 화면 회전 시에도 유지됨
val count = retain { mutableStateOf(0) }
```

### retain의 생명주기

- **유지되는 경우**: configuration change, 백스택에 있는 동안
- **초기화되는 경우**: 백스택에서 제거, 프로세스 종료

### retain vs ViewModel

| | ViewModel | retain |
|---|---|---|
| **생명주기** | Activity/Fragment | NavEntry (백스택) |
| **정리** | 수동 (onCleared) | 자동 (백스택에서 제거 시) |
| **보일러플레이트** | 많음 | 적음 |
| **프로세스 종료** | SavedStateHandle 필요 | 유지 안됨 |

## Navigation3 BackStack 관리

### Android 전용 방식

`rememberNavBackStack` 사용, Route가 `NavKey`를 상속해야 합니다.

```kotlin
// Route 정의
@Serializable
sealed interface AppRoute : NavKey

@Serializable
data class PostDetailRoute(val postId: Long) : AppRoute

// 사용
val backStack = rememberNavBackStack(PostListRoute)
```

### KMP 방식

`rememberSerializable` + `SnapshotStateListSerializer` 사용, `NavKey` 상속 불필요합니다.

```kotlin
// Route 정의
@Serializable
sealed interface AppRoute

@Serializable
data class PostDetailRoute(val postId: Long) : AppRoute

// 사용
val backStack: MutableList<AppRoute> =
    rememberSerializable(serializer = SnapshotStateListSerializer()) {
        mutableStateListOf(PostListRoute)
    }
```

### 두 방식의 차이

| | `rememberNavBackStack` | `rememberSerializable` |
|---|---|---|
| **Route 요구사항** | `NavKey` 상속 필요 | `@Serializable`만 필요 |
| **제공처** | Navigation3 라이브러리 | Compose 기본 API |
| **KMP 지원** | O | O |

## 순수 Composable Presenter 패턴

ViewModel 없이 순수 `@Composable` 함수로 상태를 관리합니다.

```kotlin
@Composable
fun postDetailPresenter(postId: Long, likeCount: Int): PostDetailUiState {
    val post = samplePosts.find { it.id == postId }
    return PostDetailUiState(post = post, likeCount = likeCount)
}
```

### ViewModel과의 차이점

**ViewModel 방식** (자동 주입):
```kotlin
class DetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    val postId: Long = savedStateHandle["postId"]!!
}
```

**Composable Presenter 방식** (명시적 전달):
```kotlin
entry<PostDetailRoute> {
    val postId = it.postId  // Route에서 꺼내서
    val uiState = postDetailPresenter(postId, likeCount)  // 직접 전달
}
```

- **장점**: 모든 의존성이 명시적, 테스트 쉬움
- **단점**: 자동 주입 없음, 직접 전달해야 함

## Navigation3 Argument 전달

Route가 data class이므로 프로퍼티로 argument를 포함합니다.

```kotlin
// Route 정의
@Serializable
data class PostDetailRoute(val postId: Long) : AppRoute

// 네비게이션
backStack.add(PostDetailRoute(postId = 123))

// 수신
entry<PostDetailRoute> {
    val postId = it.postId  // 123
}
```

## SnapshotStateMap 사용 이유

일반 `MutableMap`은 값이 변경되어도 리컴포지션이 트리거되지 않습니다.

```kotlin
// MutableMap: 값 변경해도 UI 업데이트 안됨
val map = mutableMapOf<Long, Int>()

// SnapshotStateMap: 값 변경하면 리컴포지션 트리거
val map = mutableStateMapOf<Long, Int>()
```

## 프로젝트 구조

```
app/src/main/java/com/easyhooon/retainplayground/
├── MainActivity.kt                 # Navigation3 + retain 설정
├── navigation/
│   └── NavKeys.kt                  # Route 정의
├── feature/
│   ├── postlist/
│   │   └── PostListScreen.kt       # 목록 화면 + Presenter
│   └── postdetail/
│       └── PostDetailScreen.kt     # 상세 화면 + Presenter
├── model/
│   └── Post.kt                     # 데이터 모델
└── ui/theme/                       # 테마
```

## 테스트 방법

1. 게시글 상세에서 좋아요 버튼 여러 번 클릭
2. 화면 회전
3. 좋아요 수가 유지되면 retain 동작 확인!

## 참고 자료

- [Compose Retain API 블로그](https://velog.io/@mraz3068/Compose-Retain-API)
- [Navigation3 공식 문서](https://developer.android.com/jetpack/androidx/releases/navigation3)
- [KotlinConf App](https://github.com/JetBrains/kotlinconf-app)
- [DroidKaigi 2025 App](https://github.com/DroidKaigi/conference-app-2025)
