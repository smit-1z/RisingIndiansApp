package com.example.risingindians;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.akshaykale.swipetimeline.TimelineFragment;
import com.akshaykale.swipetimeline.TimelineGroupType;
import com.akshaykale.swipetimeline.TimelineObject;
import com.akshaykale.swipetimeline.TimelineObjectClickListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class Timeline_Fragment extends Fragment implements TimelineObjectClickListener {

    private TimelineFragment mFragment;
    private FirebaseFirestore firebaseFirestore;
    private static final String TAG = "Firelog";
    private long millisecond;
    private ArrayList<TimelineObject> objs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timeline,container,false);
        setHasOptionsMenu(true);
        // instantiate the TimelineFragment
        mFragment = new TimelineFragment();

        firebaseFirestore = FirebaseFirestore.getInstance();

        objs = new ArrayList<TimelineObject>();

        firebaseFirestore.collection("Events").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if(e != null){
                    Log.d(TAG,"Error : "+e.getMessage());
                }

                for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){

                    if(doc.getType() == DocumentChange.Type.ADDED){
                        String name = doc.getDocument().getString("desc");
                        String url = doc.getDocument().getString("image_url");
                        try {
                            millisecond = doc.getDocument().getDate("timestamp").getTime();

                        } catch (Exception er) {

                            Toast.makeText(getContext(), "Exception : " + er.getMessage(), Toast.LENGTH_SHORT).show();

                        }

                        objs.add(new TestO(millisecond,name,url));
                        mFragment.setData(objs, TimelineGroupType.MONTH);


                    }
                }

            }
        });


        //Set data
       //mFragment.setData(objs, TimelineGroupType.MONTH);

        //Set configurations
        mFragment.addOnClickListener(this);
        mFragment.setImageLoadEngine(new ImageLoad(getContext()));
        //TimeLineConfig.setTimelineCardTextBackgroundColour("#fff000");
        //TimeLineConfig.setTimelineIndicatorLineColour("#fff000"); //yellow line color

        //Load frag after configs and setting the data
        loadFragment(mFragment);

        return view;
    }



    private void loadFragment(Fragment newFragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, newFragment);
        transaction.commit();
    }


    @Override
    public void onTimelineObjectClicked(TimelineObject timelineObject) {
        Toast.makeText(getContext(),"Clicked: "+timelineObject.getTitle(),Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTimelineObjectLongClicked(TimelineObject timelineObject) {
        Toast.makeText(getContext(),"LongClicked: "+timelineObject.getTitle(),Toast.LENGTH_LONG).show();

    }
}
