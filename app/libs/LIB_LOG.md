# 外部 LIB 添加记录

1. Apache POI

   <https://www.jianshu.com/p/8d23b7f54b8e>

   <https://blog.csdn.net/qq_21972583/article/details/82740281>

   1. docx: `poi-3.9-20121203.jar` `poi-ooxml-3.9-20121203.jar` `poi-ooxml-schemas-3.9-20121203.jar` `dom4j-1.6.1.jar` `stax-api-1.0.1.jar` `xmlbeans-2.3.0.jar`
   2. `org.apache.poi.hwpf` : `poi-scratchpad-3.9-20121203.jar`

   ```json
   buildTypes {
       release {
       minifyEnabled false
       proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
   }
   }
   packagingOptions {
       exclude 'META-INF/LICENSE'
   }
   defaultConfig {
       multiDexEnabled true
   }
   ```

2. nil

