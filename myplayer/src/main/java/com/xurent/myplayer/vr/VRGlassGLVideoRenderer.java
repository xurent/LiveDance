package com.xurent.myplayer.vr;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.view.Surface;


import com.xurent.myplayer.R;
import com.xurent.myplayer.opengl.ShaderUtils;
import com.xurent.myplayer.util.ResReadUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;



public class VRGlassGLVideoRenderer implements GLSurfaceView.Renderer,SurfaceTexture.OnFrameAvailableListener{
    public static final int DISPLAY_NORMAL_MODE = 1;
    public static final int DISPLAY_GLASS_MODE = 2;

    public static final int RENDER_YUV = 1;
    public static final int RENDER_MEDIACODEC = 2;
    private int renderType = RENDER_MEDIACODEC;
    private int samplerOES_mediacodec;
    private Context context;
    //顶点，纹理本地引用
    private FloatBuffer vertexBuffer, mTexVertexBuffer;
    //程序id
    private int mProgram;
    //向量个数
    private int vCount;

    //顶点向量元素个数（x,y,z）
    private static final int COORDS_PER_VERTEX = 3;
    //纹理向量元素个数（s,t）
    private static final int COORDS_PER_TEXTURE = 2;

    //纹理id
    private int textureId;
    //纹理
    private SurfaceTexture surfaceTexture;

    //返回属性变量的位置
    //顶点
    private int aPositionLocation;
    //纹理
    private int aTextureLocation;
    //投影矩阵
    private int projectMatrixLocation;
    //旋转矩阵
    private int rotateMatrixLocation;
    //相机矩阵
    private int viewMatrixLocation;
    //模型矩阵
    private int modelMatrixLocation;

    //相机矩阵
    private final float[] mViewMatrix = new float[16];
    //投影矩阵
    private final float[] mProjectMatrix = new float[16];
    //旋转矩阵
    private float[] mRotateMatrix = new float[16];
    private float[] mTempRotateMatrix = new float[16];
    //模型矩阵
    private final float[] mModelMatrix = new float[16];


    //展示模式：分为 全景；VR眼睛
    private int displayMode = DISPLAY_NORMAL_MODE;

    //交互模式：是否响应传感器
    private boolean interactionModeNormal = true;

    public void changeInteractionMode(){
        interactionModeNormal = !interactionModeNormal;
    }

    public void changeDisplayMode(){
        if (displayMode==DISPLAY_NORMAL_MODE){
            displayMode = DISPLAY_GLASS_MODE;
        }else if (displayMode==DISPLAY_GLASS_MODE){
            displayMode = DISPLAY_NORMAL_MODE;
        }
    }

    public void setRenderType(int renderType) {
        this.renderType = renderType;
    }

    public void setuRotateMatrix(float[] rotateMatrix) {
        mTempRotateMatrix = rotateMatrix;
        if (interactionModeNormal){
            System.arraycopy(mTempRotateMatrix, 0, mRotateMatrix, 0, 16);
        }
    }

    public VRGlassGLVideoRenderer(Context context) {

        this.context=context;
        calculateAttribute();

        initMediaPlayer();
    }

