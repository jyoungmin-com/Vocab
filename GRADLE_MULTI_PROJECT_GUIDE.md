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

---

## 11. Multi-Project 전환 후 발생한 문제들과 해결 과정

### 문제 상황 요약
Multi-Project 구조로 전환한 후, IntelliJ에서 다음과 같은 문제들이 발생:

1. **Gradle Sync 시 NullPointerException 발생**
2. **VocabAuth와 VocabList 모듈에서 import 인식 안 됨**
3. **application.yml 파일을 IntelliJ가 이해하지 못함**
4. **빌드 실패: backend 모듈에서 Main class not found 오류**

---

### 문제 1: NullPointerException과 Import 인식 불가

#### 증상
```
IntelliJ에서 "Sync All Gradle Projects" 클릭 시:
- java.lang.NullPointerException 발생
- VocabAuth/VocabList의 Java 파일에서 import가 빨간색으로 표시됨
- 코드 자동완성 안 됨
```

#### 원인 분석
**1. `backend` 폴더가 Gradle 프로젝트로 인식되지만 설정 파일이 없음**

```
Vocab/
├── settings.gradle        "backend:VocabAuth, backend:VocabList를 include"
├── build.gradle           "subprojects에 Spring Boot 적용"
└── backend/
    ├── build.gradle       없음! ← 문제의 원인
    ├── VocabAuth/
    └── VocabList/
```

IntelliJ의 동작:
1. `settings.gradle`을 읽음: "backend:VocabAuth, backend:VocabList가 있구나"
2. Gradle은 `backend`를 **부모 프로젝트**로 자동 인식
3. IntelliJ가 `backend/build.gradle`을 찾으려고 시도
4. 파일이 없어서 **NullPointerException** 발생

**2. `subprojects` 블록이 `backend`에도 적용됨**

```gradle
subprojects {
    apply plugin: 'java'
    apply plugin: 'org.springframework.boot'  // ← backend에도 적용됨!
    apply plugin: 'io.spring.dependency-management'
}
```

- `subprojects`는 루트를 제외한 **모든 하위 프로젝트**에 적용됨
- `backend`, `backend:VocabAuth`, `backend:VocabList` 모두 포함
- `backend`는 조직용 폴더일 뿐인데 Spring Boot 플러그인이 적용됨

**3. Spring Boot 플러그인이 `bootJar` 태스크를 실행하려 시도**

```
./gradlew build 실행 시:

> Task :backend:bootJar FAILED

* What went wrong:
Execution failed for task ':backend:bootJar'.
> Main class name has not been configured and it could not be resolved
```

- Spring Boot 플러그인은 자동으로 `bootJar` 태스크를 실행
- `backend` 폴더에는 실제 Java 코드가 없음 (src 폴더 없음)
- 메인 클래스를 찾을 수 없어서 빌드 실패

#### 해결 방법

**Step 1: `backend/build.gradle` 생성**

`backend` 폴더에 빈 `build.gradle` 파일 생성:

```gradle
// backend/build.gradle
// Backend 폴더 - 하위 모듈들을 조직하기 위한 부모 프로젝트
// 실제 애플리케이션은 VocabAuth, VocabList에서 실행됩니다.
```

**이유:**
- IntelliJ가 프로젝트를 파싱할 때 `build.gradle` 파일을 기대함
- 파일이 없으면 NPE 발생
- 비어있어도 되지만, 파일 자체는 존재해야 함

**Step 2: `settings.gradle`에서 `backend` 명시적으로 include**

```gradle
rootProject.name = 'Vocab'

// Backend 폴더를 명시적으로 include
include 'backend'

// Backend microservices
include 'backend:VocabAuth'
include 'backend:VocabList'
```

**이유:**
- `backend:VocabAuth`만 include하면 Gradle이 암묵적으로 `backend`를 생성
- 명시적으로 include하면 설정을 더 명확하게 제어 가능

