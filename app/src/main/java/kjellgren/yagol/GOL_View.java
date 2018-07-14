package kjellgren.yagol;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.WindowManager;

public class GOL_View extends SurfaceView implements Runnable {
    public static final int DEFAULT_CELL_SIZE = 50;
    public static final int DEFAULT_UPDATE_CANVAS = 100;
    public static final int DEFAULT_UPDATE_GRID = 1000;
    public static final int DEFAULT_ALIVE_COLOR = Color.GREEN;
    public static final int DEFAULT_DEAD_COLOR = Color.BLACK;
    // Thread for updating of Grid
    private Thread thread;
    // Boolean indicating if the app is not running
    private boolean is_running = false;
    private byte[][] grid, grid2;
    private int columnWidth, rowHeight, nbColumns, nbRows, count;
    private int time_counter = 0;
    private int next_update_time = 0;
    // Utilitaries objects : a Rectangle instance and a Paint instance used to draw the elements
    private Rect r = new Rect();
    private Paint p = new Paint();
    public GOL_View(Context context) {
        super(context);
        init_Grid();
    }
    public GOL_View(Context context, AttributeSet attrs) {
        super(context, attrs);
        init_Grid();
    }
    @Override
    public void run() {
        while (is_running) {
            if (!getHolder().getSurface().isValid())
                continue;
            try {
                Thread.sleep(DEFAULT_UPDATE_CANVAS);
            } catch (InterruptedException e) {
            }
            time_counter = time_counter + 100;
            // TODO: Make method to only draw singular cell so this only need to be drawn after every evolve_Grid()
            Canvas canvas = getHolder().lockCanvas();
            // Update time from touch have to be different from update time
            // from evolve_Grid()
            if (time_counter > next_update_time){
                evolve_Grid();
                next_update_time = next_update_time + DEFAULT_UPDATE_GRID;
            }

            draw_Grid(canvas);
            getHolder().unlockCanvasAndPost(canvas);
        }
    }
    public void start() {
        is_running = true;
        thread = new Thread(this);
        // we start the Thread for the app updating of the grid
        thread.start();
    }
    public void stop() {
        is_running = false;
        while (true) {
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
            break;
        }
    }

    private void init_Grid() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        // Get the size of the Grid
        nbColumns = point.x / DEFAULT_CELL_SIZE;
        nbRows = point.y / DEFAULT_CELL_SIZE;
        // Get the dimensions of each cell
        columnWidth = point.x / nbColumns;
        rowHeight = point.y / nbRows;
        grid = new byte[nbColumns][nbRows];
        grid2 = new byte[nbColumns][nbRows];
        for (int i = 0; i < nbColumns; i++) {
            for (int j = 0; j < nbRows; j++) {
                grid[i][j] = 0;
                grid2[i][j] = 0;
            }
        }
    }

    private void evolve_Grid() {
        for (int i = 0; i < nbColumns; i++) {
            for (int j = 0; j < nbRows; j++) {
                count = 0;
                if (i != 0) {count += grid[i-1][j];}
                if (i != nbColumns-1) {count += grid[i+1][j];}
                if (j != 0) {count += grid[i][j-1];}
                if (j != nbRows-1) {count += grid[i][j+1];}
                if (i != 0 & j != 0 ) {count += grid[i-1][j-1];}
                if (i != 0 & j != nbRows-1 ) {count += grid[i-1][j+1];}
                if (i != nbColumns-1 & j != 0 ) {count += grid[i+1][j-1];}
                if (i != nbColumns-1 & j != nbRows-1 ) {count += grid[i+1][j+1];}

                if (grid[i][j] == 1 & count < 2){grid2[i][j] = 0;}
                else if (grid[i][j] == 1 & count > 3){grid2[i][j] = 0;}
                else if (grid[i][j] == 1 & count == 2){grid2[i][j] = 1;}
                else if (grid[i][j] == 0 & count == 3){grid2[i][j] = 1;}
            }
        }
        for (int i = 0; i < nbColumns; i++) {
            for (int j = 0; j < nbRows; j++) {
                grid[i][j] = grid2[i][j];
            }
        }
    }

    private void draw_Grid(Canvas canvas) {
        for (int i = 0; i < nbColumns; i++) {
            for (int j = 0; j < nbRows; j++) {
                r.set((i * columnWidth) - 1, (j * rowHeight) - 1,
                        (int)(i * columnWidth + columnWidth*0.9) - 1,
                        (int) (j * rowHeight + rowHeight*0.9) - 1);
                // we change the color according the alive status of the cell
                if(grid[i][j] == 1) {
                    p.setColor(DEFAULT_ALIVE_COLOR);
                    r.set((i * columnWidth) - 1, (j * rowHeight) - 1,
                            (int)(i * columnWidth + columnWidth*0.9) - 1,
                            (int) (j * rowHeight + rowHeight*0.9) - 1);
                }
                else{
                    p.setColor(DEFAULT_DEAD_COLOR);
                    r.set((i * columnWidth) - 1, (j * rowHeight) - 1,
                            (i * columnWidth + columnWidth) - 1,
                            (j * rowHeight + rowHeight) - 1);
                }
                canvas.drawRect(r, p);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // we get the coordinates of the touch and we convert it in coordinates for the board
            int i = (int) (event.getX() / columnWidth);
            int j = (int) (event.getY() / rowHeight);
            if(grid[i][j] == 1) {grid[i][j] = 0;}
            else{grid[i][j] = 1;}
            // Need to set grid2 also, else it will just disappear if doing evolve_Grid()
            if(grid2[i][j] == 1) {grid2[i][j] = 0;}
            else{grid2[i][j] = 1;}
            // TODO: Make a method that only redraws this singular cell from thouch. Remmeber to check for memory leak afterwards
            invalidate();
        }
        return super.onTouchEvent(event);
    }
}
