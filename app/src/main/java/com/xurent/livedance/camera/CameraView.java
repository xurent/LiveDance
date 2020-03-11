package com.xurent.livedance.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;
import com.xurent.livedance.egl.EGLSurfaceView;

public class CameraView extends EGLSurfaceView {

    public static final int FRONT=Camera.CameraInfo.CAMERA_FACING_FRONT;
    public static final int BACK=Camera.CameraInfo.CAMERA_FACING_BACK;
    private CameraRender cameraRender;
    private XRCamera camera;
    private int textrueId=-1;
    SurfaceTexture surface;
    private Context context;
    private int cameraId = FRONT;

    public void setWaterMark(String text){
        cameraRender.setWaterMark(text);
    }

    public CameraView(Context context) {
        this(context, null);this.context=context;
    }

    public CameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);this.context=context;
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context=context;
        cameraRender = new CameraRender(context);
        camera = new XRCamera(context);
        setRender(cameraRender);
        previewAngle(context);
        cameraRender.setOnSurfaceCreateListener(new CameraRender.OnSurfaceCreateListener() {
            @Override
            public void onSurfaceCreate(SurfaceTexture surfaceTexture,int tid) {
                surface=surfaceTexture;
                camera.initCamera(surfaceTexture, cameraId);
                textrueId=tid;
            }
        });
    }




    public void setCameraId(int cameraId) {
        this.cameraId = cameraId;
        camera.changeCamera(cameraId);
        previewAngle(context);
    }

    public void onDestory()
    {
        Log.d("camera","相机被销毁");
        if(camera != null)
        {
            camera.stopPreview();
        }
    }


    public void previewAngle(Context context) {
        int angle = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        cameraRender.resetMatrix();
        switch (angle) {
            case Surface.ROTATION_0:
                Log.d("xurent", "0");
                if (cameraId == android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK) {
                    cameraRender.setAngle(90, 0, 0, 1);
                    cameraRender.setAngle(180, 1, 0, 0);
                } else {
                    cameraRender.setAngle(90f, 0f, 0f, 1f);
                }

                break;
            case Surface.ROTATION_90:
                Log.d("xurent", "90");
                if (cameraId == android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK) {
                    cameraRender.setAngle(180, 0, 0, 1);
                    cameraRender.setAngle(180, 0, 1, 0);
                } else {
                    cameraRender.setAngle(90f, 0f, 0f, 1f);
                }
                break;
            case Surface.ROTATION_180:
                Log.d("xurent", "180");
                if (cameraId == android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK) {
                    cameraRender.setAngle(90f, 0.0f, 0f, 1f);
                    cameraRender.setAngle(180f, 0.0f, 1f, 0f);
                } else {
                    cameraRender.setAngle(-90, 0f, 0f, 1f);
                }
                break;
            case Surface.ROTATION_270:
                Log.d("xurent", "270");
                if (cameraId == android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK) {
                    cameraRender.setAngle(180f, 0.0f, 1f, 0f);
                } else {
                    cameraRender.setAngle(0f, 0f, 0f, 1f);
                }
                break;
        }
    }

    public int getTextrueId(){

        return textrueId;
    }


    public void stopPreView(){
        camera.pausePreview();
    }

    public void startPreView(){
        camera.startPreview();
    }

}
