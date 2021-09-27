/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
 *  ---
 *  Copyright (C) 2021 T-Systems International GmbH and all other contributors
 *  ---
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  ---license-end
 *
 */

package it.ministerodellasalute.verificaC19.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.observe
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import it.ministerodellasalute.verificaC19.BuildConfig
import it.ministerodellasalute.verificaC19.R
import it.ministerodellasalute.verificaC19.VerificaApplication
import it.ministerodellasalute.verificaC19.databinding.ActivityFirstBinding
import it.ministerodellasalute.verificaC19.ui.main.MainActivity
import it.ministerodellasalute.verificaC19sdk.data.VerifierRepository
import it.ministerodellasalute.verificaC19sdk.util.Utility
import it.ministerodellasalute.verificaC19sdk.model.FirstViewModel
import it.ministerodellasalute.verificaC19sdk.util.FORMATTED_DATE_LAST_SYNC
import it.ministerodellasalute.verificaC19sdk.util.TimeUtility.parseTo
import it.ministerodellasalute.verificaC19sdk.worker.LoadKeysWorker


@AndroidEntryPoint
class FirstActivity : AppCompatActivity(), View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var binding: ActivityFirstBinding
    private lateinit var shared: SharedPreferences

    private val viewModel by viewModels<FirstViewModel>()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                openQrCodeReader()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        binding = ActivityFirstBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.qrButton.setOnClickListener(this)

        val string = getString(R.string.version, BuildConfig.VERSION_NAME)
        val spannableString = SpannableString(string).also {
            it.setSpan(UnderlineSpan(), 0, it.length, 0)
            it.setSpan(StyleSpan(Typeface.BOLD), 0, it.length, 0)
        }
        binding.versionText.text = spannableString

        viewModel.getDateLastSync().let {
            binding.dateLastSyncText.text = getString(
                R.string.lastSyncDate,
                if (it == -1L) getString(R.string.notAvailable) else it.parseTo(
                    FORMATTED_DATE_LAST_SYNC
                )
            )
        }

        val lastChunk = viewModel.getLastChunk().toInt()

        binding.updateProgressBar.max = lastChunk
        Log.i("lastChunk", lastChunk.toString())

        Log.i("viewModel.getauthorizedToDownload()", viewModel.getauthorizedToDownload().toString())
        viewModel.getauthorizedToDownload().let {
            if (it == 0L) //if not authorized, show button
                binding.downloadBigFile.visibility = View.VISIBLE
            else
            {
                binding.downloadBigFile.visibility = View.GONE
            }
        }
        Log.i("viewModel.getAuthResume()", viewModel.getAuthResume().toString())
        viewModel.getAuthResume().let {
            if (it == 0.toLong()) //if not authorized, show button
                binding.resumeDownload.visibility = View.VISIBLE
            else
            {
                binding.resumeDownload.visibility = View.GONE
            }
        }

        shared = this.getSharedPreferences("dgca.verifier.app.pref", Context.MODE_PRIVATE)
        Log.i("Shared Preferences Info", shared.toString())

        viewModel.fetchStatus.observe(this) {
            if (it) {
                binding.qrButton.isEnabled = false
                binding.dateLastSyncText.text = getString(R.string.loading)
                binding.updateProgressBar.visibility = View.VISIBLE

            } else {
                binding.qrButton.isEnabled = true
                viewModel.getDateLastSync().let { date ->
                    binding.dateLastSyncText.text = getString(
                        R.string.lastSyncDate,
                        if (date == -1L) getString(R.string.notAvailable) else date.parseTo(
                            FORMATTED_DATE_LAST_SYNC
                        )
                    )
                }
                binding.updateProgressBar.visibility = View.GONE
            }
        }
        binding.privacyPolicyCard.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.dgc.gov.it/web/pn.html"))
            startActivity(browserIntent)
        }
        binding.faqCard.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.dgc.gov.it/web/faq.html"))
            startActivity(browserIntent)
        }
        binding.downloadBigFile.setOnClickListener {
            viewModel.setauthorizedToDownload()
            var verificaApplication = VerificaApplication()
            verificaApplication.setWorkManager()
        }

        binding.resumeDownload.setOnClickListener {
            viewModel.setAuthResume()
            var verificaApplication = VerificaApplication()
            verificaApplication.setWorkManager()
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED
        ) {
            createPermissionAlert()
        } else {
            openQrCodeReader()
        }
    }

    private fun createPermissionAlert() {
        try {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.privacyTitle))
            builder.setMessage(getString(R.string.privacy))
            builder.setPositiveButton(getString(R.string.next)) { dialog, which ->
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            builder.setNegativeButton(getString(R.string.back)) { dialog, which ->
            }
            val dialog = builder.create()
            dialog.show()
        } catch (e: Exception) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }


    override fun onResume() {
        super.onResume()
        viewModel.getAppMinVersion().let {
            if (Utility.versionCompare(it, BuildConfig.VERSION_NAME) > 0) {
                createForceUpdateDialog()
            }
        }
    }

    private fun openQrCodeReader() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onClick(v: View?) {
        viewModel.getDateLastSync().let {
            if (it == -1L) {
                createNoKeyAlert()
                return
            }
        }
        when (v?.id) {
            R.id.qrButton -> checkCameraPermission()
        }
    }

    private fun createNoKeyAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.noKeyAlertTitle))
        builder.setMessage(getString(R.string.noKeyAlertMessage))
        builder.setPositiveButton(getString(R.string.ok)) { dialog, which ->
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun createForceUpdateDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.updateTitle))
        builder.setMessage(getString(R.string.updateMessage))

        builder.setPositiveButton(getString(R.string.updateLabel)) { dialog, which ->
            openGooglePlay()
        }
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun openGooglePlay() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }

    override fun onStart() {
        super.onStart()
        shared.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key != null) {
            if (key == "last_downloed_chunk") {
                val lastDownloadedChunk = viewModel.getLastDownloadedChunk().toInt()
                val lastChunk = viewModel.getLastChunk().toInt()
                val singleChunkSize = viewModel.getSizeSingleChunkInByte()
                val totalChunksSize = (singleChunkSize * lastChunk) / 1024

                binding.updateProgressBar.progress = lastDownloadedChunk
                binding.chunkCount.text = "Pacchetto $lastDownloadedChunk su $lastChunk"
                binding.chunkSize.text = "${(lastDownloadedChunk * singleChunkSize)/1024}Mb su ${totalChunksSize}Mb"
                Log.i(key.toString(), viewModel.getLastDownloadedChunk().toString())
            } else if (key == "last_chunk") {
                val lastChunk = viewModel.getLastChunk().toInt()
                binding.updateProgressBar.max = lastChunk
                Log.i("lastChunk", lastChunk.toString())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        shared.unregisterOnSharedPreferenceChangeListener(this)
        //TODO: Check if last chunk is equal to last downloaded chunk. If it's so, set authToResume to 1. [DONE]
        /*if (viewModel.getLastChunk() != viewModel.getLastDownloadedChunk()) {
            Log.i("PIPPO", "PIPPO")
            viewModel.setAuthResume()
        }*/
    }
}