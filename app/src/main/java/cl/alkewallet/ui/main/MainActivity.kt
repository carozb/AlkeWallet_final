package cl.alkewallet.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import cl.alkewallet.R
import cl.alkewallet.appContainer
import cl.alkewallet.databinding.ActivityMainBinding
import cl.alkewallet.ui.auth.LoginActivity
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: WalletViewModel by viewModels { appContainer().viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!appContainer().session.isLoggedIn()) {
            openLogin()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> show(HomeFragment())
                R.id.nav_transactions -> show(TransactionsFragment())
                R.id.nav_send -> show(SendFragment())
                else -> return@setOnItemSelectedListener false
            }
            true
        }
        if (savedInstanceState == null) {
            binding.bottomNav.selectedItemId = R.id.nav_home
        }

        // Mensajes globales (errores, confirmaciones) y cierre de sesión
        viewModel.message.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                Snackbar.make(binding.container, it, Snackbar.LENGTH_LONG)
                    .setAnchorView(binding.bottomNav)
                    .show()
            }
        }
        viewModel.loggedOut.observe(this) { event ->
            if (event.getContentIfNotHandled() != null) openLogin()
        }
    }

    private fun show(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    private fun openLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }
}
