package com.minassoftware.morkosmedicalsuppliesserverside;

import android.app.AlertDialog;
import android.app.VoiceInteractor;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.minassoftware.morkosmedicalsuppliesserverside.Interface.ItemClickListener;
import com.minassoftware.morkosmedicalsuppliesserverside.Model.Common;
import com.minassoftware.morkosmedicalsuppliesserverside.Model.MyResponse;
import com.minassoftware.morkosmedicalsuppliesserverside.Model.Notification;
import com.minassoftware.morkosmedicalsuppliesserverside.Model.Request;
import com.minassoftware.morkosmedicalsuppliesserverside.Model.Sender;
import com.minassoftware.morkosmedicalsuppliesserverside.Model.Token;
import com.minassoftware.morkosmedicalsuppliesserverside.Remote.APIService;
import com.minassoftware.morkosmedicalsuppliesserverside.ViewHolder.OrderViewHolder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderStatus extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseRecyclerAdapter<Request,OrderViewHolder> adapter;

    MaterialSpinner spinner;
    APIService mService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        database=FirebaseDatabase.getInstance();
        reference=database.getReference("Requests");

        mService=Common.getFCMClient();

        recyclerView=(RecyclerView)findViewById(R.id.rv_orders);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        loadOrders();

    }

    private void loadOrders() {

        adapter=new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                reference
        ) {
            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, final Request model, final int position) {

                viewHolder.orderID.setText(adapter.getRef(position).getKey());
                viewHolder.orderSatus.setText(Common.convertCode(model.getStatus()));
                viewHolder.orderPhone.setText(model.getPhone());
                viewHolder.orderAddress.setText(model.getAddress());

                //Button Edit status
                viewHolder.btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showUpdateDialog(adapter.getRef(position).getKey(),adapter.getItem(position));
                    }
                });

                //Button Delete
                viewHolder.btnRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteOrder(adapter.getRef(position).getKey());
                    }
                });


                //Button Order Details
                viewHolder.btnDetails.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent orderDetails=new Intent(OrderStatus.this,OrderDetails.class);
                        Common.CurrentRequest=model;
                        orderDetails.putExtra("OrderId",adapter.getRef(position).getKey());
                        startActivity(orderDetails);
                    }
                });


                //Button Get Order Directions
                viewHolder.btnDirections.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent maps=new Intent(OrderStatus.this,TrackingOrder.class);
                        Common.CurrentRequest=model;
                        startActivity(maps);
                    }
                });

            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    private void deleteOrder(String key) {
        reference.child(key).removeValue();
        adapter.notifyDataSetChanged();
    }

    private void showUpdateDialog(String key, final Request item) {

        final AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);
        alertDialog.setTitle("Update Order");
        alertDialog.setMessage("Choose the status of order");

        LayoutInflater inflater=this.getLayoutInflater();
        final View view=inflater.inflate(R.layout.update_order_layout,null);

        spinner=(MaterialSpinner)view.findViewById(R.id.spinner);
        spinner.setItems("Submitted","Out For Delivery","Delivered");

        alertDialog.setView(view);

        final String localKey=key;
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                item.setStatus(String.valueOf(spinner.getSelectedIndex()));
                reference.child(localKey).setValue(item);
                adapter.notifyDataSetChanged();//to update
                sendOrderStatusToUser(localKey,item);

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

    private void sendOrderStatusToUser(final String key,Request item) {
        DatabaseReference tokens=database.getReference("Tokens");
        tokens.orderByKey().equalTo(item.getPhone())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                            Token token=postSnapshot.getValue(Token.class);
                            Notification notification=new Notification("Markos Medical Supplies","Your Order "+key+" was updated");
                            Sender content=new Sender(token.getToken(),notification);

                            mService.sendNotification(content)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if(response.body().success==1)
                                            {
                                                Toast.makeText(OrderStatus.this,"Order was updated !",Toast.LENGTH_LONG).show();
                                            }
                                            else
                                            {
                                                Toast.makeText(OrderStatus.this,"Order was updated but Failed to send a notification!",Toast.LENGTH_LONG).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<MyResponse> call, Throwable throwable) {
                                            Log.e("ERROR",throwable.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }
}
