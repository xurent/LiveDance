package com.xurent.livedance.egl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

public class ShaderUtil {

    public static String getRawResource(Context context, int rawId)
    {
        InputStream inputStream = context.getResources().openRawResource(rawId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuffer sb = new StringBuffer();
        String line;
        try
        {
            while((line = reader.readLine()) != null)
            {
                sb.append(line).append("\n");
            }
            reader.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private static int loadShader(int shaderType, String source)
    {
        int shader = GLES20.glCreateShader(shaderType);
        if(shader != 0)
        {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);

            int[] compile = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compile, 0);
            if(compile[0] != GLES20.GL_TRUE)
            {
                Log.d("ywl5320", "shader compile error");
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
            return shader;
        }
        else
        {
            return 0;
        }
    }

    public static int createProgram(String vertexSource, String fragmentSoruce)
    {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSoruce);

        if(vertexShader != 0 && fragmentShader != 0)
        {
            int program = GLES20.glCreateProgram();

            GLES20.glAttachShader(program, vertexShader);
            GLES20.glAttachShader(program, fragmentShader);

            GLES20.glLinkProgram(program);
            return program;
        }
        return 0;
    }



    public static Bitmap createTextImage(String text,int textSize,String textColor,String bgColor,int padding){

        Paint paint=new Paint();
        paint.setColor(Color.parseColor(textColor));
        paint.setTextSize(textSize);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        float width=paint.measureText(text,0,text.length());

        float top=paint.getFontMetrics().top;
        float bottom=paint.getFontMetrics().bottom;

        Bitmap bm=Bitmap.createBitmap((int) (width+padding*2),(int) ((bottom-top)+padding*2),Bitmap.Config.ARGB_8888);

        Canvas canvas=new Canvas(bm);
        canvas.drawColor(Color.parseColor(bgColor));
        canvas.drawText(text,padding,-top+padding,paint);

        return  bm;
    }

    public static  int loadBitmapTexture(Bitmap bitmap){

        int[] textrueIds=new int[1];
        GLES20.glGenTextures(1,textrueIds,0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textrueIds[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);

        ByteBuffer bitmapBuffer=ByteBuffer.allocate(bitmap.getHeight()*bitmap.getWidth()*4);
        bitmap.copyPixelsToBuffer(bitmapBuffer);
        bitmapBuffer.flip();
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_RGBA,bitmap.getWidth(),bitmap.getHeight(),
                0,GLES20.GL_RGBA,GLES20.GL_UNSIGNED_BYTE,ByteBuffer.wrap(bitmapBuffer.array()));

        return textrueIds[0];
    }

    public static int loadTexrute(int src, Context context)
    {
        int []textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), src);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
        bitmap = null;
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return  textureIds[0];

    }


}
