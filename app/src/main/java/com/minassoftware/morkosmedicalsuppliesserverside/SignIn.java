package com.minassoftware.morkosmedicalsuppliesserverside;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.minassoftware.morkosmedicalsuppliesserverside.Model.Common;
import com.minassoftware.morkosmedicalsuppliesserverside.Model.User;
import com.rengwuxian.materialedittext.MaterialEditText;

public class SignIn extends AppCompatActivity {

    MaterialEditText phoneNum,pass;
    Button btnSignIn;//Id bta3oo mn 3'ear _
    //Elli b _ da bta3 el main Screen intro
    TextView tvSignin;
    DatabaseReference tb_user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_in);
        phoneNum=(MaterialEditText)findViewById(R.id.ET_Phone);
        pass=(MaterialEditText)findViewById(R.id.ET_Pass);
        btnSignIn=(Button)findViewById(R.id.BtnSignIn);
        tvSignin=(TextView)findViewById(R.id.tv_signin);

        //Firebase
        FirebaseDatabase database=FirebaseDatabase.getInstance();
        tb_user=database.getReference("User");

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInUser(phoneNum.getText().toString(),pass.getText().toString());
            }
        });

}

private void signInUser(final String phone, String password){

        final ProgressDialog dialog=new ProgressDialog(SignIn.this);
        dialog.setMessage("Please Wait...");
        dialog.show();

        final String localph=phone;
        final String localpass=password;
        tb_user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(phone).exists()){
                    dialog.dismiss();
                    User user=dataSnapshot.child(localph).getValue(User.class);
                    user.setPhone(localph);
                    if(Boolean.parseBoolean(user.getIsStaff())) {
                        if (user.getpassword().equals(localpass))
                        {
                            //true Start a new activity
                            Intent intent=new Intent(SignIn.this,home.class);
                            Common.CurrentUser=user;
                            startActivity(intent);
                            finish();
                        } else
                           Toast.makeText(SignIn.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                        
                    }else
                        Toast.makeText(SignIn.this, "Login with staff account", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(SignIn.this, "User is not exists", Toast.LENGTH_SHORT).show();
           
                }

                
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
}

    }