    //计算顶点位置和纹理位置数据
    private void calculateAttribute(){
        float radius=2f;
        final double angleSpan = Math.PI/90f;// 将球进行单位切分的角度

        ArrayList<Float> alVertix = new ArrayList<>();
        ArrayList<Float> textureVertix = new ArrayList<>();
        for (double vAngle = 0; vAngle < Math.PI; vAngle = vAngle + angleSpan){

            for (double hAngle = 0; hAngle < 2*Math.PI; hAngle = hAngle + angleSpan){
                float x0 = (float) (radius* Math.sin(vAngle) * Math.cos(hAngle));
                float y0 = (float) (radius* Math.sin(vAngle) * Math.sin(hAngle));
                float z0 = (float) (radius * Math.cos((vAngle)));

                float x1 = (float) (radius* Math.sin(vAngle) * Math.cos(hAngle + angleSpan));
                float y1 = (float) (radius* Math.sin(vAngle) * Math.sin(hAngle + angleSpan));
                float z1 = (float) (radius * Math.cos(vAngle));

                float x2 = (float) (radius* Math.sin(vAngle + angleSpan) * Math.cos(hAngle + angleSpan));
                float y2 = (float) (radius* Math.sin(vAngle + angleSpan) * Math.sin(hAngle + angleSpan));
                float z2 = (float) (radius * Math.cos(vAngle + angleSpan));

                float x3 = (float) (radius* Math.sin(vAngle + angleSpan) * Math.cos(hAngle));
                float y3 = (float) (radius* Math.sin(vAngle + angleSpan) * Math.sin(hAngle));
                float z3 = (float) (radius * Math.cos(vAngle + angleSpan));

                alVertix.add(x1);
                alVertix.add(y1);
                alVertix.add(z1);
                alVertix.add(x0);
                alVertix.add(y0);
                alVertix.add(z0);
                alVertix.add(x3);
                alVertix.add(y3);
                alVertix.add(z3);

                float s0 = (float) (hAngle / Math.PI/2);
                float s1 = (float) ((hAngle + angleSpan)/Math.PI/2);
                float t0 = (float) (vAngle / Math.PI);
                float t1 = (float) ((vAngle + angleSpan) / Math.PI);

                textureVertix.add(s1);// x1 y1对应纹理坐标
                textureVertix.add(t0);
                textureVertix.add(s0);// x0 y0对应纹理坐标
                textureVertix.add(t0);
                textureVertix.add(s0);// x3 y3对应纹理坐标
                textureVertix.add(t1);

                alVertix.add(x1);
                alVertix.add(y1);
                alVertix.add(z1);
                alVertix.add(x3);
                alVertix.add(y3);
                alVertix.add(z3);
                alVertix.add(x2);
                alVertix.add(y2);
                alVertix.add(z2);

                textureVertix.add(s1);// x1 y1对应纹理坐标
                textureVertix.add(t0);
                textureVertix.add(s0);// x3 y3对应纹理坐标
                textureVertix.add(t1);
                textureVertix.add(s1);// x2 y3对应纹理坐标
                textureVertix.add(t1);
            }
        }

        vCount = alVertix.size() / 3;
        vertexBuffer = convertToFloatBuffer(alVertix);
        mTexVertexBuffer = convertToFloatBuffer(textureVertix);
    }

    //动态数组转FloatBuffer
    private FloatBuffer convertToFloatBuffer(ArrayList<Float> data){
        float[] d=new float[data.size()];
        for (int i=0;i<d.length;i++){
            d[i]=data.get(i);
        }

        ByteBuffer buffer=ByteBuffer.allocateDirect(data.size()*4);
        buffer.order(ByteOrder.nativeOrder());
        FloatBuffer ret=buffer.asFloatBuffer();
        ret.put(d);
        ret.position(0);
        return ret;
    }

    //初始化IjkMediaPlayer
    private void initMediaPlayer() {


    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        //将背景设置为黑色
        GLES30.glClearColor(0.0f,0.0f,0.0f,1.0f);

        //初始化OpenGl程序
        initGLProgram();
        //获取各个属性位置
        initLocation();
        //初始化纹理id
        initTextureId();
        //播放器和纹理绑定
        attachTexture();
        //播放
        preparePlay();
    }

