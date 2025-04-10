package com.example.ptyxiakh;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ptyxiakh.adapters.AdapterComments;
import com.example.ptyxiakh.models.ModelComment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import androidx.appcompat.widget.Toolbar;

public class PostDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;

    String hisUid, myUid, myEmail, myName, myDp, postId, pLikes, hisDp, hisName, pImage;
    boolean mProcessComment = false;
    boolean mProcessLike = false;
    ProgressDialog pd;
    ImageView uPictireIv, pImageIv;
    TextView uNameTv, pTimeTv, pTitleTv, pDescriptionTv, pLikesTv, pCommentsTv;
    ImageButton moreBtn;
    Button likeBtn, shareBtn;
    LinearLayout profileLayout;
    RecyclerView recyclerView;
    List<ModelComment> commentList;
    AdapterComments adapterComments;
    EditText commentEt;
    ImageButton sendBtn;
    ImageView cAvatarIv;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);



        toolbar = findViewById(R.id.toolbar);  // Add this line
        setSupportActionBar(toolbar);  // Add this line

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Post Detail");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        uPictireIv = findViewById(R.id.uPictureIv);
        pImageIv = findViewById(R.id.pImageIv);
        uNameTv = findViewById(R.id.uNameTv);
        pTimeTv = findViewById(R.id.pTimeTv);
        pTitleTv = findViewById(R.id.pTitleTv);
        pDescriptionTv = findViewById(R.id.pDescriptionTv);
        pLikesTv = findViewById(R.id.pLikesTv);
        pCommentsTv = findViewById(R.id.pCommentsTv);
        moreBtn = findViewById(R.id.moreBtn);
        likeBtn = findViewById(R.id.likeBtn);
        shareBtn = findViewById(R.id.shareBtn);
        profileLayout = findViewById(R.id.profilelayout);
        recyclerView = findViewById(R.id.recyclerView);
        commentEt = findViewById(R.id.commentEt);
        sendBtn = findViewById(R.id.sendBtn);
        cAvatarIv = findViewById(R.id.cAvatarIv);

        // Get post ID from intent
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        // Load post and user information
        loadPostInfo();
        checkUserStatus();
        loadUserInfo();

        if (actionBar != null) {
            actionBar.setSubtitle("SignedIn as: " + myEmail);
        }

        setLikes();
        loadComments();

        // Set button click listeners
        sendBtn.setOnClickListener(view -> postComment());
        likeBtn.setOnClickListener(view -> likePost());
        moreBtn.setOnClickListener(view -> showMoreOptions());

        //share button click
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pTitle = pTitleTv.getText().toString().trim();
                String pDescription = pDescriptionTv.getText().toString().trim();

                BitmapDrawable bitmapDrawable = (BitmapDrawable)pImageIv.getDrawable();
                if (bitmapDrawable==null){
                    shareTextOnly(pTitle,pDescription);
                }
                else {
                    //post with image

                    //convert image to bitmap
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(pTitle,pDescription,bitmap);
                }
            }
        });
    }



    private void shareTextOnly(String pTitle, String pDescription) {
        String shareBody = pTitle +"\n"+ pDescription;

        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.putExtra(Intent.EXTRA_TEXT,shareBody);
        startActivity(Intent.createChooser(sIntent,"Share Via"));
    }

    private void shareImageAndText(String pTitle, String pDescription, Bitmap bitmap) {

        String shareBody = pTitle+ "\n"+ pDescription;

        Uri uri = saveImageToShare(bitmap);

        //share intent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sIntent.putExtra(Intent.EXTRA_TEXT,shareBody);
        sIntent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
        sIntent.setType("image/png");
        startActivity(Intent.createChooser(sIntent,"Share Via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(getCacheDir(),"images");
        Uri uri = null;
        try {
            imageFolder.mkdirs();
            File file = new File(imageFolder,"shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,90,stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(this,"com.example.ptyxiakh.fileprovider",file);
        }
        catch (Exception e) {
            Toast.makeText(this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }

    private void loadComments() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        commentList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelComment modelComment = ds.getValue(ModelComment.class);
                    commentList.add(modelComment);
                }
                adapterComments = new AdapterComments(getApplicationContext(), commentList , myUid,postId);
                recyclerView.setAdapter(adapterComments);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PostDetailActivity", "Error loading comments", error.toException());
            }
        });
    }

    private void showMoreOptions() {
        PopupMenu popupMenu = new PopupMenu(this, moreBtn, Gravity.END);
        if (hisUid.equals(myUid)) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == 0) {
                beginDelete();
            } else if (id == 1) {
                Intent intent = new Intent(PostDetailActivity.this, AddPostActivity.class);
                intent.putExtra("key", "editPost");
                intent.putExtra("editPostId", postId);
                startActivity(intent);
            }
            return false;
        });
        popupMenu.show();
    }

    private void beginDelete() {
        if (pImage.equals("noImage")) {
            deleteWithoutImage();
        } else {
            deleteWithImage();
        }
    }

    private void deleteWithImage() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting...");
        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete().addOnSuccessListener(unused -> {
            Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
            fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        ds.getRef().removeValue();
                    }
                    Toast.makeText(PostDetailActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("PostDetailActivity", "Error onDataChange: " + error.getMessage());
                }
            });
        }).addOnFailureListener(e -> {
            pd.dismiss();
            Toast.makeText(PostDetailActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void deleteWithoutImage() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting...");
        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ds.getRef().removeValue();
                }
                Toast.makeText(PostDetailActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PostDetailActivity", "Error onDataChange: " + error.getMessage());
            }
        });
    }

    private void setLikes() {
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postId).hasChild(myUid)) {
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
                    likeBtn.setText("Liked");
                } else {
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0);
                    likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PostDetailActivity", "Error loading likes", error.toException());
            }
        });
    }

    private void likePost() {
        mProcessLike = true;
        DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mProcessLike) {
                    try {
                        if (snapshot.child(postId).hasChild(myUid)) {
                            int currentLikes = Integer.parseInt(pLikes);
                            postsRef.child(postId).child("pLikes").setValue(String.valueOf(currentLikes - 1));
                            likeRef.child(postId).child(myUid).removeValue();
                        } else {
                            int currentLikes = Integer.parseInt(pLikes);
                            postsRef.child(postId).child("pLikes").setValue(String.valueOf(currentLikes + 1));
                            likeRef.child(postId).child(myUid).setValue("Liked");
                        }
                        mProcessLike = false;
                    } catch (NumberFormatException e) {
                        Log.e("PostDetailActivity", "NumberFormatException: " + e.getMessage());
                        mProcessLike = false;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PostDetailActivity", "Error liking post", error.toException());
            }
        });
    }

    private void postComment() {
        if (mProcessComment) return;
        mProcessComment = true;
        pd = new ProgressDialog(this);
        pd.setMessage("Adding comment...");
        pd.show();

        String comment = commentEt.getText().toString().trim();
        if (TextUtils.isEmpty(comment)) {
            Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            mProcessComment = false;
            pd.dismiss();
            return;
        }

        String timestamp = String.valueOf(System.currentTimeMillis());
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Posts").child(postId);

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("cId", timestamp);
        hashMap.put("comment", comment);
        hashMap.put("timestamp", timestamp);
        hashMap.put("uid", myUid);
        hashMap.put("uEmail", myEmail);
        hashMap.put("uDp", myDp);
        hashMap.put("uName", myName);

        commentsRef.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(aVoid -> {
                    postRef.child("pComments").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String comments = snapshot.getValue(String.class);
                            int newCommentCount;
                            try {
                                if (comments == null || comments.isEmpty() || !comments.matches("\\d+")) {
                                    newCommentCount = 1;
                                } else {
                                    newCommentCount = Integer.parseInt(comments) + 1;
                                }
                                postRef.child("pComments").setValue(String.valueOf(newCommentCount));
                            } catch (NumberFormatException e) {
                                newCommentCount = 1;
                            }
                            mProcessComment = false;
                            commentEt.setText("");
                            pd.dismiss();
                            Toast.makeText(PostDetailActivity.this, "Comment added", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            mProcessComment = false;
                            pd.dismiss();
                            Toast.makeText(PostDetailActivity.this, "Failed to update comment count", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    mProcessComment = false;
                    pd.dismiss();
                    Toast.makeText(PostDetailActivity.this, "Failed to add comment", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadPostInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String pTitle = "" + ds.child("pTitle").getValue();
                    String pDescription = "" + ds.child("pDescription").getValue();
                    pLikes = "" + ds.child("pLikes").getValue();
                    if (TextUtils.isEmpty(pLikes) || !pLikes.matches("\\d+")) {
                        pLikes = "0";
                    }
                    String pTimeStamp = "" + ds.child("pTime").getValue();
                    pImage = "" + ds.child("pImage").getValue();
                    hisDp = "" + ds.child("uDp").getValue();
                    hisUid = "" + ds.child("uid").getValue();
                    String uEmail = "" + ds.child("uEmail").getValue();
                    hisName = "" + ds.child("uName").getValue();
                    String commentCount = "" + ds.child("pComments").getValue();

                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm aa", Locale.getDefault());
                    String pTime = dateFormat.format(calendar.getTime());

                    pTitleTv.setText(pTitle);
                    pDescriptionTv.setText(pDescription);
                    pLikesTv.setText(pLikes + " Likes");
                    pTimeTv.setText(pTime);
                    pCommentsTv.setText(commentCount + " Comments");
                    uNameTv.setText(hisName);

                    if (pImage.equals("noImage")) {
                        pImageIv.setVisibility(View.GONE);
                    } else {
                        pImageIv.setVisibility(View.VISIBLE);
                        try {
                            Picasso.get().load(pImage).into(pImageIv);
                        } catch (Exception e) {
                            Log.e("PostDetailActivity", "Error loading post image", e);
                        }
                    }

                    try {
                        Picasso.get().load(hisDp).placeholder(R.drawable.ic_default_img).into(uPictireIv);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_default_img).into(uPictireIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PostDetailActivity", "Error loading post info", error.toException());
            }
        });
    }

    private void loadUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        myUid = user.getUid();
        myEmail = user.getEmail();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myName = "" + snapshot.child("name").getValue();
                myDp = "" + snapshot.child("image").getValue();
                try {
                    Picasso.get().load(myDp).placeholder(R.drawable.ic_default_img).into(cAvatarIv);
                } catch (Exception e) {
                    Picasso.get().load(R.drawable.ic_default_img).into(cAvatarIv);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PostDetailActivity", "Error loading user info", error.toException());
            }
        });
    }

    private void checkUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            myUid = user.getUid();
            myEmail = user.getEmail();
        } else {
            startActivity(new Intent(PostDetailActivity.this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
