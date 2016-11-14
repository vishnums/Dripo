package co.evelabs.beep;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    private Button addButton;
    String uid;
    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mRef;
    RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<Patient, PatientsViewHolder> adapter;
    private RecyclerView.AdapterDataObserver mObserver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
            mRef = mDatabase.getReference(uid);

        }

        setContentView(R.layout.activity_main);
        addButton = (Button) this.findViewById(R.id.add_button);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(MainActivity.this, AddPatient.class);
                newIntent.putExtra("id", "na");
                startActivity(newIntent);
            }

            // writeNewPatient("Sruthy", "111");

        });
    }


    protected void onStart() {
        super.onStart();

        recyclerView = (RecyclerView) findViewById(R.id.patientRecycler);
        recyclerView.setHasFixedSize(false);
        System.out.println(mRef.child("patients"));
        adapter=new FirebaseRecyclerAdapter<Patient, PatientsViewHolder>(Patient.class, android.R.layout.two_line_list_item, PatientsViewHolder.class, mRef.child("patients")) {

                    @Override
                    protected void populateViewHolder(final PatientsViewHolder patientViewHolder,Patient pat, final int position) {
                        patientViewHolder.nameText.setText(pat.getName());
                        patientViewHolder.roomText.setText(pat.getBed());


                        patientViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                               //   System.out.println(adapter.getRef(position).getKey());
                                Intent newIntent = new Intent(MainActivity.this, AddPatient.class);
                                newIntent.putExtra("id", adapter.getRef(position).getKey());
                                startActivity(newIntent);
                            }
                        });

                    }

        };
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        mObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                //perform check and show/hide empty view
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                //perform check and show/hide empty view
            }
        };
        adapter.registerAdapterDataObserver(mObserver);


    }

    public static class PatientsViewHolder extends RecyclerView.ViewHolder{

        TextView nameText;
        TextView roomText;
        View mView;

        public PatientsViewHolder(View v) {
            super(v);
            mView = v;
            nameText = (TextView) v.findViewById(android.R.id.text1);
            roomText = (TextView) v.findViewById(android.R.id.text2);

        }


    }


}





