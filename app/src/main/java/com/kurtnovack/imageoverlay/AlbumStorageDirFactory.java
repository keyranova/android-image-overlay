package com.kurtnovack.imageoverlay;

import java.io.File;

/**
 * Created by kurt on 3/7/15.
 */
abstract class AlbumStorageDirFactory {
    public abstract File getAlbumStorageDir(String albumName);
}
