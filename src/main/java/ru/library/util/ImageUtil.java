package ru.library.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ImageUtil {
    public static byte[] downloadImage(String url) throws IOException {
        try (InputStream in = new URL(url).openStream()) {
            return in.readAllBytes();
        }
    }
}
