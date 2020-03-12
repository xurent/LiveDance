package com.xurent.myplayer.player;


import com.xurent.myplayer.opengl.GlSurfaceView;


import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import com.xurent.myplayer.TimeInfoBean;
import com.xurent.myplayer.listener.WlOnCompleteListener;
import com.xurent.myplayer.listener.WlOnErrorListener;
import com.xurent.myplayer.listener.WlOnParparedListener;
import com.xurent.myplayer.listener.WlOnPauseResumeListener;
import com.xurent.myplayer.listener.WlOnloadListener;
import com.xurent.myplayer.listener.WlOntimeInfoListener;
import com.xurent.myplayer.log.MyLog;
import com.xurent.myplayer.opengl.WlRender;
import com.xurent.myplayer.util.WlVideoSupportUitl;
import com.xurent.myplayer.vr.VRGlassGLVideoRenderer;
import com.xurent.myplayer.vr.VrSurfaceView;

import java.nio.ByteBuffer;

public class WLPlayer {

    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("avcodec-57");
        System.loadLibrary("avdevice-57");
        System.loadLibrary("avfilter-6");
        System.loadLibrary("avformat-57");
        System.loadLibrary("avutil-55");
        System.loadLibrary("postproc-54");
        System.loadLibrary("swresample-2");
        System.loadLibrary("swscale-4");
    }

    private static String source;
    private static boolean playNext = false;

    private WlOnParparedListener wlOnParparedListener;
    private WlOnloadListener wlOnloadListener;
    private WlOnPauseResumeListener wlOnPauseResumeListener;
    private WlOntimeInfoListener wlOntimeInfoListener;
    private WlOnErrorListener wlOnErrorListener;
    private WlOnCompleteListener wlOnCompleteListener;
    private static TimeInfoBean timeInfoBean;
    private int duration = 0;
    private GlSurfaceView glSurfaceView;
    private VrSurfaceView vrSurfaceView;
    private MediaFormat mediaFormat;
    private MediaCodec mediaCodec;
    private Surface surface;
    private MediaCodec.BufferInfo info;
    private boolean isOpenVr=false;
    private boolean ok=true;
    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public boolean isOk() {
        return ok;
    }

    public WLPlayer() {
    }

    public void setOpenVr(boolean openVr) {
        isOpenVr = openVr;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setWlOnErrorListener(WlOnErrorListener wlOnErrorListener) {
        this.wlOnErrorListener = wlOnErrorListener;
    }

    public void setWlOnCompleteListener(WlOnCompleteListener wlOnCompleteListener) {
        this.wlOnCompleteListener = wlOnCompleteListener;
    }

    public void setGlSurfaceView(GlSurfaceView glSurfaceView) {
        this.glSurfaceView = glSurfaceView;

        glSurfaceView.getWlRender().setOnSurfaceCreateListener(new WlRender.OnSurfaceCreateListener() {
            @Override
            public void onSurfaceCreate(Surface s) {
                if(surface == null)
                {
                    surface = s;
                    MyLog.d("onSurfaceCreate");
                }
            }
        });
        MyLog.d("硬解码设置");

    }

    public void setVrSurfaceView(VrSurfaceView vrSurfaceView) {
        this.vrSurfaceView = vrSurfaceView;
        isOpenVr=true;
        vrSurfaceView.getVrGlassGLVideoRenderer().setOnSurfaceCreateListener(new VRGlassGLVideoRenderer.OnSurfaceCreateListener() {
            @Override
            public void OnSurfaceCreate(Surface s) {
                if (surface == null) {
                    surface = s;
                    MyLog.d("VR -onSurfaceCreate");
                }
            }
        });
        MyLog.d("VR硬解码设置");
    }

    public void setWlOnParparedListener(WlOnParparedListener wlOnParparedListener) {
        this.wlOnParparedListener = wlOnParparedListener;
    }

    public void setWlOnloadListener(WlOnloadListener wlOnloadListener) {
        this.wlOnloadListener = wlOnloadListener;
    }

    public void setWlOnPauseResumeListener(WlOnPauseResumeListener wlOnPauseResumeListener) {
        this.wlOnPauseResumeListener = wlOnPauseResumeListener;
    }

    public void setWlOntimeInfoListener(WlOntimeInfoListener wlOntimeInfoListener) {
        this.wlOntimeInfoListener = wlOntimeInfoListener;
    }

    public int getDuration() {
        return duration;
    }

    public void onCallParpared() {
        if (wlOnParparedListener != null) {

            wlOnParparedListener.onParpared();
        } else {
            System.out.println("回调对象为空");
        }
    }

    public void onCallLoad(boolean load) {

        if (wlOnloadListener != null) {
            wlOnloadListener.onload(load);
        }

    }

    public void onCallTimeInfo(int currentTime, int totalTime) {

        if (wlOntimeInfoListener != null) {

            if (timeInfoBean == null) {
                timeInfoBean = new TimeInfoBean();

            }
            duration = totalTime;
            timeInfoBean.setCurrentTime(currentTime);
            timeInfoBean.setTotalTime(totalTime);
            wlOntimeInfoListener.onTimeInfo(timeInfoBean);
        }
    }

    public void parpared() {

        if (TextUtils.isEmpty(source)) {
            MyLog.d("source not be empty");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                if(ok){
                    n_parpared(source);
                }
                MyLog.d("播放开始");
            }
        }).start();
    }

    public void start() {
        if (TextUtils.isEmpty(source)) {
            MyLog.d("source not be empty");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(ok){
                    n_start();
                }

            }
        }).start();
    }

    public void pause() {
        n_pause();
        if (wlOnPauseResumeListener != null) {
            wlOnPauseResumeListener.onPause(true);
        }

    }

    public void resume() {
        n_resume();
        if (wlOnPauseResumeListener != null) {
            wlOnPauseResumeListener.onPause(false);
        }
    }

    public void stop() {
        timeInfoBean = null;
        duration = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                n_stop();
                releaseMediacodec();
            }
        }).start();

    }

    public void onCallError(int code, String msg) {
        stop();
        if (wlOnErrorListener != null) {
            wlOnErrorListener.onError(code, msg);
        }
    }

    public void seek(int sedc) {
        n_seek(sedc);
    }

    public void onCallComplete() {

        stop();
        if (wlOnCompleteListener != null) {
            wlOnCompleteListener.onComplete();
        }
    }

    public void playNext(String url) {
        source = url;
        playNext = true;
        stop();
    }

    public void onCallNext() {

        if (playNext) {
            playNext = false;
            parpared();
        }
    }

    public void onCallRenderYUV(int width, int height, byte[] y, byte[] u, byte[] v) {

        MyLog.d("获取到YUV数据");

        //vr
        if(isOpenVr){
            if (vrSurfaceView != null) {
                vrSurfaceView .getVrGlassGLVideoRenderer().setRenderType(WlRender.RENDER_YUV);
                vrSurfaceView .setYUVData(width, height, y, u, v);
                Log.d("------VRVRVRVRVRV","FFmpeg回调YUV数据传输");
            }

        }else {
        //普通
            if (glSurfaceView != null) {
                glSurfaceView.getWlRender().setRenderType(WlRender.RENDER_YUV);
                glSurfaceView.setYUVData(width, height, y, u, v);
            }
        }

    }

    public boolean onCallIsSupportMediaCodec(String ffcodecname) {
        return WlVideoSupportUitl.isSupportCodec(ffcodecname);
    }


    /**
     * 初始化MediaCodec
     *
     * @param codecName
     * @param width
     * @param height
     * @param csd_0
     * @param csd_1
     */
    public void initMediaCodec(String codecName, int width, int height, byte[] csd_0, byte[] csd_1) {
        if (surface != null) {
            try {
                if(glSurfaceView!=null){
                    glSurfaceView.getWlRender().setRenderType(WlRender.RENDER_MEDIACODEC);
                }
                //vrSurfaceView.getVrGlassGLVideoRenderer().setRenderType(VRGlassGLVideoRenderer.RENDER_MEDIACODEC);
                Log.d("------VRVRVRVRVRV","FFmpeg回调硬解码数据传输");
                String mime = WlVideoSupportUitl.findVideoCodecName(codecName);
                mediaFormat = MediaFormat.createVideoFormat(mime, width, height);
                mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, width * height);
                mediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(csd_0));
                mediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(csd_1));
                MyLog.d(mediaFormat.toString());
                mediaCodec = MediaCodec.createDecoderByType(mime);

                info = new MediaCodec.BufferInfo();
                mediaCodec.configure(mediaFormat, surface, null, 0);
                mediaCodec.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (wlOnErrorListener != null) {
                wlOnErrorListener.onError(2001, "surface is null");
            }
        }
    }


    public void decodeAVPacket(int datasize, byte[] data) {
       // Log.d("decodeAVPacket","获取到硬解码数据");
        try {
            if (surface != null && datasize > 0 && data != null && mediaCodec != null) {
                int intputBufferIndex = mediaCodec.dequeueInputBuffer(10);
                if (intputBufferIndex >= 0) {
                    ByteBuffer byteBuffer = mediaCodec.getInputBuffers()[intputBufferIndex];
                    byteBuffer.clear();
                    byteBuffer.put(data);
                    mediaCodec.queueInputBuffer(intputBufferIndex, 0, datasize, 0, 0);
                }
                int outputBufferIndex = mediaCodec.dequeueOutputBuffer(info, 10);
                while (outputBufferIndex >= 0) {
                    mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
                    outputBufferIndex = mediaCodec.dequeueOutputBuffer(info, 10);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void releaseMediacodec() {
        if (mediaCodec != null) {

            try {
                mediaCodec.flush();
                mediaCodec.stop();
                mediaCodec.release();

            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaCodec = null;
            mediaFormat = null;
            info = null;
        }
    }


    private native void n_parpared(String source);

    private native void n_start();

    private native void n_pause();

    private native void n_resume();

    private native void n_stop();

    private native void n_seek(int sedc);


    public void setGlSurfaceView(Surface surface) {
        this.surface=surface;
    }
}
