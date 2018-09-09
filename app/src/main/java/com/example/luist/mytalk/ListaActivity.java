package com.example.luist.mytalk;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

public class ListaActivity extends AppCompatActivity {

    private static final String TAG = ListaActivity.class.getSimpleName();
    private static final int REGISTER_FORM_REQUEST = 100;
    private static final int REGISTER_MAPS_REQUEST = 200;

    LocationManager locationManager;
    double longitudeGPS, latitudeGPS;
    FirebaseUser currentUser;
    User user;
    private File lastPhoto;
    private FirebaseAnalytics mFirebaseAnalytics;
    private RecyclerView recyclerView;
    private EditText v_edit_name;
    private RelativeLayout v_imput_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        v_edit_name = (EditText)findViewById(R.id.edit_name);
        v_imput_name = (RelativeLayout)findViewById(R.id.imput_name);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Get currentuser from FirebaseAuth
        currentUser = FirebaseAuth.getInstance().getCurrentUser();



        toggleGPSUpdates();

        // Obteniendo datos del usuario de Firebase en tiempo real
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange " + dataSnapshot.getKey());

                // Obteniendo datos del usuario
                User user = dataSnapshot.getValue(User.class);
                setTitle("Bienvenido " + user.getDisplayName());

                if (user.getDisplayName()==null)
                {
                    v_imput_name.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled " + databaseError.getMessage(), databaseError.toException());
            }
        });

        // Lista de post con RecyclerView
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final UserAdapter userAdapter = new UserAdapter();
        recyclerView.setAdapter(userAdapter);

        // Obteniendo lista de posts de Firebase (con realtime)
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("users");
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                // Obteniendo nuevo post de Firebase
                String userKey = dataSnapshot.getKey();
                final User addeduser = dataSnapshot.getValue(User.class);

                // Actualizando adapter datasource
                List<User> users = userAdapter.getUsers();
                users.add(0, addeduser);
                userAdapter.notifyDataSetChanged();

                // ...
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {

                // Obteniendo post modificado de Firebase
                final User changeduser = dataSnapshot.getValue(User.class);

                // Actualizando adapter datasource
                List<User> users = userAdapter.getUsers();
                int index = users.indexOf(changeduser); // Necesario implementar Post.equals()
                if (index != -1) {
                    users.set(index, changeduser);
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                // ...
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved " + dataSnapshot.getKey());

                // A post has changed position, use the key to determine if we are
                // displaying this post and if so move it.


                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled " + databaseError.getMessage(), databaseError.toException());
            }
        };
        postsRef.addChildEventListener(childEventListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                callLogout(null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void callLogout(View view) {
        Log.d(TAG, "Sign out user");
        FirebaseAuth.getInstance().signOut();
        finish();
    }

    private boolean checkLocation() {
        if (!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Su ubicación esta desactivada.\npor favor active su ubicación " +
                        "usa esta app")
                .setPositiveButton("Configuración de ubicación", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void toggleGPSUpdates() {


        if (!checkLocation())
            return;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            }
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 2 * 20 * 1000, 10, locationListenerGPS);
    }

    private final LocationListener locationListenerGPS = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitudeGPS = location.getLongitude();
            latitudeGPS = location.getLatitude();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {


                }
            });
        }
        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }
        @Override
        public void onProviderDisabled(String s) {
        }
    };

    public void GrabarDatos(View view) {

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
        userRef.child(currentUser.getUid())
                .child("latitude").setValue(latitudeGPS);
        userRef.child(currentUser.getUid())
                .child("longitude").setValue(longitudeGPS);
        userRef.child(currentUser.getUid())
                .child("displayName").setValue(v_edit_name.getText().toString());

        v_imput_name.setVisibility(View.GONE);
    }

    public void showMaps(View view)
    {
        startActivityForResult(new Intent(this, MapsActivity.class), REGISTER_MAPS_REQUEST);
    }

    public void takePicture(View view) {
        Log.d(TAG, "takePicture");

        final String[] permissions = new String[]{
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };

        Nammu.init(this);

        // ValidatePermissions: https://github.com/tajchert/Nammu
        if (!Nammu.hasPermission(this, permissions)) {
            Nammu.askForPermission(this, permissions, new PermissionCallback() {
                @Override
                public void permissionGranted() {
                    Log.d(TAG, "permissionGranted");
                    EasyImage.openChooserWithGallery(ListaActivity.this, "Seleccione una imagen…", 0);
                }

                @Override
                public void permissionRefused() {
                    Log.d(TAG, "permissionRefused");
                    Toast.makeText(ListaActivity.this, "No ha concedido el permiso", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        // Start camera/gallery: https://github.com/jkwiecien/EasyImage
        EasyImage.openChooserWithGallery(this, "Seleccione una imagen…", 0);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePicked(File file, EasyImage.ImageSource imageSource, int i) {
                Log.d(TAG, "onImagePicked: file: " + file);
                Log.d(TAG, "onImagePicked: imageSource: " + imageSource);

                // Reducir la imagen a 800px solo si lo supera
                File resizedFile = scaleBitmapDown(file, 800);

                lastPhoto = resizedFile;

                if (lastPhoto == null || !lastPhoto.exists()) {
                    Toast.makeText(ListaActivity.this, "Debes incluir una foto", Toast.LENGTH_LONG).show();
                    return;
                }

                final ProgressDialog progressDialog = new ProgressDialog(ListaActivity.this);
                progressDialog.setMessage("Cargando foto…");
                progressDialog.setIndeterminate(false);
                progressDialog.setMax(100);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCancelable(false);
                progressDialog.show();

                // Get currentuser from FirebaseAuth
                final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                Uri localUri = Uri.fromFile(lastPhoto);
                // Registrar a Firebase Storage /posts/{userid}/{nombre del archivo}
                StorageReference storageRef = FirebaseStorage.getInstance().getReference("users").child(currentUser.getUid());
                final StorageReference photoRef = storageRef.child(localUri.getLastPathSegment());
                photoRef.putFile(localUri)
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                progressDialog.setProgress((int) Math.round(progress));
                            }
                        })
                        .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                // Continue with the task to get the download URL
                                return photoRef.getDownloadUrl();
                            }
                        })
                        .addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                progressDialog.dismiss();
                                if (task.isSuccessful()) {


                                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
                                    userRef.child(currentUser.getUid())
                                            .child("photoUrl").setValue(task.getResult().toString())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(ListaActivity.this, "Imagen Guardado Correctamente", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(ListaActivity.this, "Error al guardar", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                } else {
                                    Toast.makeText(ListaActivity.this, "Error al cargar", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                //Cancel handling, you might wanna remove taken photo if it was canceled
                if (source == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(ListaActivity.this);
                    if (photoFile != null) photoFile.delete();
                }
            }

            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                Log.d(TAG, "onImagePickerError " + e.getMessage(), e);
            }
        });
    }

    // Redimensionar una imagen
    private File scaleBitmapDown(File file, int maxDimension) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());

            int originalWidth = bitmap.getWidth();
            int originalHeight = bitmap.getHeight();
            int resizedWidth = maxDimension;
            int resizedHeight = maxDimension;

            if (originalHeight > originalWidth) {
                resizedHeight = maxDimension;
                resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
            } else if (originalWidth > originalHeight) {
                resizedWidth = maxDimension;
                resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
            } else if (originalHeight == originalWidth) {
                resizedHeight = maxDimension;
                resizedWidth = maxDimension;
            }

            bitmap = Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);

            File resizedFile = new File(file.getPath() + "_resized.jpg");
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(resizedFile));

            return resizedFile;
        } catch (Throwable t) {
            Log.e(TAG, t.toString(), t);
            return file;
        }
    }

}
