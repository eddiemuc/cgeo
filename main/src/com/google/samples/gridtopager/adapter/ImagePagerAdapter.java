/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.gridtopager.adapter;

import cgeo.geocaching.R;

import android.view.View;

import static com.google.samples.gridtopager.adapter.ImageData.IMAGE_DRAWABLES;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import com.google.samples.gridtopager.fragment.ImageFragment;

public class ImagePagerAdapter extends FragmentStateAdapter {

    private Map<Integer, View> imageViews = new HashMap<>();

  public ImagePagerAdapter(Fragment fragment) {
    // Note: Initialize with the child fragment manager.
    super(fragment);
  }

  @Override
  public int getItemCount() {
    return IMAGE_DRAWABLES.length;
  }

  @NonNull
  @Override
  public Fragment createFragment(int position) {
      final Fragment frag = ImageFragment.newInstance(IMAGE_DRAWABLES[position]);
      //imageViews.put(position, frag.getView().findViewById(R.id.image));
      return frag;
  }

  public View getViewFor(final int pos) {
      return imageViews.get(pos);
  }
}
