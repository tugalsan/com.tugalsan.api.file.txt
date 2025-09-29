package com.tugalsan.api.file.txt.server;

import module com.tugalsan.api.log;
import module com.tugalsan.api.file;
import module com.tugalsan.api.function;
import module com.tugalsan.api.list;
import module com.tugalsan.api.stream;
import module com.tugalsan.api.string;
import java.nio.charset.*;
import java.nio.file.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.*;

public class TS_FileTxtUtils {

    private TS_FileTxtUtils() {

    }

    final private static TS_Log d = TS_Log.of(TS_FileTxtUtils.class);

    public static byte[] getUTF8BOM() {
        return new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    }

    //FILE-READER----------------------------------------------------------------------
    public static String toString(Path sourceFile) {
        return toString(sourceFile, StandardCharsets.UTF_8);
    }

    public static String toString(Path sourceFile, Charset charset) {
        return TGS_FuncMTCUtils.call(() -> Files.readString(sourceFile, charset));
    }

    public static List<String> toList(Path sourceFile) {
        return toList(sourceFile, StandardCharsets.UTF_8);
    }

    public static List<String> toList(Path sourceFile, Charset charset) {
        return TGS_FuncMTCUtils.call(() -> TGS_ListUtils.of(Files.readAllLines(sourceFile, charset)));
    }

    //FILE-WRITER----------------------------------------------------------------------
    public static Path toFile(CharSequence sourceText, Path destFile, boolean append) {
        return toFile(sourceText, destFile, append, StandardCharsets.UTF_8, false, true);
    }

    public static Path toFile(CharSequence sourceText, Path destFile, boolean append, Charset charset, boolean withUTF8BOM, boolean windowsCompatable) {
        return TGS_FuncMTCUtils.call(() -> {
            TS_DirectoryUtils.createDirectoriesIfNotExists(destFile.getParent());
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
        return TGS_FuncMTCUtils.call(() -> {
            TS_DirectoryUtils.createDirectoriesIfNotExists(destFile.getParent());
            if (!append) {//DO NOT DELETE THE CODEIT IS NEEDED
                TS_FileUtils.deleteFileIfExists(destFile);
                if (TS_FileUtils.isExistFile(destFile)) {
                    TGS_FuncMTUUtils.thrw(d.className(), "toFile", "Cannot Delete File " + destFile);
                }
            }
            IntStream.range(0, sourceTexts.size()).forEachOrdered(i -> {
                var sourceText = sourceTexts.get(i);
                sourceText = sourceText.replace("\r\n", "\n");//for source normilize
                sourceText = sourceText.replace("\n", "\r\n");
                sourceTexts.set(i, sourceText);
            });
            if (withUTF8BOM) {
                sourceTexts.set(0, new String(getUTF8BOM()) + sourceTexts.get(0));
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
        TS_DirectoryUtils.createDirectoriesIfNotExists(destFile.getParent());
        var filteredSourceTexts = TGS_StreamUtils.toLst(
                IntStream.range(fromIdx, toIdx).mapToObj(i -> toString(sourceTexts.get(i), charset))
        );
        if (withUTF8BOM) {
            filteredSourceTexts.set(0, TGS_StringUtils.cmn().concat(String.valueOf(getUTF8BOM()), filteredSourceTexts.get(0)));
        }
        return toFile(filteredSourceTexts, destFile, false);
    }
}
