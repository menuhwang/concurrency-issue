# 동시성 문제와 트랜잭션
   동시성 문제 해결을 위해 트랜잭션 락을 실습해보고 테스트해보기 위한 프로젝트입니다.
## 프로젝트 환경

- SpringBoot 3.0.5
- JAVA 17
- MySQL 8.0

## 테스트 설정
- Database 생성 필요 (이름 : board)
- `application.properties` 에 `datasource` 값 본인 환경에 맞게 설정
  - `spring.datasource.url=jdbc:mysql://localhost:3306/transaction-study`
  - `spring.datasource.username=root`
  - `spring.datasource.password=비밀번호 입력`

## 설명

데모 게시판을 만들어 게시글에 좋아요를 동시에 눌렀을 경우를 테스트합니다.

실제 Application을 구동하는 통합테스트로 진행했습니다.

### 예시

#### 좋아요 로직

- 게시물 1 좋아요 -> 게시물 1을 읽어온다 -> 좋아요 +1 로 수정한다.

#### 사용자 1과 사용자 2가 동시에 좋아요를 누른 경우

- 사용자1 : 게시물 1의 좋아요 = 1 -> 1 + 1 = 2 저장

- 사용자2 : 게시물 1의 좋아요 = 1 -> 1 + 1 = 2 저장

- 정상적인 기댓값 : 1 + 1 + 1 = 3

### ???
- 낙관락으로 처리한 좋아요 로직이 수행되는 동안 게시물 수정이 요청되면 어떻게 되는가?
- 좋아요를 비관락-배타락으로 처리하면?
  - 셀럽이 인스타에 글을 올렸을 경우
  - 순식간에 하트가 파바박!
  - 좋아요 로직이 처리되는 동안 게시물을 읽어 올 수 없는 것인가?
- 동시에 배타락을 걸면 데드락?

## 관련 내용 정리 (추가 예정)

### 트랜잭션

트랜잭션 : "쪼갤 수 없는 업무 처리의 최소 단위"

#### ACID

- Atomicity (원자성) :
  - 하나의 트랜잭션은 온전하게 모두 완료, 반영되어야함.
  - 만약, 하나라도 실패한다면 작업했던 내역을 모두 롤백하여 이전 상태로 복원한다.
  
- Consistency (일관성) :
  - 트랜잭션 작업 처리 결과가 항상 일관되어야함.
  - 트랜잭션이 진행되는 동안 데이터베이스가 변경되더라도 업데이트된 데이터베이스로 트랜잭션이 진행되는 것이 아니라, 처음 트랜잭션을 진행하기 위해 참조한 데이터베이스로 진행된다.
- Isolation (고립성) :
  - 하나의 트랜잭셔 수행시 다른 트랜잭션의 작업이 끼어들지 못하도록 보장.
  - 트랜잭션 끼리 간섭할 수 없음.
- Durability (지속성) : 
  - 트랜잭션이 정상적으로 종료되면 영구적으로 DB에 작업 결과가 저장되어야함.

#### Lock

- Shared Lock (공유락) : 
  - 한 트랜잭션이 자원을 사용하고 있는 경우, 다른 트랜잭션에서 읽을 수는 있지만, 수정할 수는 없다.
  - 공유락이 걸린 자원은, 다른 트랜잭션에서 공유락은 걸수 있지만, 배타락은 걸 수 없다.
- Exclusive Lock (배타락) : 
  - 배타락이 걸린 트랜잭션은 다른 트랜잭션이 읽기, 쓰기 모두 할 수 없다.
  - 또한 다른 트랜잭션에서 공유락과 배타락 모두 걸 수 없다.

#### Isolation Level
> Dirty Read : 현재 트랜잭션에서 커밋되지 않은 변경 데이터를 다른 트랜잭션이 읽을 수 있음을 말함.
> 
> Non Repeatable Read: 가장 먼저 데이터를 읽은 데이터가, 다른 트랜잭션에서 변경됐고, 이후 다시 데이터를 읽을때 변경된 데이터를 읽을 수 있음을 의미한다. (즉, 먼저 변경한 쪽의 데이터를 다시 읽게 됨을 의미)
>
> Phantom read: 다른 트랜잭션이 신규 데이터를 추가하거나, 기존 데이터를 삭제할때, 범위 쿼리를 수행하면 데이터 row 가 달라지는 현상을 말한다.
- Read Uncommitted :
  - 트랜잭션에서 처리 중인 아직 커밋되지 않은 데이터를 다른 트랜잭션이 읽는 것을 허용.
  - Dirty Read, Non-Repeatable Read, Phantom Read 현상 발생
