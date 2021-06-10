"# 1588-pizza" 

# PIZZA 통합주문콜센터

- 체크포인트 : https://workflowy.com/s/assessment-check-po/T5YrzcMewfo4J6LW


# Table of contents

- [온라인서점](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [분석/설계](#분석설계)
    - [Event Storming 결과](#Event-Storming-결과)
    - [헥사고날 아키텍처 다이어그램 도출](#헥사고날-아키텍처-다이어그램-도출)
  - [구현:](#구현:)
    - [DDD 의 적용](#DDD-의-적용)
    - [기능적 요구사항 검증](#기능적-요구사항-검증)
    - [비기능적 요구사항 검증](#비기능적-요구사항-검증)
    - [Saga](#saga)
    - [CQRS](#cqrs)
    - [Correlation](#correlation)
    - [GateWay](#gateway)
    - [Polyglot](#polyglot)
    - [동기식 호출(Req/Resp) 패턴](#동기식-호출reqresp-패턴)
    - [비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트](#비동기식-호출--시간적-디커플링--장애격리--최종-eventual-일관성-테스트)
  - [운영](#운영)
    - [Deploy / Pipeline](#deploy--pipeline)
    - [Config Map](#configmap)
    - [Secret](#secret)
    - [Circuit Breaker와 Fallback 처리](#circuit-breaker와-fallback-처리)
    - [오토스케일 아웃](#오토스케일-아웃)
    - [Zero-downtime deploy (Readiness Probe) 무정지 재배포](#zero-downtime-deploy-readiness-probe-무정지-재배포)
    - [Self-healing (Liveness Probe))](#self-healing-liveness-probe)

# 서비스 시나리오

기능적 요구사항
1. 고객이 피자를 주문한다.
2. 고객 주문이 완료되면 해당지역 체인점에 주문이 접수된다.
3. 체인점에서 피자 조리가 완료되면 지배인(Master)이 "조리완료" 처리한다.
4. 피자 조리가 완료되면 배달을 시작한다.
5. 고객이 마이페이지를 통해 주문 상태를 확인할 수 있다.
6. 고객이 주문을 취소할 수 있다.
7. 관리자가 신규 체인점을 등록할 수 있다.

비기능적 요구사항
1. 트랜잭션
    1. 주문 시 해당 지역의 체인점 중 "영업중"인 곳이 단 한 곳도 없다면 주문이력만 남기고 주문은 거절된다. (Sync 호출)
1. 장애격리
    1. 고객센터/배달 기능이 수행되지 않더라도 주문은 365일 24시간 받을 수 있어야 한다  Async(event-driven), Eventual Consistency
    2. 체인점 시스템이 과중되면 주문을 잠시동안 받지 않고 재주문하도록 유도한다  Circuit breaker, fallback


# 분석/설계


## Event Storming 결과
* MSAEz 로 모델링한 이벤트스토밍 결과:


### 이벤트 도출
![image](https://user-images.githubusercontent.com/20077391/121368642-2dffe100-c976-11eb-8cf2-8fedc5cbda74.png)

### 부적격 이벤트 탈락
![image](https://user-images.githubusercontent.com/20077391/121368470-10cb1280-c976-11eb-9bfa-8d1fe654c50f.png)

### 완성된 1차 모형
![image](https://user-images.githubusercontent.com/20077391/121369235-a1095780-c976-11eb-964f-172f756d66ce.png)


### 1차 완성본에 대한 기능적 요구사항을 커버하는지 검증
![image](https://user-images.githubusercontent.com/20077391/121370919-17f32000-c978-11eb-8348-67da8294dd0e.png)

1. 고객이 피자를 주문한다.
2. 고객 주문이 완료되면 해당지역 체인점에 주문이 접수된다.
3. 체인점에서 피자 조리가 완료되면 지배인(Master)이 "조리완료" 처리한다.
4. 피자 조리가 완료되면 배달을 시작한다.
5. 고객이 마이페이지를 통해 주문 상태를 확인할 수 있다.
6. 고객이 주문을 취소할 수 있다.
7. 관리자가 신규 체인점을 등록할 수 있다.

### 1차 완성본에 대한 비기능적 요구사항을 커버하는지 검증
![image](https://user-images.githubusercontent.com/20077391/121371566-9a7bdf80-c978-11eb-8a2e-9f37c13f8013.png)

비기능적 요구사항
1. 트랜잭션
    1. 주문 시 해당 지역의 체인점 중 "영업중"인 곳이 단 한 곳도 없다면 주문이력만 남기고 주문은 거절된다. (Sync 호출)
1. 장애격리
    1. 고객센터/배달 기능이 수행되지 않더라도 주문은 365일 24시간 받을 수 있어야 한다  Async(event-driven), Eventual Consistency
    2. 체인점 시스템이 과중되면 주문을 잠시동안 받지 않고 재주문하도록 유도한다  Circuit breaker, fallback


### 최종 완성된 모형
![image](https://user-images.githubusercontent.com/20077391/121464276-8f639680-c9ee-11eb-9649-5672d7c2737a.png)


## 헥사고날 아키텍처 다이어그램 도출 (ㅌㅌㅌㅌㅌㅌㅌㅌㅌㅌㅌㅌㅌㅌㅌㅌㅌㅌㅌ)

![image](https://user-images.githubusercontent.com/84316082/120965636-238bee80-c7a0-11eb-80b4-f22239207caa.png)
    
    - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 PubSub 과 Req/Resp 를 구분함
    - 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐


# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 808n 이다)

```
cd gateway
mvn spring-boot:run

cd order
mvn spring-boot:run 

... 이하 동일(생략) ...
```

## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다: (예시는 Order 마이크로서비스). 이때 가능한 현업에서 사용하는 언어 (유비쿼터스 랭귀지)를 그대로 사용하려고 노력하였다. 

```
package pizza;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.Date;

@Entity
@Table(name="Order_table")
public class Order {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long orderId;
    private Long customerId;
    private String pizzaNm;
    private Integer qty;
    private String status;
    private String regionNm;
    private Date orderDt;

    @PrePersist
    public void onPrePersist(){
        //Following code causes dependency to external APIs
        // Req/Res Calling
        boolean bResult = false;

        // mappings goes here
        bResult = OrderApplication.applicationContext.getBean(pizza.external.StoreService.class).chkOpenYn(this.regionNm);

        // 주문가능 (해당 regionNm에 Open된 Store가 있음)
        if (bResult) {
            this.status = "Ordered" ;
        } else {
            this.status = "NoStoreOpened" ;
        }

        this.orderDt = new Date();
    }

    @PostPersist
    public void onPostPersist(){

        if ("Ordered".equals(this.status)) {
            System.out.println("#### PUB :: Ordered : orderId = " + this.orderId);
            Ordered ordered = new Ordered();
            BeanUtils.copyProperties(this, ordered);
            ordered.publishAfterCommit();
        } else if ("NoStoreOpened".equals(this.status)) {
            System.out.println("#### PUB :: OrderRejected : orderId = " + this.orderId);
            OrderRejected orderRejected = new OrderRejected();
            BeanUtils.copyProperties(this, orderRejected);
            orderRejected.publishAfterCommit();
        }
    }

    @PostUpdate
    public void onPostUpdate(){
        if(this.status.equals("OrderCancelled"))
        {
            System.out.println("#### PUB :: OrderCancelled : orderId = " + this.orderId);
            OrderCancelled orderCancelled = new OrderCancelled();
            BeanUtils.copyProperties(this, orderCancelled);
            orderCancelled.publishAfterCommit();
        } else {
            System.out.println("#### PUB :: StatusUpdated : status updated to " + this.status);
            StatusUpdated statusUpdated = new StatusUpdated();
            BeanUtils.copyProperties(this, statusUpdated);
            statusUpdated.publishAfterCommit();
        }
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    public String getPizzaNm() {
        return pizzaNm;
    }

    public void setPizzaNm(String pizzaNm) {
        this.pizzaNm = pizzaNm;
    }
    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getRegionNm() {
        return regionNm;
    }

    public void setRegionNm(String regionNm) {
        this.regionNm = regionNm;
    }
    public Date getOrderDt() {
        return orderDt;
    }

    public void setOrderDt(Date orderDt) {
        this.orderDt = orderDt;
    }
}
```
- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (MySQL or H2)에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다. (로컬 개발환경에서는 MySQL/H2를, 쿠버네티스에서는 SQLServer/H2를 각각 사용하였다)
```
package pizza;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="orders", path="orders")
public interface OrderRepository extends PagingAndSortingRepository<Order, Long>{

    Optional<Order> findByOrderId(Long orderId);
}
```
- 적용 후 REST API 의 테스트
```
# Store 서비스의 신규 체인점 등록
http POST http://localhost:8088/stores regionNm="강남구" openYN=true

# Order 서비스의 주문
http POST http://localhost:8088/orders customerId=1 pizzaNm="페퍼로니피자" qty=1 regionNm="강남구"

# 주문 상태 확인
http GET http://localhost:8088/myPages/1

```

## 기능적 요구사항 검증

1. 고객이 피자를 주문한다.

--> 정상적으로 주문됨을 확인하였음
![image](https://user-images.githubusercontent.com/20077391/121451877-54a33380-c9d9-11eb-960e-7061bb138995.png)


2. 고객 주문이 완료되면 해당지역 체인점에 주문이 접수된다.

--> 정상적으로 주문이 접수됨을 확인하였음
![image](https://user-images.githubusercontent.com/20077391/121452388-4d305a00-c9da-11eb-9719-9f398801e0c9.png)


3. 체인점에서 피자 조리가 완료되면 체인점 지배인(Master)이 "조리완료" 처리한다.

--> 정상적으로 조리완료 처리됨을 확인하였음
![image](https://user-images.githubusercontent.com/20077391/121452939-3fc79f80-c9db-11eb-84d4-81c37682303d.png)


4. 피자 조리가 완료되면 배달을 시작한다.

--> 정상적으로 배달 시작됨을 확인하였음
![image](https://user-images.githubusercontent.com/20077391/121453046-730a2e80-c9db-11eb-864b-49c4855f20ad.png)


5. 고객이 마이페이지를 통해 주문 상태를 확인할 수 있다.

--> 정상적으로 조회됨 확인하였음
![image](https://user-images.githubusercontent.com/20077391/121453233-c7ada980-c9db-11eb-8fcf-0d3958ee0bfb.png)


6. 고객이 주문을 취소할 수 있다.

--> 정상적으로 취소됨을 확인하였음
![image](https://user-images.githubusercontent.com/20077391/121465258-6f34d700-c9f0-11eb-8dcf-b43771965440.png)


7. 관리자가 신규 체인점을 등록할 수 있다.

--> 정상적으로 등록됨을 확인하였음
![image](https://user-images.githubusercontent.com/20077391/121465434-c5a21580-c9f0-11eb-90eb-ffd73b821d6a.png)


## 비기능적 요구사항 검증

1. 트랜잭션

주문 시 해당 지역의 체인점 중 "영업중"인 곳이 단 한 곳도 없다면 주문이력만 남기고 주문은 거절된다. (Sync 호출)

![image](https://user-images.githubusercontent.com/20077391/121465789-377a5f00-c9f1-11eb-99a9-989d87630653.png)


2. 장애격리
고객센터/배달 기능이 수행되지 않더라도 주문은 365일 24시간 받을 수 있어야 한다  Async(event-driven), Eventual Consistency

![image](https://user-images.githubusercontent.com/20077391/121466075-bff8ff80-c9f1-11eb-9a20-68e5455850eb.png)
![image](https://user-images.githubusercontent.com/20077391/121466182-f20a6180-c9f1-11eb-94d8-e6e22168ad45.png)


3. 상점시스템이 과중되면 주문을 잠시동안 받지 않고 재접속하도록 유도한다 Circuit breaker, fallback

--> 뒤의 Hystrix를 통한 Circuit Break 구현에서 검증하도록 한다.


## Saga
분석/설계 및 구현을 통해 이벤트를 Publish/Subscribe 하도록 구현하였다.
[Publish]
![image](https://user-images.githubusercontent.com/20077391/121466412-5decca00-c9f2-11eb-8e95-b783c193db96.png)

[Subscribe]
![image](https://user-images.githubusercontent.com/20077391/121466502-7e1c8900-c9f2-11eb-92df-2edb04293844.png)


## CQRS
Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능하게 구현해 두었다.

본 프로젝트에서 View 역할은 CustomerCenter 서비스가 수행한다.

CQRS를 구현하여 주문건에 대한 상태는 Order 마이크로서비스의 접근없이 CustomerCenter의 마이페이지를 통해 조회할 수 있도록 구현하였다.

- 주문(ordered) 실행 후 myPage 화면

![image](https://user-images.githubusercontent.com/20077391/121016627-4f769680-c7d7-11eb-8f60-f9640223c1ec.png)


- 주문취소(OrderCancelled) 후 myPage 화면

![image](https://user-images.githubusercontent.com/20077391/120961678-3d760300-c799-11eb-829c-16f296d61f27.png)


위와 같이 주문을 하게되면 Order -> Book -> Order -> Delivery 로 주문이 Assigend 되고

주문 취소가 되면 Status가 "Delivery Cancelled"로 Update 되는 것을 볼 수 있다.

## Correlation 
각 이벤트 건(메시지)이 어떤 Policy를 처리할 때 어떤건에 연결된 처리건인지를 구별하기 위한 Correlation-key를 제대로 연결하였는지를 검증하였다.
![image](https://user-images.githubusercontent.com/20077391/121104779-b333aa80-c83d-11eb-9110-e56c6be57c86.png)

## GateWay 
API GateWay를 통하여 마이크로 서비스들의 진입점을 통일할 수 있다.
다음과 같이 GateWay를 적용하여 모든 마이크로서비스들은 http://localhost:8088/{context}로 접근할 수 있다.

``` (gateway) application.yaml

server:
  port: 8088
---
spring:
  profiles: default
  cloud:
    gateway:
      routes:
        - id: CustomerCenter
          uri: http://localhost:8081
          predicates:
            - Path= /myPages/**
        - id: Book
          uri: http://localhost:8082
          predicates:
            - Path=/books/** 
        - id: Order
          uri: http://localhost:8083
          predicates:
            - Path=/orders/** 
        - id: Delivery
          uri: http://localhost:8084
          predicates:
            - Path=/deliveries/** 
        - id: customer
          uri: http://localhost:8085
          predicates:
            - Path=/customers/** 
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true
---
spring:
  profiles: docker
  cloud:
    gateway:
      routes:
        - id: customercenter
          uri: http://customercenter:8080
          predicates:
            - Path= /marketingTargets/**,/outOfStockOrders/**,/myPages/**
        - id: Book
          uri: http://Book:8080
          predicates:
            - Path=/books/** 
        - id: Order
          uri: http://Order:8080
          predicates:
            - Path=/orders/** 
        - id: Delivery
          uri: http://Delivery:8080
          predicates:
            - Path=/deliveries/** 
        - id: customer
          uri: http://customer:8080
          predicates:
            - Path=/customers/** 
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true

server:
  port: 8080
```


## Polyglot

각 마이크로서비스의 다양한 요구사항에 능동적으로 대처하고자 최적의 구현언어 및 DBMS를 선택할 수 있다.
OnlineBookStore에서는 다음과 같이 2가지 DBMS를 적용하였다.
- MySQL(쿠버네티스에서는 SQLServer) : Book, CustomerCenter, Customer, Delivery
- H2    : Order

```
# (Book, CustomerCenter, Customer, Delivery) application.yml

spring:
  profiles: default
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/bookdb?useSSL=false&characterEncoding=UTF-8&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: *****
    password: *****

spring:
  profiles: docker
  datasource:
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
    url: jdbc:sqlserver://skccteam2.database.windows.net:1433;database=bookstore;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;
    username: ${SQLSERVER_USERNAME}
    password: ${SQLSERVER_PASSWORD}
...

# (Order) application.yml

spring:
  profiles: default
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:file:/data/orderdb
    username: *****
    password: 
```


## 동기식 호출(Req/Resp) 패턴

분석단계에서의 조건 중 하나로 주문(Order)->책 재고 확인(Book) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 
호출 프로토콜은 RestController를 FeignClient 를 이용하여 호출하도록 한다. 

- 재고 확인 서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

```
# (Order) BookService.java


package onlinebookstore.external;

@FeignClient(name="Book", url="${api.url.book}")
public interface BookService {

    @RequestMapping(method= RequestMethod.GET, path="/books/chkAndModifyStock")
    public boolean chkAndModifyStock(@RequestParam("bookId") Long bookId,
                                        @RequestParam("qty") int qty);

}
```

- 주문을 받은 직후 재고(Book) 확인을 요청하도록 처리
```
# BookController.java

package onlinebookstore;

 @RestController
 public class BookController {
     @Autowired  BookRepository bookRepository;

     @RequestMapping(value = "/books/chkAndModifyStock",
             method = RequestMethod.GET,
             produces = "application/json;charset=UTF-8")
     public boolean chkAndModifyStock(@RequestParam("bookId") Long bookId,
                                      @RequestParam("qty")  int qty)
             throws Exception {
             
         boolean status = false;
         Optional<Book> bookOptional = bookRepository.findByBookId(bookId);
         if (bookOptional.isPresent()) {
            Book book = bookOptional.get();
            // 현 재고보다 주문수량이 적거나 같은경우에만 true 회신
            if( book.getStock() >= qty){
                status = true;
                book.setStockBeforeUpdate(book.getStock());
                book.setStock(book.getStock() - qty); // 주문수량만큼 재고 감소
                bookRepository.save(book);
         }
      }

      return status;
  }

```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 재고 관리 시스템이 장애가 나면 주문도 못받는다는 것을 확인:


```
# 책 재고 관리 (Book) 서비스를 잠시 내려놓음 (ctrl+c)

#주문처리
http POST localhost:8088/orders bookId=1 qty=10 customerId=1   #Fail
http POST localhost:8088/orders bookId=2 qty=20 customerId=2   #Fail

#재고 관리 서비스 재기동
cd Book
mvn spring-boot:run

#주문처리
http POST localhost:8088/orders bookId=1 qty=10 customerId=1   #Success
http POST localhost:8088/orders bookId=2 qty=20 customerId=2   #Success
```
추후 운영단계에서는 Circuit Breaker를 이용하여 재고 관리 시스템에 장애가 발생하여도 주문 접수는 가능하도록 개선할 예정이다.


## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트

주문이 이루어진 후에 배송 시스템으로 이를 알려주는 행위는 동기식이 아니라 비 동기식으로 처리하여 배송 시스템의 처리를 위하여 주문이 블로킹 되지 않도록 처리한다.
 
- 이를 위하여 주문이력에 기록을 남긴 후에 곧바로 주문이 완료되었다는 도메인 이벤트를 카프카로 송출한다(Publish)
 
```
package onlinebookstore;

@Entity
@Table(name="Order_table")
public class Order {

 ...
    @PostPersist
    public void onPostPersist(){
        if(this.status.equals("Ordered"))
        {
            Ordered ordered = new Ordered();
            BeanUtils.copyProperties(this, ordered);
            ordered.publishAfterCommit();
            System.out.println("** PUB :: Ordered : orderId="+this.orderId);
        }
        else
        {
            OutOfStocked outOfStocked = new OutOfStocked();
            BeanUtils.copyProperties(this, outOfStocked);
            outOfStocked.publish();
            System.out.println("** PUB :: OutOfStocked : orderId="+this.orderId);
        }
    }

}
```
- 배송관리 서비스에서는 주문 완료 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다:

```
package onlinebookstore;

...

@Service
public class PolicyHandler{

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrdered_Delivery(@Payload Ordered ordered){

        if(!ordered.validate()) return;

        System.out.println("\n\n##### listener Delivery : " + ordered.toJson() + "\n\n");

        Delivery delivery = new Delivery();
        
        delivery.setOrderid(ordered.getOrderId());
        delivery.setDeliverystatus("Order-Delivery");         
        
        deliveryRepository.save(delivery);
            
    }
}

```

배송 시스템은 주문/재고관리와 완전히 분리되어있으며, 이벤트 수신에 따라 처리되기 때문에, 배송 시스템이 유지보수로 인해 잠시 내려간 상태라도 주문을 받는데 문제가 없다:
```
# 배송관리 서비스 (Delivery) 를 잠시 내려놓음 (ctrl+c)

#주문처리
http POST localhost:8088/orders bookId=1 qty=10 customerId=1   #Success
http POST localhost:8088/orders bookId=2 qty=20 customerId=2   #Success

#주문상태 확인
http localhost:8088/orders     # 주문상태 안바뀜 확인

#배송 서비스 기동
cd Delivery
mvn spring-boot:run

#주문상태 확인
http localhost:8080/orders     # 모든 주문의 상태가 "Delivery Started"로 확인
```


# 운영

## Deploy / Pipeline

- git에서 소스 가져오기
```
git clone https://github.com/aramidhwan/OnlineBookStore.git
```
- Build 하기
```
cd /book
mvn package

cd ../customer
mvn package

cd ../customercenter
mvn package

cd ../order
mvn package

cd ../delivery
mvn package

cd ../gateway
mvn package

```

- Docker Image build/Push/
```

cd ../gateway
docker build -t skccteam2acr.azurecr.io/gateway:latest .
docker push skccteam2acr.azurecr.io/gateway:latest

cd ../book
docker build -t skccteam2acr.azurecr.io/book:latest .
docker push skccteam2acr.azurecr.io/book:latest

cd ../customer
docker build -t skccteam2acr.azurecr.io/customer:latest .
docker push skccteam2acr.azurecr.io/customer:latest

cd ../customercenter
docker build -t skccteam2acr.azurecr.io/customercenter:latest .
docker push skccteam2acr.azurecr.io/customercenter:latest

cd ../order
docker build -t skccteam2acr.azurecr.io/order:latest .
docker push skccteam2acr.azurecr.io/order:latest

cd ../delivery
docker build -t skccteam2acr.azurecr.io/delivery:latest .
docker push skccteam2acr.azurecr.io/delivery:latest


```

- yml파일 이용한 deploy
```
kubectl apply -f deployment.yml

- OnlineBookStore/Order/kubernetes/deployment.yml 파일 
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order
  namespace: onlinebookstore
  labels:
    app: order
spec:
  replicas: 1
  selector:
    matchLabels:
      app: order
  template:
    metadata:
      labels:
        app: order
    spec:
      containers:
        - name: order
          image: skccteam2acr.azurecr.io/order:latest
          ports:
            - containerPort: 8080
          readinessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 10
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 10
          livenessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 120
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 5
          env:
            - name: configmap
              valueFrom:
                configMapKeyRef:
                  name: resturl
                  key: url
          resources:
            requests:
              cpu: 300m
              # memory: 256Mi
            limits:
              cpu: 500m
              # memory: 256Mi
```	  

- deploy 완료

![image](https://user-images.githubusercontent.com/20077391/121022073-fc9fdd80-c7dc-11eb-9f50-962556056728.png)


## ConfigMap 
- 시스템별로 변경 가능성이 있는 설정들을 ConfigMap을 사용하여 관리
- OnlineBookStore에서는 주문에서 책 재고 서비스 호출 시 "호출 주소"를 ConfigMap 처리하기로 결정

- Java 소스에 "호출 주소"를 변수(api.url.book) 처리(/Order/src/main/java/onlinebookstore/external/BookService.java) 


![image](https://user-images.githubusercontent.com/20077391/120964977-24705080-c79f-11eb-8e5b-be9f8e6d2128.png)


- application.yml 파일에서 api.url.book을 ConfigMap과 연결


![image](https://user-images.githubusercontent.com/20077391/120963090-f0dff700-c79b-11eb-88b4-247efe73a301.png)


- ConfigMap 생성

```
kubectl create configmap resturl --from-literal=url=http://Book:8080
```

- Deployment.yml 에 ConfigMap 적용

![image](https://user-images.githubusercontent.com/20077391/120965103-58e40c80-c79f-11eb-8abd-d3a98048166e.png)


## Secret 
- DBMS 연결에 필요한 username 및 password는 민감한 정보이므로 Secret 처리하였다.

![image](https://user-images.githubusercontent.com/20077391/121105591-59cc7b00-c83f-11eb-96b7-e9649498fdf2.png)

- deployment.yml에서 env로 설정하였다.

![image](https://user-images.githubusercontent.com/20077391/121105685-841e3880-c83f-11eb-9c3e-645f4a21cb8a.png)

- 쿠버네티스에서는 다음과 같이 Secret object를 생성하였다.

![image](https://user-images.githubusercontent.com/20077391/121105756-a9ab4200-c83f-11eb-902a-bc276651bf7b.png)


## Circuit Breaker와 Fallback 처리

* Spring FeignClient + Hystrix를 사용하여 구현함

시나리오는 주문(Order)-->재고(Book) 확인 시 주문 요청에 대한 재고확인이 3초를 넘어설 경우 Circuit Breaker 를 통하여 장애격리.

- Hystrix 를 설정:  FeignClient 요청처리에서 처리시간이 3초가 넘어서면 CB가 동작하도록 (요청을 빠르게 실패처리, 차단) 설정
                    추가로, 테스트를 위해 1번만 timeout이 발생해도 CB가 발생하도록 설정
```
# application.yml
```
![image](https://user-images.githubusercontent.com/20077391/120970089-ed516d80-c7a5-11eb-8abb-d57cdbf77065.png)


- 호출 서비스(주문)에서는 재고API 호출에서 문제 발생 시 주문건을 OutOfStock 처리하도록 FallBack 구현
```
# (Order) BookService.java 
```
![image](https://user-images.githubusercontent.com/20077391/121100878-b034bc00-c835-11eb-97de-2bec90b7f3b0.png)


- 피호출 서비스(책재고:Book)에서 테스트를 위해 bookId가 2인 주문건에 대해 sleep 처리
```
# (Book) BookController.java 
```
![image](https://user-images.githubusercontent.com/20077391/120971537-b54b2a00-c7a7-11eb-9595-8fa8cb444be5.png)



* 서킷 브레이커 동작 확인:

bookId가 1번 인 경우 정상적으로 주문 처리 완료
```
# http POST http://52.141.32.129:8080/orders bookId=1 customerId=4 qty=1
```
![image](https://user-images.githubusercontent.com/20077391/120970620-a152f880-c7a6-11eb-843a-855d85678638.png)

bookId가 2번 인 경우 CB에 의한 timeout 발생 확인 (Order건은 OutOfStocked 처리됨)
```
# http POST http://52.141.32.129:8080/orders bookId=2 customerId=4 qty=1
```
![image](https://user-images.githubusercontent.com/20077391/120970699-bcbe0380-c7a6-11eb-8c71-ad71101ca1dc.png)

time 아웃이 연달아 2번 발생한 경우 CB가 OPEN되어 Book 호출이 아예 차단된 것을 확인 (테스트를 위해 circuitBreaker.requestVolumeThreshold=1 로 설정)

![image](https://user-images.githubusercontent.com/20077391/120970889-fabb2780-c7a6-11eb-9ab9-e44700c270a7.png)


일정시간 뒤에는 다시 주문이 정상적으로 수행되는 것을 알 수 있다.

![image](https://user-images.githubusercontent.com/20077391/120973450-ea587c00-c7a9-11eb-863b-f15dda3bdaa9.png)


- 운영시스템은 죽지 않고 지속적으로 CB 에 의하여 적절히 회로가 열림과 닫힘이 벌어지면서 Thread 자원 등을 보호하고 있음을 보여줌.



### 오토스케일 아웃
주문 서비스가 몰릴 경우를 대비하여 자동화된 확장 기능을 적용하였다.

- 주문서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 테스트를 위해 CPU 사용량이 50프로를 넘어서면 replica 를 3개까지 늘려준다:
```
hpa.yml
```
![image](https://user-images.githubusercontent.com/20077391/120973949-8aaea080-c7aa-11eb-80ce-eccb3c8cbc0d.png)

- deployment.yml에 resource 관련 설정을 추가해 준다.
```
deployment.yml
```
![image](https://user-images.githubusercontent.com/20077391/121101100-25a08c80-c836-11eb-81f1-a7df0f0dcaeb.png)


- 100명이 60초 동안 주문을 넣어준다.
```
siege -c100 -t60S -r10 --content-type "application/json" 'http://52.141.32.129:8080/orders POST {"bookId":"1","customerId":"1","qty":"1"}
```

- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다:
```
kubectl get deploy -l app=order -w
```

- 어느정도 시간이 흐른 후 스케일 아웃이 벌어지는 것을 확인할 수 있다.

![image](https://user-images.githubusercontent.com/20077391/120974885-9babe180-c7ab-11eb-9a84-07bfb408ed34.png)

- siege 의 로그를 보면 오토스케일 확장이 일어나며 주문을 100% 처리완료한 것을 알 수 있었다.
```
** SIEGE 4.0.4
** Preparing 100 concurrent users for battle.
The server is now under siege...
Lifting the server siege...
Transactions:                   2904 hits
Availability:                 100.00 %        
Elapsed time:                  59.64 secs     
Data transferred:               0.90 MB       
Response time:                  2.02 secs     
Transaction rate:              48.69 trans/sec
Throughput:                     0.02 MB/sec   
Concurrency:                   98.52
Successful transactions:        2904
Failed transactions:               0
Longest transaction:           13.62
Shortest transaction:           0.11
```



## Zero-downtime deploy (Readiness Probe) 무정지 재배포

* Zero-downtime deploy를 위해 readiness Probe를 설정함
![image](https://user-images.githubusercontent.com/20077391/121024696-6e792680-c7df-11eb-8cc3-ad8e1cbda949.png)


* Zero-downtime deploy 확인을 위해 seige 로 1명이 지속적인 고객등록 작업을 수행함
```
siege -c1 -t180S -r100 --content-type "application/json" 'http://localhost:8080/customers POST {"name": "CUSTOMER99","email":"CUSTOMER99@onlinebookstore.com"}'
```

먼저 customer 이미지가 v1.0 임을 확인
![image](https://user-images.githubusercontent.com/20077391/120979102-31e20680-c7b0-11eb-8bb6-53481781e62c.png)

새 버전으로 배포(이미지를 v2.0으로 변경)
```
kubectl set image deployment customer customer=skccteam2acr.azurecr.io/customer:v2.0
```

customer 이미지가 변경되는 과정 (POD 상태변화)
![image](https://user-images.githubusercontent.com/20077391/120978979-0bbc6680-c7b0-11eb-91e9-7317f2b15ee8.png)


customer 이미지가 v2.0으로 변경되었임을 확인
![image](https://user-images.githubusercontent.com/20077391/120979060-27c00800-c7b0-11eb-8915-93197a3174b5.png)

- seige 의 화면으로 넘어가서 Availability가 100% 인지 확인 (무정지 배포 성공)
```
** SIEGE 4.0.4
** Preparing 1 concurrent users for battle.
The server is now under siege...
Lifting the server siege...
Transactions:                  15793 hits
Availability:                 100.00 %
Elapsed time:                 179.41 secs
Data transferred:               3.31 MB
Response time:                  0.01 secs
Transaction rate:              88.03 trans/sec
Throughput:                     0.02 MB/sec
Concurrency:                    0.99
Successful transactions:           0
Failed transactions:               0
Longest transaction:            0.29
Shortest transaction:           0.00
```


# Self-healing (Liveness Probe)

- Self-healing 확인을 위한 Liveness Probe 옵션 변경 (Port 변경)

onlinebookstore/delivery/kubernetes/deployment.yml

![image](https://user-images.githubusercontent.com/20077391/120980312-7621d680-c7b1-11eb-885f-cd9bc9a9011f.png)


- Delivery pod에 Liveness Probe 옵션 적용 확인

![image](https://user-images.githubusercontent.com/20077391/120981097-458e6c80-c7b2-11eb-9a3c-d17396a59048.png)


- Liveness 확인 실패에 따른 retry발생 확인

![image](https://user-images.githubusercontent.com/20077391/120981283-7e2e4600-c7b2-11eb-92ef-2d5e4f2837eb.png)



이상으로 12가지 체크포인트가 구현 및 검증 완료되었음 확인하였다.

# 끗~
