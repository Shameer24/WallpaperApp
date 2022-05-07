package com.example.wallpaperapp;


import androidx.appcompat.widget.SearchView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.wallpaperapp.Adapters.CuratedAdapter;
import com.example.wallpaperapp.Listeners.CuratedResponseListener;
import com.example.wallpaperapp.Listeners.OnRecyclerClickListener;
import com.example.wallpaperapp.Listeners.SearchResponseListener;
import com.example.wallpaperapp.Models.CuratedApiResponse;
import com.example.wallpaperapp.Models.Photo;
import com.example.wallpaperapp.Models.SearchApiResponse;
import com.example.wallpaperapp.databinding.ActivityDrawerBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends DrawerActivity implements OnRecyclerClickListener {

    ActivityDrawerBinding activityDrawerBinding;
    RecyclerView recyclerView_home;
    CuratedAdapter adapter;
    ProgressDialog dialog;
    RequestManager manager;
    FloatingActionButton fab_next,fab_prev;
    int page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        activityDrawerBinding = ActivityDrawerBinding.inflate(getLayoutInflater());
        setContentView(activityDrawerBinding.getRoot());
        allocateActivityTitle("Home");


        fab_next=findViewById(R.id.fab_next);
        fab_prev=findViewById(R.id.fab_prev);

        
        dialog = new ProgressDialog(this);
        dialog.setTitle("Loading....");
        
        manager = new RequestManager(this);
        manager.getCuratedWallpapers(listener,"1");


    }
    
    private  final CuratedResponseListener listener = new CuratedResponseListener() {
        @Override
        public void onFetch(CuratedApiResponse response, String message) {
            dialog.dismiss();
            if(response.getPhotos().isEmpty()){
                Toast.makeText(MainActivity.this, "No Image Found!!", Toast.LENGTH_SHORT).show();
                return;
            }
            page= response.getPage();
            showData(response.getPhotos());

            fab_next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String next_page = String.valueOf(page+1);
                    manager.getCuratedWallpapers(listener,next_page);
                    dialog.show();
                }
            });
            fab_prev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (page>1){
                        String prev_page =String.valueOf(page-1);
                        manager.getCuratedWallpapers(listener,prev_page);
                        dialog.show();
                    }
                }
            });
        }

        @Override
        public void onError(String message) {
            dialog.dismiss();
            Toast.makeText(MainActivity.this,"Please Check your Internet Connection!!", Toast.LENGTH_SHORT).show();
        }
    };

    private void showData(ArrayList<Photo> photos) {
        recyclerView_home =findViewById(R.id.recycler_home);
        recyclerView_home.setHasFixedSize(true);
        recyclerView_home.setLayoutManager(new GridLayoutManager(this,2));
        adapter = new CuratedAdapter(MainActivity.this,photos,this);
        recyclerView_home.setAdapter(adapter);
    }

    @Override
    public void onClick(Photo photo) {
        startActivity(new Intent(MainActivity.this,WallpaperActivity.class)
        .putExtra("photo", photo));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Type here to search...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                manager.searchCuratedWallpapers(searchResponseListener, "1",query);
                dialog.show();

                fab_next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String next_page = String.valueOf(page+1);
                        manager.searchCuratedWallpapers(searchResponseListener,next_page,query);
                        dialog.show();
                    }
                });

                fab_prev.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (page>1){
                            String prev_page =String.valueOf(page-1);
                            manager.searchCuratedWallpapers(searchResponseListener,prev_page,query);
                            dialog.show();
                        }
                    }
                });

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);

    }
    private final SearchResponseListener searchResponseListener =new SearchResponseListener() {
        @Override
        public void onFetch(SearchApiResponse response, String message) {
            dialog.dismiss();
            if(response.getPhotos().isEmpty()){
                Toast.makeText(MainActivity.this, "No Image Found!!", Toast.LENGTH_SHORT).show();
                return;
            }
            page=response.getPage();
            showData(response.getPhotos());

        }

        @Override
        public void onError(String message) {
            dialog.dismiss();
            Toast.makeText(MainActivity.this,"Searched word is not Relevant.Try another!!", Toast.LENGTH_SHORT).show();
        }
    };

}