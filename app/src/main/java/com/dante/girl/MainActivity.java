package com.dante.girl;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.blankj.utilcode.utils.BarUtils;
import com.blankj.utilcode.utils.ToastUtils;
import com.dante.girl.base.BaseActivity;
import com.dante.girl.base.Constants;
import com.dante.girl.helper.Updater;
import com.dante.girl.lib.PopupDialogActivity;
import com.dante.girl.model.DataBase;
import com.dante.girl.picture.FavoriteFragment;
import com.dante.girl.ui.SettingFragment;
import com.dante.girl.ui.SettingsActivity;
import com.dante.girl.utils.Imager;
import com.dante.girl.utils.Share;
import com.dante.girl.utils.SpUtil;
import com.dante.girl.utils.UiUtils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.Date;

import butterknife.BindView;

import static android.support.design.widget.AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED;
import static android.support.design.widget.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
import static com.dante.girl.net.API.TYPE_A_ANIME;
import static com.dante.girl.net.API.TYPE_A_FULI;
import static com.dante.girl.net.API.TYPE_A_HENTAI;
import static com.dante.girl.net.API.TYPE_A_UNIFORM;
import static com.dante.girl.net.API.TYPE_A_YSJ;
import static com.dante.girl.net.API.TYPE_A_ZATU;
import static com.dante.girl.net.API.TYPE_DB_BREAST;
import static com.dante.girl.net.API.TYPE_DB_BUTT;
import static com.dante.girl.net.API.TYPE_DB_LEG;
import static com.dante.girl.net.API.TYPE_DB_RANK;
import static com.dante.girl.net.API.TYPE_DB_SILK;
import static com.dante.girl.net.API.TYPE_GANK;
import static com.dante.girl.net.API.TYPE_HIDE;
import static com.dante.girl.net.API.TYPE_MZ_INNOCENT;
import static com.dante.girl.net.API.TYPE_MZ_JAPAN;
import static com.dante.girl.net.API.TYPE_MZ_SEXY;
import static com.dante.girl.net.API.TYPE_MZ_TAIWAN;
import static com.dante.girl.utils.AppUtil.donate;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String MAIN_FRAGMENT_TAG = "main";
    public static final int DRAWER_CLOSE_DELAY = 230;
    private static final String TAG = "MainActivity";
    @BindView(R.id.fab)
    public FloatingActionButton fab;
    public ActionBarDrawerToggle toggle;
    @BindView(R.id.nav_view)
    NavigationView navView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.container)
    ViewGroup container;
    @BindView(R.id.reveal)
    FrameLayout revealView;
    @BindView(R.id.root)
    CoordinatorLayout root;
    private boolean backPressed;
    private MenuItem currentMenu;
    private SparseArray<Fragment> fragmentSparseArray;
    private Updater updater;
    private boolean secretMode;
    private int placeHolderHeight;

    @Override
    protected int initLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
        secretMode = SpUtil.getBoolean(SettingFragment.SECRET_MODE) ||
                DataBase.isVIP(SpUtil.getString("deviceId"));
        setupDrawer();
        initToolbar();
        updater = Updater.getInstance(this);
        updater.check();
        initNavigationView();
        initFragments(savedInstanceState);
    }

    private void initToolbar() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
            layoutParams.height = BarUtils.getActionBarHeight(this);
            toolbar.setLayoutParams(layoutParams);
        }
        initFab();
    }

    private void collapseToolbar() {
//        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(toolbar, "height", placeHolderHeight, BarUtils.getActionBarHeight(this));
//        objectAnimator.setDuration(3000);
//        objectAnimator.start();
//        toolbar.requestLayout();

        ValueAnimator animator = ValueAnimator.ofInt(placeHolderHeight, BarUtils.getActionBarHeight(this));
        animator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
            layoutParams.height = (int) animation.getAnimatedValue();
            Log.d(TAG, "collapseToolbar: " + layoutParams.height);
            toolbar.setLayoutParams(layoutParams);

        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                Log.d(TAG, "onAnimationEnd: ");
                initFab();
            }
        });
        animator.start();
    }

    private void initFab() {
        if (secretMode) {
            fab.setOnClickListener(v -> UiUtils.showSnack(fab, R.string.you_are_vip));
            root.removeView(fab);
            return;
        }
        fab.animate().setStartDelay(0)
                .setDuration(400).scaleY(1).scaleX(1).start();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            fab.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                AlertDialog alertDialog = builder.setTitle(R.string.hint)
                        .setMessage(R.string.thanks_for_donation)
                        .setPositiveButton(R.string.donate, (dialog, which) -> donate(MainActivity.this))
                        .create();
//                alertDialog.getWindow().getAttributes().windowAnimations = R.style.SlideDialog;
                alertDialog.show();
            });
            return;
        }
        //Morph transition
        fab.setOnClickListener(v -> {
            Intent login = PopupDialogActivity.getStartIntent(MainActivity.this, PopupDialogActivity.MORPH_TYPE_FAB);
            ActivityOptionsCompat options;
            options = ActivityOptionsCompat.makeSceneTransitionAnimation
                    (MainActivity.this, fab, getString(R.string.transition_morph_view));
            startActivity(login, options.toBundle());
        });
    }

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        if (currentMenu != null) {
//            outState.putInt("itemId", currentMenu.getItemId());
//        }
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        int itemId = savedInstanceState.getInt("itemId", R.id.nav_beauty);
//        MenuItem currentMenu = navView.getMenu().findItem(itemId);
//        navView.setCheckedItem(itemId);
//        if (currentMenu != null) {
//            onNavigationItemSelected(currentMenu);
//        }
//    }

    private void initFragments(Bundle savedInstanceState) {
        if (fragmentSparseArray == null) {
            String[] titles, types;
            fragmentSparseArray = new SparseArray<>();
            String[] all = getResources().getStringArray(R.array.db_titles);
//            secretMode = true;
            Log.d(TAG, "initFragments: secret " + secretMode + " " + SpUtil.getString("deviceId"));
            if (secretMode) {
                //Gank & Douban
                titles = all;
                types = new String[]{TYPE_GANK, TYPE_DB_RANK, TYPE_DB_BREAST, TYPE_DB_BUTT, TYPE_DB_LEG, TYPE_DB_SILK};
            } else {
                titles = new String[]{all[0], getString(R.string.hide)};
                types = new String[]{TYPE_GANK, TYPE_HIDE};
            }
            fragmentSparseArray.put(R.id.nav_beauty, MainTabsFragment.newInstance(titles, types));

            //二次元
            all = getResources().getStringArray(R.array.a_titles);
            if (secretMode) {
                titles = all;
                types = new String[]{TYPE_A_ANIME, TYPE_A_FULI, TYPE_A_HENTAI, TYPE_A_UNIFORM, TYPE_A_ZATU, TYPE_A_YSJ};
            } else {
                titles = new String[]{all[0], getString(R.string.hide)};
                types = new String[]{TYPE_A_ANIME, TYPE_HIDE};
            }
            fragmentSparseArray.put(R.id.nav_a, MainTabsFragment.newInstance(titles, types));
//妹子图
            all = getResources().getStringArray(R.array.mz_titles);
            if (secretMode) {
                titles = all;
                types = new String[]{TYPE_MZ_INNOCENT, TYPE_MZ_SEXY, TYPE_MZ_JAPAN, TYPE_MZ_TAIWAN};
            } else {
                titles = new String[]{all[0], getString(R.string.hide)};
                types = new String[]{TYPE_MZ_INNOCENT, TYPE_HIDE};
            }
            fragmentSparseArray.put(R.id.nav_mz, MainTabsFragment.newInstance(titles, types));


            //favorite
            fragmentSparseArray.put(R.id.nav_favorite, new FavoriteFragment());
        }
        setMainFragment(R.id.nav_beauty, fragmentSparseArray, savedInstanceState == null);
    }

    private void setupDrawer() {
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
            return;
        }
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            return;
        }
        doublePressBackToQuit();
    }

    private void doublePressBackToQuit() {
        if (backPressed) {
            super.onBackPressed();
            return;
        }
        backPressed = true;
        UiUtils.showSnack(root, R.string.leave_app);
        new Handler().postDelayed(() -> backPressed = false, 2000);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initNavigationView() {
//        Colorful.config(this)
//                .translucent(true)
//                .apply();

        //load headerView's image
        ImageView head = navView.getHeaderView(0).findViewById(R.id.headimage);
        Imager.load(this, R.drawable.head, head);
        head.setOnClickListener(new View.OnClickListener() {
            int index;
            long now;
            long lastTime;

            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: " + now + " - " + lastTime);
                now = new Date().getTime();
                if (now - lastTime < 500) {
                    if (index < 3) {
                        index++;
                    } else {
                        ToastUtils.showShortToast(R.string.head_view_hint);
                    }
                }
                lastTime = now;
            }
        });
        navView.setNavigationItemSelectedListener(this);
        boolean isSecretOn = SpUtil.getBoolean(SettingFragment.SECRET_MODE);
        navView.inflateMenu(R.menu.menu_main);
//        navView.setCheckedItem(R.id.nav_beauty);
        Menu menu = navView.getMenu();
        menu.getItem(0).setChecked(true);
        menu.getItem(0).setIcon(new IconicsDrawable(this).
                icon(GoogleMaterial.Icon.gmd_home)
                .color(ContextCompat.getColor(this, R.color.pink)));

        menu.getItem(1).setIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_border_color));

        menu.getItem(2).setIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_face)
                .color(Color.RED));
        menu.getItem(3).setIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_favorite)
                .color(Color.RED));

        Menu sub = menu.getItem(4).getSubMenu();
        sub.getItem(0).setIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_share)
                .color(Color.DKGRAY));
        sub.getItem(1).setIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_settings)
                .color(Color.GRAY));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_setting:
                new Handler().postDelayed(() -> {
                    changeDrawer(false);
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                }, DRAWER_CLOSE_DELAY);
                break;
            case R.id.nav_share:
                String text = SpUtil.get(Updater.SHARE_APP, getString(R.string.share_app_description));
                Share.shareText(this, text);
                break;
            default:
                currentMenu = item;
                setToolbarTitle(getCurrentMenuTitle());
                switchMenu(id, fragmentSparseArray);
                break;
        }
        drawerLayout.closeDrawers();
        return true;
    }


    public void changeNavigator(boolean enable) {
        if (toggle == null) return;
        if (enable) {
            toggle.setDrawerIndicatorEnabled(true);
        } else {
            toggle.setDrawerIndicatorEnabled(false);
            toggle.setToolbarNavigationClickListener(v -> onBackPressed());
        }
    }

    public void toggleToolbarFlag(boolean scroll) {
        AppBarLayout.LayoutParams p = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        if (scroll) {
            p.setScrollFlags(SCROLL_FLAG_SCROLL | SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED);
        } else {
            p.setScrollFlags(0);
        }
        toolbar.setLayoutParams(p);
    }

    public String getCurrentMenuTitle() {
        if (currentMenu == null) {
            currentMenu = navView.getMenu().getItem(0);
        }
        return currentMenu.getTitle().toString();
    }

    @Override
    protected void onDestroy() {
        updater.release();
        if (isMoreThanDaysOf(3) && SpUtil.getBoolean("regularly_clear")) {
            DataBase.clearAllImages();
            SpUtil.save(Constants.CLEAR_DATE, new Date().getTime());
        }
        super.onDestroy();
    }

    public boolean isMoreThanDaysOf(int days) {
        long now = new Date().getTime();
        if (SpUtil.getLong(Constants.CLEAR_DATE) <= 0) {
            SpUtil.save(Constants.CLEAR_DATE, now);
        }
        return now - SpUtil.getLong(Constants.CLEAR_DATE) > days * 24 * millisOfHour();
    }

    public int millisOfHour() {
        return 1000 * 60 * 60;
    }


    Intent get(Class clz) {
        return new Intent(getApplicationContext(), clz);
    }

    public void changeDrawer(boolean enable) {
        drawerLayout.setDrawerLockMode(enable ?
                DrawerLayout.LOCK_MODE_UNLOCKED : LOCK_MODE_LOCKED_CLOSED);
    }

}
