---
name: feature-planner
description: 단계별 기능 계획을 생성하고, 품질 게이트와 점진적 제공 구조를 갖춥니다. 기능 계획, 작업 정리, 작업 세분화, 로드맵 작성, 개발 전략 수립 시 사용하세요. 키워드: plan, planning, phases, breakdown, strategy, roadmap, organize, structure, outline.
---

# Feature Planner

## 목적
다음과 같은 구조화된 단계별(phase-based) 계획을 생성합니다:
- 각 Phase는 완전하고 실행 가능한 기능을 제공함
- 진행하기 전에 Quality Gate가 검증을 강제함
- 작업 시작 전 사용자가 계획을 승인함
- 마크다운 체크박스로 진행 상황 추적
- 각 Phase는 최대 1-4시간 소요

## 계획 워크플로우

### 1단계: 요구사항 분석
1. 관련 파일을 읽어 코드베이스 아키텍처 이해
2. 의존성 및 통합 포인트 식별
3. 복잡성 및 리스크 평가
4. 적절한 범위 결정 (소/중/대)

### 2단계: TDD 통합을 포함한 Phase 세분화
기능을 3-7개의 Phase로 나누며, 각 Phase는:
- **Test-First**: 구현 전 테스트 작성
- 작동하고 테스트 가능한 기능 제공
- 최대 1-4시간 소요
- Red-Green-Refactor 주기 따름
- 측정 가능한 테스트 커버리지 요구사항 포함
- 독립적으로 롤백 가능
- 명확한 성공 기준 보유

**Phase 구조**:
- Phase 이름: 명확한 결과물
- 목표: 어떤 작동 기능을 생산하는지
- **테스트 전략**: 테스트 유형, 커버리지 목표, 테스트 시나리오
- 작업 (TDD 워크플로우 순서):
  1. **RED 작업**: 실패하는 테스트 먼저 작성
  2. **GREEN 작업**: 테스트를 통과시키는 최소한의 코드 구현
  3. **REFACTOR 작업**: 테스트가 통과하는 상태에서 코드 품질 개선
- Quality Gate: TDD 준수 + 검증 기준
- 의존성: 시작 전 존재해야 하는 것
- **커버리지 목표**: 해당 Phase에 대한 구체적 비율 또는 체크리스트

### 3단계: 계획 문서 생성
plan-template.md를 사용하여 생성: `docs/plans/PLAN_<feature-name>.md`

포함 내용:
- 개요 및 목표
- 아키텍처 결정 및 근거
- 체크박스가 포함된 전체 Phase 세분화
- Quality Gate 체크리스트
- 리스크 평가 테이블
- Phase별 롤백 전략
- 진행 상황 추적 섹션
- 참고 및 배운 점 영역

### 4단계: 사용자 승인
**중요**: 진행하기 전 AskUserQuestion을 사용하여 명시적 승인을 받으세요.

질문:
- "이 Phase 구성이 프로젝트에 적합한가요?"
- "제안된 접근 방식에 우려되는 점이 있나요?"
- "계획 문서를 생성하고 진행할까요?"

사용자가 승인을 확인한 후에만 계획 문서를 생성하세요.

### 5단계: 문서 생성
1. `docs/plans/` 디렉토리가 없으면 생성
2. 모든 체크박스가 해제된 계획 문서 생성
3. 헤더에 Quality Gate에 대한 명확한 지침 추가
4. 사용자에게 계획 위치 및 다음 단계 알림

## Quality Gate 표준

각 Phase는 다음 Phase로 넘어가기 전 이 항목들을 반드시 검증해야 함:

**빌드 및 컴파일**:
- [ ] 프로젝트가 오류 없이 빌드/컴파일됨
- [ ] 문법 오류 없음

**테스트 주도 개발 (TDD)**:
- [ ] 프로덕션 코드 이전에 테스트 작성됨
- [ ] Red-Green-Refactor 주기 준수됨
- [ ] 단위 테스트: 비즈니스 로직에 대해 80% 이상 커버리지
- [ ] 통합 테스트: 핵심 사용자 흐름 검증됨
- [ ] 테스트 스위트가 허용 가능한 시간 내에 실행됨 (<5분)

