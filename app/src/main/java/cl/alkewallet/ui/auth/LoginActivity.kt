package cl.alkewallet.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import cl.alkewallet.appContainer
import cl.alkewallet.databinding.ActivityLoginBinding
import cl.alkewallet.ui.main.MainActivity
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels { appContainer().viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Gestión de sesión: si ya hay un token guardado se entra directo a la app.
        if (viewModel.isLoggedIn()) {
            openMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            viewModel.login(
                binding.etEmail.text.toString(),
                binding.etPassword.text.toString()
            )
        }
        binding.tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        viewModel.loading.observe(this) { loading ->
            binding.progress.isVisible = loading
            binding.btnLogin.isEnabled = !loading
        }
        viewModel.errors.observe(this) { errors ->
            binding.tilEmail.error = errors.email
            binding.tilPassword.error = errors.password
        }
        viewModel.message.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
        viewModel.authenticated.observe(this) { event ->
            if (event.getContentIfNotHandled() != null) openMain()
        }
    }

    private fun openMain() {
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }
}
