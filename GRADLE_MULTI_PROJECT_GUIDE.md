# Gradle Multi-Project 구조

## 1. Gradle 프로젝트 구조의 이해

### Single Project vs Multi-Project

**Single Project (기존 VocabAuth, VocabList):**
```
VocabAuth/
├── settings.gradle       # "나는 VocabAuth 프로젝트다"
├── build.gradle          # 의존성, 플러그인, 빌드 설정
└── src/
```
- 각 프로젝트가 독립적으로 존재
- IntelliJ는 "어느 것을 열어야 하지?" 혼란

**Multi-Project (현재 Vocab):**
```
Vocab/                    # 루트 프로젝트
├── settings.gradle       # "나는 Vocab이고, VocabAuth와 VocabList를 포함한다"
├── build.gradle          # 공통 설정
└── backend/
    ├── VocabAuth/        # 서브프로젝트 1
    │   └── build.gradle  # VocabAuth만의 설정
    └── VocabList/        # 서브프로젝트 2
        └── build.gradle  # VocabList만의 설정
```
- 하나의 거대한 프로젝트 안에 여러 서브프로젝트
- IntelliJ는 "Vocab을 열고, 내부에 2개 프로젝트가 있구나!" 인식

---

## 2. settings.gradle 상세 분석

```gradle
rootProject.name = 'Vocab'

include 'backend:VocabAuth'
include 'backend:VocabList'
```

### `rootProject.name = 'Vocab'`
- 이 프로젝트의 최상위 이름 정의
- IntelliJ Project 창에 **"Vocab"**으로 표시됨
- 빌드 결과물의 기본 이름으로 사용됨

### `include 'backend:VocabAuth'`
**문법 해석:**
- `'backend:VocabAuth'` = `backend/VocabAuth` 폴더를 프로젝트로 인식
- `:` = 디렉토리 구분자 (경로의 `/`를 의미)
- Gradle은 `backend/VocabAuth/build.gradle`을 찾음

**만약 이렇게 쓰면?**
```gradle
include 'VocabAuth'  # 루트/VocabAuth 폴더를 찾음
include 'backend:VocabAuth'  # 루트/backend/VocabAuth 폴더를 찾음
```

**프로젝트 참조 이름:**
- 다른 프로젝트에서 이렇게 참조 가능:
```gradle
dependencies {
    implementation project(':backend:VocabAuth')
}
```

---

## 3. build.gradle 상세 분석

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.5.7' apply false
    id 'io.spring.dependency-management' version '1.1.7' apply false
}
```

### `apply false`의 의미
**없으면:**
```gradle
plugins {
    id 'org.springframework.boot' version '3.5.7'
}
// → 루트 프로젝트에 Spring Boot 플러그인 적용
// → 루트는 Spring Boot 앱이 아니므로 에러!
```

**있으면:**
```gradle
plugins {
    id 'org.springframework.boot' version '3.5.7' apply false
}
// → "3.5.7 버전을 정의만 해두고, 루트엔 적용하지 마"
// → 서브프로젝트에서 버전 없이 사용 가능
```

**실제 효과:**
```gradle
// 서브프로젝트 build.gradle
plugins {
    id 'org.springframework.boot'  // 버전 안 써도 됨! 루트에서 관리
}
```

---

### `allprojects { }` vs `subprojects { }`

```gradle
allprojects {
    group = 'jyoungmin'
    version = '0.0.1-SNAPSHOT'
    repositories {
        mavenCentral()
    }
}
```

**`allprojects`의 범위:**
- 루트 프로젝트 **포함**
- 모든 서브프로젝트
- "진짜 모든 프로젝트"에 적용

**실제 적용 결과:**
```
Vocab (루트)          → group: jyoungmin, repositories: mavenCentral()
├── VocabAuth         → group: jyoungmin, repositories: mavenCentral()
└── VocabList         → group: jyoungmin, repositories: mavenCentral()
```

---

```gradle
subprojects {
    apply plugin: 'java'
    apply plugin: 'org.springframework.boot'
    apply plugin: 'io.spring.dependency-management'

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }
}
```

**`subprojects`의 범위:**
- 루트 프로젝트 **제외**
- 서브프로젝트만

**실제 적용 결과:**
```
Vocab (루트)          → 플러그인 적용 안 됨
├── VocabAuth         → java, spring-boot, dependency-management 적용
└── VocabList         → java, spring-boot, dependency-management 적용
```

**왜 이렇게 나눌까?**
- 루트는 빈 프로젝트 (관리만 담당)
- 실제 코드는 서브프로젝트에만 있음
- 루트에 Spring Boot 적용하면 쓸데없는 오버헤드

---

## 4. 서브프로젝트 build.gradle 변경 이유

### Before (독립 프로젝트)
```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.5.7'
    id 'io.spring.dependency-management' version '1.1.7'
}

