package com.minassoftware.morkosmedicalsuppliesserverside;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.minassoftware.morkosmedicalsuppliesserverside.Interface.ItemClickListener;
import com.minassoftware.morkosmedicalsuppliesserverside.Model.Category;
import com.minassoftware.morkosmedicalsuppliesserverside.Model.Common;
import com.minassoftware.morkosmedicalsuppliesserverside.Model.Token;
import com.minassoftware.morkosmedicalsuppliesserverside.ViewHolder.MenuViewHolder;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    TextView tvFullName;
    MaterialEditText categoryName;
    Button upload,selectImage;
    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseStorage storage;
    StorageReference storageReference;
    FirebaseRecyclerAdapter adapter;
    Category newCategory;
    Uri saveUri;
    private final int PICK_IMAGE_REQUEST=71;
    DrawerLayout drawer;

    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Menu Management");
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               showDialog();
            }
        });

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Set name for user
        View headerView=navigationView.getHeaderView(0);
        tvFullName=(TextView)headerView.findViewById(R.id.fullname);
        tvFullName.setText(Common.CurrentUser.getName());
        database=FirebaseDatabase.getInstance();
        reference=database.getReference("Category");
        storage=FirebaseStorage.getInstance();
        storageReference=storage.getReference();

        recyclerView=(RecyclerView)findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        loadMenu();

        //sendToken
        updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    private void updateToken(String token) {

        FirebaseDatabase db=FirebaseDatabase.getInstance();
        DatabaseReference tokens=db.getReference("Tokens");
        Token data=new Token(token,true);//Server
        tokens.child(Common.CurrentUser.getPhone()).setValue(data);
    }

    private void uploadImage() {
        if(saveUri!=null){
            final ProgressDialog dialog=new ProgressDialog(this);
            dialog.setMessage("Uploading...");
            dialog.show();
            String Imagename= UUID.randomUUID().toString();
            final StorageReference imageFolder=storageReference.child("images/"+selectImage);
            imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    dialog.dismiss();
                    Toast.makeText(home.this,"Category is added!",Toast.LENGTH_SHORT).show();
                    imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            newCategory=new Category(categoryName.getText().toString(),uri.toString());
                        }
                    });
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(home.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            int progress=(int)Math.round(100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            dialog.setMessage("Uploaded: "+progress+"%");
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICK_IMAGE_REQUEST && resultCode==RESULT_OK && data!=null
                && data.getData()!=null){
            saveUri=data.getData();
            selectImage.setText("Image Selected");
        }
    }
    private void chooseImage() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST);
    }


    private void showDialog() {
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);
        alertDialog.setTitle("Add New Category ");
        alertDialog.setMessage("Add the category titlte & select the image");

        LayoutInflater inflater=this.getLayoutInflater();
        View add_menu_layout=inflater.inflate(R.layout.add_new_category,null);

        categoryName=add_menu_layout.findViewById(R.id.cat_name);
        selectImage=add_menu_layout.findViewById(R.id.btnSelect);
        upload=add_menu_layout.findViewById(R.id.btnUpload);
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });
        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                if(newCategory!=null){
                    reference.push().setValue(newCategory);
                    Snackbar.make(drawer,"New Category "+newCategory.getName()+" was added!",Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    private void loadMenu() {
        adapter= new FirebaseRecyclerAdapter<Category, MenuViewHolder>(Category.class, R.layout.menu_item, MenuViewHolder.class, reference) {
            @Override
            protected void populateViewHolder(MenuViewHolder viewHolder, Category model, final int position) {
                viewHolder.tv_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.imageView);
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int postion, boolean isLongClick) {
                        Intent intent=new Intent(home.this,FoodList.class);
                        intent.putExtra("CategoryId",adapter.getRef(position).getKey());
                        startActivity(intent);
                    }
                });
            }
        };

      recyclerView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();



        if (id == R.id.nav_orders) {

            Intent intent=new Intent(home.this,OrderStatus.class);
            startActivity(intent);

        } else if (id == R.id.nav_logout) {
            Intent intent=new Intent(home.this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    //Update and Delete code :


    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if(item.getTitle().equals(Common.UPDATE)){
            showDialogUpdate(adapter.getRef(item.getOrder()).getKey(), (Category) adapter.getItem(item.getOrder()));
        }else{
            deleteCategory(adapter.getRef(item.getOrder()).getKey(), (Category) adapter.getItem(item.getOrder()));
            Toast.makeText(this, "Category is deleted!", Toast.LENGTH_SHORT).show();
        }
        return super.onContextItemSelected(item);
    }

    private void deleteCategory(String key, Category item) {
        DatabaseReference foods=database.getReference("Food");
        Query foodInCate=foods.orderByChild("menuId").equalTo(key);
        foodInCate.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                    postSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        reference.child(key).removeValue();
        Toast.makeText(this, "Catrgory deleted!", Toast.LENGTH_SHORT).show();
    }

    private void showDialogUpdate(final String key, final Category item) {
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);
        alertDialog.setTitle("Update Category ");
        alertDialog.setMessage("Add the category new titlte & select the new image");

        LayoutInflater inflater=this.getLayoutInflater();
        View add_menu_layout=inflater.inflate(R.layout.add_new_category,null);

        categoryName=add_menu_layout.findViewById(R.id.cat_name);
        selectImage=add_menu_layout.findViewById(R.id.btnSelect);
        upload=add_menu_layout.findViewById(R.id.btnUpload);

        categoryName.setText(item.getName());
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeImage(item);
            }
        });
        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                item.setName(categoryName.getText().toString());
                reference.child(key).setValue(item);
                Toast.makeText(home.this, "Category is updated!", Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    private void changeImage(final Category item) {

        if(saveUri!=null){
            final ProgressDialog dialog=new ProgressDialog(this);
            dialog.setMessage("Uploading...");
            dialog.show();
            String Imagename= UUID.randomUUID().toString();
            final StorageReference imageFolder=storageReference.child("images/"+selectImage);
            imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    dialog.dismiss();
                    Toast.makeText(home.this,"Category is added!",Toast.LENGTH_SHORT).show();
                    imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                           item.setImage(uri.toString());
                        }
                    });
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(home.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            int progress=(int)Math.round(100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            dialog.setMessage("Uploaded: "+progress+"%");
                        }
                    });
        }
    }
}
