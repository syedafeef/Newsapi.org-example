package com.afeef.testnews.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.afeef.testnews.R;
import com.afeef.testnews.api.ApiClient;
import com.afeef.testnews.callback.CategorySelectedCallback;
import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.Holder> {

    Context mcontect;
    private List<ApiClient.Category> localList;
    CategorySelectedCallback callback;


    public CategoryAdapter(Context context, List<ApiClient.Category> list, CategorySelectedCallback listner) {
        localList = list;
        mcontect = context;
        callback = listner;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from( mcontect ).inflate( R.layout.breakcrumb_adapter, parent, false );
        return new Holder( view );
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, final int position) {

        holder.Category.setText( localList.get( position ).title);

        holder.Category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.categorySelectedCallback(localList.get(position).title);
            }
        });

    }

    @Override
    public int getItemCount() {
        return localList.size( );
    }

    public class Holder extends RecyclerView.ViewHolder {
        TextView Category;

        public Holder(@NonNull View itemView) {
            super( itemView );
            Category = itemView.findViewById( R.id.tv_category );
        }
    }

}
