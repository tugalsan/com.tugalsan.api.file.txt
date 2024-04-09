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
import com.tugalsan.api.union.client.TGS_Union;
import java.io.IOException;

public class TS_FileTxtUtils {
    
    final private static TS_Log d = TS_Log.of(TS_FileTxtUtils.class);
    
    public static byte[] getUTF8BOM() {
        return new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    }

    //FILE-READER----------------------------------------------------------------------
    public static TGS_Union<String> toString(Path sourceFile) {
        return toString(sourceFile, StandardCharsets.UTF_8);
    }
    
    public static TGS_Union<String> toString(Path sourceFile, Charset charset) {
        try {
            return TGS_Union.of(Files.readString(sourceFile, charset));
        } catch (IOException ex) {
            return TGS_Union.ofExcuse(ex);
        }
    }
    
    public static TGS_Union<List<String>> toList(Path sourceFile) {
        return toList(sourceFile, StandardCharsets.UTF_8);
    }
    
    public static TGS_Union<List<String>> toList(Path sourceFile, Charset charset) {
        try {
            return TGS_Union.of(TGS_ListUtils.of(Files.readAllLines(sourceFile, charset)));
        } catch (IOException ex) {
            return TGS_Union.ofExcuse(ex);
        }
    }

    //FILE-WRITER----------------------------------------------------------------------
    public static TGS_Union<Boolean> toFile(CharSequence sourceText, Path destFile, boolean append) {
        return toFile(sourceText, destFile, append, StandardCharsets.UTF_8, false, true);
    }
    
    public static TGS_Union<Boolean> toFile(CharSequence sourceText, Path destFile, boolean append, Charset charset, boolean withUTF8BOM, boolean windowsCompatable) {
        try {
            var sourceTextStr = sourceText.toString();
            if (!append) {
                TS_FileUtils.deleteFileIfExists(destFile);
            }
            if (windowsCompatable) {
                sourceTextStr = sourceTextStr.replace("\r\n", "\n");//for source normilize
                sourceTextStr = sourceTextStr.replace("\n", "\r\n");
            }
            var result = null != Files.writeString(destFile, withUTF8BOM ? new String(getUTF8BOM()) + sourceTextStr : sourceTextStr,
                    charset, StandardOpenOption.CREATE, append ? StandardOpenOption.APPEND : StandardOpenOption.WRITE);
            return TGS_Union.of(result);
        } catch (IOException ex) {
            return TGS_Union.ofExcuse(ex);
        }
    }
    
    public static TGS_Union<Boolean> toFile(List<String> sourceTexts, Path destFile, boolean append) {
        return toFile(sourceTexts, destFile, append, StandardCharsets.UTF_8, false);
    }
    
    public static TGS_Union<Boolean> toFile(List<String> sourceTexts, Path destFile, boolean append, Charset charset, boolean withUTF8BOM) {
        try {
            if (!append) {//DO NOT DELETE THE CODEIT IS NEEDED
                TS_FileUtils.deleteFileIfExists(destFile);
                if (TS_FileUtils.isExistFile(destFile)) {
                    return TGS_Union.ofExcuse(d.className, "toFile", "Cannot Delete File " + destFile);
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
            var result = null != Files.write(destFile, sourceTexts, charset, StandardOpenOption.CREATE, append ? StandardOpenOption.APPEND : StandardOpenOption.WRITE);
            return TGS_Union.of(result);
        } catch (IOException ex) {
            return TGS_Union.ofExcuse(ex);
        }
    }

    //FILE MERGER--------------------------------------
    public static TGS_Union<Boolean> toFile(List<Path> sourceTexts, Path destFile) {
        return toFile(sourceTexts, destFile, StandardCharsets.UTF_8);
    }
    
    public static TGS_Union<Boolean> toFile(List<Path> sourceTexts, Path destFile, Charset charset) {
        return toFile(sourceTexts, 0, sourceTexts.size(), destFile, charset, false);
    }
    
    public static TGS_Union<Boolean> toFile(List<Path> sourceTexts, int fromIdx, int toIdx, Path destFile) {
        return toFile(sourceTexts, fromIdx, toIdx, destFile, StandardCharsets.UTF_8, false);
    }
    
    public static TGS_Union<Boolean> toFile(List<Path> sourceTexts, int fromIdx, int toIdx, Path destFile, Charset charset, boolean withUTF8BOM) {
        var u_filteredSourceTexts = TGS_StreamUtils.toLst(
                IntStream.range(fromIdx, toIdx).mapToObj(i -> toString(sourceTexts.get(i), charset))
        );
        var error = u_filteredSourceTexts.stream().filter(fst -> fst.isError()).findAny().orElse(null);
        if (error != null) {
            return TGS_Union.ofExcuse(error.excuse());
        }
        var filteredSourceTexts = TGS_StreamUtils.toLst(
                u_filteredSourceTexts.stream()
                        .map(fst -> fst.value())
        );
        if (withUTF8BOM) {
            filteredSourceTexts.set(0, TGS_StringUtils.concat(String.valueOf(getUTF8BOM()), filteredSourceTexts.get(0)));
        }
        return toFile(filteredSourceTexts, destFile, false);
    }
}
