package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class ShopActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        BottomNavHelper.bind(this, BottomNavHelper.TAB_SHOP);

        MaterialToolbar toolbar = findViewById(R.id.toolbarShop);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> NavUtils.navigateUpFromSameTask(this));

        String[] titles = getResources().getStringArray(R.array.shop_product_titles);
        String[] prices = getResources().getStringArray(R.array.shop_product_prices);
        String[] details = getResources().getStringArray(R.array.shop_product_details);

        RecyclerView recyclerView = findViewById(R.id.recyclerShop);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(new ShopAdapter(titles, prices, details));
    }

    private final class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.Vh> {

        private final String[] titles;
        private final String[] prices;
        private final String[] details;

        ShopAdapter(String[] titles, String[] prices, String[] details) {
            this.titles = titles;
            this.prices = prices;
            this.details = details;
        }

        @NonNull
        @Override
        public Vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View tile =
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop_tile, parent, false);
            return new Vh(tile);
        }

        @Override
        public void onBindViewHolder(@NonNull Vh holder, int position) {
            holder.title.setText(titles[position]);
            holder.price.setText(prices[position]);
            holder.detail.setText(details[position]);
            holder.buy.setOnClickListener(
                    v ->
                            Toast.makeText(
                                            ShopActivity.this,
                                            getString(R.string.shop_added_cart_format, titles[position]),
                                            Toast.LENGTH_SHORT)
                                    .show());
        }

        @Override
        public int getItemCount() {
            return titles.length;
        }

        final class Vh extends RecyclerView.ViewHolder {
            final TextView title;
            final TextView price;
            final TextView detail;
            final MaterialButton buy;

            Vh(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.textShopTitle);
                price = itemView.findViewById(R.id.textShopPrice);
                detail = itemView.findViewById(R.id.textShopDetail);
                buy = itemView.findViewById(R.id.buttonShopBuy);
            }
        }
    }
}
