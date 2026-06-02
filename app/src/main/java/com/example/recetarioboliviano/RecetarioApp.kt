package com.example.recetarioboliviano

import android.app.Application
import com.example.recetarioboliviano.modelo.repositorio.RecetarioRepository
import com.example.recetarioboliviano.modelo.util.Constantes
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.realtime.Realtime

/**
 * Clase Application para la aplicación Recetario Boliviano.
 */
class RecetarioApp : Application() {

    lateinit var supabaseClient: SupabaseClient
        private set

    val repository: RecetarioRepository by lazy {
        RecetarioRepository(supabaseClient)
    }

    companion object {
        private lateinit var instance: RecetarioApp

        fun getInstance(): RecetarioApp = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        supabaseClient = createSupabaseClient(
            supabaseUrl = Constantes.SUPABASE_URL,
            supabaseKey = Constantes.SUPABASE_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
            install(Realtime)
        }
    }
}
