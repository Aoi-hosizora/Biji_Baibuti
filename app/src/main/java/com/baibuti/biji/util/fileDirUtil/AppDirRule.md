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
   
+ OCRTmp
    + 网络图片识别的临时保存
    + OCRAct
    
+ Schedule / UserId.json (local.json)
    + 课表 Json
    + ScheduleDao

+ ~~TessData~~
    + ~~文字识别的文件目录~~
    + ~~ExtractUtil~~

## 图片文件的产生记录：

+ `_PHOTO`
    + `NoteFrag (OCR)`: `takePhone()` 生成，`onActivityResult()` 删除
    + `MNoteAct (Ins)`: `takePhone()` 生成，`InsertEditedImg()` 删除
    
+ `_EDITED`
    + `IMGEditActivity`: `saveEditedImgToSdCard()` 生成
    + `NoteFrag`: `StartEditImg()` 调用，`OpenOCRAct()` 删除
    + `MNoteAct`: `StartEditImg()` 调用，`insertImagesSync()` 删除

+ `_SMALL`
    + `SDCardUtil`: `saveSmallImgToSdCard()` 生成
    + `NoteFrag`: `OpenOCRAct()` 调用
    + `MNoteAct`: `insertImagesSync()` 调用
    