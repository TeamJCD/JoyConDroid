package com.rdapps.gamepad;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.rdapps.gamepad.fragment.FeatureSwitchFragment;
import com.rdapps.gamepad.fragment.ResettableSettingFragment;
import com.rdapps.gamepad.util.PreferenceUtils;
import java.util.List;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity
        implements FeatureSwitchFragment.FeatureSwitchListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar mainToolbar = findViewById(R.id.mainMenuToolbar);
        setSupportActionBar(mainToolbar);
        Fragment amiiboFrag = getSupportFragmentManager().findFragmentById(R.id.amiiboFeatureFrag);
        if (Objects.nonNull(amiiboFrag) && amiiboFrag instanceof FeatureSwitchFragment) {
            ((FeatureSwitchFragment) amiiboFrag).setFeatureSwitchListener(this);
        }
        showHideAmiiboSelector();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mapping_menu, menu);
        return true;
    }

    public void showHideAmiiboSelector() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment amiiboSelector = fm.findFragmentById(R.id.amiiboSelector);
        boolean amiiboEnabled = PreferenceUtils.getAmiiboEnabled(getApplicationContext());
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        if (amiiboEnabled) {
            fragmentTransaction.show(amiiboSelector);
        } else {
            fragmentTransaction.hide(amiiboSelector);
        }
        fragmentTransaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_reset) {
            reset();
            return true;
        }

        return false;
    }

    private void reset() {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = supportFragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof ResettableSettingFragment) {
                ((ResettableSettingFragment) fragment).reset();
            }
        }
    }

    @Override
    public void onChanged(boolean set) {
        showHideAmiiboSelector();
    }
}
