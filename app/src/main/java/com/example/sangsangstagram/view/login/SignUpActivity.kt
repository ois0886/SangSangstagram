package com.example.sangsangstagram.view.login

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.sangsangstagram.R
import com.example.sangsangstagram.databinding.ActivitySignUpBinding
import com.google.android.material.snackbar.Snackbar

import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, SignUpActivity::class.java)
        }
    }

    lateinit var binding: ActivitySignUpBinding

    private val viewModel: SignUpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initEventListeners()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::updateUi)
            }
        }
    }

    private fun initEventListeners() {
        binding.email.addTextChangedListener {
            if (it != null) {
                viewModel.updateEmail(it.toString())
            }
        }
        binding.password.addTextChangedListener {
            if (it != null) {
                viewModel.updatePassword(it.toString())
            }
        }
        binding.signUpButton.setOnClickListener {
            viewModel.signUp()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun updateUi(uiState: SignUpUiState) {
        binding.emailInputLayout.apply {
            isErrorEnabled = uiState.showEmailError
            error = if (uiState.showEmailError) {
                context.getString(R.string.email_is_not_valid)
            } else null
        }
        binding.passwordInputLayout.apply {
            isErrorEnabled = uiState.showPasswordError
            error = if (uiState.showPasswordError) {
                context.getString(R.string.password_is_not_valid)
            } else null
        }

        if (uiState.successToSignUp) {
            Toast.makeText(this, "회원가입에 성공했습니다.", Toast.LENGTH_LONG).show()
            navigateLoginView()
        }
        if (uiState.userMessage != null) {
            showSnackBar(uiState.userMessage)
            viewModel.userMessageShown()
        }
        binding.signUpButton.apply {
            isEnabled = uiState.isInputValid && !uiState.isLoading
            setText(if (uiState.isLoading) R.string.loading else R.string.signUp)
        }
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(this, binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun navigateLoginView() {
        val intent = LoginActivity.getIntent(this).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}