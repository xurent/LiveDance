package com.xurent.livedance;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.xurent.livedance.LiveRoom.InteractiveFrag;
import com.xurent.livedance.LiveRoom.LayerFrag;
import com.xurent.livedance.LiveRoom.LiveFrag;
import com.xurent.livedance.LiveRoom.MessageAdapter;
import com.xurent.livedance.LiveRoom.utils.CircleImageView;
import com.xurent.livedance.LiveRoom.utils.DisplayUtil;
import com.xurent.livedance.LiveRoom.utils.MagicTextView;
import com.xurent.livedance.LiveRoom.utils.SoftKeyBoardListener;
import com.xurent.livedance.common.Constants;
import com.xurent.livedance.http.RoomService;
import com.xurent.livedance.model.Fans;
import com.xurent.livedance.model.Gift;
import com.xurent.livedance.model.LiveMsg;
import com.xurent.livedance.model.LiveRoomBean;
import com.xurent.livedance.model.MessageData;
import com.xurent.livedance.ui.adapter.FansAdapter;
import com.xurent.livedance.ui.adapter.GiftAdapter;
import com.xurent.livedance.utils.OKhttpUtils;
import com.xurent.livedance.websoket.WsManager;
import com.xurent.myplayer.listener.WlOnCompleteListener;
import com.xurent.myplayer.listener.WlOnErrorListener;
import com.xurent.myplayer.listener.WlOnParparedListener;
import com.xurent.myplayer.listener.WlOnPauseResumeListener;
import com.xurent.myplayer.listener.WlOnloadListener;
import com.xurent.myplayer.log.MyLog;
import com.xurent.myplayer.opengl.GlSurfaceView;
import com.xurent.myplayer.player.WLPlayer;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LiveRoomActivity extends AppCompatActivity {

    public LiveRoomBean data;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liveroom);
        data = (LiveRoomBean) getIntent().getSerializableExtra("anchor");

        //加载直播fragment
        LiveFrag liveFrag = new LiveFrag();
        getSupportFragmentManager().beginTransaction().add(R.id.fl_root, liveFrag).commit();

        // 加载
        InteractiveFrag frag= new InteractiveFrag(liveFrag);
        frag.show(getSupportFragmentManager(), "InteractiveFrag");


    }




}