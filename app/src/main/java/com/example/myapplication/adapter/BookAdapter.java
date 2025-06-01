// app/src/main/java/com/example/myapplication/adapter/BookAdapter.java
package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.model.Book;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BookAdapter extends ListAdapter<Book, BookAdapter.VH> {

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

    private final OnBookmarkClickListener    bookmarkListener;
    private final OnRateClickListener        rateListener;
    private final OnViewCommentsClickListener viewCommentsListener;
    private final List<Book>                 actualBookmarkList;
    private final Set<String>                bookmarkedIsbns;

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
        for (Book b : actualBookmarkList) {
            String isbn = b.getIsbn();
            if (isbn != null && !isbn.isEmpty()) {
                this.bookmarkedIsbns.add(isbn);
            }
        }
    }

    /** 외부(MainActivity)에서 북마크 Set에 isbn을 추가하도록 호출 */
    public void addToBookmarkSet(String isbn) {
        if (isbn != null && !isbn.isEmpty()) {
            bookmarkedIsbns.add(isbn);
        }
    }

    /** 외부(MainActivity)에서 북마크 Set에서 isbn을 제거하도록 호출 */
    public void removeFromBookmarkSet(String isbn) {
        if (isbn != null && !isbn.isEmpty()) {
            bookmarkedIsbns.remove(isbn);
        }
    }

    private static final DiffUtil.ItemCallback<Book> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Book>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull Book oldItem,
                        @NonNull Book newItem
                ) {
                    return oldItem.getIsbn().equals(newItem.getIsbn());
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

        // (1) 표지 · 제목 · 저자·연도 · 북마크 아이콘
        holder.title.setText(book.getTitle());
        holder.authorYear.setText(book.getAuthor() + " | " + book.getYear());
        Glide.with(holder.cover.getContext())
                .load(book.getCoverUrl())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .fallback(R.drawable.placeholder)
                .into(holder.cover);

        String isbn = (book.getIsbn() != null) ? book.getIsbn() : "";
        boolean isBookmarked = bookmarkedIsbns.contains(isbn);

        if (!isbn.isEmpty()) {
            FirebaseFirestore.getInstance()
                    .collection("books")
                    .document(isbn)
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

        // (3) 평가하기 버튼 클릭 리스너
        holder.btnRate.setOnClickListener(v -> rateListener.onRateClick(book));

        // (4) 댓글 보기 버튼 클릭 리스너
        holder.btnViewComments.setOnClickListener(v -> viewCommentsListener.onViewCommentsClick(book));

        // (5) 짝수/홀수 배경색 (선택)
        int colorResId = (position % 2 == 0)
                ? R.color.row_even_background
                : R.color.row_odd_background;
        holder.itemView.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.getContext(), colorResId)
        );
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView    cover;
        TextView     title, authorYear;
        ImageButton  btnBookmark;
        TextView     tvRatingCommentSummary;
        Button       btnRate, btnViewComments;

        VH(View itemView) {
            super(itemView);
            cover                      = itemView.findViewById(R.id.book_cover);
            title                      = itemView.findViewById(R.id.book_title);
            authorYear                 = itemView.findViewById(R.id.book_author_year);
            btnBookmark                = itemView.findViewById(R.id.btnBookmark);
            tvRatingCommentSummary     = itemView.findViewById(R.id.tvRatingCommentSummary);
            btnRate                    = itemView.findViewById(R.id.btnRate);
            btnViewComments            = itemView.findViewById(R.id.btnViewComments);
        }
    }
}
