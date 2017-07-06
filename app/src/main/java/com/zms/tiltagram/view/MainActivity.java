package com.zms.tiltagram.view;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.zms.tiltagram.R;
import com.zms.tiltagram.controller.domain.User;
import com.zms.tiltagram.model.UserDAO;
import com.zms.tiltagram.view.fragments.FragmentAccount;
import com.zms.tiltagram.view.fragments.FragmentFeed;
import com.zms.tiltagram.view.fragments.FragmentHome;
import com.zms.tiltagram.view.fragments.FragmentPicture;
import com.zms.tiltagram.view.fragments.FragmentSearch;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity{

    User user;

    Bundle infosFacebook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(AccessToken.getCurrentAccessToken()!=null){
            processLoginFacebook(AccessToken.getCurrentAccessToken());
        }else{
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        switch (item.getItemId()) {
                            case R.id.action_home:
                                selectedFragment = FragmentHome.newInstance();
                                break;
                            case R.id.action_search:
                                selectedFragment = FragmentSearch.newInstance();
                                break;
                            case R.id.action_picture:
                                selectedFragment = FragmentPicture.newInstance();
                                break;
                            case R.id.action_feed:
                                selectedFragment = FragmentFeed.newInstance();
                                break;
                            case R.id.action_account:
                                selectedFragment = FragmentAccount.newInstance();
                                break;
                        }
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.frame_layout, selectedFragment);
                        transaction.commit();
                        return true;
                    }
                });

        //Manually displaying the first fragment - one time only
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, FragmentHome.newInstance());
        transaction.commit();
    }

    private void processLoginFacebook(AccessToken token){

        GraphRequest request = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {

            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                // Get facebook data from login
                infosFacebook = getFacebookData(object);

                UserDAO userDAO = new UserDAO(MainActivity.this);
                user = userDAO.getUserbyID(infosFacebook.getString("idFacebook"));
                userDAO.close();
            }

        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id"); // Parâmetros que pedimos ao facebook
        request.setParameters(parameters);
        request.executeAsync();
    }

    private Bundle getFacebookData(JSONObject object) {

        Bundle bundle = new Bundle();

        try {
            String id = object.getString("id");
            bundle.putString("idFacebook", id);
        }catch(JSONException e){
            e.printStackTrace();
        }
        return bundle;
    }
}
