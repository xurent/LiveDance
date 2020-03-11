package com.xurent.livedance.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.xurent.livedance.R;
import com.xurent.livedance.egl.EGLSurfaceView;
import com.xurent.livedance.egl.ShaderUtil;
import com.xurent.myplayer.util.DisplayUtil;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class CameraRender implements EGLSurfaceView.GLRender, SurfaceTexture.OnFrameAvailableListener {



    private Context context;

    private float[] vertexData = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f,

            0f,0f,
            0f,0f,
            0f,0f,
            0f,0f

    };
    private FloatBuffer vertexBuffer;

    private float[] fragmentData = {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
    };
    private FloatBuffer fragmentBuffer;

    private int program;
    private int vPosition;
    private int fPosition;
    private int vboId;
    private int fboId;

    private int fboTextureid;
    private int cameraTextureid;

    private int umatrix;
    private float[] matrix = new float[16];

    private SurfaceTexture surfaceTexture;
    private OnSurfaceCreateListener onSurfaceCreateListener;

    private CameraFboRender wlCameraFboRender;

    private int screenWidth;
    private int screenHeight;

    private int width;
    private int height;

    public void setWaterMark(String mark){
        wlCameraFboRender.setWatermark(mark);
    }

    public CameraRender(Context context) {
        this.context = context;

        screenWidth = DisplayUtil.getScreenWidth(context);
        screenHeight = DisplayUtil.getScreenHeight(context);

        wlCameraFboRender = new CameraFboRender(context);
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);

        fragmentBuffer = ByteBuffer.allocateDirect(fragmentData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(fragmentData);
        fragmentBuffer.position(0);
    }

    public void setOnSurfaceCreateListener(OnSurfaceCreateListener onSurfaceCreateListener) {
        this.onSurfaceCreateListener = onSurfaceCreateListener;
    }

    @Override
    public void onSurfaceCreated() {

        wlCameraFboRender.onCreate();
        String vertexSource = ShaderUtil.getRawResource(context, R.raw.vertex_shader1);
        String fragmentSource = ShaderUtil.getRawResource(context, R.raw.fragment_shader1);

        program = ShaderUtil.createProgram(vertexSource, fragmentSource);
        vPosition = GLES20.glGetAttribLocation(program, "v_Position");
        fPosition = GLES20.glGetAttribLocation(program, "f_Position");
        umatrix = GLES20.glGetUniformLocation(program, "u_Matrix");

        //vbo
        int [] vbos = new int[1];
        GLES20.glGenBuffers(1, vbos, 0);
        vboId = vbos[0];

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4 + fragmentData.length * 4, null, GLES20. GL_STATIC_DRAW);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.length * 4, vertexBuffer);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4, fragmentData.length * 4, fragmentBuffer);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        //fbo
        int[] fbos = new int[1];
        GLES20.glGenBuffers(1, fbos, 0);
        fboId = fbos[0];
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);

        int []textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        fboTextureid = textureIds[0];

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTextureid);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, screenWidth, screenHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, fboTextureid, 0);
        if(GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE)
        {
            Log.e("xurent", "fbo wrong");
        }
        else
        {
            Log.e("xurent", "fbo success");
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);


        int []textureidseos = new int[1];
        GLES20.glGenTextures(1, textureidseos, 0);
        cameraTextureid = textureidseos[0];

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTextureid);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        surfaceTexture = new SurfaceTexture(cameraTextureid);
        surfaceTexture.setOnFrameAvailableListener(this);

        if(onSurfaceCreateListener != null)
        {
            onSurfaceCreateListener.onSurfaceCreate(surfaceTexture,fboTextureid);
        }
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
    }


    public void resetMatrix()
    {
        Matrix.setIdentityM(matrix, 0);
    }

    public void setAngle(float angle, float x, float y, float z)
    {
        Matrix.rotateM(matrix, 0, angle, x, y, z);
    }


    @Override
    public void onSurfaceChanged(int width, int height) {
//        wlCameraFboRender.onChange(width, height);
//        GLES20.glViewport(0, 0, width, height);
        this.width = width;
        this.height = height;
    }

    @Override
    public void onDrawFrame() {

        surfaceTexture.updateTexImage();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1f,0f, 0f, 1f);

        GLES20.glUseProgram(program);

        GLES20.glViewport(0, 0, screenWidth, screenHeight);
        GLES20.glUniformMatrix4fv(umatrix, 1, false, matrix, 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);

        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8,
                0);

        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8,
                vertexData.length * 4);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        wlCameraFboRender.onChange(width, height);
        wlCameraFboRender.onDraw(fboTextureid);



    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }


    public int getFboTextureid() {
        return fboTextureid;
    }

    public interface OnSurfaceCreateListener
    {
        void onSurfaceCreate(SurfaceTexture surfaceTexture, int textrueId);
    }
}