**Step 3: 루트 `build.gradle`에서 `backend`의 bootJar 비활성화**

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

// backend 폴더는 실제 애플리케이션이 아니므로 bootJar 비활성화
project(':backend') {
    bootJar {
        enabled = false
    }
    jar {
        enabled = true
    }
}
```

**이유:**
- `subprojects`가 `backend`에도 Spring Boot 플러그인을 적용
- `backend`는 실제 애플리케이션이 아니므로 `bootJar` 실행할 필요 없음
- `bootJar`를 비활성화하고 일반 `jar`만 활성화

**빌드 결과 확인:**
```bash
./gradlew clean build -x test

> Task :backend:bootJar SKIPPED  ← 성공!
> Task :backend:jar
> Task :backend:VocabAuth:bootJar
> Task :backend:VocabList:bootJar

BUILD SUCCESSFUL
```

---

### 문제 2: IntelliJ에서 application.yml 인식 불가

#### 증상
```
- application.yml 파일이 회색으로 표시됨
- ${SQL_URL} 같은 플레이스홀더가 인식 안 됨
- Spring Boot 자동완성이 작동하지 않음
```

#### 원인 분석

**1. IntelliJ 모듈 구조가 Gradle과 동기화 안 됨**

```
Gradle 프로젝트 구조:
Vocab
└── backend
    ├── VocabAuth    ← Spring Boot 모듈
    └── VocabList    ← Spring Boot 모듈

IntelliJ 모듈 구조 (동기화 전):
Vocab                 ← 일반 폴더로 인식
└── backend           ← Spring Boot 설정 없음
```

**2. Resources 폴더가 제대로 마킹되지 않음**

IntelliJ가 `src/main/resources`를 **Resources Root**로 인식하지 못하면:
- `application.yml`을 일반 텍스트 파일로 취급
- Spring Boot 설정 파일로 인식 안 함
- 자동완성, 검증 기능 작동 안 함

**3. Spring Boot 플러그인이 모듈에 적용 안 됨**

IntelliJ의 Spring Boot 지원:
- `build.gradle`에 `org.springframework.boot` 플러그인이 있어야 함
- Gradle Sync가 실패하면 Spring Boot 지원 활성화 안 됨
- NullPointerException으로 Sync 실패 → Spring Boot 지원 없음

#### 해결 방법

**Step 1: Gradle 캐시 정리 및 재빌드**

```bash
# 터미널에서 실행
./gradlew clean build -x test

# 출력:
BUILD SUCCESSFUL in 6s
```

- Gradle 레벨에서 빌드가 성공하는지 확인
- 터미널에서 성공하면 Gradle 설정 자체는 올바름

**Step 2: IntelliJ Gradle Sync**

IntelliJ에서:
1. 우측 **Gradle** 탭 열기
2. **Reload All Gradle Projects** 클릭 (아이콘)

**Sync 성공 후 변화:**

```
[이전] Gradle 탭:
Vocab
  backend (일반 폴더)

[이후] Gradle 탭:
Vocab
  backend
    VocabAuth (Spring Boot 아이콘)
    VocabList (Spring Boot 아이콘)
```

**Step 3: 모듈 구조 확인**

**File > Project Structure > Modules** 확인:

```
Vocab.backend.VocabAuth.main
  Sources:
    src/main/java           [Sources]
    src/main/resources      [Resources]  ← 이게 중요!

Vocab.backend.VocabList.main
  Sources:
    src/main/java           [Sources]
    src/main/resources      [Resources]  ← 이것도!
```

- `src/main/resources`가 **Resources**로 표시되어야 함
- 이래야 `application.yml`을 Spring 설정 파일로 인식

**Step 4: (필요시) 강력한 캐시 제거**

위 방법으로 안 되면:

```bash
# IntelliJ 완전 종료 후
rm -rf .idea
rm -rf .gradle
./gradlew clean

