package aditya.parkeasyQR;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /*Variables :
        User ID
        User name
        Car number
     */
    int user_id;
    String user_name;
    String car_number;
    int parking_slot_id;
    int slot;
    //-------------------//
    TextView largeView;
    TextView mediumView;
    Button btn;
    ImageView img;

    boolean booked_online;
    boolean entry;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        largeView=(TextView)findViewById(R.id.textView);
        mediumView=(TextView)findViewById(R.id.textView2);
        img=(ImageView)findViewById(R.id.imageView);
        btn=(Button)findViewById(R.id.button);
        img.setImageResource(R.drawable.green_tick);
        img.setVisibility(View.INVISIBLE);

    }

    public void scanQR(View view){
        entry=true;
        IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
        //integrator.addExtra("SCAN_WIDTH", 640);
        //integrator.addExtra("SCAN_HEIGHT", 480);
        integrator.setCameraId(0);
        //customize the prompt message before scanning
        integrator.addExtra("PROMPT_MESSAGE", "Scanner Start!");
        integrator.initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        List<String> items;
        if (result != null) {
            String contents = result.getContents();
            if (contents != null) {
                items = Arrays.asList(contents.split("\\s*,\\s*"));
                user_id = Integer.parseInt(items.get(0));
                user_name = items.get(1);
                car_number = items.get(2);

                if(entry) {
                    bookedOnline();
                }else{
                    exitParking();
                }

            } else {

                Toast.makeText(MainActivity.this, contents, Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void enterSuccess(){
        largeView.setText("Hi,");
        mediumView.setText(user_name);
        img.setVisibility(View.VISIBLE);
        btn.setEnabled(false);
        btn.setText("Please Enter!");
        Toast.makeText(MainActivity.this, user_name+" "+car_number, Toast.LENGTH_SHORT).show();

    }

    public void resetAll(View view){
        largeView.setText("");
        mediumView.setText("");
        img.setVisibility(View.INVISIBLE);
        btn.setEnabled(true);
        btn.setText("Click here to enter!");

    }

    //Function for checking whether it is connected to internet or not.
    private boolean connectivityInfo(){
        boolean bool=false;
        ConnectivityManager manager=(ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        try{
            NetworkInfo info=manager.getActiveNetworkInfo();
            bool=info.isConnected();
        }
        catch (NullPointerException e){
            //It was throwing a NullPointerException and crash if not connected to internet.
            //Working fine with this.
        }


        return bool;
    }


    //On enter check booked Online or not
    private void bookedOnline(){

        RequestQueue queue = SingletonRequest.getInstance(this.getApplicationContext()).
                getRequestQueue();
        //How to check from localhost
        //http://stackoverflow.com/questions/6760585/accessing-localhostport-from-android-emulator

        String url="https://vast-wave-6400.herokuapp.com/api/v1/booked_online/"+user_id;
        //https://vast-wave-6400.herokuapp.com/api/v1/parking_status/id

        Log.d("Articles", url);
        StringRequest request=new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                      //  parseJSON(response)

                        Toast.makeText(MainActivity.this, response, Toast.LENGTH_LONG).show();
                        if(response.equals("false")){
                            booked_online=false;
                            Log.d("booked online","calledParkingstatus");

                            Toast.makeText(MainActivity.this, "booked online", Toast.LENGTH_LONG).show();
                            parkingStatus();
                            Log.d("booked online","calledParkingstatus");
                        }else{
                            booked_online=true;
                            enterSuccess();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Booked_online", error.toString());
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);
    }

    // Get parking status:
    private void parkingStatus(){

        RequestQueue queue = SingletonRequest.getInstance(this.getApplicationContext()).
                getRequestQueue();
        //How to check from localhost
        //http://stackoverflow.com/questions/6760585/accessing-localhostport-from-android-emulator

        String url="https://vast-wave-6400.herokuapp.com/api/v1/parking_status/1";
        //https://vast-wave-6400.herokuapp.com/api/v1/parking_status/id


        Log.d("Parking Status:)", url);

        JsonArrayRequest request=new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //  parseJSON(response)
                       // JSONObject ob= new JSONObject();

                      //  List<String> items;
                       // items=Arrays.asList(response.split("\\s*,\\s*"));
                        JSONObject ob=new JSONObject();
                        try {
                            ob=response.getJSONObject(0);
                            Toast.makeText(MainActivity.this, ob.toString(), Toast.LENGTH_LONG).show();

                            parking_slot_id=ob.getInt("id");
                            slot=ob.getInt("slot_id");

                            Toast.makeText(MainActivity.this, ""+parking_slot_id, Toast.LENGTH_LONG).show();
                            bookSlot();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Booked_online", error.toString());
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);
    }


    //Book him a slot!
    private void bookSlot(){

        RequestQueue queue = SingletonRequest.getInstance(this.getApplicationContext()).
                getRequestQueue();
        //How to check from localhost
        //http://stackoverflow.com/questions/6760585/accessing-localhostport-from-android-emulator

       // String url="https://vast-wave-6400.herokuapp.com/api/v1/booked_online/"+user_id;
        //https://vast-wave-6400.herokuapp.com/api/v1/parking_status/id
        if(parking_slot_id==0){
            parking_slot_id=1;
        }
        String url="https://vast-wave-6400.herokuapp.com/api/v1/entry?ids%5b%5d="+user_id+"&ids%5b%5d=1&ids%5b%5d="+parking_slot_id;


        Log.d("Articles", url);
        StringRequest request=new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //  parseJSON(response)

                        Toast.makeText(MainActivity.this, response, Toast.LENGTH_LONG).show();

                        enterSuccess();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("BookSlot", error.toString());
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);
    }

    //Exit
    public void exit(View view){
        IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
        //integrator.addExtra("SCAN_WIDTH", 640);
        //integrator.addExtra("SCAN_HEIGHT", 480);
        integrator.setCameraId(0);
        //customize the prompt message before scanning
        integrator.addExtra("PROMPT_MESSAGE", "Scanner Start!");
        integrator.initiateScan();
        entry=false;
    }

    //Exit Pakring
    private void exitParking(){

        RequestQueue queue = SingletonRequest.getInstance(this.getApplicationContext()).
                getRequestQueue();
        //How to check from localhost
        //http://stackoverflow.com/questions/6760585/accessing-localhostport-from-android-emulator

        String url="https://vast-wave-6400.herokuapp.com/api/v1/exit/"+user_id;
        //https://vast-wave-6400.herokuapp.com/api/v1/parking_status/id

        Log.d("exit", url);
        StringRequest request=new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //  parseJSON(response)
                        exitSuccess();
                        Toast.makeText(MainActivity.this, response, Toast.LENGTH_LONG).show();


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Exit", error.toString());
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);
    }

    private void exitSuccess() {
        largeView.setText("Thank You,");
        mediumView.setText(user_name);
        img.setVisibility(View.VISIBLE);
        btn.setEnabled(false);
        btn.setText("Have a nice day!");
        Toast.makeText(MainActivity.this, user_name+" "+car_number, Toast.LENGTH_SHORT).show();
    }


}
