package io.kristal.signature;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.File;

/**
 * Created by vincent Rifa on 23/03/2019.
 */

public final class SignatureActivity extends AppCompatActivity {

    private static final String TAG = SignatureActivity.class.getSimpleName();

    public static final String EXTRA_SIZE = "io.kristal.signature.SignatureActivity.EXTRA_SIZE";
    public static final String EXTRA_FILEPATH = "io.kristal.signature.SignatureActivity.EXTRA_FILEPATH";
    public static final String EXTRA_BASE64 = "io.kristal.signature.SignatureActivity.EXTRA_BASE64";

    LinearLayout mContent;
    signature mSignature;
    private CardView cardview_instructions, cardview_clear;
    private TextView instructions;
    private Button clear_button;
    private boolean draw;
    private int shortAnimationDuration;
    Image image;
    String base64;
    private int mSize;


    /***********************************************************************************************
     *
     * LIFECYCLE
     *
     **********************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);

        mSize = getIntent().getIntExtra(EXTRA_SIZE,0);

        mContent = (LinearLayout) this.findViewById(R.id.layout);
        mSignature = new signature(this, null);
        mSignature.setBackgroundColor(Color.WHITE);
        mContent.addView(mSignature, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        this.setTitle(R.string.signature_title);

        cardview_instructions = findViewById(R.id.cardview_instructions);
        instructions = findViewById(R.id.instructions);
        cardview_clear = findViewById(R.id.cardview_clear);
        clear_button = findViewById(R.id.clear_button);
        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        //Display of Instructions
        draw = false;
        refreshInfo();

        //Behavior of hidden Clear button
        clear_button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Log.v("log_tag", "Panel Cleared");
                mSignature.clear();
                refreshInfo();
            }
        });

    }

    /***********************************************************************************************
     *
     * MENU
     *
     **********************************************************************************************/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_map, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_ok) {

            //Clicking OK, save signature as .jpg, and send back its filepath and base64 version
            final Context context = this;
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    //Defining directory and filepath
                    File directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    image = new Image(directory, ".jpg", context);

                    // generate bitmap of signature
                    Bitmap mBitmap =  Bitmap.createBitmap (mContent.getWidth(), mContent.getHeight(), Bitmap.Config.RGB_565);
                    Canvas canvas = new Canvas(mBitmap);
                    mContent.draw(canvas);

                    //Save bitmap to jpg
                    image.saveBmp(mBitmap,50);

                    //Save bitmap in base64 at requiredSize
                    base64 = image.toBase64(context, mSize);
                 }
            });
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent();
            intent.putExtra(EXTRA_BASE64, base64);
            intent.putExtra(EXTRA_FILEPATH, image.getPath());
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    /***********************************************************************************************
     *
     * User Interface
     *
     **********************************************************************************************/

    public void refreshInfo(){
        //If signature is empty
        if(!draw){
            //Hide clear button
            cardview_clear.animate()
                    .alpha(0f)
                    .setDuration(shortAnimationDuration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            cardview_clear.setVisibility(View.GONE);
                        }
                    });
            clear_button.animate()
                    .alpha(0f)
                    .setDuration(shortAnimationDuration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            cardview_clear.setVisibility(View.GONE);
                        }
                    });
            //Display instructions
            instructions.setAlpha(0f);
            instructions.setVisibility(View.VISIBLE);
            instructions.animate()
                    .alpha(1f)
                    .setDuration(shortAnimationDuration)
                    .setListener(null);
            cardview_instructions.setAlpha(0f);
            cardview_instructions.setVisibility(View.VISIBLE);
            cardview_instructions.animate()
                    .alpha(1f)
                    .setDuration(shortAnimationDuration)
                    .setListener(null);

        }
        //If there's a signature
        else {
            //Hide instructions
            instructions.animate()
                    .alpha(0f)
                    .setDuration(shortAnimationDuration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            instructions.setVisibility(View.GONE);
                        }
                    });
            cardview_instructions.animate()
                    .alpha(0f)
                    .setDuration(shortAnimationDuration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            instructions.setVisibility(View.GONE);
                        }
                    });

            //Display clear button
            cardview_clear.setAlpha(0f);
            cardview_clear.setVisibility(View.VISIBLE);
            cardview_clear.animate()
                    .alpha(1f)
                    .setDuration(shortAnimationDuration)
                    .setListener(null);
            clear_button.setAlpha(0f);
            clear_button.setVisibility(View.VISIBLE);
            clear_button.animate()
                    .alpha(1f)
                    .setDuration(shortAnimationDuration)
                    .setListener(null);
        }
    }

    /***********************************************************************************************
     *
     * Signature
     *
     **********************************************************************************************/

    public class signature extends View
    {
        private static final float STROKE_WIDTH = 5f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        private Paint paint = new Paint();
        private Path path = new Path();

        private float lastTouchX;
        private float lastTouchY;
        private final RectF dirtyRect = new RectF();

        public signature(Context context, AttributeSet attrs)
        {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        public void clear()
        {
            draw=false;
            refreshInfo();
            path.reset();
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event)
        {
            float eventX = event.getX();
            float eventY = event.getY();
            if(!draw) {
                draw = true;
                refreshInfo();
            }

            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:

                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++)
                    {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;

                default:
                    debug("Ignored touch event: " + event.toString());
                    return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void debug(String string){
        }

        private void expandDirtyRect(float historicalX, float historicalY)
        {
            if (historicalX < dirtyRect.left)
            {
                dirtyRect.left = historicalX;
            }
            else if (historicalX > dirtyRect.right)
            {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top)
            {
                dirtyRect.top = historicalY;
            }
            else if (historicalY > dirtyRect.bottom)
            {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY)
        {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }
    }

}
