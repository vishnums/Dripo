package co.evelabs.beep;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import ca.uol.aig.fftpack.RealDoubleFFT;

public class AddPatient extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextBed;
    private Button buttonSave;
    String name,bed,volumeDis,durationDis,dropFactorDis;

    public int dropFactor=0, duration=0, volume=0;

    public int[] vols = {0,100, 200, 300, 400, 500};
    public int[] dur = {0,30, 60, 120, 180, 240};
    public int[] df = {0,10, 20, 30, 40, 50};

    MovingAverage ma;
    double result;

    TextView rate;

    int frequency = 8000;
    int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    int blockSize = 256;
    boolean flag;

    long startTime = 0;
    long eT;
    int droprate=0,buff=0;
    boolean started = false;

    RecordAudio recordTask;
    DatabaseReference mRef;

    TextView actualRate;
    Intent myIntend;


    private RealDoubleFFT transformer;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

    }


    @Override
    public void onStart() {

        super.onStart();

        ma = new MovingAverage(5);
        transformer = new RealDoubleFFT(blockSize);
        buttonSave = (Button) findViewById(R.id.buttonSave);
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextBed = (EditText) findViewById(R.id.editTextBed);
        actualRate = (TextView) this.findViewById(R.id.DripRate);

        Firebase.setAndroidContext(this);

        RecordAudio recordTask=null;
        recordTask = new RecordAudio();
        recordTask.execute();

        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        myIntend = getIntent();
        final String data=myIntend.getStringExtra("id").toString();

        if(data.equals("na")){

            mRef= mDatabase.getReference(user.getUid()+"/patients");

            name="Name";
            bed="bed/id";

        }else{

            if (user != null) {
                mRef= mDatabase.getReference(user.getUid()+"/patients");
                mRef=mRef.getRef().child(data);
                System.out.println("data " + mRef);

                // getting user data

                mRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Patient post = dataSnapshot.getValue(Patient.class);
                        volumeDis=post.VolumeStr();
                        durationDis=post.DurationInFormat();
                        dropFactorDis=post.dpfStr();
                        name=post.getName();
                        bed=post.getBed();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });


            }


        }





        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                 //Getting values to store
                String name = editTextName.getText().toString().trim();
                String bed = editTextBed.getText().toString().trim();


                //Creating Person object
                Patient patient = new Patient();
                patient.setName(name);
                patient.setBed(bed);
                patient.setVolume(volume);
                patient.setDuration(duration);
                patient.setDropFactor(dropFactor);

                if(data.equals("na")) {
                    String key = mRef.push().getKey();
                    Map<String, Object> patientValues = patient.toMap();
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put(key, patientValues);
                    mRef.updateChildren(childUpdates);
                }else{
                    String key = mRef.getKey();
                    mRef.setValue(patient);
                }




            }
        });


            System.out.println(volumeDis);
        System.out.println(dropFactorDis);
        System.out.println(durationDis);
        editTextName.setText(name);
        editTextBed.setText(bed);
        Spinner spinner = (Spinner) findViewById(R.id.volume_spinner);
        Spinner spinner1 = (Spinner) findViewById(R.id.duration_spinner);
        Spinner spinner2 = (Spinner) findViewById(R.id.drop_factor_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.volume_array, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,
                R.array.duration_array, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.dropFactor_array, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        int spinnerPosition = adapter.getPosition(volumeDis);
        spinner.setSelection(spinnerPosition);
        int spinnerPosition1 = adapter1.getPosition(durationDis);
        spinner1.setSelection(spinnerPosition1);
        int spinnerPosition2 = adapter2.getPosition(dropFactorDis);
        spinner2.setSelection(spinnerPosition2);

        spinner.setAdapter(adapter);
        spinner1.setAdapter(adapter1);
        spinner2.setAdapter(adapter2);




       spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                volume=vols[position];
                show_rate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
               duration=dur[position];
                show_rate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                dropFactor=df[position];
                show_rate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

    }


    public void show_rate(){

        if(volume!=0&&dropFactor!=0&&duration!=0){

            rate = (TextView) findViewById(R.id.rateTobeSet);
            result = volume * dropFactor / duration;
            rate.setText("Rate to be Set: " + Double.toString(result));
        }
    }


public void onStop(){
    super.onStop();
   // recordTask.cancel(true);
   // System.out.println("SHUT");
}

    public class RecordAudio extends AsyncTask<Void, double[], Void> {

        @Override
        protected Void doInBackground(Void... arg0) {

            try {

                int bufferSize = AudioRecord.getMinBufferSize(frequency,
                        channelConfiguration, audioEncoding);

                AudioRecord audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, frequency,
                        channelConfiguration, audioEncoding, bufferSize);

                short[] buffer = new short[blockSize];
                double[] toTransform = new double[blockSize];

                audioRecord.startRecording();


                started=true;

                while (started) {
                    int bufferReadResult = audioRecord.read(buffer, 0,
                            blockSize);

                    for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                        toTransform[i] = (double) buffer[i] / 32768.0; // signed
                        // 16
                    }                                       // bit
                    transformer.ft(toTransform);
                    publishProgress(toTransform);



                }

                audioRecord.stop();

            } catch (Throwable t) {
                t.printStackTrace();
                Log.e("AudioRecord", "Recording Failed");
             //   System.out.println("ok.fail.");
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(double[]... toTransform) {


            // canvas.drawColor(Color.BLACK);

            for (int i = 0; i < toTransform[0].length; i++) {
                // int x = i;
                int downy = (int) (100 - (toTransform[0][i] * 10));
                // int upy = 100;
                if(downy<80 && i>175 && i<185){
                    flag= true;
                }

                // canvas.drawLine(x, downy, x, upy, paint);
            }

            if (flag==true) {

                eT = System.currentTimeMillis()-startTime;


                if (eT !=0){
                buff=(int) (60000/eT);}
                flag=false;
                if(buff<500){
                    droprate=buff;
                }

                ma.newNum(droprate);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        actualRate.setText(Integer.toString((int)ma.getAvg()));
                    }
                });

                startTime=System.currentTimeMillis();

            }else{
                //  textView.setText("");
            }

        }


    }



}


