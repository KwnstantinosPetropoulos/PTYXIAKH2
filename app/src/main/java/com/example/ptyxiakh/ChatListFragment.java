package com.example.ptyxiakh;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.example.ptyxiakh.adapters.AdapterChatList;
import com.example.ptyxiakh.models.ModelChat;
import com.example.ptyxiakh.models.ModelChatlist;
import com.example.ptyxiakh.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatListFragment extends Fragment {

    // Firebase Authentication and User
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    // UI Components
    private RecyclerView recyclerView;

    // Data Lists
    private List<ModelChatlist> chatlistList;
    private List<ModelUsers> usersList;

    // Database Reference and Adapter
    private DatabaseReference reference;
    private AdapterChatList adapterChatList;

    public ChatListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatListFragment.
     */
    public static ChatListFragment newInstance(String param1, String param2) {
        ChatListFragment fragment = new ChatListFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Retrieve parameters from the bundle if needed
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        // Initialize Firebase Authentication and User
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);

        // Initialize Data Lists
        chatlistList = new ArrayList<>();
        usersList = new ArrayList<>();

        // Load chat list data
        loadChatList();

        return view;
    }

    private void loadChatList() {
        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatlistList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelChatlist chatlist = ds.getValue(ModelChatlist.class);
                    if (chatlist != null) {
                        chatlistList.add(chatlist);
                    }
                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors.
            }
        });
    }

    private void loadChats() {
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelUsers user = ds.getValue(ModelUsers.class);
                    if (user != null && user.getUid() != null) {
                        for (ModelChatlist chatlist : chatlistList) {
                            if (chatlist.getId() != null && user.getUid().equals(chatlist.getId())) {
                                usersList.add(user);
                                break;
                            }
                        }
                    }
                }
                // Initialize Adapter
                adapterChatList = new AdapterChatList(getContext(), usersList);
                recyclerView.setAdapter(adapterChatList);
                // Set last message for each user
                for (ModelUsers user : usersList) {
                    lastMessage(user.getUid());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors.
            }
        });
    }

    private void lastMessage(String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String theLastMessage = "default";
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat == null) {
                        continue;
                    }

                    String sender = chat.getSender();
                    String receiver = chat.getReceiver();
                    String type = chat.getType();
                    String message = chat.getMessage();

                    // Ensure sender and receiver are not null before comparing
                    if (sender != null && receiver != null) {
                        if ((receiver.equals(currentUser.getUid()) && sender.equals(userId)) ||
                                (sender.equals(currentUser.getUid()) && receiver.equals(userId))) {
                            if (type != null && type.equals("image")) {
                                theLastMessage = "Sent a photo";
                            } else {
                                theLastMessage = message != null ? message : "No message";
                            }
                        }
                    }
                }
                adapterChatList.setLastMessageMap(userId, theLastMessage);
                adapterChatList.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors.
            }
        });
    }



    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            // User is not signed in, redirect to main activity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        // Hide addpost icon from this fragment
        menu.findItem(R.id.action_add_post).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        }
        else if (id==R.id.action_settings){
            startActivity(new Intent(getActivity(),SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
