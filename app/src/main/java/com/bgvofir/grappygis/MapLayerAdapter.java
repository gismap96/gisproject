package com.bgvofir.grappygis;

import android.content.Context;
import android.media.Image;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.layers.LegendInfo;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MapLayerAdapter extends RecyclerView.Adapter<MapLayerAdapter.MapLayerViewHolder> {

    private List<Layer> mLayers ;
    private final Context mContext;
    private final String TAG = MapLayerAdapter.class.getSimpleName();

    public MapLayerAdapter(final Context context){
        mContext = context;
    }

    /**
     * Set the data for this adapter
     * @param layers - List
     */
    public void setLayerList(final List layers){

        mLayers = layers;

    }
    /**
     *This method calls onCreateViewHolder(ViewGroup, int) to create a new RecyclerView.ViewHolder
     * and initializes some private fields to be used by RecyclerView.
     * @param viewGroup - ViewGroup
     * @param i - int
     * @return MapLayerViewHolder
     */
    @Override public MapLayerViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int i) {
        final View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.map_layer_view, viewGroup, false);

        return new MapLayerViewHolder(itemView);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * @param holder RecycleViewHolder
     * @param position - int
     */
    @Override public void onBindViewHolder(final MapLayerViewHolder holder, final int position) {
        final Layer layer = mLayers.get(position);
        holder.layerName.setText(layer.getName());
        Log.d(TAG, layer.getName());
        final boolean layerVisible = (layer.isVisible());
        holder.checkBox.setChecked(layerVisible);




        holder.checkBox.setTag(mLayers.get(position));
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Layer checkedLayer = (Layer)holder.checkBox.getTag();

                checkedLayer.setVisible(holder.checkBox.isChecked());

                mLayers.get(position).setVisible(holder.checkBox.isChecked());

            }
        });
        /*holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                if (layer.isVisible()){
                    layer.setVisible(false);
                }else{
                    layer.setVisible(true);
                }
            }
        });*/
        final MapLegendAdapter legendAdapter = new MapLegendAdapter(mContext);
        holder.legendItems.setLayoutManager(new LinearLayoutManager(mContext));
        holder.legendItems.setAdapter(legendAdapter);
        // Retrieve any legend info
        if (layer instanceof FeatureLayer) {
            final ListenableFuture<List<LegendInfo>> legendInfoFuture = layer.fetchLegendInfosAsync();
            legendInfoFuture.addDoneListener(new Runnable() {
                @Override public void run() {
                    try {
                        final List<LegendInfo> legendList = legendInfoFuture.get();
                        legendAdapter.setLegendInfo(legendList);
                        legendAdapter.notifyDataSetChanged();
                    } catch (InterruptedException | ExecutionException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            });

        }
        if (layer.getName().equals("Feature Collection")){
            holder.layerName.setText("דקירות שלי");
            holder.iconForClientPointIV.setVisibility(View.VISIBLE);

        }

        if (layer.getName().contains("$$##")){
            String finalLayerName = layer.getName().replace("$$##", "");
            holder.layerName.setText(finalLayerName);
        }
    }
    /**
     * Returns the total number of items in the data set held by the adapter.
     * @return int
     */
    @Override public int getItemCount() {
        return mLayers == null ? 0 : mLayers.size();
    }

    public class MapLayerViewHolder extends RecyclerView.ViewHolder{

        public final TextView layerName;
        public final CheckBox checkBox;
        public final RecyclerView legendItems;
        public ImageView iconForClientPointIV;

        public MapLayerViewHolder(final View view){
            super(view);
            checkBox = (CheckBox) view.findViewById(R.id.cbLayer) ;
            layerName = (TextView) view.findViewById(R.id.txtLayerName);
            legendItems = (RecyclerView) view.findViewById(R.id.legendRecylerView);
            iconForClientPointIV = view.findViewById(R.id.iconForClientPointIV);
        }


    }
}
