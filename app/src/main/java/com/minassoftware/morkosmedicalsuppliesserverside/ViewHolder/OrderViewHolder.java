package com.minassoftware.morkosmedicalsuppliesserverside.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.minassoftware.morkosmedicalsuppliesserverside.Interface.ItemClickListener;
import com.minassoftware.morkosmedicalsuppliesserverside.R;

/**
 * Created by Mina on 6/8/2018.
 */


public class OrderViewHolder extends RecyclerView.ViewHolder {
    public TextView orderID,orderSatus,orderPhone,orderAddress;
    public Button btnEdit,btnRemove,btnDetails,btnDirections;



    public OrderViewHolder(View itemView) {
        super(itemView);

        orderID=(TextView)itemView.findViewById(R.id.order_id);
        orderSatus=(TextView)itemView.findViewById(R.id.order_status);
        orderAddress=(TextView)itemView.findViewById(R.id.order_address);
        orderPhone=(TextView)itemView.findViewById(R.id.order_phone);
        //Buttons
        btnDetails=(Button) itemView.findViewById(R.id.btnDetails);
        btnDirections=(Button) itemView.findViewById(R.id.btnDirection);
        btnEdit=(Button) itemView.findViewById(R.id.btnEdit);
        btnRemove=(Button) itemView.findViewById(R.id.btnRemove);

    }



}
