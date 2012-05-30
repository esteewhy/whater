package com.wheelly.whater;

import java.util.Random;

import com.wheelly.whater.BallBounces.GameThread;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

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
	private int size, oldind, newind, riprad = 3, mapind;
	private short[] ripplemap, last_map;
	Bitmap ripple;
	private static final int line_width = 20;
	private static final int step = line_width * 2; 
	
	public WaterView(Context context) {
		super(context);
		initialize();
	}
	
	void initialize() {
		//width = getWidth();
		//height = getHeight();
		reinitgGlobals();
		fpsPaint.setTextSize(30);

        //Set thread
        getHolder().addCallback(this);

        setFocusable(true);
	}
	
	void reinitgGlobals() {
		size = width * (height + 2) * 2;
		ripplemap = new short[size * 2];
		last_map = new short[size * 2];
		Bitmap texture = createBackground(width, height); // this creates a MUTABLE bitmap
		ripple = texture;//.copy(Bitmap.Config.RGB_565, true);
		oldind = width;
		newind = width * (height + 3);
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
        dx <<= 0;
        dy <<= 0;
        
        for (int j = dy - riprad; j < dy + riprad; j++) {
            for (int k = dx - riprad; k < dx + riprad; k++) {
                ripplemap[oldind + (j * width) + k] += 128;
            }
        }
    }
	int[] _td;
    int[] _rd;

    /**
     * Generates new ripples
     */
    private void newframe() {
        System.arraycopy(_td, 0, _rd, 0, width * height);
        int i = oldind;
        oldind = newind;
        newind = i;
        mapind = transformRipples(height, width, ripplemap, last_map, _td, _rd, oldind, newind);
        ripple.setPixels(_rd, 0, width, 0, 0, width, height);
    }

	private int transformRipples(final int _height, final int _width,
			short[] rippleMap, short[] lastMap,
			final int[] textureBitmap, int[] rippleBitmap,
			int mapIndex, final int newIndex) {
		int i = 0;
		final int half_width = _width / 2;
		final int half_height = _height / 2;
		
		for (int y = _height; y > 0; y--) {
            for (int x = _width; x > 0; x--) {
                int data = (
                    rippleMap[mapIndex - _width] + 
                    rippleMap[mapIndex + _width] + 
                    rippleMap[mapIndex - 1] + 
                    rippleMap[mapIndex + 1]) >> 1;
                    
                data -= rippleMap[newIndex + i];
                data -= data >> 5;
                
        		rippleMap[newIndex + i] = (short)data;
        		
                //where data=0 then still, where data>0 then wave
                data = 1024 - data;
                
                int old_data = lastMap[i];
                lastMap[i] = (short)data;
                
                if (old_data != data) {
                    //offsets
                    int a = (((x - half_width) * data / 1024) << 0) + half_width;
                    int b = (((y - half_height) * data / 1024) << 0) + half_height;
    
                    //bounds check
                    if (a >= _width) a = _width - 1;
                    if (a < 0) a = 0;
                    if (b >= _height) b = _height - 1;
                    if (b < 0) b = 0;
    
                    int new_pixel = (a + (b * _width));// * 4;
                    int cur_pixel = i;// * 4;
                    
                    rippleBitmap[cur_pixel] = textureBitmap[new_pixel];
                }
                
                ++mapIndex;
                ++i;
            }
        }
		return mapIndex;
	}
    
    @Override
    public synchronized boolean onTouchEvent(MotionEvent event) {
    	// TODO Auto-generated method stub
    	disturb((int)event.getX(), (int)event.getY());
    	return super.onTouchEvent(event);
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
