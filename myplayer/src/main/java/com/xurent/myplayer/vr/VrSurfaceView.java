package com.xurent.myplayer.vr;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.xurent.myplayer.listener.MyLaoutCallBackListener;
import com.xurent.myplayer.opengl.GlSurfaceView;


public class VrSurfaceView extends GLSurfaceView implements GestureDetector.OnGestureListener, View.OnTouchListener{

    private VRGlassGLVideoRenderer vrGlassGLVideoRenderer;
    private Context context;
    private boolean listen=true;

    public void setListen(boolean listen) {
        this.listen = listen;
    }

    public VrSurfaceView(Context context) {
        this(context, null);
        this.context=context;
        //setGestureListener();
        //设置Touch监听
        this.setOnTouchListener(this);
        //允许长按
        this.setLongClickable(true);
    }

    public VrSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(3);
        vrGlassGLVideoRenderer = new VRGlassGLVideoRenderer(context);
        setRenderer(vrGlassGLVideoRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        vrGlassGLVideoRenderer.setOnRenderListener(new VRGlassGLVideoRenderer.OnRenderListener() {
            @Override
            public void onRender() {
                requestRender();
            }
        });
        this.context=context;
        //setGestureListener();
        //设置Touch监听
        this.setOnTouchListener(this);
        //允许长按
        this.setLongClickable(true);
    }

    public void setYUVData(int width, int height, byte[] y, byte[] u, byte[] v) {
        if (vrGlassGLVideoRenderer != null) {
            //vrGlassGLVideoRenderer.setYUVRenderData(width, height, y, u, v);
            Log.d("------VRVRVRVRVRV","YUV数据传输");
            requestRender();
        }
    }

    public VRGlassGLVideoRenderer getVrGlassGLVideoRenderer() {
        return vrGlassGLVideoRenderer;
    }


    public void changeInteRactionMode() {
        vrGlassGLVideoRenderer.changeInteractionMode();
    }

    public void changeDisplayMode() {
        vrGlassGLVideoRenderer.changeDisplayMode();
    }


    private static final int FLING_MIN_DISTANCE = 20;// 移动最小距离
    private static final int FLING_MIN_VELOCITY = 200;// 移动最大速度

    private MyLaoutCallBackListener myLayoutCallBack;
    //构建手势探测器
    GestureDetector mygesture = new GestureDetector(context,this);
    public void setMyLayoutCallBack(MyLaoutCallBackListener myLayoutCallBack) {
        this.myLayoutCallBack = myLayoutCallBack;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        Log.d("pingan", "onDown");
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // TODO Auto-generated method stub
        // e1：第1个ACTION_DOWN MotionEvent
        // e2：最后一个ACTION_MOVE MotionEvent
        // velocityX：X轴上的移动速度（像素/秒）
        // velocityY：Y轴上的移动速度（像素/秒）

        // X轴的坐标位移大于FLING_MIN_DISTANCE，且移动速度大于FLING_MIN_VELOCITY个像素/秒
        //向
        if(listen){
            if (e1.getY() - e2.getY() > FLING_MIN_DISTANCE){
//                     && Math.abs(velocityX) > FLING_MIN_VELOCITY) {

                myLayoutCallBack.onDown();
            }
            //向上
            if (e2.getY() - e1.getY() > FLING_MIN_DISTANCE
                    && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
                myLayoutCallBack.onUp();
            }
        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mygesture.onTouchEvent(event);
    }



}
