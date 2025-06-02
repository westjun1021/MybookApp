package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.BookAdapter;
import com.example.myapplication.adapter.CommentsAdapter;
import com.example.myapplication.model.Book;
import com.example.myapplication.model.BookRatingAggregate;
import com.example.myapplication.model.RatingComment;
import com.example.myapplication.model.RatingCommentStorage;
import com.example.myapplication.model.UserRatingComment;
import com.example.myapplication.network.LibraryApiService;
import com.example.myapplication.network.RetrofitClient;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Transaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements BookAdapter.OnBookmarkClickListener,
        BookAdapter.OnRateClickListener,
        BookAdapter.OnViewCommentsClickListener {

    private static final String API_KEY = "22c226e7b1bdbaf8c4e32631e83c85c542df343381694a2d2d677aafce9e13c4";

    // SharedPreferences 키
    private static final String PREFS_NAME       = "prefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_BOOKMARKS    = "bookmarks_json";

    private FirebaseAuth     mAuth;
    private FirebaseUser     currentUser;
    private SharedPreferences prefs;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // 네비게이션 드로어 관련
    private DrawerLayout      drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView    navigationView;

    // 메인 화면 뷰
    private Toolbar     toolbar;
    private EditText    etKeyword;
    private Button      btnSearch, btnHome;
    private TextView    tvHeader;
    private RecyclerView rvContent;

    // 어댑터 및 데이터
    private BookAdapter searchAdapter;
    private BookAdapter bookmarkAdapter;
    private final List<Book> bookmarkList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 로그인 상태 체크
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        if (!isLoggedIn) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // 레이아웃 설정
        setContentView(R.layout.activity_main);

        // FirebaseAuth, 현재 사용자
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        Log.d("RatingTest", "onCreate: currentUser = " + currentUser);

        // 툴바 & 드로어 초기화
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout   = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // 네비게이션 헤더에 닉네임/이메일 표시
        if (currentUser != null) {
            View headerView = navigationView.getHeaderView(0);
            TextView tvNavNickname = headerView.findViewById(R.id.tvNickname);

            String displayName = currentUser.getDisplayName();
            if (!TextUtils.isEmpty(displayName)) {
                tvNavNickname.setText(displayName);
            } else {
                tvNavNickname.setText(currentUser.getEmail());
            }
        }

        // 네비게이션 메뉴 클릭 리스너
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.nav_my_page) {
                startActivity(new Intent(MainActivity.this, MyPageActivity.class));
            } else if (id == R.id.nav_settings) {
                // 기존 Toast → SettingsActivity 호출로 변경
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            } else if (id == R.id.nav_logout) {
                mAuth.signOut();
                prefs.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply();
                Toast.makeText(MainActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
            drawerLayout.closeDrawers();
            return true;
        });

        // 뷰 바인딩
        etKeyword = findViewById(R.id.etKeyword);
        btnSearch = findViewById(R.id.btnSearch);
        btnHome   = findViewById(R.id.btnHome);
        tvHeader  = findViewById(R.id.tvHeader);
        rvContent = findViewById(R.id.rvContent);

        // 저장된 북마크 불러오기
        loadBookmarksFromPrefs();

        // 어댑터 초기화 (세 리스너와 bookmarkList 전달)
        bookmarkAdapter = new BookAdapter(
                this,   // OnBookmarkClickListener
                this,   // OnRateClickListener
                this,   // OnViewCommentsClickListener
                bookmarkList
        );
        searchAdapter = new BookAdapter(
                this,
                this,
                this,
                bookmarkList
        );

        // RecyclerView 세팅
        rvContent.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        // 홈 화면(북마크) 표시
        showBookmarks();

        // 검색 버튼 클릭
        btnSearch.setOnClickListener(v -> {
            String kw = etKeyword.getText().toString().trim();
            if (!kw.isEmpty()) {
                InputMethodManager imm = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etKeyword.getWindowToken(), 0);
                etKeyword.clearFocus();
                searchBooks(kw);
            } else {
                Toast.makeText(this, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        // 홈 버튼 클릭
        btnHome.setOnClickListener(v -> {
            etKeyword.setText("");
            etKeyword.clearFocus();
            searchAdapter.submitList(new ArrayList<>());
            showBookmarks();
        });
    }

    // ── 홈(북마크 리스트) 표시 ─────────────────────────────────────────────
    private void showBookmarks() {
        tvHeader.setText("내 북마크");
        rvContent.setAdapter(bookmarkAdapter);
        bookmarkAdapter.submitList(new ArrayList<>(bookmarkList));
        btnHome.setVisibility(View.GONE);
    }

    // ── 검색 결과 표시 ─────────────────────────────────────────────────────
    private void showSearchResults(List<Book> results) {
        tvHeader.setText("검색 결과");
        rvContent.setAdapter(searchAdapter);
        searchAdapter.submitList(results);
        btnHome.setVisibility(View.VISIBLE);
    }

    // ── 북마크 클릭 리스너 ─────────────────────────────────────────────────
    @Override
    public void onBookmarkClick(Book book) {
        String isbn = (book.getIsbn() != null) ? book.getIsbn() : "";
        boolean wasBookmarked = false;
        for (Book b : bookmarkList) {
            if (b.getIsbn().equals(isbn)) {
                wasBookmarked = true;
                break;
            }
        }
        if (wasBookmarked) {
            for (int i = 0; i < bookmarkList.size(); i++) {
                if (bookmarkList.get(i).getIsbn().equals(isbn)) {
                    bookmarkList.remove(i);
                    break;
                }
            }
            searchAdapter.removeFromBookmarkSet(isbn);
            Toast.makeText(this, "북마크가 취소되었습니다: " + book.getTitle(), Toast.LENGTH_SHORT).show();
        } else {
            bookmarkList.add(book);
            searchAdapter.addToBookmarkSet(isbn);
            Toast.makeText(this, "북마크에 추가되었습니다: " + book.getTitle(), Toast.LENGTH_SHORT).show();
        }
        bookmarkAdapter.submitList(new ArrayList<>(bookmarkList));
        searchAdapter.submitList(searchAdapter.getCurrentList());
        saveBookmarksToPrefs();
    }

    // ── 별점·코멘트 클릭 리스너 ─────────────────────────────────────────────
    @Override
    public void onRateClick(Book book) {
        showRatingCommentDialog(book);
    }

    private void showRatingCommentDialog(Book book) {
        // (1) Firestore에서 이미 저장된 내 별점·코멘트 불러오기
        String isbn = book.getIsbn();
        RatingComment existingLocal = RatingCommentStorage.get(this, isbn);
        float initRating = (existingLocal != null) ? existingLocal.getRating() : 0f;
        String initComment = (existingLocal != null) ? existingLocal.getComment() : "";

        // (2) 다이얼로그 레이아웃 인플레이트
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_rating_comment, null);
        RatingBar ratingBar = dialogView.findViewById(R.id.dialogRatingBar);
        EditText etComment  = dialogView.findViewById(R.id.dialogEtComment);

        ratingBar.setRating(initRating);
        etComment.setText(initComment);

        // (3) AlertDialog 구성
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("별점 및 코멘트")
                .setView(dialogView)
                .setPositiveButton("저장", (dialog, which) -> {
                    // ● 반드시 final 또는 effectively final로 선언
                    final float  newRating  = ratingBar.getRating();
                    final String newComment = etComment.getText().toString().trim();
                    final String userId     = currentUser.getUid();
                    // ● 삼항 연산자로만 한 번 값 할당 → effectively final
                    final String userName   =
                            (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty())
                                    ? currentUser.getDisplayName()
                                    : currentUser.getEmail();

                    Log.d("RatingTest",
                            "저장 클릭: isbn="   + book.getIsbn() +
                                    " rating="         + newRating +
                                    " comment="        + newComment +
                                    " userName="       + userName
                    );

                    db.runTransaction((Transaction.Function<Void>) transaction -> {
                        DocumentSnapshot bookSnap = transaction.get(
                                db.collection("books").document(book.getIsbn())
                        );
                        double ratingSum   = 0.0;
                        long   ratingCount = 0;
                        if (bookSnap.exists()) {
                            Object rsObj = bookSnap.get("ratingSum");
                            Object rcObj = bookSnap.get("ratingCount");
                            ratingSum   = (rsObj instanceof Number)
                                    ? ((Number) rsObj).doubleValue()
                                    : 0.0;
                            ratingCount = (rcObj instanceof Number)
                                    ? ((Number) rcObj).longValue()
                                    : 0L;
                        }
                        // 기존 사용자 별점 보정
                        DocumentSnapshot userSnap = transaction.get(
                                db.collection("books")
                                        .document(book.getIsbn())
                                        .collection("userRatings")
                                        .document(userId)
                        );
                        if (userSnap.exists()) {
                            double oldRating = userSnap.getDouble("rating");
                            ratingSum   -= oldRating;
                            ratingCount -= 1;
                        }
                        // 새로운 별점 반영
                        ratingSum   += newRating;
                        ratingCount += 1;

                        // (1) books/{isbn} 문서 업데이트
                        transaction.set(
                                db.collection("books").document(book.getIsbn()),
                                new BookRatingAggregate(ratingSum, ratingCount)
                        );
                        // (2) books/{isbn}/userRatings/{userId} 문서에 rating, comment, userName 저장
                        transaction.set(
                                db.collection("books")
                                        .document(book.getIsbn())
                                        .collection("userRatings")
                                        .document(userId),
                                new UserRatingComment(newRating, newComment, userName)
                        );
                        return null;
                    }).addOnSuccessListener(unused -> {
                        Log.d("RatingTest", "Firestore 트랜잭션 성공");
                        // 로컬 저장 및 UI 갱신
                        RatingComment rcLocal = new RatingComment(newRating, newComment);
                        RatingCommentStorage.put(MainActivity.this, book.getIsbn(), rcLocal);
                        searchAdapter.notifyDataSetChanged();
                        bookmarkAdapter.notifyDataSetChanged();
                        Toast.makeText(MainActivity.this, "평가·코멘트가 저장되었습니다.", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Log.e("RatingTest", "트랜잭션 실패: " + e.getMessage(), e);
                        Toast.makeText(MainActivity.this,
                                "저장 중 오류가 발생했습니다:\n" + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
                })
                .setNegativeButton("취소", null);

        // 다이얼로그를 create → show 한 뒤 버튼 색을 바꿔줍니다.
        AlertDialog dialog = builder.create();
        dialog.show();

        // “저장” 버튼 글자 색을 검정으로 설정
        Button btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (btnSave != null) {
            btnSave.setTextColor(
                    ContextCompat.getColor(this, android.R.color.black)
            );
        }
        // “취소” 버튼 글자 색도 검정으로 바꿔줍니다.
        Button btnCancel = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        if (btnCancel != null) {
            btnCancel.setTextColor(
                    ContextCompat.getColor(this, android.R.color.black)
            );
        }
    }

    // ── 댓글 보기 클릭 리스너 ─────────────────────────────────────────────────
    @Override
    public void onViewCommentsClick(Book book) {
        showCommentsDialog(book.getIsbn());
    }

    private void showCommentsDialog(String isbn) {
        // 다이얼로그 레이아웃 인플레이트
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_comments, null);
        RecyclerView rvComments = dialogView.findViewById(R.id.rvComments);
        rvComments.setLayoutManager(new LinearLayoutManager(this));

        // Firestore에서 해당 ISBN의 userRatings 전체 읽어오기
        db.collection("books")
                .document(isbn)
                .collection("userRatings")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<UserRatingComment.Item> commentItems = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        // userName 필드를 가져와서 표시
                        String userName = doc.getString("userName");
                        double rating   = doc.getDouble("rating") == null
                                ? 0.0 : doc.getDouble("rating");
                        String comment  = doc.getString("comment");

                        commentItems.add(
                                new UserRatingComment.Item(userName, rating, comment)
                        );
                    }

                    if (commentItems.isEmpty()) {
                        Toast.makeText(this, "등록된 댓글이 없습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        CommentsAdapter adapter = new CommentsAdapter(commentItems);
                        rvComments.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "댓글 불러오기 실패: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });

        // 다이얼로그 빌더 설정
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("댓글 목록")
                .setView(dialogView)
                .setPositiveButton("닫기", null);

        // create → show 순서로 다이얼로그를 띄워야 버튼 객체를 가져와 수정할 수 있습니다
        AlertDialog dialog = builder.create();
        dialog.show();

        // “닫기” 버튼 글자 색을 검정으로 설정
        Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (positive != null) {
            positive.setTextColor(
                    ContextCompat.getColor(this, android.R.color.black)
            );
        }
    }

    // ── SharedPreferences로 북마크 저장/불러오기 ────────────────────────────────
    private void saveBookmarksToPrefs() {
        JSONArray array = new JSONArray();
        for (Book b : bookmarkList) {
            array.put(b.toJson());
        }
        prefs.edit()
                .putString(KEY_BOOKMARKS, array.toString())
                .apply();
    }

    private void loadBookmarksFromPrefs() {
        String json = prefs.getString(KEY_BOOKMARKS, null);
        if (json == null || json.isEmpty()) return;
        try {
            JSONArray array = new JSONArray(json);
            bookmarkList.clear();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Book b = Book.fromJson(obj);
                bookmarkList.add(b);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // ── 도서 검색 API 호출 및 결과 처리 ─────────────────────────────────────────
    private void searchBooks(String keyword) {
        LibraryApiService apiService = RetrofitClient.getInstance()
                .create(LibraryApiService.class);
        Call<String> call = apiService.searchBooksRaw(
                API_KEY,
                "xml",       // apiType
                "total",     // srchTarget
                keyword,     // kwd
                1,           // pageNum
                20,          // pageSize
                "",          // sort
                "도서",      // category
                ""           // licYn
        );
        Log.d("BookSearch", "searchBooks 호출: keyword=" + keyword);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> res) {
                if (res.isSuccessful() && res.body() != null) {
                    parseAndDisplay(res.body());
                } else {
                    Toast.makeText(MainActivity.this,
                            "응답 실패: 코드 " + res.code(),
                            Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(MainActivity.this,
                        "네트워크 오류: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void parseAndDisplay(String xml) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xml));

            List<Book> list = new ArrayList<>();
            String title = "", author = "", year = "", isbn = "";
            String text = "";
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tag = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("item".equals(tag)) {
                            title = author = year = isbn = "";
                        }
                        break;
                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if ("title_info".equals(tag)) {
                            title = text;
                        } else if ("author_info".equals(tag)) {
                            author = text;
                        } else if ("pub_year_info".equals(tag)) {
                            year = text;
                        } else if ("isbn".equals(tag)) {
                            isbn = text.replaceAll("-", "").trim();
                        } else if ("item".equals(tag)) {
                            list.add(new Book(title, author, year, isbn));
                        }
                        break;
                }
                eventType = parser.next();
            }

            if (list.isEmpty()) {
                Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
            }
            showSearchResults(list);

        } catch (Exception e) {
            Toast.makeText(this,
                    "파싱 오류 발생: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    // ── Firestore에서 평균 평점·카운트 읽어오기 헬퍼 ─────────────────────────────
    public void loadAggregateIntoView(String isbn, TextView tvAvg, TextView tvCount) {
        db.collection("books")
                .document(isbn)
                .get()
                .addOnSuccessListener(docSnap -> {
                    if (docSnap.exists()) {
                        Double sumObj   = docSnap.getDouble("ratingSum");
                        Long   countObj = docSnap.getLong("ratingCount");

                        double sum   = (sumObj == null) ? 0.0 : sumObj;
                        long   count = (countObj == null) ? 0L : countObj;

                        if (count > 0) {
                            double avg = sum / count;
                            tvAvg.setText(String.format("평균 평점: %.1f", avg));
                            tvCount.setText("평가자 수: " + count);
                        } else {
                            tvAvg.setText("평균 평점: -");
                            tvCount.setText("평가자 수: 0");
                        }
                    } else {
                        tvAvg.setText("평균 평점: -");
                        tvCount.setText("평가자 수: 0");
                    }
                })
                .addOnFailureListener(e -> {
                    tvAvg.setText("평균 평점: -");
                    tvCount.setText("평가자 수: -");
                });
    }
}