group = 'jyoungmin'
version = '0.0.1-SNAPSHOT'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
}
```

### After (멀티 프로젝트의 서브프로젝트)
```gradle
plugins {
    id 'java'                              // 루트의 subprojects에서 apply됨 (중복이지만 명시)
    id 'org.springframework.boot'          // 버전 제거 (루트가 관리)
    id 'io.spring.dependency-management'   // 버전 제거 (루트가 관리)
}

// group, version, repositories → 루트의 allprojects에서 상속됨

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
}
```

**변경 사항 요약:**
1. 플러그인 버전 제거 → 루트에서 관리
2. `group`, `version` 제거 → 루트 allprojects에서 상속
3. `repositories` 제거 → 루트 allprojects에서 상속
4. 의존성만 남김 → 프로젝트 고유 설정

**장점:**
- 버전 업그레이드 시 루트 1곳만 수정
- 모든 서브프로젝트 자동 동기화
- 일관성 보장

---

## 5. 프로젝트 계층 구조

```
Vocab (Root Project)
│
├─── :backend (가상 프로젝트)
│     │
│     ├─── :backend:VocabAuth (실제 프로젝트)
│     │     └─── src/main/java/...
│     │
│     └─── :backend:VocabList (실제 프로젝트)
│           └─── src/main/java/...
```

**Gradle 명령어:**
```bash
./gradlew tasks                    # 루트 프로젝트 태스크
./gradlew :backend:VocabAuth:tasks # VocabAuth 프로젝트 태스크
./gradlew :backend:VocabList:bootRun # VocabList 실행
./gradlew build                    # 모든 프로젝트 빌드
```

---

## 6. IntelliJ 인식 과정

### IntelliJ가 프로젝트를 여는 과정:

1. **폴더 열기**
2. **`settings.gradle` 찾기** ← 중요!
   - 있으면: "아, Gradle 프로젝트구나!"
   - 없으면: "그냥 폴더네..."
3. **`settings.gradle` 파싱**
   ```gradle
   rootProject.name = 'Vocab'
   include 'backend:VocabAuth'
   include 'backend:VocabList'
   ```
   - "Vocab 프로젝트 안에 2개 서브프로젝트가 있구나"
4. **`build.gradle` 파싱**
   - 의존성, 플러그인 정보 읽기
5. **Gradle Sync 실행**
   - 의존성 다운로드
   - 프로젝트 구조 생성

### IntelliJ Project 창 표시:
```
Vocab
  .gradle
  .idea
  backend
    VocabAuth         ← Spring Boot 아이콘
      src
      build.gradle
    VocabList         ← Spring Boot 아이콘
      src
      build.gradle
  gradle
  build.gradle
  settings.gradle
  gradlew
```

---

## 7. 왜 이 방식이 좋은가?

### 장점

**1. 버전 중앙 관리**
```gradle
// 루트에서만 수정
plugins {
    id 'org.springframework.boot' version '3.6.0' apply false  // 업그레이드
}
// → 모든 서브프로젝트 자동 적용
```

**2. 공통 의존성 관리**
```gradle
subprojects {
    dependencies {
        implementation 'org.projectlombok:lombok'  // 모든 프로젝트에 적용
    }
}
```

**3. 프로젝트 간 의존성**
```gradle
// VocabList에서 VocabAuth 사용 가능
dependencies {
    implementation project(':backend:VocabAuth')
}
```

**4. 한 번에 빌드**
```bash
./gradlew build  # 모든 프로젝트 빌드
```

**5. IDE 통합**
- IntelliJ에서 모든 프로젝트 동시 작업
- 검색, 리팩토링이 전체 코드베이스에 적용

---

## 8. 실제 예시: 빌드 과정

```bash
./gradlew build
```

**실행 순서:**
1. 루트 `settings.gradle` 읽기
2. 루트 `build.gradle` 적용
   - `allprojects` → 모든 프로젝트에 적용
   - `subprojects` → 서브프로젝트에만 적용
3. `backend:VocabAuth` 빌드
   - `backend/VocabAuth/build.gradle` 적용
   - 컴파일, 테스트, JAR 생성
4. `backend:VocabList` 빌드
   - `backend/VocabList/build.gradle` 적용
   - 컴파일, 테스트, JAR 생성

**출력:**
```
> Task :backend:VocabAuth:compileJava
> Task :backend:VocabAuth:processResources
> Task :backend:VocabAuth:classes
> Task :backend:VocabAuth:bootJar
> Task :backend:VocabList:compileJava
> Task :backend:VocabList:processResources
> Task :backend:VocabList:classes
> Task :backend:VocabList:bootJar

