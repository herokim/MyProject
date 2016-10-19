package kushiel.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.view.TextureView;

/**
 *  게임 화면을 그리기 위한 TextureView
 */
public class GameView extends TextureView implements TextureView.SurfaceTextureListener {

    private Thread mThread;

    /**
     *  슈퍼 클래스에는 기본 생성자가 없으므로 인수가 있는 생성자를 명시적으로 호출
     *
     */
    public GameView(Context context) {
        super(context); // 슈퍼 클래스의 생성자를 인수 context로 호출
        setSurfaceTextureListener(this);
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Paint paint = new Paint();
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.FILL);
                Canvas canvas = lockCanvas();
                if(canvas == null){
                    return;
                }
                canvas.drawCircle(100,100,50,paint);
                unlockCanvasAndPost(canvas);
            }
        }
        );

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {

        Canvas canvas = lockCanvas();
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);

        canvas.drawCircle(300,300,200,paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(8);
        canvas.drawRect(0,0,500,300,paint);
        unlockCanvasAndPost(canvas);


    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }
}