# IntelliJ 재시작 후
# File > Open → 프로젝트 폴더 선택
```

**결과 확인:**

```yaml
# application.yml - 이제 제대로 인식됨!
spring:
  application:
    name: VocabAuth  ← 자동완성 작동
  datasource:
    url: ${SQL_URL}  ← 플레이스홀더 인식 (회색 아님)
```

---

### 문제 3: Import 인식 안 됨 (상세)

#### 증상

VocabAuth의 Java 파일:
```java
package jyoungmin.vocabauth.controller;

import org.springframework.web.bind.annotation.RestController;  // ← 빨간색!
import org.springframework.security.core.Authentication;        // ← 빨간색!

@RestController  // ← "Cannot resolve symbol 'RestController'"
public class AuthController {
    // ...
}
```

VocabList의 Java 파일도 동일한 문제 발생.

#### 원인 분석

**1. IntelliJ가 의존성을 다운로드하지 못함**

```
정상 상태:
VocabAuth 모듈 → Spring Boot 의존성 (from Maven Central)

문제 상태:
VocabAuth 모듈 → ??? (의존성 정보 없음)
```

**Gradle Sync 실패** → **의존성 정보 로드 실패** → **import 인식 불가**

**2. 모듈의 Dependencies가 비어있음**

**File > Project Structure > Modules > VocabAuth > Dependencies**:

```
[이전]
(비어있음)

[이후]
spring-boot-starter-web
spring-boot-starter-security
lombok
...
```

**3. External Libraries가 로드 안 됨**

Project 탭에서:
```
[이전]
External Libraries (비어있음)

[이후]
External Libraries
  Gradle: org.springframework.boot:spring-boot-starter-web:3.5.7
  Gradle: org.springframework:spring-web:6.2.1
  ...
```

#### 해결 과정

**1단계: Gradle 빌드로 의존성 검증**

```bash
./gradlew :backend:VocabAuth:dependencies

# 출력:
compileClasspath - Compile classpath for source set 'main'.
+--- org.springframework.boot:spring-boot-starter-web -> 3.5.7
|    +--- org.springframework.boot:spring-boot-starter:3.5.7
|    +--- org.springframework:spring-web:6.2.1
...
```

- Gradle 레벨에서는 의존성이 제대로 해석됨
- 문제는 IntelliJ와 Gradle의 동기화

**2단계: IntelliJ 캐시 문제 해결**

```bash
# NullPointerException 문제 해결 후:
# 1. backend/build.gradle 생성
# 2. backend bootJar 비활성화
# 3. Gradle Sync 성공

# 이제 IntelliJ가 제대로 의존성을 로드함
```

**3단계: Gradle Sync 재실행**

IntelliJ Gradle 탭 → **Reload All Gradle Projects**

**결과:**
```
Sync 진행...
Resolving dependencies for configuration 'compileClasspath'...
Downloaded spring-boot-starter-web-3.5.7.jar
Downloaded spring-security-core-6.4.2.jar
...
Sync 성공!
```

**4단계: Import 인식 확인**

```java
package jyoungmin.vocabauth.controller;

import org.springframework.web.bind.annotation.RestController;  // ← 정상!
import org.springframework.security.core.Authentication;        // ← 정상!

@RestController  // ← 자동완성 작동
public class AuthController {
    // Ctrl+Space로 자동완성 작동!
}
```

---

### 정리: 왜 이런 문제가 발생했나?

#### Multi-Project 구조의 특수성

**Single Project (기존):**
```
VocabAuth/
├── settings.gradle  ← "나는 VocabAuth다"
└── build.gradle     ← IntelliJ가 직접 읽음
```
- IntelliJ가 프로젝트를 바로 이해함
- 한 개의 `build.gradle`만 파싱하면 됨

**Multi-Project (변경 후):**
```
Vocab/
├── settings.gradle      ← "backend:VocabAuth, backend:VocabList 포함"
├── build.gradle         ← "공통 설정"
└── backend/
    ├── build.gradle     ← 없으면 문제!
    ├── VocabAuth/
    │   └── build.gradle ← "VocabAuth 고유 설정"
    └── VocabList/
        └── build.gradle ← "VocabList 고유 설정"
