---
name: feature-planner
description: Phase 기반 기능 구현 계획을 생성합니다. Quality gate와 점진적 전달 구조를 갖춘 계획을 수립합니다. 기능 계획, 작업 구성, 태스크 분해, 로드맵 작성, 개발 전략 수립 시 사용하세요. 키워드: plan, planning, phases, breakdown, strategy, roadmap, organize, structure, outline.
---

# Feature Planner

## 목적
다음 원칙에 따라 구조화된 Phase 기반 계획을 생성합니다:
- 각 Phase는 완전하고 실행 가능한 기능을 전달
- Quality gate로 다음 Phase 진행 전 검증을 강제
- 작업 시작 전 사용자의 계획 승인 필수
- Markdown checkbox로 진행 상황 추적
- 각 Phase는 최대 1~4시간

## 계획 수립 워크플로우

### Step 1: 요구사항 분석
1. 관련 파일을 읽어 코드베이스 아키텍처 파악
2. 의존성 및 연동 지점 식별
3. 복잡도와 리스크 평가
4. 적절한 범위 결정 (소규모/중규모/대규모)

### Step 2: TDD 통합 Phase 분해
기능을 3~7개 Phase로 분해하며, 각 Phase는:
- **Test-First**: 구현 전에 테스트를 먼저 작성
- 동작하고 테스트 가능한 기능을 전달
- 최대 1~4시간 소요
- Red-Green-Refactor 사이클 준수
- 측정 가능한 테스트 커버리지 요구사항 보유
- 독립적으로 rollback 가능
- 명확한 성공 기준 보유

**Phase 구조**:
- Phase 이름: 명확한 전달물
- 목표: 이 Phase가 생산하는 동작하는 기능
- **테스트 전략**: 테스트 유형, 커버리지 목표, 테스트 시나리오
- 태스크 (TDD 워크플로우 순서):
  1. **RED 태스크**: 실패하는 테스트를 먼저 작성
  2. **GREEN 태스크**: 테스트를 통과시키는 최소한의 코드 구현
  3. **REFACTOR 태스크**: 테스트가 통과하는 상태에서 코드 품질 개선
- Quality Gate: TDD 준수 + 검증 기준
- 의존성: 시작 전 갖춰져야 할 것
- **커버리지 목표**: 이 Phase의 구체적인 백분율 또는 체크리스트

### Step 3: 계획 문서 생성
plan-template.md를 사용하여 생성: `docs/plans/PLAN_<기능명>.md`

포함 사항:
- 개요 및 목표
- 근거가 포함된 아키텍처 결정 사항
- checkbox가 포함된 전체 Phase 분해
- Quality gate 체크리스트
- 리스크 평가 테이블
- Phase별 rollback 전략
- 진행 상황 추적 섹션
- 메모 및 학습 내용 영역

### Step 4: 사용자 승인
**필수**: AskUserQuestion을 사용하여 명시적 승인을 받을 것.

질문:
- "이 Phase 분해가 프로젝트에 적합합니까?"
- "제안된 접근 방식에 우려사항이 있습니까?"
- "계획 문서를 생성해도 될까요?"

사용자가 승인을 확인한 후에만 계획 문서를 생성할 것.

### Step 5: 문서 생성
1. `docs/plans/` 디렉토리가 없으면 생성
2. 모든 checkbox가 체크되지 않은 상태로 계획 문서 생성
3. 헤더에 quality gate에 대한 명확한 안내 추가
4. 사용자에게 계획 위치와 다음 단계 안내

## Quality Gate 기준

각 Phase는 다음 Phase로 진행하기 전에 반드시 아래 항목을 검증해야 합니다:

**빌드 & 컴파일**:
- [ ] 프로젝트가 에러 없이 빌드/컴파일됨
- [ ] 구문 오류 없음

**Test-Driven Development (TDD)**:
- [ ] 프로덕션 코드 작성 전에 테스트를 먼저 작성함
- [ ] Red-Green-Refactor 사이클을 따름
- [ ] Unit test: 비즈니스 로직 ≥80% 커버리지
- [ ] Integration test: 핵심 사용자 플로우 검증
- [ ] 테스트 suite가 허용 가능한 시간 내 실행 (<5분)

**테스트**:
- [ ] 기존 모든 테스트 통과
- [ ] 새 기능에 대한 테스트 추가
- [ ] 테스트 커버리지 유지 또는 향상

**코드 품질**:
- [ ] Lint 에러 없이 통과
- [ ] Type 검사 통과 (해당 시)
- [ ] 코드 포맷팅 일관성 유지

**기능**:
- [ ] 수동 테스트로 기능 정상 동작 확인
- [ ] 기존 기능에 regression 없음
- [ ] Edge case 테스트 완료

**보안 & 성능**:
- [ ] 새로운 보안 취약점 없음
- [ ] 성능 저하 없음
- [ ] 리소스 사용량 허용 범위 내

**문서화**:
- [ ] 코드 주석 업데이트
- [ ] 문서가 변경 사항 반영

## 진행 상황 추적 프로토콜

계획 문서 헤더에 다음을 추가:

```markdown
**필수 지침**: 각 Phase 완료 후:
1. 완료된 태스크 checkbox를 체크
2. 모든 quality gate 검증 명령 실행
3. 모든 quality gate 항목 통과 확인
4. "최종 수정일" 날짜 업데이트
5. 메모 섹션에 학습 내용 기록
6. 그 후에만 다음 Phase로 진행

**Quality gate를 건너뛰거나 실패한 상태로 진행하지 말 것**
```

## Phase 규모 가이드라인

