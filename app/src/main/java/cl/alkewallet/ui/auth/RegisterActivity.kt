package cl.alkewallet.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import cl.alkewallet.appContainer
import cl.alkewallet.databinding.ActivityRegisterBinding
import cl.alkewallet.ui.main.MainActivity
import com.google.android.material.snackbar.Snackbar

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels { appContainer().viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            viewModel.register(
                binding.etUsername.text.toString(),
                binding.etEmail.text.toString(),
                binding.etPassword.text.toString(),
                binding.etConfirmPassword.text.toString()
            )
        }
        binding.tvGoLogin.setOnClickListener { finish() }

        viewModel.loading.observe(this) { loading ->
            binding.progress.isVisible = loading
            binding.btnRegister.isEnabled = !loading
        }
        viewModel.errors.observe(this) { errors ->
            binding.tilUsername.error = errors.username
            binding.tilEmail.error = errors.email
            binding.tilPassword.error = errors.password
            binding.tilConfirmPassword.error = errors.confirmPassword
        }
        viewModel.message.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
        viewModel.authenticated.observe(this) { event ->
            if (event.getContentIfNotHandled() != null) {
                startActivity(
                    Intent(this, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
                finish()
            }
        }
    }
}
