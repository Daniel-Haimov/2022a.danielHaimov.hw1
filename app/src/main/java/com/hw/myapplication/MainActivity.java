package com.hw.myapplication;



import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final int MAX_LIVES = 3;
    private int lives = MAX_LIVES;

    private final int VIBRATE_DURATION = 500;
    Vibrator vibrator;

    private int num_of_rows;
    private int num_of_cols;
    private boolean row_with_stone;
    private Random rand;
    private int player_pos;


    final int DELAY = 1000;
    final Handler handler = new Handler();
    private Runnable timerRunnable;

    private ImageView       panel_IMG_BG        ;

    private ImageView[]     panel_IMG_hearts    ;
    private ImageView[][]   panel_IMG_stones    ;
    private ImageView[]     panel_IMG_players   ;
    private ImageButton     panel_BTN_left      ;
    private ImageButton     panel_BTN_right     ;

//    private Drawable[]      panel_DRW_icons     ;
//    private final int       PLAYER_DRAW             = 0     ;
    private final int       PLAYER_DRAW_ROTATION    = 0     ;
//    private final int       HIT_DRAW                = 1     ;
    private final int       HIT_DRAW_ROTATION       = 0     ;
//    private final int       STONE_DRAW              = 2     ;
//    private final int       STONE_DRAW_ROTATION     = 135   ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        Glide.with(MainActivity.this).load(R.drawable.bg).centerCrop().into(panel_IMG_BG);
        boot_variables();
    }

    private void findViews() {
        panel_IMG_BG        = findViewById(R.id.panel_IMG_backGround);
        panel_IMG_hearts    = new ImageView[] {
                findViewById(R.id.panel_IMG_heart_0),
                findViewById(R.id.panel_IMG_heart_1),
                findViewById(R.id.panel_IMG_heart_2)
        };
        panel_IMG_stones    = new ImageView[][]{
                {
                        findViewById(R.id.panel_IMG_stone0_0),
                        findViewById(R.id.panel_IMG_stone0_1),
                        findViewById(R.id.panel_IMG_stone0_2),
                },
                {
                        findViewById(R.id.panel_IMG_stone1_0),
                        findViewById(R.id.panel_IMG_stone1_1),
                        findViewById(R.id.panel_IMG_stone1_2),
                },
                {
                        findViewById(R.id.panel_IMG_stone2_0),
                        findViewById(R.id.panel_IMG_stone2_1),
                        findViewById(R.id.panel_IMG_stone2_2),
                },
                {
                        findViewById(R.id.panel_IMG_stone3_0),
                        findViewById(R.id.panel_IMG_stone3_1),
                        findViewById(R.id.panel_IMG_stone3_2),
                },
//                {
//                        findViewById(R.id.panel_IMG_stone4_0),
//                        findViewById(R.id.panel_IMG_stone4_1),
//                        findViewById(R.id.panel_IMG_stone4_2),
//                },
//                {
//                        findViewById(R.id.panel_IMG_stone5_0),
//                        findViewById(R.id.panel_IMG_stone5_1),
//                        findViewById(R.id.panel_IMG_stone5_2),
//                },
//                {
//                        findViewById(R.id.panel_IMG_stone6_0),
//                        findViewById(R.id.panel_IMG_stone6_1),
//                        findViewById(R.id.panel_IMG_stone6_2),
//                },
                {
                        findViewById(R.id.panel_IMG_player_0),
                        findViewById(R.id.panel_IMG_player_1),
                        findViewById(R.id.panel_IMG_player_2),
                },
        };
        panel_BTN_left      = findViewById(R.id.panel_BTN_left );
        panel_BTN_right     = findViewById(R.id.panel_BTN_right);

    }

    private void boot_variables() {
        num_of_rows = panel_IMG_stones      .length;
        num_of_cols = panel_IMG_stones[0]   .length;
        panel_IMG_players = panel_IMG_stones[num_of_rows - 1];
        row_with_stone = true;
        rand = new Random();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        timerRunnable = () -> {
            updateClockView();
            handler.postDelayed(timerRunnable, DELAY);
        };
//        panel_DRW_icons = new Drawable[]{
//                Drawable.createFromPath("drawable/ic_player.xml"),
//                Drawable.createFromPath("drawable/ic_hit.xml"),
//                Drawable.createFromPath("drawable/ic_stone.xml"),
//        };
        initButtons();
        initPlayer();
    }

    private void initPlayer() {
        player_pos = num_of_cols / 2;
        fixPlayerRow();
    }

    private void initButtons() {
        panel_BTN_left.setOnClickListener(v -> {
            if (player_pos > 0){
                MovementController(-1);
            }
        });

        panel_BTN_right.setOnClickListener(v -> {
            if (player_pos < num_of_cols - 1){
                MovementController(1);
            }
        });
    }

    /*
    param direction -> 1 for right; -1 for left;
     */
    private void MovementController(int direction){
        panel_IMG_players[player_pos    ].setImageResource(R.drawable.ic_player );
        panel_IMG_players[player_pos    ].setVisibility(View.INVISIBLE          );
        player_pos += direction;
        checkHits();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startTicker();
    }

    private void startTicker() {
        handler.postDelayed(timerRunnable, DELAY);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopTicker();
    }

    private void stopTicker() {
        handler.removeCallbacks(timerRunnable);
    }

    private void updateClockView() {
        updateView();
        updateFirstRow();
        checkHits();
    }

    private void updateView() {
        for (int i = num_of_rows - 1; i > 0; i--)
            for (int j = 0; j < num_of_cols; j++) {
                Drawable id = panel_IMG_stones[i - 1][j].getDrawable();
                panel_IMG_stones[i][j].setImageDrawable(id);
                panel_IMG_stones[i][j].setVisibility(panel_IMG_stones[i - 1][j].getVisibility() );
                panel_IMG_stones[i][j].setRotation  (panel_IMG_stones[i - 1][j].getRotation()   );
            }
    }

    private void checkHits() {
        // If there is object in player position
        ImageView obj = panel_IMG_stones[num_of_rows-1][player_pos];
        if (obj.getVisibility() == View.VISIBLE) {
            //TODO
            // check if obj is stone or coin
            hitByStone();
        }else
            hitView(false);
    }

    private void hitByStone() {
        hitView(true);
        Toast.makeText(MainActivity.this, "Hit", Toast.LENGTH_SHORT).show();
        Vibrate();
        lives--;
        if (lives <= 0)//remove the '=' after changes unlimited lives
            gameOver();
        else
            panel_IMG_hearts[lives].setVisibility(View.INVISIBLE);
    }

    private void hitView(boolean hit) {
        if(hit){
            panel_IMG_players[player_pos].setRotation       (HIT_DRAW_ROTATION      );
            panel_IMG_players[player_pos].setImageResource  (R.drawable.ic_hit_png  );
        }else{
            fixPlayerRow();
        }
    }

    private void fixPlayerRow() {
        panel_IMG_players[player_pos].setRotation       (PLAYER_DRAW_ROTATION);
        panel_IMG_players[player_pos].setImageResource  (R.drawable.ic_player);
        panel_IMG_players[player_pos].setVisibility     (View.VISIBLE        );
    }

    private void updateFirstRow() {
        if (row_with_stone)
            randomizeFirstRow();
        else
            InvisibleFirstRow();
        row_with_stone = !row_with_stone;
    }

    private void randomizeFirstRow() {
        int col = rand.nextInt(num_of_cols);
        panel_IMG_stones[0][col].setVisibility(View.VISIBLE);
    }

    private void InvisibleFirstRow() {
        for (int i = 0; i < num_of_cols; i++)
            panel_IMG_stones[0][i].setVisibility(View.INVISIBLE);
    }

    private void gameOver() {
        //TODO
        for (ImageView heart: panel_IMG_hearts) {
            heart.setVisibility(View.VISIBLE);
            lives = MAX_LIVES;
        }
    }

    private void Vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(VIBRATE_DURATION, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /*
    *
    private void changeVisibility(@NonNull View imageView) {
        int x = (imageView.getVisibility() + View.INVISIBLE) % View.GONE;
        imageView.setVisibility(x);
    }
    *
     */

}