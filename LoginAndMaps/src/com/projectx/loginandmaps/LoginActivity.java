package com.projectx.loginandmaps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AppEventsLogger;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.foursquare.android.nativeoauth.FoursquareCancelException;
import com.foursquare.android.nativeoauth.FoursquareDenyException;
import com.foursquare.android.nativeoauth.FoursquareInvalidRequestException;
import com.foursquare.android.nativeoauth.FoursquareOAuth;
import com.foursquare.android.nativeoauth.FoursquareOAuthException;
import com.foursquare.android.nativeoauth.FoursquareUnsupportedVersionException;
import com.foursquare.android.nativeoauth.model.AccessTokenResponse;
import com.foursquare.android.nativeoauth.model.AuthCodeResponse;
import com.projectx.loginandmaps.FoursquareApp.FsqAuthListener;

public class LoginActivity extends FragmentActivity {
	
	public static final String MyPREFS = "MyPrefs";
	private FoursquareApp fsapp;
	private SharedPreferences myprefs;
	
	public static final String CLIENT_ID = "PDELA4BIYIQIJQ0MNGOQUJ0PPV0NPO3KLHI02SRM24HH5PCY";
	public static final String CLIENT_SECRET = "ZMZKW3DVG535N2J0HYO4ZJNT3LRATHONLYD0HQLBCNWCFHDA";
	
	private static final int REQUEST_CODE_FSQ_CONNECT = 200;
    private static final int REQUEST_CODE_FSQ_TOKEN_EXCHANGE = 201;
	private static final String ACCESS_TOKEN = null;
	
	// For facebook
	private static final String USER_SKIPPED_LOGIN_KEY = "user_skipped_login";
	
	private static final int SPLASH = 0;
    private static final int SELECTION = 1;
    private static final int SETTINGS = 2;
    private static final int FRAGMENT_COUNT = SETTINGS +1;

    private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];
    private MenuItem settings;
    private boolean isResumed = false;
    private boolean userSkippedLogin = false;
    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        // setting default screen to login.xml
        setContentView(R.layout.login);
        myprefs = getSharedPreferences(MyPREFS, Context.MODE_PRIVATE);
        if (myprefs.contains("api_key")) {
//        	Editor editor = myprefs.edit();
//            editor.putString("access_token", myprefs.getString("access_token", ACCESS_TOKEN));
//            editor.commit();
            Intent i = new Intent(getApplicationContext(), MapsActivity.class);
			startActivity(i);
        }
        fsapp = new FoursquareApp(getApplicationContext(), CLIENT_ID, CLIENT_SECRET);
        
        Button login = (Button) findViewById(R.id.btnLogin);
        login.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = FoursquareOAuth.getConnectIntent(LoginActivity.this, CLIENT_ID);
        		startActivityForResult(intent, REQUEST_CODE_FSQ_CONNECT);
			}
		});
        FsqAuthListener listener = new FsqAuthListener() {
        	@Override
        	public void onSuccess() {
//        		Toast.makeText(getApplicationContext(), "Connected as " + fsapp.getUserName(), Toast.LENGTH_SHORT).show();
//        		nameTv.setText("Connected as " + mFsqApp.getUserName());
        	}
        	
        	@Override
        	public void onFail(String error) {
        		Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
        	}
        };
        
        fsapp.setListener(listener);
        
        // Facebook setup.
        if (savedInstanceState != null) {
            userSkippedLogin = savedInstanceState.getBoolean(USER_SKIPPED_LOGIN_KEY);
        }
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

//        FragmentManager fm = getSupportFragmentManager();
//        SplashFragment splashFragment = (SplashFragment) fm.findFragmentById(R.id.splashFragment);
//        fragments[SPLASH] = splashFragment;
////        fragments[SELECTION] = fm.findFragmentById(R.id.selectionFragment);
////        fragments[SETTINGS] = fm.findFragmentById(R.id.userSettingsFragment);
//
//        FragmentTransaction transaction = fm.beginTransaction();
//        transaction.hide(fragments[SPLASH]);
//        transaction.commit();
//
//        splashFragment.setSkipLoginCallback(new SplashFragment.SkipLoginCallback() {
//            @Override
//            public void onSkipLoginPressed() {
//                userSkippedLogin = true;
//                showFragment(SELECTION, false);
//            }
//        });
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		isResumed = true;
		uiHelper.onResume();
		myprefs = getSharedPreferences(MyPREFS, Context.MODE_PRIVATE);
		if (myprefs.contains("access_token")) {
			startActivity(new Intent(getApplicationContext(), AddCard.class));
		}