    private void initGLProgram() {
        //编译
        final int vertexShaderId = ShaderUtils.compileVertexShader(ResReadUtils.readResource(context,R.raw.vertex_vr_shader));
        final int fragmentShaderId = ShaderUtils.compileFragmentShader(ResReadUtils.readResource(context, R.raw.fragment_vr_shader));
        //链接程序片段
        mProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId);
        //在OpenGLES环境中使用程序
        GLES30.glUseProgram(mProgram);
    }

    private void preparePlay() {

    }

    //播放器和纹理绑定
    private void attachTexture() {

        surfaceTexture = new SurfaceTexture(textureId);
        surfaceTexture.setOnFrameAvailableListener(this);//监听是否有新的一帧数据到来
        Surface surface = new Surface(surfaceTexture);
        if (onSurfaceCreateListener != null) {
            onSurfaceCreateListener.OnSurfaceCreate(surface);
        }
    }

    //初始化纹理id
    private void initTextureId() {
        int[] textures = new int[1];

        GLES30.glGenTextures(1, textures, 0);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        textureId = textures[0];
    }

    private void initLocation() {
        aPositionLocation = GLES30.glGetAttribLocation(mProgram, "aPosition");
        aTextureLocation = GLES30.glGetAttribLocation(mProgram, "aTexCoord");

        samplerOES_mediacodec=GLES30.glGetAttribLocation(mProgram, "sTexture");

        projectMatrixLocation = GLES20.glGetUniformLocation(mProgram,"uProjMatrix");
        rotateMatrixLocation = GLES20.glGetUniformLocation(mProgram,"uRotateMatrix");
        viewMatrixLocation = GLES20.glGetUniformLocation(mProgram,"uViewMatrix");
        modelMatrixLocation = GLES20.glGetUniformLocation(mProgram,"uModelMatrix");
    }

    private int screenWidth;
    private int screenHeight;
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        screenWidth = width;
        screenHeight = height;

        //模型矩阵
        Matrix.setIdentityM(mModelMatrix,0);
        //旋转矩阵
        Matrix.setIdentityM(mRotateMatrix,0);
    }

    public void setSize(int width,int height){
        //计算宽高比
        float ratio=(float)width/height;
        //透视投影矩阵/视锥
        Matrix.perspectiveM(mProjectMatrix,0,100,ratio,0f,300f);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0.0f,0.0f, 0.0f, 0.0f,-1.0f, 0.0f,1.0f, 0.0f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT | GLES30.GL_COLOR_BUFFER_BIT);

        //更新画面
        surfaceTexture.updateTexImage();

        channgeDrawState();
    }

    private void channgeDrawState() {
        switch (displayMode){
            case DISPLAY_NORMAL_MODE:
                drawNormal();
                break;
            case DISPLAY_GLASS_MODE:
                drawGlass();
                break;
        }
    }

    //普通全景模式
    private void drawNormal() {
        draw();
    }

    private void draw() {
        //视口为全屏
        GLES30.glViewport(0, 0, screenWidth, screenHeight);
        setSize(screenWidth,screenHeight);

        //将变换矩阵传入顶点渲染器
        GLES20.glUniformMatrix4fv(projectMatrixLocation,1,false,mProjectMatrix,0);
        GLES20.glUniformMatrix4fv(rotateMatrixLocation,1,false,mRotateMatrix,0);
        GLES20.glUniformMatrix4fv(viewMatrixLocation,1,false,mViewMatrix,0);
        GLES20.glUniformMatrix4fv(modelMatrixLocation,1,false,mModelMatrix,0);

        //激活纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

        //启用顶点坐标属性
        GLES30.glEnableVertexAttribArray(aPositionLocation);
        GLES30.glVertexAttribPointer(aPositionLocation, COORDS_PER_VERTEX, GLES30.GL_FLOAT, false, 0, vertexBuffer);

        //启用纹理坐标属性
        GLES30.glEnableVertexAttribArray(aTextureLocation);
        GLES30.glVertexAttribPointer(aTextureLocation, COORDS_PER_TEXTURE, GLES30.GL_FLOAT, false, 0, mTexVertexBuffer);

        // 绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);

        //禁止顶点数组的句柄
        GLES30.glDisableVertexAttribArray(aPositionLocation);
        GLES30.glDisableVertexAttribArray(aTextureLocation);

        GLES20.glUniform1i(samplerOES_mediacodec, 0);
    }

    //VR眼睛模式
    private void drawGlass() {
        draw0();

        draw1();
    }

    //左眼
    private void draw0() {
        GLES30.glViewport(0, 0, screenWidth/2, screenHeight);
        setSize(screenWidth/2,screenHeight);

        //将变换矩阵传入顶点渲染器
        GLES20.glUniformMatrix4fv(projectMatrixLocation,1,false,mProjectMatrix,0);
        GLES20.glUniformMatrix4fv(rotateMatrixLocation,1,false,mRotateMatrix,0);
        GLES20.glUniformMatrix4fv(viewMatrixLocation,1,false,mViewMatrix,0);
        GLES20.glUniformMatrix4fv(modelMatrixLocation,1,false,mModelMatrix,0);

        //激活纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

        //启用顶点坐标属性
        GLES30.glEnableVertexAttribArray(aPositionLocation);
        GLES30.glVertexAttribPointer(aPositionLocation, COORDS_PER_VERTEX, GLES30.GL_FLOAT, false, 0, vertexBuffer);

        //启用纹理坐标属性
        GLES30.glEnableVertexAttribArray(aTextureLocation);
        GLES30.glVertexAttribPointer(aTextureLocation, COORDS_PER_TEXTURE, GLES30.GL_FLOAT, false, 0, mTexVertexBuffer);

        // 绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);

        //禁止顶点数组的句柄
        GLES30.glDisableVertexAttribArray(aPositionLocation);
        GLES30.glDisableVertexAttribArray(aTextureLocation);

        GLES20.glUniform1i(samplerOES_mediacodec, 0);
    }

    //右眼
    private void draw1() {
        GLES30.glViewport(screenWidth/2, 0, screenWidth/2, screenHeight);
        setSize(screenWidth/2,screenHeight);

        //将变换矩阵传入顶点渲染器
        GLES20.glUniformMatrix4fv(projectMatrixLocation,1,false,mProjectMatrix,0);
        GLES20.glUniformMatrix4fv(rotateMatrixLocation,1,false,mRotateMatrix,0);
        GLES20.glUniformMatrix4fv(viewMatrixLocation,1,false,mViewMatrix,0);
        GLES20.glUniformMatrix4fv(modelMatrixLocation,1,false,mModelMatrix,0);

        //激活纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

        //启用顶点坐标属性
        GLES30.glEnableVertexAttribArray(aPositionLocation);
        GLES30.glVertexAttribPointer(aPositionLocation, COORDS_PER_VERTEX, GLES30.GL_FLOAT, false, 0, vertexBuffer);

        //启用纹理坐标属性
        GLES30.glEnableVertexAttribArray(aTextureLocation);
        GLES30.glVertexAttribPointer(aTextureLocation, COORDS_PER_TEXTURE, GLES30.GL_FLOAT, false, 0, mTexVertexBuffer);

        // 绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);

        //禁止顶点数组的句柄
        GLES30.glDisableVertexAttribArray(aPositionLocation);
        GLES30.glDisableVertexAttribArray(aTextureLocation);

        GLES20.glUniform1i(samplerOES_mediacodec, 0);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        //Mlog.e("==onFrameAvailable");
        //请求渲染
        if (onRenderListener != null) {
            onRenderListener.onRender();
        }
    }

    private OnRenderListener onRenderListener;

    private OnSurfaceCreateListener onSurfaceCreateListener;

    public void setContext(Context context) {
        this.context = context;
    }

    public void setOnRenderListener(OnRenderListener onRenderListener) {
        this.onRenderListener = onRenderListener;
    }

    public void setOnSurfaceCreateListener(OnSurfaceCreateListener onSurfaceCreateListener) {
        this.onSurfaceCreateListener = onSurfaceCreateListener;
    }

    public interface OnSurfaceCreateListener {

        void OnSurfaceCreate(Surface surface);

    }

    public interface OnRenderListener {

        void onRender();

    }

}