```
- IntelliJ가 계층 구조를 파싱해야 함
- **한 곳이라도 빠지면 전체 Sync 실패**

#### 실제 문제의 연쇄 작용

```
1. backend/build.gradle 없음
   ↓
2. IntelliJ Gradle Sync 시 NullPointerException
   ↓
3. Sync 실패로 프로젝트 구조 로드 안 됨
   ↓
4. 모듈별 의존성 정보 로드 안 됨
   ↓
5. Import 인식 불가, application.yml 인식 불가
```

#### 해결의 핵심

```
backend/build.gradle 생성
   ↓
backend bootJar 비활성화 (빌드 성공)
   ↓
IntelliJ Gradle Sync 성공
   ↓
모듈 구조 제대로 로드
   ↓
의존성 다운로드 및 인식
   ↓
Import 정상, application.yml 인식 정상
```

---

### 최종 설정 파일

#### `settings.gradle`
```gradle
rootProject.name = 'Vocab'

// Backend 폴더를 명시적으로 include
include 'backend'

// Backend microservices
include 'backend:VocabAuth'
include 'backend:VocabList'
```

#### 루트 `build.gradle`
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

// backend 폴더는 실제 애플리케이션이 아니므로 bootJar 비활성화
project(':backend') {
    bootJar {
        enabled = false
    }
    jar {
        enabled = true
    }
}
```

#### `backend/build.gradle`
```gradle
// Backend 폴더 - 하위 모듈들을 조직하기 위한 부모 프로젝트
// 실제 애플리케이션은 VocabAuth, VocabList에서 실행됩니다.
```

#### 검증
```bash
# 빌드 성공
./gradlew clean build -x test
BUILD SUCCESSFUL in 6s

# IntelliJ Gradle Sync 성공
# Import 인식 정상
# application.yml 인식 정상
```

---

## 12. 새로운 프로젝트 추가하기

Multi-Project 구조에서 새로운 서비스를 추가하는 방법.

### 시나리오 1: Backend 서비스 추가 (예: VocabSearch)

#### Step 1: 프로젝트 폴더 생성

```bash
mkdir -p backend/VocabSearch/src/main/java/jyoungmin/vocabsearch
mkdir -p backend/VocabSearch/src/main/resources
mkdir -p backend/VocabSearch/src/test/java/jyoungmin/vocabsearch
```

#### Step 2: build.gradle 생성

`backend/VocabSearch/build.gradle`:
```gradle
plugins {
    id 'java'
    id 'org.springframework.boot'
    id 'io.spring.dependency-management'
}

group = 'jyoungmin'
version = '0.0.1-SNAPSHOT'
description = 'VocabSearch'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    compileOnly 'org.projectlombok:lombok'
    runtimeOnly 'com.mysql:mysql-connector-j'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'

    // 다른 서브프로젝트 의존성 추가 가능
    // implementation project(':backend:VocabAuth')
}

tasks.named('test') {
    useJUnitPlatform()
}
```

**주의사항:**
- `group`, `version`, `repositories`는 루트에서 상속되므로 생략 가능하지만, 명시하면 더 명확함
- 플러그인 버전은 **절대 쓰지 않음** (루트에서 관리)
- 프로젝트 간 의존성이 필요하면 `implementation project(':backend:VocabAuth')` 형태로 추가

#### Step 3: settings.gradle에 추가

```gradle
rootProject.name = 'Vocab'

include 'backend'

// Backend microservices
include 'backend:VocabAuth'
include 'backend:VocabList'
include 'backend:VocabSearch'  // ← 추가!
```

#### Step 4: 메인 클래스 생성

