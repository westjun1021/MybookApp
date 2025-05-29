// app/src/main/java/com/example/myapplication/adapter/BookAdapter.java
package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.model.Book;

public class BookAdapter extends ListAdapter<Book, BookAdapter.VH> {
    public BookAdapter() {
        super(DIFF_CALLBACK);
    }
    private static final DiffUtil.ItemCallback<Book> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Book>() {
                @Override
                public boolean areItemsTheSame(Book a, Book b) {
                    return a.title.equals(b.title);
                }
                @Override
                public boolean areContentsTheSame(Book a, Book b) {
                    return a.equals(b);
                }
            };

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new VH(v);
    }
    @Override
    public void onBindViewHolder(VH holder, int pos) {
        holder.bind(getItem(pos));
    }

    class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAuthor, tvYear;
        VH(View itemView) {
            super(itemView);
            tvTitle  = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvYear   = itemView.findViewById(R.id.tvYear);
        }
        void bind(Book b) {
            tvTitle.setText(b.title);
            tvAuthor.setText(b.author);
            tvYear.setText(b.year);
        }
    }
}
