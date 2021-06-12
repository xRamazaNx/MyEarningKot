package ru.developer.press.myearningkot.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import ru.developer.press.myearningkot.R;

import static android.content.ContentValues.TAG;

public class SendHelper {

    public static Bitmap getBitmapFromView(View view) {
        if (view.getVisibility() != View.VISIBLE) {
            view = getNextView(view);
            Log.d(TAG, "New view id: " + view.getId());
        }
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap;
        if (view instanceof ScrollView) {
            returnedBitmap = Bitmap.createBitmap(((ViewGroup) view).getChildAt(0).getWidth(), ((ViewGroup) view).getChildAt(0).getHeight(), Bitmap.Config.ARGB_8888);
        } else if (view instanceof RecyclerView) {
            view.measure(
                    View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        } else {
            returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        }

        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

    /**
     * If the base view is not visible, then it has no width or height.
     * This causes a problem when we are creating a PDF based on its size.
     * This method gets the next visible View.
     *
     * @param view The invisible view
     * @return The next visible view after the given View, or the original view if there's no more
     * visible views.
     */
    private static View getNextView(View view) {
        if (view.getParent() != null && (view.getParent() instanceof ViewGroup)) {
            ViewGroup group = (ViewGroup) view.getParent();
            View child;
            boolean getNext = false;
            //Iterate through all views from parent
            for (int i = 0; i < group.getChildCount(); i++) {
                child = group.getChildAt(i);
                if (getNext) {
                    //Make sure the view is visible, else iterate again until we find a visible view
                    if (child.getVisibility() == View.VISIBLE) {
                        Log.d(TAG, String.format(Locale.ENGLISH, "CHILD: %s : %s", child.getClass().getSimpleName(), child.getId()));
                        view = child;
                    }
                }
                //Iterate until we find out current view,
                // then we want to get the NEXT view
                if (child.getId() == view.getId()) {
                    getNext = true;
                }
            }
        }
        return view;
    }

    public static Bitmap getScreenshotFromRecyclerView(RecyclerView view) {
        RecyclerView.Adapter adapter = view.getAdapter();
        Bitmap bigBitmap = null;
        if (adapter != null) {
            int size = adapter.getItemCount();
            int height = 0;
            Paint paint = new Paint();
            int iHeight = 0;
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

            // Use 1/8th of the available memory for this memory cache.
            final int cacheSize = maxMemory / 8;
            LruCache<String, Bitmap> bitmaCache = new LruCache<>(cacheSize);
            for (int i = 0; i < size; i++) {
                RecyclerView.ViewHolder holder = adapter.createViewHolder(view, adapter.getItemViewType(i));
                adapter.onBindViewHolder(holder, i);
                holder.itemView.measure(View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                holder.itemView.layout(0, 0, holder.itemView.getMeasuredWidth(), holder.itemView.getMeasuredHeight());
                holder.itemView.setDrawingCacheEnabled(true);
                holder.itemView.buildDrawingCache();
                Bitmap drawingCache = holder.itemView.getDrawingCache();
                if (drawingCache != null) {

                    bitmaCache.put(String.valueOf(i), drawingCache);
                }
//                holder.itemView.setDrawingCacheEnabled(false);
//                holder.itemView.destroyDrawingCache();
                height += holder.itemView.getMeasuredHeight();
            }

            bigBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), height, Bitmap.Config.ARGB_8888);
            Canvas bigCanvas = new Canvas(bigBitmap);
            bigCanvas.drawColor(Color.WHITE);

            for (int i = 0; i < size; i++) {
                Bitmap bitmap = bitmaCache.get(String.valueOf(i));
                bigCanvas.drawBitmap(bitmap, 0f, iHeight, paint);
                iHeight += bitmap.getHeight();
                bitmap.recycle();
            }

        }
        return bigBitmap;
    }

    public static Bitmap resizeBitmapFitXY(int width, int height, Bitmap bitmap) {
        Bitmap background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        float originalWidth = bitmap.getWidth(), originalHeight = bitmap.getHeight();
        Canvas canvas = new Canvas(background);
        float scale, xTranslation = 0.0f, yTranslation = 0.0f;
        if (originalWidth > originalHeight) {
            scale = height / originalHeight;
            xTranslation = (width - originalWidth * scale) / 2.0f;
        } else {
            scale = width / originalWidth;
            yTranslation = (height - originalHeight * scale) / 2.0f;
        }
        Matrix transformation = new Matrix();
        transformation.postTranslate(xTranslation, yTranslation);
        transformation.preScale(scale, scale);
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        canvas.drawBitmap(bitmap, transformation, paint);
        return background;
    }

    public static Bitmap getNameCardBitmap(int width, int height, String nameCard, Context context) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(ContextCompat.getColor(context, R.color.colorAccent));
//        Bitmap ic = BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon_gold_monet);
//        int size = StaticUtil.convertDpToPixels(24, context);
        int textSize = 18;
//        ic = resizeBitmapFitXY(size, size, ic);
        int padding = 8;
        int paddingNameTop = height / 2 + textSize / 2;
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        paint.setColor(Color.WHITE);
//        canvas.drawBitmap(ic, padding, height / 2 - size / 2, new Paint());
        canvas.drawText(nameCard, padding * 2, paddingNameTop, paint);

        return bitmap;
    }
}