**소규모** (2~3 Phase, 총 3~6시간):
- 단일 컴포넌트 또는 간단한 기능
- 최소한의 의존성
- 명확한 요구사항
- 예시: 다크 모드 토글 추가, 새 폼 컴포넌트 생성

**중규모** (4~5 Phase, 총 8~15시간):
- 다수 컴포넌트 또는 중간 규모 기능
- 어느 정도의 연동 복잡성
- 데이터베이스 변경 또는 API 작업
- 예시: 사용자 인증 시스템, 검색 기능

**대규모** (6~7 Phase, 총 15~25시간):
- 여러 영역에 걸친 복잡한 기능
- 상당한 아키텍처 영향
- 다수 연동
- 예시: AI 기반 검색 with embeddings, 실시간 협업

## 리스크 평가

식별 및 문서화:
- **기술 리스크**: API 변경, 성능 이슈, 데이터 마이그레이션
- **의존성 리스크**: 외부 라이브러리 업데이트, 서드파티 서비스 가용성
- **일정 리스크**: 복잡도 불확실성, blocking 의존성
- **품질 리스크**: 테스트 커버리지 공백, regression 가능성

각 리스크에 대해 명시:
- 발생 확률: 낮음/중간/높음
- 영향도: 낮음/중간/높음
- 완화 전략: 구체적인 실행 단계

## Rollback 전략

각 Phase에 대해 이슈 발생 시 변경사항을 되돌리는 방법을 문서화.
고려 사항:
- 되돌려야 할 코드 변경사항
- 되돌려야 할 데이터베이스 migration (해당 시)
- 복원할 설정 변경사항
- 제거할 의존성

## 테스트 사양 가이드라인

### Test-First 개발 워크플로우

**각 기능 컴포넌트에 대해**:
1. **테스트 케이스 명세** (코드 작성 전에)
   - 어떤 입력을 테스트할 것인가?
   - 어떤 출력이 예상되는가?
   - 어떤 edge case를 처리해야 하는가?
   - 어떤 에러 조건을 테스트해야 하는가?

2. **테스트 작성** (Red Phase)
   - 실패할 테스트를 작성
   - 올바른 이유로 테스트가 실패하는지 확인
   - 테스트를 실행하여 실패 확인
   - TDD 준수를 추적하기 위해 실패하는 테스트를 commit

3. **코드 구현** (Green Phase)
   - 테스트를 통과시키는 최소한의 코드 작성
   - 자주 테스트 실행 (2~5분마다)
   - 모든 테스트가 통과하면 중단
   - 테스트 범위를 넘어서는 추가 기능 없음

4. **Refactor** (Blue Phase)
   - 테스트가 통과하는 상태를 유지하면서 코드 품질 개선
   - 중복 로직 추출
   - 네이밍과 구조 개선
   - 각 리팩토링 단계 후 테스트 실행
   - 리팩토링 완료 시 commit

### 테스트 유형

**Unit Test**:
- **대상**: 개별 함수, 메서드, 클래스
- **의존성**: 없음 또는 mock/stub 처리
- **속도**: 빠름 (테스트당 <100ms)
- **격리**: 외부 시스템으로부터 완전 격리
- **커버리지**: 비즈니스 로직의 ≥80%

**Integration Test**:
- **대상**: 컴포넌트/모듈 간 상호작용
- **의존성**: 실제 의존성 사용 가능
- **속도**: 중간 (테스트당 <1s)
- **격리**: 컴포넌트 경계를 테스트
- **커버리지**: 핵심 연동 지점

**End-to-End (E2E) Test**:
- **대상**: 완전한 사용자 워크플로우
- **의존성**: 실제 또는 유사 실제 환경
- **속도**: 느림 (수 초~수 분)
- **격리**: 전체 시스템 통합
- **커버리지**: 핵심 사용자 여정

### 테스트 커버리지 계산

**커버리지 임계값** (프로젝트에 맞게 조정):
- **비즈니스 로직**: ≥90% (핵심 코드 경로)
- **Data Access Layer**: ≥80% (repository, DAO)
- **API/Controller Layer**: ≥70% (endpoint)
- **UI/Presentation**: 커버리지보다 integration test 선호

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
test '동작 설명':
  // Arrange: 테스트 데이터 및 의존성 설정
  input = createTestData()

  // Act: 테스트 대상 동작 실행
  result = systemUnderTest.method(input)

  // Assert: 예상 결과 검증
  assert result == expectedOutput
```

**Given-When-Then (BDD 스타일)**:
```
test '기능이 특정 방식으로 동작해야 함':
  // Given: 초기 컨텍스트/상태
  given userIsLoggedIn()

  // When: 액션 발생
  when userClicksButton()

  // Then: 관찰 가능한 결과
  then shouldSeeConfirmation()
```

**의존성 Mocking/Stubbing**:
```
test '컴포넌트가 의존성을 호출해야 함':
  // mock/stub 생성
  mockService = createMock(ExternalService)
  component = new Component(mockService)

  // mock 동작 설정
  when(mockService.method()).thenReturn(expectedData)

  // 실행 및 검증
  component.execute()
  verify(mockService.method()).calledOnce()
```

### 계획 내 테스트 문서화

**각 Phase에서 명시할 사항**:
1. **테스트 파일 위치**: 테스트가 작성될 정확한 경로
2. **테스트 시나리오**: 구체적인 테스트 케이스 목록
3. **예상 실패**: 테스트가 초기에 보여야 할 에러는?
4. **커버리지 목표**: 이 Phase의 백분율
5. **Mock할 의존성**: 무엇을 mocking/stubbing 해야 하는가?
6. **테스트 데이터**: 어떤 fixture/factory가 필요한가?

## 참고 파일
- [plan-template.md](plan-template.md) - 전체 계획 문서 템플릿
