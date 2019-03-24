package org.elasticsearch.plugin.hanlp.conf;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.io.ByteArray;
import com.hankcs.hanlp.corpus.io.IOUtil;
import com.hankcs.hanlp.corpus.io.ResourceIOAdapter;
import com.hankcs.hanlp.dictionary.stopword.StopWordDictionary;
import com.hankcs.hanlp.model.perceptron.utility.IOUtility;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.Viterbi.ViterbiSegment;
import com.hankcs.hanlp.utility.TextUtility;

/**
 * elasticsearch-analysis-hanlp
 * elasticsearch-analysis-hanlp
 * Created by hezl on 2018-12-03.
 */
public class ConfigHelper {

    private static final Logger     logger            = Loggers.getLogger(ConfigHelper.class, "ConfigHelper");

    public static final HanLPConfig INDEX_CONFIG      = new HanLPConfig();
    public static final HanLPConfig SMART_CONFIG      = new HanLPConfig();
    public static final HanLPConfig NLP_CONFIG        = new HanLPConfig();
    public static final HanLPConfig PERCEPTRON_CONFIG = new HanLPConfig();
    public static final HanLPConfig CRF_CONFIG        = new HanLPConfig();
    static {
        //INDEX_CONFIG
        INDEX_CONFIG.setAlgorithm("viterbi");
        INDEX_CONFIG.setEnableIndexMode(true);
        //CustomDic
        INDEX_CONFIG.setCustomDictionaryPath("");
        INDEX_CONFIG.setEnableCustomDictionary(true);
        INDEX_CONFIG.setEnableCustomDictionaryForcing(false);
        //StopWord
        INDEX_CONFIG.setEnableStopWord(false);
        INDEX_CONFIG.setStopWordDictionaryPath("");
        //NLP
        INDEX_CONFIG.setEnableNameRecognize(true);
        INDEX_CONFIG.setEnableJapaneseNameRecognize(false);
        INDEX_CONFIG.setEnableTranslatedNameRecognize(false);
        INDEX_CONFIG.setEnableNumberQuantifierRecognize(true);
        INDEX_CONFIG.setEnableOrganizationRecognize(false);
        INDEX_CONFIG.setEnablePlaceRecognize(false);
        INDEX_CONFIG.setEnableTraditionalChineseMode(false);

        //SMART_CONFIG
        SMART_CONFIG.setAlgorithm("viterbi");
        SMART_CONFIG.setEnableIndexMode(false);
        //CustomDic
        SMART_CONFIG.setCustomDictionaryPath("");
        SMART_CONFIG.setEnableCustomDictionary(true);
        SMART_CONFIG.setEnableCustomDictionaryForcing(false);
        //StopWord
        SMART_CONFIG.setEnableStopWord(false);
        SMART_CONFIG.setStopWordDictionaryPath("");
        //NLP
        SMART_CONFIG.setEnableNameRecognize(true);
        SMART_CONFIG.setEnableJapaneseNameRecognize(false);
        SMART_CONFIG.setEnableTranslatedNameRecognize(false);
        SMART_CONFIG.setEnableNumberQuantifierRecognize(true);
        SMART_CONFIG.setEnableOrganizationRecognize(false);
        SMART_CONFIG.setEnablePlaceRecognize(false);
        SMART_CONFIG.setEnableTraditionalChineseMode(false);

        //HLP_CONFIG
        NLP_CONFIG.setAlgorithm("viterbi");
        NLP_CONFIG.setEnableIndexMode(false);
        //CustomDic
        NLP_CONFIG.setCustomDictionaryPath("");
        NLP_CONFIG.setEnableCustomDictionary(true);
        NLP_CONFIG.setEnableCustomDictionaryForcing(false);
        //StopWord
        NLP_CONFIG.setEnableStopWord(false);
        NLP_CONFIG.setStopWordDictionaryPath("");
        //NLP
        NLP_CONFIG.setEnableNameRecognize(true);
        NLP_CONFIG.setEnableJapaneseNameRecognize(true);
        NLP_CONFIG.setEnableTranslatedNameRecognize(true);
        NLP_CONFIG.setEnableNumberQuantifierRecognize(true);
        NLP_CONFIG.setEnableOrganizationRecognize(true);
        NLP_CONFIG.setEnablePlaceRecognize(true);
        NLP_CONFIG.setEnableTraditionalChineseMode(false);

        PERCEPTRON_CONFIG.setAlgorithm("perceptron");
        PERCEPTRON_CONFIG.setEnableIndexMode(false);
        //CustomDic
        PERCEPTRON_CONFIG.setCustomDictionaryPath("");
        PERCEPTRON_CONFIG.setEnableCustomDictionary(true);
        PERCEPTRON_CONFIG.setEnableCustomDictionaryForcing(false);
        //StopWord
        PERCEPTRON_CONFIG.setEnableStopWord(false);
        PERCEPTRON_CONFIG.setStopWordDictionaryPath("");
        //NLP
        PERCEPTRON_CONFIG.setEnableNameRecognize(true);
        PERCEPTRON_CONFIG.setEnableJapaneseNameRecognize(false);
        PERCEPTRON_CONFIG.setEnableTranslatedNameRecognize(false);
        PERCEPTRON_CONFIG.setEnableNumberQuantifierRecognize(true);
        PERCEPTRON_CONFIG.setEnableOrganizationRecognize(true);
        PERCEPTRON_CONFIG.setEnablePlaceRecognize(true);
        PERCEPTRON_CONFIG.setEnableTraditionalChineseMode(false);

        //CRF_CONFIG
        CRF_CONFIG.setAlgorithm("crf");
        CRF_CONFIG.setEnableIndexMode(false);
        //CustomDic
        CRF_CONFIG.setCustomDictionaryPath("");
        CRF_CONFIG.setEnableCustomDictionary(true);
        CRF_CONFIG.setEnableCustomDictionaryForcing(false);
        //StopWord
        CRF_CONFIG.setEnableStopWord(false);
        CRF_CONFIG.setStopWordDictionaryPath("");
        //NLP
        CRF_CONFIG.setEnableNameRecognize(true);
        CRF_CONFIG.setEnableJapaneseNameRecognize(false);
        CRF_CONFIG.setEnableTranslatedNameRecognize(false);
        CRF_CONFIG.setEnableNumberQuantifierRecognize(true);
        CRF_CONFIG.setEnableOrganizationRecognize(true);
        CRF_CONFIG.setEnablePlaceRecognize(true);
        CRF_CONFIG.setEnableTraditionalChineseMode(false);

    }

