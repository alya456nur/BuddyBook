package com.example.buddybook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

public final class ImageStorageUtils {

    private static final String TAG = "ImageStorageUtils";
    private static final String IMAGE_FOLDER = "profile_images";

    private static final int MAX_IMAGE_SIZE = 1200;
    private static final int JPEG_QUALITY = 88;

    private ImageStorageUtils() {
    }

    /**
     * Copies a selected gallery picture into app-private internal storage.
     * Store the returned absolute path in SQLite.
     */
    public static String saveImageToInternalStorage(
            Context context,
            Uri sourceUri,
            String filePrefix
    ) {
        if (context == null || sourceUri == null) {
            return null;
        }

        File directory = new File(
                context.getFilesDir(),
                IMAGE_FOLDER
        );

        if (!directory.exists() && !directory.mkdirs()) {
            Log.e(TAG, "Could not create internal picture directory.");
            return null;
        }

        String safePrefix = TextUtils.isEmpty(filePrefix)
                ? "picture"
                : filePrefix.replaceAll(
                "[^A-Za-z0-9_-]",
                "_"
        );

        File destination = new File(
                directory,
                safePrefix + "_"
                        + UUID.randomUUID()
                        + ".jpg"
        );

        try {
            Bitmap bitmap = decodeScaledBitmap(
                    context,
                    sourceUri
            );

            if (bitmap == null) {
                Log.e(TAG, "The selected picture could not be decoded.");
                return null;
            }

            try (FileOutputStream outputStream =
                         new FileOutputStream(destination)) {

                boolean saved = bitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        JPEG_QUALITY,
                        outputStream
                );

                outputStream.flush();

                if (!saved) {
                    deleteFileQuietly(destination);
                    return null;
                }
            } finally {
                bitmap.recycle();
            }

            return destination.getAbsolutePath();

        } catch (Exception exception) {
            Log.e(
                    TAG,
                    "The selected picture could not be stored.",
                    exception
            );

            deleteFileQuietly(destination);
            return null;
        }
    }

    /**
     * Loads a permanently stored picture as a true circular bitmap.
     *
     * It also supports old content:// and file:// values that may already
     * exist in the database.
     */
    public static void loadCircularIntoImageView(
            Context context,
            ImageView imageView,
            String storedValue,
            @DrawableRes int fallbackDrawable
    ) {
        if (context == null || imageView == null) {
            return;
        }

        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(0, 0, 0, 0);

        Bitmap bitmap = loadBitmap(
                context,
                storedValue
        );

        if (bitmap != null) {
            Bitmap circularBitmap =
                    createCircularBitmap(bitmap);

            imageView.setImageBitmap(circularBitmap);

            if (circularBitmap != bitmap) {
                bitmap.recycle();
            }

            return;
        }

        /*
         * A vector fallback is kept centred with small padding so that the
         * default person icon does not fill the entire circle.
         */
        imageView.setImageResource(fallbackDrawable);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setPadding(
                dpToPx(context, 8),
                dpToPx(context, 8),
                dpToPx(context, 8),
                dpToPx(context, 8)
        );
    }

    /**
     * Normal non-circular loader, useful for preview screens if required.
     */
    public static void loadIntoImageView(
            Context context,
            ImageView imageView,
            String storedValue,
            @DrawableRes int fallbackDrawable
    ) {
        if (context == null || imageView == null) {
            return;
        }

        Bitmap bitmap = loadBitmap(
                context,
                storedValue
        );

        imageView.setPadding(0, 0, 0, 0);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(fallbackDrawable);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setPadding(
                    dpToPx(context, 8),
                    dpToPx(context, 8),
                    dpToPx(context, 8),
                    dpToPx(context, 8)
            );
        }
    }

    /**
     * Deletes only pictures copied into this app's private picture folder.
     * It never deletes a user's original gallery picture.
     */
    public static boolean deleteStoredImage(
            Context context,
            String storedValue
    ) {
        if (context == null || TextUtils.isEmpty(storedValue)) {
            return false;
        }

        try {
            File ownedDirectory =
                    new File(
                            context.getFilesDir(),
                            IMAGE_FOLDER
                    ).getCanonicalFile();

            File candidate =
                    new File(storedValue).getCanonicalFile();

            if (!candidate.getPath().startsWith(
                    ownedDirectory.getPath()
                            + File.separator
            )) {
                return false;
            }

            return !candidate.exists()
                    || candidate.delete();

        } catch (Exception exception) {
            Log.w(
                    TAG,
                    "Could not delete stored picture.",
                    exception
            );

            return false;
        }
    }

    private static Bitmap loadBitmap(
            Context context,
            String storedValue
    ) {
        if (TextUtils.isEmpty(storedValue)) {
            return null;
        }

        try {
            File file = new File(storedValue);

            if (file.exists() && file.isFile()) {
                return BitmapFactory.decodeFile(
                        file.getAbsolutePath()
                );
            }

            Uri uri = Uri.parse(storedValue);

            try (InputStream inputStream =
                         context.getContentResolver()
                                 .openInputStream(uri)) {

                if (inputStream != null) {
                    return BitmapFactory.decodeStream(
                            inputStream
                    );
                }
            }

        } catch (Exception exception) {
            Log.w(
                    TAG,
                    "Stored picture could not be loaded: "
                            + storedValue,
                    exception
            );
        }

        return null;
    }

    private static Bitmap createCircularBitmap(
            Bitmap source
    ) {
        if (source == null) {
            return null;
        }

        int size = Math.min(
                source.getWidth(),
                source.getHeight()
        );

        int left =
                (source.getWidth() - size) / 2;

        int top =
                (source.getHeight() - size) / 2;

        Bitmap squareBitmap = Bitmap.createBitmap(
                source,
                left,
                top,
                size,
                size
        );

        Bitmap circularBitmap = Bitmap.createBitmap(
                size,
                size,
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(circularBitmap);

        Paint paint = new Paint(
                Paint.ANTI_ALIAS_FLAG
        );

        BitmapShader shader = new BitmapShader(
                squareBitmap,
                Shader.TileMode.CLAMP,
                Shader.TileMode.CLAMP
        );

        paint.setShader(shader);

        float radius = size / 2f;

        canvas.drawCircle(
                radius,
                radius,
                radius,
                paint
        );

        if (squareBitmap != source) {
            squareBitmap.recycle();
        }

        return circularBitmap;
    }

    private static Bitmap decodeScaledBitmap(
            Context context,
            Uri sourceUri
    ) throws Exception {

        BitmapFactory.Options boundsOptions =
                new BitmapFactory.Options();

        boundsOptions.inJustDecodeBounds = true;

        try (InputStream boundsStream =
                     context.getContentResolver()
                             .openInputStream(sourceUri)) {

            BitmapFactory.decodeStream(
                    boundsStream,
                    null,
                    boundsOptions
            );
        }

        BitmapFactory.Options decodeOptions =
                new BitmapFactory.Options();

        decodeOptions.inSampleSize =
                calculateInSampleSize(
                        boundsOptions.outWidth,
                        boundsOptions.outHeight
                );

        try (InputStream imageStream =
                     context.getContentResolver()
                             .openInputStream(sourceUri)) {

            return BitmapFactory.decodeStream(
                    imageStream,
                    null,
                    decodeOptions
            );
        }
    }

    private static int calculateInSampleSize(
            int width,
            int height
    ) {
        int sampleSize = 1;

        while (width / sampleSize > MAX_IMAGE_SIZE
                || height / sampleSize > MAX_IMAGE_SIZE) {

            sampleSize *= 2;
        }

        return Math.max(
                1,
                sampleSize
        );
    }

    private static int dpToPx(
            Context context,
            int dp
    ) {
        return Math.round(
                dp * context.getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    private static void deleteFileQuietly(
            File file
    ) {
        try {
            if (file != null && file.exists()) {
                file.delete();
            }
        } catch (Exception ignored) {
        }
    }
}