//		AppEventsLogger.activateApp(this);
	}
	
	@Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }
	
	private void onCompleteConnect(int resultCode, Intent data) {
        AuthCodeResponse codeResponse = FoursquareOAuth.getAuthCodeFromResult(resultCode, data);
        Exception exception = codeResponse.getException();
        
        if (exception == null) {
            // Success.
            String code = codeResponse.getCode();
            performTokenExchange(code);

        } else {
            if (exception instanceof FoursquareCancelException) {
                // Cancel.
                toastMessage(this, "Canceled");

            } else if (exception instanceof FoursquareDenyException) {
                // Deny.
            	toastMessage(this, "Denied");
                
            } else if (exception instanceof FoursquareOAuthException) {
                // OAuth error.
                String errorMessage = exception.getMessage();
                String errorCode = ((FoursquareOAuthException) exception).getErrorCode();
                toastMessage(this, errorMessage + " [" + errorCode + "]");
                
            } else if (exception instanceof FoursquareUnsupportedVersionException) {
                // Unsupported Fourquare app version on the device.
            	toastError(this, exception);
                
            } else if (exception instanceof FoursquareInvalidRequestException) {
                // Invalid request.
            	toastError(this, exception);
                
            } else {
                // Error.
            	toastError(this, exception);
            }
        }
    }
    
    private void onCompleteTokenExchange(int resultCode, Intent data) {
        AccessTokenResponse tokenResponse = FoursquareOAuth.getTokenFromResult(resultCode, data);
        Exception exception = tokenResponse.getException();
        
        if (exception == null) {
            String accessToken = tokenResponse.getAccessToken();
            // Success.
//            toastMessage(this, "Access token: " + accessToken);
            
            // Persist the token for later use. In this example, we save
            // it to shared prefs.
            //ExampleTokenStore.get().setToken(accessToken);
            fsapp.setupAccessToken(accessToken);
            Editor editor = myprefs.edit();
            editor.putString("access_token", accessToken);
            editor.commit();
            Intent i = new Intent(getApplicationContext(), AddCard.class);
			startActivity(i);
            
        } else {
            if (exception instanceof FoursquareOAuthException) {
                // OAuth error.
                String errorMessage = ((FoursquareOAuthException) exception).getMessage();
                String errorCode = ((FoursquareOAuthException) exception).getErrorCode();
                toastMessage(this, errorMessage + " [" + errorCode + "]");
                
            } else {
                // Other exception type.
            	toastError(this, exception);
            }
        }
    }
    @Override
    public void onPause() {
    	isResumed = false;
    	uiHelper.onPause();
    	super.onPause();
    }
    
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_FSQ_CONNECT:
                onCompleteConnect(resultCode, data);
                break;
                
            case REQUEST_CODE_FSQ_TOKEN_EXCHANGE:
                onCompleteTokenExchange(resultCode, data);
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }
    
    /**
     * Exchange a code for an OAuth Token. Note that we do not recommend you
     * do this in your app, rather do the exchange on your server. Added here
     * for demo purposes.
     * 
     * @param code 
     *          The auth code returned from the native auth flow.
     */
    private void performTokenExchange(String code) {
        Intent intent = FoursquareOAuth.getTokenExchangeIntent(this, CLIENT_ID, CLIENT_SECRET, code);
        startActivityForResult(intent, REQUEST_CODE_FSQ_TOKEN_EXCHANGE);
    }
    
    public static void toastMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void toastError(Context context, Throwable t) {
        Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
    }
    
    /*
     *  Facebook login functions.
     */
    
    private void showFragment(final int fragmentIndex, final boolean addToBackStack) {
    	new Handler().post(new Runnable() {
			
			@Override
			public void run() {
				FragmentManager fm = getSupportFragmentManager();
		        FragmentTransaction transaction = fm.beginTransaction();
//		        for (int i = 0; i < fragments.length; i++) {
//		            if (i == fragmentIndex) {
//		                transaction.show(fragments[i]);
//		            } else {
//		                transaction.hide(fragments[i]);
//		            }
//		        }
		        transaction.show(fragments[fragmentIndex]);
		        if (addToBackStack) {
		            transaction.addToBackStack(null);
		        }
		        transaction.commit();
				
			}
		});
        
    }
    
    @Override
    protected void onResumeFragments() {
        super.onResume();
        Session session = Session.getActiveSession();

        if (session != null && session.isOpened()) {
            // if the session is already open, try to show the selection fragment
//            showFragment(SELECTION, false);
        	startActivity(new Intent(getApplicationContext(), AddCard.class));
//            userSkippedLogin = false;
//        } else if (userSkippedLogin) {
//            showFragment(SELECTION, false);
        } else {
            // otherwise present the splash screen and ask the user to login, unless the user explicitly skipped.
//            showFragment(SPLASH, false);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // only add the menu when the selection fragment is showing
//        if (fragments[SELECTION].isVisible()) {
//            if (menu.size() == 0) {
//                settings = menu.add(R.string.settings);
//            }
//            return true;
//        } else {
            menu.clear();
            settings = null;
//        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.equals(settings)) {
            showSettingsFragment();
            return true;
        }
        return false;
    }

    public void showSettingsFragment() {
        showFragment(SETTINGS, true);
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (isResumed) {
//            FragmentManager manager = getSupportFragmentManager();
//            int backStackSize = manager.getBackStackEntryCount();
//            for (int i = 0; i < backStackSize; i++) {
//                manager.popBackStack();
//            }
            // check for the OPENED state instead of session.isOpened() since for the
            // OPENED_TOKEN_UPDATED state, the selection fragment should already be showing.
            if (state.equals(SessionState.OPENED)) {
//                showFragment(SELECTION, false);
            	startActivity(new Intent(getApplicationContext(), AddCard.class));
            } else if (state.isClosed()) {
//                showFragment(SPLASH, false);
            }
        }
    }

	
}