# AppDirRule

```java
public static String SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
public static String APP_NAME = "Biji" + File.separator;
public static String SAVE_FILETYPE = "...";
```
## 程序目录的命名规则 (SAVE_FILETYPE)：

+ NoteFile
    + 笔记的默认文件保存目录
    + ToDocUtil

+ NoteImage
    + 笔记的默认图片保存目录
    + SDCardUtil

+ ~~TessData~~
    + ~~文字识别的文件目录~~
    + ~~ExtractUtil~~
