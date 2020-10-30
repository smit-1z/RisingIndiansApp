package com.example.risingindians;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EventRecyclerAdapter extends RecyclerView.Adapter<EventRecyclerAdapter.ViewHolder> {

    public List<EventPost> event_list;
    public Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public EventRecyclerAdapter(List<EventPost> event_list){
        this.event_list = event_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_list_layout,parent,false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        holder.setIsRecyclable(false);

        final String eventPostId = event_list.get(position).EventPostId;
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();

        final String desc_data = event_list.get(position).getDesc();
        holder.setDecText(desc_data);

        final String image_url = event_list.get(position).getImage_url();
        holder.setEventImage(image_url);

        String event_user_id = event_list.get(position).getUser_id();

        if(event_user_id.equals(currentUserId)){
            holder.eventDeleteBtn.setEnabled(true);
            holder.eventDeleteBtn.setVisibility(View.VISIBLE);
        }

        //User Retrieve Data
        firebaseFirestore.collection("Users").document(event_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){

                    String userName = task.getResult().getString("name");
                    String userImage = task.getResult().getString("image");

                    holder.setUserData(userName, userImage);


                } else {

                    //Firebase Exception

                }
            }
        });


        try {
            long millisecond = event_list.get(position).getTimestamp().getTime();
            String dateString = DateFormat.format("MM/dd/yyyy", new Date(millisecond)).toString();
            holder.setTime(dateString);
        } catch (Exception e) {

            Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();

        }
        //Get Likes Count
        firebaseFirestore.collection("Events/" + eventPostId + "/Likes").addSnapshotListener( new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if(!documentSnapshots.isEmpty()){

                    int count = documentSnapshots.size();

                    holder.updateLikesCount(count);

                } else {

                    holder.updateLikesCount(0);

                }

            }
        });

        //Get Likes
        firebaseFirestore.collection("Events/" + eventPostId + "/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if(documentSnapshot.exists()){

                    holder.eventLikeBtn.setImageDrawable(context.getDrawable(R.drawable.ic_likeaccent));

                } else {

                    holder.eventLikeBtn.setImageDrawable(context.getDrawable(R.drawable.ic_likegray));

                }

            }
        });

        //likes Feature
        holder.eventLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Events/" + eventPostId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(!task.getResult().exists()){

                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Events/" + eventPostId + "/Likes").document(currentUserId).set(likesMap);

                        } else {

                            firebaseFirestore.collection("Events/" + eventPostId + "/Likes").document(currentUserId).delete();

                        }

                    }
                });
            }
        });

        //Event Delete
        holder.eventDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Events").document(eventPostId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        event_list.remove(position);
                        notifyDataSetChanged();
                    }
                });
            }
        });

        //Event comment
        holder.eventCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent commentIntent = new Intent(context, CommentsActivity.class);
                commentIntent.putExtra("event_post_id", eventPostId);
                context.startActivity(commentIntent);

            }
        });

        //Get Comments Count
        firebaseFirestore.collection("Events/" + eventPostId + "/Comments").addSnapshotListener( new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if(!documentSnapshots.isEmpty()){

                    int count = documentSnapshots.size();

                    holder.updateCommentsCount(count);

                } else {

                    holder.updateCommentsCount(0);

                }

            }
        });

       /* //Share Button
        holder.eventShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM,image_url );
                shareIntent.setType("image/*");
                shareIntent.putExtra(Intent.EXTRA_TEXT, desc_data);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(Intent.createChooser(shareIntent, "Share Image Using"));


            }
        });
*/
    }

    @Override
    public int getItemCount() {
        return event_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;

        private TextView descView;
        private ImageView eventImageView;
        private TextView eventDate;

        private TextView eventUserName;
        private CircleImageView eventUserImage;

        private ImageView eventLikeBtn;
        private TextView eventLikeCount;
        private TextView eventCommentCount;
        private Button eventDeleteBtn;

        private ImageView eventCommentBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            eventLikeBtn = mView.findViewById(R.id.event_like_btn);
            eventDeleteBtn = mView.findViewById(R.id.event_delete_btn);
            eventCommentBtn = mView.findViewById(R.id.event_comment_icon);

        }

        public void setDecText(String descText){

            descView = mView.findViewById(R.id.event_desc);
            descView.setText(descText);

        }

        public void setEventImage(String downloadUri){

            eventImageView = mView.findViewById(R.id.event_image);

                Glide.with(context).load(downloadUri).into(eventImageView);

        }

        public void setTime(String date) {

            eventDate = mView.findViewById(R.id.event_date);
            eventDate.setText(date);

        }

        public void setUserData(String name, String image){

            eventUserImage = mView.findViewById(R.id.event_user_image);
            eventUserName = mView.findViewById(R.id.event_user_name);

            eventUserName.setText(name);

            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.ic_face);

            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(eventUserImage);

        }

        public void updateLikesCount(int count){

            eventLikeCount = mView.findViewById(R.id.event_like_count);
            eventLikeCount.setText(count + " Likes");

        }

        public void updateCommentsCount(int count){

            eventCommentCount = mView.findViewById(R.id.event_comment_count);
            eventCommentCount.setText(count + " comments");

        }
    }
}