    public static Segment getSegment(HanLPConfig config) {
        //SpecialPermission.check();
        return AccessController.doPrivileged((PrivilegedAction<Segment>) () -> {
            Segment segment;
            String algorithm = config.getAlgorithm();
            if ("crf".equals(algorithm) || "条件随机场".equals(algorithm) || "perceptron".equals(algorithm)
                            || "感知机".equals(algorithm)) {
                if (HanLP.Config.IOAdapter instanceof ResourceIOAdapter) {
                    return null;
                }
            }
            if ("viterbi".equals(algorithm) || "维特比".equals(algorithm)) {
                String customDictionaryPath = config.getCustomDictionaryPath();
                if (TextUtility.isBlank(customDictionaryPath)) {
                    segment = new ViterbiSegment();
                } else {
                    segment = new ViterbiSegment(customDictionaryPath);
                }
            } else {
                segment = HanLP.newSegment(algorithm);
            }
            segment.enableIndexMode(config.isEnableIndexMode())
                .enableCustomDictionary(config.isEnableCustomDictionary())
                .enableCustomDictionaryForcing(config.isEnableCustomDictionaryForcing())
                .enableNameRecognize(config.isEnableNameRecognize())
                .enableJapaneseNameRecognize(config.isEnableJapaneseNameRecognize())
                .enableTranslatedNameRecognize(config.isEnableTranslatedNameRecognize())
                .enableNumberQuantifierRecognize(config.isEnableNumberQuantifierRecognize())
                .enableOrganizationRecognize(config.isEnableOrganizationRecognize())
                .enablePlaceRecognize(config.isEnablePlaceRecognize())
                .enableTranslatedNameRecognize(config.isEnableTraditionalChineseMode())
                .enableOffset(true)
                .enablePartOfSpeechTagging(true);
            if (logger.isDebugEnabled()) {
                logger.debug(segment.seg("HanLP中文分词工具包！"));
            }
            return segment;
        });
    }

