package com.example.ptyxiakh.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ptyxiakh.R;
import com.example.ptyxiakh.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private final Context context;
    private List<ModelChat> chatList;
    private final FirebaseUser fUser;

    public AdapterChat(Context context, List<ModelChat> chatList) {
        this(context, chatList, null);
    }

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList != null ? chatList : new ArrayList<>();
        this.fUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view;
        if (viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, viewGroup, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, viewGroup, false);
        }
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int position) {
        ModelChat chat = chatList.get(position);
        String message = chat.getMessage();
        String timeStamp = chat.getTimestamp();
        String type = chat.getType();

        String dateTime = convertTimestampToDateTime(timeStamp);

        if ("text".equals(type)) {
            myHolder.messageTv.setVisibility(View.VISIBLE);
            myHolder.messageIv.setVisibility(View.GONE);
            myHolder.messageTv.setText(message);
        } else {
            myHolder.messageTv.setVisibility(View.GONE);
            myHolder.messageIv.setVisibility(View.VISIBLE);
            Picasso.get().load(message).placeholder(R.drawable.ic_image_black).into(myHolder.messageIv);
        }

        myHolder.timeTv.setText(dateTime);

        myHolder.messageLAyout.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Delete");
            builder.setMessage("Are you sure you want to delete this message?");
            builder.setPositiveButton("Delete", (dialogInterface, i1) -> {
                int pos = myHolder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    deleteMessage(pos);
                }
            });
            builder.setNegativeButton("No", (dialogInterface, i1) -> dialogInterface.dismiss());
            builder.create().show();
        });

        if (position == chatList.size() - 1) {
            myHolder.isSeenTv.setText(chat.isSeen() ? "Seen" : "Delivered");
        } else {
            myHolder.isSeenTv.setVisibility(View.GONE);
        }
    }

    private String convertTimestampToDateTime(String timestamp) {
        try {
            long time = Long.parseLong(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("Europe/Athens"));
            return sdf.format(time);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return "Invalid date";
        }
    }

    private void deleteMessage(int position) {
        if (position < 0 || position >= chatList.size()) {
            Toast.makeText(context, "Invalid message position", Toast.LENGTH_SHORT).show();
            return;
        }

        String myUID = fUser.getUid();
        String msgTimeStamp = chatList.get(position).getTimestamp();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isMessageDeleted = false;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (myUID.equals(ds.child("sender").getValue())) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("message", "This message was deleted...");
                        ds.getRef().updateChildren(hashMap);
                        Toast.makeText(context, "Message deleted...", Toast.LENGTH_SHORT).show();
                        isMessageDeleted = true;
                    }
                }
                if (!isMessageDeleted) {
                    Toast.makeText(context, "You can only delete your own messages...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to delete message.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList != null ? chatList.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (fUser != null && chatList.get(position).getSender() != null) {
            return chatList.get(position).getSender().equals(fUser.getUid()) ? MSG_TYPE_RIGHT : MSG_TYPE_LEFT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    class MyHolder extends RecyclerView.ViewHolder {

        ImageView profileIv, messageIv;
        TextView messageTv, timeTv, isSeenTv;
        LinearLayout messageLAyout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profileIv = itemView.findViewById(R.id.profileIv);
            messageIv = itemView.findViewById(R.id.messageIv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            isSeenTv = itemView.findViewById(R.id.isSeenTv);
            messageLAyout = itemView.findViewById(R.id.messageLayout);
        }
    }
}
