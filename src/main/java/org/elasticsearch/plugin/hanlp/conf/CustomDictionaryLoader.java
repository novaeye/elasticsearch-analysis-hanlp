/*
 * CustomDictionaryLoader.java
 * 创建日期 2024年10月29日
 * 创 建 人 LIANG
 */
package org.elasticsearch.plugin.hanlp.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.Settings;

import com.hankcs.hanlp.dictionary.CustomDictionary;

/**
 * CustomDictionaryLoader
 *
 * @author LIANG
 * @since 2024年10月29日
 */
public enum CustomDictionaryLoader {
    ;

    private static final int    DEFAULT_BUFFER_SIZE = 2 << 14;

    private static final Logger LOGGER              = ESLoggerFactory.getLogger(CustomDictionaryLoader.class);

    public static void loadAll(Settings settings, Path configPath, String dictionariesPath) {
        final File dictionariesDir = configPath.resolve(dictionariesPath).toFile();
        if (dictionariesDir.exists() && dictionariesDir.isDirectory() && dictionariesDir.canRead()) {
            final File[] dictFiles = dictionariesDir.listFiles(CustomDictionaryLoader::filterTextFile);
            if (dictFiles == null || dictFiles.length == 0) {
                return;
            }
            for (File dictFile : dictFiles) {
                if (isUTF8TextFile(dictFile)) {
                    try {
                        LOGGER.info(String.format("加载字典: %s", dictFile.toPath().toString()));
                        final AtomicInteger counter = new AtomicInteger(0);
                        Files.lines(dictFile.toPath(), StandardCharsets.UTF_8).forEach(item -> {
                            CustomDictionary.add(item);
                            counter.incrementAndGet();
                        });
                        LOGGER.info(String.format("加载字典完成(共 %d 项): %s", counter.get(), dictFile.toPath().toString()));
                    } catch (IOException e) {
                        LOGGER.error(String.format("加载字典失败: %s", dictFile.toPath().toString()), e);
                    }
                } else {
                    LOGGER.warn(String.format("无效字典文件(不是文本文件或不是UTF-8编码): %s", dictFile.toPath().toString()));
                }
            }
        }
    }

    private static boolean filterTextFile(File dir, String name) {
        return name.toLowerCase().endsWith(".txt");
    }

    private static boolean isUTF8TextFile(File file) {
        try (InputStream in = new FileInputStream(file)) {
            return detectCharset(in, StandardCharsets.UTF_8) == StandardCharsets.UTF_8;
        } catch (IOException ioe) {
            //just ignore
        }
        return false;
    }

    private static Charset detectCharset(InputStream in, Charset... charsets) throws IOException {
        if (charsets == null || charsets.length == 0) {
            return null;
        }

        final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        while (in.read(buffer) > -1) {
            for (Charset charset : charsets) {
                final CharsetDecoder decoder = charset.newDecoder();
                if (identifyCharset(buffer, decoder)) {
                    return charset;
                }
            }
        }
        return null;
    }

    private static boolean identifyCharset(byte[] bytes, CharsetDecoder decoder) {
        try {
            decoder.decode(ByteBuffer.wrap(bytes));
        } catch (CharacterCodingException e) {
            return false;
        }
        return true;
    }
}