BUILD SUCCESSFUL
```

---

## 9. 추가 팁

### 특정 프로젝트만 빌드
```bash
./gradlew :backend:VocabAuth:build
```

### 프로젝트 구조 확인
```bash
./gradlew projects
```

### 특정 프로젝트 실행
```bash
./gradlew :backend:VocabAuth:bootRun
```

### 의존성 트리 보기
```bash
./gradlew :backend:VocabAuth:dependencies
```

---

## 10. 요약: Monorepo 구조를 IntelliJ가 인식하도록 만든 방법

### 문제 상황:
- 루트 폴더에 여러 개의 독립적인 Gradle 프로젝트가 있음
- IntelliJ가 어떤 프로젝트를 열어야 할지 모름

### 해결 방법: Gradle Multi-Project 구조로 변경

#### 1. 루트에 `settings.gradle` 생성
```gradle
rootProject.name = 'Vocab'

// Backend microservices
include 'backend:VocabAuth'
include 'backend:VocabList'
```
- 루트 프로젝트 이름 정의
- 하위 프로젝트들을 `include`로 등록

#### 2. 루트에 `build.gradle` 생성
```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.5.7' apply false
    id 'io.spring.dependency-management' version '1.1.7' apply false
}

allprojects {
    group = 'jyoungmin'
    version = '0.0.1-SNAPSHOT'

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply plugin: 'java'
    apply plugin: 'org.springframework.boot'
    apply plugin: 'io.spring.dependency-management'

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }
}
```
- 루트에서 플러그인 버전 관리 (`apply false`로 루트에는 적용 안 함)
- `allprojects`: 모든 프로젝트 공통 설정
- `subprojects`: 하위 프로젝트에만 적용되는 설정

#### 3. Gradle Wrapper를 루트로 복사
```bash
cp -r backend/VocabAuth/gradle .
cp backend/VocabAuth/gradlew backend/VocabAuth/gradlew.bat .
```
- 루트에서 `./gradlew` 명령어 사용 가능하도록

#### 4. 하위 프로젝트 정리
- 각 서브프로젝트의 `settings.gradle` 삭제
- 각 서브프로젝트 `build.gradle`에서 플러그인 버전 제거:
```gradle
// Before
plugins {
    id 'org.springframework.boot' version '3.5.7'
}

// After (루트에서 버전 관리)
plugins {
    id 'org.springframework.boot'
}
```

### 결과 구조:
```
Vocab/
├── settings.gradle          # 프로젝트 구조 정의
├── build.gradle             # 공통 설정
├── gradlew                  # Gradle Wrapper
├── gradlew.bat
├── gradle/
│   └── wrapper/
└── backend/
    ├── VocabAuth/
    │   └── build.gradle     # 프로젝트별 의존성
    └── VocabList/
        └── build.gradle     # 프로젝트별 의존성
```

### IntelliJ에서 열기:
1. **File > Open**
2. **루트 폴더(`vocab`) 선택**
3. IntelliJ가 자동으로 Gradle Multi-Project 인식

### 핵심 포인트:
- 루트의 `settings.gradle`이 프로젝트 구조를 정의
- 루트의 `build.gradle`이 공통 설정 관리
- 버전 관리를 루트에서 일원화 (`apply false` 사용)
- 각 서브프로젝트는 자신만의 의존성만 관리
