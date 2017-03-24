package launcher;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.gilshelef.feedme.R;

/**
 * Created by gilshe on 3/21/17.
 */

public class RegistrationDonorActivity extends AppCompatActivity{
    public static final int REGISTER_DONOR = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_donor);
    }


    private void sendDataAndFinish() {
        Intent intent = new Intent();
        Bundle mBundle = new Bundle();
        mBundle.putParcelable(RegistrationActivity.DONOR, null);
        intent.putExtras(mBundle);
        setResult(RESULT_OK, intent);
        finish();
    }
}
