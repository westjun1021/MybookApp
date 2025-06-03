// app/src/main/java/com/example/myapplication/adapter/BookAdapter.java
package com.example.myapplication.adapter;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.myapplication.R;
import com.example.myapplication.model.Book;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * BookAdapter: RecyclerView 어댑터
 * - ISBN을 기반으로 Open Library Covers API의 JPG URL을 바로 Glide 로드
 * - 투명 빈 이미지(너비·높이 ≤ 10px)는 placeholder로 대체
 */
public class BookAdapter extends ListAdapter<Book, BookAdapter.VH> {

    private static final String TAG = "BookAdapter";

    /** (1) 북마크 클릭 이벤트 전달용 */
    public interface OnBookmarkClickListener {
        void onBookmarkClick(Book book);
    }

    /** (2) 별점·코멘트 클릭 이벤트 전달용 */
    public interface OnRateClickListener {
        void onRateClick(Book book);
    }

    /** (3) 댓글 보기 클릭 이벤트 전달용 */
    public interface OnViewCommentsClickListener {
        void onViewCommentsClick(Book book);
    }

    private final OnBookmarkClickListener     bookmarkListener;
    private final OnRateClickListener         rateListener;
    private final OnViewCommentsClickListener viewCommentsListener;
    private final List<Book>                  actualBookmarkList;
    private final Set<String>                 bookmarkedIsbns;

    public BookAdapter(
            OnBookmarkClickListener bookmarkListener,
            OnRateClickListener rateListener,
            OnViewCommentsClickListener viewCommentsListener,
            List<Book> actualBookmarkList
    ) {
        super(DIFF_CALLBACK);
        this.bookmarkListener     = bookmarkListener;
        this.rateListener         = rateListener;
        this.viewCommentsListener = viewCommentsListener;
        this.actualBookmarkList   = actualBookmarkList;
        this.bookmarkedIsbns      = new HashSet<>();
        // 로컬에 저장된 bookmarkList를 기반으로 Set을 초기화
        for (Book b : actualBookmarkList) {
            String isbn = b.getIsbn();
            if (isbn != null && !isbn.isEmpty()) {
                this.bookmarkedIsbns.add(isbn.trim());
            }
        }
    }

    /** 외부(MainActivity)에서 북마크 Set에 isbn을 추가하도록 호출 */
    public void addToBookmarkSet(String isbn) {
        if (isbn != null && !isbn.isEmpty()) {
            bookmarkedIsbns.add(isbn.trim());
        }
    }

    /** 외부(MainActivity)에서 북마크 Set에서 isbn을 제거하도록 호출 */
    public void removeFromBookmarkSet(String isbn) {
        if (isbn != null && !isbn.isEmpty()) {
            bookmarkedIsbns.remove(isbn.trim());
        }
    }

