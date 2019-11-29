package com.neuricius.masterproject.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.neuricius.masterproject.R;
import com.neuricius.masterproject.activity.ActorDetailsActivity;
import com.neuricius.masterproject.adapter.RvFavoriteActorAdapter;
import com.neuricius.masterproject.database.model.ActorDB;

import java.util.List;

public class FavoriteActorsListFragment extends Fragment {

    private RecyclerView rvList;
    //data provider
    private List<ActorDB> data;

    public List<ActorDB> getData() {
        return data;
    }

    public void setData(List<ActorDB> data) {
        this.data = data;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.actor_list_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupActorList(view);
    }

    private void setupActorList(View view) {
        rvList = view.findViewById(R.id.rvActorList);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvList.setLayoutManager(llm);


        RvFavoriteActorAdapter rvfadapter = new RvFavoriteActorAdapter(data, getContext());
        rvList.setAdapter(rvfadapter);
        rvfadapter.setListener(new RvFavoriteActorAdapter.OnFavoritesClickListener() {
            @Override
            public void onFavoritesSelected(ActorDB idActor) {

                Intent intent = new Intent(getContext(), ActorDetailsActivity.class);
                //id u bazi krece od 1, a u listama od 0 ergo -1
                intent.putExtra("idActor", idActor.getId());
                intent.putExtra("origin", "db");
                startActivity(intent);
            }
        });
    }
}
