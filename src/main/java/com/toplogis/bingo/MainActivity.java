package com.toplogis.bingo;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener, ValueEventListener {

    private static final int RC_SING_IN = 100;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private TextView tvNickName;
    private Member member;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViews();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        auth = FirebaseAuth.getInstance();
    }

    private void findViews() {
        tvNickName = findViewById(R.id.tv_nick_name);
    }


    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        auth.addAuthStateListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                break;
            case R.id.action_signout:
                auth.signOut();
                break;
        }
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        user = firebaseAuth.getCurrentUser();
        if (user != null) {
            FirebaseDatabase.getInstance().getReference("user")
                    .child(user.getUid())
                    .child("uid")
                    .setValue(user.getUid());
            FirebaseDatabase.getInstance().getReference("user")
                    .child(user.getUid())
                    .addValueEventListener(this);
        } else {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
            .setAvailableProviders(Arrays.asList(
                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                    new AuthUI.IdpConfig.EmailBuilder().build()
                    )).setIsSmartLockEnabled(false).build(), RC_SING_IN);
        }
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        member = dataSnapshot.getValue(Member.class);
        if(Objects.requireNonNull(member).getNickNmae() == null) {
            final EditText editNickName = new EditText(this);
            new AlertDialog.Builder(this)
                    .setTitle("請輸入暱稱")
                    .setView(editNickName)
                    .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String nickName = editNickName.getText().toString();
                            member.setNickNmae(nickName);
                            FirebaseDatabase.getInstance().getReference("user")
                                    .child(user.getUid())
                                    .setValue(member);

                        }
                    }).setNeutralButton("取消", null)
                    .show();
        } else {
            FirebaseDatabase.getInstance().getReference("user")
                    .child(user.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            member = dataSnapshot.getValue(Member.class);
                            tvNickName.setText(member.getNickNmae());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
}