    private static final DiffUtil.ItemCallback<Book> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Book>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull Book oldItem,
                        @NonNull Book newItem
                ) {
                    return oldItem.getIsbn().trim().equals(newItem.getIsbn().trim());
                }
                @Override
                public boolean areContentsTheSame(
                        @NonNull Book oldItem,
                        @NonNull Book newItem
                ) {
                    return oldItem.equals(newItem);
                }
            };

    @NonNull
    @Override
    public VH onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull VH holder,
            int position
    ) {
        Book book = getItem(position);

        // (1) 제목 · 저자 · 연도
        holder.title.setText(book.getTitle());
        holder.authorYear.setText(book.getAuthor() + " | " + book.getYear());

        // (2) ISBN → covers.openlibrary.org JPG URL을 바로 Glide 로드
        String isbnRaw = (book.getIsbn() != null) ? book.getIsbn().trim() : "";
        loadCoverDirectJpg(isbnRaw, holder.cover);

        // (3) 북마크 아이콘 상태 반영
        boolean isBookmarked = bookmarkedIsbns.contains(isbnRaw);
        if (isBookmarked) {
            holder.btnBookmark.setImageResource(R.drawable.ic_bookmark_filled);
        } else {
            holder.btnBookmark.setImageResource(R.drawable.ic_bookmark_border);
        }
        holder.btnBookmark.setOnClickListener(v -> bookmarkListener.onBookmarkClick(book));

        // (4) Firestore에서 평균 별점·평가자 수 가져와서 요약 표시
        if (!isbnRaw.isEmpty()) {
            FirebaseFirestore.getInstance()
                    .collection("books")
                    .document(isbnRaw)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            Double ratingSum = document.getDouble("ratingSum");
                            Long ratingCount = document.getLong("ratingCount");
                            if (ratingSum != null && ratingCount != null && ratingCount > 0) {
                                float avg = (float) (ratingSum / ratingCount);
                                holder.tvRatingCommentSummary.setText(
                                        String.format("평균: %.1f   (%d명)", avg, ratingCount)
                                );
                            } else {
                                holder.tvRatingCommentSummary.setText("평균: -   (0명)");
                            }
                        } else {
                            holder.tvRatingCommentSummary.setText("평균: -   (0명)");
                        }
                    })
                    .addOnFailureListener(e -> {
                        holder.tvRatingCommentSummary.setText("평균: -   (0명)");
                    });
        } else {
            holder.tvRatingCommentSummary.setText("평균: -   (0명)");
        }

        // (5) 평가하기 버튼 클릭 리스너
        holder.btnRate.setOnClickListener(v -> rateListener.onRateClick(book));

        // (6) 댓글 보기 버튼 클릭 리스너
        holder.btnViewComments.setOnClickListener(v -> viewCommentsListener.onViewCommentsClick(book));

        // (7) 짝수/홀수 배경색 처리 (선택사항)
        int colorResId = (position % 2 == 0)
                ? R.color.row_even_background
                : R.color.row_odd_background;
        holder.itemView.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.getContext(), colorResId)
        );
    }

    /**
     * “직접 JPG URL”을 Glide로 로드한다.
     * - ex) https://covers.openlibrary.org/b/isbn/{ISBN}-L.jpg
     * - 서버에서 투명 빈 이미지를 내려줄 수도 있으므로,
     *   onResourceReady()에서 너비·높이가 10px 이하인 경우 placeholder로 대체
     */
    private void loadCoverDirectJpg(String isbn, ImageView targetView) {
        // 1) ISBN이 비어 있으면 placeholder
        if (isbn == null || isbn.isEmpty()) {
            Log.d(TAG, "ISBN이 비어 있음 → placeholder 설정");
            targetView.setImageResource(R.drawable.placeholder);
            return;
        }

        // 2) “공백”으로 여러 ISBN이 붙어 있는 경우 첫 번째 토큰만 사용
        String primaryIsbn = isbn.split("\\s+")[0].trim();
        if (primaryIsbn.isEmpty()) {
            Log.d(TAG, "split 결과가 비어 있음 → placeholder 설정");
            targetView.setImageResource(R.drawable.placeholder);
            return;
        }

        // 3) JPG URL 생성
        String jpgUrl = "https://covers.openlibrary.org/b/isbn/" + primaryIsbn + "-L.jpg";
        Log.d(TAG, "JPG 직접 로드 시도: " + jpgUrl);

        // 4) Glide 로드
        Glide.with(targetView.getContext())
                .load(jpgUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e,
                                                Object model,
                                                Target<Drawable> target,
                                                boolean isFirstResource) {
                        // 다운로드 실패 또는 URL이 잘못된 경우 placeholder
                        targetView.setImageResource(R.drawable.placeholder);
                        return true; // Glide가 추가로 뷰에 세팅하지 않도록
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource,
                                                   Object model,
                                                   Target<Drawable> target,
                                                   DataSource dataSource,
                                                   boolean isFirstResource) {
                        // 아주 작은 투명 빈 이미지는 placeholder로 대체
                        int w = resource.getIntrinsicWidth();
                        int h = resource.getIntrinsicHeight();
                        if (w < 10 && h < 10) {
                            targetView.setImageResource(R.drawable.placeholder);
                            return true; // Glide가 추가로 뷰에 세팅하지 않도록
                        }
                        // 정상 이미지인 경우, 그대로 뷰에 세팅
                        return false;
                    }
                })
                .into(targetView);
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView    cover;
        TextView     title, authorYear;
        ImageButton  btnBookmark;
        TextView     tvRatingCommentSummary;
        Button       btnRate, btnViewComments;

        VH(View itemView) {
            super(itemView);
            cover                   = itemView.findViewById(R.id.book_cover);
            title                   = itemView.findViewById(R.id.book_title);
            authorYear              = itemView.findViewById(R.id.book_author_year);
            btnBookmark             = itemView.findViewById(R.id.btnBookmark);
            tvRatingCommentSummary  = itemView.findViewById(R.id.tvRatingCommentSummary);
            btnRate                 = itemView.findViewById(R.id.btnRate);
            btnViewComments         = itemView.findViewById(R.id.btnViewComments);
        }
    }
}
