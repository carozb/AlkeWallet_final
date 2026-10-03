package cl.alkewallet.ui

import android.widget.ImageView
import cl.alkewallet.R
import com.squareup.picasso.Picasso

/** Carga de imágenes con Picasso (asíncrona, con caché y sin bloquear la UI). */
object ImageLoader {
    fun loadAvatar(view: ImageView, url: String?) {
        if (url.isNullOrBlank()) {
            Picasso.get().cancelRequest(view)
            view.setImageResource(R.drawable.ic_person)
            return
        }
        Picasso.get()
            .load(url)
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .fit()
            .centerCrop()
            .transform(CircleTransform())
            .into(view)
    }
}
