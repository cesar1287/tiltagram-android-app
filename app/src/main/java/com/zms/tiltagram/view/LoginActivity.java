package com.zms.tiltagram.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import com.loopj.android.http.*;
import com.zms.tiltagram.R;
import com.zms.tiltagram.controller.domain.User;
import com.zms.tiltagram.model.UserDAO;

import cz.msebera.android.httpclient.Header;

public class LoginActivity extends AppCompatActivity {

    LoginButton loginButton;
    CallbackManager callbackManager;

    private Bundle bFacebookData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        setContentView(R.layout.activity_login);

        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                processLoginFacebook(loginResult);
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, R.string.error_facebook_login_canceled, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(LoginActivity.this, R.string.error_facebook_login_unknown_error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private Bundle getFacebookData(JSONObject object) {

        Bundle bundle = new Bundle();

        try {
            String id = object.getString("id");

            try {
                URL profile_pic = new URL("https://graph.facebook.com/" + id + "/picture?width=200&height=150");
                bundle.putString("profile_pic", profile_pic.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }

            bundle.putString("idFacebook", id);
            if (object.has("first_name")) {
                bundle.putString("first_name", object.getString("first_name"));
            }if (object.has("last_name")) {
                bundle.putString("last_name", object.getString("last_name"));
            }if (object.has("email")) {
                bundle.putString("email", object.getString("email"));
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        return bundle;
    }

    private void processLoginFacebook(LoginResult loginResult){

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("Processando login...");
        progressDialog.show();

        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                // Get facebook data from login
                bFacebookData = getFacebookData(object);

                assert bFacebookData != null;
                String first_name = bFacebookData.getString("first_name");
                String last_name = bFacebookData.getString("last_name");

                final String name = first_name + " " + last_name;

                //String email = bFacebookData.getString("email");

                final String profilePicture = bFacebookData.getString("profile_pic");

                final String facebookId = bFacebookData.getString("idFacebook");

                AsyncHttpClient client = new AsyncHttpClient();

                HashMap<String, String> paramMap = new HashMap<>();
                paramMap.put("name", name);
                paramMap.put("profilePicture", profilePicture);
                RequestParams params = new RequestParams(paramMap);

                client.post("https://tiltgram-api.herokuapp.com/user/create", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                        try {
                            JSONObject json = new JSONObject(
                                    new String(responseBody));

                            User user = new User();
                            user.setId(json.getString("userId"));
                            user.setFacebookId(facebookId);
                            user.setName(name);
                            user.setProfilePicture(profilePicture);

                            UserDAO userDAO = new UserDAO(LoginActivity.this);
                            if(!userDAO.isUserCreated(user.getId())) {
                                userDAO.insert(user);
                                userDAO.close();
                            }

                            progressDialog.dismiss();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                        LoginManager.getInstance().logOut();
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Falha", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, first_name, last_name, email"); // Parâmetros que pedimos ao facebook
        request.setParameters(parameters);
        request.executeAsync();
    }
}
