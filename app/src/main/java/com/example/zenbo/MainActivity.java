package com.example.zenbo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import androidx.core.app.ActivityCompat;

import com.asus.robotframework.API.ExpressionConfig;
import com.google.ai.client.generativeai.BuildConfig;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import com.asus.robotframework.API.RobotAPI;
import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.asus.robotframework.API.RobotUtil;
import com.asus.robotframework.API.SpeakConfig;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends RobotActivity {

    public static TextView tvResult;
    public static Button btnStart;

    private static CountDownTimer mCountDownTimer;
    static String clearData = "";
    static String inputStr = "";
    static int wordCount = 0;


    static String apiKey = "AIzaSyB8izLGxCxBsbaNfRq2_9cNDLNdxyAB96I";
    static GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", apiKey);
    static GenerativeModelFutures model = GenerativeModelFutures.from(gm);

    private static RobotAPI mRobotAPI = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, 100);
        tvResult = findViewById(R.id.textView1);
        btnStart = findViewById(R.id.button1);
        tvResult.setText("開始");
        btnStart.setText("錄音");


        btnStart.setOnClickListener(v -> {
            tvResult.setText("錄音");
            robotAPI.robot.stopSpeak();
            robotAPI.robot.speakAndListen("start", new SpeakConfig().timeout(20));
        });

        //設定定時彈跳視窗
        mCountDownTimer = new CountDownTimer(estimateSpeechTime(wordCount), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // not need
            }

            @Override
            public void onFinish() {
                robotAPI.robot.setExpression(RobotFace.HIDEFACE);
            }
        };


    }




    private static void sendMessage(String userMessageText, GenerativeModelFutures model) {

        //多輪對話寫法
        //Create previous chat rule for context
        Content.Builder userContentBuilder = new Content.Builder();
        userContentBuilder.setRole("user");
        userContentBuilder.addText("How much does the bag cost?");
        Content userContent = userContentBuilder.build();

        Content.Builder modelContentBuilder = new Content.Builder();
        modelContentBuilder.setRole("model");
        modelContentBuilder.addText("You are an English conversation chatbot, responsible for providing scenarios for conversations, such as at customs, asking for directions, or making a payment. You will only respond in English.");
        Content modelContent = modelContentBuilder.build();

        List<Content> history = Arrays.asList(userContent, modelContent);
        // Initialize the chat
        ChatFutures chat = model.startChat(history);

        //Create a new user message
        Content.Builder userMessageBuilder = new Content.Builder();
        userMessageBuilder.setRole("user");
        userMessageBuilder.addText(userMessageText);
        Content userMessage = userMessageBuilder.build();


        //注意
        Executor executor = Executors.newSingleThreadExecutor();

        ListenableFuture<GenerateContentResponse> response = chat.sendMessage(userMessage);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                inputStr = resultText;
                setRobotExpression(RobotFace.HAPPY, inputStr);
                mCountDownTimer.start();
                tvResult.setText(inputStr);

            }

            @Override
            public void onFailure(Throwable t) {
                tvResult.setText("Error: AI generation failed");
                t.printStackTrace();
            }
        }, executor);
    }

    // 預估說話時間
    public int estimateSpeechTime(int wordCount) {
        double averageWordsPerSecond = 6;
        int estimatedSeconds = (int) Math.ceil((double) wordCount* (double) averageWordsPerSecond);
        int bufferTime = 3;
        return (estimatedSeconds + bufferTime) * 1000;
    }

    public static String extractData(String input) {
        // 正则表达式匹配模式
        String regex = "\\\\\"result\\\\\":\\s*\\[\\\\\"(.*?)\\\\\"\\]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        // 查找并返回匹配的内容
        if (matcher.find()) {
            return matcher.group(1); // 提取捕获组中的内容
        }

        return null; // 如果没有找到
    }


    // 申請權限
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static RobotCallback robotCallback = new RobotCallback() {
        @Override
        public void onResult(int cmd, int serial, RobotErrorCode err_code, Bundle result) {
            super.onResult(cmd, serial, err_code, result);
        }

        @Override
        public void onStateChange(int cmd, int serial, RobotErrorCode err_code, RobotCmdState state) {
            super.onStateChange(cmd, serial, err_code, state);
        }

        @Override
        public void initComplete() {
            super.initComplete();
        }
    };


    public static RobotCallback.Listen robotListenCallback = new RobotCallback.Listen() {
        @Override
        public void onFinishRegister() {
        }

        @Override
        public void onVoiceDetect(JSONObject jsonObject) {

        }

        @Override
        public void onSpeakComplete(String s, String s1) {

        }

        @Override
        public void onEventUserUtterance(JSONObject jsonObject) {
            clearData = extractData(jsonObject.toString());
            if (clearData != null) {
                tvResult.setText(clearData);
                inputStr = clearData;
                wordCount = inputStr.split("\\s+").length;
                sendMessage(clearData, model);
            } else {
                tvResult.setText("No Data");
            }

        }

        @Override
        public void onResult(JSONObject jsonObject) {

        }

        @Override
        public void onRetry(JSONObject jsonObject) {

        }
    };

    // zenbo視窗設定
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
    }

    // 設置表情或說話或細節的函式
    private static void setRobotExpression(RobotFace face) {
        robotAPI.robot.setExpression(face);
    }

    private static void setRobotExpression(RobotFace face, String text) {
        ExpressionConfig config = new ExpressionConfig();
        config.speed(85);
        robotAPI.robot.setExpression(face, text, config);
    }

    private static void setRobotExpression(RobotFace face, String text, ExpressionConfig config) {
        robotAPI.robot.setExpression(face, text, config);
    }

    // handle StateIntentions LanguageType
    private static JSONObject handleLanguageType(String language) {
        JSONObject LanguageType = new JSONObject();

        try {
            LanguageType.put("LanguageType", language);
        } catch (JSONException jex) {
            jex.printStackTrace();
        }
        return LanguageType;
    }


    public MainActivity() {
        super(robotCallback, robotListenCallback);
    }
}



