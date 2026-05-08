package com.example.myapplication;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

/** Assignment 4 host activity with dynamic fragment replacements in fragment1 container. */
public class Activity3 extends AppCompatActivity
        implements Activity3ListFragment.Host,
        Activity3FormFragment.Host,
        Activity3DetailFragment.Host {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity3);
        BottomNavHelper.bind(this, BottomNavHelper.TAB_MENU);
        if (savedInstanceState == null) {
            showList(false);
        }
    }

    @Override
    public void showList(boolean addToBackStack) {
        navigateTo(new Activity3ListFragment(), addToBackStack);
    }

    @Override
    public void showForm(long projectId) {
        navigateTo(Activity3FormFragment.newInstance(projectId), true);
    }

    @Override
    public void showDetail(long projectId) {
        navigateTo(Activity3DetailFragment.newInstance(projectId), true);
    }

    private void navigateTo(Fragment fragment, boolean addToBackStack) {
        androidx.fragment.app.FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment1, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }
}
