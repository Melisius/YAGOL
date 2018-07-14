package kjellgren.yagol;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private GOL_View gol_View;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gol_View = (GOL_View) findViewById(R.id.game_of_life);
    }
    @Override
    protected void onResume() {
        super.onResume();
        gol_View.start();
    }
    @Override
    protected void onPause() {
        super.onPause();
        gol_View.stop();
    }
}
