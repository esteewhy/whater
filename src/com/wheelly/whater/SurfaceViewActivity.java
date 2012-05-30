package com.wheelly.whater;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SurfaceViewActivity extends Activity {
    BallBounces ball;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ball = new BallBounces(this);
        setContentView(ball);
    }
}


class BallBounces extends SurfaceView implements SurfaceHolder.Callback {
    GameThread thread;
    int screenW; //Device's screen width.
    int screenH; //Devices's screen height.
    int ballX; //Ball x position.
    int ballY; //Ball y position.
    int initialY ;
    float dY; //Ball vertical speed.
    int ballW;
    int ballH;
    int bgrW;
    int bgrH;
    int angle;
    int bgrScroll;
    int dBgrY; //Background scroll speed.
    float acc;
    Bitmap ball, bgr, bgrReverse;
    boolean reverseBackroundFirst;
    boolean ballFingerMove;

    //Measure frames per second.
    long now;
    int framesCount=0;
    int framesCountAvg=0;
    long framesTimer=0;
    Paint fpsPaint=new Paint();

    //Frame speed
    long timeNow;
    long timePrev = 0;
    long timePrevFrame = 0;
    long timeDelta;


    public BallBounces(Context context) {
        super(context);
        ball = BitmapFactory.decodeResource(getResources(),R.drawable.football); //Load a ball image.
        bgr = BitmapFactory.decodeResource(getResources(),R.drawable.sky_bgr); //Load a background.
        ballW = ball.getWidth();
        ballH = ball.getHeight();

        //Create a flag for the onDraw method to alternate background with its mirror image.
        reverseBackroundFirst = false;

        //Initialise animation variables.
        acc = 0.2f; //Acceleration
        dY = 0; //vertical speed
        initialY = 100; //Initial vertical position
        angle = 0; //Start value for the rotation angle
        bgrScroll = 0;  //Background scroll position
        dBgrY = 1; //Scrolling background speed

        fpsPaint.setTextSize(30);

        //Set thread
        getHolder().addCallback(this);

        setFocusable(true);
    }

    @Override
    public void onSizeChanged (int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //This event-method provides the real dimensions of this custom view.
        screenW = w;
        screenH = h;

        bgr = Bitmap.createScaledBitmap(bgr, w, h, true); //Scale background to fit the screen.
        bgrW = bgr.getWidth();
        bgrH = bgr.getHeight();

        //Create a mirror image of the background (horizontal flip) - for a more circular background.
        Matrix matrix = new Matrix();  //Like a frame or mould for an image.
        matrix.setScale(-1, 1); //Horizontal mirror effect.
        bgrReverse = Bitmap.createBitmap(bgr, 0, 0, bgrW, bgrH, matrix, true); //Create a new mirrored bitmap by applying the matrix.

        ballX = (int) (screenW /2) - (ballW / 2) ; //Centre ball X into the centre of the screen.
        ballY = -50; //Centre ball height above the screen.
    }

    //***************************************
    //*************  TOUCH  *****************
    //***************************************
    @Override
    public synchronized boolean onTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                ballX = (int) ev.getX() - ballW/2;
                ballY = (int) ev.getY() - ballH/2;

                ballFingerMove = true;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                ballX = (int) ev.getX() - ballW/2;
                ballY = (int) ev.getY() - ballH/2;

                break;
            }

            case MotionEvent.ACTION_UP:
                ballFingerMove = false;
                dY = 0;
                break;
            }
        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //Draw scrolling background.
        Rect fromRect1 = new Rect(0, 0, bgrW - bgrScroll, bgrH);
        Rect toRect1 = new Rect(bgrScroll, 0, bgrW, bgrH);

        Rect fromRect2 = new Rect(bgrW - bgrScroll, 0, bgrW, bgrH);
        Rect toRect2 = new Rect(0, 0, bgrScroll, bgrH);

        if (!reverseBackroundFirst) {
            canvas.drawBitmap(bgr, fromRect1, toRect1, null);
            canvas.drawBitmap(bgrReverse, fromRect2, toRect2, null);
        }
        else{
            canvas.drawBitmap(bgr, fromRect2, toRect2, null);
            canvas.drawBitmap(bgrReverse, fromRect1, toRect1, null);
        }

        //Next value for the background's position.
        if ( (bgrScroll += dBgrY) >= bgrW) {
            bgrScroll = 0;
            reverseBackroundFirst = !reverseBackroundFirst;
        }

        //Compute roughly the ball's speed and location.
        if (!ballFingerMove) {
            ballY += (int) dY; //Increase or decrease vertical position.
            if (ballY > (screenH - ballH)) {
                dY=(-1)*dY; //Reverse speed when bottom hit.
            }
            dY+= acc; //Increase or decrease speed.
        }

        //Increase rotating angle
        if (angle++ >360)
            angle =0;
        
        //DRAW BALL
        //Rotate method one
        /*
        Matrix matrix = new Matrix();
        matrix.postRotate(angle, (ballW / 2), (ballH / 2)); //Rotate it.
        matrix.postTranslate(ballX, ballY); //Move it into x, y position.
        canvas.drawBitmap(ball, matrix, null); //Draw the ball with applied matrix.

        */// Rotate method two

        canvas.save(); //Save the position of the canvas matrix.
        canvas.rotate(angle, ballX + (ballW / 2), ballY + (ballH / 2)); //Rotate the canvas matrix.
        canvas.drawBitmap(ball, ballX, ballY, null); //Draw the ball by applying the canvas rotated matrix.
        canvas.restore(); //Rotate the canvas matrix back to its saved position - only the ball bitmap was rotated not all canvas.

        //*/

        //Measure frame rate (unit: frames per second).
         now=System.currentTimeMillis();
         canvas.drawText(framesCountAvg+" fps", 40, 70, fpsPaint);
         framesCount++;
         if(now-framesTimer>1000) {
                 framesTimer=now;
                 framesCountAvg=framesCount;
                 framesCount=0;
         }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread = new GameThread(getHolder(), this);
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {

            }
        }
    }


    class GameThread extends Thread {
        private SurfaceHolder surfaceHolder;
        private BallBounces gameView;
        private boolean run = false;

        public GameThread(SurfaceHolder surfaceHolder, BallBounces gameView) {
            this.surfaceHolder = surfaceHolder;
            this.gameView = gameView;
        }

        public void setRunning(boolean run) {
            this.run = run;
        }

        public SurfaceHolder getSurfaceHolder() {
            return surfaceHolder;
        }

        @Override
        public void run() {
            Canvas c;
            while (run) {
                c = null;

                //limit frame rate to max 60fps
                timeNow = System.currentTimeMillis();
                timeDelta = timeNow - timePrevFrame;
                if ( timeDelta < 16) {
                    try {
                        Thread.sleep(16 - timeDelta);
                    }
                    catch(InterruptedException e) {

                    }
                }
                timePrevFrame = System.currentTimeMillis();

                try {
                    c = surfaceHolder.lockCanvas(null);
                    synchronized (surfaceHolder) {
                       //call methods to draw and process next fame
                        gameView.onDraw(c);
                    }
                } finally {
                    if (c != null) {
                        surfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }
    }
}