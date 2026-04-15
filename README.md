# MybookApp — 도서 검색·리뷰 Android 앱

<p align="left">
  <img src="https://img.shields.io/badge/Android-Java-3DDC84?logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Firebase-Firestore-FFCA28?logo=firebase&logoColor=black" />
  <img src="https://img.shields.io/badge/Retrofit2-REST_API-48B983" />
</p>

---

## 📌 프로젝트 개요

**MybookApp**은 도서 검색, 별점 평가, 리뷰 작성 기능을 제공하는 Android 앱입니다.  
외부 도서관 API를 통해 도서 정보를 조회하고, Firebase 기반 사용자 인증과 리뷰 저장 기능을 제공합니다.

---

## 🎯 주요 기능

| 기능 | 설명 |
|------|------|
| 도서 검색 | 키워드로 도서 목록 조회 (도서관 API 연동) |
| 별점 평가 | 도서에 1~5점 별점 부여 |
| 리뷰 작성 | 도서별 코멘트 작성 및 조회 |
| 북마크 | 관심 도서 즐겨찾기 저장 |
| 로그인 | Firebase 기반 사용자 인증 |
| 마이페이지 | 내가 평가한 도서·리뷰 목록 관리 |

---

## 📁 파일 구조

```
MybookApp/
└── app/src/main/java/com/example/myapplication/
    ├── MainActivity.java           # 메인 화면 (도서 목록)
    ├── LoginActivity.java          # 로그인 화면
    ├── MyPageActivity.java         # 마이페이지
    ├── BookRatingAggregate.java    # 별점 집계
    ├── UserRatingComment.java      # 사용자 평가 데이터
    ├── adapter/
    │   ├── BookAdapter.java        # 도서 목록 RecyclerView 어댑터
    │   └── CommentsAdapter.java    # 리뷰 목록 어댑터
    ├── model/
    │   ├── Book.java               # 도서 데이터 모델
    │   ├── LoginRequest/Response   # 인증 모델
    │   └── RatingComment.java      # 리뷰 모델
    └── network/
        ├── LibraryApiService.java  # Retrofit API 인터페이스
        └── RetrofitClient.java     # Retrofit 클라이언트 설정
```

---

## 🚀 실행 방법

1. Android Studio에서 프로젝트 열기
2. `app/google-services.json`에 Firebase 설정 확인
3. Android 기기 또는 에뮬레이터 연결 후 **Run** 실행

---

## 🛠 기술 스택

- **언어**: Java
- **플랫폼**: Android (minSdk 24+)
- **네트워크**: Retrofit2, OkHttp
- **인증/DB**: Firebase Authentication, Firestore
- **UI**: RecyclerView, Material Design, Navigation Drawer

---

## 👤 개발자

**westjun1021** — Android Developer  
Email: tjwns4603@gmail.com
