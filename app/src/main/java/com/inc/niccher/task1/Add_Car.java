package com.inc.niccher.task1;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.INotificationSideChannel;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.gson.internal.Primitives;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class Add_Car extends AppCompatActivity {

    private Spinner vmaker,vbody,vmodel,vyear,vmileage,vcondi,vng,vcolo,vtrans,vint,vfuel;
    private EditText bigdesc;
    private Button btnSubmit,btnupload;
    private int coun=0;
    private String uploadId ;
    private ProgressDialog pds;

    private ImageView imgsel;

    ProgressDialog pds2;

    Uri uri_image;

    FirebaseAuth mAuth;
    FirebaseUser userf;
    DatabaseReference dref,drefup;
    FirebaseDatabase fdbas;

    private StorageReference mStorageRef;
    private StorageTask mUploadTask;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_car);

        getSupportActionBar().setTitle("Add A vehicle");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        pds=new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        userf=mAuth.getCurrentUser();
        fdbas= FirebaseDatabase.getInstance();

        mStorageRef = FirebaseStorage.getInstance().getReference("VehicleImgs");

        dref = FirebaseDatabase.getInstance().getReference("Posteds").child(userf.getUid());

        uploadId= dref.push().getKey();
        Log.e("Ids ", "dref.push().getKey()  "+uploadId );

        pds2=new ProgressDialog(this);


        vmaker = (Spinner) findViewById(R.id.cmaka);
        vmaker.setOnItemSelectedListener(new CarMakerListing());

        vbody = (Spinner) findViewById(R.id.cbody);
        vbody.setOnItemSelectedListener(new CarMakerListing());

        vmodel = (Spinner) findViewById(R.id.cmodel);
        vmodel.setOnItemSelectedListener(new CarMakerListing());

        vyear = (Spinner) findViewById(R.id.cyear);
        vyear.setOnItemSelectedListener(new CarMakerListing());

        vmileage = (Spinner) findViewById(R.id.cmileage);
        vmileage.setOnItemSelectedListener(new CarMakerListing());

        vcondi = (Spinner) findViewById(R.id.ccond);
        vcondi.setOnItemSelectedListener(new CarMakerListing());

        vng = (Spinner) findViewById(R.id.cengine);
        vng.setOnItemSelectedListener(new CarMakerListing());

        vcolo = (Spinner) findViewById(R.id.ccolor);
        vcolo.setOnItemSelectedListener(new CarMakerListing());

        vtrans = (Spinner) findViewById(R.id.ctrans);
        vtrans.setOnItemSelectedListener(new CarMakerListing());

        vint = (Spinner) findViewById(R.id.cinte);
        vint.setOnItemSelectedListener(new CarMakerListing());

        vfuel = (Spinner) findViewById(R.id.cfuel);
        vfuel.setOnItemSelectedListener(new CarMakerListing());

        bigdesc =  findViewById(R.id.cdesc);

        imgsel = findViewById(R.id.com_imagesel);
        btnupload = findViewById(R.id.upload);

        btnSubmit = (Button) findViewById(R.id.fina);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendComit();
            }

        });

        imgsel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                    .setBorderLineColor(Color.RED)
                    .setBorderCornerColor(Color.BLUE)
                    .setGuidelinesColor(Color.GREEN)
                    .setBorderLineThickness(2)
                    .start(Add_Car.this);
            }
        });

        btnupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(Add_Car.this, "Upload in progress", Toast.LENGTH_SHORT).show();
                } else {
                    uploadFile();
                    //Toast.makeText(MainActivity.this, mPostBody.getText().toString().trim(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                uri_image = result.getUri();
                Picasso.get().load(uri_image).into(imgsel);//imgshow
                //Verrif();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                String er=error.getMessage().toString();
                Toast.makeText(this, ""+er, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void SendComit() {
        pds.setMessage("Uploading Info");
        pds.show();

        Calendar cal= Calendar.getInstance();
        SimpleDateFormat ctime=new SimpleDateFormat("HH:mm");
        SimpleDateFormat cdate=new SimpleDateFormat("dd-MMMM-yyyy");

        final String ctim=ctime.format(cal.getTime());
        final String cdat=cdate.format(cal.getTime());

        DatabaseReference mDatabaseRef= FirebaseDatabase.getInstance().getReference("Posteds/"+(userf.getUid()+"/Vehicles"));//.push();

        HashMap<String , Object> hasm2=new HashMap<String, Object>();

        hasm2.put("cMaker" ,String.valueOf(vmaker.getSelectedItem()));
        hasm2.put("cBody" ,String.valueOf(vbody.getSelectedItem()));
        hasm2.put("cModel" ,String.valueOf(vmodel.getSelectedItem()));
        hasm2.put("cYear" ,String.valueOf(vyear.getSelectedItem()));
        hasm2.put("cMileage" ,String.valueOf(vmileage.getSelectedItem()));
        hasm2.put("cCondition",String.valueOf(vcondi.getSelectedItem()));
        hasm2.put("cEngine" ,String.valueOf(vng.getSelectedItem()));
        hasm2.put("cColor" ,String.valueOf(vcolo.getSelectedItem()));
        hasm2.put("cTransmision",String.valueOf(vtrans.getSelectedItem()));
        hasm2.put("cInterior" ,String.valueOf(vint.getSelectedItem()));
        hasm2.put("cFuel" ,String.valueOf(vfuel.getSelectedItem()));
        hasm2.put("cDesc",bigdesc.getText().toString());
        hasm2.put("cKey",uploadId);
        hasm2.put("cOwner",userf.getUid());
        hasm2.put("cTime","On "+cdat+" At "+ctim);

        mDatabaseRef.child(uploadId).updateChildren(hasm2).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                pds.dismiss();
                Intent carimg=new Intent(Add_Car.this, Casa.class);
                carimg.putExtra("PostUUIDCode","Posts");
                startActivity(carimg);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pds.dismiss();
                Toast.makeText(Add_Car.this, "Add car addOnFailureListener\n"+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void uploadFile() {
        pds2.setTitle("Uploading Image Resource");
        pds2.setMessage("Please Wait");
        pds2.show();

        if (uri_image != null) {
            final StorageReference fileReference = mStorageRef.child("Img"+System.currentTimeMillis()+ ".jpeg");

            mUploadTask = fileReference.putFile(uri_image)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    drefup=FirebaseDatabase.getInstance().getReference("Posteds/"+userf.getUid()+"/Vehicles/"+uploadId);

                                    HashMap<String , Object> hasm3=new HashMap<String, Object>();

                                    hasm3.put("cImg"+coun/*System.currentTimeMillis()*/ ,uri.toString());
                                    coun=coun+1;
                                    //Log.e("mUploadTask ", "onSuccess: "+coun );
                                    uri_image=null;

                                    drefup.updateChildren(hasm3).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pds.dismiss();
                                            Toast.makeText(Add_Car.this, "Add Image URL addOnFailureListener\n"+e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });


                                    Picasso.get().load(R.drawable.ic_addcimg).placeholder(R.drawable.ic_addcimg).into(imgsel);
                                    pds2.dismiss();

                                    Toast.makeText(Add_Car.this, "Upload Succesfull", Toast.LENGTH_SHORT).show();

                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pds2.dismiss();
                            Toast.makeText(Add_Car.this, "Upload task addOnFailureListener\n"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            //pds.setProgress((int) progress);
                        }
                    });
        } else {
            pds2.dismiss();
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int idd=item.getItemId();
        if (idd==android.R.id.home){
            finish();
            startActivity(new Intent(Add_Car.this,Casa.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, Login.class));
        }
    }

}
