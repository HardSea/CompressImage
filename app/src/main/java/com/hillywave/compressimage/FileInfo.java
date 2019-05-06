package com.hillywave.compressimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class FileInfo implements Serializable {
    private final File file;

    private String cachedName = null;
    private String cachedPath = null;
    private String cachedMimeType = null;
    private String cachedExtension = null;
    private String cachedSize = null;
    private Boolean cachedIsImage = null;
    private Boolean cachedIsPdf = null;
    private Boolean cachedIsAudio = null;
    private Boolean cachedIsVideo = null;
    private Boolean cachedIsDirectory = null;
    private Integer cachedNumberOfChildren = null;
    private String cachedImageFormat = null;
    private transient SoftReference<Bitmap> cachedBitmap;
    private boolean isSelected = false;
    private String TAG = ".CompressActivity";

    public FileInfo(File file) {
        this.file = file;
        this.cachedBitmap = new SoftReference<>(null);
    }

    public List<FileInfo> files() {
        List<FileInfo> result = new ArrayList<>();

        if (isDirectory()) {
            for (File currentFile : children()) {
                if (currentFile != null) {
                    FileInfo fileInfo = new FileInfo(currentFile);
                    result.addAll(fileInfo.files());
                }
            }
        } else {
            result.add(this);
        }

        return result;
    }

    File getFIle(){
        return file;
    }


    public ArrayList<FileInfo> getImages(){
        ArrayList<FileInfo> tempArr = new ArrayList<>();
        if (isDirectory()){
            for (File currentFile : children()){
                if (currentFile != null) {
                    FileInfo fileInfo = new FileInfo(currentFile);
                    if (fileInfo.isImage()){
                        tempArr.add(fileInfo);
                    }
                    if (fileInfo.isDirectory()){
                        if (fileInfo.hasFiles()) {
                            tempArr.addAll(fileInfo.getImages());
                        }
                    }

                }
            }
            return tempArr;
        } else {
            return tempArr;
        }
    }

    public boolean hasFiles() {
        if (isDirectory()) {
            for (File currentFile : children()) {
                if (currentFile != null) {
                    FileInfo fileInfo = new FileInfo(currentFile);
                    if (fileInfo.hasFiles()) {
                        return true;
                    }
                }
            }

            return false;
        } else {
            return true;
        }
    }

    public String name() {
        if (cachedName == null) {
            cachedName = file.getName();
        }

        return cachedName;
    }

    public Uri uri(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
            } catch (Exception e) {
                return Uri.fromFile(file);
            }
        } else {
            return Uri.fromFile(file);
        }
    }

    public String path() {
        if (cachedPath == null) {
            cachedPath = file.getAbsolutePath();
        }

        return cachedPath;
    }


    public String mimeType() {
        if (cachedMimeType == null) {
            try {
                cachedMimeType = URLConnection.guessContentTypeFromName(file.getAbsolutePath());
            } catch (Exception e) {
                cachedMimeType = "*/*";

                e.printStackTrace();
            }
        }

        return cachedMimeType;
    }

    public String getImageFormat(){
        if (cachedImageFormat == null) {
            cachedImageFormat =  path().substring(path().lastIndexOf(".") + 1);
        }

        return cachedImageFormat;
    }

    public boolean isImage() {
        if (cachedIsImage == null) {
            String mimeType = mimeType();

            cachedIsImage = (mimeType != null) && mimeType.startsWith("image/");
        }

        return cachedIsImage;
    }

    public boolean isPdf() {
        if (cachedIsPdf == null) {
            String mimeType = mimeType();

            cachedIsPdf = (mimeType != null) && mimeType.startsWith("application/pdf");
        }

        return cachedIsPdf;
    }

    public boolean isAudio() {
        if (cachedIsAudio == null) {
            String mimeType = mimeType();

            cachedIsAudio = (mimeType != null) && mimeType.startsWith("audio/");
        }

        return cachedIsAudio;
    }

    public boolean isVideo() {
        if (cachedIsVideo == null) {
            String mimeType = mimeType();

            cachedIsVideo = (mimeType != null) && mimeType.startsWith("video");
        }

        return cachedIsVideo;
    }

    public boolean isDirectory() {
        if (cachedIsDirectory == null) {
            cachedIsDirectory = file.isDirectory();
        }

        return cachedIsDirectory;
    }

    public int numberOfChildren() {
        if (cachedNumberOfChildren == null) {
            cachedNumberOfChildren = children().length;
        }

        return cachedNumberOfChildren;
    }

    private File[] children() {
        File[] children = file.listFiles();

        return (children != null) ? children : new File[0];
    }

    public String extension() {
        if (cachedExtension == null) {
            cachedExtension = "";

            String name = name();

            int index = name.lastIndexOf(".");

            if (index > -1) {
                String extension = name.substring(index + 1);

                if (extension.length() <= 4) {
                    cachedExtension = extension.toUpperCase();
                }
            }
        }

        return cachedExtension;
    }

    public String size() {
        if (cachedSize == null) {
            SpaceFormatter spaceFormatter = new SpaceFormatter();
            cachedSize = spaceFormatter.format(file.length());
        }

        return cachedSize;
    }

    public boolean hasCachedBitmap() {
        if (cachedBitmap != null)
            return (cachedBitmap.get() != null);
        else {
            this.cachedBitmap = new SoftReference<>(null);
            return (cachedBitmap.get() != null);
        }
    }

    public Bitmap bitmap(int maxSize) {
        Bitmap bitmap = cachedBitmap.get();

        if (bitmap == null) {
            String path = path();

            // decode with inJustDecodeBounds=true to check dimensions
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);

            // calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, maxSize, maxSize);
            // decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;

            bitmap = BitmapFactory.decodeFile(path, options);
            cachedBitmap = new SoftReference<>(bitmap);
        }

        return bitmap;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // raw height and width of image
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;

            // calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public boolean toggleSelection() {
        isSelected = !isSelected;

        return isSelected;
    }

    public void select(boolean value) {
        isSelected = value;
    }

    public boolean isSelected() {
        return isSelected;
    }
}
