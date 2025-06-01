// app/src/main/java/com/example/myapplication/adapter/CommentsAdapter.java
package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.UserRatingComment;  //☆☆ 수정된 경로

import java.util.List;

/**
 * Firestore에서 받아온 댓글(userRatings) 리스트를 표시하기 위한 어댑터
 */
public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.VH> {

    private final List<UserRatingComment.Item> dataList;

    // 생성자
    public CommentsAdapter(List<UserRatingComment.Item> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        UserRatingComment.Item item = dataList.get(position);
        // ● 이제 UID 대신 “닉네임 (userName)” 을 화면에 표시
        holder.tvUser.setText(item.userName);
        holder.tvRating.setText("별점: " + item.rating);
        holder.tvText.setText(item.comment);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvUser, tvRating, tvText;

        VH(View itemView) {
            super(itemView);
            tvUser   = itemView.findViewById(R.id.tvCommentUser);
            tvRating = itemView.findViewById(R.id.tvCommentRating);
            tvText   = itemView.findViewById(R.id.tvCommentText);
        }
    }
}
