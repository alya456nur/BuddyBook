package com.example.buddybook;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class HomeActivity extends BaseActivity {

    private View navBuddies;
    private View navReports;
    private View navProfile;
    private View navAdd;

    private View cardAddBuddy;
    private View cardFindBuddy;

    private TextView tvViewAll;
    private TextView tvUserName;

    private ImageView imgProfile;

    private LinearLayout cardTotalBuddies;
    private LinearLayout cardStatesCovered;
    private LinearLayout cardNewThisMonth;
    private LinearLayout recentBuddiesContainer;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbHelper = DatabaseHelper.getInstance(getApplicationContext());

        connectViews();
        setNavigationClicks();
        setQuickActionClicks();
        refreshHomeData();
    }

    private void connectViews() {
        navBuddies = findViewById(R.id.navBuddies);
        navReports = findViewById(R.id.navReports);
        navProfile = findViewById(R.id.navProfile);
        navAdd = findViewById(R.id.navAdd);

        cardAddBuddy = findViewById(R.id.cardAddBuddy);
        cardFindBuddy = findViewById(R.id.cardFindBuddy);

        recentBuddiesContainer = findViewById(R.id.recentlyAddedContainer);

        tvViewAll = findViewById(R.id.tvViewAll);
        tvUserName = findViewById(R.id.tvUserName);

        imgProfile = findViewById(R.id.imgProfile);

        cardTotalBuddies = findViewById(R.id.cardTotalBuddies);
        cardStatesCovered = findViewById(R.id.cardStatesCovered);
        cardNewThisMonth = findViewById(R.id.cardNewThisMonth);
    }

    private void setNavigationClicks() {
        if (navBuddies != null) {
            navBuddies.setOnClickListener(
                    view -> animateAndOpen(view, BuddiesActivity.class)
            );
        }

        if (navReports != null) {
            navReports.setOnClickListener(
                    view -> animateAndOpen(view, ReportsActivity.class)
            );
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(
                    view -> animateAndOpen(view, ProfileActivity.class)
            );
        }

        if (navAdd != null) {
            navAdd.setOnClickListener(
                    view -> animateAndOpen(view, AddBuddyActivity.class)
            );
        }
    }

    private void setQuickActionClicks() {
        if (cardAddBuddy != null) {
            cardAddBuddy.setOnClickListener(
                    view -> animateAndOpen(view, AddBuddyActivity.class)
            );
        }

        if (cardFindBuddy != null) {
            cardFindBuddy.setOnClickListener(
                    view -> animateAndOpen(view, BuddiesActivity.class)
            );
        }

        if (tvViewAll != null) {
            tvViewAll.setOnClickListener(
                    view -> animateAndOpen(view, BuddiesActivity.class)
            );
        }
    }

    private void refreshHomeData() {
        if (!hasValidSession()) {
            clearHomeData();
            return;
        }

        loadUserInfo();
        loadStatistics();
        populateRecentBuddiesList();
    }

    private boolean hasValidSession() {
        return currentUserId != null && !currentUserId.trim().isEmpty();
    }

    @SuppressLint("SetTextI18n")
    private void loadUserInfo() {
        User user = dbHelper.getUserProfile(currentUserId);

        if (user != null) {
            String displayName = safeText(user.getUserName()).trim();

            if (displayName.isEmpty()) {
                displayName = "User";
            }

            if (tvUserName != null) {
                tvUserName.setText(displayName + "! 👋");
            }

            if (imgProfile != null) {
                ImageStorageUtils.loadCircularIntoImageView(
                        this,
                        imgProfile,
                        user.getUserPic(),
                        R.drawable.ic_profile_person
                );
            }

        } else {
            if (tvUserName != null) {
                tvUserName.setText("User! 👋");
            }

            if (imgProfile != null) {
                ImageStorageUtils.loadCircularIntoImageView(
                        this,
                        imgProfile,
                        "",
                        R.drawable.ic_profile_person
                );
            }
        }
    }

    private void loadStatistics() {
        int totalBuddies = dbHelper.getTotalBuddies(currentUserId);
        int statesCovered = dbHelper.getTotalStatesCovered(currentUserId);
        int newBuddies = dbHelper.getNewBuddies(currentUserId);

        updateStatCard(cardTotalBuddies, totalBuddies);
        updateStatCard(cardStatesCovered, statesCovered);
        updateStatCard(cardNewThisMonth, newBuddies);
    }

    private void updateStatCard(LinearLayout card, int value) {
        if (card == null) {
            return;
        }

        TextView largestTextView = null;
        float largestTextSize = -1f;

        for (int i = 0; i < card.getChildCount(); i++) {
            View child = card.getChildAt(i);

            if (child instanceof TextView) {
                TextView textView = (TextView) child;

                if (textView.getTextSize() > largestTextSize) {
                    largestTextSize = textView.getTextSize();
                    largestTextView = textView;
                }
            }
        }

        if (largestTextView != null) {
            largestTextView.setText(String.valueOf(value));
        }
    }

    private void populateRecentBuddiesList() {
        if (recentBuddiesContainer == null) {
            return;
        }

        recentBuddiesContainer.removeAllViews();

        List<Buddy> recentBuddies = dbHelper.getRecentBuddies(currentUserId);

        if (recentBuddies == null || recentBuddies.isEmpty()) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);

        for (Buddy buddy : recentBuddies) {
            if (buddy == null) {
                continue;
            }

            View cardView = inflater.inflate(
                    R.layout.item_recent_buddy_card,
                    recentBuddiesContainer,
                    false
            );

            ImageView imgRecentBuddyPhoto =
                    cardView.findViewById(R.id.imgRecentBuddyPhoto);

            TextView tvRecentCardBuddyName =
                    cardView.findViewById(R.id.tvRecentCardBuddyName);

            TextView tvRecentCardBuddyStateTime =
                    cardView.findViewById(R.id.tvRecentCardBuddyStateTime);

            if (imgRecentBuddyPhoto != null) {
                ImageStorageUtils.loadCircularIntoImageView(
                        this,
                        imgRecentBuddyPhoto,
                        buddy.getBuddyPic(),
                        R.drawable.ic_profile_person
                );
            }

            if (tvRecentCardBuddyName != null) {
                String buddyName = safeText(buddy.getBuddyName()).trim();
                tvRecentCardBuddyName.setText(
                        buddyName.isEmpty() ? "Unnamed Buddy" : buddyName
                );
            }

            if (tvRecentCardBuddyStateTime != null) {
                tvRecentCardBuddyStateTime.setText(
                        buildRecentBuddySubtitle(buddy)
                );
            }

            cardView.setOnClickListener(
                    view -> openBuddyDetails(view, buddy.getBuddyId())
            );

            recentBuddiesContainer.addView(cardView);
        }
    }

    private String buildRecentBuddySubtitle(Buddy buddy) {
        String state = safeText(buddy.getBuddyState()).trim();
        String dateAdded = safeText(buddy.getDateAdded()).trim();

        if (state.isEmpty() && dateAdded.isEmpty()) {
            return "";
        }

        if (state.isEmpty()) {
            return "Added on " + dateAdded;
        }

        if (dateAdded.isEmpty()) {
            return state;
        }

        return state + " • Added on " + dateAdded;
    }

    private void openBuddyDetails(View view, String buddyId) {
        if (buddyId == null || buddyId.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Unable to open this buddy.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        view.animate()
                .scaleX(0.98f)
                .scaleY(0.98f)
                .setDuration(80)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(80)
                            .start();

                    Intent intent = new Intent(
                            HomeActivity.this,
                            BuddyDetailsActivity.class
                    );

                    // BuddyDetailsActivity loads all current values from SQLite.
                    intent.putExtra("buddy_id", buddyId);
                    intent.putExtra("is_owner_profile", false);

                    startActivity(intent);

                    overridePendingTransition(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                    );
                })
                .start();
    }

    private void animateAndOpen(
            View view,
            Class<?> destinationActivity
    ) {
        if (view == null || destinationActivity == null) {
            return;
        }

        view.animate()
                .scaleX(0.94f)
                .scaleY(0.94f)
                .setDuration(80)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(80)
                            .start();

                    Intent intent = new Intent(
                            HomeActivity.this,
                            destinationActivity
                    );

                    startActivity(intent);

                    overridePendingTransition(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                    );
                })
                .start();
    }

    private void clearHomeData() {
        if (tvUserName != null) {
            tvUserName.setText("User! 👋");
        }

        updateStatCard(cardTotalBuddies, 0);
        updateStatCard(cardStatesCovered, 0);
        updateStatCard(cardNewThisMonth, 0);

        if (recentBuddiesContainer != null) {
            recentBuddiesContainer.removeAllViews();
        }
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (dbHelper != null) {
            refreshHomeData();
        }
    }
}