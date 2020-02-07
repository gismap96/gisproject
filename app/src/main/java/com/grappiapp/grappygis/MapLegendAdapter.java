package com.grappiapp.grappygis;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.layers.LegendInfo;
import com.esri.arcgisruntime.symbology.Symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MapLegendAdapter extends RecyclerView.Adapter<MapLegendAdapter.MapLegendViewHodler> {

  private List<LegendInfo> mLegendInfoList = new ArrayList<>();
  private final Context mContext;
  private final String TAG = MapLegendAdapter.class.getSimpleName();

  public MapLegendAdapter(final Context context){
    mContext = context;

  }

  /**
   * Set the data for this adapter
   * @param legendInfo List<LegendInfo></LegendInfo>
   */
  public void setLegendInfo(final List<LegendInfo> legendInfo){
    mLegendInfoList = legendInfo;
  }

  /**
   *This method calls onCreateViewHolder(ViewGroup, int) to create a new RecyclerView.ViewHolder
   * and initializes some private fields to be used by RecyclerView.
   * @param parent - ViewGroup
   * @param viewType - int
   * @return MapLayerViewHolder
   */
  @Override public MapLegendViewHodler onCreateViewHolder(final ViewGroup parent, final int viewType) {
    final View itemView = LayoutInflater.
        from(parent.getContext()).
        inflate(R.layout.legend_view, parent, false);

    return new MapLegendViewHodler(itemView);
  }

  /**
   * Called by RecyclerView to display the data at the specified position.
   * @param holder RecycleViewHolder
   * @param position - int
   */
  @Override public void onBindViewHolder(final MapLegendViewHodler holder, final int position) {
    final LegendInfo legendInfo = mLegendInfoList.get(position);
    holder.legendName.setText(legendInfo.getName().trim());
    final Symbol symbol = legendInfo.getSymbol();

    final TypedValue a = new TypedValue();
    final int color;

    // Match the background color of the bitmap to the background of the theme.
    mContext.getTheme().resolveAttribute(android.R.attr.colorBackground, a, true);
    color =
        a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT ? a.data : Color.WHITE;

    final ListenableFuture<Bitmap> future = symbol.createSwatchAsync(mContext, color);
    future.addDoneListener(new Runnable() {
      @Override public void run() {
        try {
          final Bitmap bitmap = future.get();
          holder.legendSymbol.setImageBitmap(bitmap);
          Bitmap emptyBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        } catch (ExecutionException | InterruptedException e) {
          Log.e(TAG, e.getMessage());
        }
      }
    });

  }
  /**
   * Returns the total number of items in the data set held by the adapter.
   * @return int
   */
  @Override public int getItemCount() {
    return mLegendInfoList.size();
  }

  public class MapLegendViewHodler extends RecyclerView.ViewHolder{

    final ImageView legendSymbol;
    final TextView legendName;

    public MapLegendViewHodler(final View itemView) {
      super(itemView);
      legendSymbol = (ImageView) itemView.findViewById(R.id.imgSymbol);
      legendName = (TextView) itemView.findViewById(R.id.txtLegend);
    }
  }
}