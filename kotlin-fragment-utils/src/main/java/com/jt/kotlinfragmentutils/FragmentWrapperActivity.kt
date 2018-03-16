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


open class FragmentWrapperActivity : AppCompatActivity() {
    interface FragmentNavigationListener {
        fun onBackPressed(): Boolean
        fun onHomePressed(): Boolean
    }

    companion object {
        private const val TAG = "SimpleFragmentActivity"

        private const val TITLE = "sfaActivityTitle"
        private const val THEME = "sfaActivityTheme"
        private const val FRAGMENT_NAME = "sfaFragmentName"
        private const val FRAGMENT_TAG = "sfaFragmentTag"

        /**
         *  Creates an Intent to launch the FragmentWrapperActivity (or specified inherited Activity)
         *  with the arguments supplied.  It does not start the activity.  This allows
         *  the user to add additional extras before starting the activity.  It also allows the user to
         *  decide whether to startActivity or startActivityForResult with this intent.
         *
         * @param context The calling context being used to instantiate the activity.
         * @param fragmentClass The fragment class that is to be launched inside this activity.
         * @param activityClass Optional activity class (use for inherited classes), defaults to FragmentWrapperActivity.
         */
        fun newIntent(context: Context,
                      fragmentClass: Class<*>,
                      tag: String? = null,
                      title: Any? = null,
                      theme: Int? = null,
                      activityClass: Class<*>? = FragmentWrapperActivity::class.java) = Intent(context, activityClass).apply {
            putExtra(FRAGMENT_NAME, fragmentClass.name)
            putExtra(FRAGMENT_TAG, tag ?: fragmentClass.name)
            title?.let {
                putExtra(TITLE, when (title) {
                    is Int -> context.getString(title)
                    is String -> title
                    else -> title.toString()
                })
            }
            theme?.let { putExtra(THEME, theme) }
        }
    }

    var navigationListener: FragmentNavigationListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val extras = intent.extras

        // set theme if specified
        extras?.getInt(THEME)?.let {
            if (it > 0) {
                setTheme(it)
            }
        }

        super.onCreate(savedInstanceState)

        if (extras == null || !extras.containsKey(FRAGMENT_NAME) || !extras.containsKey(FRAGMENT_TAG)) {
            Log.e(TAG, "Missing Fragment Information")
            finish()
            return
        }

        // set title if specified
        extras.getString(TITLE)?.let { title = it }

        if (savedInstanceState == null) {
            val frag = Fragment.instantiate(this, extras.getString(FRAGMENT_NAME), extras)

            supportFragmentManager
                    .beginTransaction()
                    .add(android.R.id.content, frag, extras.getString(FRAGMENT_TAG))
                    .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // See if we have a home listener
        if (item.itemId == android.R.id.home) {
            navigationListener?.let {
                if (it.onHomePressed()) return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        // See if the listener wants to handle this event
        navigationListener?.let {
            if (it.onBackPressed()) return
        }

        super.onBackPressed()
    }
}