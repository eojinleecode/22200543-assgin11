# 22200543 이어진 웹서비스 개발 01분반 11주차 과제 

## 1. Project Infomation



## 2. Installation :

## 3. Soruce Code Exploration 

### Project Structure & Spring MVC
Q1. PostController, PostService, PostRepository는 각각 어떤 역할을 하며, 서로 어떻게 연결되는가?

PostController는 http 요청 수신하고 Service 메서드를 호출하는 역할을 한다.
PostService는 게시글 작성,조회,수정,삭제(Crud) 등 비지니스로직을 처리한다.
PostRepository는 JPA를 통해 DB table에 있는 데이터에 접근하고 다루는 통신적 역할을 하고 있다.
연결관계는 PostController -> PostService -> PostRepository 이다.
즉 요청을 받고 메서드 호출에 의해 적절한 기능을 수행하고 그 데이터를 DB에 넣는다.

Q2. 게시글 작성 시 HTML Form의 입력값과 첨부파일은 Controller에 어떻게 전달되는가?

html form을 제출하게 되면 http요청이 발생되며 그것이 위에 1번 문제의 경로를 통해 결국 서버DB로 전달된다.
enctype 속성의 mutipart/form-data을 설정하여 텍스트 입력값, 파일 등 하나의 http요청으로 바인딩하여 서버에 전송한다.
이때 Contorller에서 @ModelAttribute를 사용하여 일반 form data를 DTO 쪽에 바인딩하고 @Requestparam을 이용하여 여러개의 첨부파일을 수신할 수 있다.

### JPA & Entity Relationship

Q3. Post와 Attachment Entity는 어떤 관계이며, @ManyToOne과 @OneToMany는 각각 어떤 역할을 하는가?

두 Entity는 일 대 다수 관계를 가집니다. 즉 Post(게시글)하나에 여러개의 첨부파일을 참조할수 있다는 의미를 지니고 있다.
@ManyToOne는 여러 첨부파일이 하나의 게시글에 속한다는 것을 명시하는 어노테이션 역할이다.
@OneToMany는 하나의 게시글이 여러 첨부파일을 가진다는 것을 명시하는 어노테이션 역할이다.

Q4. cascade = CascadeType.ALL과 orphanRemoval = true는 어떤 역할을 하며, 실제 디스크에 저장된 파일에도 영향을 주는가?

cascade = CascadeType.ALL 은 부모 Entity(Post)의 정보 변화를 자식 Entity(Attachment)에도 전파하는 역할을한다.
orphanRemoval = true 은 부모 Entity의 컬렉션에서 자식 Entity 관계가 끊어지면 자식 Entity를 DB에서 자동으로 삭제하는 역할이다.
두 설정 모두, 서버 디스크에 파일에 영향을 주지 않는다. 두 설정은 그저 Entity 관리에만 영향을 끼친다.

### File Upload

Q5. MultipartFile은 무엇이며, 업로드된 파일은 어떤 과정을 거쳐 서버에 저장되는가?

MultipartFile은 업로드 된 파일을 처리하기 위한 인터페이스이다. 실제 저장된 파일이 아닌 http 요청으로 전달 받은 업로드 파일 자체를 표현한다.
저장 과정 : 사용자가 어느 한 파일을 제출하면 , Controller가 MultipartFile로 요청을 수신한 후 , Service layer에서 확장자,크기 등을 체킹합니다.
그리고 저장 파일명을 정하고, 지정된 서버 폴더에 실제 파일을 저장한다. 마지막엔 DB에 원본 파일명, 저장 경로,메타데이터를 저장한다.

Q6. 파일을 원본 이름 대신 UUID 기반 파일명으로 저장하는 이유는 무엇인가?

웹에서는 실제 파일, 파일에 관한 DB 정보를 구분하고 원본명과 저장명을 분리해서 관리한다.
식별자 즉, 파일명 중복 및 덮어쓰기 문제를 방지하기 위해서이다.

### File Download & Security

Q7. 파일 다운로드 시 Resource, ResponseEntity, Content-Disposition은 각각 어떤 역할을 하는가?

Resource는 Controller에서 파일 데이터를 반환할 때 사용되는 인터페이스 역할을 한다.
ResponseEntity는 http 응답의 상태코드,header,body 를 구성하여 파일 데이타를 전달하는 객체 역할을 한다.
Content-Disposition는 브라우저가 파일을 다운로드 하도록 지정한다, 이 때 원본 파일명과 다운로드 동작을 설정하는데 하는 역할을 한다.

Q8. 파일 저장 과정에서 normalize()와 startsWith()를 사용하는 이유는 무엇인가?

normalize()는 경로를 정리하는 역할이고 startsWith()은 타겟경로가 지정된 업로드 디렉토리에 있는지 확인한다.
이를 사용하는 이유는 악의적인 경로 접근, 즉 Path Traversal을 예방하기 위함이다.

## 4. Customizing


## 5. New Feature


## 6. Testing & Deployment


## 7. Weekly Review