`backend/VocabSearch/src/main/java/jyoungmin/vocabsearch/VocabSearchApplication.java`:
```java
package jyoungmin.vocabsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VocabSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(VocabSearchApplication.class, args);
    }

}
```

#### Step 5: application.yml 생성

`backend/VocabSearch/src/main/resources/application.yml`:
```yaml
spring:
  application:
    name: VocabSearch
  config:
    import: optional:application-dev.properties
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: ${SQL_URL}
    username: ${SQL_USERNAME}
    password: ${SQL_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
    show-sql: true

server:
  port: 8082  # 다른 서비스와 포트 중복 안 되게!
```

#### Step 6: 빌드 및 검증

```bash
# 프로젝트 구조 확인
./gradlew projects

# 출력:
# Root project 'Vocab'
# \--- Project ':backend'
#      +--- Project ':backend:VocabAuth'
#      +--- Project ':backend:VocabList'
#      \--- Project ':backend:VocabSearch'  ← 추가됨!

# 새 프로젝트만 빌드
./gradlew :backend:VocabSearch:build -x test

# 전체 빌드
./gradlew clean build -x test
```

#### Step 7: IntelliJ 동기화

1. **Gradle** 탭 → **Reload All Gradle Projects** 클릭
2. 프로젝트 탭에서 `VocabSearch`가 Spring Boot 아이콘과 함께 나타나는지 확인

#### Step 8: 실행 확인

```bash
# Gradle로 실행
./gradlew :backend:VocabSearch:bootRun

# 또는 IntelliJ에서
# VocabSearchApplication.java 우클릭 → Run
```

---

### 시나리오 2: 공통 모듈 추가 (Common/Core)

여러 서비스에서 공유하는 코드를 위한 공통 모듈 추가.

#### Step 1: 프로젝트 폴더 생성

```bash
mkdir -p backend/VocabCommon/src/main/java/jyoungmin/vocabcommon
mkdir -p backend/VocabCommon/src/main/resources
```

#### Step 2: build.gradle 생성

`backend/VocabCommon/build.gradle`:
```gradle
plugins {
    id 'java'
    id 'io.spring.dependency-management'
    // ⚠️ Spring Boot 플러그인 제외! (라이브러리이므로)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // 공통 의존성
    implementation 'org.springframework:spring-context'
    implementation 'org.springframework:spring-web'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}

// 라이브러리이므로 bootJar 비활성화
bootJar {
    enabled = false
}

jar {
    enabled = true
}
```

**중요:**
- `org.springframework.boot` 플러그인 **제외**
- 실행 가능한 애플리케이션이 아니므로 `bootJar` 비활성화
- 일반 `jar`만 생성

#### Step 3: settings.gradle에 추가

```gradle
rootProject.name = 'Vocab'

include 'backend'

include 'backend:VocabCommon'  // ← 추가
include 'backend:VocabAuth'
include 'backend:VocabList'
include 'backend:VocabSearch'
```

#### Step 4: 다른 프로젝트에서 사용

`backend/VocabAuth/build.gradle`:
```gradle
dependencies {
    // 기존 의존성들...

    // VocabCommon 모듈 의존성 추가
    implementation project(':backend:VocabCommon')
}
```

#### Step 5: 공통 클래스 작성

`backend/VocabCommon/src/main/java/jyoungmin/vocabcommon/dto/ApiResponse.java`:
```java
package jyoungmin.vocabcommon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
}
```

#### Step 6: 다른 모듈에서 사용

`backend/VocabAuth/src/main/java/jyoungmin/vocabauth/controller/AuthController.java`:
```java
package jyoungmin.vocabauth.controller;

import jyoungmin.vocabcommon.dto.ApiResponse;  // ← 공통 모듈 import
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    @GetMapping("/api/auth/test")
    public ApiResponse<String> test() {
        return new ApiResponse<>(true, "Success", "Hello");
    }
}
```

---

### 프로젝트 추가 체크리스트

새 프로젝트를 추가할 때 확인할 사항:

