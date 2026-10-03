package cl.alkewallet

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import androidx.lifecycle.ViewModelProvider
import cl.alkewallet.data.local.AppDatabase
import cl.alkewallet.data.remote.RetrofitClient
import cl.alkewallet.data.remote.WalletApi
import cl.alkewallet.data.repository.AuthRepository
import cl.alkewallet.data.repository.AuthRepositoryImpl
import cl.alkewallet.data.repository.WalletRepository
import cl.alkewallet.data.repository.WalletRepositoryImpl
import cl.alkewallet.data.session.SessionManager
import cl.alkewallet.ui.ViewModelFactory

class WalletApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/** Contenedor de dependencias manual (suficiente para este tamaño de proyecto). */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val session: SessionManager by lazy { SessionManager(appContext) }
    private val database: AppDatabase by lazy { AppDatabase.getInstance(appContext) }
    private val api: WalletApi by lazy {
        RetrofitClient.create(BuildConfig.API_BASE_URL, { session.getToken() }, BuildConfig.DEBUG)
    }

    val authRepository: AuthRepository by lazy { AuthRepositoryImpl(api, session, database) }
    val walletRepository: WalletRepository by lazy { WalletRepositoryImpl(api, database) }
    val viewModelFactory: ViewModelProvider.Factory by lazy {
        ViewModelFactory(authRepository, walletRepository)
    }
}

fun Context.appContainer(): AppContainer {
    var ctx: Context = this
    while (ctx is ContextWrapper) {
        if (ctx is WalletApp) return ctx.container
        ctx = ctx.baseContext
    }
    return (applicationContext as WalletApp).container
}
