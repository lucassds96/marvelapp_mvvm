package daniel.lop.io.marvelappstarter.di

import android.content.Context
import androidx.room.Room
import daniel.lop.io.marvelappstarter.data.local.MarvelDao
import daniel.lop.io.marvelappstarter.data.local.MarvelDataBase
import daniel.lop.io.marvelappstarter.data.remote.ServiceApi
import daniel.lop.io.marvelappstarter.repository.MarvelRepository
import daniel.lop.io.marvelappstarter.ui.details.DetailsCharacterViewModel
import daniel.lop.io.marvelappstarter.ui.favorite.FavoriteCharacterViewModel
import daniel.lop.io.marvelappstarter.ui.list.ListCharacterViewModel
import daniel.lop.io.marvelappstarter.ui.search.SearchCharacterViewModel
import daniel.lop.io.marvelappstarter.util.Constants
import daniel.lop.io.marvelappstarter.util.Constants.BASE_URL
import daniel.lop.io.marvelappstarter.util.Constants.DATABASE_NAME
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object Module {

    val viewModelModule = module {
        viewModel { ListCharacterViewModel(get()) }
        viewModel { DetailsCharacterViewModel(get()) }
        viewModel { FavoriteCharacterViewModel(get()) }
        viewModel { SearchCharacterViewModel(get()) }
    }

    val repositoryModule = module {
        fun provideMarvelRepository(api: ServiceApi, dao: MarvelDao): MarvelRepository{
            return MarvelRepository(api, dao)
        }
        single { provideMarvelRepository(get(), get()) }
    }

    val dataBaseModule = module {
        fun provideMarvelDatabase(context: Context) = Room.databaseBuilder(
            context, MarvelDataBase::class.java,
            DATABASE_NAME
        ).build()

        fun provideMarvelDao(dataBase: MarvelDataBase) = dataBase.marvelDao()

        single { provideMarvelDatabase( androidApplication()) }
        single { provideMarvelDao(get()) }
    }

    val netModule = module {
        fun provideOkHttpClient(): OkHttpClient {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            return OkHttpClient().newBuilder()
                .addInterceptor { chain ->
                    val currentTimeStamp = System.currentTimeMillis()
                    val newUrl = chain.request().url
                        .newBuilder()
                        .addQueryParameter(Constants.TS, currentTimeStamp.toString())
                        .addQueryParameter(Constants.API_KEY, Constants.PUBLIC_KEY)
                        .addQueryParameter(Constants.HASH,
                            provideToMd5Hash(currentTimeStamp.toString() + Constants.PRIVATE_KEY + Constants.PUBLIC_KEY)
                        )
                        .build()

                    val newRequest = chain.request()
                        .newBuilder()
                        .url(newUrl)
                        .build()
                    chain.proceed(newRequest)
                }
                .addInterceptor(logging)
                .build()
        }

        fun provideRetrofit(client: OkHttpClient): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }

        fun provideServiceApi(retrofit: Retrofit): ServiceApi {
            return retrofit.create(ServiceApi::class.java)
        }

        single { provideOkHttpClient() }
        single { provideRetrofit(get()) }
        single { provideServiceApi(get()) }
    }

    fun provideToMd5Hash(encrypted: String): String{
        var pass = encrypted
        var encryptedString: String? = null
        val md5: MessageDigest
        try {
            md5 = MessageDigest.getInstance("MD5")
            md5.update(pass.toByteArray(), 0, pass.length)
            pass = BigInteger(1, md5.digest()).toString(16)
            while (pass.length < 32){
                pass = "0$pass"
            }
            encryptedString = pass
        }catch (e1: NoSuchAlgorithmException){
            e1.printStackTrace()
        }
        Timber.d("hash -> $encryptedString")
        return encryptedString ?: ""
    }
}