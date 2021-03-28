package com.example.als.ui.more;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.example.als.R;
import com.example.als.handler.Connectivity;
import com.example.als.object.Donation;
import com.example.als.object.Event;
import com.example.als.object.Variable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class DonationDetailsActivity extends AppCompatActivity {

    Connectivity device;
    WebView donationWebView;
    Button downloadAsPdfBtn;

    String donationSessionId;
    Toolbar customizeDonationDetailsToolbar;
    FirebaseUser cUser;

    String[] separatedDateAndTime;
    String donationAmount;
    String status;
    String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_details);

        device = new Connectivity(this);

        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_LONG).show();
        }
        else{

            cUser = FirebaseAuth.getInstance().getCurrentUser();

            if(cUser != null){
                donationWebView = findViewById(R.id.donationDetailsWebView);
                downloadAsPdfBtn = findViewById(R.id.downloadAsPdfButton);
                customizeDonationDetailsToolbar = findViewById(R.id.customizeSearchUserMessageToolbar);
                setSupportActionBar(customizeDonationDetailsToolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle("Donation Details");

                initialize();

                downloadAsPdfBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Context context = DonationDetailsActivity.this;
                        String documentName=donationSessionId +"Document";
                        PrintManager printManager=(PrintManager)DonationDetailsActivity.this.getSystemService(context.PRINT_SERVICE);
                        PrintDocumentAdapter adapter = donationWebView.createPrintDocumentAdapter(documentName);
                        printManager.print(donationSessionId, adapter, new PrintAttributes.Builder().build());

                    }
                });
            }

        }


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initialize(){
        Intent session = getIntent();

        if(session.hasExtra(Variable.DONATION_SESSION_ID)){
            donationSessionId = session.getStringExtra(Variable.DONATION_SESSION_ID);
        }
        else{
            Toasty.warning(getApplicationContext(), "Session is expired. Please try again. ", Toast.LENGTH_LONG).show();
            finish();
        }

        Variable.DONATION_REF.child(donationSessionId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    Donation donation = snapshot.getValue(Donation.class);

                    if(donation != null){
                        if(donation.getDonationDateTime() != null){
                            //separate the date and time
                            separatedDateAndTime = donation.getDonationDateTime().split(" ");
                        }

                        if(donation.getDonationAmount() != 0){
                            donationAmount = String.valueOf(donation.getDonationAmount());
                        }

                        if(donation.getDonationState() != null){
                            status = donation.getDonationState();
                        }

                        if(donation.getDonationEventId() != null){
                            eventId = donation.getDonationEventId();
                        }

                        String html = "<!DOCTYPE html>\n" +
                                "<html lang=\"en\">\n" +
                                "<head>\n" +
                                "    <meta charset=\"UTF-8\">\n" +
                                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                                "    <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta3/dist/css/bootstrap.min.css\" rel=\"stylesheet\">\n" +
                                "    <title>Document</title>\n" +
                                "</head>\n" +
                                "<body>\n" +
                                "    <div class=\"container\">\n" +
                                "        <h3>Donation Receipt</h3>\n" +
                                "        <h5>Invoice: "+ donationSessionId +"</h5>\n" +
                                "        <h6>Date: "+separatedDateAndTime[0]+"</h6>\n" +
                                "        <h6>Time: "+separatedDateAndTime[1]+ " "+separatedDateAndTime[2]+"</h6>\n" +
                                "        <table class=\"table\">\n" +
                                "            <thead>\n" +
                                "              <tr>\n" +
                                "                <th scope=\"col\">Item</th>\n" +
                                "                <th scope=\"col\"></th>\n" +
                                "                <th scope=\"col\">Sub Total (RM)</th>\n" +
                                "              </tr>\n" +
                                "            </thead>\n" +
                                "            <tbody>\n" +
                                "              <tr>\n" +
                                "                <td id=\"description\">Donation for "+eventId +"</td>\n" +
                                "                <td></td>\n" +
                                "                <td id=\"amount\"> "+donationAmount+"</td>\n" +
                                "              </tr>\n" +
                                "              <tr>\n" +
                                "                  <td></td>\n" +
                                "                  <td>Total</td>\n" +
                                "                  <td id=\"tAmount\"> "+donationAmount+"</td>\n" +
                                "              </tr>\n" +
                                "            </tbody>\n" +
                                "          </table>\n" +
                                "          <h6 id=\"status\"> Payment Status: "+status+"</h6>\n" +
                                "    </div>\n" +
                                "    \n" +
                                "</body>\n" +
                                "</html>";

                        donationWebView.loadDataWithBaseURL(null,html,"text/html","utf-8",null);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("DonationDetails", "DatabaseError: " + error.getMessage());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}