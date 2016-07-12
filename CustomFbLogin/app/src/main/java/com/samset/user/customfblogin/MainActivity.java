package com.samset.user.customfblogin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ImageView imgFacebookLogin, logout;
    public static CallbackManager callbackManager;

    private Intent intent;
    private ImageView imageView;
    private ProfilePictureView profile;
    private TextView tvName, tvEmail;
    private LinearLayout linearLayout;
    private RelativeLayout relativeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        imgFacebookLogin = (ImageView) findViewById(R.id.imgFacebookLogin);
        logout = (ImageView) findViewById(R.id.imgFacebooklogout);
        tvEmail = (TextView) findViewById(R.id.useremail);
        tvName = (TextView) findViewById(R.id.username);
        profile = (ProfilePictureView) findViewById(R.id.picture);
        linearLayout = (LinearLayout) findViewById(R.id.profilelayout);
        relativeLayout = (RelativeLayout) findViewById(R.id.logoutlayout);
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        imgFacebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fblogin();
            }
        });
    }

    public void getHashKey() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.samset.user.customfblogin", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("HASH KEY:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
        } catch (NoSuchAlgorithmException e) {
        }
    }

    private void fblogin() {
        callbackManager = CallbackManager.Factory.create();
        final List<String> permissionNeeds = Arrays.asList("user_photos", "email", "user_birthday", "user_friends", "public_profile");
        //List<String> permissionNeed = Arrays.asList("publish_actions");
        LoginManager.getInstance().logInWithReadPermissions(this, permissionNeeds);
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResults) {

                        GraphRequest request = GraphRequest.newMeRequest(loginResults.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        try {
                                            linearLayout.setVisibility(View.VISIBLE);
                                            relativeLayout.setVisibility(View.VISIBLE);
                                            imgFacebookLogin.setVisibility(View.GONE);


                                            String email = object.getString("email");
                                            String name = object.getString("name");
                                            String id = object.getString("id");
                                            URL fbUserImageUrl = new URL("https://graph.facebook.com/" + id + "/picture?width=200&height=150");

                                            tvName.setText(name);
                                            tvEmail.setText(email);
                                            profile.setProfileId(object.getString("id"));
                                           /* try {
                                                Bitmap bmp = BitmapFactory.decodeStream((InputStream) new URL(fbUserImageUrl.toString()).getContent());
                                                imageView.setImageBitmap(bmp);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
*/
                                            Log.e("Fb", "Fb details" + name + " email " + email + " image" + fbUserImageUrl.toString());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,gender, birthday");
                        request.setParameters(parameters);
                        request.executeAsync();

                    }

                    @Override
                    public void onCancel() {
                        Log.e("FB", "facebook login canceled");
                        try {
                            LoginManager.getInstance().logOut();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(FacebookException e) {
                        Log.e("FB", "facebook login failed error");
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void logout() {
        Log.e("FB", "facebook login logout");
        LoginManager.getInstance().logOut();
        linearLayout.setVisibility(View.GONE);
        relativeLayout.setVisibility(View.GONE);
        imgFacebookLogin.setVisibility(View.VISIBLE);

    }
}