**테스팅**:
- [ ] 모든 기존 테스트 통과
- [ ] 새로운 기능에 대한 새 테스트 추가됨
- [ ] 테스트 커버리지 유지 또는 개선됨

**코드 품질**:
- [ ] 린트(Linting) 오류 없이 통과
- [ ] 타입 체크 통과 (해당되는 경우)
- [ ] 코드 포맷팅 일관성 유지

**기능성**:
- [ ] 수동 테스트로 기능 작동 확인
- [ ] 기존 기능에 회귀(regression) 없음
- [ ] 엣지 케이스 테스트됨

**보안 및 성능**:
- [ ] 새로운 보안 취약점 없음
- [ ] 성능 저하 없음
- [ ] 자원 사용량 허용 범위 내

**문서화**:
- [ ] 코드 주석 업데이트됨
- [ ] 문서에 변경 사항 반영됨

## 진행 상황 추적 프로토콜

계획 문서 헤더에 다음을 추가:

```markdown
**중요 지침**: 각 Phase 완료 후:
1. ✅ 완료된 작업 체크박스 체크
2. 🧪 모든 Quality Gate 검증 명령어 실행
3. ⚠️ 모든 Quality Gate 항목 통과 확인
4. 📅 "최종 업데이트" 날짜 갱신
5. 📝 참고 섹션에 배운 점 기록
6. ➡️ 그 후에만 다음 Phase로 진행

⛔ Quality Gate를 건너뛰거나 체크 실패 상태로 진행하지 마시오
```

## Phase 규모 산정 가이드라인

**소규모** (2-3 Phases, 총 3-6시간):
- 단일 컴포넌트 또는 단순 기능
- 최소한의 의존성
- 명확한 요구사항
- 예: 다크 모드 토글 추가, 새 폼 컴포넌트 생성

**중규모** (4-5 Phases, 총 8-15시간):
- 다중 컴포넌트 또는 중간 규모 기능
- 일부 통합 복잡성
- 데이터베이스 변경 또는 API 작업
- 예: 사용자 인증 시스템, 검색 기능

**대규모** (6-7 Phases, 총 15-25시간):
- 여러 영역에 걸친 복잡한 기능
- 중대한 아키텍처 영향
- 다중 통합
- 예: 임베딩을 이용한 AI 검색, 실시간 협업

## 리스크 평가

식별 및 문서화:
- **기술적 리스크**: API 변경, 성능 문제, 데이터 마이그레이션
- **의존성 리스크**: 외부 라이브러리 업데이트, 서드파티 서비스 가용성
- **일정 리스크**: 복잡성 미지수, 블로킹 의존성
- **품질 리스크**: 테스트 커버리지 공백, 회귀 가능성

각 리스크에 대해 명시:
- 확률: 낮음/중간/높음
- 영향: 낮음/중간/높음
- 완화 전략: 구체적 조치 단계

## 롤백 전략

각 Phase에 대해, 문제 발생 시 변경 사항을 되돌리는 방법을 문서화.
고려 사항:
- 어떤 코드 변경을 취소해야 하는지
- 되돌릴 데이터베이스 마이그레이션 (해당되는 경우)
- 복구할 설정 변경 사항
- 제거할 의존성

## 테스트 명세 가이드라인

### Test-First 개발 워크플로우

**각 기능 컴포넌트별**:
1. **테스트 케이스 명세** (어떤 코드도 작성하기 전)
   - 어떤 입력을 테스트할 것인가?
   - 어떤 출력이 예상되는가?
   - 어떤 엣지 케이스를 처리해야 하는가?
   - 어떤 오류 조건을 테스트해야 하는가?

2. **테스트 작성** (Red Phase)
   - 실패할 테스트 작성
   - 올바른 이유로 실패하는지 확인
   - 테스트를 실행하여 실패 확인
   - 실패하는 테스트를 커밋하여 TDD 준수 추적

3. **코드 구현** (Green Phase)
   - 테스트를 통과시키는 최소한의 코드 작성
   - 자주 테스트 실행 (2-5분마다)
   - 모든 테스트가 통과하면 중단
   - 테스트 범위를 넘어서는 추가 기능 구현 금지

