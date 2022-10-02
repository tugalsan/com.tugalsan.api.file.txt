package com.tugalsan.api.file.txt.server;

import java.nio.charset.*;
import java.nio.file.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.*;
import com.tugalsan.api.log.server.*;
import com.tugalsan.api.file.server.*;
import com.tugalsan.api.list.client.*;
import com.tugalsan.api.stream.client.*;
import com.tugalsan.api.string.client.*;
import com.tugalsan.api.unsafe.client.*;

public class TS_FileTxtUtils {

    final private static TS_Log d = TS_Log.of(TS_FileTxtUtils.class);

    public static byte[] getUTF8BOM() {
        return new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    }

    //BASE64
    public static String toBase64(byte[] sourceBytes) {
        return Base64.getEncoder().encodeToString(sourceBytes);
    }

    public static String toBase64(Path sourceFile) {
        return TGS_UnSafe.compile(() -> {
            var bytes = Files.readAllBytes(sourceFile);
            return TS_FileTxtUtils.toBase64(bytes);
        });
    }

    public static byte[] toByteArrayFromBase64(String sourceBase64) {
        return Base64.getDecoder().decode(sourceBase64);
    }

    public static Path toFileFromBase64(String sourceBase64, Path destFile) {
        return TGS_UnSafe.compile(() -> {
            var bytes = toByteArrayFromBase64(sourceBase64);
            return Files.write(destFile, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        });
    }

    //FILE-READER----------------------------------------------------------------------
    public static String toString(Path sourceFile) {
        return toString(sourceFile, StandardCharsets.UTF_8);
    }

    public static String toString(Path sourceFile, Charset charset) {
        return TGS_UnSafe.compile(() -> Files.readString(sourceFile, charset));
    }

    public static List<String> toList(Path sourceFile) {
        return toList(sourceFile, StandardCharsets.UTF_8);
    }

    public static List<String> toList(Path sourceFile, Charset charset) {
        return TGS_UnSafe.compile(() -> TGS_ListUtils.of(Files.readAllLines(sourceFile, charset)));
    }

    //FILE-WRITER----------------------------------------------------------------------
    public static Path toFile(CharSequence sourceText, Path destFile, boolean append) {
        return toFile(sourceText, destFile, append, StandardCharsets.UTF_8, false, true);
    }

    public static Path toFile(CharSequence sourceText, Path destFile, boolean append, Charset charset, boolean withUTF8BOM, boolean windowsCompatable) {
        return TGS_UnSafe.compile(() -> {
            var sourceTextStr = sourceText.toString();
            if (!append) {
                TS_FileUtils.deleteFileIfExists(destFile);
            }
            if (windowsCompatable) {
                sourceTextStr = sourceTextStr.replace("\r\n", "\n");//for source normilize
                sourceTextStr = sourceTextStr.replace("\n", "\r\n");
            }
            Files.writeString(destFile, withUTF8BOM ? new String(getUTF8BOM()) + sourceTextStr : sourceTextStr,
                    charset, StandardOpenOption.CREATE, append ? StandardOpenOption.APPEND : StandardOpenOption.WRITE);
            return destFile;
        });
    }

    public static Path toFile(List<String> sourceTexts, Path destFile, boolean append) {
        return toFile(sourceTexts, destFile, append, StandardCharsets.UTF_8, false);
    }

    public static Path toFile(List<String> sourceTexts, Path destFile, boolean append, Charset charset, boolean withUTF8BOM) {
        return TGS_UnSafe.compile(() -> {
            if (!append) {//DO NOT DELETE THE CODEIT IS NEEDED
                TS_FileUtils.deleteFileIfExists(destFile);
                if (TS_FileUtils.isExistFile(destFile)) {
                    TGS_UnSafe.catchMeIfUCan(d.className, "toFile", "Cannot Delete File " + destFile);
                }
            }
            IntStream.range(0, sourceTexts.size()).forEachOrdered(i -> {
                var sourceText = sourceTexts.get(i);
                sourceText = sourceText.replace("\r\n", "\n");//for source normilize
                sourceText = sourceText.replace("\n", "\r\n");
                sourceTexts.set(i, sourceText);
            });
            if (withUTF8BOM) {
                sourceTexts.set(0, getUTF8BOM() + sourceTexts.get(0));
            }
            Files.write(destFile, sourceTexts, charset, StandardOpenOption.CREATE, append ? StandardOpenOption.APPEND : StandardOpenOption.WRITE);
            return destFile;
        });
    }

    //FILE MERGER--------------------------------------
    public static Path toFile(List<Path> sourceTexts, Path destFile) {
        return toFile(sourceTexts, destFile, StandardCharsets.UTF_8);
    }

    public static Path toFile(List<Path> sourceTexts, Path destFile, Charset charset) {
        return toFile(sourceTexts, 0, sourceTexts.size(), destFile, charset, false);
    }

    public static Path toFile(List<Path> sourceTexts, int fromIdx, int toIdx, Path destFile) {
        return toFile(sourceTexts, fromIdx, toIdx, destFile, StandardCharsets.UTF_8, false);
    }

    public static Path toFile(List<Path> sourceTexts, int fromIdx, int toIdx, Path destFile, Charset charset, boolean withUTF8BOM) {
        var filteredSourceTexts = TGS_StreamUtils.toLst(
                IntStream.range(fromIdx, toIdx).mapToObj(i -> toString(sourceTexts.get(i), charset))
        );
        if (withUTF8BOM) {
            filteredSourceTexts.set(0, TGS_StringUtils.concat(String.valueOf(getUTF8BOM()), filteredSourceTexts.get(0)));
        }
        return toFile(filteredSourceTexts, destFile, false);
    }
}