- Read Committed :
  - 트랜잭션이 커밋된 데이터만 읽는 것을 허용.
  - Dirty Read 방지
  - Non-Repeatable Read, Phantom Read 현상 발생.
  - 대부분의 DBMS 가 기본모드로 채택.
- Repeatable Read :
  - 커밋되지 않은 변경점에 영향을 받지 않는다.
  - 행에 대한 쿼리를 다시 요청한 경우, 같은 결과를 받지만, 범위 쿼리를 실행할 경우 Phantom Read 현상이 발생힌다.
  - Dirty Read, Non-Repeatable Read 방지.
- Serializable :
  - 가장 엄격한 레벨.
  - 동시에 들어온 트랜잭션을 순자적으로 처리한다.
  - 동시성에 관한 영향이 없다.
  - 따라서, 성능이 매우 떨어진다.

#### 낙관락 (Optimistic Lock) && 비관락 (Pessimistic Lock)
낙관락과 비관락은 동시성 제어 방법 중 하나로, 여러 사용자가 동시에 접근할 때 발생하는 충돌을 방지하기 위해 사용된다.

- 낙관락
  - 데이터를 읽을 때는 락을 걸지 않음.
  - 변경할 때만 락을 걸어 충돌을 방지한다.
  - 여러 사용자가 동시에 읽기 작업을 수행하더라도 락이 걸리지 않아 빠른 속도로 작업이 수행됨.
  - 데이터를 변경할 때는 이전에 읽은 데이터와 비교하여 충돌이 발생하는지 확인하고 충돌이 발생한 경우 롤백하거나 다시 시도함.
  - 롤백이나 재시도를 직접 처리해줘야함.
- 비관락
  - 데이터를 읽을 때 락을 걸어 다른 사용자가 접근하지 못하도록 한다.
  - 충돌이 발생하지 않아 일관성을 보장할 수 있지만, 락을 걸고 있어 성능이 떨어진다.

낙관락은 충돌이 발생하지 않는 경우에 성능이 높아지므로, 충돌 발생에 낙관적(충돌 안나유~~)일 때 사용.
비관락은 실시간으로 데이터 일관성을 보장해야할 경우 사용.

## DB 트랜잭션 락 테스트

### MySQL 트랜잭션 관련 쿼리문

- 트랜잭션 목록 : `select * from information_schema.innodb_trx;`
- 트랜잭션 대기 목록 : `select * from sys.innodb_lock_waits;`

### 테스트 케이스
- 상황 1 : T1이 shard락을 걸고 조회하는 도중 T2가 shard락을 걸고 조회하는 경우 (s락에 s락을 거는 경우)
  - T1
    ```sql
    begin;
    select *
    from board
    where id = 1
    for share ;
    ```
  - T2
    ```sql
    begin;
    select *
    from board
    where id = 1
    for share ;
    ```
  - 결과
    - waiting 없이 정상적으로 동작
- 상황 2 : T1이 x락을 걸고 update 하는 도중 T2가 shard락을 걸고 조회하는 경우 (x락에 s락을 거는 경우)
  - T1
    ```sql
    begin;
    update board
    set likes = likes + 1
    where id = 1;
    ```
  - T2
    ```sql
    begin;
    select *
    from board
    where id = 1
    for share ;
    ```
  - 결과
    - T2 blocking
    - T1이 commit 또는 rollback되어 트랜잭션이 종료되면 T2 진행.
- 상황 3 : T1이 x락을 걸고 update 하는 도중 T2가 락없이 조회하는 경우 (x락이 걸린 레코드를 락없이 조회하는 경우)
  - T1
    ```sql
    begin;
    update board
    set likes = likes + 1
    where id = 1;
    ```
  - T2
    ```sql
    begin;
    select *
    from board
    where id = 1;
    ```
  - 결과
    - waiting 없이 정상적으로 동작.
    - T2는 T1이 커밋되기 전이므로 기존 데이터를 조회해옴. (Read Committed)
- 상황 4 : T1이 s락을 걸고 조회하는 도중 T2가 x락을 걸고 update하는 경우 (s락에 x락을 거는 경우)
  - T1
    ```sql
    begin;
    select *
    from board
    where id = 1
    for shard;
    ```
  - T2
    ```sql
    begin;
    update board
    set likes = likes + 1
    where id = 1;
    ```
  - 결과
    - T2 blocking
    - T1이 종료되면 T2 진행.