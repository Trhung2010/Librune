package com.librune.app.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.librune.app.R;
import com.librune.app.ui.borrow.BorrowCartActivity;
import com.librune.app.ui.fragment.BorrowsFragment;
import com.librune.app.ui.fragment.HomeFragment;
import com.librune.app.ui.fragment.NotificationsFragment;
import com.librune.app.ui.fragment.ProfileFragment;
import com.librune.app.ui.fragment.SearchFragment;
import com.librune.app.util.BorrowCartManager;

public class MainActivity extends AppCompatActivity {

    private Button btnBorrowCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnBorrowCart = findViewById(R.id.btnBorrowCart);
        btnBorrowCart.setOnClickListener(v -> startActivity(new Intent(this, BorrowCartActivity.class)));

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected;
            int id = item.getItemId();
            if (id == R.id.nav_search) {
                selected = new SearchFragment();
            } else if (id == R.id.nav_borrows) {
                selected = new BorrowsFragment();
            } else if (id == R.id.nav_notifications) {
                selected = new NotificationsFragment();
            } else if (id == R.id.nav_profile) {
                selected = new ProfileFragment();
            } else {
                selected = new HomeFragment();
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, selected)
                    .commit();
            return true;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
    }

    public void updateCartBadge() {
        if (btnBorrowCart != null) {
            btnBorrowCart.setText("Giỏ mượn (" + BorrowCartManager.getInstance().getTotalCount() + ")");
        }
    }
}