#### Spring Boot 애플리케이션 (VocabSearch 등)
- [ ] 프로젝트 폴더 생성 (`backend/프로젝트명/`)
- [ ] `build.gradle` 생성 (플러그인 버전 제외)
- [ ] `settings.gradle`에 `include 'backend:프로젝트명'` 추가
- [ ] 메인 클래스 작성 (`@SpringBootApplication`)
- [ ] `application.yml` 작성 (포트 중복 주의!)
- [ ] `./gradlew projects`로 프로젝트 확인
- [ ] `./gradlew :backend:프로젝트명:build`로 빌드 확인
- [ ] IntelliJ Gradle Sync
- [ ] Spring Boot 아이콘 확인

#### 공통 라이브러리 모듈 (VocabCommon 등)
- [ ] 프로젝트 폴더 생성
- [ ] `build.gradle` 생성 (**Spring Boot 플러그인 제외**)
- [ ] `bootJar { enabled = false }` 설정
- [ ] `jar { enabled = true }` 설정
- [ ] `settings.gradle`에 추가
- [ ] 다른 프로젝트에서 `implementation project(':backend:모듈명')` 추가
- [ ] 빌드 확인

---

### 프로젝트 구조 예시 (최종)

```
Vocab/
├── settings.gradle              # 모든 프로젝트 정의
├── build.gradle                 # 공통 설정
├── gradlew
├── gradlew.bat
├── gradle/
│   └── wrapper/
│
├── backend/
│   ├── build.gradle             # backend 자체는 조직용 폴더
│   │
│   ├── VocabCommon/             # 공통 라이브러리 모듈
│   │   ├── build.gradle         # Spring Boot 플러그인 제외
│   │   └── src/
│   │
│   ├── VocabAuth/               # 인증 서비스
│   │   ├── build.gradle
│   │   └── src/
│   │
│   ├── VocabList/               # 리스트 서비스
│   │   ├── build.gradle
│   │   └── src/
│   │
│   └── VocabSearch/             # 검색 서비스 (새로 추가)
│       ├── build.gradle
│       └── src/
│
└── frontend/                    # Frontend
    ├── package.json
    ├── node_modules/
    ├── src/
    └── ...
```

#### settings.gradle (최종)
```gradle
rootProject.name = 'Vocab'

include 'backend'

// Common library
include 'backend:VocabCommon'

// Backend microservices
include 'backend:VocabAuth'
include 'backend:VocabList'
include 'backend:VocabSearch'
```

#### 빌드 명령어
```bash
# 전체 빌드
./gradlew build

# 특정 서비스만 빌드
./gradlew :backend:VocabSearch:build

# 특정 서비스 실행
./gradlew :backend:VocabSearch:bootRun

# 프로젝트 구조 확인
./gradlew projects
```

---

### 주의사항

#### 1. 포트 충돌 방지
각 Spring Boot 애플리케이션은 다른 포트를 사용해야 합니다:
```yaml
# VocabAuth: 8080
# VocabList: 8081
# VocabSearch: 8082
```

#### 2. 데이터베이스 분리
Microservice 아키텍처라면 각 서비스가 자신의 DB를 가져야 합니다:
```yaml
# VocabAuth
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/vocab_auth

# VocabList
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/vocab_list
```

#### 3. 프로젝트 간 의존성 주의
순환 의존성을 피하세요:
```
❌ 나쁜 예:
VocabAuth → VocabList
VocabList → VocabAuth  (순환!)

✅ 좋은 예:
VocabAuth → VocabCommon
VocabList → VocabCommon
```

#### 4. 공통 모듈은 Spring Boot 플러그인 제외
```gradle
// ❌ 나쁜 예
plugins {
    id 'org.springframework.boot'  // 실행 가능한 JAR 생성됨
}

// ✅ 좋은 예
plugins {
    id 'io.spring.dependency-management'  // 의존성 관리만
}
bootJar { enabled = false }
jar { enabled = true }
```