4. **리팩터링** (Blue Phase)
   - 테스트가 통과하는 상태에서 코드 품질 개선
   - 중복 로직 추출
   - 네이밍 및 구조 개선
   - 각 리팩터링 단계 후 테스트 실행
   - 리팩터링 완료 시 커밋

### 테스트 유형

**단위 테스트**:
- **대상**: 개별 함수, 메서드, 클래스
- **의존성**: 없음 또는 Mock/Stub
- **속도**: 빠름 (테스트당 <100ms)
- **격리**: 외부 시스템으로부터 완전 격리
- **커버리지**: 비즈니스 로직의 80% 이상

**통합 테스트**:
- **대상**: 컴포넌트/모듈 간 상호작용
- **의존성**: 실제 의존성 사용 가능
- **속도**: 보통 (테스트당 <1초)
- **격리**: 컴포넌트 경계 테스트
- **커버리지**: 핵심 통합 포인트

**E2E (End-to-End) 테스트**:
- **대상**: 전체 사용자 워크플로우
- **의존성**: 실제 또는 거의 실제와 같은 환경
- **속도**: 느림 (초~분)
- **격리**: 전체 시스템 통합
- **커버리지**: 핵심 사용자 여정

### 테스트 커버리지 계산

**커버리지 임계값** (프로젝트에 맞게 조정):
- **비즈니스 로직**: ≥90% (핵심 코드 경로)
- **데이터 접근 계층**: ≥80% (리포지토리, DAO)
- **API/컨트롤러 계층**: ≥70% (엔드포인트)
- **UI/프레젠테이션**: 커버리지보다 통합 테스트 선호

**생태계별 커버리지 명령어**:
```bash
# JavaScript/TypeScript
jest --coverage
nyc report --reporter=html

# Python
pytest --cov=src --cov-report=html
coverage report

# Java
mvn jacoco:report
gradle jacocoTestReport

# Go
go test -cover ./...
go tool cover -html=coverage.out

# .NET
dotnet test /p:CollectCoverage=true /p:CoverageReporter=html
reportgenerator -reports:coverage.xml -targetdir:coverage

# Ruby
bundle exec rspec --coverage
open coverage/index.html

# PHP
phpunit --coverage-html coverage
```

### 일반적인 테스트 패턴

**Arrange-Act-Assert (AAA) 패턴**:
```
test 'description of behavior':
  // Arrange: 테스트 데이터 및 의존성 설정
  input = createTestData()

  // Act: 테스트 대상 동작 실행
  result = systemUnderTest.method(input)

  // Assert: 예상 결과 검증
  assert result == expectedOutput
```

**Given-When-Then (BDD 스타일)**:
```
test 'feature should behave in specific way':
  // Given: 초기 컨텍스트/상태
  given userIsLoggedIn()

  // When: 액션 발생
  when userClicksButton()

  // Then: 관찰 가능한 결과
  then shouldSeeConfirmation()
```

**의존성 Mocking/Stubbing**:
```
test 'component should call dependency':
  // Mock/Stub 생성
  mockService = createMock(ExternalService)
  component = new Component(mockService)

  // Mock 동작 구성
  when(mockService.method()).thenReturn(expectedData)

  // 실행 및 검증
  component.execute()
  verify(mockService.method()).calledOnce()
```

### 계획 내 테스트 문서화

**각 Phase에 명시할 것**:
1. **테스트 파일 위치**: 테스트가 작성될 정확한 경로
2. **테스트 시나리오**: 구체적인 테스트 케이스 목록
3. **예상되는 실패**: 초기에 테스트가 어떤 오류를 보여야 하는가?
4. **커버리지 목표**: 이 Phase의 목표 비율
5. **Mock할 의존성**: 무엇을 Mocking/Stubbing 해야 하는가?
6. **테스트 데이터**: 어떤 Fixture/Factory가 필요한가?

## 지원 파일 참조
- [plan-template.md](plan-template.md) - 전체 계획 문서 템플릿
