// MainActivity.java
package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.BookAdapter;
import com.example.myapplication.model.Book;
import com.example.myapplication.network.LibraryApiService;
import com.example.myapplication.network.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String API_KEY = "22c226e7b1bdbaf8c4e32631e83c85c542df343381694a2d2d677aafce9e13c4";
    private LibraryApiService apiService;
    private BookAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiService = RetrofitClient
                .getInstance()
                .create(LibraryApiService.class);

        RecyclerView rv = findViewById(R.id.rvBooks);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookAdapter();
        rv.setAdapter(adapter);

        EditText et = findViewById(R.id.etKeyword);
        Button btn = findViewById(R.id.btnSearch);
        btn.setOnClickListener(v -> {
            String kw = et.getText().toString().trim();
            if (!kw.isEmpty()) searchBooks(kw);
        });
    }

    private void searchBooks(String keyword) {
        apiService.searchBooksRaw(
                API_KEY, keyword, "json", 1, 20, null, null
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call,
                                   Response<String> res) {
                if (res.isSuccessful() && res.body()!=null) {
                    parseAndDisplay(res.body());
                } else {
                    Toast.makeText(MainActivity.this,
                            "응답 실패: "+res.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(MainActivity.this,
                        "연결 오류: "+t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void parseAndDisplay(String json) {
        try {
            JSONObject root = new JSONObject(json);
            JSONObject response = root.getJSONObject("response");
            JSONArray docs = response.getJSONArray("docs");

            List<Book> list = new ArrayList<>();
            for (int i = 0; i < docs.length(); i++) {
                JSONObject item = docs.getJSONObject(i);
                String title  = item.optString("title_info");
                String author = item.optString("author_info");
                String year   = item.optString("pub_year_info");
                list.add(new Book(title, author, year));
            }
            adapter.submitList(list);
        } catch (Exception e) {
            Toast.makeText(this,
                    "파싱 오류: "+e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}
