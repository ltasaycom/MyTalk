package com.example.luist.mytalk;

import android.app.Activity;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{

    private static final String TAG = RecyclerView.class.getSimpleName();

    private List<User> users;

    private Activity activity;

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public UserAdapter(List<User> posts) {
        this.users = users;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public UserAdapter() {
        this.users = new ArrayList<>();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView userImage;
        TextView displaynameText;
        TextView user_displaymail;
        TextView user_distancia;

        ViewHolder(View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.user_picture);
            displaynameText = itemView.findViewById(R.id.user_displayname);
            user_displaymail = itemView.findViewById(R.id.user_displaymail);
            user_distancia = itemView.findViewById(R.id.user_distancia);
        }
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_talk, parent, false);
        return new ViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull final UserAdapter.ViewHolder holder, int position) {
        final User user = users.get(position);

        holder.displaynameText.setText(user.getDisplayName());
        holder.user_displaymail.setText(user.getEmail());

        if(user.getPhotoUrl() != null) {
            Picasso.with(holder.itemView.getContext()).load(user.getPhotoUrl()).into(holder.userImage);
        }else{
            holder.userImage.setImageResource(R.drawable.ic_picture);
        }

        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                User userYou = dataSnapshot.getValue(User.class);
                Location locationA = new Location("punto A");

                locationA.setLatitude(userYou.getLatitude());
                locationA.setLongitude(userYou.getLongitude());

                Location locationB = new Location("punto B");

                locationB.setLatitude(user.getLatitude());
                locationB.setLongitude(user.getLongitude());

                float distance = locationA.distanceTo(locationB);

                float distanceKM = (distance / 1000);

                DecimalFormat formato = new DecimalFormat("#,###.00");
                String valorFormateado = formato.format(distanceKM);

                if (user.getUid() == userYou.getUid()) {
                    holder.user_distancia.setText("Tú");
                }
                else
                {
                    holder.user_distancia.setText(String.valueOf(valorFormateado) + "Kms");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled " + databaseError.getMessage(), databaseError.toException());
            }
        });

    }

    @Override
    public int getItemCount() {
        return this.users.size();
    }

}
