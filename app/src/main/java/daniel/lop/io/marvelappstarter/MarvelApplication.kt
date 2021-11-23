package daniel.lop.io.marvelappstarter

import android.app.Application
import daniel.lop.io.marvelappstarter.di.Module
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MarvelApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MarvelApplication)
            modules(listOf(Module.viewModelModule,
                   Module.repositoryModule,
                   Module.dataBaseModule,
                   Module.netModule)) }
    }
}