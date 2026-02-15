## 📌 프로젝트 소개

**K-Bulkup**은 금융 초보자를 대상으로 금융 교육과 자산 관리를 결합한 플랫폼입니다.

전문가는 본인의 커리큘럼 기반으로 금융 강의를 제공하고, 사용자는 강의 수강 및 과제 수행을 통해 실전 금융 지식과 자산 관리 역량을 함께 향상시킬 수 있습니다. 또한 교육 수료 후에는 개인 자산 데이터를 기반으로 한 1:1 맞춤형 컨설팅을 제공받을 수 있습니다.

본 저장소는 기존 팀 프로젝트를 Fork하여, **성능 최적화**, **트래픽 대응**, **실시간 처리** 구조 개선을 중심으로 백엔드 아키텍처 전반을 **리팩토링**한 프로젝트입니다. 

특히 **부하 테스트** 기반 병목 지점 분석과 구조 개선에 초점을 맞추어, 실제 운영 환경을 가정한 안정성 검증을 수행했습니다.

- 팀 프로젝트 원본 저장소 : https://github.com/K-Bulkup
- 외부 Mock 서버 : https://github.com/Jung-kr/kbulkup-mockserver

<br>

## 🏗️ 시스템 아키텍처

<img width="1920" height="1080" alt="3조 (발표자료)" src="https://github.com/user-attachments/assets/b2f61f49-3606-45c0-8763-6b0a5bad6a05" />

- 단일 EC2 환경에서 **Nginx 리버스 프록시**를 활용해 정적 리소스와 API 트래픽을 분리
- Docker 기반 **컨테이너 격리**로 실행 환경 표준화 및 **배포 안정성** 확보
- MySQL · MongoDB · Redis · S3를 **데이터 특성별 분리 설계**하여 저장 책임 분리
- 사용자 자산 데이터를 제공하는 **외부 Mock 서버**를 별도로 구축
- `t3.micro` 자원 제약 환경을 고려한 **Memory Swap** + 사전 **부하 테스트** 기반 안정성 검증

<br>

## 🛠 기술 스택

| 구분           | 기술                                       |
|--------------|--------------------------------------------|
| Language     | Java 17                                    |
| Framework    | Spring Framework 5.3.30                    |
| Database & SQL Mapper | MySQL, MongoDB, MyBatis            |
| Cache        | Redis                                      |
| Infra        | AWS, Docker, NginX                         |
| CI/CD        | GitHub Actions                             |
| Test & Docs  | JUnit5, Mockito, Swagger                  |

<br>

## 🚀 핵심 구현 내용

**1️⃣ 복합 인덱스 기반 대용량 조회 쿼리 최적화**

- **기능:** 특정 계좌의 기간별 거래 내역 조회 API
- **문제:** 단일 인덱스 구조로 Filesort + 엔진 레벨 필터링 → 815ms
- **해결:** 필터 + 정렬 조건 반영한 복합 인덱스 설계 → Range Scan 유도  
  👉 **_거래 내역 조회 815ms → 505ms (38% 개선), Filesort 제거_**

**2️⃣ Redis 캐싱으로 핵심 API 조회 성능 개선**

- **기능:** 사용자의 전체 자산 내역 조회 API
- **문제:** 외부 API + 다중 DB 조회 구조로 평균 응답 6.55초 → UX 저하
- **해결:** Cache Aside 전략 + 데이터 특성별 TTL 분리(30분 / 24시간) 적용  
  👉 **_자산 조회 응답 6.55초 → 67ms (99% 단축), DB·외부 API 부하 감소_**

**3️⃣ 커서 기반 페이지네이션 도입으로 대용량 목록 조회 최적화**

- **기능:** 사용자 대상 강의 목록 조회 API
- **문제:** Offset 기반 구조로 10만 건 환경에서 Full Scan + Filesort → 1.32초
- **해결:** 커서 기반 페이지네이션 + 복합 인덱스 적용  
  👉 **_10만 건 환경에서 응답 1.32s → 652ms (50% 개선), 무한 스크롤 UX 구현_**

**4️⃣ MongoDB 대량 Update + 인덱스로 실시간 채팅 성능 개선**

- **기능:** 채팅방 입장 시 미읽음 메시지 일괄 읽음 처리
- **문제:** 개별 save() 반복 + COLLSCAN → 1.56초 지연
- **해결:** Bulk Update + 복합 인덱스 적용 → 서버 내부 일괄 처리  
  👉 **_읽음 처리 1.56s → 101ms (93% 개선), COLLSCAN → IXSCAN 전환_**

**5️⃣ WebSocket 메시지 처리 구조 개선 & 부하 테스트 기반 병목 제거**

- **기능:** WebSocket 기반 실시간 채팅 메시지 전송
- **문제:** 다중 DB 접근 + 동기 처리 → 200 msg/s 병목 발생
- **해결:** 동기 처리 최소화 + 비동기 저장 + Retry + Redis Queue 구조 재설계  
  👉 **_메시지 처리량 200 → 303 msg/s (51% 개선), 동시 200명 안정 처리_**

<br>

## 🗄️ ERD

### ✅ Service Server
<img width="1920" height="1080" alt="20" src="https://github.com/user-attachments/assets/71cf56a7-3fbe-46a0-98bf-80004de85bfe" />

### ✅ External Mock Server
<img width="1920" height="1080" alt="21" src="https://github.com/user-attachments/assets/328a3707-a176-404b-bf71-3634ef016095" />
