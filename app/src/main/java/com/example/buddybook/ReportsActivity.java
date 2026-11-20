package com.example.buddybook;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

public class ReportsActivity extends BaseActivity {

    private View btnBackReports;

    private View navHome;
    private View navBuddies;
    private View navReports;
    private View navProfile;
    private View navAdd;

    private TextView tvTotalBuddiesValue;
    private TextView tvStatesCoveredValue;
    private TextView tvMalePercent;
    private TextView tvFemalePercent;

    private ProgressBar pbGender;
    private LinearLayout stateChartContainer;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        // Always use the singleton DatabaseHelper.
        dbHelper = DatabaseHelper.getInstance(getApplicationContext());

        connectViews();
        setClickListeners();
        loadReportData();
    }

    private void connectViews() {
        btnBackReports = findViewById(R.id.btnBackReports);

        tvTotalBuddiesValue = findViewById(R.id.tvTotalValue);
        tvStatesCoveredValue = findViewById(R.id.tvStatesValue);
        tvMalePercent = findViewById(R.id.tvMaleStat);
        tvFemalePercent = findViewById(R.id.tvFemaleStat);

        pbGender = findViewById(R.id.pbGenderDonut);
        stateChartContainer = findViewById(R.id.llStateChartContainer);

        navHome = findViewById(R.id.navHome);
        navBuddies = findViewById(R.id.navBuddies);
        navReports = findViewById(R.id.navReports);
        navProfile = findViewById(R.id.navProfile);
        navAdd = findViewById(R.id.navAdd);
    }

    private void loadReportData() {
        if (!hasValidSession()) {
            clearReportData();

            Toast.makeText(
                    this,
                    "Your login session is invalid. Please log in again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        int totalBuddies = dbHelper.getTotalBuddies(currentUserId);
        int statesCovered = dbHelper.getTotalStatesCovered(currentUserId);
        int maleCount = dbHelper.getGenderCountForUser(currentUserId, "Male");
        int femaleCount = dbHelper.getGenderCountForUser(currentUserId, "Female");

        if (tvTotalBuddiesValue != null) {
            tvTotalBuddiesValue.setText(String.valueOf(totalBuddies));
        }

        if (tvStatesCoveredValue != null) {
            tvStatesCoveredValue.setText(String.valueOf(statesCovered));
        }

        updateGenderStats(totalBuddies, maleCount, femaleCount);
        updateStateStats();
    }

    private boolean hasValidSession() {
        return currentUserId != null && !currentUserId.trim().isEmpty();
    }

    @SuppressLint("SetTextI18n")
    private void updateGenderStats(
            int total,
            int male,
            int female
    ) {
        int malePercent = total > 0
                ? Math.round((male * 100f) / total)
                : 0;

        int femalePercent = total > 0
                ? Math.round((female * 100f) / total)
                : 0;

        if (tvMalePercent != null) {
            tvMalePercent.setText(
                    "Male " + male + " (" + malePercent + "%)"
            );
        }

        if (tvFemalePercent != null) {
            tvFemalePercent.setText(
                    "Female " + female + " (" + femalePercent + "%)"
            );
        }

        if (pbGender != null) {
            pbGender.setMax(100);
            pbGender.setProgress(malePercent);
        }
    }

    private void updateStateStats() {
        if (stateChartContainer == null) {
            return;
        }

        stateChartContainer.removeAllViews();

        List<StateStat> stats = dbHelper.getStateCountsForUser(currentUserId);

        if (stats == null) {
            stats = Collections.emptyList();
        }

        int[] barColors = {
                R.drawable.bg_bar_maroon,
                R.drawable.bg_bar_red,
                R.drawable.bg_bar_orange,
                R.drawable.bg_bar_yellow,
                R.drawable.bg_bar_light_orange
        };

        int highestCount = 0;

        for (StateStat stat : stats) {
            if (stat != null && stat.count > highestCount) {
                highestCount = stat.count;
            }
        }

        for (int i = 0; i < stats.size(); i++) {
            StateStat stat = stats.get(i);

            if (stat == null) {
                continue;
            }

            View barItem = getLayoutInflater().inflate(
                    R.layout.item_bar,
                    stateChartContainer,
                    false
            );

            View bar = barItem.findViewById(R.id.barView);
            TextView tvLabel = barItem.findViewById(R.id.tvLabel);
            TextView tvCount = barItem.findViewById(R.id.tvCount);

            if (tvLabel != null) {
                String stateName =
                        stat.name == null || stat.name.trim().isEmpty()
                                ? "Unknown"
                                : stat.name.trim();

                tvLabel.setText(stateName);
            }

            if (tvCount != null) {
                tvCount.setText(String.valueOf(stat.count));
            }

            if (bar != null) {
                bar.setBackgroundResource(
                        barColors[i % barColors.length]
                );

                ViewGroup.LayoutParams layoutParams = bar.getLayoutParams();

                if (layoutParams != null) {
                    int maxHeightDp = 150;
                    int minimumHeightDp = stat.count > 0 ? 8 : 0;

                    int scaledHeightDp;

                    if (highestCount <= 0) {
                        scaledHeightDp = 0;
                    } else {
                        scaledHeightDp = Math.round(
                                (stat.count / (float) highestCount) * maxHeightDp
                        );

                        scaledHeightDp = Math.max(
                                minimumHeightDp,
                                scaledHeightDp
                        );
                    }

                    layoutParams.height = dpToPx(scaledHeightDp);
                    bar.setLayoutParams(layoutParams);
                }
            }

            stateChartContainer.addView(barItem);
        }
    }

    private void clearReportData() {
        if (tvTotalBuddiesValue != null) {
            tvTotalBuddiesValue.setText("0");
        }

        if (tvStatesCoveredValue != null) {
            tvStatesCoveredValue.setText("0");
        }

        if (tvMalePercent != null) {
            tvMalePercent.setText("Male 0 (0%)");
        }

        if (tvFemalePercent != null) {
            tvFemalePercent.setText("Female 0 (0%)");
        }

        if (pbGender != null) {
            pbGender.setProgress(0);
        }

        if (stateChartContainer != null) {
            stateChartContainer.removeAllViews();
        }
    }

    private void setClickListeners() {
        if (btnBackReports != null) {
            btnBackReports.setOnClickListener(v -> {
                animateOnly(v);
                finish();
            });
        }

        if (navHome != null) {
            navHome.setOnClickListener(
                    v -> animateAndOpenPage(v, HomeActivity.class)
            );
        }

        if (navBuddies != null) {
            navBuddies.setOnClickListener(
                    v -> animateAndOpenPage(v, BuddiesActivity.class)
            );
        }

        if (navReports != null) {
            navReports.setOnClickListener(this::animateOnly);
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(
                    v -> animateAndOpenPage(v, ProfileActivity.class)
            );
        }

        if (navAdd != null) {
            navAdd.setOnClickListener(
                    v -> animateAndOpenPage(v, AddBuddyActivity.class)
            );
        }
    }

    private void animateOnly(View view) {
        if (view == null) {
            return;
        }

        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(80)
                .withEndAction(() ->
                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(80)
                                .start()
                )
                .start();
    }

    private void animateAndOpenPage(
            View view,
            Class<?> destination
    ) {
        if (view == null || destination == null) {
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
                            ReportsActivity.this,
                            destination
                    );

                    startActivity(intent);

                    overridePendingTransition(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                    );
                })
                .start();
    }

    private int dpToPx(int dp) {
        return Math.round(
                dp * getResources().getDisplayMetrics().density
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (dbHelper != null) {
            loadReportData();
        }
    }
}