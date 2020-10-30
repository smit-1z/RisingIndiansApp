package com.example.risingindians;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class EventsFragment extends Fragment {

    private RecyclerView events_list_view;
    private List<EventPost> event_list;;

    private EventRecyclerAdapter eventRecyclerAdapter;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;


    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public EventsFragment(){

    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_events,container,false);

        event_list = new ArrayList<>();
        events_list_view = view.findViewById(R.id.events_list_view);

        firebaseAuth = FirebaseAuth.getInstance();

        eventRecyclerAdapter = new EventRecyclerAdapter(event_list);
        events_list_view.setLayoutManager(new LinearLayoutManager(container.getContext()));
        events_list_view.setAdapter(eventRecyclerAdapter);
        events_list_view.setHasFixedSize(true);

        if(firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            events_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if(reachedBottom){

                        loadMorePost();

                    }
                }
            });


            Query firstQuery = firebaseFirestore.collection("Events").orderBy("timestamp", Query.Direction.DESCENDING).limit(3);
            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                    if (e != null) {
                        Log.d(TAG, "Error:" + e.getMessage());
                    }
                    else {
                        if (isFirstPageFirstLoad) {
                            lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                            event_list.clear();

                        }

                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String eventPostId = doc.getDocument().getId();
                                final EventPost eventPost = doc.getDocument().toObject(EventPost.class).withId(eventPostId);
                                if (isFirstPageFirstLoad) {

                                    event_list.add(eventPost);

                                } else {

                                    event_list.add(0, eventPost);

                                }


                                eventRecyclerAdapter.notifyDataSetChanged();

                            }
                        }

                        isFirstPageFirstLoad = false;

                    }
                }

            });
        }
        return view;
    }

    public void loadMorePost(){

        if(firebaseAuth.getCurrentUser() != null) {

            Query nextQuery = firebaseFirestore.collection("Events")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(3);

            nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String eventPostId = doc.getDocument().getId();
                                final EventPost eventPost = doc.getDocument().toObject(EventPost.class).withId(eventPostId);

                                event_list.add(eventPost);

                                eventRecyclerAdapter.notifyDataSetChanged();
                            }

                        }
                    }
                }
            });
        }

    }


}
