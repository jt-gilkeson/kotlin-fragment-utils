/*
 * Copyright (C) 2018 J.T. Gilkeson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jt.kotlinfragmentutils

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.support.v4.app.Fragment
import android.util.Log
import android.view.MenuItem

private const val TAG = "FragmentWrapperActivity"
private const val TITLE = "fwaActivityTitle"
private const val THEME = "fwaActivityTheme"
private const val FRAGMENT_NAME = "fwaFragmentName"
private const val FRAGMENT_TAG = "fwaFragmentTag"

/**
 * Basic Activity that handles displaying a custom fragment.
 *
 * The fragment can intercept action bar's home/up menu item via onOptionsItemSelected() and
 * can also intercept onBackPressed() by implementing BackPressedListener.
 */
open class FragmentWrapperActivity : AppCompatActivity() {
    companion object {
        /**
         *  Creates an Intent to launch the FragmentWrapperActivity (or specified derived Activity)
         *  with the arguments supplied.  It does not start the activity.  This allows
         *  the user to add additional extras before starting the activity.  It also allows the user to
         *  decide whether to startActivity or startActivityForResult with this intent.
         *
         * @param context The calling context being used to instantiate the activity.
         * @param fragmentClass The fragment class that is to be launched inside this activity.
         * @param title Optional Title for activity (String or Resource ID)
         * @param theme Optional Resource ID for activity theme
         * @param tag Optional fragment tag
         * @param activityClass Optional activity class (use for derived classes), defaults to FragmentWrapperActivity.
         * @return intent for activity
         */
        @JvmStatic @JvmOverloads
        fun newIntent(context: Context,
                      fragmentClass: Class<*>,
                      title: Title = Title.NoTitle,
                      theme: Int? = null,
                      tag: String? = null,
                      activityClass: Class<*>? = FragmentWrapperActivity::class.java) = Intent(context, activityClass).apply {
            putExtra(FRAGMENT_NAME, fragmentClass.name)
            putExtra(FRAGMENT_TAG, tag ?: fragmentClass.name)
            when (title) {
                is Title.StringTitle -> putExtra(TITLE, title.name)
                is Title.IdTitle -> putExtra(TITLE, context.getString(title.id))
                is Title.NoTitle -> {}
            }
            theme?.let { putExtra(THEME, theme) }
        }
    }

    /**
     * Called when the activity has detected the user's press of the back key. The default
     * implementation simply finishes the current activity, but you can override this to do
     * whatever you want.
     *
     * @return Return false to allow normal back key processing, true to consume it here.
    */
    var backPressedListener: (() -> Boolean)? = null

    protected lateinit var fragmentTag: String

    override fun onCreate(savedInstanceState: Bundle?) {
        val extras = intent.extras

        // set theme if specified
        extras?.getInt(THEME)?.let {
            if (it > 0) setTheme(it)
        }

        super.onCreate(savedInstanceState)

        if (extras == null || !extras.containsKey(FRAGMENT_NAME) || !extras.containsKey(FRAGMENT_TAG)) {
            Log.e(TAG, "Missing Fragment Information")
            finish()
            return
        }

        // set title if specified
        extras.getString(TITLE)?.let { title = it }

        fragmentTag = extras.getString(FRAGMENT_TAG)

        if (savedInstanceState == null) {
            val frag = Fragment.instantiate(this, extras.getString(FRAGMENT_NAME), extras)

            supportFragmentManager
                    .beginTransaction()
                    .add(android.R.id.content, frag, fragmentTag)
                    .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // See if fragment wants to handle home
        if (item.itemId == android.R.id.home &&
                supportFragmentManager.findFragmentByTag(fragmentTag).onOptionsItemSelected(item)) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        // See if fragment wants to handle back pressed
        backPressedListener?.let {
            if (it.invoke()) return
        }

        super.onBackPressed()
    }

    /**
     * Type of title for Activity (String, Resource ID, None)
     */
    sealed class Title {
        class StringTitle(val name: String) : Title()
        class IdTitle(val id: Int) : Title()
        object NoTitle : Title()
    }
}