    public static Set<String> getStopWords(HanLPConfig config) {
        if (!config.isEnableStopWord()) {
            return null;
        }
        String filePath = config.getStopWordDictionaryPath();
        if (TextUtility.isBlank(filePath)) {
            filePath = HanLP.Config.CoreStopWordDictionaryPath;
        }
        final String cfPath = filePath;
        try {
            //SpecialPermission.check();
            String cachedPath = (cfPath.startsWith("/") ? cfPath : "/" + cfPath) + ".bin";
            InputStream cachedResource = IOUtil.getResourceAsStream(cachedPath);
            if (cachedResource != null) {
                StopWordDictionary stopwordDict = new StopWordDictionary();
                stopwordDict.load(new ByteArray(IOUtility.readBytesFromOtherInputStream(cachedResource)));
                return stopwordDict.getAllStrings();
            }
            byte[] bytes = AccessController.doPrivileged((PrivilegedAction<byte[]>) () -> {
                byte[] bs;
                if (IOUtil.isResource(cfPath)) {
                    try {
                        bs = IOUtil.readBytesFromResource(cfPath);
                    } catch (IOException e) {
                        return new byte[0];
                    }
                } else {
                    bs = IOUtil.readBytes(cfPath);
                }
                return bs;
            });
            if (bytes == null || bytes.length <= 0) {
                return null;
            }
            Set<String> resultSet = new HashSet<>();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            InputStreamReader reader = new InputStreamReader(byteArrayInputStream);
            BufferedReader br = new BufferedReader(reader);
            String str;
            while ((str = br.readLine()) != null) {
                resultSet.add(str);
            }
            br.close();
            reader.close();
            byteArrayInputStream.close();
            return resultSet;
        } catch (Exception ex) {
            logger.error("", ex);
        }
        return null;
    }

    public static HanLPConfig getConfig(Settings settings) {
        HanLPConfig config = new HanLPConfig();
        config.setAlgorithm(settings.get("algorithm", "viterbi"));
        config.setEnableIndexMode(settings.getAsBoolean("enableIndexMode", false));
        //CustomDic
        config.setCustomDictionaryPath(settings.get("customDictionaryPath", ""));
        config.setEnableCustomDictionary(settings.getAsBoolean("enableCustomDictionary", true));
        config.setEnableCustomDictionaryForcing(settings.getAsBoolean("enableCustomDictionaryForcing", false));
        //StopWord
        config.setEnableStopWord(settings.getAsBoolean("enableStopWord", false));
        config.setStopWordDictionaryPath(settings.get("stopWordDictionaryPath", ""));
        //NLP
        config.setEnableNameRecognize(settings.getAsBoolean("enableNameRecognize", true));
        config.setEnableJapaneseNameRecognize(settings.getAsBoolean("enableJapaneseNameRecognize", false));
        config.setEnableTranslatedNameRecognize(settings.getAsBoolean("enableTranslatedNameRecognize", false));
        config.setEnableNumberQuantifierRecognize(settings.getAsBoolean("enableNumberQuantifierRecognize", true));
        config.setEnableOrganizationRecognize(settings.getAsBoolean("enableOrganizationRecognize", false));
        config.setEnablePlaceRecognize(settings.getAsBoolean("enablePlaceRecognize", false));
        config.setEnableTraditionalChineseMode(settings.getAsBoolean("enableTraditionalChineseMode", false));

        return config;
    }

    public static Tuple<Segment, Set<String>> getSegmentAndFilter(Settings settings) {
        HanLPConfig config = getConfig(settings);
        return Tuple.tuple(getSegment(config), getStopWords(config));
    }
}
