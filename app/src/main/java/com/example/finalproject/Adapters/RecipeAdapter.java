package com.example.finalproject.Adapters;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.Listeners.RecipeClickListener;
import com.example.finalproject.Models.FavouriteRecipe;
import com.example.finalproject.Models.Recipe;
import com.example.finalproject.R;
import com.example.finalproject.Storage.SystemStorage;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeViewHolder> {
    Context context;
    List<Recipe> list;
    RecipeClickListener listener;

    public RecipeAdapter(Context context, List<Recipe> list, RecipeClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecipeViewHolder(LayoutInflater.from(context).inflate(R.layout.recipe_list,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        holder.textView_title.setText(list.get(position).title);
        holder.textView_title.setSelected(true);
        holder.textView_likes.setText(list.get(position).aggregateLikes+" Likes");
        holder.textView_servings.setText(list.get(position).servings+ " Servings");
        holder.textView_time.setText(list.get(position).readyInMinutes+" Minutes");
        Picasso.get().load(list.get(position).image).into(holder.imageView_dish);

        holder.list_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onRecipeClicked(String.valueOf(list.get(holder.getAdapterPosition()).id));
            }
        });

        holder.imageView_addToFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView favouriteImage = holder.imageView_addToFavourite;
                Drawable currentImage = favouriteImage.getDrawable();
                Drawable checkedImage = holder.checked;

                if (currentImage.getConstantState().equals(checkedImage.getConstantState())) {
                    Toast.makeText(v.getContext(),String.format("%s was removed from favourites", holder.textView_title.getText().toString()),Toast.LENGTH_SHORT).show();
                    holder.imageView_addToFavourite.setImageResource(R.drawable.ic_add_to_favourite);
                    removeElementFromFavouriteRecipes(SystemStorage.getCurrentUID(), String.valueOf(list.get(holder.getAdapterPosition()).id), FirebaseDatabase.getInstance());
                } else {
                    Toast.makeText(v.getContext(),String.format("%s was added to favourites", holder.textView_title.getText().toString()),Toast.LENGTH_SHORT).show();
                    holder.imageView_addToFavourite.setImageResource(R.drawable.ic_added_to_favourites);

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference ref = database.getReference("favourite_recipes");

                    ref.push().setValue(new FavouriteRecipe(list.get(holder.getAdapterPosition()), SystemStorage.getCurrentUID()));
                }
            }
            public void removeElementFromFavouriteRecipes(String uid, String recipeId, FirebaseDatabase database) {
                DatabaseReference ref = database.getReference("favourite_recipes");

                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if (snapshot.child("uid").getValue().equals(uid) && String.valueOf(snapshot.child("recipe").child("id").getValue()).equals(recipeId)) {
                                snapshot.getRef().removeValue();
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("Firebase", "onCancelled", databaseError.toException());
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
class  RecipeViewHolder extends RecyclerView.ViewHolder{
    CardView list_container;
    TextView textView_title;
    TextView textView_servings;
    TextView textView_time;
    TextView textView_likes;
    ImageView imageView_dish;
    ImageView imageView_addToFavourite;
    Drawable checked;

    public RecipeViewHolder(@NonNull View itemView) {
        super(itemView);
        list_container = itemView.findViewById(R.id.list_container);
        textView_title = itemView.findViewById(R.id.textView_title);
        textView_servings = itemView.findViewById(R.id.textView_servings);
        textView_time = itemView.findViewById(R.id.textView_time);
        textView_likes = itemView.findViewById(R.id.textView_likes);
        imageView_dish = itemView.findViewById(R.id.imageView_dish);
        imageView_addToFavourite = itemView.findViewById(R.id.imageView_fav);

        checked = itemView.getContext().getResources().getDrawable(R.drawable.ic_added_to_favourites);
    }
}
