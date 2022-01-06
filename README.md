"# 1588-pizza" 

# PIZZA 통합주문콜센터x

- 체크포인트 : https://workflowy.com/s/assessment-check-po/T5YrzcMewfo4J6LW


# Table of contents

- [PIZZA 통합주문콜센터](#---)
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


## 헥사고날 아키텍처 다이어그램 도출 

![image](https://user-images.githubusercontent.com/20077391/121859335-88ac8a80-cd32-11eb-9159-9599abcf67cf.png)

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
분석/설계 및 구현을 통해 이벤트를 Publish/Subscribe 하도록 구현하므로써, 다음 서비스가 트리거될 수 있도록 하였다.

![image](https://user-images.githubusercontent.com/20077391/123185556-2b9e9a80-d4d1-11eb-9af3-54493a7f307a.png)


[Publish]

![image](https://user-images.githubusercontent.com/20077391/121466412-5decca00-c9f2-11eb-8e95-b783c193db96.png)

[Subscribe]

![image](https://user-images.githubusercontent.com/20077391/121466695-ce93e680-c9f2-11eb-938e-03ce98a64282.png)


또한, 아래와 같이 보상 이벤트를 준비하여 Rollback이 가능하도록 구현되었다.

![image](https://user-images.githubusercontent.com/20077391/123185781-a667b580-d4d1-11eb-937e-11aabf1fc3e0.png)


SAGA 및 ROLLBACK의 동작은 앞 서 기능/비기능 검증부분에서 이미 검증완료하였다.


## CQRS
Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능하게 구현해 두었다.

본 프로젝트에서 View 역할은 CustomerCenter 서비스의 마이페이지가 수행한다.

CQRS를 구현하여 주문건에 대한 상태는 Order 마이크로서비스의 접근없이 CustomerCenter의 마이페이지를 통해 조회할 수 있도록 구현하였다.

- 주문(ordered) 실행 후 myPage 화면

![image](https://user-images.githubusercontent.com/20077391/121466962-39452200-c9f3-11eb-83e9-fc75710a4cea.png)


- 주문취소(OrderCancelled) 후 myPage 화면

![image](https://user-images.githubusercontent.com/20077391/121467122-83c69e80-c9f3-11eb-8777-cb2923d25412.png)


## Correlation 
각 이벤트 건(메시지)이 어떤 Policy를 처리할 때 어떤건에 연결된 처리건인지를 구별하기 위한 Correlation-key를 제대로 연결하였는지를 검증하였다.
![image](https://user-images.githubusercontent.com/20077391/121467407-f59ee800-c9f3-11eb-8a15-1eaa2763abbb.png)


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
        - id: order
          uri: http://localhost:8081
          predicates:
            - Path=/orders/** 
        - id: store
          uri: http://localhost:8082
          predicates:
            - Path=/stores/**,/storeOrders/** 
        - id: delivery
          uri: http://localhost:8083
          predicates:
            - Path=/deliveries/** 
        - id: customercenter
          uri: http://localhost:8084
          predicates:
            - Path= /myPages/**
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
        - id: order
          uri: http://order:8080
          predicates:
            - Path=/orders/** 
        - id: store
          uri: http://store:8080
          predicates:
            - Path=/stores/**/storeOrders/** 
        - id: delivery
          uri: http://delivery:8080
          predicates:
            - Path=/deliveries/** 
        - id: customercenter
          uri: http://customercenter:8080
          predicates:
            - Path= /myPages/**
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
1588-pizza에서는 다음과 같이 2가지 DBMS를 적용하였다.
- MySQL(쿠버네티스에서는 SQLServer) : Order, Store, Delivery
- H2    : CustomerCenter

```
# (Order, Store, Delivery) application.yml

spring:
  profiles: default
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/1588-pizza?useSSL=false&characterEncoding=UTF-8&serverTimezone=UTC
    username: *****
    password: *****

spring:
  profiles: docker
  datasource:
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
    url: jdbc:sqlserver://aramidhwan.database.windows.net:1433;database=1588-pizza;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;
    username: ${SQLSERVER_USERNAME}
    password: ${SQLSERVER_PASSWORD}
...

# (CustomerCenter) application.yml

spring:
  profiles: default
  h2:
    console:
      enabled: true
      path: /h2-console
```


## 동기식 호출(Req/Resp) 패턴

분석단계에서의 조건 중 하나로 주문(Order)->체인점(Store) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 
호출 프로토콜은 RestController를 FeignClient 를 이용하여 호출하도록 한다. 

- 체인점 "영업중" 상태 확인 서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

```
# (Order) StoreService.java

package pizza.external;

@FeignClient(name="store", url="${api.url.book}")
public interface StoreService {

    @RequestMapping(method= RequestMethod.GET, path="/stores/chkOpenYN")
    public boolean chkOpenYN(@RequestParam("regionNm") String regionNm);
}
```

- 주문을 받은 직후 해당 지역의 체인점 "영업중" 확인을 요청하도록 처리
```
# StoreController.java

package pizza;

 @RestController
 public class StoreController {
    @Autowired
    StoreRepository storeRepository;

    @RequestMapping(value = "/stores/chkOpenYN", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public boolean chkOpenYN(@RequestParam("regionNm") String regionNm) throws Exception {

        System.out.println("##### /store/chkOpenYn  called #####");
        boolean status = false;

        List<Store> storeList = storeRepository.findByRegionNmAndOpenYN(regionNm, Boolean.valueOf(true));
        // 주문이 들어온 regionNm에 Open된 Sotre가 한군데라도 있으면 true를 리턴
        if (storeList.size() > 0) {
                status = true ;
        }

        return status;
    }
 }
```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 체인점 관리 시스템이 장애가 나면 주문도 못받는다는 것을 확인:


```
# 체인점 관리 (Store)) 서비스를 잠시 내려놓음 (ctrl+c)

#주문처리
http POST http://localhost:8088/orders customerId=1 pizzaNm="페퍼로니피자" qty=1 regionNm="강남구"   #Fail

#체인점 관리 서비스 재기동
cd Store
mvn spring-boot:run

#주문처리
http POST http://localhost:8088/orders customerId=1 pizzaNm="페퍼로니피자" qty=1 regionNm="강남구"   #Success
```
추후 운영단계에서는 Circuit Breaker를 이용하여 재고 관리 시스템에 장애가 발생하여도 주문 접수는 가능하도록 개선할 예정이다.


## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트

주문이 이루어진 후에 체인점/배달 시스템으로 이를 알려주는 행위는 동기식이 아니라 비 동기식으로 처리하여 체인점/배송 시스템의 처리를 위하여 주문이 블로킹 되지 않도록 처리한다.
 
- 이를 위하여 주문이력에 기록을 남긴 후에 곧바로 주문이 완료되었다는 도메인 이벤트를 카프카로 송출한다(Publish)
 
```
package pizza;

@Entity
@Table(name="Order_table")
public class Order {
 ...
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
}
```
- 배달 서비스에서는 주문 및 조리완료 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다:
```
package pizza;

...

@Service
public class PolicyHandler{
    @Autowired
    DeliveryRepository deliveryRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCooked_DeliveryAccept(@Payload Cooked cooked){

        if(!cooked.validate()) return;

        System.out.println("\n\n##### listener DeliveryAccept : " + cooked.getOrderId());

        // 배달접수
        Delivery delivery = new Delivery();
        BeanUtils.copyProperties(cooked, delivery);
        delivery.setStatus("DeliveryStart");
        deliveryRepository.save(delivery);
    }
}

```

배달 시스템은 주문과 완전히 분리되어있으며, 이벤트 수신에 따라 처리되기 때문에, 배달 시스템이 유지보수로 인해 잠시 내려간 상태라도 주문을 받는데 문제가 없다:
```
# 배달관리 서비스 (Delivery) 를 잠시 내려놓음 (ctrl+c)

#주문처리
http POST http://localhost:8088/orders customerId=1 pizzaNm="페퍼로니피자" qty=1 regionNm="강남구"   #Success

#주문상태 확인
http GET http://localhost:8088/orders/1     # 정상적으로 주문됨을 확인

#체인점에서 "조리완료" 처리
http PATCH http://localhost:8088/storeOrders/1 status=Cooked

#배송 서비스 기동
cd Delivery
mvn spring-boot:run

#주문상태 확인
http localhost:8088/orders/1     # 주문 상태가 "DeliveryStart"로 확인
```


# 운영

## Deploy / Pipeline

- git에서 소스 가져오기
```
git clone https://github.com/aramidhwan/1588-pizza.git
```
- Build 하기
```
cd order
mvn package

cd ../store
mvn package

...이하 생략...
```

- Docker Image build/Push/
```

cd order
docker build -t myacr00.azurecr.io/order:latest .
docker push myacr00.azurecr.io/order:latest

cd ../store
docker build -t myacr00.azurecr.io/store:latest .
docker push myacr00.azurecr.io/store:latest

...이하 생략...
```

- yml파일 이용한 deploy (예시: 1588-pizza/order/kubernetes/deployment.yml 파일)
```
kubectl apply -f deployment.yml

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order
  namespace: pizza
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
          image: myacr00.azurecr.io/order:latest
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
```	  

- deploy 완료

![image](https://user-images.githubusercontent.com/20077391/121856003-f656b780-cd2e-11eb-8fbc-c5b2c061f3e1.png)


## ConfigMap 
- 시스템별로 변경 가능성이 있는 설정들을 ConfigMap을 사용하여 관리
- 1588-pizza에서는 주문에서 체인점 영업상태 체크 호출 시 "호출 주소"를 ConfigMap 처리하기로 결정

- Java 소스에 "호출 주소"를 변수(api.url.book) 처리 (/order/src/main/java/pizza/external/StoreService.java) 

![image](https://user-images.githubusercontent.com/20077391/121827797-6c8bf780-ccf8-11eb-90ad-1ae553bf9ced.png)



- application.yml 파일에서 api.url.book을 ConfigMap과 연결

![image](https://user-images.githubusercontent.com/20077391/121827883-b379ed00-ccf8-11eb-9bca-554dec2a142e.png)


- 클러스터에 ConfigMap 생성

```
kubectl create configmap resturl --from-literal=sotreUrl=http://Store:8080
```

- Deployment.yml 에 ConfigMap 적용

![image](https://user-images.githubusercontent.com/20077391/121828019-3602ac80-ccf9-11eb-90f4-4a1943f372b2.png)


## Secret 
- DBMS 연결에 필요한 username 및 password는 민감한 정보이므로 Secret 처리하였다.

![image](https://user-images.githubusercontent.com/20077391/121828116-942f8f80-ccf9-11eb-913e-b52eba263e84.png)


- deployment.yml에서 env로 설정하였다.

![image](https://user-images.githubusercontent.com/20077391/121828252-fe483480-ccf9-11eb-8e91-7438f8f5cb3c.png)


- 쿠버네티스에서는 base64 처리하여 다음과 같이 Secret object를 생성하였다.

![image](https://user-images.githubusercontent.com/20077391/121828737-6f3c1c00-ccfb-11eb-8691-2f88a6be4c4a.png)


## Circuit Breaker와 Fallback 처리

* Spring FeignClient + Hystrix를 사용하여 구현함

시나리오는 주문(order)-->체인점(Store) 영업여부 체크 확인 시 1초를 넘어설 경우 Circuit Breaker 를 통하여 장애격리.

- Hystrix 를 설정:  FeignClient 요청처리에서 처리시간이 1초가 넘어서면 CB가 동작하도록 (요청을 빠르게 실패처리, 차단) 설정
                    추가로, 테스트를 위해 1번만 timeout이 발생해도 CB가 발생하도록 설정
```
# application.yml
```
![image](https://user-images.githubusercontent.com/20077391/121836584-b54eab00-cd0e-11eb-95da-8affd593f85c.png)


- 호출 서비스(주문)에서는 체인점(Store) API 호출에서 문제 발생 시 FallBack 구현
```
# (Order) StoreService.java 
```
![image](https://user-images.githubusercontent.com/20077391/121837037-a1577900-cd0f-11eb-8452-e5552a445f44.png)

```
# (Order) StoreServiceFallbackFactory.java 
```
![image](https://user-images.githubusercontent.com/20077391/123183614-d6f92080-d4cc-11eb-9301-3ca4b24d5d6e.png)



- 피호출 서비스(체인점:Store)에서 테스트를 위해 주문지역이 "종로구"인 주문건에 대해 sleep 처리
```
# (Store) StoreController.java 
```
![image](https://user-images.githubusercontent.com/20077391/121837234-0c08b480-cd10-11eb-8e8d-496bfd8c851c.png)



* 서킷 브레이커 동작 확인:

주문지역이 "강남구" 인 경우 정상적으로 주문 처리 완료
```
# http POST http://104.42.177.6:8080/orders customerId=1 pizzaNm="하와이안피자" qty=1 regionNm="강남구"
```
![image](https://user-images.githubusercontent.com/20077391/121841496-59d5ea80-cd19-11eb-949f-67c53bc68bc7.png)


주문지역이 "종로구" 인 경우 CB에 의한 timeout 발생 확인 (Order건은 NoStoreOpened 처리됨)
```
# http POST http://104.42.177.6:8080/orders customerId=1 pizzaNm="페퍼로니피자" qty=1 regionNm="종로구"
```
![image](https://user-images.githubusercontent.com/20077391/121842758-ec778900-cd1b-11eb-9698-5a4ac08c39e8.png)
![image](https://user-images.githubusercontent.com/20077391/121841895-411a0480-cd1a-11eb-8d39-247abb36ecc4.png)


time 아웃이 연달아 2번 발생한 경우 CB가 OPEN되어 체인점(Store) 호출이 아예 차단된 것을 확인 (테스트를 위해 circuitBreaker.requestVolumeThreshold=1 로 설정)

![image](https://user-images.githubusercontent.com/20077391/121842905-395b5f80-cd1c-11eb-88c3-9ae9b08e4ce1.png)


일정시간 뒤에는 다시 주문이 정상적으로 수행되는 것을 알 수 있다.

![image](https://user-images.githubusercontent.com/20077391/121843051-80e1eb80-cd1c-11eb-9f78-dc1960e2000a.png)


- 시스템이 죽지 않고 지속적으로 CB 에 의하여 적절히 회로가 열림과 닫힘이 벌어지면서 Thread 자원 등을 보호하고 있음을 보여줌.



### 오토스케일 아웃
주문 서비스가 몰릴 경우를 대비하여 자동화된 확장 기능을 적용하였다.

- 주문서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 테스트를 위해 CPU 사용량이 50프로를 넘어서면 replica 를 3개까지 늘려준다:
```
hpa.yml
```
![image](https://user-images.githubusercontent.com/20077391/121843544-6e1be680-cd1d-11eb-9c2c-da27c0842e89.png)

- deployment.yml에 resource 관련 설정을 추가해 준다.

![image](https://user-images.githubusercontent.com/20077391/121843585-7ffd8980-cd1d-11eb-876d-2a5c516a9101.png)


- 100명이 60초 동안 주문을 넣어준다.
```
siege -c100 -t60S --content-type "application/json" 'http://10.0.223.154:8080/orders POST {"customerId":"1","pizzaNm":"페퍼로니피자","qty":"1","regionNm":"강남구"}'
```

- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다:
```
kubectl get deploy -l app=order -w
```

- 어느정도 시간이 흐른 후 스케일 아웃이 벌어지는 것을 확인할 수 있다.

![image](https://user-images.githubusercontent.com/20077391/121853157-8430a380-cd2b-11eb-9df2-dec3a01a7cb3.png)


- siege 의 로그를 보면 오토스케일 확장이 일어나며 주문을 100% 처리완료한 것을 알 수 있었다.
```
** SIEGE 4.0.4
** Preparing 100 concurrent users for battle.
The server is now under siege...
Lifting the server siege...
Transactions:                   5077 hits
Availability:                 100.00 %
Elapsed time:                  59.58 secs
Data transferred:               1.58 MB
Response time:                  1.16 secs
Transaction rate:              85.21 trans/sec
Throughput:                     0.03 MB/sec
Concurrency:                   98.86
Successful transactions:        5077
Failed transactions:               0
Longest transaction:            5.64
Shortest transaction:           0.00
```



## Zero-downtime deploy (Readiness Probe) 무정지 재배포

* Zero-downtime deploy를 위해 deployment.yml에 readiness Probe를 설정함

![image](https://user-images.githubusercontent.com/20077391/121853740-42542d00-cd2c-11eb-9455-08562342a5ab.png)


* 먼저 store 이미지가 v1.0 임을 확인

![image](https://user-images.githubusercontent.com/20077391/121855205-0a4de980-cd2e-11eb-9242-062fc4bcbd00.png)


* Zero-downtime deploy 확인을 위해 seige 로 1명이 지속적인 체인점 등록 작업을 수행함
```
siege -c1 -t180S --content-type "application/json" 'http://10.0.223.154:8080/stores POST {"regionNm": "강남구","openYN":"true"}'
```

새 버전으로 배포(이미지를 v2.0으로 변경)
```
kubectl set image deployment store store=myacr00.azurecr.io/store:v2.0
```

store 이미지가 변경되는 과정 (POD 상태변화)
```
kubectl get pod -l app=store -w
```
![image](https://user-images.githubusercontent.com/20077391/121856415-6e24e200-cd2f-11eb-88d6-c59372893827.png)


store 이미지가 v2.0으로 변경되었임을 확인

![image](https://user-images.githubusercontent.com/20077391/121856540-8eed3780-cd2f-11eb-90b6-124774cd6c08.png)


- seige 의 화면으로 넘어가서 Availability가 100% 인지 확인 (무정지 배포 성공)
```
** SIEGE 4.0.4
** Preparing 1 concurrent users for battle.
The server is now under siege...
Lifting the server siege...
Transactions:                  51297 hits
Availability:                 100.00 %
Elapsed time:                 179.17 secs
Data transferred:              10.18 MB
Response time:                  0.00 secs
Transaction rate:             286.30 trans/sec
Throughput:                     0.06 MB/sec
Concurrency:                    0.99
Successful transactions:       51297
Failed transactions:               1
Longest transaction:            0.42
Shortest transaction:           0.00
```


# Self-healing (Liveness Probe)

- Self-healing 확인을 위한 Liveness Probe 옵션 변경 (Port 변경)

1588-pizza/delivery/kubernetes/deployment.yml

![image](https://user-images.githubusercontent.com/20077391/121857004-1aff5f00-cd30-11eb-9d0b-4a3f9cfbc565.png)


- Delivery pod에 Liveness Probe 옵션 적용 확인

![image](https://user-images.githubusercontent.com/20077391/121857693-d9bb7f00-cd30-11eb-9f2d-2078a137e964.png)


- Liveness 확인 실패에 따른 retry발생 확인

![image](https://user-images.githubusercontent.com/20077391/121858044-2c953680-cd31-11eb-99d6-14127178e16d.png)



이상으로 12가지 체크포인트가 구현 및 검증 완료되었음 확인하였다.

# 끗~
