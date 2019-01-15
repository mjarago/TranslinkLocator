package com.example.arago.translinklocator.adapters;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.arago.translinklocator.R;
import com.example.arago.translinklocator.models.Bus;

import java.util.ArrayList;

public class BusRecyclerAdapter extends RecyclerView.Adapter<BusRecyclerAdapter.ViewHolder> {

    private final String TAG = "BusRecyclerAdapter";
    private ArrayList<Bus> busArrayList = new ArrayList<>();
    public static int selectedItem = -1;
    private static int busStopID;

    private FragmentInterface mInterface;

    public interface  FragmentInterface{
        void respond(int busStopNo, String destination);
    }

    public BusRecyclerAdapter(ArrayList<Bus> buses, FragmentInterface fragmentInterface) {
        this.busArrayList = buses;
        mInterface = fragmentInterface;
        Log.d(TAG,"busRecyclerAdapter");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate items using an external layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bus_list_item,parent,false);
        final ViewHolder holder = new ViewHolder(view);
        Log.d(TAG,"onCreateView");
        return holder;

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        //extract data from array of objects
        Bus[] bus = new Bus[busArrayList.size()];
        bus[position] = busArrayList.get(position);
        String name = bus[position].getBusRouteNo() + " " + bus[position].getBusDestination();
        holder.busName.setText(name);

        //Click listener for rows/holder
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedItem = position;
                notifyDataSetChanged();
                Log.d(TAG,"LinearLayout Clicked + Position: " + position);

            }
        });
        //if item = selected position changed background
        //used so that it changed the color back to white when other item is clicked
        if(selectedItem == position){

            holder.linearLayout.setBackgroundColor(Color.parseColor("#d1fff4"));
            //What the interface would respond back to BusListFragment
            mInterface.respond(bus[position].getBusRouteNo(),bus[position].getBusDestination());

        }
        //back to white
        else{
            holder.linearLayout.setBackgroundColor(Color.parseColor("#ffffff"));
        }


        Log.d(TAG,"Size: " +busArrayList.size());

    }



    @Override
    public int getItemCount() {
        return busArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView busName;
        LinearLayout linearLayout;

        public ViewHolder(final View itemView) {

            super(itemView);
            busName = itemView.findViewById(R.id.busName);
            linearLayout = (LinearLayout)itemView.findViewById(R.id.linearLayout);
        }

        @Override
        public void onClick(View view) {

        }
    }
    }

