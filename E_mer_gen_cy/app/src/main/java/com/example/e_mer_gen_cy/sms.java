package com.example.e_mer_gen_cy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class sms extends AppCompatActivity
{
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0;
    private static final int REQUEST_CODE_PERMISSION = 2;
    Intent intent;
    EditText phn;
    String smsMsg=null;
    String phoneNo = null;
    String password = null;

    private static final int RESULT_PICK_CONTACT =1;
    private ImageView select;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);
        //password = ((EditText)findViewById(R.id.txtPassword)).getText().toString();

        select = findViewById (R.id.pick);
        phn = (EditText)findViewById(R.id.editTextPhoneNo);

        //picking contacts
        select.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                Intent in = new Intent (Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult (in, RESULT_PICK_CONTACT);
            }
        });


        // WE NEED TO SET THESE PERMISSIONS IN ORDER TO ALLOW THE LOCATIOON SERVICE TO RESPOND
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            // Permission already Granted

            intent = new Intent(this, MyService.class);
            this.startService(intent);


//Perform operations here only which requires permission
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }



        ///////////////////////////////////////////////////
        ///////////// SMS LISTENING CODE START ////////////
        //////////////////////////////////////////////////

        try {

            SmsReceiver.bindListener(new SmsListener() {
                @Override
                public void messageReceived(String messageText, String sender) {
                    phoneNo = sender;

                    if (messageText.equals(null))
                        sendSMSMessage();

                }
            });

        }
        catch (Exception ex)
        {
            Toast.makeText(this,ex.getMessage(),Toast.LENGTH_LONG).show();
        }
        //##############################################
        //========== SMS LISTENING CODE END  ===========
        //##############################################


    }

    public void btnStartService_Click(View view)
    {
        intent = new Intent(this, MyService.class);
        this.startService(intent);
    }

    public void btnStopService_Click(View view)
    {
        this.stopService(intent);
    }

    public void btnSendSMS_Click(View view)
    {
        phoneNo = ((EditText) findViewById(R.id.editTextPhoneNo)).getText().toString();
        smsMsg = ((TextView)findViewById(R.id.textviewGPS)).getText().toString();
        sendSMSMessage();
    }

    protected void sendSMSMessage()
    {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS))
        {
        }
        else
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
        }
        //  }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    SmsManager smsManager = SmsManager.getDefault();

                    smsManager.sendTextMessage(phoneNo, null, smsMsg, null, null);
                    Toast.makeText(getApplicationContext(), "SMS Sent", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            break;

            case 1:
                if (requestCode == 1) {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    {
                        //Permission Granted
                        //Do your work here
//Perform operations here only which requires permission
                    }

                    if (grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                    {
                        //Permission Granted
                        //Do your work here
//Perform operations here only which requires permission
                    }


                    break;
                }
        }
    }



//////////////////////////////////////////////
////////////// INNER CLASS BEGINS ////////////
//////////////////////////////////////////////

    // The below code will be receiving the local broadcast messages sent by the Service
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            // TODO Auto-generated method stub
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            Log.d("receiver", "Got message: " + message);
            ((TextView)findViewById(R.id.textviewGPS)).setText(message);

        }
    };
    //######################################
    ///////// INNER CLASS ENDS /////////////
    //######################################






    @Override
    protected void onPause()
    {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "custom-event-name".
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("custom-event-name"));
        super.onResume();
    }



    ////////////////////////////
    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        if(resultCode==RESULT_OK)
        {
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    contactPicked (data);
                    break;
            }
        }
        else
        {
            Toast.makeText (this, "Failed To pick contact", Toast.LENGTH_SHORT).show ();
        }
    }

    private void contactPicked(Intent data) {
        Cursor cursor = null;

        try {
            String phoneNo = null;
            Uri uri = data.getData ();
            cursor = getContentResolver ().query (uri, null, null,null,null);
            cursor.moveToFirst ();
            int phoneIndex = cursor.getColumnIndex (ContactsContract.CommonDataKinds.Phone.NUMBER);

            phoneNo = cursor.getString (phoneIndex);

            phn.setText (phoneNo);


        } catch (Exception e) {
            e.printStackTrace ();
        }
    }

}
