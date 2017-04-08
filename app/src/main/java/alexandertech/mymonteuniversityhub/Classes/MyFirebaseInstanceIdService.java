package alexandertech.mymonteuniversityhub.Classes;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by jayva on 3/31/2017.
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {
private static final String REG_TOKEN = "REG_TOKEN";

    @Override
    public void onTokenRefresh() {
        String recent_token = FirebaseInstanceId.getInstance().getToken();
        Log.v(REG_TOKEN, recent_token);

    }
    public String getFirebaseAndroidID(){

    return FirebaseInstanceId.getInstance().getToken();
    }
}
