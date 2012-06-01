package com.wheelly.whater;

import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class WaterView extends SurfaceView implements SurfaceHolder.Callback {
	GameThread thread;
	
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
	
	private int width = 400;
	private int height = 400;
	private short riprad = 3;
	boolean flip;
	private short[] ripplemap, last_map;
	Bitmap ripple;
	private static final int line_width = 20;
	private static final int step = line_width * 2;
	
	private Rippler rippler;
	
	public WaterView(Context context) {
		super(context);
		initialize();
	}
	
	void initialize() {
		rippler = new NativeRippler();
		reinitgGlobals();
		fpsPaint.setTextSize(30);

        //Set thread
        getHolder().addCallback(this);

        setFocusable(true);
	}
	
	void reinitgGlobals() {
		int size = width * (height + 2) * 2;
		ripplemap = new short[size];
		last_map = new short[size];
		Bitmap texture = createBackground(width, height); // this creates a MUTABLE bitmap
		ripple = texture;
    	_td = new int[width * height];
    	texture.getPixels(_td, 0, width, 0, 0, width, height);
    	_rd = new int[width * height];
	}
	
	void randomizer() {
		final Random rnd = new Random();
		final Handler disHAndler = new Handler();
		final Runnable disturbWater = new Runnable() {
			@Override
			public void run() {
				disturb(rnd.nextInt(width), rnd.nextInt(height));
				disHAndler.postDelayed(this, 7000);
			}
		};
		disHAndler.post(disturbWater);
	}
	
	private static Bitmap createBackground(int width, int height) {
		Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bm);
		c.drawColor(Color.parseColor("#a2ddf8"));
		c.save();
		c.rotate(-45);
		Paint p = new Paint();
		p.setColor(Color.parseColor("#0077bb"));
		for (int i = 0; i < height / line_width; i++) {
			c.drawRect(-width, i * step, width * 3, i * step + line_width, p);
		}
		
		c.restore();
		return bm;
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		width = w;
		height = h;
		reinitgGlobals();
	}
	
	@Override
	protected void onDraw(android.graphics.Canvas canvas) {
		super.onDraw(canvas);
		newframe();
		canvas.drawBitmap(ripple, 0, 0, null);
		
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
	
    /**
     * Disturb water at specified point
     */
    private void disturb(int dx, int dy) {
    	rippler.disturb(dx, dy, width, height, riprad, ripplemap, flip);
    }
	
    int[] _td;
    int[] _rd;

    /**
     * Generates new ripples
     */
    private void newframe() {
        System.arraycopy(_td, 0, _rd, 0, width * height);
        flip = !flip;
        rippler.transformRipples(height, width, ripplemap, last_map, _td, _rd, flip);
        ripple.setPixels(_rd, 0, width, 0, 0, width, height);
    }
    
    
    @Override
    public synchronized boolean onTouchEvent(MotionEvent event) {
    	//switch(event.getAction()) {
		//case MotionEvent.ACTION_MOVE:
			disturb((int)event.getX(), (int)event.getY());
			return true;
    	//}
    	//return super.onTouchEvent(event);
    }

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		thread = new GameThread(getHolder(), this);
		thread.setRunning(true);
		thread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
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
        private WaterView gameView;
        private boolean run = false;

        public GameThread(SurfaceHolder surfaceHolder, WaterView gameView) {
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