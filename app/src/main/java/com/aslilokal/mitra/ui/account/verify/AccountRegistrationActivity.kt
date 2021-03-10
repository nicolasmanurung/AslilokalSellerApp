package com.aslilokal.mitra.ui.account.verify

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.aceinteract.android.stepper.StepperNavListener
import com.aceinteract.android.stepper.StepperNavigationView
import com.aslilokal.mitra.R
import com.aslilokal.mitra.databinding.ActivityAccountRegistrationBinding


class AccountRegistrationActivity : AppCompatActivity(), StepperNavListener {
    private lateinit var binding: ActivityAccountRegistrationBinding
    private lateinit var stepper: StepperNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideProgress()

        stepper = binding.stepper

        //Toast.makeText(this, step, Toast.LENGTH_SHORT).show()

        val navController = findNavController(R.id.fragmentRegistration)
        stepper.setupWithNavController(navController)
    }

    fun nextFragment() {
        stepper.goToNextStep()
    }


    override fun onCompleted() {

    }


    override fun onStepChanged(step: Int) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        for (fragment in supportFragmentManager.fragments) {
            fragment.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun hideProgress() {
        binding.llProgressBar.progressbar.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    fun showProgress() {
        binding.llProgressBar.progressbar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }
}