package com.example.ptyxiakh.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ptyxiakh.R;
import com.example.ptyxiakh.models.ModelComment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterComments extends RecyclerView.Adapter<AdapterComments.myHolder> {

    private final Context context;
    private final List<ModelComment> commentList;
    String myUid, postId;

    public AdapterComments(Context context, List<ModelComment> commentList, String myUid, String postId) {
        this.context = context;
        this.commentList = commentList;
        this.myUid = myUid;
        this.postId = postId;
    }

    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Bind the row comment XML layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_comments, parent, false);
        return new myHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myHolder holder, int position) {
        // Get data
        ModelComment comment = commentList.get(position);
        String uid = comment.getUid();
        String name = comment.getuName();
        String email = comment.getuEmail();
        String image = comment.getuDp();
        String cid = comment.getcId();
        String commentText = comment.getComment();
        String timestamp = comment.getTimestamp();

        // Default values in case data is null
        holder.nameTv.setText(name != null ? name : "Unknown Name");
        holder.commentTv.setText(commentText != null ? commentText : "No Comment");
        holder.timeTv.setText("Unknown Time");

        // Convert timestamp to date string
        if (timestamp != null) {
            try {
                long timeMillis = Long.parseLong(timestamp);
                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                calendar.setTimeInMillis(timeMillis);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm aa", Locale.getDefault());
                String formattedTime = dateFormat.format(calendar.getTime());
                holder.timeTv.setText(formattedTime);
            } catch (NumberFormatException e) {}

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (myUid.equals(uid)){
                        //my comm show delete dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                        builder.setTitle("Delete");
                        builder.setMessage("Are you sure to delete this comment?");
                        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //delete comment
                                deleteComment(cid);
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss(); // Use dialogInterface here instead of dialog
                            }
                        });
                        builder.create().show();

                    }
                    else {
                        Toast.makeText(context, "Can't delete other's comment...", Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }

        // Load image using Picasso, with a placeholder
        if (image != null && !image.isEmpty()) {
            try {
                Picasso.get().load(image).placeholder(R.drawable.ic_default_img).into(holder.avatarIv);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Set a default image if the URL is null or empty
            holder.avatarIv.setImageResource(R.drawable.ic_default_img);
        }
    }

    private void deleteComment(String cid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.child("Comments").child(cid).removeValue();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String comments = "" + snapshot.child("pComments").getValue();
                int newCommentVal = Integer.parseInt(comments)-1;
                ref.child("pComments").setValue(""+newCommentVal);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class myHolder extends RecyclerView.ViewHolder {

        final ImageView avatarIv;
        final TextView nameTv;
        final TextView commentTv;
        final TextView timeTv;

        public myHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            commentTv = itemView.findViewById(R.id.commentTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}
