package net.sourcewalker.vfrmap;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;

public class PlaneOverlay extends Overlay implements Runnable {

    private final Bitmap bitmap;
    private final MapView mapView;
    private final int centerX;
    private final int centerY;
    private final ScheduledExecutorService resetScheduler;
    private ScheduledFuture<?> future;
    private IGeoPoint planeLocation;
    private float azimuth;
    private boolean snapToLocation;

    public IGeoPoint getPlaneLocation() {
        return planeLocation;
    }

    public void setPlaneLocation(IGeoPoint planeLocation) {
        this.planeLocation = planeLocation;
        mapView.postInvalidate();
    }

    public float getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(float azimuth) {
        this.azimuth = azimuth;
        mapView.postInvalidate();
    }

    public void setSnapToLocation(boolean snapToLocation) {
        this.snapToLocation = snapToLocation;
        mapView.postInvalidate();
    }

    public PlaneOverlay(Context ctx, MapView mapView) {
        super(ctx);
        this.mapView = mapView;

        final BitmapDrawable drawable = (BitmapDrawable) ctx.getResources()
                .getDrawable(R.drawable.ic_aircraft);
        this.bitmap = drawable.getBitmap();
        this.centerX = bitmap.getWidth() / 2;
        this.centerY = bitmap.getHeight() / 2;
        this.resetScheduler = Executors.newSingleThreadScheduledExecutor();
        this.planeLocation = null;
        this.azimuth = 0;
        this.snapToLocation = true;
    }

    /*
     * (non-Javadoc)
     * @see org.osmdroid.views.overlay.Overlay#draw(android.graphics.Canvas,
     * org.osmdroid.views.MapView, boolean)
     */
    @Override
    protected void draw(Canvas c, MapView osmv, boolean shadow) {
        if (shadow) {
            return;
        }

        if (planeLocation != null) {
            Projection projection = osmv.getProjection();
            Point planePoint = projection.toMapPixels(planeLocation, null);

            Matrix bitmapMatrix = new Matrix();
            bitmapMatrix.setRotate(azimuth, centerX, centerY);
            bitmapMatrix.postTranslate(planePoint.x - centerX, planePoint.y
                    - centerY);
            c.drawBitmap(bitmap, bitmapMatrix, null);

            if (snapToLocation) {
                osmv.getController().setCenter(planeLocation);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * org.osmdroid.views.overlay.Overlay#onTouchEvent(android.view.MotionEvent,
     * org.osmdroid.views.MapView)
     */
    @Override
    public boolean onTouchEvent(MotionEvent event, MapView mapView) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            setSnapToLocation(false);
            startResetTimer();
        }
        return super.onTouchEvent(event, mapView);
    }

    private void startResetTimer() {
        synchronized (resetScheduler) {
            if (future != null) {
                future.cancel(false);
            }
            future = resetScheduler.schedule(this, 2000, TimeUnit.MILLISECONDS);
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        synchronized (resetScheduler) {
            setSnapToLocation(true);
            future = null;
        }
    }

}
