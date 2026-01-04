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

### retain 관련 API

#### 1. `retain` - 상태 유지
```kotlin
val player = retain {
    ExoPlayer.Builder(context).build()
}
```

#### 2. `RetainedEffect` - retain 수명 주기 side effect
`DisposableEffect`의 retain 버전입니다. composition을 떠날 때가 아니라 **진짜 retire될 때만** 정리됩니다.

```kotlin
val player = retain(mediaUri) { MediaPlayer(mediaUri) }

// player가 retain될 때 초기화, retire될 때만 정리
RetainedEffect(player) {
    player.initialize()  // 최초 retain 시 실행
    onRetire {
        player.close()  // 백스택에서 완전히 제거될 때만 실행
    }
}
```

| | DisposableEffect | RetainedEffect |
|---|---|---|
| **정리 시점** | composition 떠날 때 | 백스택에서 완전히 제거될 때 |
| **화면 회전** | dispose → 다시 setup | 유지됨 (정리 안됨) |
| **사용 위치** | Composable 내부 | Composable 내부 (key 필수) |
| **정리 함수** | `onDispose { }` | `onRetire { }` |

#### 3. `@DoNotRetain` - retain 금지 어노테이션
메모리 누수 위험이 있는 클래스에 사용합니다.

```kotlin
@DoNotRetain
class MyManagedClass  // 이 클래스는 retain 불가
```

**retain 금지 대상**: Activity, View, Fragment, ViewModel, Context, Lifecycle

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

ViewModel 없이 순수 `@Composable` 함수로 상태를 관리합니다. (DroidKaigi 스타일)

### EventFlow & EventEffect (DroidKaigi 스타일)

이벤트 처리를 presenter 내부에서 담당합니다.

```kotlin
// EventFlow 유틸리티 (common/EventEffect.kt)
typealias EventFlow<T> = MutableSharedFlow<T>

@Composable
fun <T> rememberEventFlow(): EventFlow<T> {
    return remember { MutableSharedFlow(extraBufferCapacity = 20) }
}

@Composable
fun <EVENT> EventEffect(
    eventFlow: EventFlow<EVENT>,
    block: suspend CoroutineScope.(event: EVENT) -> Unit,
) {
    LaunchedEffect(eventFlow) {
        supervisorScope {
            eventFlow.collect { event ->
                launch { block(event) }
            }
        }
    }
}
```

### Presenter 구현

```kotlin
@Composable
fun postDetailPresenter(
    postId: Long,
    likeCount: Int,
    eventFlow: EventFlow<PostDetailUiEvent>,  // 이벤트 수신
    onBackClick: () -> Unit,
    onLikeClick: () -> Unit,
): PostDetailUiState {
    var commentDraft by retain(postId) { mutableStateOf("") }

    // 이벤트 처리를 presenter 내부에서 담당
    EventEffect(eventFlow) { event ->
        when (event) {
            is PostDetailUiEvent.OnBackClick -> onBackClick()
            is PostDetailUiEvent.OnLikeClick -> onLikeClick()
            is PostDetailUiEvent.OnCommentDraftChange -> {
                commentDraft = event.text  // 내부 상태 직접 업데이트
            }
        }
    }

    return PostDetailUiState(post = post, likeCount = likeCount, commentDraft = commentDraft)
}
```

### 사용 방법

```kotlin
entry<PostDetailRoute> {
    val eventFlow = rememberEventFlow<PostDetailUiEvent>()
    val scope = rememberCoroutineScope()

    val uiState = postDetailPresenter(
        postId = it.postId,
        likeCount = likeCounts[it.postId] ?: 0,
        eventFlow = eventFlow,
        onBackClick = { backStack.removeLastOrNull() },
        onLikeClick = { likeCounts[it.postId] = (likeCounts[it.postId] ?: 0) + 1 },
    )

    PostDetailScreen(
        uiState = uiState,
        onEvent = { event -> scope.launch { eventFlow.emit(event) } },
    )
}
```

### ViewModel과의 차이점

| | ViewModel | Composable Presenter |
|---|---|---|
| **상태 범위** | Activity/Fragment | NavEntry (백스택) |
| **의존성 주입** | Hilt 등으로 자동 | 명시적 전달 |
| **이벤트 처리** | 외부에서 호출 | EventEffect로 내부 처리 |
| **테스트** | Mock 필요 | 순수 함수로 쉬움 |

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
├── common/
│   └── EventEffect.kt              # EventFlow, EventEffect (DroidKaigi 스타일)
├── navigation/
│   └── NavKeys.kt                  # Route 정의
├── feature/
│   ├── postlist/
│   │   ├── PostListPresenter.kt    # UiState, UiEvent, Presenter
│   │   └── PostListScreen.kt       # 목록 화면 UI
│   └── postdetail/
│       ├── PostDetailPresenter.kt  # UiState, UiEvent, Presenter + RetainedEffect
│       └── PostDetailScreen.kt     # 상세 화면 UI
├── model/
│   └── Post.kt                     # 데이터 모델
└── ui/theme/                       # 테마
```

## 테스트 방법

1. 게시글 상세에서 좋아요 버튼 여러 번 클릭
2. 화면 회전
3. 좋아요 수가 유지되면 retain 동작 확인!

## 참고 자료

- [Compose 상태 수명 공식 문서](https://developer.android.com/develop/ui/compose/state-lifespans?hl=ko)
- [Compose Retain API 블로그](https://velog.io/@mraz3068/Compose-Retain-API)
- [Navigation3 공식 문서](https://developer.android.com/jetpack/androidx/releases/navigation3)
- [KotlinConf App](https://github.com/JetBrains/kotlinconf-app)
- [DroidKaigi 2025 App](https://github.com/DroidKaigi/conference-app-2025)
- [RetainedEffect 소개 - droidcon](https://www.droidcon.com/2025/08/18/previewing-retainedeffect-a-new-side-effect-to-bridge-between-composition-and-retention-lifecycles/)
