package com.minassoftware.morkosmedicalsuppliesserverside;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.minassoftware.morkosmedicalsuppliesserverside.Model.Common;
import com.minassoftware.morkosmedicalsuppliesserverside.ViewHolder.OrderDetailsAdapter;

public class OrderDetails extends AppCompatActivity {

    TextView order_id,order_phone,order_address,order_total,order_comment;
    String order_id_value="";
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        order_id = (TextView) findViewById(R.id.order_id);
        order_phone = (TextView) findViewById(R.id.order_phone);
        order_address = (TextView) findViewById(R.id.order_address);
        order_total = (TextView) findViewById(R.id.order_total);
        order_comment = (TextView) findViewById(R.id.order_comment);

        recyclerView = (RecyclerView) findViewById(R.id.rv_details);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        if (getIntent() != null) {
            order_id_value = getIntent().getStringExtra("OrderId");

        }

        order_id.setText(order_id_value);
        order_phone.setText(Common.CurrentRequest.getPhone());
        order_total.setText(Common.CurrentRequest.getTotal());
        order_address.setText(Common.CurrentRequest.getAddress());
        order_comment.setText(Common.CurrentRequest.getComment());

        OrderDetailsAdapter adapter = new OrderDetailsAdapter(Common.CurrentRequest.getFood());
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }
}
