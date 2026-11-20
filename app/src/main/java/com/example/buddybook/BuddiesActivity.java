package com.example.buddybook;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

public class BuddiesActivity extends BaseActivity {
    private TextView chipMore;
    private TextView chipAll;
    private TextView chipSelangor;
    private TextView chipJohor;
    private TextView chipKedah;
    private TextView tvBuddyCount;
    private EditText etSearchBuddy;

    private View navHome;
    private View navReports;
    private View navProfile;
    private View navAdd;
    private View btnAddBuddyFloating;
    private LinearLayout buddiesContainer;
    private DatabaseHelper dbHelper;

    private ImageView btnGroups;

    private String currentSelectedState = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buddies);

        dbHelper = DatabaseHelper.getInstance(
                getApplicationContext()
        );

        chipAll = findViewById(R.id.chipAll);
        chipSelangor = findViewById(R.id.chipSelangor);
        chipJohor = findViewById(R.id.chipJohor);
        chipKedah = findViewById(R.id.chipKedah);
        chipMore = findViewById(R.id.chipMore);
        tvBuddyCount = findViewById(R.id.tvBuddyCount);
        etSearchBuddy = findViewById(R.id.etSearchBuddy);
        btnAddBuddyFloating = findViewById(R.id.btnAddBuddyFloating);
        btnGroups = findViewById(R.id.btnGroups);

        buddiesContainer = findViewById(R.id.buddyListContainer);

        btnGroups.setOnClickListener(v -> {
            v.animate()
                    .rotation(12f)
                    .setDuration(80)
                    .withEndAction(() -> {
                        v.animate()
                                .rotation(0f)
                                .setDuration(80)
                                .start();

                        Intent intent = new Intent(BuddiesActivity.this, MyGroupsActivity.class);
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    })
                    .start();
        });

        btnAddBuddyFloating.setOnClickListener(v ->
                animateAndOpenPage(v, AddBuddyActivity.class)
        );

        // Chip Clicks
        chipAll.setOnClickListener(v -> {
            selectChip(chipAll);
            currentSelectedState = "All";
            populateBuddiesList(currentSelectedState, etSearchBuddy.getText().toString());
        });

        chipSelangor.setOnClickListener(v -> {
            selectChip(chipSelangor);
            currentSelectedState = "Selangor";
            populateBuddiesList(currentSelectedState, etSearchBuddy.getText().toString());
        });

        chipJohor.setOnClickListener(v -> {
            selectChip(chipJohor);
            currentSelectedState = "Johor";
            populateBuddiesList(currentSelectedState, etSearchBuddy.getText().toString());
        });

        chipKedah.setOnClickListener(v -> {
            selectChip(chipKedah);
            currentSelectedState = "Kedah";
            populateBuddiesList(currentSelectedState, etSearchBuddy.getText().toString());
        });

        chipMore.setOnClickListener(v -> showStateDialog());

        // Search Engine
        etSearchBuddy.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Refilter current active state + name typed
                populateBuddiesList(currentSelectedState, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        navHome = findViewById(R.id.navHome);
        navReports = findViewById(R.id.navReports);
        navProfile = findViewById(R.id.navProfile);
        navAdd = findViewById(R.id.navAdd);

        navHome.setOnClickListener(v -> animateAndOpenPage(v, HomeActivity.class));
        navReports.setOnClickListener(v -> animateAndOpenPage(v, ReportsActivity.class));
        navProfile.setOnClickListener(v -> animateAndOpenPage(v, ProfileActivity.class));
        navAdd.setOnClickListener(v -> animateAndOpenPage(v, AddBuddyActivity.class));

        // Initial Load
        populateBuddiesList("All", "");
    }

    // =========================================================================
    // DYNAMIC FILTER
    // =========================================================================
    private void populateBuddiesList(
            String stateFilter,
            String searchQuery
    ) {
        buddiesContainer.removeAllViews();

        List<Buddy> listBuddies =
                dbHelper.getBuddiesByUserId(currentUserId);

        LayoutInflater inflater =
                LayoutInflater.from(this);

        int displayedCount = 0;

        if (listBuddies == null) {
            tvBuddyCount.setText("0 buddies found");
            return;
        }

        String cleanSearch =
                searchQuery == null
                        ? ""
                        : searchQuery.trim().toLowerCase(Locale.ROOT);

        for (Buddy buddy : listBuddies) {
            String buddyState =
                    buddy.getBuddyState() == null
                            ? ""
                            : buddy.getBuddyState();

            String buddyName =
                    buddy.getBuddyName() == null
                            ? ""
                            : buddy.getBuddyName();

            if (!stateFilter.equalsIgnoreCase("All")
                    && !buddyState.equalsIgnoreCase(stateFilter)) {
                continue;
            }

            if (!cleanSearch.isEmpty()
                    && !buddyName.toLowerCase(Locale.ROOT).contains(cleanSearch)) {
                continue;
            }

            displayedCount++;

            View cardView = inflater.inflate(
                    R.layout.item_buddy_card,
                    buddiesContainer,
                    false
            );

            ImageView imgCardBuddyPhoto =
                    cardView.findViewById(
                            R.id.imgCardBuddyPhoto
                    );

            TextView tvCardBuddyName =
                    cardView.findViewById(
                            R.id.tvCardBuddyName
                    );

            TextView tvCardBuddyStateGender =
                    cardView.findViewById(
                            R.id.tvCardBuddyStateGender
                    );

            TextView tvCardBuddyEmail =
                    cardView.findViewById(
                            R.id.tvCardBuddyEmail
                    );

            ImageStorageUtils.loadCircularIntoImageView(
                    this,
                    imgCardBuddyPhoto,
                    buddy.getBuddyPic(),
                    R.drawable.ic_profile_person
            );

            tvCardBuddyName.setText(buddyName);

            tvCardBuddyStateGender.setText(
                    buddyState
                            + " • "
                            + safeText(buddy.getBuddyGender())
            );

            tvCardBuddyEmail.setText(
                    safeText(buddy.getBuddyEmail())
            );

            cardView.setOnClickListener(v ->
                    openBuddyDetails(
                            v,
                            buddy
                    )
            );

            buddiesContainer.addView(cardView);
        }

        if (displayedCount == 1) {
            tvBuddyCount.setText("1 buddy found");
        } else {
            tvBuddyCount.setText(
                    displayedCount + " buddies found"
            );
        }
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private void animateAndOpenPage(View view, Class<?> destination) {
        view.animate()
                .scaleX(0.94f)
                .scaleY(0.94f)
                .setDuration(80)
                .withEndAction(() -> {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(80).start();
                    Intent intent = new Intent(BuddiesActivity.this, destination);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }).start();
    }

    private void openBuddyDetails(
            View view,
            Buddy buddy
    ) {
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
                            BuddiesActivity.this,
                            BuddyDetailsActivity.class
                    );
                    intent.putExtra("buddy_id", buddy.getBuddyId());
                    intent.putExtra("is_owner_profile", false);

                    startActivity(intent);

                    overridePendingTransition(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                    );
                })
                .start();
    }

    private void selectChip(TextView selectedChip) {
        resetAllChips();
        selectedChip.setBackgroundResource(R.drawable.bg_chip_active);
        selectedChip.setTextColor(Color.parseColor("#8F1747"));
        selectedChip.setTypeface(selectedChip.getTypeface(), android.graphics.Typeface.BOLD);
    }

    private void resetAllChips() {
        TextView[] chips = {chipAll, chipSelangor, chipJohor, chipKedah, chipMore};
        for (TextView chip : chips) {
            chip.setBackgroundResource(R.drawable.bg_chip_normal);
            chip.setTextColor(Color.parseColor("#756969"));
            chip.setTypeface(chip.getTypeface(), android.graphics.Typeface.NORMAL);
        }
        chipMore.setText("More⌄");
    }

    private void showStateDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_states);
        dialog.setCancelable(true);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        int[] stateIds = {
                R.id.stateJohor, R.id.stateKedah, R.id.stateKelantan, R.id.stateMelaka,
                R.id.stateNegeriSembilan, R.id.statePahang, R.id.statePenang, R.id.statePerak,
                R.id.statePerlis, R.id.stateSabah, R.id.stateSarawak, R.id.stateSelangor,
                R.id.stateTerengganu, R.id.stateKualaLumpur, R.id.stateLabuan, R.id.statePutrajaya
        };

        for (int stateId : stateIds) {
            TextView stateView = dialog.findViewById(stateId);
            stateView.setOnClickListener(v -> {
                TextView selectedState = (TextView) v;
                resetAllChips();

                currentSelectedState = selectedState.getText().toString();
                chipMore.setText(currentSelectedState);
                chipMore.setBackgroundResource(R.drawable.bg_chip_active);
                chipMore.setTextColor(Color.parseColor("#8F1747"));
                chipMore.setTypeface(chipMore.getTypeface(), android.graphics.Typeface.BOLD);

                // Run filters with the new state alongside current search inputs
                populateBuddiesList(currentSelectedState, etSearchBuddy.getText().toString());
                dialog.dismiss();
            });
        }

        TextView btnClose = dialog.findViewById(R.id.btnCloseStateDialog);
        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();

        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.88);
            window.setAttributes(params);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateBuddiesList(currentSelectedState, etSearchBuddy.getText().toString());
    }
}
