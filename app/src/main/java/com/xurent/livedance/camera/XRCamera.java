package com.xurent.livedance.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;

import com.xurent.myplayer.util.DisplayUtil;

import java.io.IOException;
import java.util.List;

public class XRCamera {

    private android.hardware.Camera camera;



    private SurfaceTexture surfaceTexture;

    private int width;
    private int height;

    public XRCamera(Context context){

        this.width = DisplayUtil.getScreenWidth(context);
        this.height = DisplayUtil.getScreenHeight(context);

    }

    public void initCamera(SurfaceTexture surfaceTexture, int cameraId)
    {
        this.surfaceTexture = surfaceTexture;
        setCameraParm(cameraId);

    }

    private void setCameraParm(int cameraId)
    {
        try {
            camera = android.hardware.Camera.open(cameraId);
            camera.setPreviewTexture(surfaceTexture);
            android.hardware.Camera.Parameters parameters = camera.getParameters();

            parameters.setFlashMode("off");
            parameters.setPreviewFormat(ImageFormat.NV21);

            android.hardware.Camera.Size size = getFitSize(parameters.getSupportedPictureSizes());
            parameters.setPictureSize(size.width, size.height);

            size = getFitSize(parameters.getSupportedPreviewSizes());
            parameters.setPreviewSize(size.width, size.height);

            camera.setParameters(parameters);
            camera.startPreview();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startPreview()
    {
        if(camera != null)
        {
            camera.startPreview();
        }
    }

    public void stopPreview()
    {
        if(camera != null)
        {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public void pausePreview()
    {
        if(camera != null)
        {
            camera.stopPreview();
        }
    }

    public void changeCamera(int cameraId)
    {
        if(camera != null)
        {
            stopPreview();
        }
        setCameraParm(cameraId);
    }

    private android.hardware.Camera.Size getFitSize(List<android.hardware.Camera.Size> sizes)
    {
        if(width < height)
        {
            int t = height;
            height = width;
            width = t;
        }

        for(android.hardware.Camera.Size size : sizes)
        {
            if(1.0f * size.width / size.height == 1.0f * width / height)
            {
                return size;
            }
        }
        return sizes.get(0);
    }

}
