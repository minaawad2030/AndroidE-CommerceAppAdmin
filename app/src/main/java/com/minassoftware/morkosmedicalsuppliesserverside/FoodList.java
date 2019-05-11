package com.minassoftware.morkosmedicalsuppliesserverside;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.minassoftware.morkosmedicalsuppliesserverside.Interface.ItemClickListener;
import com.minassoftware.morkosmedicalsuppliesserverside.Model.Category;
import com.minassoftware.morkosmedicalsuppliesserverside.Model.Common;
import com.minassoftware.morkosmedicalsuppliesserverside.Model.Food;
import com.minassoftware.morkosmedicalsuppliesserverside.ViewHolder.FoodViewHolder;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class FoodList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FloatingActionButton fab;
    RelativeLayout root;

    FirebaseDatabase database;
    DatabaseReference databaseReference;
    FirebaseStorage storage;
    StorageReference storageReference;
    String categoryId="";
    FirebaseRecyclerAdapter<Food,FoodViewHolder> adapter;

    //add new food
    MaterialEditText etName,etDes,etPrice,etDiscount;
    Button uploadImage,selectImage;
    Food newFood;
    private final int PICK_IMAGE_REQUEST=71;
    Uri saveUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);
        //Recyvler View
        recyclerView=(RecyclerView)findViewById(R.id.food_recylcler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        root=(RelativeLayout)findViewById(R.id.root);
        //fab
        fab=(FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddFoodDialog();
            }
        });

        //database
        database=FirebaseDatabase.getInstance();
        databaseReference=database.getReference("Food");
        storage=FirebaseStorage.getInstance();
        storageReference=storage.getReference();


        if(getIntent()!=null){
            categoryId=getIntent().getStringExtra("CategoryId");
        }
        if(!categoryId.isEmpty()){
          LoadFoodList(categoryId);
        }


    }

    private void showAddFoodDialog() {

        AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);
        alertDialog.setTitle("Add New Food ");
        alertDialog.setMessage("Please fill these data");

        LayoutInflater inflater=this.getLayoutInflater();
        View add_food_layout=inflater.inflate(R.layout.add_new_food,null);

        etName=add_food_layout.findViewById(R.id.new_food_name);
        etDes=add_food_layout.findViewById(R.id.new_food_description);
        etDiscount=add_food_layout.findViewById(R.id.new_food_discount);
        etPrice=add_food_layout.findViewById(R.id.new_food_price);

        selectImage=add_food_layout.findViewById(R.id.btnSelectFood);
        uploadImage=add_food_layout.findViewById(R.id.btnUploadFood);

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImageToFirebase();
            }
        });
        alertDialog.setView(add_food_layout);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                if(newFood!=null){
                    databaseReference.push().setValue(newFood);
                    Snackbar.make(root,"New Food "+newFood.getName()+" is added!",Snackbar.LENGTH_SHORT).show();
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

    private void LoadFoodList(String categoryId) {
        adapter=new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
                Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                databaseReference.orderByChild("menuId").equalTo(categoryId)
        ) {
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {

                viewHolder.tv_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.imageView);
                if (!model.getDiscount().equals("0")) {
                    Picasso.with(getBaseContext()).load(R.drawable.sale).into(viewHolder.saleIcon);
                }else{
                    Picasso.with(getBaseContext()).load(R.drawable.empty).into(viewHolder.saleIcon);
                }

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int postion, boolean isLongClick) {
                        //later
                    }
                });

            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    private void chooseImage() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST);
    }

    private void uploadImageToFirebase() {
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
                   // Toast.makeText(FoodList.this,"Category is added!",Toast.LENGTH_SHORT).show();
                    imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            newFood=new Food();
                            newFood.setName(etName.getText().toString());
                            newFood.setDescription(etDes.getText().toString());
                            newFood.setDiscount(etDiscount.getText().toString());
                            newFood.setPrice(etPrice.getText().toString());
                            newFood.setMenuId(categoryId);
                            newFood.setImage(uri.toString());
                        }
                    });
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(FoodList.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            int progress=(int)Math.round(100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            dialog.setMessage("Uploaded: "+progress+"%");
                        }
                    });
        }else{
            Toast.makeText(this, "Please Select an Image! ", Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.UPDATE)){
            showUpdateFoodDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }else if(item.getTitle().equals(Common.DELETE)){
            deleteFood(adapter.getRef(item.getOrder()).getKey());
            Toast.makeText(this, "Item is deleted!", Toast.LENGTH_SHORT).show();
        }
        return super.onContextItemSelected(item);
    }

    private void deleteFood(String key) {

        databaseReference.child(key).removeValue();
    }

    private void showUpdateFoodDialog(final String key, final Food item) {
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);
        alertDialog.setTitle("Update Food ");
        alertDialog.setMessage("Please fill these data");

        LayoutInflater inflater=this.getLayoutInflater();
        View add_food_layout=inflater.inflate(R.layout.add_new_food,null);

        etName=add_food_layout.findViewById(R.id.new_food_name);
        etDes=add_food_layout.findViewById(R.id.new_food_description);
        etDiscount=add_food_layout.findViewById(R.id.new_food_discount);
        etPrice=add_food_layout.findViewById(R.id.new_food_price);

        //assign values
        etName.setText(item.getName());
        etPrice.setText(item.getPrice());
        etDes.setText(item.getDescription());
        etDiscount.setText(item.getDiscount());


        selectImage=add_food_layout.findViewById(R.id.btnSelectFood);
        uploadImage=add_food_layout.findViewById(R.id.btnUploadFood);

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeImage(item);
            }
        });
        alertDialog.setView(add_food_layout);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();


                    item.setName(etName.getText().toString());
                    item.setPrice(etPrice.getText().toString());
                    item.setDescription(etDes.getText().toString());
                    item.setDiscount(etDiscount.getText().toString());
                    databaseReference.child(key).setValue(item);
                    Snackbar.make(root," Food "+item.getName()+" is edited!",Snackbar.LENGTH_SHORT).show();

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

    private void changeImage(final Food item) {

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
                    Toast.makeText(FoodList.this,"Item was edited!",Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(FoodList.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
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
