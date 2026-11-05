package com.dwarkadhish.dwarkaspay.data.local

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.dwarkadhish.dwarkaspay.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalAudioScanner(private val context: Context) {

    suspend fun scanLocalTracks(): List<Track> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()

        val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID
        )

        // Only music tracks longer than 15 seconds to filter out short ringtones/notifications
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 15000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Unknown Track"
                    val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val album = cursor.getString(albumColumn) ?: "Unknown Album"
                    val duration = cursor.getLong(durationColumn)
                    val albumId = cursor.getLong(albumIdColumn)

                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val artworkUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    ).toString()

                    tracks.add(
                        Track(
                            id = "local_$id",
                            title = title,
                            artist = if (artist == "<unknown>") "Unknown Artist" else artist,
                            album = album,
                            durationMs = duration,
                            mediaUri = contentUri.toString(),
                            artworkUri = artworkUri,
                            genre = "Local Storage"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        tracks
    }

    suspend fun parseTrackFromUri(uri: Uri): Track? = withContext(Dispatchers.IO) {
        try {
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val title = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?: uri.lastPathSegment?.substringAfterLast('/')?.substringBeforeLast('.')
                ?: "Selected Audio"
            val artist = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?: "Local Device"
            val album = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ALBUM)
                ?: "Imported Music"
            val durationStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationStr?.toLongOrNull() ?: 180000L
            retriever.release()

            Track(
                id = "picked_${System.currentTimeMillis()}",
                title = title,
                artist = artist,
                album = album,
                durationMs = durationMs,
                mediaUri = uri.toString(),
                artworkUri = null,
                genre = "Imported Audio"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
